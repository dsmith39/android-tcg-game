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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.HealthRed
import com.milehighweb.riftclash.ui.theme.ParchmentWhite
import com.milehighweb.riftclash.ui.theme.RiftPurpleLight
import com.milehighweb.riftclash.ui.theme.boardBackgroundBrush

/**
 * Shown once, on the launch right after the app crashed, so the crash can be diagnosed from
 * the phone alone: no exceptions thrown inside Compose recomposition reach the try/catch
 * guards in [com.milehighweb.riftclash.GameViewModel], so this is often the only trace of them.
 */
@Composable
fun CrashReportScreen(report: String, onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize().background(boardBackgroundBrush()).padding(20.dp)) {
        Text(
            text = "Rift Clash crashed last time",
            color = HealthRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Copy this and send it over so the crash can be fixed.",
            color = ParchmentWhite.copy(alpha = 0.75f),
            fontSize = 13.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(RiftPurpleLight.copy(alpha = 0.6f))
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(text = report, color = ParchmentWhite, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { clipboardManager.setText(AnnotatedString(report)) },
                colors = ButtonDefaults.buttonColors(containerColor = EmberOrange),
                modifier = Modifier.weight(1f),
            ) {
                Text("Copy Crash Report")
            }
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentWhite),
                modifier = Modifier.weight(1f),
            ) {
                Text("Dismiss")
            }
        }
    }
}
