package com.milehighweb.riftclash

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milehighweb.riftclash.game.ActionResult
import com.milehighweb.riftclash.game.AiController
import com.milehighweb.riftclash.game.CardType
import com.milehighweb.riftclash.game.GameEngine
import com.milehighweb.riftclash.game.GameState
import com.milehighweb.riftclash.game.Side
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** A short pause after the AI's turn so the player can follow what it did before control returns. */
private const val AI_TURN_DELAY_MS = 600L
private const val TAG = "GameViewModel"

class GameViewModel : ViewModel() {

    private var revision = 0
    private val _snapshot = MutableStateFlow(GameSnapshot(revision, GameEngine.newGame()))
    val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    var selectedHandCardId by mutableStateOf<String?>(null)
        private set
    var selectedAttackerId by mutableStateOf<String?>(null)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set

    private val game: GameState get() = _snapshot.value.game

    fun startNewGame() {
        selectedHandCardId = null
        selectedAttackerId = null
        statusMessage = null
        revision = 0
        _snapshot.value = GameSnapshot(revision, GameEngine.newGame())
    }

    fun onHandCardTapped(cardInstanceId: String) {
        if (game.activeSide != Side.PLAYER || game.isGameOver) return
        selectedAttackerId = null
        if (selectedHandCardId == cardInstanceId) {
            selectedHandCardId = null
            statusMessage = null
            return
        }
        selectedHandCardId = cardInstanceId
        val card = game.player.hand.find { it.instanceId == cardInstanceId } ?: return
        statusMessage = when {
            card.template.type == CardType.CREATURE -> "Tap Play to summon ${card.template.name}."
            else -> card.template.description
        }
    }

    fun onCreatureTapped(creatureInstanceId: String, owner: Side) {
        if (game.activeSide != Side.PLAYER || game.isGameOver) return

        val handCardId = selectedHandCardId
        if (handCardId != null) {
            val card = game.player.hand.find { it.instanceId == handCardId }
            if (card != null && card.template.type == CardType.SPELL) {
                applyResult(GameEngine.playCard(game, Side.PLAYER, handCardId, creatureInstanceId))
                selectedHandCardId = null
                return
            }
        }

        if (owner == Side.PLAYER) {
            selectAttacker(creatureInstanceId)
        } else {
            val attackerId = selectedAttackerId
            if (attackerId == null) {
                statusMessage = "Select one of your creatures first."
                return
            }
            applyResult(GameEngine.attack(game, Side.PLAYER, attackerId, creatureInstanceId))
            selectedAttackerId = null
        }
    }

    fun onEnemyHeroTapped() {
        if (game.activeSide != Side.PLAYER || game.isGameOver) return

        if (selectedHandCardId != null) {
            playSelectedCard()
            return
        }
        val attackerId = selectedAttackerId
        if (attackerId == null) {
            statusMessage = "Select an attacker or a card first."
            return
        }
        applyResult(GameEngine.attack(game, Side.PLAYER, attackerId, null))
        selectedAttackerId = null
    }

    /** Plays the selected card with no explicit target: creatures, and spells that target the
     *  enemy hero, all enemies, or nothing (self-heal, draw) all resolve this way. */
    fun playSelectedCard() {
        val handCardId = selectedHandCardId ?: return
        applyResult(GameEngine.playCard(game, Side.PLAYER, handCardId, null))
        selectedHandCardId = null
    }

    fun endTurn() {
        if (game.activeSide != Side.PLAYER || game.isGameOver) return
        selectedHandCardId = null
        selectedAttackerId = null
        statusMessage = null
        GameEngine.endTurn(game)
        bump()
        runAiTurnIfNeeded()
    }

    private fun selectAttacker(creatureInstanceId: String) {
        val creature = game.player.board.find { it.instanceId == creatureInstanceId } ?: return
        if (!creature.canAttack) {
            statusMessage = "${creature.template.name} can't attack right now."
            selectedAttackerId = null
            return
        }
        selectedAttackerId = if (selectedAttackerId == creatureInstanceId) null else creatureInstanceId
        statusMessage = if (selectedAttackerId != null) "Tap an enemy creature or hero to attack." else null
    }

    private fun runAiTurnIfNeeded() {
        if (game.isGameOver || game.activeSide != Side.AI) return
        viewModelScope.launch {
            delay(AI_TURN_DELAY_MS)
            val logSizeBeforeAiTurn = game.log.size
            try {
                AiController.takeTurn(game)
            } catch (e: Exception) {
                // Whatever went wrong, don't strand the match on the AI's turn forever --
                // hand control back to the player so the game stays playable.
                Log.e(TAG, "AI turn threw; ending it defensively", e)
                if (game.activeSide == Side.AI) GameEngine.endTurn(game)
            }
            statusMessage = summarizeAiTurn(logSizeBeforeAiTurn)
            bump()
        }
    }

    /** Turns the AI's own engine log lines from the turn just played into a short recap,
     *  so it's visible when the AI genuinely had nothing to play rather than looking stuck. */
    private fun summarizeAiTurn(logSizeBeforeAiTurn: Int): String {
        val events = game.log.drop(logSizeBeforeAiTurn).filterNot { it.contains("begins turn") }
        if (events.isEmpty()) return "Opponent had nothing to play."
        val recap = events.joinToString(" ").replace(Regex("\\bAI\\b"), "Opponent")
        return recap
    }

    private fun applyResult(result: ActionResult) {
        statusMessage = (result as? ActionResult.Failure)?.reason
        bump()
    }

    private fun bump() {
        revision++
        _snapshot.value = GameSnapshot(revision, game)
    }
}
