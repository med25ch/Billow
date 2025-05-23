package com.tamersarioglu.flowpay.domain.model

import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import java.time.YearMonth

data class MonthlySpending(
    val month: YearMonth,
    val totalAmount: Double,
    val subscriptionCount: Int
)

data class CategorySpending(
    val category: SubscriptionCategory,
    val totalAmount: Double,
    val subscriptionCount: Int,
    val percentage: Float
)

data class AnalyticsData(
    val monthlySpending: List<MonthlySpending>,
    val categoryBreakdown: List<CategorySpending>,
    val totalMonthlySpend: Double,
    val totalYearlySpend: Double,
    val averageSubscriptionCost: Double,
    val nextPayments: List<UpcomingPayment>,
    val spendingTrend: SpendingTrend
)

data class UpcomingPayment(
    val subscription: Subscription,
    val daysUntilPayment: Int,
    val amount: Double
)

enum class SpendingTrend {
    INCREASING,
    DECREASING,
    STABLE
}