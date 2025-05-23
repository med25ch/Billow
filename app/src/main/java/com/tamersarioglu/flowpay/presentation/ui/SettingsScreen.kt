package com.tamersarioglu.flowpay.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tamersarioglu.flowpay.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Notifications section
        SettingsSection(title = "Notifications") {
            SettingsItem(
                title = "Payment Reminders",
                subtitle = "Get notified about upcoming payments",
                trailing = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::toggleNotifications
                    )
                }
            )

            if (uiState.notificationsEnabled) {
                SettingsItem(
                    title = "Reminder Days",
                    subtitle = "How many days in advance to remind you",
                    trailing = {
                        ReminderDaysSelector(
                            selectedDays = uiState.reminderDays,
                            onDaysSelected = viewModel::updateReminderDays
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Appearance section
        SettingsSection(title = "Appearance") {
            SettingsItem(
                title = "Dark Mode",
                subtitle = "Use dark theme",
                trailing = {
                    Switch(
                        checked = uiState.darkModeEnabled,
                        onCheckedChange = viewModel::toggleDarkMode
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About section
        SettingsSection(title = "About") {
            SettingsItem(
                title = "Version",
                subtitle = "1.0.0",
                onClick = { }
            )

            SettingsItem(
                title = "Privacy Policy",
                subtitle = "Learn how we protect your data",
                onClick = { }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDaysSelector(
    selectedDays: Int,
    onDaysSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(1, 2, 3, 5, 7)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Text(
            text = "$selectedDays day${if (selectedDays != 1) "s" else ""}",
            modifier = Modifier
                .menuAnchor()
                .clickable { expanded = true }
                .padding(8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { days ->
                DropdownMenuItem(
                    text = { Text("$days day${if (days != 1) "s" else ""}") },
                    onClick = {
                        onDaysSelected(days)
                        expanded = false
                    }
                )
            }
        }
    }
}