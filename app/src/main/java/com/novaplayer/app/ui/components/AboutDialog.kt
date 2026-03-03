package com.novaplayer.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.novaplayer.app.ui.theme.GlassBorder
import com.novaplayer.app.ui.theme.NovaCyan
import com.novaplayer.app.ui.theme.NovaPink
import com.novaplayer.app.ui.theme.NovaPurple
import com.novaplayer.app.ui.theme.NovaTextSecondary

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val dialogShape = RoundedCornerShape(28.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = dialogShape,
        containerColor = Color(0xFF0D1128),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "NovaPlayer",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "v0.2.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaPurple
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info card
                val cardShape = RoundedCornerShape(16.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0x1AFFFFFF),
                                    Color(0x08FFFFFF)
                                )
                            ),
                            cardShape
                        )
                        .border(1.dp, GlassBorder, cardShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoRow("Author", "Kamil \"Novik\" Nowicki")
                    InfoRow("License", "GNU GPL v3")
                    InfoRow("Copyright", "2026")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "If you like this app, consider supporting development with a small donation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = NovaTextSecondary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // GitHub button
                TextButton(
                    onClick = {
                        uriHandler.openUri("https://github.com/novik133/NovaPlayer")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NovaCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GitHub", color = NovaCyan)
                }

                // Donate button
                TextButton(
                    onClick = {
                        uriHandler.openUri("https://ko-fi.com/novadesktop")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = NovaPink
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Donate", color = NovaPink)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NovaTextSecondary)
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = NovaTextSecondary.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
