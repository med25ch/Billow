package com.tamersarioglu.flowpay.data.database.subcription

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tamersarioglu.flowpay.data.database.Converters
import com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistory
import com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistoryDao

@Database([Subscription::class, PaymentHistory::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SubscriptionDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun paymentHistoryDao(): PaymentHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: SubscriptionDatabase? = null

        fun getDatabase(context: Context): SubscriptionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubscriptionDatabase::class.java,
                    "subscription_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}