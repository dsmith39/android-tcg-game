package com.milehighweb.riftclash.game

/**
 * A simple, deterministic heuristic opponent. It is intentionally not perfect play:
 * it plays affordable cards greedily (expensive first), avoids bad trades when a
 * clearly better option exists, and goes face when it can't do anything smarter.
 */
object AiController {

    fun takeTurn(state: GameState) {
        check(state.activeSide == Side.AI) { "It is not the AI's turn." }
        playCards(state)
        if (!state.isGameOver) GameEngine.declareCombat(state, Side.AI)
        performAttacks(state)
        GameEngine.endTurn(state)
    }

    private fun playCards(state: GameState) {
        var madeProgress = true
        while (madeProgress && !state.isGameOver) {
            madeProgress = false
            val ai = state.ai
            val candidates = ai.hand
                .filter { it.template.cost <= ai.currentMana }
                .sortedByDescending { it.template.cost }

            for (cardInstance in candidates) {
                val template = cardInstance.template
                if (template.type == CardType.CREATURE && ai.board.size >= MAX_BOARD_SIZE) continue

                val targetId = pickSpellTarget(state, template)
                val needsTarget = template.spellEffect?.target in setOf(
                    TargetType.ENEMY_CREATURE,
                    TargetType.FRIENDLY_CREATURE,
                    TargetType.ANY_CREATURE,
                )
                if (needsTarget && targetId == null) continue

                val result = GameEngine.playCard(state, Side.AI, cardInstance.instanceId, targetId)
                if (result is ActionResult.Success) {
                    madeProgress = true
                    break
                }
            }
        }
    }

    private fun pickSpellTarget(state: GameState, template: CardTemplate): String? {
        val effect = template.spellEffect ?: return null
        return when {
            effect is SpellEffect.Silence ->
                // Only worth casting on a creature that actually has something to strip.
                state.player.board.filter { it.isAlive && it.keywords.isNotEmpty() }
                    .maxByOrNull { it.currentAttack + it.currentHealth }?.instanceId
            effect is SpellEffect.Heal && effect.target == TargetType.FRIENDLY_CREATURE ->
                // Only worth casting on a creature that's actually missing health.
                state.ai.board.filter { it.isAlive && it.currentHealth < it.maxHealth }
                    .minByOrNull { it.currentHealth }?.instanceId
            effect.target == TargetType.ENEMY_CREATURE ->
                state.player.board.filter { it.isAlive }.minByOrNull { it.currentHealth }?.instanceId
            effect.target == TargetType.FRIENDLY_CREATURE ->
                state.ai.board.filter { it.isAlive }.maxByOrNull { it.currentAttack }?.instanceId
            effect.target == TargetType.ANY_CREATURE ->
                (state.player.board + state.ai.board).filter { it.isAlive }.minByOrNull { it.currentHealth }?.instanceId
            else -> null
        }
    }

    private fun performAttacks(state: GameState) {
        val ai = state.ai
        val attackers = ai.board.filter { it.canAttack }
        for (attacker in attackers) {
            if (state.isGameOver || !attacker.canAttack) continue

            val enemyTaunts = state.player.board.filter { it.isTaunt && it.isAlive }
            val target = if (enemyTaunts.isNotEmpty()) {
                enemyTaunts.minByOrNull { it.currentHealth }
            } else {
                chooseBestTarget(attacker, state.player)
            }
            GameEngine.attack(state, Side.AI, attacker.instanceId, target?.instanceId)
        }
    }

    /** Returns the enemy creature to attack, or null to attack the hero directly. */
    private fun chooseBestTarget(attacker: CreatureInstance, opponent: PlayerState): CreatureInstance? {
        if (attacker.currentAttack >= opponent.heroHealth) return null // lethal: go face

        return opponent.board
            .filter { it.isAlive }
            // A Divine Shield absorbs the whole hit instead of taking damage, so this attack
            // wouldn't actually kill it -- don't mistake it for a safe, lethal trade.
            .filter { !it.hasDivineShield && attacker.currentAttack >= it.currentHealth && it.currentAttack < attacker.currentHealth }
            .maxByOrNull { it.currentAttack } // trade up into the biggest threat we can kill safely
    }
}
