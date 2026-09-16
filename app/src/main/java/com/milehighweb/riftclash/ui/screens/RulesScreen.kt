package com.milehighweb.riftclash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.ParchmentWhite
import com.milehighweb.riftclash.ui.theme.boardBackgroundBrush

private data class RuleSection(val title: String, val body: String)

private val RULE_SECTIONS = listOf(
    RuleSection(
        "The Basics",
        "Each hero starts with 30 health. Reduce your opponent's hero to 0 to win. " +
            "You start with a 3-card hand (the AI, going second, starts with 4) and draw one card at the start of each of your turns.",
    ),
    RuleSection(
        "Mana",
        "You gain 1 maximum mana crystal each turn, up to 10, and it fully refills every turn. " +
            "Spend mana to play cards from your hand -- each card's cost is shown on its blue gem.",
    ),
    RuleSection(
        "Creatures & Spells",
        "Creatures go onto your board and can attack on later turns. Spells resolve immediately: " +
            "damage, healing, buffs, or extra card draw, then go to your graveyard.",
    ),
    RuleSection(
        "Summoning Sickness",
        "A creature can't attack the turn it's played, unless it has Charge. Once it survives to your next turn, it's ready.",
    ),
    RuleSection(
        "Taunt",
        "If your opponent controls any Taunt creatures, you must attack one of them before you can attack anything else on their side.",
    ),
    RuleSection(
        "Turn Phases",
        "Each of your turns has a Main Phase and a Combat Phase. Play creatures and spells during the Main Phase, " +
            "then tap Combat to move into the Combat Phase, where your ready creatures can attack. " +
            "You can't play more cards once you've declared combat, so plan your turn before you commit.",
    ),
    RuleSection(
        "Combat",
        "Attacking a creature trades damage both ways -- your attacker takes damage back equal to the defender's attack. " +
            "Attacking the hero deals damage with no return hit.",
    ),
    RuleSection(
        "Fatigue",
        "Once your deck is empty, drawing deals escalating damage to your own hero instead (1, then 2, then 3, and so on).",
    ),
    RuleSection(
        "Controls",
        "Tap a card to select it, then tap Play (for creatures, or spells that don't need a target) or tap a target on the " +
            "board directly (for targeted spells). Once you've declared combat, tap a ready creature to select it as an " +
            "attacker, then tap an enemy creature or hero to attack. Long-press any card, in your hand or on the board, " +
            "to see its full details.",
    ),
)

@Composable
fun RulesScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(boardBackgroundBrush()).safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = ParchmentWhite)
            }
            Text(
                text = "How to Play",
                color = EmberOrange,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        ) {
            items(RULE_SECTIONS) { section ->
                Text(
                    text = section.title,
                    color = EmberOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                )
                Text(
                    text = section.body,
                    color = ParchmentWhite.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
