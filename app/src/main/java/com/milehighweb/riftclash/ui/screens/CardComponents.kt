package com.milehighweb.riftclash.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.milehighweb.riftclash.game.CardInstance
import com.milehighweb.riftclash.game.CardType
import com.milehighweb.riftclash.game.CreatureInstance
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

@Composable
fun HandCardView(
    card: CardInstance,
    isSelected: Boolean,
    isPlayable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.width(112.dp).height(162.dp),
) {
    val template = card.template
    val frameColor = if (template.type == CardType.CREATURE) EmberOrangeDeep else SpellVioletDeep

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxSize().clickable(enabled = isPlayable) { onClick() },
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

@Composable
fun BoardCreatureView(
    creature: CreatureInstance,
    isSelected: Boolean,
    isSelectableAttacker: Boolean,
    onClick: () -> Unit,
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
            modifier = Modifier.fillMaxSize().clickable { onClick() },
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

        if (creature.isTaunt) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TauntGold)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
            ) {
                Text(text = "TAUNT", color = Color(0xFF3A2A00), fontSize = 7.sp, fontWeight = FontWeight.Bold)
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
