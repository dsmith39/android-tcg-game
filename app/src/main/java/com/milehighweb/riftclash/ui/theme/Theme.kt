package com.milehighweb.riftclash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RiftPurple = Color(0xFF1B1033)
val RiftPurpleLight = Color(0xFF2E1E52)
val EmberOrange = Color(0xFFF2A93B)
val HealthRed = Color(0xFFE0483E)
val ManaBlue = Color(0xFF3E8FE0)
val ParchmentWhite = Color(0xFFF4EFE6)

// The game always uses this dark, parchment-on-purple palette -- it reads best against
// card art and doesn't need to track the system light/dark setting.
private val RiftClashColorScheme = darkColorScheme(
    primary = EmberOrange,
    secondary = ManaBlue,
    background = RiftPurple,
    surface = RiftPurpleLight,
    onPrimary = RiftPurple,
    onBackground = ParchmentWhite,
    onSurface = ParchmentWhite,
    error = HealthRed,
)

@Composable
fun RiftClashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RiftClashColorScheme,
        content = content,
    )
}
