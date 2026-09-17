package com.milehighweb.riftclash.game

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GameEngineTest {

    @Test
    fun `card database has exactly one starter deck of forty-five cards`() {
        val deck = CardDatabase.defaultDeck()
        assertEquals(45, deck.size)
    }

    @Test
    fun `new game deals opening hands and starts player turn one with one mana`() {
        val state = GameEngine.newGame()
        assertEquals(4, state.player.hand.size) // 3 opening + 1 turn-1 draw
        assertEquals(4, state.ai.hand.size)
        assertEquals(Side.PLAYER, state.activeSide)
        assertEquals(1, state.turnNumber)
        assertEquals(1, state.player.maxMana)
        assertEquals(1, state.player.currentMana)
    }

    @Test
    fun `mana increases each of the player's turns up to the cap`() {
        val state = GameEngine.newGame()
        repeat(15) {
            GameEngine.endTurn(state) // player -> ai
            GameEngine.endTurn(state) // ai -> player
        }
        assertEquals(MAX_MANA, state.player.maxMana)
        assertEquals(MAX_MANA, state.player.currentMana)
    }

    @Test
    fun `playing a creature spends mana and puts it on the board with summoning sickness`() {
        val state = fixedState(playerHand = listOf("stone_golem"))
        state.player.currentMana = 5
        val card = state.player.hand.first()

        val result = GameEngine.playCard(state, Side.PLAYER, card.instanceId)

        assertTrue(result is ActionResult.Success)
        assertEquals(2, state.player.currentMana) // 5 - cost(3)
        assertEquals(1, state.player.board.size)
        assertFalse(state.player.board.first().canAttack)
    }

    @Test
    fun `cannot play a card without enough mana`() {
        val state = fixedState(playerHand = listOf("ancient_titan"))
        state.player.currentMana = 1
        val card = state.player.hand.first()

        val result = GameEngine.playCard(state, Side.PLAYER, card.instanceId)

        assertTrue(result is ActionResult.Failure)
        assertEquals(0, state.player.board.size)
    }

    @Test
    fun `charge creature can attack the turn it is played`() {
        val state = fixedState(playerHand = listOf("riftboar"))
        state.player.currentMana = 2
        GameEngine.playCard(state, Side.PLAYER, state.player.hand.first().instanceId)

        val attacker = state.player.board.first()
        assertTrue(attacker.canAttack)

        GameEngine.declareCombat(state, Side.PLAYER)
        val result = GameEngine.attack(state, Side.PLAYER, attacker.instanceId)
        assertTrue(result is ActionResult.Success)
        assertEquals(MAX_HERO_HEALTH - 2, state.ai.heroHealth)
    }

    @Test
    fun `cards cannot be played after combat is declared`() {
        val state = fixedState(playerHand = listOf("stone_golem"))
        state.player.currentMana = 5
        GameEngine.declareCombat(state, Side.PLAYER)

        val result = GameEngine.playCard(state, Side.PLAYER, state.player.hand.first().instanceId)

        assertTrue(result is ActionResult.Failure)
        assertEquals(0, state.player.board.size)
    }

    @Test
    fun `creatures cannot attack before combat is declared`() {
        val state = fixedState()
        val attacker = CreatureInstance(instanceId = "a", template = CardDatabase.card("embercub"), summoningSick = false)
        state.player.board.add(attacker)

        val result = GameEngine.attack(state, Side.PLAYER, attacker.instanceId)

        assertTrue(result is ActionResult.Failure)
    }

    @Test
    fun `starting a new turn resets the phase back to main`() {
        val state = fixedState()
        GameEngine.declareCombat(state, Side.PLAYER)
        assertEquals(TurnPhase.COMBAT, state.phase)

        GameEngine.endTurn(state) // player -> ai
        GameEngine.endTurn(state) // ai -> player

        assertEquals(TurnPhase.MAIN, state.phase)
    }

    @Test
    fun `must attack a taunt creature before the hero`() {
        val state = fixedState(playerHand = listOf("riftboar"))
        state.player.currentMana = 2
        GameEngine.playCard(state, Side.PLAYER, state.player.hand.first().instanceId)
        val attacker = state.player.board.first()

        val taunt = CreatureInstance(instanceId = "taunt-1", template = CardDatabase.card("shieldwall_recruit"))
        state.ai.board.add(taunt)

        GameEngine.declareCombat(state, Side.PLAYER)
        val faceResult = GameEngine.attack(state, Side.PLAYER, attacker.instanceId, targetInstanceId = null)
        assertTrue(faceResult is ActionResult.Failure)
        assertEquals(MAX_HERO_HEALTH, state.ai.heroHealth)

        val tauntResult = GameEngine.attack(state, Side.PLAYER, attacker.instanceId, targetInstanceId = taunt.instanceId)
        assertTrue(tauntResult is ActionResult.Success)
    }

    @Test
    fun `combat trades damage both ways and removes dead creatures`() {
        val state = fixedState()
        val attacker = CreatureInstance(instanceId = "a", template = CardDatabase.card("embercub"), summoningSick = false) // 3/2
        val defender = CreatureInstance(instanceId = "d", template = CardDatabase.card("sprig_scout")) // 1/1
        state.player.board.add(attacker)
        state.ai.board.add(defender)

        GameEngine.declareCombat(state, Side.PLAYER)
        GameEngine.attack(state, Side.PLAYER, attacker.instanceId, defender.instanceId)

        assertTrue(state.ai.board.isEmpty()) // defender (1 hp) died to 3 damage
        assertEquals(1, state.player.board.size) // attacker (2 hp) survived 1 damage
        assertEquals(1, state.player.board.first().currentHealth)
    }

    @Test
    fun `divine shield absorbs the first hit of damage and is then removed`() {
        val state = fixedState()
        val attacker = CreatureInstance(instanceId = "a", template = CardDatabase.card("embercub"), summoningSick = false) // 3/2
        val defender = CreatureInstance(instanceId = "d", template = CardDatabase.card("aegis_sentinel")) // 1/4, Divine Shield
        state.player.board.add(attacker)
        state.ai.board.add(defender)

        GameEngine.declareCombat(state, Side.PLAYER)
        GameEngine.attack(state, Side.PLAYER, attacker.instanceId, defender.instanceId)

        assertEquals(4, defender.currentHealth) // shield absorbed the 3 damage entirely
        assertFalse(defender.keywords.contains(Keyword.DIVINE_SHIELD)) // and was consumed
        assertEquals(2 - 1, attacker.currentHealth) // attacker still takes the defender's 1 damage back
    }

    @Test
    fun `poisonous creature kills whatever it damages in combat regardless of health`() {
        val state = fixedState()
        val attacker = CreatureInstance(instanceId = "a", template = CardDatabase.card("barbed_viper"), summoningSick = false) // 2/1, Poisonous
        val defender = CreatureInstance(instanceId = "d", template = CardDatabase.card("ironclad_guardian")) // 3/7, Taunt
        state.player.board.add(attacker)
        state.ai.board.add(defender)

        GameEngine.declareCombat(state, Side.PLAYER)
        GameEngine.attack(state, Side.PLAYER, attacker.instanceId, defender.instanceId)

        assertTrue(state.ai.board.isEmpty()) // died to poison, not its 7 health worth of damage
    }

    @Test
    fun `lifesteal heals the attacker's hero for damage dealt to the enemy hero`() {
        val state = fixedState()
        state.player.heroHealth = 20
        val attacker = CreatureInstance(instanceId = "a", template = CardDatabase.card("leech_imp"), summoningSick = false) // 1/2, Lifesteal
        state.player.board.add(attacker)

        GameEngine.declareCombat(state, Side.PLAYER)
        GameEngine.attack(state, Side.PLAYER, attacker.instanceId)

        assertEquals(MAX_HERO_HEALTH - 1, state.ai.heroHealth)
        assertEquals(21, state.player.heroHealth)
    }

    @Test
    fun `silence strips all keywords from the target creature`() {
        val state = fixedState(playerHand = listOf("silence"))
        state.player.currentMana = 1
        val taunted = CreatureInstance(instanceId = "t", template = CardDatabase.card("stone_golem"))
        state.ai.board.add(taunted)

        val result = GameEngine.playCard(state, Side.PLAYER, state.player.hand.first().instanceId, taunted.instanceId)

        assertTrue(result is ActionResult.Success)
        assertTrue(taunted.keywords.isEmpty())
        assertFalse(taunted.isTaunt)
    }

    @Test
    fun `deal damage spell hits the enemy hero`() {
        val state = fixedState(playerHand = listOf("spark_bolt"))
        state.player.currentMana = 1

        val result = GameEngine.playCard(state, Side.PLAYER, state.player.hand.first().instanceId)

        assertTrue(result is ActionResult.Success)
        assertEquals(MAX_HERO_HEALTH - 2, state.ai.heroHealth)
    }

    @Test
    fun `heal spell cannot overheal past the hero max health`() {
        val state = fixedState(playerHand = listOf("mend"))
        state.player.currentMana = 2
        state.player.heroHealth = MAX_HERO_HEALTH - 2

        GameEngine.playCard(state, Side.PLAYER, state.player.hand.first().instanceId)

        assertEquals(MAX_HERO_HEALTH, state.player.heroHealth)
    }

    @Test
    fun `an empty deck deals escalating fatigue damage instead of drawing`() {
        val state = fixedState()
        state.player.deck.clear()

        GameEngine.drawCard(state, Side.PLAYER)
        assertEquals(MAX_HERO_HEALTH - 1, state.player.heroHealth)

        GameEngine.drawCard(state, Side.PLAYER)
        assertEquals(MAX_HERO_HEALTH - 1 - 2, state.player.heroHealth)
    }

    @Test
    fun `reducing the enemy hero to zero ends the game with a winner`() {
        val state = fixedState()
        state.ai.heroHealth = 2
        val attacker = CreatureInstance(instanceId = "a", template = CardDatabase.card("embercub"), summoningSick = false)
        state.player.board.add(attacker)

        GameEngine.declareCombat(state, Side.PLAYER)
        GameEngine.attack(state, Side.PLAYER, attacker.instanceId)

        assertTrue(state.isGameOver)
        assertEquals(Side.PLAYER, state.winner)
    }

    @Test
    fun `ai takes a full turn without crashing and hands control back to the player`() {
        val state = GameEngine.newGame()
        GameEngine.endTurn(state) // hand control to the AI
        assertEquals(Side.AI, state.activeSide)

        AiController.takeTurn(state)

        assertTrue(state.isGameOver || state.activeSide == Side.PLAYER)
    }

    @Test
    fun `full simulated game between two ai-driven sides always terminates`() {
        val state = GameEngine.newGame()
        var safetyCounter = 0
        while (!state.isGameOver && safetyCounter < 500) {
            if (state.activeSide == Side.AI) {
                AiController.takeTurn(state)
            } else {
                // Drive the "player" side with the same heuristic so the match is self-playing.
                simulatePlayerTurnWithAiHeuristics(state)
            }
            safetyCounter++
        }
        assertTrue(state.isGameOver, "Game should reach a conclusion within a bounded number of turns.")
    }

    private fun simulatePlayerTurnWithAiHeuristics(state: GameState) {
        assertEquals(Side.PLAYER, state.activeSide)
        // Reuse AiController's logic by temporarily treating PLAYER as if it were AI-controlled
        // is not directly possible since it is hardcoded to Side.AI, so just end the turn immediately
        // after a minimal pass: play whatever is free, then pass.
        val player = state.player
        var progressed = true
        while (progressed) {
            progressed = false
            val playable = player.hand.filter { it.template.cost <= player.currentMana && it.template.type == CardType.CREATURE }
            for (card in playable) {
                if (player.board.size >= MAX_BOARD_SIZE) break
                if (GameEngine.playCard(state, Side.PLAYER, card.instanceId) is ActionResult.Success) {
                    progressed = true
                    break
                }
            }
        }
        if (!state.isGameOver) GameEngine.declareCombat(state, Side.PLAYER)
        for (attacker in player.board.filter { it.canAttack }) {
            if (state.isGameOver) break
            GameEngine.attack(state, Side.PLAYER, attacker.instanceId)
        }
        GameEngine.endTurn(state)
    }

    /** Builds a fresh game with empty decks and an optional fixed opening hand for the player. */
    private fun fixedState(playerHand: List<String> = emptyList()): GameState {
        val player = PlayerState(Side.PLAYER, mutableListOf())
        val ai = PlayerState(Side.AI, mutableListOf())
        val state = GameState(player, ai)
        state.activeSide = Side.PLAYER
        playerHand.forEach { id ->
            player.hand.add(CardInstance(instanceId = "hand-${id}-${player.hand.size}", template = CardDatabase.card(id)))
        }
        return state
    }
}
