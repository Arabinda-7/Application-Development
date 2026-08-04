package com.example.allinone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.allinone.DataManager
import com.example.allinone.data.NotificationQuoteProvider
import com.example.allinone.NotificationScheduler
import com.example.allinone.domain.repository.UserSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*

class DailyReminderReceiver : BroadcastReceiver() {
    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("REMINDER_TYPE") ?: return
        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                if (!DataManager.isDataLoaded.value) {
                    DataManager.initialize(context.applicationContext)
                    withTimeoutOrNull(3000) {
                        DataManager.isDataLoaded.first { it }
                    }
                }

                val entryPoint = DataManager.getEntryPoint(context)
                val userSettings = entryPoint.userRepository().getUserSettings().first()

                if (type == "MORNING") {
                    handleMorningReminder(context, userSettings)
                } else {
                    handleNightReminder(context, userSettings)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMorningReminder(context: Context, settings: UserSettings) {
        val quote = NotificationQuoteProvider.getRandomMorningQuote()
        val sectionsWithTasks = mutableSetOf<String>()
        
        synchronized(DataManager.tasks) {
            DataManager.tasks.filter { !it.isCompleted }.forEach { 
                val shouldNotify = when (it.section) {
                    "Tasks" -> settings.isTaskNotificationEnabled
                    "Habits" -> settings.isHabitNotificationEnabled
                    "Workouts" -> settings.isWorkoutNotificationEnabled
                    "Notes" -> settings.isNoteNotificationEnabled
                    "Projects" -> settings.isProjectNotificationEnabled
                    "Finance" -> settings.isFinanceNotificationEnabled
                    else -> true
                }
                if (shouldNotify) {
                    sectionsWithTasks.add(it.section)
                }
            }
        }
        
        if (settings.isWorkspaceNotificationEnabled) {
            sectionsWithTasks.add("Workspaces")
        }

        val bigText = if (sectionsWithTasks.isNotEmpty()) {
            "Start your day with these focus areas: ${sectionsWithTasks.joinToString(", ")}"
        } else {
            "No pending tasks for today. Have a relaxed day!"
        }

        showNotification(context, "Morning Motivation", quote, bigText, 101)
        
        if (settings.isMorningReminderEnabled) {
            NotificationScheduler.scheduleMorningReminder(context, settings.morningReminderTime)
        }
    }

    private fun handleNightReminder(context: Context, settings: UserSettings) {
        val allTasks = synchronized(DataManager.tasks) { DataManager.tasks.toList() }
        val unfinishedTasks = allTasks.filter { !it.isCompleted }.filter { 
            when (it.section) {
                "Tasks" -> settings.isTaskNotificationEnabled
                "Habits" -> settings.isHabitNotificationEnabled
                "Workouts" -> settings.isWorkoutNotificationEnabled
                "Notes" -> settings.isNoteNotificationEnabled
                "Projects" -> settings.isProjectNotificationEnabled
                "Finance" -> settings.isFinanceNotificationEnabled
                else -> true
            }
        }
        val completionRate = if (allTasks.isEmpty()) 100 else ((allTasks.size - unfinishedTasks.size) * 100) / allTasks.size
        
        val quote = NotificationQuoteProvider.getClosingQuote(completionRate)
        
        val bigText = if (unfinishedTasks.isNotEmpty()) {
            "Unfinished tasks to keep in mind: ${unfinishedTasks.joinToString(", ") { it.name }}"
        } else {
            "All tasks completed! You've had a perfect day."
        }

        showNotification(context, "Day End Review", quote, bigText, 102)
        
        if (settings.isNightReminderEnabled) {
            NotificationScheduler.scheduleNightReminder(context, settings.nightReminderTime)
        }
    }

    private fun showNotification(context: Context, title: String, content: String, bigText: String, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "daily_reminders"

        val channel = NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Motivational reminders and daily summaries"
        }
        notificationManager.createNotificationChannel(channel)

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_task)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\n\n$bigText"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }
}
