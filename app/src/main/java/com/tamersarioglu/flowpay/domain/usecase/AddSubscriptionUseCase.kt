package com.tamersarioglu.flowpay.domain.usecase

import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import java.time.LocalDate
import javax.inject.Inject

class AddSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) {
        val subscriptionWithNextBilling = subscription.copy(
            nextBillingDate = calculateNextBillingDate(subscription)
        )
        subscriptionRepository.insertSubscription(subscriptionWithNextBilling)
    }

    private fun calculateNextBillingDate(subscription: Subscription): LocalDate {
        return when (subscription.billingInterval) {
            BillingInterval.WEEKLY -> subscription.startDate.plusWeeks(1)
            BillingInterval.MONTHLY -> subscription.startDate.plusMonths(1)
            BillingInterval.QUARTERLY -> subscription.startDate.plusMonths(3)
            BillingInterval.YEARLY -> subscription.startDate.plusYears(1)
            BillingInterval.CUSTOM -> subscription.startDate.plusDays(subscription.customIntervalDays.toLong())
        }
    }
}
