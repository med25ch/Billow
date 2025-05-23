package com.tamersarioglu.flowpay.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.tamersarioglu.flowpay.R
import com.tamersarioglu.flowpay.domain.model.UpcomingPayment
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_REMINDERS,
                "Payment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming subscription payments"
            }
        )
        notificationManager.createNotificationChannels(channels)
    }

    fun showDueTodayNotification(payment: UpcomingPayment) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Payment Due Today")
            .setContentText("${payment.subscription.name} - ${String.format("%.2f", payment.amount)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(payment.subscription.id.hashCode(), notification)
    }

    fun showDueTomorrowNotification(payment: UpcomingPayment) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Payment Due Tomorrow")
            .setContentText("${payment.subscription.name} - ${String.format("%.2f", payment.amount)}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(payment.subscription.id.hashCode(), notification)
    }

    fun showUpcomingNotification(payment: UpcomingPayment) {
        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Upcoming Payment")
            .setContentText("${payment.subscription.name} in ${payment.daysUntilPayment} days - ${String.format("%.2f", payment.amount)}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(payment.subscription.id.hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_REMINDERS = "reminders"
    }
}