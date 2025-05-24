package com.tamersarioglu.flowpay.domain.usecase

import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.util.BillingCalculator
import javax.inject.Inject

class AddSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription) {
        val subscriptionWithNextBilling = subscription.copy(
            nextBillingDate = BillingCalculator.calculateNextBillingDate(subscription, subscription.startDate)
        )
        subscriptionRepository.insertSubscription(subscriptionWithNextBilling)
    }
}
