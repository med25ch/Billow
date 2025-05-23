package com.tamersarioglu.flowpay.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicReminders() {
        val reminderWork = PeriodicWorkRequestBuilder<ReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "subscription_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }

    fun cancelReminders() {
        workManager.cancelUniqueWork("subscription_reminders")
    }
}