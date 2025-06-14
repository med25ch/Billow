package com.tamersarioglu.flowpay.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.usecase.GetActiveSubscriptionsUseCase
import com.tamersarioglu.flowpay.domain.util.BillingCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
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

    // List of all fetched subscriptions
    private lateinit var allSubscriptions: List<Subscription>

    // filter options
    private val filterOptions = MutableStateFlow(FilterOptions())



    private val _uiState = MutableStateFlow<SubscriptionListUiState>(SubscriptionListUiState.Loading)
    val uiState: StateFlow<SubscriptionListUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            getActiveSubscriptionsUseCase()
                .catch { exception ->
                    _uiState.value = SubscriptionListUiState.Error(
                        message = exception.message ?: "Failed to load subscriptions"
                    )
                }
                .collect { subscriptions ->
                    allSubscriptions = subscriptions
                    _uiState.value = SubscriptionListUiState.Success(
                        subscriptions = subscriptions.toPersistentList(),
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
                _uiState.value = SubscriptionListUiState.Error(
                    message = "Failed to delete subscription: ${e.message}"
                )
            }
        }
    }

    private fun calculateTotalMonthlySpend(subscriptions: List<Subscription>): Double {
        return subscriptions.sumOf { subscription ->
            BillingCalculator.calculateMonthlyAmount(subscription)
        }
    }

    fun updateFilterOptions(newOptions: FilterOptions){
        filterOptions.value = newOptions
        applyFilters()
    }

    fun resetFilters() {
        filterOptions.value = FilterOptions() // Reset to default values
        applyFilters() // Re-apply filters to show full list
    }

    private fun applyFilters() {
        viewModelScope.launch {
            _uiState.value = SubscriptionListUiState.Loading

            val filters = filterOptions.value

            val filteredSubscriptions = allSubscriptions.filter { subscription ->
                val matchesPrice = subscription.price in filters.priceRange

                val matchesBilling = filters.billing?.let {
                    subscription.billingInterval.displayName == it
                } ?: true // No filter applied if null

                val matchesCategory = if (filters.categories.isNotEmpty()) {
                    subscription.category.displayName in filters.categories
                } else true

                val matchesSearch = subscription.name.contains(filters.searchQuery, ignoreCase = true)

                matchesPrice && matchesBilling && matchesCategory && matchesSearch
            }

            _uiState.value = SubscriptionListUiState.Success(
                subscriptions = filteredSubscriptions.toPersistentList(),
                totalMonthlySpend = calculateTotalMonthlySpend(filteredSubscriptions)
            )
        }
    }

    sealed interface SubscriptionListUiState {
        data object Loading : SubscriptionListUiState
        
        data class Success(
            val subscriptions: PersistentList<Subscription>,
            val totalMonthlySpend: Double
        ) : SubscriptionListUiState
        
        data class Error(
            val message: String
        ) : SubscriptionListUiState
    }

    // Hold current selected filters
    data class FilterOptions(
        val priceRange: ClosedFloatingPointRange<Float> = 0f..1000f,
        val billing: String? = null,
        val categories: Set<String> = emptySet(),
        val searchQuery: String = ""
    )


}