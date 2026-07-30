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
import kotlinx.coroutines.*
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
                    var count = 0
                    while (!DataManager.isDataLoaded.value && count < 30) {
                        delay(100)
                        count++
                    }
                }

                if (type == "MORNING") {
                    handleMorningReminder(context)
                } else {
                    handleNightReminder(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleMorningReminder(context: Context) {
        val quote = NotificationQuoteProvider.getRandomMorningQuote()
        val sectionsWithTasks = mutableSetOf<String>()
        
        synchronized(DataManager.tasks) {
            DataManager.tasks.filter { !it.isCompleted }.forEach { 
                sectionsWithTasks.add(it.section)
            }
        }

        val bigText = if (sectionsWithTasks.isNotEmpty()) {
            "Start your day with these focus areas: ${sectionsWithTasks.joinToString(", ")}"
        } else {
            "No pending tasks for today. Have a relaxed day!"
        }

        showNotification(context, "Morning Motivation", quote, bigText, 101)
        
        // Reschedule for next day
        if (DataManager.isMorningReminderEnabled) {
            NotificationScheduler.scheduleMorningReminder(context, DataManager.morningReminderTime)
        }
    }

    private fun handleNightReminder(context: Context) {
        val allTasks = synchronized(DataManager.tasks) { DataManager.tasks.toList() }
        val unfinishedTasks = allTasks.filter { !it.isCompleted }
        val completionRate = if (allTasks.isEmpty()) 100 else ((allTasks.size - unfinishedTasks.size) * 100) / allTasks.size
        
        val quote = NotificationQuoteProvider.getClosingQuote(completionRate)
        
        val bigText = if (unfinishedTasks.isNotEmpty()) {
            "Unfinished tasks to keep in mind: ${unfinishedTasks.joinToString(", ") { it.name }}"
        } else {
            "All tasks completed! You've had a perfect day."
        }

        showNotification(context, "Day End Review", quote, bigText, 102)
        
        // Reschedule for next day
        if (DataManager.isNightReminderEnabled) {
            NotificationScheduler.scheduleNightReminder(context, DataManager.nightReminderTime)
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
