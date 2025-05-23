package com.tamersarioglu.flowpay.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.presentation.viewmodel.AddEditSubscriptionViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditSubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Name field
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::updateName,
            label = { Text("Subscription Name") },
            placeholder = { Text("Netflix, Spotify, etc.") },
            isError = uiState.showValidationErrors && uiState.name.isBlank(),
            supportingText = if (uiState.showValidationErrors && uiState.name.isBlank()) {
                { Text("Name is required") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Price field
        OutlinedTextField(
            value = uiState.price,
            onValueChange = viewModel::updatePrice,
            label = { Text("Price") },
            placeholder = { Text("9.99") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            leadingIcon = { Text("$") },
            isError = uiState.showValidationErrors && uiState.price.toDoubleOrNull() == null,
            supportingText = if (uiState.showValidationErrors && uiState.price.toDoubleOrNull() == null) {
                { Text("Enter a valid price") }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Billing interval dropdown
        BillingIntervalDropdown(
            selectedInterval = uiState.billingInterval,
            onIntervalSelected = viewModel::updateBillingInterval,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Custom interval days (shown only when CUSTOM is selected)
        AnimatedVisibility(
            visible = uiState.billingInterval == BillingInterval.CUSTOM,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = uiState.customIntervalDays,
                onValueChange = viewModel::updateCustomIntervalDays,
                label = { Text("Billing Days") },
                placeholder = { Text("30") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                isError = uiState.showValidationErrors &&
                        uiState.billingInterval == BillingInterval.CUSTOM &&
                        uiState.customIntervalDays.toIntOrNull() == null,
                supportingText = if (uiState.showValidationErrors &&
                    uiState.billingInterval == BillingInterval.CUSTOM &&
                    uiState.customIntervalDays.toIntOrNull() == null) {
                    { Text("Enter number of days") }
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        // Start date picker
        StartDatePicker(
            selectedDate = uiState.startDate,
            onDateSelected = viewModel::updateStartDate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Category dropdown
        CategoryDropdown(
            selectedCategory = uiState.category,
            onCategorySelected = viewModel::updateCategory,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Description field (optional)
        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::updateDescription,
            label = { Text("Description (Optional)") },
            placeholder = { Text("Additional notes...") },
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        // Save button
        Button(
            onClick = viewModel::saveSubscription,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (uiState.isEditMode) "Update Subscription" else "Add Subscription")
        }
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Handle error (show snackbar, etc.)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingIntervalDropdown(
    selectedInterval: BillingInterval,
    onIntervalSelected: (BillingInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedInterval.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Billing Interval") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BillingInterval.values().forEach { interval ->
                DropdownMenuItem(
                    text = { Text(interval.displayName) },
                    onClick = {
                        onIntervalSelected(interval)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    selectedCategory: SubscriptionCategory,
    onCategorySelected: (SubscriptionCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Category") },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = selectedCategory.color,
                            shape = CircleShape
                        )
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SubscriptionCategory.values().forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = category.color,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(category.displayName)
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    OutlinedTextField(
        value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
        onValueChange = { },
        readOnly = true,
        label = { Text("Start Date") },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select date")
            }
        },
        modifier = modifier.clickable { showDatePicker = true }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let {
                    val localDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(localDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            datePickerState = datePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}