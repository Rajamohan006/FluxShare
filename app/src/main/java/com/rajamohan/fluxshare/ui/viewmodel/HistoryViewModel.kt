package com.rajamohan.fluxshare.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajamohan.fluxshare.domain.model.TransferDirection
import com.rajamohan.fluxshare.domain.model.TransferEntity
import com.rajamohan.fluxshare.domain.model.TransferState
import com.rajamohan.fluxshare.domain.repository.TransferRepository
import com.rajamohan.fluxshare.domain.usecase.GetTransferHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getTransferHistoryUseCase: GetTransferHistoryUseCase,
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterType.ALL)
    val selectedFilter: StateFlow<FilterType> = _selectedFilter.asStateFlow()

    init {
        observeTransferHistory()
    }

    private fun observeTransferHistory() {
        combine(
            getTransferHistoryUseCase(),
            _selectedFilter
        ) { transfers, filter ->
            val filtered = when (filter) {
                FilterType.ALL -> transfers
                FilterType.SENT -> transfers.filter { it.direction == TransferDirection.SEND }
                FilterType.RECEIVED -> transfers.filter { it.direction == TransferDirection.RECEIVE }
                FilterType.COMPLETED -> transfers.filter { it.state == TransferState.COMPLETED }
                FilterType.FAILED -> transfers.filter { it.state == TransferState.FAILED }
            }
            filtered
        }
            .onEach { transfers ->
                _uiState.update { it.copy(transfers = transfers) }
            }
            .catch { error ->
                Timber.e(error, "Error loading history")
                _uiState.update { it.copy(error = error.message) }
            }
            .launchIn(viewModelScope)
    }

    fun setFilter(filter: FilterType) {
        _selectedFilter.value = filter
    }

    fun deleteTransfer(transferId: String) {
        viewModelScope.launch {
            try {
                transferRepository.deleteTransfer(transferId)
            } catch (e: Exception) {
                Timber.e(e, "Error deleting transfer")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                transferRepository.clearCompletedTransfers()
            } catch (e: Exception) {
                Timber.e(e, "Error clearing history")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class HistoryUiState(
    val transfers: List<TransferEntity> = emptyList(),
    val error: String? = null
)

enum class FilterType {
    ALL, SENT, RECEIVED, COMPLETED, FAILED
}