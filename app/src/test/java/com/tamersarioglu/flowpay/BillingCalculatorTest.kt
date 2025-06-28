package com.tamersarioglu.flowpay

import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.subcription.Subscription
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionCategory
import com.tamersarioglu.flowpay.domain.util.BillingCalculator
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class BillingCalculatorTest {

    private lateinit var subscription: Subscription

    @Before
    fun setUp() {
        // This runs before each test
        subscription = Subscription(
            name = "Netflix",
            price = 15.99,
            currency = "USD",
            billingInterval = BillingInterval.MONTHLY,
            startDate = LocalDate.of(2024, 1, 1),
            nextBillingDate = LocalDate.of(2025, 7, 1),
            iconUrl = "https://example.com/netflix.png",
            category = SubscriptionCategory.ENTERTAINMENT,
            description = "Streaming service"
        )
    }


    @Test
    fun `should calculate correct amount for weekly subscription`() {
        subscription = subscription.copy(
            billingInterval = BillingInterval.WEEKLY,
            nextBillingDate = BillingCalculator.calculateNextBillingDate(subscription, subscription.startDate))

        val monthlyAmount = BillingCalculator.calculateMonthlyAmount(subscription)
        val rounded = BigDecimal(monthlyAmount).setScale(2, RoundingMode.HALF_UP).toDouble()
        assertTrue(rounded == 69.53)
    }

    @Test
    fun `should calculate correct amount for monthly subscription`() {
        subscription = subscription.copy(billingInterval = BillingInterval.MONTHLY,
            nextBillingDate = BillingCalculator.calculateNextBillingDate(subscription, subscription.startDate))
        assertTrue(BillingCalculator.calculateMonthlyAmount(subscription) == subscription.price)
    }

    @Test
    fun `should calculate correct amount for quarterly subscription`() {
        subscription = subscription.copy(billingInterval = BillingInterval.QUARTERLY,
            nextBillingDate = BillingCalculator.calculateNextBillingDate(subscription, subscription.startDate))

        val quarterly = BillingCalculator.calculateMonthlyAmount(subscription)
        assertTrue(quarterly == 5.33)
    }

    @Test
    fun `should calculate correct amount for yearly subscription`() {
        subscription = subscription.copy(billingInterval = BillingInterval.YEARLY)
        subscription = subscription.copy(nextBillingDate = BillingCalculator.calculateNextBillingDate(subscription, subscription.startDate))

        val yearly = BillingCalculator.calculateMonthlyAmount(subscription)
        val rounded = BigDecimal(yearly).setScale(2, RoundingMode.HALF_UP).toDouble()
        assertTrue(rounded == 1.33)

    }
    
}