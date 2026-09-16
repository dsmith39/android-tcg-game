package com.milehighweb.riftclash.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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

/**
 * Every section below sizes itself from what's actually available (weights, or
 * fillMaxHeight inside a weighted row) rather than fixed dp guesses -- a fixed-height
 * layout here previously overflowed short landscape phone screens and pushed the
 * status banner and hand off the bottom of the screen entirely.
 */
@Composable
fun GameScreen(viewModel: GameViewModel, snapshot: GameSnapshot, onExitToMenu: () -> Unit) {
    val game = snapshot.game
    val isPlayerTurn = game.activeSide == Side.PLAYER && !game.isGameOver

    Box(modifier = Modifier.fillMaxSize().background(boardBackgroundBrush())) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp)) {
            TopInfoBar(state = game.ai, turnNumber = game.turnNumber, onExitToMenu = onExitToMenu, onHeroTapped = { viewModel.onEnemyHeroTapped() })

            BoardRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                creatures = game.ai.board,
                selectedId = null,
                selectableIds = emptySet(),
                onTapped = { id -> viewModel.onCreatureTapped(id, Side.AI) },
            )

            StatusBanner(
                message = viewModel.statusMessage,
                isPlayerTurn = isPlayerTurn,
                modifier = Modifier.fillMaxWidth(),
            )

            BoardRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                creatures = game.player.board,
                selectedId = viewModel.selectedAttackerId,
                selectableIds = game.player.board.filter { it.canAttack }.map { it.instanceId }.toSet(),
                onTapped = { id -> viewModel.onCreatureTapped(id, Side.PLAYER) },
            )

            BottomBar(viewModel = viewModel, game = game, isPlayerTurn = isPlayerTurn)
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
private fun TopInfoBar(
    state: PlayerState,
    turnNumber: Int,
    onExitToMenu: () -> Unit,
    onHeroTapped: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp).clickable { onHeroTapped() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onExitToMenu, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Menu", tint = ParchmentWhite.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        HeroPanel(state = state, label = "Opponent", icon = Icons.Filled.SmartToy)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Deck ${state.deck.size} · Hand ${state.hand.size}",
            color = ParchmentWhite.copy(alpha = 0.5f),
            fontSize = 10.sp,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "TURN $turnNumber",
            color = ParchmentWhite.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusBanner(message: String?, isPlayerTurn: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
        Crossfade(targetState = message to isPlayerTurn, label = "status") { (msg, playerTurn) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(RiftPurpleLight.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Text(
                    text = msg ?: if (playerTurn) "Your turn" else "Opponent is thinking...",
                    color = if (playerTurn) EmberOrange else ParchmentWhite.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun HeroPanel(
    state: PlayerState,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(RiftPurpleLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = EmberOrange, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Gem(value = state.heroHealth, colors = listOf(HealthRed, Color(0xFF8F241D)), size = 22.dp)
        Spacer(modifier = Modifier.width(4.dp))
        ManaPip(current = state.currentMana, max = state.maxMana)
    }
}

@Composable
private fun ManaPip(current: Int, max: Int) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(ManaBlue).padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(text = "$current/$max", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BoardRow(
    modifier: Modifier,
    creatures: List<CreatureInstance>,
    selectedId: String?,
    selectableIds: Set<String>,
    onTapped: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.15f)),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(creatures, key = { it.instanceId }) { creature ->
                BoardCreatureView(
                    creature = creature,
                    isSelected = creature.instanceId == selectedId,
                    isSelectableAttacker = creature.instanceId in selectableIds,
                    onClick = { onTapped(creature.instanceId) },
                    modifier = Modifier.fillMaxHeight().width(64.dp),
                )
            }
        }
    }
}

@Composable
private fun BottomBar(viewModel: GameViewModel, game: GameState, isPlayerTurn: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().height(112.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.width(88.dp), horizontalAlignment = Alignment.Start) {
            Text(text = "You", color = ParchmentWhite.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Gem(value = game.player.heroHealth, colors = listOf(HealthRed, Color(0xFF8F241D)), size = 22.dp)
                Spacer(modifier = Modifier.width(4.dp))
                ManaPip(current = game.player.currentMana, max = game.player.maxMana)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Deck ${game.player.deck.size}", color = ParchmentWhite.copy(alpha = 0.5f), fontSize = 9.sp)
        }
        Spacer(modifier = Modifier.width(4.dp))
        HandRow(
            viewModel = viewModel,
            game = game,
            isPlayerTurn = isPlayerTurn,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        Spacer(modifier = Modifier.width(4.dp))
        EndTurnControls(viewModel = viewModel, isPlayerTurn = isPlayerTurn)
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
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(game.player.hand, key = { it.instanceId }) { card ->
            val affordable = card.template.cost <= game.player.currentMana
            HandCardView(
                card = card,
                isSelected = card.instanceId == viewModel.selectedHandCardId,
                isPlayable = isPlayerTurn && affordable,
                onClick = { viewModel.onHandCardTapped(card.instanceId) },
                modifier = Modifier.fillMaxHeight().width(78.dp),
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
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text = "Play", fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = { viewModel.endTurn() },
            enabled = isPlayerTurn,
            colors = ButtonDefaults.buttonColors(containerColor = RiftPurpleLight),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(Icons.Filled.SkipNext, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(text = "End Turn", fontSize = 11.sp)
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
