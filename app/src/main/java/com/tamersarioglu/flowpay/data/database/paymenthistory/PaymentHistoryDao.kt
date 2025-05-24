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
    
    // Daily spending
    @Query("SELECT strftime('%Y-%m-%d', paymentDate) as month, SUM(amount) as total FROM payment_history GROUP BY month ORDER BY month DESC LIMIT 30")
    suspend fun getDailySpendingHistory(): List<MonthlySpendingResult>
    
    // Weekly spending
    @Query("SELECT strftime('%Y-W%W', paymentDate) as month, SUM(amount) as total FROM payment_history GROUP BY month ORDER BY month DESC LIMIT 12")
    suspend fun getWeeklySpendingHistory(): List<MonthlySpendingResult>
    
    // Quarterly spending
    @Query("SELECT strftime('%Y-Q', paymentDate) || CASE WHEN CAST(strftime('%m', paymentDate) AS INTEGER) <= 3 THEN '1' WHEN CAST(strftime('%m', paymentDate) AS INTEGER) <= 6 THEN '2' WHEN CAST(strftime('%m', paymentDate) AS INTEGER) <= 9 THEN '3' ELSE '4' END as month, SUM(amount) as total FROM payment_history GROUP BY month ORDER BY month DESC LIMIT 8")
    suspend fun getQuarterlySpendingHistory(): List<MonthlySpendingResult>
    
    // Yearly spending
    @Query("SELECT strftime('%Y', paymentDate) as month, SUM(amount) as total FROM payment_history GROUP BY month ORDER BY month DESC LIMIT 5")
    suspend fun getYearlySpendingHistory(): List<MonthlySpendingResult>

    // Clear all payment history
    @Query("DELETE FROM payment_history")
    suspend fun clearAllPaymentHistory()
}

data class MonthlySpendingResult(
    val month: String,
    val total: Double
)