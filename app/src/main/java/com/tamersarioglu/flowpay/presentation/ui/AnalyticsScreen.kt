package com.tamersarioglu.flowpay.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.CategorySpending
import com.tamersarioglu.flowpay.domain.model.MonthlySpending
import com.tamersarioglu.flowpay.domain.model.SpendingTrend
import com.tamersarioglu.flowpay.domain.model.UpcomingPayment
import com.tamersarioglu.flowpay.presentation.ui.components.CategoryIndicator
import com.tamersarioglu.flowpay.presentation.ui.components.LoadingCard
import com.tamersarioglu.flowpay.presentation.ui.components.MetricCard
import com.tamersarioglu.flowpay.presentation.viewmodel.AnalyticsViewModel
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            LoadingCard(
                modifier = Modifier.fillMaxSize(),
                message = "Loading analytics..."
            )
        }

        uiState.analyticsData != null -> {
            AnalyticsContent(
                analyticsData = uiState.analyticsData!!,
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            ErrorState(
                message = uiState.errorMessage ?: "Failed to load analytics",
                onRetry = viewModel::loadAnalytics,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun AnalyticsContent(
    analyticsData: AnalyticsData,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Overview cards
        item {
            OverviewCards(analyticsData = analyticsData)
        }

        // Monthly spending chart
        item {
            MonthlySpendingChart(
                monthlyData = analyticsData.monthlySpending,
                modifier = Modifier.height(220.dp)
            )
        }

        // Category breakdown
        item {
            CategoryBreakdownChart(
                categoryData = analyticsData.categoryBreakdown,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Upcoming payments
        item {
            UpcomingPaymentsSection(upcomingPayments = analyticsData.nextPayments)
        }

        // Spending trend
        item {
            SpendingTrendCard(trend = analyticsData.spendingTrend)
        }
    }
}

@Composable
fun OverviewCards(
    analyticsData: AnalyticsData,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        item {
            MetricCard(
                title = "Monthly Spend",
                value = "$${String.format("%.2f", analyticsData.totalMonthlySpend)}",
                icon = Icons.Default.AttachMoney,
                modifier = Modifier
                    .width(160.dp)
                    .height(120.dp)
            )
        }
        item {
            MetricCard(
                title = "Yearly Spend",
                value = "$${String.format("%.2f", analyticsData.totalYearlySpend)}",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier
                    .width(160.dp)
                    .height(120.dp)
            )
        }
        item {
            MetricCard(
                title = "Average Cost",
                value = "$${String.format("%.2f", analyticsData.averageSubscriptionCost)}",
                icon = Icons.Default.Analytics,
                modifier = Modifier
                    .width(160.dp)
                    .height(120.dp)
            )
        }
    }
}

@Composable
fun MonthlySpendingChart(
    monthlyData: List<MonthlySpending>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Monthly Spending Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Simple bar chart implementation
            SimpleBarChart(
                data = monthlyData.map { it.totalAmount.toFloat() },
                labels = monthlyData.map { it.month.format(DateTimeFormatter.ofPattern("MMM")) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 0f
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = if (data.isNotEmpty()) size.width / data.size * 0.8f else 0f
        val barSpacing = if (data.isNotEmpty()) size.width / data.size * 0.2f else 0f
        val maxBarHeight = size.height * 0.8f

        data.forEachIndexed { index, value ->
            val barHeight = if (maxValue > 0) (value / maxValue) * maxBarHeight else 0f
            val x = index * (barWidth + barSpacing) + barSpacing / 2
            val y = size.height - barHeight

            drawRect(
                color = primaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun CategoryBreakdownChart(
    categoryData: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categoryData.forEach { category ->
                    CategorySpendingItem(
                        category = category
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySpendingItem(
    category: CategorySpending,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIndicator(
            color = category.category.color,
            size = 16
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.category.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { category.percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = category.category.color,
                trackColor = category.category.color.copy(alpha = 0.2f),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format("%.2f", category.totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${String.format("%.1f", category.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UpcomingPaymentsSection(
    upcomingPayments: List<UpcomingPayment>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Upcoming Payments (30 days)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (upcomingPayments.isEmpty()) {
                Text(
                    text = "No upcoming payments",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    upcomingPayments.take(5).forEach { payment ->
                        UpcomingPaymentItem(payment = payment)
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingPaymentItem(
    payment: UpcomingPayment,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIndicator(
            color = payment.subscription.category.color,
            size = 12
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.subscription.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = when (payment.daysUntilPayment) {
                    0 -> "Today"
                    1 -> "Tomorrow"
                    else -> "in ${payment.daysUntilPayment} days"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    payment.daysUntilPayment <= 1 -> MaterialTheme.colorScheme.error
                    payment.daysUntilPayment <= 3 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Text(
            text = "$${String.format("%.2f", payment.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SpendingTrendCard(
    trend: SpendingTrend,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = when (trend) {
                    SpendingTrend.INCREASING -> MaterialTheme.colorScheme.errorContainer
                    SpendingTrend.DECREASING -> MaterialTheme.colorScheme.secondaryContainer
                    SpendingTrend.STABLE -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = when (trend) {
                        SpendingTrend.INCREASING -> Icons.AutoMirrored.Filled.TrendingUp
                        SpendingTrend.DECREASING -> Icons.AutoMirrored.Filled.TrendingDown
                        SpendingTrend.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when (trend) {
                        SpendingTrend.INCREASING -> MaterialTheme.colorScheme.onErrorContainer
                        SpendingTrend.DECREASING -> MaterialTheme.colorScheme.onSecondaryContainer
                        SpendingTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Spending Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (trend) {
                        SpendingTrend.INCREASING -> "Your spending is increasing"
                        SpendingTrend.DECREASING -> "Your spending is decreasing"
                        SpendingTrend.STABLE -> "Your spending is stable"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilledTonalButton(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}