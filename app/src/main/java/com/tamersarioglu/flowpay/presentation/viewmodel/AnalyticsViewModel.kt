package com.tamersarioglu.flowpay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.SpendingPeriodData
import com.tamersarioglu.flowpay.domain.model.TimePeriod
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.usecase.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val repository: SubscriptionRepository
): ViewModel() {
    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _uiState.value = AnalyticsUiState.Loading
                val analyticsData = getAnalyticsUseCase()
                _uiState.value = AnalyticsUiState.Success(
                    analyticsData = analyticsData,
                    selectedTimePeriod = TimePeriod.MONTH,
                    spendingData = persistentListOf(),
                    isLoadingSpendingData = false
                )
                // Load spending data for current selected period
                loadSpendingData(TimePeriod.MONTH)
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = e.message ?: "Failed to load analytics"
                )
            }
        }
    }

    fun updateSelectedTimePeriod(timePeriod: TimePeriod) {
        val currentState = _uiState.value
        if (currentState is AnalyticsUiState.Success) {
            _uiState.value = currentState.copy(selectedTimePeriod = timePeriod)
            loadSpendingData(timePeriod)
        }
    }

    // Temporary function for clearing payment history (remove in production)
    fun clearPaymentHistory() {
        viewModelScope.launch {
            try {
                (repository as? com.tamersarioglu.flowpay.data.repository.SubscriptionRepositoryImpl)?.clearPaymentHistory()
                // Reload data after clearing
                loadAnalytics()
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = "Failed to clear payment history: ${e.message}"
                )
            }
        }
    }

    // Temporary function for generating random payment history (remove in production)
    fun generateRandomPaymentHistory() {
        viewModelScope.launch {
            try {
                (repository as? com.tamersarioglu.flowpay.data.repository.SubscriptionRepositoryImpl)?.generateRandomPaymentHistory()
                // Reload data after generating
                loadAnalytics()
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = "Failed to generate payment history: ${e.message}"
                )
            }
        }
    }

    private fun loadSpendingData(timePeriod: TimePeriod) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is AnalyticsUiState.Success) {
                    _uiState.value = currentState.copy(isLoadingSpendingData = true)
                    val spendingData = repository.getSpendingByTimePeriod(timePeriod)
                    _uiState.value = currentState.copy(
                        spendingData = spendingData.toPersistentList(),
                        isLoadingSpendingData = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = e.message ?: "Failed to load spending data"
                )
            }
        }
    }

    sealed interface AnalyticsUiState {
        data object Loading : AnalyticsUiState
        
        data class Success(
            val analyticsData: AnalyticsData,
            val selectedTimePeriod: TimePeriod,
            val spendingData: PersistentList<SpendingPeriodData>,
            val isLoadingSpendingData: Boolean
        ) : AnalyticsUiState
        
        data class Error(
            val message: String
        ) : AnalyticsUiState
    }
}