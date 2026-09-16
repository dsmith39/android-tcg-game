package com.milehighweb.riftclash.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.ParchmentWhite
import com.milehighweb.riftclash.ui.theme.menuBackgroundBrush

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onShowRules: () -> Unit,
    onShowCollection: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(menuBackgroundBrush())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "RIFT CLASH",
            color = EmberOrange,
            fontSize = 46.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            style = MaterialTheme.typography.headlineLarge,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.width(180.dp).height(2.dp).background(EmberOrange.copy(alpha = 0.6f)),
        ) {}
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Summon creatures, cast spells, and burn your\nopponent's hero down to zero.",
            color = ParchmentWhite.copy(alpha = 0.8f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStartGame,
            modifier = Modifier.width(240.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmberOrange),
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "New Game vs. AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = onShowCollection,
            modifier = Modifier.width(240.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentWhite),
        ) {
            Icon(Icons.Filled.Style, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Card Collection", fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onShowRules,
            modifier = Modifier.width(240.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentWhite),
        ) {
            Icon(Icons.Filled.MenuBook, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "How to Play", fontSize = 15.sp)
        }
    }
}
