package com.milehighweb.riftclash.game

/** A creature card that has been played onto the board. */
class CreatureInstance(
    val instanceId: String,
    val template: CardTemplate,
    var currentAttack: Int = template.attack,
    var currentHealth: Int = template.health,
    val maxHealth: Int = template.health,
    var summoningSick: Boolean = template.keywords.contains(Keyword.CHARGE).not(),
    var hasAttackedThisTurn: Boolean = false,
) {
    val isTaunt: Boolean get() = template.keywords.contains(Keyword.TAUNT)
    val isAlive: Boolean get() = currentHealth > 0
    val canAttack: Boolean get() = isAlive && !summoningSick && !hasAttackedThisTurn

    fun readyForNewTurn() {
        summoningSick = false
        hasAttackedThisTurn = false
    }
}
