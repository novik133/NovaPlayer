package com.novaplayer.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novaplayer.app.model.UserSettings

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onDarkThemeChanged: (Boolean) -> Unit,
    onGaplessChanged: (Boolean) -> Unit,
    onWaveformChanged: (Boolean) -> Unit
) {

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
            text = "Customize how NovaPlayer looks and behaves. (Settings are not persisted yet in this preview version.)",
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

