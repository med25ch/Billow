package com.tamersarioglu.flowpay.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tamersarioglu.flowpay.presentation.ui.AddEditSubscriptionScreen
import com.tamersarioglu.flowpay.presentation.ui.AnalyticsScreen
import com.tamersarioglu.flowpay.presentation.ui.SettingsScreen
import com.tamersarioglu.flowpay.presentation.ui.SubscriptionListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "subscription_list",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("subscription_list") {
                SubscriptionListScreen(
                    onNavigateToAddSubscription = {
                        navController.navigate("add_subscription")
                    },
                    onNavigateToEditSubscription = { subscriptionId ->
                        navController.navigate("edit_subscription/$subscriptionId")
                    }
                )
            }

            composable("add_subscription") {
                AddEditSubscriptionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "edit_subscription/{subscriptionId}",
                arguments = listOf(navArgument("subscriptionId") { type = NavType.StringType })
            ) {
                AddEditSubscriptionScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable("analytics") {
                AnalyticsScreen()
            }

            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("Subscriptions") },
            selected = currentRoute == "subscription_list",
            onClick = {
                navController.navigate("subscription_list") {
                    popUpTo("subscription_list") { inclusive = true }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
            label = { Text("Analytics") },
            selected = currentRoute == "analytics",
            onClick = {
                navController.navigate("analytics") {
                    popUpTo("subscription_list")
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Settings") },
            selected = currentRoute == "settings",
            onClick = {
                navController.navigate("settings") {
                    popUpTo("subscription_list")
                }
            }
        )
    }
}