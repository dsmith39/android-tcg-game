package com.milehighweb.riftclash.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RiftPurpleDark = Color(0xFF0E0921)
val RiftPurple = Color(0xFF1B1033)
val RiftPurpleLight = Color(0xFF2E1E52)
val RiftPurpleGlow = Color(0xFF4A2E7A)
val EmberOrange = Color(0xFFF2A93B)
val EmberOrangeDeep = Color(0xFFC97418)
val HealthRed = Color(0xFFE0483E)
val ManaBlue = Color(0xFF3E8FE0)
val ManaBlueDeep = Color(0xFF1F5FA8)
val SpellViolet = Color(0xFF8B5CE0)
val SpellVioletDeep = Color(0xFF5A34A8)
val ParchmentWhite = Color(0xFFF4EFE6)
val TauntGold = Color(0xFFE0B93E)

// The game always uses this dark, parchment-on-purple palette -- it reads best against
// card art and doesn't need to track the system light/dark setting.
private val RiftClashColorScheme = darkColorScheme(
    primary = EmberOrange,
    secondary = ManaBlue,
    tertiary = SpellViolet,
    background = RiftPurpleDark,
    surface = RiftPurpleLight,
    onPrimary = RiftPurple,
    onBackground = ParchmentWhite,
    onSurface = ParchmentWhite,
    error = HealthRed,
)

private val displayFont = FontFamily.Serif

val RiftClashTypography = Typography().let { base ->
    base.copy(
        headlineLarge = base.headlineLarge.copy(fontFamily = displayFont, fontWeight = FontWeight.Bold),
        headlineMedium = base.headlineMedium.copy(fontFamily = displayFont, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = displayFont, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = displayFont, fontWeight = FontWeight.Bold),
    )
}

val CardTitleStyle = TextStyle(fontFamily = displayFont, fontWeight = FontWeight.Bold, fontSize = 11.sp)

fun boardBackgroundBrush() = Brush.verticalGradient(
    colors = listOf(RiftPurpleDark, RiftPurple, RiftPurpleDark),
)

fun menuBackgroundBrush() = Brush.radialGradient(
    colors = listOf(RiftPurpleGlow, RiftPurple, RiftPurpleDark),
    radius = 1400f,
)

@Composable
fun RiftClashTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RiftClashColorScheme,
        typography = RiftClashTypography,
        content = content,
    )
}
