package com.tamersarioglu.flowpay.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Notifications section
        ModernSettingsSection(
            title = "Notifications",
            icon = Icons.Default.Notifications
        ) {
            ModernSettingsCard(
                title = "Payment Reminders",
                subtitle = "Get notified about upcoming payments",
                icon = Icons.Default.NotificationsActive,
                trailing = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::toggleNotifications
                    )
                }
            )

            if (uiState.notificationsEnabled) {
                ModernSettingsCard(
                    title = "Reminder Days",
                    subtitle = "How many days in advance to remind you",
                    icon = Icons.Default.Schedule,
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
        ModernSettingsSection(
            title = "Appearance",
            icon = Icons.Default.Palette
        ) {
            ModernSettingsCard(
                title = "Dark Mode",
                subtitle = "Use dark theme",
                icon = Icons.Default.DarkMode,
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
        ModernSettingsSection(
            title = "About",
            icon = Icons.Default.Info
        ) {
            ModernSettingsCard(
                title = "Version",
                subtitle = "1.0.0",
                icon = Icons.Default.AppRegistration,
                onClick = { }
            )

            ModernSettingsCard(
                title = "Privacy Policy",
                subtitle = "Learn how we protect your data",
                icon = Icons.Default.PrivacyTip,
                onClick = { }
            )
        }
    }
}

@Composable
fun ModernSettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ModernSettingsCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
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
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.menuAnchor()
        ) {
            Text(
                text = "$selectedDays day${if (selectedDays != 1) "s" else ""}",
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                fontWeight = FontWeight.Medium
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { days ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            "$days day${if (days != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    onClick = {
                        onDaysSelected(days)
                        expanded = false
                    }
                )
            }
        }
    }
}