package com.rajamohan.fluxshare.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rajamohan.fluxshare.ui.theme.Dimensions
import com.rajamohan.fluxshare.ui.theme.mutedText
import com.rajamohan.fluxshare.ui.viewmodel.SettingsViewModel

// ========== Settings Screen ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeviceNameDialog by remember { mutableStateOf(false) }
    var showPortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
        ) {
            // Device Settings Section
            item {
                SettingsSectionHeader(title = "Device")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Smartphone,
                    title = "Device Name",
                    subtitle = uiState.deviceName,
                    onClick = { showDeviceNameDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Router,
                    title = "Port",
                    subtitle = uiState.port.toString(),
                    onClick = { showPortDialog = true }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = Dimensions.paddingSmall)) }

            // Security Section
            item {
                SettingsSectionHeader(title = "Security")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = "Enable Encryption",
                    subtitle = "Use AES-GCM encryption for transfers",
                    checked = uiState.encryptionEnabled,
                    onCheckedChange = { viewModel.toggleEncryption(it) }
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.SecurityUpdate,
                    title = "Auto-Accept from Known Devices",
                    subtitle = "Automatically accept transfers from trusted devices",
                    checked = uiState.autoAcceptFromKnown,
                    onCheckedChange = { /* viewModel.toggleAutoAccept(it) */ }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = Dimensions.paddingSmall)) }

            // Appearance Section
            item {
                SettingsSectionHeader(title = "Appearance")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    checked = uiState.darkMode,
                    onCheckedChange = { viewModel.toggleDarkMode(it) }
                )
            }

            item { Divider(modifier = Modifier.padding(vertical = Dimensions.paddingSmall)) }

            // About Section
            item {
                SettingsSectionHeader(title = "About")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0"
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Code,
                    title = "Technology Stack",
                    subtitle = "Kotlin • Jetpack Compose • Material3"
                )
            }

            item {
                Spacer(modifier = Modifier.height(Dimensions.paddingExtraLarge))
            }
        }

        // Device Name Dialog
        if (showDeviceNameDialog) {
            var newName by remember { mutableStateOf(uiState.deviceName) }

            AlertDialog(
                onDismissRequest = { showDeviceNameDialog = false },
                icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                title = { Text("Change Device Name") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Device Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateDeviceName(newName)
                            showDeviceNameDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeviceNameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Port Dialog
        if (showPortDialog) {
            var newPort by remember { mutableStateOf(uiState.port.toString()) }

            AlertDialog(
                onDismissRequest = { showPortDialog = false },
                icon = { Icon(Icons.Default.Router, contentDescription = null) },
                title = { Text("Change Port") },
                text = {
                    OutlinedTextField(
                        value = newPort,
                        onValueChange = { newPort = it },
                        label = { Text("Port Number") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            newPort.toIntOrNull()?.let { port ->
                                if (port in 1024..65535) {
                                    viewModel.updatePort(port)
                                    showPortDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPortDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = Dimensions.paddingMedium,
            vertical = Dimensions.paddingSmall
        )
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(Dimensions.paddingMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.mutedText
                )
            }

            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.mutedText
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.paddingMedium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(Dimensions.paddingMedium))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.mutedText
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}