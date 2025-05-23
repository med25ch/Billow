package com.tamersarioglu.flowpay.data.database.paymenthistory

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "payment_history")
data class PaymentHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subscriptionId: String,
    val amount: Double,
    val paymentDate: LocalDate,
    val currency: String = "USD"
)