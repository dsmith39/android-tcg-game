package com.milehighweb.riftclash.game

enum class Side { PLAYER, AI }

const val MAX_HERO_HEALTH = 30
const val MAX_MANA = 10
const val MAX_BOARD_SIZE = 6
const val MAX_HAND_SIZE = 10

class PlayerState(
    val side: Side,
    val deck: MutableList<CardTemplate>,
) {
    var heroHealth: Int = MAX_HERO_HEALTH
    var maxMana: Int = 0
    var currentMana: Int = 0
    val hand: MutableList<CardInstance> = mutableListOf()
    val board: MutableList<CreatureInstance> = mutableListOf()
    var fatigueDamage: Int = 0

    val isDead: Boolean get() = heroHealth <= 0
}
