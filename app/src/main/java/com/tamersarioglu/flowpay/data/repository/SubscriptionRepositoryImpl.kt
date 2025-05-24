package com.tamersarioglu.flowpay.data.repository

import com.tamersarioglu.flowpay.data.database.paymenthistory.MonthlySpendingResult
import com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistoryDao
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionDao
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.CategorySpending
import com.tamersarioglu.flowpay.domain.model.MonthlySpending
import com.tamersarioglu.flowpay.domain.model.SpendingTrend
import com.tamersarioglu.flowpay.domain.model.UpcomingPayment
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.util.BillingCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val paymentHistoryDao: PaymentHistoryDao
) : SubscriptionRepository {
    override fun getActiveSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getActiveSubscriptions()
    }

    override fun getAllSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllSubscriptions()
    }

    override suspend fun getSubscriptionById(id: String): Subscription? {
        return subscriptionDao.getSubscriptionById(id)
    }

    override suspend fun insertSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription)
    }

    override suspend fun updateSubscription(subscription: Subscription) {
        subscriptionDao.updateSubscription(subscription)
    }

    override suspend fun deleteSubscription(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription)
    }

    override suspend fun getUpcomingPayments(days: Int): List<UpcomingPayment> {
        val endDate = LocalDate.now().plusDays(days.toLong())
        val subscriptions = subscriptionDao.getSubscriptionsDueBetween(LocalDate.now(), endDate)

        return subscriptions.map { subscription ->
            val daysUntil =
                ChronoUnit.DAYS.between(LocalDate.now(), subscription.nextBillingDate).toInt()
            UpcomingPayment(subscription, daysUntil, subscription.price)
        }.sortedBy { it.daysUntilPayment }
    }

    override suspend fun getAnalyticsData(): AnalyticsData {
        val monthlySpending = paymentHistoryDao.getMonthlySpendingHistory()
        val categorySpending = subscriptionDao.getCategorySpending()
        val activeSubscriptions = subscriptionDao.getActiveSubscriptions().first()

        val totalMonthlySpend = activeSubscriptions.sumOf { subscription ->
            BillingCalculator.calculateMonthlyAmount(subscription)
        }

        return AnalyticsData(
            monthlySpending = monthlySpending.map {
                MonthlySpending(YearMonth.parse(it.month), it.total, 0)
            },
            categoryBreakdown = categorySpending.map {
                CategorySpending(
                    SubscriptionCategory.valueOf(it.category),
                    it.totalAmount,
                    it.count,
                    (it.totalAmount / totalMonthlySpend * 100).toFloat()
                )
            },
            totalMonthlySpend = totalMonthlySpend,
            totalYearlySpend = totalMonthlySpend * 12,
            averageSubscriptionCost = if (activeSubscriptions.isNotEmpty())
                totalMonthlySpend / activeSubscriptions.size else 0.0,
            nextPayments = getUpcomingPayments(30),
            spendingTrend = calculateSpendingTrend(monthlySpending)
        )
    }



    private fun calculateSpendingTrend(monthlyData: List<MonthlySpendingResult>): SpendingTrend {
        if (monthlyData.size < 3) return SpendingTrend.STABLE

        val recent = monthlyData.take(3).map { it.total }
        val trend = recent.zipWithNext { a, b -> b - a }.average()

        return when {
            trend > 50 -> SpendingTrend.INCREASING
            trend < -50 -> SpendingTrend.DECREASING
            else -> SpendingTrend.STABLE
        }
    }
}