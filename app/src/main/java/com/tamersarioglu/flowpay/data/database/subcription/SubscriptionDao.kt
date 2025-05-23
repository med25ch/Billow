package com.tamersarioglu.flowpay.data.database.subcription

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions ORDER BY updatedAt DESC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: String): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("SELECT * FROM subscriptions WHERE nextBillingDate BETWEEN :startDate AND :endDate")
    suspend fun getSubscriptionsDueBetween(startDate: LocalDate, endDate: LocalDate): List<Subscription>

    @Query("SELECT category, SUM(price) as totalAmount, COUNT(*) as count FROM subscriptions WHERE isActive = 1 GROUP BY category")
    suspend fun getCategorySpending(): List<CategorySpendingResult>
}

data class CategorySpendingResult(
    val category: String,
    val totalAmount: Double,
    val count: Int
)