package com.tamersarioglu.flowpay.data.database.subcription

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tamersarioglu.flowpay.data.database.BillingInterval
import com.tamersarioglu.flowpay.data.database.Converters
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "subscriptions")
@TypeConverters(Converters::class)
data class Subscription(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val currency: String = "USD",
    val billingInterval: BillingInterval,
    val customIntervalDays: Int = 0,
    val startDate: LocalDate,
    val nextBillingDate: LocalDate,
    val isActive: Boolean = true,
    val iconUrl: String? = null,
    val category: SubscriptionCategory,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)