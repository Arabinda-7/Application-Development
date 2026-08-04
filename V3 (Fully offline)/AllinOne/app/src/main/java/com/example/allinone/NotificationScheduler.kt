package com.example.allinone

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object NotificationScheduler {

    const val MORNING_REMINDER_ID = 1001
    const val NIGHT_REMINDER_ID = 1002

    fun scheduleMorningReminder(context: Context, timeStr: String) {
        scheduleDailyReminder(context, timeStr, MORNING_REMINDER_ID, "MORNING")
    }

    fun scheduleNightReminder(context: Context, timeStr: String) {
        scheduleDailyReminder(context, timeStr, NIGHT_REMINDER_ID, "NIGHT")
    }

    private fun scheduleDailyReminder(context: Context, timeStr: String, requestId: Int, type: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            putExtra("REMINDER_TYPE", type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = timeStr.split(":")
        if (timeParts.size != 2) return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
            set(Calendar.MINUTE, timeParts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, requestId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
