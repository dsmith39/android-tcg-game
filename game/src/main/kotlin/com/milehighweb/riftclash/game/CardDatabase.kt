package com.milehighweb.riftclash.game

/**
 * The full pool of cards available in the base set, plus a helper to build the
 * default 30-card starter deck shared by the player and the AI in v1 (there is no
 * deck builder yet).
 */
object CardDatabase {

    val ALL_CARDS: List<CardTemplate> = listOf(
        CardTemplate(
            id = "sprig_scout",
            name = "Sprig Scout",
            cost = 1,
            type = CardType.CREATURE,
            attack = 1,
            health = 1,
            description = "A quick, disposable scout.",
        ),
        CardTemplate(
            id = "shieldwall_recruit",
            name = "Shieldwall Recruit",
            cost = 1,
            type = CardType.CREATURE,
            attack = 1,
            health = 3,
            keywords = setOf(Keyword.TAUNT),
            description = "Taunt. Cheap early wall.",
        ),
        CardTemplate(
            id = "embercub",
            name = "Embercub",
            cost = 2,
            type = CardType.CREATURE,
            attack = 3,
            health = 2,
            description = "Small and aggressive.",
        ),
        CardTemplate(
            id = "riftboar",
            name = "Riftboar",
            cost = 2,
            type = CardType.CREATURE,
            attack = 2,
            health = 2,
            keywords = setOf(Keyword.CHARGE),
            description = "Charge. Hits the board running.",
        ),
        CardTemplate(
            id = "stone_golem",
            name = "Stone Golem",
            cost = 3,
            type = CardType.CREATURE,
            attack = 2,
            health = 6,
            keywords = setOf(Keyword.TAUNT),
            description = "Taunt. A sturdy roadblock.",
        ),
        CardTemplate(
            id = "windfang",
            name = "Windfang",
            cost = 3,
            type = CardType.CREATURE,
            attack = 4,
            health = 3,
            description = "Balanced mid-game threat.",
        ),
        CardTemplate(
            id = "flameadept",
            name = "Flame Adept",
            cost = 4,
            type = CardType.CREATURE,
            attack = 4,
            health = 4,
            description = "Solid stats across the board.",
        ),
        CardTemplate(
            id = "ironclad_guardian",
            name = "Ironclad Guardian",
            cost = 4,
            type = CardType.CREATURE,
            attack = 3,
            health = 7,
            keywords = setOf(Keyword.TAUNT),
            description = "Taunt. Very hard to break through.",
        ),
        CardTemplate(
            id = "storm_drake",
            name = "Storm Drake",
            cost = 5,
            type = CardType.CREATURE,
            attack = 5,
            health = 5,
            description = "A fearsome flying threat.",
        ),
        CardTemplate(
            id = "voidreaver",
            name = "Voidreaver",
            cost = 6,
            type = CardType.CREATURE,
            attack = 7,
            health = 5,
            description = "Hits like a truck.",
        ),
        CardTemplate(
            id = "ancient_titan",
            name = "Ancient Titan",
            cost = 8,
            type = CardType.CREATURE,
            attack = 8,
            health = 8,
            keywords = setOf(Keyword.TAUNT),
            description = "Taunt. A late-game finisher.",
        ),
        CardTemplate(
            id = "spark_bolt",
            name = "Spark Bolt",
            cost = 1,
            type = CardType.SPELL,
            description = "Deal 2 damage to the enemy hero.",
            spellEffect = SpellEffect.DealDamage(2, TargetType.ENEMY_HERO),
        ),
        CardTemplate(
            id = "cinder_strike",
            name = "Cinder Strike",
            cost = 2,
            type = CardType.SPELL,
            description = "Deal 3 damage to an enemy creature.",
            spellEffect = SpellEffect.DealDamage(3, TargetType.ENEMY_CREATURE),
        ),
        CardTemplate(
            id = "arcane_blast",
            name = "Arcane Blast",
            cost = 3,
            type = CardType.SPELL,
            description = "Deal 4 damage to any target.",
            spellEffect = SpellEffect.DealDamage(4, TargetType.ANY_CREATURE),
        ),
        CardTemplate(
            id = "meteor_swarm",
            name = "Meteor Swarm",
            cost = 5,
            type = CardType.SPELL,
            description = "Deal 3 damage to all enemy creatures.",
            spellEffect = SpellEffect.DealDamage(3, TargetType.ALL_ENEMY_CREATURES),
        ),
        CardTemplate(
            id = "mend",
            name = "Mend",
            cost = 2,
            type = CardType.SPELL,
            description = "Restore 6 health to your hero.",
            spellEffect = SpellEffect.Heal(6, TargetType.NONE),
        ),
        CardTemplate(
            id = "natures_blessing",
            name = "Nature's Blessing",
            cost = 2,
            type = CardType.SPELL,
            description = "Give a friendly creature +2/+2.",
            spellEffect = SpellEffect.Buff(2, 2, TargetType.FRIENDLY_CREATURE),
        ),
        CardTemplate(
            id = "insight",
            name = "Insight",
            cost = 1,
            type = CardType.SPELL,
            description = "Draw 2 cards.",
            spellEffect = SpellEffect.DrawCards(2),
        ),
    )

    private val byId = ALL_CARDS.associateBy { it.id }

    /** The rarer / more powerful cards only get a single copy in the starter deck. */
    private val singleCopyIds = setOf(
        "flameadept", "ironclad_guardian", "storm_drake", "voidreaver", "ancient_titan", "meteor_swarm",
    )

    fun card(id: String): CardTemplate = byId.getValue(id)

    /**
     * The default 30-card starter deck, shared by the player and the AI in v1
     * (there is no deck builder yet). [GameEngine.startGame] shuffles it before play.
     */
    fun defaultDeck(): MutableList<CardTemplate> =
        ALL_CARDS.flatMap { card ->
            if (card.id in singleCopyIds) listOf(card) else listOf(card, card)
        }.toMutableList()
}
