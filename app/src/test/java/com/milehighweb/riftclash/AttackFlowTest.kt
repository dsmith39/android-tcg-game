package com.milehighweb.riftclash

import com.milehighweb.riftclash.game.CardDatabase
import com.milehighweb.riftclash.game.CreatureInstance
import com.milehighweb.riftclash.game.Side
import com.milehighweb.riftclash.game.TurnPhase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Exercises the exact tap sequence a player performs in the UI -- declare combat, tap
 *  a creature to select it as attacker, tap an enemy target -- through [GameViewModel]
 *  rather than [com.milehighweb.riftclash.game.GameEngine] directly. */
class AttackFlowTest {

    @Test
    fun `declaring combat then attacking the enemy hero reduces its health`() {
        val vm = GameViewModel()
        vm.startNewGame()
        val game = vm.snapshot.value.game

        game.player.board.clear()
        val attacker = CreatureInstance(instanceId = "atk1", template = CardDatabase.card("riftboar")) // charge, 2/2
        game.player.board.add(attacker)

        vm.declareCombat()
        assertEquals(TurnPhase.COMBAT, game.phase)

        vm.onCreatureTapped("atk1", Side.PLAYER)
        val aiHeroBefore = game.ai.heroHealth
        vm.onEnemyHeroTapped()

        assertEquals(aiHeroBefore - attacker.template.attack, game.ai.heroHealth)
    }

    @Test
    fun `declaring combat then attacking an enemy creature reduces its health`() {
        val vm = GameViewModel()
        vm.startNewGame()
        val game = vm.snapshot.value.game

        game.player.board.clear()
        game.ai.board.clear()
        val attacker = CreatureInstance(instanceId = "atk1", template = CardDatabase.card("riftboar")) // charge, 2/2
        val defender = CreatureInstance(instanceId = "def1", template = CardDatabase.card("stone_golem")) // 2/6
        game.player.board.add(attacker)
        game.ai.board.add(defender)

        vm.declareCombat()
        assertEquals(TurnPhase.COMBAT, game.phase)

        vm.onCreatureTapped("atk1", Side.PLAYER)
        val defenderHealthBefore = defender.currentHealth
        vm.onCreatureTapped("def1", Side.AI)

        assertEquals(defenderHealthBefore - attacker.template.attack, defender.currentHealth)
    }
}
