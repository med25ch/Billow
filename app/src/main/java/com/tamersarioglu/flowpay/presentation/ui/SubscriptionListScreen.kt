package com.tamersarioglu.flowpay.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.presentation.ui.components.CategoryIndicator
import com.tamersarioglu.flowpay.presentation.ui.components.EmptyStateCard
import com.tamersarioglu.flowpay.presentation.ui.components.LoadingCard
import com.tamersarioglu.flowpay.presentation.ui.components.MetricCard
import com.tamersarioglu.flowpay.presentation.ui.components.SearchBarWithExternalFilter
import com.tamersarioglu.flowpay.presentation.ui.components.SubscriptionFilterSheet
import com.tamersarioglu.flowpay.presentation.ui.theme.FlowPayTheme
import com.tamersarioglu.flowpay.presentation.viewmodel.SubscriptionListViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@SuppressLint("DefaultLocale")
@Composable
fun SubscriptionListScreen(
    onNavigateToAddSubscription: () -> Unit,
    onNavigateToEditSubscription: (String) -> Unit,
    viewModel: SubscriptionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val currentState = uiState) {
        is SubscriptionListViewModel.SubscriptionListUiState.Loading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Placeholder header
                MetricCard(
                    title = "Monthly Spending",
                    value = "$0.00",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Add subscription button
                FilledTonalButton(
                    onClick = onNavigateToAddSubscription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Add Subscription")
                }

                LoadingCard(
                    modifier = Modifier.fillMaxWidth(),
                    message = "Loading subscriptions..."
                )
            }
        }

        is SubscriptionListViewModel.SubscriptionListUiState.Success -> {
            SubscriptionListContent(
                subscriptions = currentState.subscriptions,
                totalMonthlySpend = currentState.totalMonthlySpend,
                onNavigateToAddSubscription = onNavigateToAddSubscription,
                onNavigateToEditSubscription = onNavigateToEditSubscription,
                onDeleteSubscription = viewModel::deleteSubscription,
                onApplyFilters = viewModel::updateFilterOptions,
                onResetFilters = viewModel::resetFilters
            )
        }

        is SubscriptionListViewModel.SubscriptionListUiState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                FilledTonalButton(
                    onClick = onNavigateToAddSubscription
                ) {
                    Text("Add Subscription")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionListContent(
    subscriptions: PersistentList<Subscription>,
    totalMonthlySpend: Double,
    onNavigateToAddSubscription: () -> Unit,
    onNavigateToEditSubscription: (String) -> Unit,
    onDeleteSubscription: (Subscription) -> Unit,
    onApplyFilters : (SubscriptionListViewModel.FilterOptions) -> Unit,
    onResetFilters : () -> Unit
) {


    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    var priceRange by remember { mutableStateOf(10f..100f) }
    var billing by remember { mutableStateOf("Monthly") }
    var categories by remember { mutableStateOf(setOf<String>()) }
    var query by remember { mutableStateOf("") }


    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        SearchBarWithExternalFilter(
            query = query,
            onQueryChange = { query = it },
            onFilterClick = { showBottomSheet = true }
        )



        // Header with total spending
        MetricCard(
            title = "Monthly Spending",
            value = "$${String.format("%.2f", totalMonthlySpend)}",
            icon = Icons.Default.AttachMoney,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        if (subscriptions.isEmpty()) {
            EmptyStateCard(
                title = "No subscriptions yet",
                description = "Start tracking your subscriptions to take control of your spending",
                icon = Icons.Default.Subscriptions,
                actionText = "Add Your First Subscription",
                onAction = onNavigateToAddSubscription,
                modifier = Modifier.fillMaxWidth()
            )
        } else {

            SubscriptionList(
                subscriptions = subscriptions,
                onNavigateToEditSubscription = { onNavigateToEditSubscription(it) },
                onDeleteSubscription = { onDeleteSubscription(it) }
            )

        }
    }

        FloatingActionButton(
            onClick = onNavigateToAddSubscription ,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // spacing from edges
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }


        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                    SubscriptionFilterSheet(
                        priceRange = priceRange,
                        onPriceChange = { priceRange = it },
                        selectedBilling = billing,
                        onBillingChange = { billing = it },
                        selectedCategories = categories,
                        onCategoryToggle = { cat ->
                            categories = if (cat in categories) categories - cat else categories + cat
                        },
                        onApply = {

                            val newOptions = SubscriptionListViewModel.FilterOptions(
                                priceRange = priceRange,
                                billing = billing,
                                categories = categories,
                            )

                            onApplyFilters(newOptions)
                        },
                        onClearAll = onResetFilters
                    )
                }
            }
        }
}


@Composable
fun SubscriptionList(
    subscriptions: List<Subscription>,
    onNavigateToEditSubscription: (String) -> Unit,
    onDeleteSubscription: (Subscription) -> Unit
) {

    var subscriptionToConfirm by remember { mutableStateOf<Subscription?>(null) }
    var resetTrigger by remember { mutableStateOf<Pair<String, Int>?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = subscriptions,
            key = { it.id }
        ) { subscription ->
            SwipeToDeleteItem(
                onSwipeConfirmed  = { subscriptionToConfirm = subscription },
                resetSwipeTrigger = resetTrigger,
                itemId = subscription.id
            ) {
                ModernSubscriptionCard(
                    subscription = subscription,
                    onEdit = { onNavigateToEditSubscription(subscription.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }


    subscriptionToConfirm?.let { sub ->

    AlertDialog(
            onDismissRequest = {
                resetTrigger = sub.id to (resetTrigger?.second?.plus(1) ?: 0)
                subscriptionToConfirm = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Delete Subscription",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete subscription ? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubscription(sub)
                        subscriptionToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    resetTrigger = sub.id to (resetTrigger?.second?.plus(1) ?: 0)
                    subscriptionToConfirm = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

}


@Composable
fun SwipeToDeleteItem(
    onSwipeConfirmed: () -> Unit, // called when swipe threshold is passed
    resetSwipeTrigger: Pair<String, Int>? = null,
    itemId: String,
    shape: Shape = RoundedCornerShape(12.dp),
    threshold: Float = 300f,
    content: @Composable () -> Unit
) {
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isSwipingLeft = offsetX.value < 0

    // 🔁 External reset logic
    LaunchedEffect(resetSwipeTrigger) {
        if (resetSwipeTrigger?.first == itemId) {
            offsetX.animateTo(0f, tween(300))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        // Background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Red.copy(alpha = 0.8f),
                            Color.Red.copy(alpha = 0.4f)
                        ),
                        startX = 0f,
                        endX = 1000f
                    )
                )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier
                    .align(if (isSwipingLeft) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(horizontal = 24.dp)
                    .size(28.dp)
            )
        }

        // Foreground content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (kotlin.math.abs(offsetX.value) > threshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSwipeConfirmed() // Delegate confirmation to parent
                            } else {
                                scope.launch {
                                    offsetX.animateTo(0f, tween(300))
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSubscriptionCard(
    subscription: Subscription,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {


    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Service icon/avatar
            Surface(
                color = subscription.category.color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CategoryIndicator(
                        color = subscription.category.color,
                        size = 16
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = subscription.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Next billing: ${subscription.nextBillingDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$${String.format("%.2f", subscription.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = subscription.billingInterval.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

        }

    }
}


@Preview
@Composable
fun PreviewSubscriptionListContent(){

    val mockSubscriptions = persistentListOf(
        Subscription(
            name = "Netflix",
            price = 15.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            nextBillingDate = LocalDate.of(2025, 7, 1),
            iconUrl = "https://example.com/netflix.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Streaming service"
        ),
        Subscription(
            name = "Spotify",
            price = 9.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2023, 11, 5),
            nextBillingDate = LocalDate.of(2025, 6, 20),
            iconUrl = "https://example.com/spotify.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Music streaming"
        ),
        Subscription(
            name = "Amazon Prime",
            price = 139.00,
            currency = "USD",
            billingInterval = BillingInterval.YEARLY,
            startDate = LocalDate.of(2023, 8, 10),
            nextBillingDate = LocalDate.of(2025, 8, 10),
            iconUrl = "https://example.com/amazon.png",
            category = SubscriptionCategory.SHOPPING,
            description = "Prime delivery and media"
        ),
        Subscription(
            name = "Notion",
            price = 4.00,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 5, 1),
            nextBillingDate = LocalDate.of(2025, 6, 1),
            iconUrl = "https://example.com/notion.png",
            category = SubscriptionCategory.PRODUCTIVITY,
            description = "Note-taking and task management"
        ),
        Subscription(
            name = "Adobe Creative Cloud",
            price = 52.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2023, 10, 12),
            nextBillingDate = LocalDate.of(2025, 6, 12),
            iconUrl = "https://example.com/adobe.png",
            category = SubscriptionCategory.PRODUCTIVITY,
            description = "Creative software suite"
        ),
        Subscription(
            name = "YouTube Premium",
            price = 11.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 2, 15),
            nextBillingDate = LocalDate.of(2025, 6, 15),
            iconUrl = "https://example.com/youtube.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Ad-free videos and music"
        ),
        Subscription(
            name = "GitHub Copilot",
            price = 10.00,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 6, 1),
            nextBillingDate = LocalDate.of(2025, 6, 30),
            iconUrl = "https://example.com/copilot.png",
            category = SubscriptionCategory.EDUCATION,
            description = "AI coding assistant"
        )
    )
    FlowPayTheme {
        SubscriptionListContent(
            subscriptions = mockSubscriptions,
            totalMonthlySpend = 99.9,
            onDeleteSubscription = {},
            onNavigateToAddSubscription = {},
            onNavigateToEditSubscription = {},
            onApplyFilters = {},
            onResetFilters = {}
        )
    }
    }
