package com.tamersarioglu.flowpay.data.database.paymenthistory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PaymentHistoryDao {
    @Insert
    suspend fun insertPayment(payment: PaymentHistory)

    @Query("SELECT * FROM payment_history WHERE subscriptionId = :subscriptionId ORDER BY paymentDate DESC")
    suspend fun getPaymentHistory(subscriptionId: String): List<PaymentHistory>

    @Query("SELECT strftime('%Y-%m', paymentDate) as month, SUM(amount) as total FROM payment_history GROUP BY month ORDER BY month DESC LIMIT 12")
    suspend fun getMonthlySpendingHistory(): List<MonthlySpendingResult>
}

data class MonthlySpendingResult(
    val month: String,
    val total: Double
)