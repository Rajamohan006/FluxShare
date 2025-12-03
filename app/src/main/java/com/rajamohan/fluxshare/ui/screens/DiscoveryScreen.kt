package com.rajamohan.fluxshare.ui.screens

import com.rajamohan.fluxshare.domain.model.Device
import com.rajamohan.fluxshare.ui.viewmodel.DiscoveryViewModel
import kotlinx.serialization.InternalSerializationApi


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rajamohan.fluxshare.ui.theme.mutedText
import androidx.compose.material3.Text
import com.rajamohan.fluxshare.ui.components.DeviceCard
import com.rajamohan.fluxshare.ui.components.EmptyState
import com.rajamohan.fluxshare.ui.theme.Dimensions
import com.rajamohan.fluxshare.ui.theme.onWarningContainer
import com.rajamohan.fluxshare.ui.theme.success
import com.rajamohan.fluxshare.ui.theme.warningContainer


@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun DiscoveryScreen(
    onDeviceSelected: (Device) -> Unit,
    viewModel: DiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    var showFilePickerTrigger by remember { mutableStateOf(false) }

    // File picker launcher - THIS IS THE KEY PART!
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            selectedDevice?.let { device ->
                // Get file details
                val cursor = context.contentResolver.query(fileUri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)

                        val fileName = if (nameIndex >= 0) it.getString(nameIndex) else "unknown_file"
                        val fileSize = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L

                        // Start the actual transfer!
                        viewModel.startFileTransfer(
                            context = context,
                            fileUri = fileUri,
                            fileName = fileName,
                            fileSize = fileSize,
                            targetDevice = device
                        )
                    }
                }
            }
        }
        selectedDevice = null
    }

    // Trigger file picker when needed
    LaunchedEffect(showFilePickerTrigger) {
        if (showFilePickerTrigger) {
            filePickerLauncher.launch("*/*")
            showFilePickerTrigger = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Discover Devices",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.devices.size} device${if (uiState.devices.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDiscovery() }) {
                        Icon(
                            imageVector = if (uiState.isDiscovering) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isDiscovering) "Stop Discovery" else "Start Discovery",
                            tint = if (uiState.isDiscovering)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.devices.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (uiState.isDiscovering) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Searching for devices...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.DevicesOther,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Devices Found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                // Device List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.devices,
                        key = { it.id }
                    ) { device ->
                        DeviceCard(
                            device = device,
                            onClick = {
                                selectedDevice = device
                                showFilePickerTrigger = true
                            }
                        )
                    }
                }
            }
        }

        // Success/Error Messages
        uiState.transferStarted?.let { message ->
            LaunchedEffect(message) {
                // Show snackbar or toast
                kotlinx.coroutines.delay(3000)
                viewModel.clearTransferMessage()
            }
        }
    }
}
@Composable
fun DiscoveryStatusCard(
    isDiscovering: Boolean,
    deviceCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDiscovering)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
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
                    imageVector = if (isDiscovering) Icons.Default.Radar else Icons.Default.SignalWifiOff,
                    contentDescription = "Discovery Status",
                    tint = if (isDiscovering)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
                    Text(
                        text = if (isDiscovering) "Discovery Active" else "Discovery Paused",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$deviceCount device${if (deviceCount != 1) "s" else ""} available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.mutedText
                    )
                }
            }

            if (isDiscovering) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            MaterialTheme.colorScheme.success,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}

// Permission Helper Composable
@Composable
fun PermissionRequestCard(
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingMedium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.warningContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.paddingSmall)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.onWarningContainer
                )
                Text(
                    text = "Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onWarningContainer
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.paddingSmall))

            Text(
                text = "Location permission is required for Wi-Fi discovery on this Android version.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onWarningContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(Dimensions.paddingMedium))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onWarningContainer,
                    contentColor = MaterialTheme.colorScheme.warningContainer
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}