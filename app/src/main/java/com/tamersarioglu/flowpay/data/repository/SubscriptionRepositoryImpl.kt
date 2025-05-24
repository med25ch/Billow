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
        
        val spendingData = data.map { SpendingPeriodData(it.month, it.total, 0) }
        
        // If no data exists, generate sample data for demo purposes
        return if (spendingData.isEmpty()) {
            generateSampleData(timePeriod)
        } else {
            spendingData
        }
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

    private fun generateSampleData(timePeriod: TimePeriod): List<SpendingPeriodData> {
        return when (timePeriod) {
            TimePeriod.DAY -> {
                (0..29).map { dayOffset ->
                    val date = LocalDate.now().minusDays(dayOffset.toLong())
                    val amount = (15..85).random().toDouble()
                    SpendingPeriodData(
                        period = date.toString(),
                        totalAmount = amount,
                        subscriptionCount = (1..3).random()
                    )
                }.reversed()
            }
            TimePeriod.WEEK -> {
                (0..11).map { weekOffset ->
                    val date = LocalDate.now().minusWeeks(weekOffset.toLong())
                    val amount = (120..350).random().toDouble()
                    SpendingPeriodData(
                        period = "${date.year}-W${date.dayOfYear / 7 + 1}",
                        totalAmount = amount,
                        subscriptionCount = (3..8).random()
                    )
                }.reversed()
            }
            TimePeriod.MONTH -> {
                (0..11).map { monthOffset ->
                    val date = LocalDate.now().minusMonths(monthOffset.toLong())
                    val amount = (150..450).random().toDouble()
                    SpendingPeriodData(
                        period = "${date.year}-${String.format("%02d", date.monthValue)}",
                        totalAmount = amount,
                        subscriptionCount = (5..12).random()
                    )
                }.reversed()
            }
            TimePeriod.QUARTER -> {
                (0..7).map { quarterOffset ->
                    val date = LocalDate.now().minusMonths((quarterOffset * 3).toLong())
                    val quarter = ((date.monthValue - 1) / 3) + 1
                    val amount = (450..1200).random().toDouble()
                    SpendingPeriodData(
                        period = "${date.year}-Q$quarter",
                        totalAmount = amount,
                        subscriptionCount = (8..15).random()
                    )
                }.reversed()
            }
            TimePeriod.HALF_YEAR -> {
                (0..3).map { halfYearOffset ->
                    val date = LocalDate.now().minusMonths((halfYearOffset * 6).toLong())
                    val halfYear = if (date.monthValue <= 6) "H1" else "H2"
                    val amount = (900..2400).random().toDouble()
                    SpendingPeriodData(
                        period = "${date.year}-$halfYear",
                        totalAmount = amount,
                        subscriptionCount = (10..20).random()
                    )
                }.reversed()
            }
            TimePeriod.YEAR -> {
                (0..4).map { yearOffset ->
                    val year = LocalDate.now().year - yearOffset
                    val amount = (1800..4800).random().toDouble()
                    SpendingPeriodData(
                        period = year.toString(),
                        totalAmount = amount,
                        subscriptionCount = (15..25).random()
                    )
                }.reversed()
            }
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
}