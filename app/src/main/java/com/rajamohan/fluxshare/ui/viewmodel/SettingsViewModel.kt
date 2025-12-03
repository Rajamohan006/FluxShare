package com.rajamohan.fluxshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // In production, inject SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateDeviceName(name: String) {
        _uiState.update { it.copy(deviceName = name) }
        // Save to preferences
    }

    fun toggleEncryption(enabled: Boolean) {
        _uiState.update { it.copy(encryptionEnabled = enabled) }
        // Save to preferences
    }

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkMode = enabled) }
        // Save to preferences
    }

    fun updatePort(port: Int) {
        _uiState.update { it.copy(port = port) }
        // Save to preferences
    }
}

data class SettingsUiState(
    val deviceName: String = android.os.Build.MODEL ?: "Android Device",
    val encryptionEnabled: Boolean = false,
    val darkMode: Boolean = false,
    val port: Int = 8888,
    val autoAcceptFromKnown: Boolean = false
)