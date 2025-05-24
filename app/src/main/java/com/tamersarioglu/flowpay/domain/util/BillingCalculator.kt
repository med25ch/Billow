package com.tamersarioglu.flowpay.domain.util

import android.annotation.SuppressLint
import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import java.time.LocalDate

object BillingCalculator {
    
    /**
     * Calculates the monthly equivalent amount for a subscription
     * Uses precise calculations based on actual calendar periods
     */
    fun calculateMonthlyAmount(subscription: Subscription): Double {
        return when (subscription.billingInterval) {
            BillingInterval.WEEKLY -> {
                // Calculate based on the specific number of weeks in a year divided by 12 months
                // 52.1775 weeks per year (365.25 days / 7 days per week) / 12 months
                subscription.price * (52.1775 / 12.0)
            }
            BillingInterval.MONTHLY -> subscription.price
            BillingInterval.QUARTERLY -> subscription.price / 3.0
            BillingInterval.YEARLY -> subscription.price / 12.0
            BillingInterval.CUSTOM -> {
                if (subscription.customIntervalDays > 0) {
                    // Calculate how many custom periods fit in a year, then divide by 12 months
                    val periodsPerYear = 365.25 / subscription.customIntervalDays
                    subscription.price * (periodsPerYear / 12.0)
                } else {
                    0.0
                }
            }
        }
    }
    
    /**
     * Calculates the yearly equivalent amount for a subscription
     */
    fun calculateYearlyAmount(subscription: Subscription): Double {
        return when (subscription.billingInterval) {
            BillingInterval.WEEKLY -> subscription.price * 52.1775 // More precise weeks per year
            BillingInterval.MONTHLY -> subscription.price * 12.0
            BillingInterval.QUARTERLY -> subscription.price * 4.0
            BillingInterval.YEARLY -> subscription.price
            BillingInterval.CUSTOM -> {
                if (subscription.customIntervalDays > 0) {
                    subscription.price * (365.25 / subscription.customIntervalDays)
                } else {
                    0.0
                }
            }
        }
    }
    
    /**
     * Calculates the daily equivalent amount for a subscription
     */
    fun calculateDailyAmount(subscription: Subscription): Double {
        return calculateYearlyAmount(subscription) / 365.25
    }
    
    /**
     * Calculates the next billing date for a subscription based on its current billing date
     */
    fun calculateNextBillingDate(subscription: Subscription, currentDate: LocalDate = LocalDate.now()): LocalDate {
        return when (subscription.billingInterval) {
            BillingInterval.WEEKLY -> currentDate.plusWeeks(1)
            BillingInterval.MONTHLY -> currentDate.plusMonths(1)
            BillingInterval.QUARTERLY -> currentDate.plusMonths(3)
            BillingInterval.YEARLY -> currentDate.plusYears(1)
            BillingInterval.CUSTOM -> currentDate.plusDays(subscription.customIntervalDays.toLong())
        }
    }
    
    /**
     * Calculates the exact monthly amount for a specific month and year
     * This is more accurate as it accounts for the actual number of days in the target month
     */
    fun calculateMonthlyAmountForSpecificMonth(
        subscription: Subscription, 
        year: Int, 
        month: Int
    ): Double {
        val daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth()
        val dailyAmount = calculateDailyAmount(subscription)
        return dailyAmount * daysInMonth
    }
    
    /**
     * Debug function to print calculation breakdown for a subscription
     */
    @SuppressLint("DefaultLocale")
    fun getCalculationBreakdown(subscription: Subscription): String {
        val monthly = calculateMonthlyAmount(subscription)
        val yearly = calculateYearlyAmount(subscription)
        val daily = calculateDailyAmount(subscription)
        
        return buildString {
            appendLine("Subscription: ${subscription.name}")
            appendLine("Price: $${String.format("%.2f", subscription.price)}")
            appendLine("Interval: ${subscription.billingInterval.displayName}")
            if (subscription.billingInterval == BillingInterval.CUSTOM) {
                appendLine("Custom Days: ${subscription.customIntervalDays}")
            }
            appendLine("Monthly Equivalent: $${String.format("%.2f", monthly)}")
            appendLine("Yearly Equivalent: $${String.format("%.2f", yearly)}")
            appendLine("Daily Equivalent: $${String.format("%.4f", daily)}")
        }
    }
    
    /**
     * Test calculations with sample data
     */
    fun runCalculationTests(): String {
        return buildString {
            appendLine("=== Billing Calculator Tests ===")
            
            // Test weekly subscription: $10/week
            val weeklyTest = createTestSubscription("Netflix", 10.0, BillingInterval.WEEKLY)
            appendLine(getCalculationBreakdown(weeklyTest))
            appendLine("Expected monthly ~$43.48")
            appendLine()
            
            // Test monthly subscription: $15/month  
            val monthlyTest = createTestSubscription("Spotify", 15.0, BillingInterval.MONTHLY)
            appendLine(getCalculationBreakdown(monthlyTest))
            appendLine("Expected monthly: $15.00")
            appendLine()
            
            // Test yearly subscription: $120/year
            val yearlyTest = createTestSubscription("Prime", 120.0, BillingInterval.YEARLY)
            appendLine(getCalculationBreakdown(yearlyTest))
            appendLine("Expected monthly: $10.00")
            appendLine()
        }
    }
    
    private fun createTestSubscription(
        name: String, 
        price: Double, 
        interval: BillingInterval,
        customDays: Int = 0
    ): Subscription {
        return Subscription(
            name = name,
            price = price,
            billingInterval = interval,
            customIntervalDays = customDays,
            startDate = LocalDate.now(),
            nextBillingDate = LocalDate.now(),
            category = com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory.ENTERTAINMENT
        )
    }
} 