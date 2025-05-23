package com.tamersarioglu.flowpay.di

import android.content.Context
import androidx.room.Room
import com.tamersarioglu.flowpay.data.database.paymenthistory.PaymentHistoryDao
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionDao
import com.tamersarioglu.flowpay.data.database.subcription.SubscriptionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SubscriptionDatabase {
        return Room.databaseBuilder(
            context,
            SubscriptionDatabase::class.java,
            "subscription_database"
        ).build()
    }

    @Provides
    fun provideSubscriptionDao(database: SubscriptionDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }

    @Provides
    fun providePaymentHistoryDao(database: SubscriptionDatabase): PaymentHistoryDao {
        return database.paymentHistoryDao()
    }
}