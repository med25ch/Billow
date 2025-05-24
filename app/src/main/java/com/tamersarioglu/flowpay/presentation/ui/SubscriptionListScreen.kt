package com.tamersarioglu.flowpay.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.presentation.ui.components.CategoryIndicator
import com.tamersarioglu.flowpay.presentation.ui.components.EmptyStateCard
import com.tamersarioglu.flowpay.presentation.ui.components.LoadingCard
import com.tamersarioglu.flowpay.presentation.ui.components.MetricCard
import com.tamersarioglu.flowpay.presentation.viewmodel.SubscriptionListViewModel
import kotlinx.collections.immutable.PersistentList
import java.time.format.DateTimeFormatter

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
                onDeleteSubscription = viewModel::deleteSubscription
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

@Composable
private fun SubscriptionListContent(
    subscriptions: PersistentList<Subscription>,
    totalMonthlySpend: Double,
    onNavigateToAddSubscription: () -> Unit,
    onNavigateToEditSubscription: (String) -> Unit,
    onDeleteSubscription: (Subscription) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = subscriptions,
                    key = { it.id }
                ) { subscription ->
                    ModernSubscriptionCard(
                        subscription = subscription,
                        onEdit = { onNavigateToEditSubscription(subscription.id) },
                        onDelete = { onDeleteSubscription(subscription) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSubscriptionCard(
    subscription: Subscription,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onEdit,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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

            IconButton(
                onClick = { showDeleteDialog = true },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete subscription"
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                    "Are you sure you want to delete ${subscription.name}? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}