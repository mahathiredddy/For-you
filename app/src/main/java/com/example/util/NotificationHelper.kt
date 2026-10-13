package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val CHANNEL_DAILY_PLAN = "channel_daily_plan"
    const val CHANNEL_CHECK_IN = "channel_check_in"
    const val CHANNEL_MILESTONES = "channel_milestones"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val planChannel = NotificationChannel(
                CHANNEL_DAILY_PLAN,
                "Daily Execution Plan",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning focus prompts and top pending actions"
            }

            val checkInChannel = NotificationChannel(
                CHANNEL_CHECK_IN,
                "Daily Check-In Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Evening reflection, friction logs, and streak tracking"
            }

            val milestonesChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "Roadmap Milestones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Celebrations and adaptations when completing key milestones"
            }

            manager.createNotificationChannel(planChannel)
            manager.createNotificationChannel(checkInChannel)
            manager.createNotificationChannel(milestonesChannel)
        }
    }

    fun showReminderNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            // Handled when POST_NOTIFICATIONS is not granted
        }
    }
}
