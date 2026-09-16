package com.milehighweb.riftclash.game

/**
 * The active side's progress through its own turn. Every turn starts in [MAIN], where
 * cards can be played but creatures can't attack yet; declaring combat moves to
 * [COMBAT], where creatures can attack but no more cards can be played. Ending the
 * turn works from either phase and hands off to the other side, which starts fresh in [MAIN].
 */
enum class TurnPhase {
    MAIN,
    COMBAT,
}

class GameState(
    val player: PlayerState,
    val ai: PlayerState,
) {
    var turnNumber: Int = 1
    var activeSide: Side = Side.PLAYER
    var phase: TurnPhase = TurnPhase.MAIN
    var isGameOver: Boolean = false
    var winner: Side? = null
    val log: MutableList<String> = mutableListOf()

    fun stateOf(side: Side): PlayerState = if (side == Side.PLAYER) player else ai
    fun opponentOf(side: Side): PlayerState = if (side == Side.PLAYER) ai else player

    fun addLog(message: String) {
        log.add(message)
    }
}
