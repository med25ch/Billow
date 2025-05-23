package com.tamersarioglu.flowpay.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tamersarioglu.flowpay.domain.repository.SubscriptionRepository
import com.tamersarioglu.flowpay.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: SubscriptionRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val upcomingPayments = repository.getUpcomingPayments(3) // Next 3 days

            upcomingPayments.forEach { payment ->
                when (payment.daysUntilPayment) {
                    0 -> notificationHelper.showDueTodayNotification(payment)
                    1 -> notificationHelper.showDueTomorrowNotification(payment)
                    else -> notificationHelper.showUpcomingNotification(payment)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}