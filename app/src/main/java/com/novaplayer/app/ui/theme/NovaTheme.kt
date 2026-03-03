package com.novaplayer.app.ui.theme

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Core palette
val NovaPurple = Color(0xFF7C5CFC)
val NovaPurpleLight = Color(0xFFAA94FF)
val NovaCyan = Color(0xFF00E5D0)
val NovaPink = Color(0xFFFF6B9D)
val NovaBlue = Color(0xFF4DA6FF)

val NovaBgDark = Color(0xFF060918)
val NovaBgCard = Color(0xFF0D1128)
val NovaSurface = Color(0xFF111633)
val NovaSurfaceLight = Color(0xFF1A2040)

val NovaTextPrimary = Color(0xFFF0F2FF)
val NovaTextSecondary = Color(0xFFB0B5D0)

val GlassWhite = Color(0x14FFFFFF)
val GlassBorder = Color(0x28FFFFFF)
val GlassHighlight = Color(0x0DFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = NovaPurple,
    onPrimary = Color.White,
    secondary = NovaCyan,
    tertiary = NovaPink,
    background = NovaBgDark,
    onBackground = NovaTextPrimary,
    surface = NovaSurface,
    onSurface = NovaTextPrimary,
    surfaceVariant = NovaSurfaceLight,
    onSurfaceVariant = NovaTextSecondary,
    error = Color(0xFFCF6679),
    outline = GlassBorder
)

data class GlassStyle(
    val backgroundBrush: Brush = Brush.linearGradient(
        colors = listOf(
            Color(0x1AFFFFFF),
            Color(0x08FFFFFF)
        )
    ),
    val borderColor: Color = GlassBorder,
    val borderWidth: Dp = 1.dp,
    val cornerRadius: Dp = 20.dp,
    val blurRadius: Dp = 24.dp
)

val LocalGlassStyle = staticCompositionLocalOf { GlassStyle() }

@Composable
fun NovaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalGlassStyle provides GlassStyle()) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}

fun Modifier.glassBackground(
    style: GlassStyle = GlassStyle(),
    shape: RoundedCornerShape = RoundedCornerShape(style.cornerRadius)
): Modifier = this
    .clip(shape)
    .background(style.backgroundBrush, shape)
    .border(BorderStroke(style.borderWidth, style.borderColor), shape)

fun Modifier.glassCard(
    cornerRadius: Dp = 20.dp
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0x1AFFFFFF),
                    Color(0x08FFFFFF)
                )
            ),
            shape
        )
        .border(BorderStroke(1.dp, GlassBorder), shape)
}

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(NovaPurple, NovaCyan)
)

val SubtleGlow = Brush.radialGradient(
    colors = listOf(
        NovaPurple.copy(alpha = 0.15f),
        Color.Transparent
    )
)
