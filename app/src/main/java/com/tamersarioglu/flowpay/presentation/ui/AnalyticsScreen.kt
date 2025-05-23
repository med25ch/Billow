package com.tamersarioglu.flowpay.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.tamersarioglu.flowpay.presentation.viewmodel.AnalyticsViewModel
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview cards
        item {
            OverviewCards(analyticsData = analyticsData)
        }

        // Monthly spending chart
        item {
            MonthlySpendingChart(
                monthlyData = analyticsData.monthlySpending,
                modifier = Modifier.height(200.dp)
            )
        }

        // Category breakdown
        item {
            CategoryBreakdownChart(
                categoryData = analyticsData.categoryBreakdown,
                modifier = Modifier.height(300.dp)
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
            OverviewCard(
                title = "Monthly Spend",
                value = "${String.format("%.2f", analyticsData.totalMonthlySpend)}",
                icon = Icons.Default.AttachMoney
            )
        }
        item {
            OverviewCard(
                title = "Yearly Spend",
                value = "${String.format("%.2f", analyticsData.totalYearlySpend)}",
                icon = Icons.Default.CalendarToday
            )
        }
        item {
            OverviewCard(
                title = "Average Cost",
                value = "${String.format("%.2f", analyticsData.averageSubscriptionCost)}",
                icon = Icons.Default.Analytics
            )
        }
    }
}

@Composable
fun OverviewCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MonthlySpendingChart(
    monthlyData: List<MonthlySpending>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Monthly Spending Trend",
                style = MaterialTheme.typography.titleMedium,
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column {
                categoryData.forEach { category ->
                    CategorySpendingItem(
                        category = category,
                        modifier = Modifier.padding(vertical = 4.dp)
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
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    color = category.category.color,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.category.displayName,
                style = MaterialTheme.typography.bodyMedium
            )
            LinearProgressIndicator(
                progress = category.percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = category.category.color
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${String.format("%.2f", category.totalAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Upcoming Payments (30 days)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (upcomingPayments.isEmpty()) {
                Text(
                    text = "No upcoming payments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    upcomingPayments.take(5).forEach { payment ->
                        UpcomingPaymentItem(
                            payment = payment,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
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
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = payment.subscription.category.color,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = payment.subscription.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = when (payment.daysUntilPayment) {
                    0 -> "Today"
                    1 -> "Tomorrow"
                    else -> "in ${payment.daysUntilPayment} days"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    payment.daysUntilPayment <= 1 -> MaterialTheme.colorScheme.error
                    payment.daysUntilPayment <= 3 -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        Text(
            text = "${String.format("%.2f", payment.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SpendingTrendCard(
    trend: SpendingTrend,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (trend) {
                    SpendingTrend.INCREASING -> Icons.AutoMirrored.Filled.TrendingUp
                    SpendingTrend.DECREASING -> Icons.AutoMirrored.Filled.TrendingDown
                    SpendingTrend.STABLE -> Icons.AutoMirrored.Filled.TrendingFlat
                },
                contentDescription = null,
                tint = when (trend) {
                    SpendingTrend.INCREASING -> MaterialTheme.colorScheme.error
                    SpendingTrend.DECREASING -> Color(0xFF4CAF50)
                    SpendingTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Spending Trend",
                    style = MaterialTheme.typography.titleSmall
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
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}