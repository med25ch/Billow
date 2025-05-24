package com.tamersarioglu.flowpay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.usecase.GetActiveSubscriptionsUseCase
import com.tamersarioglu.flowpay.domain.util.BillingCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionListViewModel @Inject constructor(
    private val getActiveSubscriptionsUseCase: GetActiveSubscriptionsUseCase,
    private val repository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionListUiState())
    val uiState: StateFlow<SubscriptionListUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            getActiveSubscriptionsUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message
                    )
                }
                .collect { subscriptions ->
                    _uiState.value = _uiState.value.copy(
                        subscriptions = subscriptions,
                        isLoading = false,
                        totalMonthlySpend = calculateTotalMonthlySpend(subscriptions)
                    )
                }
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            try {
                repository.deleteSubscription(subscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    private fun calculateTotalMonthlySpend(subscriptions: List<Subscription>): Double {
        return subscriptions.sumOf { subscription ->
            BillingCalculator.calculateMonthlyAmount(subscription)
        }
    }
}

data class SubscriptionListUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val totalMonthlySpend: Double = 0.0
)