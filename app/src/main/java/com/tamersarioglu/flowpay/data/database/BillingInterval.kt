package com.tamersarioglu.flowpay.data.database

enum class BillingInterval(val displayName: String, val days: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365),
    CUSTOM("Custom", 0)
}