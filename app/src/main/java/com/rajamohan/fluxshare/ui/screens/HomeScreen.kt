package com.rajamohan.fluxshare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rajamohan.fluxshare.ui.components.formatFileSize
import com.rajamohan.fluxshare.ui.theme.Dimensions
import com.rajamohan.fluxshare.ui.theme.mutedText
import com.rajamohan.fluxshare.ui.theme.onSuccessContainer
import com.rajamohan.fluxshare.ui.theme.success
import com.rajamohan.fluxshare.ui.theme.successContainer
import com.rajamohan.fluxshare.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToDiscovery: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FluxShare",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Fast & Secure File Sharing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.mutedText
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Dimensions.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
        ) {
            item { Spacer(modifier = Modifier.height(Dimensions.paddingSmall)) }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Dimensions.paddingSmall)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                ) {
                    QuickActionCard(
                        title = "Send Files",
                        icon = Icons.Default.Send,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateToDiscovery,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Active Transfers",
                        icon = Icons.Default.SwapHoriz,
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = onNavigateToTransfers,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Statistics
            item {
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Dimensions.paddingSmall)
                )
            }

            item {
                StatisticsCard(stats = uiState.stats)
            }

            // Device Status
            item {
                Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                Text(
                    text = "Network Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = Dimensions.paddingSmall)
                )
            }

            item {
                NetworkStatusCard(availableDevices = uiState.stats.availableDevices)
            }

            // Recent Activity
            if (uiState.stats.totalTransfers > 0) {
                item {
                    Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    ActivitySummaryCard(
                        completed = uiState.stats.completedTransfers,
                        failed = uiState.stats.failedTransfers,
                        totalSize = uiState.stats.totalDataTransferred
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(Dimensions.paddingLarge)) }
        }

        // Error Snackbar
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(Dimensions.paddingMedium),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun StatisticsCard(stats: com.rajamohan.fluxshare.ui.viewmodel.HomeStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Transfers",
                    value = stats.totalTransfers.toString(),
                    icon = Icons.Default.SwapVert
                )
                StatItem(
                    label = "Active Now",
                    value = stats.activeTransfers.toString(),
                    icon = Icons.Default.Sync,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))
            Divider()
            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Completed",
                    value = stats.completedTransfers.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.success
                )
                StatItem(
                    label = "Failed",
                    value = stats.failedTransfers.toString(),
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.mutedText
            )
        }
    }
}

@Composable
fun NetworkStatusCard(availableDevices: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (availableDevices > 0)
                MaterialTheme.colorScheme.successContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
            ) {
                Icon(
                    imageVector = if (availableDevices > 0) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = "Network",
                    tint = if (availableDevices > 0)
                        MaterialTheme.colorScheme.onSuccessContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = "$availableDevices Device${if (availableDevices != 1) "s" else ""} Available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (availableDevices > 0) "Ready to transfer" else "Searching for devices...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.mutedText
                    )
                }
            }

            if (availableDevices > 0) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.success)
                )
            }
        }
    }
}

@Composable
fun ActivitySummaryCard(
    completed: Int,
    failed: Int,
    totalSize: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Data Transferred",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = formatFileSize(totalSize),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$completed successful • $failed failed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Activity",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )
        }
    }
}