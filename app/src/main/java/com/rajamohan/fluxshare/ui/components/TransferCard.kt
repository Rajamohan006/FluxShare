package com.rajamohan.fluxshare.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.model.TransferState
import com.rajamohan.fluxshare.domain.model.TransferStats
import com.rajamohan.fluxshare.ui.theme.Dimensions
import com.rajamohan.fluxshare.ui.theme.mutedText
import com.rajamohan.fluxshare.ui.theme.success
import com.rajamohan.fluxshare.ui.theme.warning

import kotlin.math.roundToInt
@Composable
fun TransferCard(
    transfer: TransferEntity,
    stats: TransferStats,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transfer.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${if (transfer.direction == TransferDirection.SEND) "To" else "From"}: ${transfer.peerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.mutedText
                    )
                }

                TransferStateChip(state = transfer.state)
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            // Progress Bar
            TransferProgressBar(
                progress = stats.progressPercentage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(stats.progressPercentage * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatSpeed(stats.averageSpeed),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.mutedText
                )
                Text(
                    text = formatDuration(stats.estimatedTimeRemaining),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.mutedText
                )
            }

            // Action Buttons
            if (transfer.state in listOf(TransferState.TRANSFERRING, TransferState.PAUSED, TransferState.CONNECTING)) {
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (transfer.state == TransferState.TRANSFERRING) {
                        TextButton(onClick = onPause) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                    } else if (transfer.state == TransferState.PAUSED) {
                        TextButton(onClick = onResume) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    }

                    Spacer(modifier = Modifier.width(Dimensions.paddingSmall))

                    TextButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
@Composable
fun TransferStateChip(state: TransferState) {
    val (color, text) = when (state) {
        TransferState.QUEUED -> MaterialTheme.colorScheme.surfaceVariant to "Queued"
        TransferState.CONNECTING -> MaterialTheme.colorScheme.primary to "Connecting"
        TransferState.TRANSFERRING -> MaterialTheme.colorScheme.primary to "Transferring"
        TransferState.PAUSED -> MaterialTheme.colorScheme.warning to "Paused"
        TransferState.COMPLETED -> MaterialTheme.colorScheme.success to "Completed"
        TransferState.FAILED -> MaterialTheme.colorScheme.error to "Failed"
        TransferState.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant to "Cancelled"
        TransferState.VERIFYING -> MaterialTheme.colorScheme.secondary to "Verifying"
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
fun TransferProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress =  progress.coerceIn(0f, 1f),
        modifier = modifier.height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}
