package com.tamersarioglu.flowpay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.SpendingPeriodData
import com.tamersarioglu.flowpay.domain.model.TimePeriod
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.usecase.GetAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
        // Also load spending data immediately
        loadSpendingData(TimePeriod.MONTH)
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val analyticsData = getAnalyticsUseCase()
                _uiState.value = _uiState.value.copy(
                    analyticsData = analyticsData,
                    isLoading = false
                )
                // Load spending data for current selected period
                loadSpendingData(_uiState.value.selectedTimePeriod)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateSelectedTimePeriod(timePeriod: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedTimePeriod = timePeriod)
        loadSpendingData(timePeriod)
    }

    // Temporary function for clearing payment history (remove in production)
    fun clearPaymentHistory() {
        viewModelScope.launch {
            try {
                (repository as? com.tamersarioglu.flowpay.data.repository.SubscriptionRepositoryImpl)?.clearPaymentHistory()
                // Reload data after clearing
                loadAnalytics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to clear payment history: ${e.message}"
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
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to generate payment history: ${e.message}"
                )
            }
        }
    }

    private fun loadSpendingData(timePeriod: TimePeriod) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingSpendingData = true)
                val spendingData = repository.getSpendingByTimePeriod(timePeriod)
                _uiState.value = _uiState.value.copy(
                    spendingData = spendingData,
                    isLoadingSpendingData = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingSpendingData = false,
                    errorMessage = e.message
                )
            }
        }
    }

    data class AnalyticsUiState(
        val analyticsData: AnalyticsData? = null,
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
        val selectedTimePeriod: TimePeriod = TimePeriod.MONTH,
        val spendingData: List<SpendingPeriodData> = emptyList(),
        val isLoadingSpendingData: Boolean = false
    )
}