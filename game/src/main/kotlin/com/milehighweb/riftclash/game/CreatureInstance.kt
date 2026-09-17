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
    /** Starts as a copy of the template's keywords, but diverges from it once Silenced. */
    val keywords: MutableSet<Keyword> = template.keywords.toMutableSet(),
) {
    val isTaunt: Boolean get() = keywords.contains(Keyword.TAUNT)
    val hasDivineShield: Boolean get() = keywords.contains(Keyword.DIVINE_SHIELD)
    val isPoisonous: Boolean get() = keywords.contains(Keyword.POISONOUS)
    val hasLifesteal: Boolean get() = keywords.contains(Keyword.LIFESTEAL)
    val isAlive: Boolean get() = currentHealth > 0
    val canAttack: Boolean get() = isAlive && !summoningSick && !hasAttackedThisTurn

    fun readyForNewTurn() {
        summoningSick = false
        hasAttackedThisTurn = false
    }
}
