package com.novaplayer.app.ui.components

import android.media.audiofx.Equalizer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EqualizerDialog(
    audioSessionId: Int?,
    onDismiss: () -> Unit
) {
    val equalizer = remember(audioSessionId) {
        if (audioSessionId != null && audioSessionId != 0) {
            try {
                Equalizer(Int.MAX_VALUE, audioSessionId)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    val bandCount = equalizer?.numberOfBands ?: 0
    val bandLevels = remember { mutableStateOf(List(bandCount) { 0 }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Equalizer") },
        text = {
            if (equalizer == null || bandCount == 0) {
                Text(
                    text = "Equalizer not available. Audio session may not be initialized.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Adjust frequency bands",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    (0 until bandCount).forEach { band ->
                        val bandLevelRange = equalizer.getBandLevelRange()
                        val minLevel = bandLevelRange[0].toInt() / 100
                        val maxLevel = bandLevelRange[1].toInt() / 100
                        var level by remember { 
                            mutableStateOf(equalizer.getBandLevel(band.toShort()).toInt() / 100f)
                        }
                        
                        Column {
                            val centerFreq = equalizer.getCenterFreq(band.toShort()).toInt() / 1000
                            Text(
                                text = "$centerFreq Hz",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Slider(
                                value = level,
                                onValueChange = { newLevel ->
                                    level = newLevel
                                    equalizer.setBandLevel(
                                        band.toShort(),
                                        (newLevel * 100).toInt().toShort()
                                    )
                                },
                                valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
