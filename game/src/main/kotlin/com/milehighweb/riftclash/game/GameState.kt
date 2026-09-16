package com.milehighweb.riftclash.game

class GameState(
    val player: PlayerState,
    val ai: PlayerState,
) {
    var turnNumber: Int = 1
    var activeSide: Side = Side.PLAYER
    var isGameOver: Boolean = false
    var winner: Side? = null
    val log: MutableList<String> = mutableListOf()

    fun stateOf(side: Side): PlayerState = if (side == Side.PLAYER) player else ai
    fun opponentOf(side: Side): PlayerState = if (side == Side.PLAYER) ai else player

    fun addLog(message: String) {
        log.add(message)
    }
}
