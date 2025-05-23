package com.tamersarioglu.flowpay.data.database.subcription

import androidx.compose.ui.graphics.Color

enum class SubscriptionCategory(val displayName: String, val colorValue: Long) {
    ENTERTAINMENT("Entertainment", 0xFF9C27B0),
    PRODUCTIVITY("Productivity", 0xFF2196F3),
    HEALTH("Health & Fitness", 0xFF4CAF50),
    EDUCATION("Education", 0xFFFF9800),
    UTILITIES("Utilities", 0xFFF44336),
    SHOPPING("Shopping", 0xFFE91E63),
    OTHER("Other", 0xFF607D8B);

    val color: Color get() = Color(colorValue)
}