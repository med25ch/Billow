package com.tamersarioglu.flowpay.domain.repository

import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.SpendingPeriodData
import com.tamersarioglu.flowpay.domain.model.TimePeriod
import com.tamersarioglu.flowpay.domain.model.UpcomingPayment
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getActiveSubscriptions(): Flow<List<Subscription>>
    fun getAllSubscriptions(): Flow<List<Subscription>>
    suspend fun getSubscriptionById(id: String): Subscription?
    suspend fun insertSubscription(subscription: Subscription)
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun deleteSubscription(subscription: Subscription)
    suspend fun getUpcomingPayments(days: Int): List<UpcomingPayment>
    suspend fun getAnalyticsData(): AnalyticsData
    suspend fun getSpendingByTimePeriod(timePeriod: TimePeriod): List<SpendingPeriodData>
}