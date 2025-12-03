package com.rajamohan.fluxshare.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajamohan.fluxshare.domain.model.Device
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import com.rajamohan.fluxshare.domain.usecase.DiscoverPeersUseCase
import com.rajamohan.fluxshare.domain.usecase.SendFileUseCase
import com.rajamohan.fluxshare.service.TransferService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
@HiltViewModel @OptIn(InternalSerializationApi::class)
class DiscoveryViewModel @Inject constructor(
    private val discoverPeersUseCase: DiscoverPeersUseCase,
    private val sendFileUseCase: SendFileUseCase,
    private val transferRepository: TransferRepository
) : ViewModel() {


    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {
        startDiscovery()
        observeDevices()
    }

    private fun startDiscovery() {
        discoverPeersUseCase(viewModelScope)
        _uiState.update { it.copy(isDiscovering = true) }
    }

    private fun observeDevices() {
        discoverPeersUseCase.getDiscoveredDevices()
            .onEach { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
            .launchIn(viewModelScope)
    }

    fun toggleDiscovery() {
        if (_uiState.value.isDiscovering) {
            discoverPeersUseCase.stopDiscovery()
            _uiState.update { it.copy(isDiscovering = false) }
        } else {
            startDiscovery()
        }
    }

    // THE KEY METHOD - Start File Transfer
    fun startFileTransfer(
        context: Context,
        fileUri: Uri,
        fileName: String,
        fileSize: Long,
        targetDevice: Device
    ) {
        viewModelScope.launch {
            try {
                // Copy file to cache directory for access
                val cacheFile = File(context.cacheDir, fileName)
                context.contentResolver.openInputStream(fileUri)?.use { input ->
                    FileOutputStream(cacheFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Create transfer record
                val result = sendFileUseCase(
                    filePath = cacheFile.absolutePath,
                    fileName = fileName,
                    fileSize = fileSize,
                    mimeType = context.contentResolver.getType(fileUri),
                    targetDevice = targetDevice,
                    useEncryption = false
                )

                result.onSuccess { transferId ->
                    // Start the Transfer Service
                    TransferService.startTransfer(context, transferId)

                    _uiState.update {
                        it.copy(transferStarted = "Transfer started: $fileName")
                    }

                    Timber.d("Transfer started: $transferId")
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to start transfer: ${error.message}")
                    }
                    Timber.e(error, "Failed to start transfer")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error preparing file: ${e.message}")
                }
                Timber.e(e, "Error preparing file for transfer")
            }
        }
    }

    fun clearTransferMessage() {
        _uiState.update { it.copy(transferStarted = null) }
    }

    override fun onCleared() {
        super.onCleared()
        discoverPeersUseCase.stopDiscovery()
    }
}

data class DiscoveryUiState @OptIn(InternalSerializationApi::class) constructor(
    val devices: List<Device> = emptyList(),
    val isDiscovering: Boolean = false,
    val transferStarted: String? = null,
    val error: String? = null
)
