package com.novaplayer.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "About NovaPlayer")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NovaPlayer 0.1.0",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Author: Kamil \"Novik\" Nowicki",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Copyright © 2026",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "License: GNU GPL v3",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "GitHub: github.com/novik133/NovaPlayer",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "If you like this app, consider supporting development with a small donation.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    uriHandler.openUri("https://github.com/novik133/NovaPlayer")
                }
            ) {
                Text("View on GitHub")
            }
        },
        dismissButton = {
            Column {
                TextButton(
                    onClick = {
                        uriHandler.openUri("https://ko-fi.com/novadesktop")
                    }
                ) {
                    Text("Donate on Ko-fi")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

