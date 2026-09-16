package com.milehighweb.riftclash.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milehighweb.riftclash.game.CardInstance
import com.milehighweb.riftclash.game.CardType
import com.milehighweb.riftclash.game.CreatureInstance
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.HealthRed
import com.milehighweb.riftclash.ui.theme.ManaBlue
import com.milehighweb.riftclash.ui.theme.RiftPurpleLight

@Composable
fun HandCardView(
    card: CardInstance,
    isSelected: Boolean,
    isPlayable: Boolean,
    onClick: () -> Unit,
) {
    val template = card.template
    Card(
        modifier = Modifier
            .width(108.dp)
            .height(148.dp)
            .clickable(enabled = isPlayable) { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isPlayable) RiftPurpleLight else Color(0xFF3A3350)),
        border = if (isSelected) BorderStroke(2.dp, EmberOrange) else null,
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ManaCrystal(template.cost)
            }
            Text(
                text = template.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Text(
                text = template.description,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                maxLines = 4,
                modifier = Modifier.padding(top = 2.dp),
            )
            if (template.type == CardType.CREATURE) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StatPill(value = template.attack, color = EmberOrange)
                    StatPill(value = template.health, color = HealthRed)
                }
            }
        }
    }
}

@Composable
fun BoardCreatureView(
    creature: CreatureInstance,
    isSelected: Boolean,
    isSelectableAttacker: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(84.dp)
            .height(108.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelectableAttacker) RiftPurpleLight else Color(0xFF3A3350),
        ),
        border = when {
            isSelected -> BorderStroke(2.dp, EmberOrange)
            creature.isTaunt -> BorderStroke(2.dp, ManaBlue)
            else -> null
        },
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = creature.template.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            if (creature.isTaunt) {
                Text(text = "TAUNT", color = ManaBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            if (creature.summoningSick) {
                Text(text = "resting", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 8.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatPill(value = creature.currentAttack, color = EmberOrange)
                StatPill(value = creature.currentHealth, color = HealthRed)
            }
        }
    }
}

@Composable
fun ManaCrystal(amount: Int) {
    Box(
        modifier = Modifier.size(22.dp).clip(CircleShape).background(ManaBlue),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = amount.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatPill(value: Int, color: Color) {
    Box(
        modifier = Modifier.size(20.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = value.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
