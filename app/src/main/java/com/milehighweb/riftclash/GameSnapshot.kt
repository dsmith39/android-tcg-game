package com.milehighweb.riftclash

import com.milehighweb.riftclash.game.GameState

/**
 * [GameState] is a plain mutable class (by design, so the `:game` module stays a
 * lightweight, Compose-free, unit-testable engine). Wrapping it with a revision
 * number gives [androidx.compose.runtime.State] something that actually changes
 * identity/equality on every mutation, so Compose recomposes when the engine
 * advances the game -- reading the always-current fields off the same [game] object.
 */
data class GameSnapshot(
    val revision: Int,
    val game: GameState,
)
