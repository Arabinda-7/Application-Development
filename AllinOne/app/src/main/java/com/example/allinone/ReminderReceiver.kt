package com.example.allinone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ReminderReceiver : BroadcastReceiver() {
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val taskName = intent.getStringExtra("TASK_NAME") ?: run { pendingResult.finish(); return }
        val taskTimestamp = intent.getLongExtra("TASK_TIMESTAMP", -1L)
        val isWorkspace = intent.getBooleanExtra("IS_WORKSPACE", false)
        
        receiverScope.launch {
            try {
                val entryPoint = DataManager.getEntryPoint(context)
                val userSettings = entryPoint.userRepository().getUserSettings().first()
                
                if (isWorkspace) {
                    if (userSettings.isWorkspaceNotificationEnabled) {
                        triggerNotification(context, taskName)
                        triggerVibration(context)
                    }
                    return@launch
                }

                if (taskName.startsWith("Note:")) {
                    if (userSettings.isNoteNotificationEnabled) {
                        triggerNotification(context, taskName)
                        triggerVibration(context)
                    }
                    return@launch
                }
                
                if (taskName.startsWith("Milestone:")) {
                    if (userSettings.isProjectNotificationEnabled) {
                        triggerNotification(context, taskName)
                        triggerVibration(context)
                    }
                    return@launch
                }

                // Query for task status
                val tasks = entryPoint.taskRepository().getTasks().first()
                val task = tasks.find { it.timestamp == taskTimestamp }
                
                // Only notify if task exists and is NOT completed
                if (task != null && !task.isCompleted) {
                    if (userSettings.isTaskNotificationEnabled) {
                        triggerNotification(context, taskName)
                        triggerVibration(context)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun triggerNotification(context: Context, taskName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_reminders"

        val channel = NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Reminders for pending tasks"
            enableVibration(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)

        val mainIntent = Intent(context, TaskActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: 
                      RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle("To-Do Reminder")
            .setContentText("Pending Task: $taskName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(taskName.hashCode(), notification)
    }

    private fun triggerVibration(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 400, 200, 400)
        val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)
        
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        }
    }

    companion object {
        fun showSummaryNotification(context: Context, count: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "task_reminders"

            val channel = NotificationChannel(channelId, "Task Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Reminders for pending tasks"
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)

            val mainIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_task)
                .setContentTitle("Daily Agenda")
                .setContentText("You have $count deadlines to address today.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(999, notification)
        }
    }
}
