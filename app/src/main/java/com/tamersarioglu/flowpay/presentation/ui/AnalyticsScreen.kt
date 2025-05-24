package com.tamersarioglu.flowpay.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tamersarioglu.flowpay.domain.model.AnalyticsData
import com.tamersarioglu.flowpay.domain.model.CategorySpending
import com.tamersarioglu.flowpay.domain.model.MonthlySpending
import com.tamersarioglu.flowpay.domain.model.SpendingPeriodData
import com.tamersarioglu.flowpay.domain.model.SpendingTrend
import com.tamersarioglu.flowpay.domain.model.TimePeriod
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
                selectedTimePeriod = uiState.selectedTimePeriod,
                spendingData = uiState.spendingData,
                isLoadingSpendingData = uiState.isLoadingSpendingData,
                onTimePeriodChanged = viewModel::updateSelectedTimePeriod,
                onClearData = viewModel::clearPaymentHistory,
                onGenerateRandomData = viewModel::generateRandomPaymentHistory,
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
    selectedTimePeriod: TimePeriod,
    spendingData: List<SpendingPeriodData>,
    isLoadingSpendingData: Boolean,
    onTimePeriodChanged: (TimePeriod) -> Unit,
    onClearData: () -> Unit,
    onGenerateRandomData: () -> Unit,
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

        // Temporary testing buttons (remove in production)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onClearData,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🗑️ Clear Data")
                }
                FilledTonalButton(
                    onClick = onGenerateRandomData,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📊 Generate Data")
                }
            }
        }

        // Spending chart with time period selector
        item {
            SpendingChart(
                selectedTimePeriod = selectedTimePeriod,
                spendingData = spendingData,
                isLoadingSpendingData = isLoadingSpendingData,
                onTimePeriodChanged = onTimePeriodChanged,
                modifier = Modifier.height(400.dp)
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
            EnhancedMetricCard(
                title = "Monthly Spend",
                value = "$${String.format("%.2f", analyticsData.totalMonthlySpend)}",
                icon = Icons.Default.AttachMoney,
                gradientColors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .width(180.dp)
                    .height(140.dp)
            )
        }
        item {
            EnhancedMetricCard(
                title = "Yearly Spend",
                value = "$${String.format("%.2f", analyticsData.totalYearlySpend)}",
                icon = Icons.Default.CalendarToday,
                gradientColors = listOf(
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .width(180.dp)
                    .height(140.dp)
            )
        }
        item {
            EnhancedMetricCard(
                title = "Average Cost",
                value = "$${String.format("%.2f", analyticsData.averageSubscriptionCost)}",
                icon = Icons.Default.Analytics,
                gradientColors = listOf(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                ),
                modifier = Modifier
                    .width(180.dp)
                    .height(140.dp)
            )
        }
    }
}

@Composable
fun EnhancedMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<androidx.compose.ui.graphics.Color>,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient effect
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                color = gradientColors.first().copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header with icon and title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = gradientColors.first().copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = gradientColors.first(),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                    
                    // Value section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            
            // Decorative accent
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(gradientColors.first().copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun SpendingChart(
    selectedTimePeriod: TimePeriod,
    spendingData: List<SpendingPeriodData>,
    isLoadingSpendingData: Boolean,
    onTimePeriodChanged: (TimePeriod) -> Unit,
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
                text = "Spending Trend",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Time period selector
            TimePeriodSelector(
                selectedPeriod = selectedTimePeriod,
                onPeriodSelected = onTimePeriodChanged,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Chart with loading state
            if (isLoadingSpendingData) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading ${selectedTimePeriod.displayName.lowercase()} data...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Enhanced bar chart implementation
                EnhancedBarChart(
                    data = spendingData.map { it.totalAmount.toFloat() },
                    labels = spendingData.map { formatPeriodLabel(it.period, selectedTimePeriod) },
                    timePeriod = selectedTimePeriod,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun EnhancedBarChart(
    data: List<Float>,
    labels: List<String>,
    timePeriod: TimePeriod,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (data.isEmpty()) {
            // Enhanced empty state with more information
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Payment History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No ${timePeriod.displayName.lowercase()} spending data available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add subscriptions or generate test data to see charts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        // Metrics Section
        ChartMetrics(
            data = data,
            timePeriod = timePeriod,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Chart Section
        val maxValue = data.maxOrNull() ?: 0f
        val primaryColor = MaterialTheme.colorScheme.primary

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                ) {
                    if (data.isNotEmpty() && maxValue > 0) {
                        val barWidth = size.width / data.size * 0.7f
                        val barSpacing = size.width / data.size * 0.3f
                        val maxBarHeight = size.height * 0.8f

                        data.forEachIndexed { index, value ->
                            val barHeight = (value / maxValue) * maxBarHeight
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

                // Labels Section
                if (labels.isNotEmpty() && data.size <= 12) { // Show labels only for smaller datasets
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(labels.size) { index ->
                            Text(
                                text = labels[index],
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartMetrics(
    data: List<Float>,
    timePeriod: TimePeriod,
    modifier: Modifier = Modifier
) {
    val total = data.sum()
    val average = if (data.isNotEmpty()) data.average() else 0.0
    val maxValue = data.maxOrNull() ?: 0f
    val minValue = data.minOrNull() ?: 0f

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MetricChip(
                label = "Total",
                value = "$${String.format("%.0f", total)}",
                color = MaterialTheme.colorScheme.primary
            )
        }
        item {
            MetricChip(
                label = "Average",
                value = "$${String.format("%.0f", average)}",
                color = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            MetricChip(
                label = "Highest",
                value = "$${String.format("%.0f", maxValue)}",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        item {
            MetricChip(
                label = "${data.size} ${timePeriod.displayName.lowercase()}s",
                value = "",
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

fun formatPeriodLabel(period: String, timePeriod: TimePeriod): String {
    return when (timePeriod) {
        TimePeriod.DAY -> {
            // Format: 2024-01-15 -> Jan 15
            try {
                val parts = period.split("-")
                if (parts.size >= 3) {
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    "${monthNames[month]} $day"
                } else period
            } catch (e: Exception) { period }
        }
        TimePeriod.WEEK -> {
            // Format: 2024-W03 -> W3
            period.substringAfter("-W").let { "W$it" }
        }
        TimePeriod.MONTH -> {
            // Format: 2024-01 -> Jan
            try {
                val parts = period.split("-")
                if (parts.size >= 2) {
                    val month = parts[1].toInt()
                    val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    monthNames[month]
                } else period
            } catch (e: Exception) { period }
        }
        TimePeriod.QUARTER -> {
            // Format: 2024-Q1 -> Q1
            period.substringAfter("-")
        }
        TimePeriod.HALF_YEAR -> {
            // Format: 2024-H1 -> H1
            period.substringAfter("-")
        }
        TimePeriod.YEAR -> {
            // Format: 2024 -> 2024
            period
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
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimePeriod.values().toList()) { period ->
            val isSelected = period == selectedPeriod
            
            Surface(
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .selectable(
                        selected = isSelected,
                        onClick = { onPeriodSelected(period) }
                    )
            ) {
                Text(
                    text = period.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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