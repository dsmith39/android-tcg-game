package com.milehighweb.riftclash.ui.screens

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.milehighweb.riftclash.GameSnapshot
import com.milehighweb.riftclash.GameViewModel
import com.milehighweb.riftclash.game.CreatureInstance
import com.milehighweb.riftclash.game.GameState
import com.milehighweb.riftclash.game.PlayerState
import com.milehighweb.riftclash.game.Side
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.HealthRed
import com.milehighweb.riftclash.ui.theme.ManaBlue
import com.milehighweb.riftclash.ui.theme.ParchmentWhite
import com.milehighweb.riftclash.ui.theme.RiftPurpleLight
import com.milehighweb.riftclash.ui.theme.boardBackgroundBrush

@Composable
fun GameScreen(viewModel: GameViewModel, snapshot: GameSnapshot, onExitToMenu: () -> Unit) {
    val game = snapshot.game
    val isPlayerTurn = game.activeSide == Side.PLAYER && !game.isGameOver

    Box(modifier = Modifier.fillMaxSize().background(boardBackgroundBrush())) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            TopBar(turnNumber = game.turnNumber, onExitToMenu = onExitToMenu)

            HeroRow(state = game.ai, label = "Opponent", icon = Icons.Filled.SmartToy, onHeroTapped = { viewModel.onEnemyHeroTapped() })

            BoardRow(
                creatures = game.ai.board,
                selectedId = null,
                selectableIds = emptySet(),
                onTapped = { id -> viewModel.onCreatureTapped(id, Side.AI) },
            )

            StatusBanner(
                message = viewModel.statusMessage,
                isPlayerTurn = isPlayerTurn,
                turnNumber = game.turnNumber,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )

            BoardRow(
                creatures = game.player.board,
                selectedId = viewModel.selectedAttackerId,
                selectableIds = game.player.board.filter { it.canAttack }.map { it.instanceId }.toSet(),
                onTapped = { id -> viewModel.onCreatureTapped(id, Side.PLAYER) },
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HeroPanel(state = game.player, label = "You", icon = Icons.Filled.Favorite, modifier = Modifier.width(120.dp))
                Spacer(modifier = Modifier.width(6.dp))
                HandRow(
                    viewModel = viewModel,
                    game = game,
                    isPlayerTurn = isPlayerTurn,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(6.dp))
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
private fun TopBar(turnNumber: Int, onExitToMenu: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onExitToMenu) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Menu", tint = ParchmentWhite.copy(alpha = 0.7f))
        }
        Text(
            text = "TURN $turnNumber",
            color = ParchmentWhite.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun StatusBanner(message: String?, isPlayerTurn: Boolean, turnNumber: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        Crossfade(targetState = message to isPlayerTurn, label = "status") { (msg, playerTurn) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(RiftPurpleLight.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = msg ?: if (playerTurn) "Your turn" else "Opponent is thinking...",
                    color = if (playerTurn) EmberOrange else ParchmentWhite.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HeroRow(
    state: PlayerState,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onHeroTapped: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onHeroTapped() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeroPanel(state = state, label = label, icon = icon, modifier = Modifier.width(120.dp))
        Text(
            text = "Deck ${state.deck.size} · Hand ${state.hand.size}",
            color = ParchmentWhite.copy(alpha = 0.55f),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun HeroPanel(
    state: PlayerState,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(RiftPurpleLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = EmberOrange, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, color = ParchmentWhite.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Gem(value = state.heroHealth, colors = listOf(HealthRed, Color(0xFF8F241D)), size = 24.dp)
                Spacer(modifier = Modifier.width(4.dp))
                ManaPip(current = state.currentMana, max = state.maxMana)
            }
        }
    }
}

@Composable
private fun ManaPip(current: Int, max: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ManaBlue)
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(text = "$current/$max", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BoardRow(
    creatures: List<CreatureInstance>,
    selectedId: String?,
    selectableIds: Set<String>,
    onTapped: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.15f)),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
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
}

@Composable
private fun HandRow(
    viewModel: GameViewModel,
    game: GameState,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.height(168.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = "Play", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = { viewModel.endTurn() },
            enabled = isPlayerTurn,
            colors = ButtonDefaults.buttonColors(containerColor = RiftPurpleLight),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = "End Turn", fontSize = 12.sp)
        }
    }
}

@Composable
private fun GameOverOverlay(winner: Side?, onPlayAgain: () -> Unit, onExitToMenu: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xE60B0618)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = when (winner) {
                    Side.PLAYER -> "Victory!"
                    Side.AI -> "Defeat"
                    null -> "Draw"
                },
                color = if (winner == Side.PLAYER) EmberOrange else ParchmentWhite,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onPlayAgain,
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmberOrange),
            ) {
                Text("Play Again", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onExitToMenu,
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RiftPurpleLight),
            ) {
                Text("Main Menu")
            }
        }
    }
}
