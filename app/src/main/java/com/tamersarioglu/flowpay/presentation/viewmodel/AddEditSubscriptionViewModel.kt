package com.tamersarioglu.flowpay.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.usecase.AddSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AddEditSubscriptionViewModel @Inject constructor(
    private val addSubscriptionUseCase: AddSubscriptionUseCase,
    private val repository: SubscriptionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subscriptionId: String? = savedStateHandle.get<String>("subscriptionId")

    private val _uiState = MutableStateFlow(AddEditSubscriptionUiState())
    val uiState: StateFlow<AddEditSubscriptionUiState> = _uiState.asStateFlow()

    init {
        subscriptionId?.let { loadSubscription(it) }
    }

    private fun loadSubscription(id: String) {
        viewModelScope.launch {
            repository.getSubscriptionById(id)?.let { subscription ->
                _uiState.value = _uiState.value.copy(
                    name = subscription.name,
                    price = subscription.price.toString(),
                    billingInterval = subscription.billingInterval,
                    customIntervalDays = subscription.customIntervalDays.toString(),
                    startDate = subscription.startDate,
                    category = subscription.category,
                    description = subscription.description ?: "",
                    isEditMode = true
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun updatePrice(price: String) {
        _uiState.value = _uiState.value.copy(price = price)
    }

    fun updateBillingInterval(interval: BillingInterval) {
        _uiState.value = _uiState.value.copy(billingInterval = interval)
    }

    fun updateCustomIntervalDays(days: String) {
        _uiState.value = _uiState.value.copy(customIntervalDays = days)
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun updateCategory(category: SubscriptionCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun saveSubscription() {
        val state = _uiState.value

        if (!isFormValid(state)) {
            _uiState.value = state.copy(showValidationErrors = true)
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = state.copy(isSaving = true)

                val subscription = if (state.isEditMode && subscriptionId != null) {
                    repository.getSubscriptionById(subscriptionId)?.copy(
                        name = state.name,
                        price = state.price.toDouble(),
                        billingInterval = state.billingInterval,
                        customIntervalDays = if (state.billingInterval == BillingInterval.CUSTOM)
                            state.customIntervalDays.toInt() else 0,
                        startDate = state.startDate,
                        category = state.category,
                        description = state.description.takeIf { it.isNotBlank() }
                    )
                } else {
                    Subscription(
                        name = state.name,
                        price = state.price.toDouble(),
                        billingInterval = state.billingInterval,
                        customIntervalDays = if (state.billingInterval == BillingInterval.CUSTOM)
                            state.customIntervalDays.toInt() else 0,
                        startDate = state.startDate,
                        nextBillingDate = state.startDate,
                        category = state.category,
                        description = state.description.takeIf { it.isNotBlank() }
                    )
                }

                subscription?.let {
                    if (state.isEditMode) {
                        repository.updateSubscription(it)
                    } else {
                        addSubscriptionUseCase(it)
                    }
                    _uiState.value = state.copy(isSaved = true, isSaving = false)
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun isFormValid(state: AddEditSubscriptionUiState): Boolean {
        return state.name.isNotBlank() &&
                state.price.toDoubleOrNull() != null &&
                state.price.toDouble() > 0 &&
                (state.billingInterval != BillingInterval.CUSTOM ||
                        (state.customIntervalDays.toIntOrNull() != null && state.customIntervalDays.toInt() > 0))
    }
}

data class AddEditSubscriptionUiState(
    val name: String = "",
    val price: String = "",
    val billingInterval: BillingInterval = BillingInterval.MONTHLY,
    val customIntervalDays: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val category: SubscriptionCategory = SubscriptionCategory.OTHER,
    val description: String = "",
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showValidationErrors: Boolean = false,
    val errorMessage: String? = null
)