package com.milehighweb.riftclash.game

enum class CardType {
    CREATURE,
    SPELL,
}

enum class Keyword {
    /** Enemies must attack a Taunt creature before attacking anything else on that board. */
    TAUNT,

    /** Can attack the same turn it is played, ignoring summoning sickness. */
    CHARGE,

    /** Whenever this creature deals damage, its controller's hero is healed for that much. */
    LIFESTEAL,

    /** Any creature it damages in combat dies, regardless of remaining health. */
    POISONOUS,

    /** Absorbs the next instance of damage entirely, then is removed. */
    DIVINE_SHIELD,
}

enum class TargetType {
    ENEMY_HERO,
    ENEMY_CREATURE,
    FRIENDLY_CREATURE,
    ANY_CREATURE,
    ALL_ENEMY_CREATURES,
    NONE,
}

sealed class SpellEffect {
    abstract val target: TargetType

    data class DealDamage(val amount: Int, override val target: TargetType) : SpellEffect()

    data class Heal(val amount: Int, override val target: TargetType) : SpellEffect()

    data class Buff(val attack: Int, val health: Int, override val target: TargetType) : SpellEffect()

    data class DrawCards(val amount: Int) : SpellEffect() {
        override val target: TargetType = TargetType.NONE
    }

    /** Strips all keywords from a creature, removing Taunt, Divine Shield, Poisonous, etc. */
    data class Silence(override val target: TargetType) : SpellEffect()
}

/**
 * Immutable definition of a card, as it exists in a deck list / card database.
 * Runtime state (current health, summoning sickness, etc.) lives in [CreatureInstance].
 */
data class CardTemplate(
    val id: String,
    val name: String,
    val cost: Int,
    val type: CardType,
    val description: String,
    val attack: Int = 0,
    val health: Int = 0,
    val keywords: Set<Keyword> = emptySet(),
    val spellEffect: SpellEffect? = null,
)

/** A card sitting in a hand or deck: a template plus a unique identity for that physical copy. */
data class CardInstance(
    val instanceId: String,
    val template: CardTemplate,
)
