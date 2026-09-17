package com.milehighweb.riftclash.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.milehighweb.riftclash.game.CardInstance
import com.milehighweb.riftclash.game.CardTemplate
import com.milehighweb.riftclash.game.CardType
import com.milehighweb.riftclash.game.CreatureInstance
import com.milehighweb.riftclash.game.Keyword
import com.milehighweb.riftclash.ui.components.CardArt
import com.milehighweb.riftclash.ui.theme.CardTitleStyle
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.EmberOrangeDeep
import com.milehighweb.riftclash.ui.theme.HealthRed
import com.milehighweb.riftclash.ui.theme.ManaBlue
import com.milehighweb.riftclash.ui.theme.ManaBlueDeep
import com.milehighweb.riftclash.ui.theme.ParchmentWhite
import com.milehighweb.riftclash.ui.theme.RiftPurpleLight
import com.milehighweb.riftclash.ui.theme.SpellVioletDeep
import com.milehighweb.riftclash.ui.theme.TauntGold

/**
 * A card's full details, snapshotted out of whatever it came from (a hand card or a board
 * creature) so the detail dialog can show live attack/health for creatures that have been
 * buffed or damaged, while still working for cards that only exist as a [CardTemplate].
 */
data class InspectedCard(
    val template: CardTemplate,
    val currentAttack: Int? = null,
    val currentHealth: Int? = null,
    val maxHealth: Int? = null,
    // Defaults to the template's keywords, but a board creature that has been Silenced
    // carries its own, now-empty set instead.
    val keywords: Set<Keyword> = template.keywords,
) {
    companion object {
        fun fromCard(card: CardInstance) = InspectedCard(template = card.template)
        fun fromCreature(creature: CreatureInstance) = InspectedCard(
            template = creature.template,
            currentAttack = creature.currentAttack,
            currentHealth = creature.currentHealth,
            maxHealth = creature.maxHealth,
            keywords = creature.keywords,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HandCardView(
    card: CardInstance,
    isSelected: Boolean,
    isPlayable: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier.width(112.dp).height(162.dp),
) {
    val template = card.template
    val frameColor = if (template.type == CardType.CREATURE) EmberOrangeDeep else SpellVioletDeep

    Box(modifier = modifier) {
        Card(
            // Long-press works even on a dimmed, unaffordable card -- inspecting a card
            // shouldn't require being able to play it right now.
            modifier = Modifier.fillMaxSize().combinedClickable(
                onClick = { if (isPlayable) onClick() },
                onLongClick = onLongClick,
            ),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = RiftPurpleLight),
            border = BorderStroke(if (isSelected) 3.dp else 2.dp, if (isSelected) EmberOrange else frameColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 10.dp else 2.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    CardArt(template = template, modifier = Modifier.fillMaxSize(), iconSize = 22.dp)
                    // Stat gems are inset into the art instead of overhanging the card edge,
                    // so they stay inside the frame even when cards sit close together
                    // (hand row, board row, collection grid).
                    Gem(
                        value = template.cost,
                        colors = listOf(ManaBlue, ManaBlueDeep),
                        modifier = Modifier.align(Alignment.TopStart).padding(3.dp),
                        size = 20.dp,
                    )
                    if (template.type == CardType.CREATURE) {
                        Gem(
                            value = template.attack,
                            colors = listOf(EmberOrange, EmberOrangeDeep),
                            modifier = Modifier.align(Alignment.BottomStart).padding(3.dp),
                            size = 20.dp,
                        )
                        Gem(
                            value = template.health,
                            colors = listOf(HealthRed, Color(0xFF8F241D)),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp),
                            size = 20.dp,
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = template.name,
                        style = CardTitleStyle.copy(fontSize = 10.sp),
                        color = ParchmentWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = template.description,
                        fontSize = 8.sp,
                        lineHeight = 9.5.sp,
                        color = ParchmentWhite.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
                    )
                }
            }
        }

        if (!isPlayable) {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(alpha = 0.55f)),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BoardCreatureView(
    creature: CreatureInstance,
    isSelected: Boolean,
    isSelectableAttacker: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier.width(92.dp).height(122.dp),
) {
    val borderColor = when {
        isSelected -> EmberOrange
        creature.isTaunt -> TauntGold
        isSelectableAttacker -> EmberOrangeDeep
        else -> RiftPurpleLight
    }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxSize().combinedClickable(onClick = onClick, onLongClick = onLongClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = RiftPurpleLight),
            border = BorderStroke(if (isSelected) 3.dp else 2.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 1.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    CardArt(template = creature.template, modifier = Modifier.fillMaxSize(), iconSize = 18.dp)
                    // Stat gems are inset into the art instead of overhanging the card edge,
                    // so they stay inside the frame when creatures sit close together on the board.
                    Gem(
                        value = creature.currentAttack,
                        colors = listOf(EmberOrange, EmberOrangeDeep),
                        modifier = Modifier.align(Alignment.BottomStart).padding(3.dp),
                        size = 22.dp,
                    )
                    Gem(
                        value = creature.currentHealth,
                        colors = listOf(HealthRed, Color(0xFF8F241D)),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp),
                        size = 22.dp,
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp, vertical = 1.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = creature.template.name,
                        style = CardTitleStyle.copy(fontSize = 9.sp),
                        color = ParchmentWhite,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Charge only matters before a creature's first attack, which the "resting" icon
        // below already communicates, so it doesn't get its own badge here.
        val badgeKeywords = creature.keywords.filter { it != Keyword.CHARGE }
        if (badgeKeywords.isNotEmpty()) {
            Row(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 2.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
            ) {
                badgeKeywords.forEach { keyword -> KeywordBadge(keyword) }
            }
        }
        if (creature.summoningSick) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(RiftPurpleLight)
                    .padding(3.dp),
            ) {
                Icon(Icons.Filled.Bedtime, contentDescription = "Resting", tint = ParchmentWhite.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
            }
        }
    }
}

/** A compact badge for a board creature's active keywords, small enough to stack a few side by side. */
@Composable
private fun KeywordBadge(keyword: Keyword) {
    val (label, color) = when (keyword) {
        Keyword.TAUNT -> "TAUNT" to TauntGold
        Keyword.DIVINE_SHIELD -> "SHIELD" to ManaBlue
        Keyword.POISONOUS -> "POISON" to Color(0xFF3FA34D)
        Keyword.LIFESTEAL -> "LEECH" to HealthRed
        Keyword.CHARGE -> "CHARGE" to EmberOrange
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(color).padding(horizontal = 3.dp, vertical = 1.dp),
    ) {
        Text(text = label, color = Color(0xFF1A1A1A), fontSize = 6.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Gem(
    value: Int,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 26.dp,
) {
    // Hero health starts at 30, so this box needs to hold two digits from turn one --
    // a fixed font size wrapped or clipped "30" onto two lines inside the small gems.
    val fontSize = if (value >= 10) 10.sp else 12.sp
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(colors))
            .border(width = 1.5.dp, color = ParchmentWhite.copy(alpha = 0.6f), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * A full-size, un-clamped view of a single card, opened with a long press from the hand or
 * the board. Hand and board cards both truncate their name/description to fit their small
 * footprint, so this is the only place a player can read a long card's full text mid-match.
 */
@Composable
fun CardDetailDialog(card: InspectedCard, onDismiss: () -> Unit) {
    val template = card.template
    val frameColor = if (template.type == CardType.CREATURE) EmberOrangeDeep else SpellVioletDeep

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(280.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = RiftPurpleLight),
            border = BorderStroke(2.dp, frameColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp))) {
                    CardArt(template = template, modifier = Modifier.fillMaxSize(), iconSize = 44.dp)
                    Gem(
                        value = template.cost,
                        colors = listOf(ManaBlue, ManaBlueDeep),
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        size = 30.dp,
                    )
                    if (template.type == CardType.CREATURE) {
                        Gem(
                            value = card.currentAttack ?: template.attack,
                            colors = listOf(EmberOrange, EmberOrangeDeep),
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                            size = 30.dp,
                        )
                        Gem(
                            value = card.currentHealth ?: template.health,
                            colors = listOf(HealthRed, Color(0xFF8F241D)),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                            size = 30.dp,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = template.name,
                    style = CardTitleStyle.copy(fontSize = 18.sp),
                    color = ParchmentWhite,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cost ${template.cost} · ${template.type.name.lowercase().replaceFirstChar(Char::uppercase)}",
                    color = ParchmentWhite.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (card.keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    ) {
                        card.keywords.forEach { keyword -> KeywordChip(keyword) }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = template.description,
                    color = ParchmentWhite.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = frameColor),
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun KeywordChip(keyword: Keyword) {
    val (label, icon) = when (keyword) {
        Keyword.TAUNT -> "Taunt" to Icons.Filled.Shield
        Keyword.CHARGE -> "Charge" to Icons.Filled.Bolt
        Keyword.LIFESTEAL -> "Lifesteal" to Icons.Filled.Bloodtype
        Keyword.POISONOUS -> "Poisonous" to Icons.Filled.Science
        Keyword.DIVINE_SHIELD -> "Divine Shield" to Icons.Filled.GppGood
    }
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(TauntGold.copy(alpha = 0.25f)).padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TauntGold, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, color = TauntGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
