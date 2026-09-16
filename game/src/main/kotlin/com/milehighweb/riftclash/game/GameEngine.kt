package com.milehighweb.riftclash.game

import java.util.concurrent.atomic.AtomicInteger

sealed class ActionResult {
    data object Success : ActionResult()
    data class Failure(val reason: String) : ActionResult()
}

private object InstanceIds {
    private val counter = AtomicInteger(0)
    fun next(): String = "inst-${counter.incrementAndGet()}"
}

/**
 * Pure game-rules engine for Rift Clash. Holds no state itself; every function takes
 * a [GameState] and mutates it in place. This keeps the engine trivially unit-testable
 * and reusable from both the human-player UI layer and [AiController].
 */
object GameEngine {

    private const val PLAYER_OPENING_HAND = 3
    private const val AI_OPENING_HAND = 4 // AI goes second, so it gets a compensating extra card.

    fun newGame(
        playerDeck: List<CardTemplate> = CardDatabase.defaultDeck(),
        aiDeck: List<CardTemplate> = CardDatabase.defaultDeck(),
    ): GameState {
        val player = PlayerState(Side.PLAYER, playerDeck.shuffled().toMutableList())
        val ai = PlayerState(Side.AI, aiDeck.shuffled().toMutableList())
        val state = GameState(player, ai)

        repeat(PLAYER_OPENING_HAND) { drawCard(state, Side.PLAYER) }
        repeat(AI_OPENING_HAND) { drawCard(state, Side.AI) }

        state.activeSide = Side.PLAYER
        state.turnNumber = 1
        startTurn(state)
        return state
    }

    fun startTurn(state: GameState) {
        if (state.isGameOver) return
        val side = state.activeSide
        state.phase = TurnPhase.MAIN
        val ps = state.stateOf(side)
        ps.maxMana = minOf(MAX_MANA, ps.maxMana + 1)
        ps.currentMana = ps.maxMana
        ps.board.forEach { it.readyForNewTurn() }
        drawCard(state, side)
        state.addLog("$side begins turn ${state.turnNumber} with ${ps.maxMana} mana.")
    }

    /** Moves the active side from the Main Phase into the Combat Phase, so its creatures can attack. */
    fun declareCombat(state: GameState, side: Side): ActionResult {
        if (state.isGameOver) return ActionResult.Failure("Game is over.")
        if (state.activeSide != side) return ActionResult.Failure("Not your turn.")
        if (state.phase != TurnPhase.MAIN) return ActionResult.Failure("Already in the Combat Phase.")
        state.phase = TurnPhase.COMBAT
        state.addLog("$side moves to the Combat Phase.")
        return ActionResult.Success
    }

    fun drawCard(state: GameState, side: Side) {
        if (state.isGameOver) return
        val ps = state.stateOf(side)
        if (ps.deck.isEmpty()) {
            ps.fatigueDamage += 1
            ps.heroHealth -= ps.fatigueDamage
            state.addLog("$side has no cards left and takes ${ps.fatigueDamage} fatigue damage.")
            checkGameOver(state)
            return
        }
        val template = ps.deck.removeAt(0)
        if (ps.hand.size >= MAX_HAND_SIZE) {
            state.addLog("$side's hand is full; ${template.name} burns.")
            return
        }
        ps.hand.add(CardInstance(instanceId = InstanceIds.next(), template = template))
    }

    fun playCard(state: GameState, side: Side, cardInstanceId: String, targetInstanceId: String? = null): ActionResult {
        if (state.isGameOver) return ActionResult.Failure("Game is over.")
        if (state.activeSide != side) return ActionResult.Failure("Not your turn.")
        if (state.phase != TurnPhase.MAIN) return ActionResult.Failure("Cards can only be played during the Main Phase.")

        val ps = state.stateOf(side)
        val cardInstance = ps.hand.find { it.instanceId == cardInstanceId }
            ?: return ActionResult.Failure("Card not in hand.")
        val template = cardInstance.template
        if (template.cost > ps.currentMana) return ActionResult.Failure("Not enough mana.")

        return when (template.type) {
            CardType.CREATURE -> {
                if (ps.board.size >= MAX_BOARD_SIZE) return ActionResult.Failure("Board is full.")
                ps.hand.remove(cardInstance)
                ps.currentMana -= template.cost
                ps.board.add(CreatureInstance(instanceId = cardInstance.instanceId, template = template))
                state.addLog("$side plays ${template.name}.")
                ActionResult.Success
            }
            CardType.SPELL -> {
                val effect = template.spellEffect ?: return ActionResult.Failure("Spell has no effect.")
                val resolution = resolveSpellTarget(state, side, effect.target, targetInstanceId)
                if (resolution is TargetResolution.Failure) return ActionResult.Failure(resolution.reason)

                ps.hand.remove(cardInstance)
                ps.currentMana -= template.cost
                applySpellEffect(state, side, effect, (resolution as TargetResolution.Success).creature)
                state.addLog("$side casts ${template.name}.")
                checkGameOver(state)
                ActionResult.Success
            }
        }
    }

    fun attack(state: GameState, side: Side, attackerInstanceId: String, targetInstanceId: String? = null): ActionResult {
        if (state.isGameOver) return ActionResult.Failure("Game is over.")
        if (state.activeSide != side) return ActionResult.Failure("Not your turn.")
        if (state.phase != TurnPhase.COMBAT) return ActionResult.Failure("Declare the Combat Phase before attacking.")

        val attackerOwner = state.stateOf(side)
        val attacker = attackerOwner.board.find { it.instanceId == attackerInstanceId }
            ?: return ActionResult.Failure("Attacker not found.")
        if (!attacker.canAttack) return ActionResult.Failure("This creature can't attack right now.")

        val defenderOwner = state.opponentOf(side)
        val tauntCreatures = defenderOwner.board.filter { it.isTaunt && it.isAlive }

        if (targetInstanceId == null) {
            if (tauntCreatures.isNotEmpty()) return ActionResult.Failure("Must attack a Taunt creature first.")
            defenderOwner.heroHealth -= attacker.currentAttack
            attacker.hasAttackedThisTurn = true
            state.addLog("$side's ${attacker.template.name} attacks the enemy hero for ${attacker.currentAttack}.")
        } else {
            val target = defenderOwner.board.find { it.instanceId == targetInstanceId }
                ?: return ActionResult.Failure("Target not found.")
            if (tauntCreatures.isNotEmpty() && !target.isTaunt) return ActionResult.Failure("Must attack a Taunt creature first.")
            target.currentHealth -= attacker.currentAttack
            attacker.currentHealth -= target.currentAttack
            attacker.hasAttackedThisTurn = true
            state.addLog("$side's ${attacker.template.name} trades blows with ${target.template.name}.")
        }

        removeDeadCreatures(state)
        checkGameOver(state)
        return ActionResult.Success
    }

    fun endTurn(state: GameState) {
        if (state.isGameOver) return
        state.activeSide = if (state.activeSide == Side.PLAYER) Side.AI else Side.PLAYER
        if (state.activeSide == Side.PLAYER) state.turnNumber += 1
        startTurn(state)
    }

    private sealed class TargetResolution {
        data class Success(val creature: CreatureInstance?) : TargetResolution()
        data class Failure(val reason: String) : TargetResolution()
    }

    private fun resolveSpellTarget(
        state: GameState,
        casterSide: Side,
        targetType: TargetType,
        targetInstanceId: String?,
    ): TargetResolution = when (targetType) {
        TargetType.NONE, TargetType.ENEMY_HERO, TargetType.ALL_ENEMY_CREATURES -> TargetResolution.Success(null)
        TargetType.ENEMY_CREATURE -> {
            val creature = state.opponentOf(casterSide).board.find { it.instanceId == targetInstanceId }
            if (creature != null) TargetResolution.Success(creature) else TargetResolution.Failure("Invalid enemy creature target.")
        }
        TargetType.FRIENDLY_CREATURE -> {
            val creature = state.stateOf(casterSide).board.find { it.instanceId == targetInstanceId }
            if (creature != null) TargetResolution.Success(creature) else TargetResolution.Failure("Invalid friendly creature target.")
        }
        TargetType.ANY_CREATURE -> {
            val creature = state.stateOf(casterSide).board.find { it.instanceId == targetInstanceId }
                ?: state.opponentOf(casterSide).board.find { it.instanceId == targetInstanceId }
            if (creature != null) TargetResolution.Success(creature) else TargetResolution.Failure("Invalid creature target.")
        }
    }

    private fun applySpellEffect(state: GameState, casterSide: Side, effect: SpellEffect, target: CreatureInstance?) {
        val caster = state.stateOf(casterSide)
        val opponent = state.opponentOf(casterSide)
        when (effect) {
            is SpellEffect.DealDamage -> when (effect.target) {
                TargetType.ENEMY_HERO -> opponent.heroHealth -= effect.amount
                TargetType.ALL_ENEMY_CREATURES -> opponent.board.forEach { it.currentHealth -= effect.amount }
                else -> target?.let { it.currentHealth -= effect.amount }
            }
            is SpellEffect.Heal -> caster.heroHealth = minOf(MAX_HERO_HEALTH, caster.heroHealth + effect.amount)
            is SpellEffect.Buff -> target?.let {
                it.currentAttack += effect.attack
                it.currentHealth += effect.health
            }
            is SpellEffect.DrawCards -> repeat(effect.amount) { drawCard(state, casterSide) }
        }
        removeDeadCreatures(state)
    }

    private fun removeDeadCreatures(state: GameState) {
        listOf(state.player, state.ai).forEach { ps ->
            val dead = ps.board.filter { !it.isAlive }
            dead.forEach { state.addLog("${it.template.name} dies.") }
            ps.board.removeAll(dead)
        }
    }

    fun checkGameOver(state: GameState) {
        if (state.isGameOver) return
        val playerDead = state.player.isDead
        val aiDead = state.ai.isDead
        when {
            playerDead && aiDead -> {
                state.isGameOver = true
                state.winner = null
                state.addLog("Both heroes have fallen. The match ends in a draw.")
            }
            playerDead -> {
                state.isGameOver = true
                state.winner = Side.AI
                state.addLog("The AI wins!")
            }
            aiDead -> {
                state.isGameOver = true
                state.winner = Side.PLAYER
                state.addLog("You win!")
            }
        }
    }
}
