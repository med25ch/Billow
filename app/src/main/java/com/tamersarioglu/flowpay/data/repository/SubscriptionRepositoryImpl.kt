package com.tamersarioglu.flowpay.data.repository

import com.tamersarioglu.flowpay.data.database.paymenthistory.MonthlySpendingResult
import com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistoryDao
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionDao
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.CategorySpending
import com.tamersarioglu.flowpay.domain.model.MonthlySpending
import com.tamersarioglu.flowpay.domain.model.SpendingPeriodData
import com.tamersarioglu.flowpay.domain.model.SpendingTrend
import com.tamersarioglu.flowpay.domain.model.TimePeriod
import com.tamersarioglu.flowpay.domain.model.UpcomingPayment
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.domain.util.BillingCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random

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

    override suspend fun getSpendingByTimePeriod(timePeriod: TimePeriod): List<SpendingPeriodData> {
        val data = when (timePeriod) {
            TimePeriod.DAY -> paymentHistoryDao.getDailySpendingHistory()
            TimePeriod.WEEK -> paymentHistoryDao.getWeeklySpendingHistory()
            TimePeriod.MONTH -> paymentHistoryDao.getMonthlySpendingHistory()
            TimePeriod.QUARTER -> paymentHistoryDao.getQuarterlySpendingHistory()
            TimePeriod.HALF_YEAR -> {
                // For half year, we can group monthly data
                val monthlyData = paymentHistoryDao.getMonthlySpendingHistory()
                groupDataByHalfYear(monthlyData)
            }
            TimePeriod.YEAR -> paymentHistoryDao.getYearlySpendingHistory()
        }
        
        return data.map { SpendingPeriodData(it.month, it.total, 0) }
    }

    private fun groupDataByHalfYear(monthlyData: List<MonthlySpendingResult>): List<MonthlySpendingResult> {
        return monthlyData.groupBy { 
            val year = it.month.substring(0, 4)
            val month = it.month.substring(5, 7).toInt()
            val halfYear = if (month <= 6) "H1" else "H2"
            "$year-$halfYear"
        }.map { (period, data) ->
            MonthlySpendingResult(period, data.sumOf { it.total })
        }
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

    // Function to clear payment history for testing
    suspend fun clearPaymentHistory() {
        paymentHistoryDao.clearAllPaymentHistory()
    }

    // Function to generate random payment history for testing
    suspend fun generateRandomPaymentHistory() {
        val testPayments = mutableListOf<com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistory>()
        
        // Common subscription amounts for more realistic data
        val subscriptionAmounts = listOf(9.99, 12.99, 14.99, 19.99, 29.99, 39.99, 49.99, 79.99, 99.99, 129.99)
        
        // Create payments for the last 12 months
        repeat(12) { monthOffset ->
            val baseDate = LocalDate.now().minusMonths(monthOffset.toLong())
            val paymentsInMonth = Random.nextInt(4, 10) // 4-9 payments per month
            
            repeat(paymentsInMonth) {
                testPayments.add(
                    com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistory(
                        subscriptionId = "subscription-${Random.nextInt(1, 8)}",
                        amount = subscriptionAmounts.random(),
                        paymentDate = baseDate.minusDays(Random.nextInt(0, 28).toLong()),
                        currency = "USD"
                    )
                )
            }
        }
        
        testPayments.forEach { payment ->
            paymentHistoryDao.insertPayment(payment)
        }
    }
}