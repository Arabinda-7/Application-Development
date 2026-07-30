package com.example.allinone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.allinone.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    // Initialize DataManager to get settings
                    DataManager.initialize(context.applicationContext)
                    var count = 0
                    while (!DataManager.isDataLoaded.value && count < 30) {
                        delay(100)
                        count++
                    }

                    if (DataManager.isMorningReminderEnabled) {
                        NotificationScheduler.scheduleMorningReminder(context, DataManager.morningReminderTime)
                    }
                    if (DataManager.isNightReminderEnabled) {
                        NotificationScheduler.scheduleNightReminder(context, DataManager.nightReminderTime)
                    }
                    
                    // Also reschedule task reminders if any
                    synchronized(DataManager.tasks) {
                        DataManager.tasks.filter { !it.isCompleted && it.reminderTime != null }.forEach { task ->
                            if (task.reminderTime!! > System.currentTimeMillis()) {
                                // Logic to reschedule task reminder (assuming existing ReminderReceiver handles this)
                                // We might need a common scheduler for tasks too, but for now focusing on daily reminders
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
