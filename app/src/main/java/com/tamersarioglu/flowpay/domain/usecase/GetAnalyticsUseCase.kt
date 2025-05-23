package com.tamersarioglu.flowpay.domain.usecase

import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAnalyticsUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(): AnalyticsData = subscriptionRepository.getAnalyticsData()
}