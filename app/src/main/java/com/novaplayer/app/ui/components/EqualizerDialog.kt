package com.novaplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.novaplayer.app.playback.AudioEffectsManager
import com.novaplayer.app.ui.theme.GlassBorder
import com.novaplayer.app.ui.theme.NovaBlue
import com.novaplayer.app.ui.theme.NovaCyan
import com.novaplayer.app.ui.theme.NovaPink
import com.novaplayer.app.ui.theme.NovaPurple
import com.novaplayer.app.ui.theme.NovaPurpleLight
import com.novaplayer.app.ui.theme.NovaTextSecondary

@Composable
fun AudioEffectsDialog(
    audioEffects: AudioEffectsManager,
    bassBoost: Int,
    virtualizer: Int,
    loudnessGain: Int,
    onBassBoostChanged: (Int) -> Unit,
    onVirtualizerChanged: (Int) -> Unit,
    onLoudnessChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isAvailable = audioEffects.isInitialized()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFF0D1128),
        title = {
            Column {
                Text(
                    "Audio Effects",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    if (isAvailable) "Fine-tune your listening experience"
                    else "Audio effects unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary.copy(alpha = 0.6f)
                )
            }
        },
        text = {
            if (!isAvailable) {
                GlassInfoBox("Start playing a track to enable audio effects.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bass Boost
                    SectionLabel("BASS BOOST")
                    EffectSliderCard(
                        icon = Icons.Filled.SurroundSound,
                        label = "Bass Boost",
                        value = bassBoost,
                        maxValue = 1000,
                        accentColor = NovaPurple,
                        onValueChange = onBassBoostChanged,
                        valueLabel = "${(bassBoost / 10f).toInt()}%"
                    )

                    // Virtualizer (3D Audio)
                    SectionLabel("3D AUDIO")
                    EffectSliderCard(
                        icon = Icons.Filled.Headphones,
                        label = "Virtualizer",
                        value = virtualizer,
                        maxValue = 1000,
                        accentColor = NovaCyan,
                        onValueChange = onVirtualizerChanged,
                        valueLabel = "${(virtualizer / 10f).toInt()}%"
                    )

                    // Loudness Enhancer
                    SectionLabel("LOUDNESS")
                    EffectSliderCard(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Volume Boost",
                        value = loudnessGain,
                        maxValue = 1000,
                        accentColor = NovaPink,
                        onValueChange = onLoudnessChanged,
                        valueLabel = "${(loudnessGain / 10f).toInt()}%"
                    )

                    // Equalizer Presets
                    val presets = audioEffects.getEqualizerPresetNames()
                    if (presets.isNotEmpty()) {
                        SectionLabel("EQ PRESETS")
                        EqualizerPresetRow(
                            presets = presets,
                            onPresetSelected = { audioEffects.applyEqualizerPreset(it) }
                        )
                    }

                    // Equalizer Bands
                    val bandCount = audioEffects.getEqualizerBandCount()
                    if (bandCount > 0) {
                        SectionLabel("EQUALIZER")
                        EqualizerBands(audioEffects = audioEffects, bandCount = bandCount)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = NovaCyan)
            }
        }
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = NovaPurpleLight.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun EffectSliderCard(
    icon: ImageVector,
    label: String,
    value: Int,
    maxValue: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit,
    valueLabel: String
) {
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0x0DFFFFFF), cardShape)
            .border(1.dp, GlassBorder, cardShape)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..maxValue.toFloat(),
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0x28FFFFFF)
            )
        )
    }
}

@Composable
private fun EqualizerPresetRow(
    presets: List<String>,
    onPresetSelected: (Int) -> Unit
) {
    var selectedPreset by remember { mutableIntStateOf(-1) }
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0x0DFFFFFF), cardShape)
            .border(1.dp, GlassBorder, cardShape)
            .padding(12.dp)
    ) {
        val chipShape = RoundedCornerShape(10.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Show first few presets in a wrapped row
            presets.take(6).forEachIndexed { index, preset ->
                val isSelected = index == selectedPreset
                Box(
                    modifier = Modifier
                        .clip(chipShape)
                        .then(
                            if (isSelected) {
                                Modifier.background(
                                    Brush.linearGradient(
                                        colors = listOf(NovaPurple, NovaCyan)
                                    ),
                                    chipShape
                                )
                            } else {
                                Modifier
                                    .background(Color(0x0DFFFFFF), chipShape)
                                    .border(1.dp, GlassBorder, chipShape)
                            }
                        )
                        .clickable {
                            selectedPreset = index
                            onPresetSelected(index)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else NovaTextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun EqualizerBands(
    audioEffects: AudioEffectsManager,
    bandCount: Int
) {
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(Color(0x0DFFFFFF), cardShape)
            .border(1.dp, GlassBorder, cardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val (minLevel, maxLevel) = audioEffects.getEqualizerBandLevelRange()

        for (band in 0 until bandCount) {
            var level by remember {
                mutableFloatStateOf(audioEffects.getEqualizerBandLevel(band))
            }
            val centerFreq = audioEffects.getEqualizerCenterFreq(band)
            val freqLabel = if (centerFreq >= 1000) "${centerFreq / 1000}k" else "$centerFreq"

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${freqLabel}Hz",
                    style = MaterialTheme.typography.labelMedium,
                    color = NovaBlue.copy(alpha = 0.8f),
                    modifier = Modifier.width(50.dp)
                )
                Slider(
                    value = level,
                    onValueChange = { newLevel ->
                        level = newLevel
                        audioEffects.setEqualizerBandLevel(band, newLevel)
                    },
                    valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = NovaBlue,
                        activeTrackColor = NovaBlue,
                        inactiveTrackColor = Color(0x28FFFFFF)
                    )
                )
                Text(
                    text = "${level.toInt()}dB",
                    style = MaterialTheme.typography.labelMedium,
                    color = NovaTextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.width(40.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassInfoBox(text: String) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x1AFFFFFF), Color(0x08FFFFFF))
                ),
                shape
            )
            .border(1.dp, GlassBorder, shape)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = NovaTextSecondary
        )
    }
}

// Keep backward compatibility
@Composable
fun EqualizerDialog(
    audioSessionId: Int?,
    onDismiss: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = shape,
        containerColor = Color(0xFF0D1128),
        title = { Text("Equalizer", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            GlassInfoBox("Use the Audio Effects panel in Settings for full control over Bass Boost, 3D Audio, Loudness, and Equalizer.")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = NovaCyan)
            }
        }
    )
}
