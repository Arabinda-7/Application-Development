package com.example.allinone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("TASK_NAME") ?: return
        val taskTimestamp = intent.getLongExtra("TASK_TIMESTAMP", -1L)
        
        if (taskName.startsWith("Note:")) {
            triggerNotification(context, taskName)
            triggerVibration(context)
            return
        }

        // Reload data to check completion status
        DataManager.loadData(context)
        val task = DataManager.tasks.find { it.timestamp == taskTimestamp }
        
        // Only notify if task exists and is NOT completed
        if (task != null && !task.isCompleted) {
            triggerNotification(context, taskName)
            triggerVibration(context)
        }
    }

    private fun triggerNotification(context: Context, taskName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for pending tasks"
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, ToDoListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: 
                      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_todo_list)
            .setContentTitle("To-Do Reminder")
            .setContentText("Pending Task: $taskName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Makes it pop up more aggressively
            .build()

        notificationManager.notify(taskName.hashCode(), notification)
    }

    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }
    }
}
