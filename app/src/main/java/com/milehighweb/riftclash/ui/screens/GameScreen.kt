package com.milehighweb.riftclash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milehighweb.riftclash.GameSnapshot
import com.milehighweb.riftclash.GameViewModel
import com.milehighweb.riftclash.game.PlayerState
import com.milehighweb.riftclash.game.Side
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.HealthRed
import com.milehighweb.riftclash.ui.theme.ManaBlue
import com.milehighweb.riftclash.ui.theme.RiftPurpleLight

@Composable
fun GameScreen(viewModel: GameViewModel, snapshot: GameSnapshot, onExitToMenu: () -> Unit) {
    val game = snapshot.game
    val isPlayerTurn = game.activeSide == Side.PLAYER && !game.isGameOver

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            HeroRow(state = game.ai, label = "Opponent", onHeroTapped = { viewModel.onEnemyHeroTapped() })

            BoardRow(
                creatures = game.ai.board,
                selectedId = null,
                selectableIds = emptySet(),
                onTapped = { id -> viewModel.onCreatureTapped(id, Side.AI) },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = viewModel.statusMessage
                        ?: if (isPlayerTurn) "Your turn -- turn ${game.turnNumber}" else "Opponent is thinking...",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            BoardRow(
                creatures = game.player.board,
                selectedId = viewModel.selectedAttackerId,
                selectableIds = game.player.board.filter { it.canAttack }.map { it.instanceId }.toSet(),
                onTapped = { id -> viewModel.onCreatureTapped(id, Side.PLAYER) },
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HeroPanel(state = game.player, label = "You", modifier = Modifier.width(140.dp))
                Spacer(modifier = Modifier.width(8.dp))
                HandRow(
                    viewModel = viewModel,
                    game = game,
                    isPlayerTurn = isPlayerTurn,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                EndTurnControls(viewModel = viewModel, isPlayerTurn = isPlayerTurn)
            }
        }

        if (game.isGameOver) {
            GameOverOverlay(
                winner = game.winner,
                onPlayAgain = { viewModel.startNewGame() },
                onExitToMenu = onExitToMenu,
            )
        }
    }
}

@Composable
private fun HeroRow(state: PlayerState, label: String, onHeroTapped: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeroTapped() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeroPanel(state = state, label = label, modifier = Modifier.width(140.dp))
        Text(
            text = "Deck: ${state.deck.size}  Hand: ${state.hand.size}",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun HeroPanel(state: PlayerState, label: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = label, color = MaterialTheme.colorScheme.onBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatBadge(text = state.heroHealth.toString(), color = HealthRed)
                Spacer(modifier = Modifier.width(4.dp))
                StatBadge(text = "${state.currentMana}/${state.maxMana}", color = ManaBlue)
            }
        }
    }
}

@Composable
private fun StatBadge(text: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = text, color = androidx.compose.ui.graphics.Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BoardRow(
    creatures: List<com.milehighweb.riftclash.game.CreatureInstance>,
    selectedId: String?,
    selectableIds: Set<String>,
    onTapped: (String) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(112.dp).padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(creatures, key = { it.instanceId }) { creature ->
            BoardCreatureView(
                creature = creature,
                isSelected = creature.instanceId == selectedId,
                isSelectableAttacker = creature.instanceId in selectableIds,
                onClick = { onTapped(creature.instanceId) },
            )
        }
    }
}

@Composable
private fun HandRow(
    viewModel: GameViewModel,
    game: com.milehighweb.riftclash.game.GameState,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.height(152.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(game.player.hand, key = { it.instanceId }) { card ->
            val affordable = card.template.cost <= game.player.currentMana
            HandCardView(
                card = card,
                isSelected = card.instanceId == viewModel.selectedHandCardId,
                isPlayable = isPlayerTurn && affordable,
                onClick = { viewModel.onHandCardTapped(card.instanceId) },
            )
        }
    }
}

@Composable
private fun EndTurnControls(viewModel: GameViewModel, isPlayerTurn: Boolean) {
    val selectedCardId = viewModel.selectedHandCardId
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = { viewModel.playSelectedCard() },
            enabled = isPlayerTurn && selectedCardId != null,
            colors = ButtonDefaults.buttonColors(containerColor = EmberOrange),
        ) {
            Text(text = "Play", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = { viewModel.endTurn() },
            enabled = isPlayerTurn,
            colors = ButtonDefaults.buttonColors(containerColor = RiftPurpleLight),
        ) {
            Text(text = "End Turn", fontSize = 12.sp)
        }
    }
}

@Composable
private fun GameOverOverlay(winner: Side?, onPlayAgain: () -> Unit, onExitToMenu: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0xCC0B0618)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (winner) {
                    Side.PLAYER -> "Victory!"
                    Side.AI -> "Defeat"
                    null -> "Draw"
                },
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPlayAgain, colors = ButtonDefaults.buttonColors(containerColor = EmberOrange)) {
                Text("Play Again")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onExitToMenu, colors = ButtonDefaults.buttonColors(containerColor = RiftPurpleLight)) {
                Text("Main Menu")
            }
        }
    }
}
