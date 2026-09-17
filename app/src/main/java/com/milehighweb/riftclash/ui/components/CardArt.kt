package com.milehighweb.riftclash.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.milehighweb.riftclash.game.CardTemplate
import com.milehighweb.riftclash.game.CardType
import com.milehighweb.riftclash.game.Keyword
import com.milehighweb.riftclash.game.SpellEffect
import com.milehighweb.riftclash.ui.theme.EmberOrange
import com.milehighweb.riftclash.ui.theme.EmberOrangeDeep
import com.milehighweb.riftclash.ui.theme.ParchmentWhite
import com.milehighweb.riftclash.ui.theme.SpellViolet
import com.milehighweb.riftclash.ui.theme.SpellVioletDeep
import com.milehighweb.riftclash.ui.theme.TauntGold

/** A deterministic icon + color pair per card, standing in for hand-painted art. */
private data class ArtSpec(val icon: ImageVector, val topColor: Color, val bottomColor: Color)

private fun artSpecFor(template: CardTemplate): ArtSpec = when (template.type) {
    CardType.CREATURE -> when {
        Keyword.DIVINE_SHIELD in template.keywords -> ArtSpec(Icons.Filled.GppGood, TauntGold, EmberOrangeDeep)
        Keyword.TAUNT in template.keywords -> ArtSpec(Icons.Filled.Shield, TauntGold, EmberOrangeDeep)
        Keyword.POISONOUS in template.keywords -> ArtSpec(Icons.Filled.Science, EmberOrange, EmberOrangeDeep)
        Keyword.LIFESTEAL in template.keywords -> ArtSpec(Icons.Filled.Bloodtype, EmberOrange, EmberOrangeDeep)
        Keyword.CHARGE in template.keywords -> ArtSpec(Icons.Filled.Bolt, EmberOrange, EmberOrangeDeep)
        else -> ArtSpec(Icons.Filled.Pets, EmberOrange, EmberOrangeDeep)
    }
    CardType.SPELL -> when (template.spellEffect) {
        is SpellEffect.DealDamage -> ArtSpec(Icons.Filled.Whatshot, SpellViolet, SpellVioletDeep)
        is SpellEffect.Heal -> ArtSpec(Icons.Filled.Favorite, SpellViolet, SpellVioletDeep)
        is SpellEffect.Buff -> ArtSpec(Icons.Filled.TrendingUp, SpellViolet, SpellVioletDeep)
        is SpellEffect.DrawCards -> ArtSpec(Icons.Filled.Style, SpellViolet, SpellVioletDeep)
        is SpellEffect.Silence -> ArtSpec(Icons.Filled.VolumeOff, SpellViolet, SpellVioletDeep)
        null -> ArtSpec(Icons.Filled.AutoAwesome, SpellViolet, SpellVioletDeep)
    }
}

@Composable
fun CardArt(template: CardTemplate, modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 30.dp) {
    val spec = artSpecFor(template)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.radialGradient(colors = listOf(spec.topColor, spec.bottomColor))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = spec.icon,
            contentDescription = null,
            tint = ParchmentWhite.copy(alpha = 0.9f),
            modifier = Modifier.size(iconSize),
        )
    }
}
