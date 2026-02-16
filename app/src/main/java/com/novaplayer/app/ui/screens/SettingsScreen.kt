package com.novaplayer.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novaplayer.app.model.RepeatMode
import com.novaplayer.app.model.UserSettings

@Composable
fun SettingsScreen(
    settings: UserSettings,
    audioSessionId: Int?,
    onDarkThemeChanged: (Boolean) -> Unit,
    onGaplessChanged: (Boolean) -> Unit,
    onWaveformChanged: (Boolean) -> Unit,
    onShuffleChanged: (Boolean) -> Unit,
    onRepeatModeChanged: (RepeatMode) -> Unit,
    onSleepTimerChanged: (Int) -> Unit,
    onEqualizerClick: () -> Unit
) {
    var showEqualizer by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Customize how NovaPlayer looks and behaves.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        SettingToggleRow(
            label = "Dark theme",
            description = "Always use dark theme UI.",
            checked = settings.darkTheme,
            onCheckedChange = onDarkThemeChanged
        )

        SettingToggleRow(
            label = "Gapless playback",
            description = "Reduce gaps between tracks where possible.",
            checked = settings.gaplessPlayback,
            onCheckedChange = onGaplessChanged
        )

        SettingToggleRow(
            label = "Animated waveform",
            description = "Show an animated waveform on the now playing bar.",
            checked = settings.showWaveform,
            onCheckedChange = onWaveformChanged
        )

        SettingToggleRow(
            label = "Shuffle mode",
            description = "Randomize track order during playback.",
            checked = settings.shuffleMode,
            onCheckedChange = onShuffleChanged
        )

        RepeatModeSelector(
            currentMode = settings.repeatMode,
            onModeChanged = onRepeatModeChanged
        )

        SleepTimerSelector(
            currentMinutes = settings.sleepTimerMinutes,
            onMinutesChanged = onSleepTimerChanged
        )

        Button(
            onClick = { showEqualizer = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Open Equalizer")
        }
    }

    if (showEqualizer) {
        com.novaplayer.app.ui.components.EqualizerDialog(
            audioSessionId = audioSessionId,
            onDismiss = { showEqualizer = false }
        )
    }
}

@Composable
private fun RepeatModeSelector(
    currentMode: RepeatMode,
    onModeChanged: (RepeatMode) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Repeat mode",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Control how tracks repeat during playback.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            RepeatMode.values().forEach { mode ->
                Button(
                    onClick = { onModeChanged(mode) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (currentMode == mode) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = when (mode) {
                            RepeatMode.OFF -> "Off"
                            RepeatMode.ALL -> "All"
                            RepeatMode.ONE -> "One"
                        }
                    )
                }
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
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Sleep timer",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Automatically pause playback after specified minutes (0 to disable).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { 
                    inputText = it.filter { char -> char.isDigit() }
                    val minutes = inputText.toIntOrNull() ?: 0
                    onMinutesChanged(minutes)
                },
                modifier = Modifier.weight(1f),
                label = { Text("Minutes") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                inputText = ""
                onMinutesChanged(0)
            }) {
                Text("Clear")
            }
        }
        if (currentMinutes > 0) {
            Text(
                text = "Timer set: ${currentMinutes} minutes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

