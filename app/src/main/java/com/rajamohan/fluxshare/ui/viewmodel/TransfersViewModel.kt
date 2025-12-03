package com.rajamohan.fluxshare.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.model.TransferStats
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import com.rajamohan.fluxshare.domain.usecase.CalculateTransferStatsUseCase
import com.rajamohan.fluxshare.domain.usecase.CancelTransferUseCase
import com.rajamohan.fluxshare.domain.usecase.PauseTransferUseCase
import com.rajamohan.fluxshare.domain.usecase.ResumeTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TransfersViewModel @Inject constructor(
    private val transferRepository: TransferRepository,
    private val pauseTransferUseCase: PauseTransferUseCase,
    private val resumeTransferUseCase: ResumeTransferUseCase,
    private val cancelTransferUseCase: CancelTransferUseCase,
    private val calculateStatsUseCase: CalculateTransferStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransfersUiState())
    val uiState: StateFlow<TransfersUiState> = _uiState.asStateFlow()

    init {
        observeActiveTransfers()
    }

    private fun observeActiveTransfers() {
        transferRepository.getActiveTransfers()
            .onEach { transfers ->
                val transfersWithStats = transfers.map { transfer ->
                    val stats = calculateStatsUseCase.calculateStats(
                        completedChunks = transfer.completedChunks,
                        totalChunks = transfer.totalChunks,
                        transferredBytes = transfer.transferredBytes,
                        fileSize = transfer.fileSize,
                        startTime = transfer.startTime
                    )
                    TransferWithStats(transfer, stats)
                }
                _uiState.update { it.copy(activeTransfers = transfersWithStats) }
            }
            .catch { error ->
                Timber.e(error, "Error observing transfers")
                _uiState.update { it.copy(error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    fun pauseTransfer(transferId: String) {
        viewModelScope.launch {
            pauseTransferUseCase(transferId)
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun resumeTransfer(transferId: String) {
        viewModelScope.launch {
            resumeTransferUseCase(transferId)
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun cancelTransfer(transferId: String) {
        viewModelScope.launch {
            cancelTransferUseCase(transferId)
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class TransfersUiState(
    val activeTransfers: List<TransferWithStats> = emptyList(),
    val error: String? = null
)

data class TransferWithStats(
    val transfer: TransferEntity,
    val stats: TransferStats
)
