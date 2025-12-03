package com.rajamohan.fluxshare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rajamohan.fluxshare.ui.components.EmptyState
import com.rajamohan.fluxshare.ui.components.TransferCard
import com.rajamohan.fluxshare.ui.theme.Dimensions
import com.rajamohan.fluxshare.ui.theme.mutedText
import com.rajamohan.fluxshare.ui.viewmodel.TransfersViewModel

// ========== Transfers Screen ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    viewModel: TransfersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Active Transfers",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.activeTransfers.size} transfer${if (uiState.activeTransfers.size != 1) "s" else ""} in progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.mutedText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.activeTransfers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CloudOff,
                    title = "No Active Transfers",
                    subtitle = "Start sending files from the Devices tab",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimensions.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.paddingMedium)
                ) {
                    items(
                        items = uiState.activeTransfers,
                        key = { it.transfer.id }
                    ) { transferWithStats ->
                        TransferCard(
                            transfer = transferWithStats.transfer,
                            stats = transferWithStats.stats,
                            onPause = { viewModel.pauseTransfer(transferWithStats.transfer.id) },
                            onResume = { viewModel.resumeTransfer(transferWithStats.transfer.id) },
                            onCancel = { viewModel.cancelTransfer(transferWithStats.transfer.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.paddingLarge))
                    }
                }
            }
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