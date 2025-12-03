package com.rajamohan.fluxshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajamohan.fluxshare.domain.model.TransferState
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import com.rajamohan.fluxshare.domain.usecase.DiscoverPeersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val discoverPeersUseCase: DiscoverPeersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeStatistics()
    }

    @OptIn(InternalSerializationApi::class)
    private fun observeStatistics() {
        viewModelScope.launch {
            combine(
                transferRepository.getAllTransfers(),
                transferRepository.getActiveTransfers(),
                discoverPeersUseCase.getDiscoveredDevices()
            ) { allTransfers, activeTransfers, devices ->
                val completed = allTransfers.count { it.state == TransferState.COMPLETED }
                val failed = allTransfers.count { it.state == TransferState.FAILED }
                val totalSize = allTransfers
                    .filter { it.state == TransferState.COMPLETED }
                    .sumOf { it.fileSize }

                HomeStats(
                    totalTransfers = allTransfers.size,
                    activeTransfers = activeTransfers.size,
                    completedTransfers = completed,
                    failedTransfers = failed,
                    totalDataTransferred = totalSize,
                    availableDevices = devices.size
                )
            }
                .onEach { stats ->
                    _uiState.update { it.copy(stats = stats) }
                }
                .catch { error ->
                    Timber.e(error, "Error loading statistics")
                    _uiState.update { it.copy(error = error.message) }
                }
                .launchIn(this)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HomeUiState(
    val stats: HomeStats = HomeStats(),
    val error: String? = null
)

data class HomeStats(
    val totalTransfers: Int = 0,
    val activeTransfers: Int = 0,
    val completedTransfers: Int = 0,
    val failedTransfers: Int = 0,
    val totalDataTransferred: Long = 0,
    val availableDevices: Int = 0
)