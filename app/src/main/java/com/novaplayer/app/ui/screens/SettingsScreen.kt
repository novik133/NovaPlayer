package com.novaplayer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.novaplayer.app.model.RepeatMode
import com.novaplayer.app.model.UserSettings
import com.novaplayer.app.playback.AudioEffectsManager
import com.novaplayer.app.ui.components.AudioEffectsDialog
import com.novaplayer.app.ui.theme.GlassBorder
import com.novaplayer.app.ui.theme.NovaCyan
import com.novaplayer.app.ui.theme.NovaPurple
import com.novaplayer.app.ui.theme.NovaPurpleLight
import com.novaplayer.app.ui.theme.NovaTextSecondary
import com.novaplayer.app.ui.theme.glassCard

@Composable
fun SettingsScreen(
    settings: UserSettings,
    audioSessionId: Int?,
    audioEffects: AudioEffectsManager,
    onDarkThemeChanged: (Boolean) -> Unit,
    onGaplessChanged: (Boolean) -> Unit,
    onWaveformChanged: (Boolean) -> Unit,
    onShuffleChanged: (Boolean) -> Unit,
    onRepeatModeChanged: (RepeatMode) -> Unit,
    onSleepTimerChanged: (Int) -> Unit,
    onSpeedChanged: (Float) -> Unit,
    onSkipSilenceChanged: (Boolean) -> Unit,
    onCrossfadeChanged: (Int) -> Unit,
    onBassBoostChanged: (Int) -> Unit,
    onVirtualizerChanged: (Int) -> Unit,
    onLoudnessChanged: (Int) -> Unit
) {
    var showEffects by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Customize how NovaPlayer looks and behaves.",
            style = MaterialTheme.typography.bodySmall,
            color = NovaTextSecondary.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Appearance
        SectionLabel("Appearance")

        GlassToggleCard(
            icon = Icons.Filled.DarkMode,
            label = "Dark theme",
            description = "Always use dark theme UI",
            checked = settings.darkTheme,
            onCheckedChange = onDarkThemeChanged
        )

        GlassToggleCard(
            icon = Icons.Filled.GraphicEq,
            label = "Animated waveform",
            description = "Show waveform on the now playing bar",
            checked = settings.showWaveform,
            onCheckedChange = onWaveformChanged
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Playback
        SectionLabel("Playback")

        GlassToggleCard(
            icon = Icons.Filled.SkipNext,
            label = "Gapless playback",
            description = "Reduce gaps between tracks",
            checked = settings.gaplessPlayback,
            onCheckedChange = onGaplessChanged
        )

        GlassToggleCard(
            icon = Icons.Filled.Shuffle,
            label = "Shuffle mode",
            description = "Randomize track order",
            checked = settings.shuffleMode,
            onCheckedChange = onShuffleChanged
        )

        GlassToggleCard(
            icon = Icons.AutoMirrored.Filled.VolumeOff,
            label = "Skip silence",
            description = "Auto-skip silent parts of tracks",
            checked = settings.skipSilence,
            onCheckedChange = onSkipSilenceChanged
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Playback Speed
        SectionLabel("Playback Speed")
        SpeedSelector(
            currentSpeed = settings.playbackSpeed,
            onSpeedChanged = onSpeedChanged
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Crossfade
        SectionLabel("Crossfade")
        CrossfadeSelector(
            currentDurationMs = settings.crossfadeDurationMs,
            onDurationChanged = onCrossfadeChanged
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Repeat mode
        SectionLabel("Repeat Mode")
        RepeatModeSelector(
            currentMode = settings.repeatMode,
            onModeChanged = onRepeatModeChanged
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Sleep timer
        SectionLabel("Sleep Timer")
        SleepTimerSelector(
            currentMinutes = settings.sleepTimerMinutes,
            onMinutesChanged = onSleepTimerChanged
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Audio Effects button
        val eqShape = RoundedCornerShape(16.dp)
        Button(
            onClick = { showEffects = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = eqShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(colors = listOf(NovaPurple, NovaCyan)),
                        eqShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Audio Effects & Equalizer",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showEffects) {
        AudioEffectsDialog(
            audioEffects = audioEffects,
            bassBoost = settings.bassBoostStrength,
            virtualizer = settings.virtualizerStrength,
            loudnessGain = settings.loudnessGain,
            onBassBoostChanged = onBassBoostChanged,
            onVirtualizerChanged = onVirtualizerChanged,
            onLoudnessChanged = onLoudnessChanged,
            onDismiss = { showEffects = false }
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = NovaPurpleLight.copy(alpha = 0.7f),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun GlassToggleCard(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) NovaPurple else NovaTextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.padding(end = 14.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = NovaTextSecondary.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NovaPurple,
                uncheckedThumbColor = NovaTextSecondary.copy(alpha = 0.4f),
                uncheckedTrackColor = Color(0x28FFFFFF),
                uncheckedBorderColor = GlassBorder
            )
        )
    }
}

@Composable
private fun SpeedSelector(
    currentSpeed: Float,
    onSpeedChanged: (Float) -> Unit
) {
    val presets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Speed,
                contentDescription = null,
                tint = if (currentSpeed != 1.0f) NovaCyan else NovaTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 14.dp)
            )
            Text(
                "Speed: ${formatSpeed(currentSpeed)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Preset chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            presets.forEach { speed ->
                val isSelected = kotlin.math.abs(currentSpeed - speed) < 0.01f
                val chipShape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .clip(chipShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.linearGradient(colors = listOf(NovaPurple, NovaCyan)),
                                    chipShape
                                )
                            } else {
                                Modifier
                                    .background(Color(0x0DFFFFFF), chipShape)
                                    .border(1.dp, GlassBorder, chipShape)
                            }
                        )
                        .clickable { onSpeedChanged(speed) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = formatSpeed(speed),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else NovaTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = currentSpeed,
            onValueChange = { onSpeedChanged((it * 20).toInt() / 20f) },
            valueRange = 0.25f..3.0f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = NovaCyan,
                activeTrackColor = NovaCyan,
                inactiveTrackColor = Color(0x28FFFFFF)
            )
        )
    }
}

@Composable
private fun CrossfadeSelector(
    currentDurationMs: Int,
    onDurationChanged: (Int) -> Unit
) {
    val presets = listOf(0, 2000, 4000, 6000, 8000, 12000)
    val labels = listOf("Off", "2s", "4s", "6s", "8s", "12s")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = if (currentDurationMs > 0) NovaPurple else NovaTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 14.dp)
            )
            Text(
                "Crossfade: ${if (currentDurationMs == 0) "Off" else "${currentDurationMs / 1000}s"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            presets.forEachIndexed { index, duration ->
                val isSelected = currentDurationMs == duration
                val chipShape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .clip(chipShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.linearGradient(colors = listOf(NovaPurple, NovaCyan)),
                                    chipShape
                                )
                            } else {
                                Modifier
                                    .background(Color(0x0DFFFFFF), chipShape)
                                    .border(1.dp, GlassBorder, chipShape)
                            }
                        )
                        .clickable { onDurationChanged(duration) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = labels[index],
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else NovaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RepeatModeSelector(
    currentMode: RepeatMode,
    onModeChanged: (RepeatMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        RepeatMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val chipShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(chipShape)
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                Brush.linearGradient(colors = listOf(NovaPurple, NovaCyan)),
                                chipShape
                            )
                        } else {
                            Modifier
                                .background(Color(0x0DFFFFFF), chipShape)
                                .border(1.dp, GlassBorder, chipShape)
                        }
                    )
                    .clickable { onModeChanged(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (mode) {
                        RepeatMode.OFF -> "Off"
                        RepeatMode.ALL -> "All"
                        RepeatMode.ONE -> "One"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else NovaTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SleepTimerSelector(
    currentMinutes: Int,
    onMinutesChanged: (Int) -> Unit
) {
    var inputText by remember { mutableStateOf(if (currentMinutes > 0) currentMinutes.toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 16.dp)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = null,
                tint = if (currentMinutes > 0) NovaCyan else NovaTextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 14.dp)
            )
            val fieldShape = RoundedCornerShape(12.dp)
            TextField(
                value = inputText,
                onValueChange = {
                    inputText = it.filter { char -> char.isDigit() }
                    onMinutesChanged(inputText.toIntOrNull() ?: 0)
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(fieldShape)
                    .border(1.dp, GlassBorder, fieldShape),
                placeholder = { Text("Minutes", color = NovaTextSecondary.copy(alpha = 0.4f)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1AFFFFFF),
                    unfocusedContainerColor = Color(0x0DFFFFFF),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = NovaCyan
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { inputText = ""; onMinutesChanged(0) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF)),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) { Text("Clear", color = NovaTextSecondary) }
        }

        if (currentMinutes > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Timer: $currentMinutes min", style = MaterialTheme.typography.bodySmall, color = NovaCyan)
        }
    }
}

private fun formatSpeed(speed: Float): String {
    return if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x"
}
