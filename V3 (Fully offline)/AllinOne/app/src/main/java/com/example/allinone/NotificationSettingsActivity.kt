package com.example.allinone

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.allinone.DataManager
import java.util.*

class NotificationSettingsActivity : BaseActivity() {

    private lateinit var settingsList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        settingsList = findViewById(R.id.settings_list)
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        settingsList.layoutManager = LinearLayoutManager(this)
        setupKeyboardHandling(findViewById(R.id.section_settings_root), findViewById(R.id.section_settings_content_container))
        loadSettings()
    }

    private fun loadSettings() {
        val settings = mutableListOf<ConfigItem>()
        
        settings.add(ConfigItem("Daily Summaries", isHeader = true))
        
        settings.add(ConfigItem("Morning Motivation", "Enable daily motivation alerts", isToggle = true, isChecked = DataManager.isMorningReminderEnabled) {
            DataManager.isMorningReminderEnabled = !DataManager.isMorningReminderEnabled
            if (DataManager.isMorningReminderEnabled) {
                NotificationScheduler.scheduleMorningReminder(this, DataManager.morningReminderTime)
            } else {
                NotificationScheduler.cancelReminder(this, NotificationScheduler.MORNING_REMINDER_ID)
            }
        })
        
        settings.add(ConfigItem("Morning Time", "Scheduled at ${DataManager.morningReminderTime}") {
            showTimePicker(DataManager.morningReminderTime) { newTime ->
                DataManager.morningReminderTime = newTime
                if (DataManager.isMorningReminderEnabled) {
                    NotificationScheduler.scheduleMorningReminder(this, newTime)
                }
                loadSettings()
            }
        })

        settings.add(ConfigItem("Day End Review", "Enable night summary alerts", isToggle = true, isChecked = DataManager.isNightReminderEnabled) {
            DataManager.isNightReminderEnabled = !DataManager.isNightReminderEnabled
            if (DataManager.isNightReminderEnabled) {
                NotificationScheduler.scheduleNightReminder(this, DataManager.nightReminderTime)
            } else {
                NotificationScheduler.cancelReminder(this, NotificationScheduler.NIGHT_REMINDER_ID)
            }
        })

        settings.add(ConfigItem("Night Time", "Scheduled at ${DataManager.nightReminderTime}") {
            showTimePicker(DataManager.nightReminderTime) { newTime ->
                DataManager.nightReminderTime = newTime
                if (DataManager.isNightReminderEnabled) {
                    NotificationScheduler.scheduleNightReminder(this, newTime)
                }
                loadSettings()
            }
        })

        settings.add(ConfigItem("Section Notifications", isHeader = true))
        
        settings.add(ConfigItem("Tasks", "Receive task reminders", isToggle = true, isChecked = DataManager.isTaskNotificationEnabled) {
            DataManager.isTaskNotificationEnabled = !DataManager.isTaskNotificationEnabled
        })
        
        settings.add(ConfigItem("Habits", "Receive habit alerts", isToggle = true, isChecked = DataManager.isHabitNotificationEnabled) {
            DataManager.isHabitNotificationEnabled = !DataManager.isHabitNotificationEnabled
        })
        
        settings.add(ConfigItem("Workouts", "Receive workout alerts", isToggle = true, isChecked = DataManager.isWorkoutNotificationEnabled) {
            DataManager.isWorkoutNotificationEnabled = !DataManager.isWorkoutNotificationEnabled
        })
        
        settings.add(ConfigItem("Notes", "Receive note reminders", isToggle = true, isChecked = DataManager.isNoteNotificationEnabled) {
            DataManager.isNoteNotificationEnabled = !DataManager.isNoteNotificationEnabled
        })
        
        settings.add(ConfigItem("Projects", "Receive project alerts", isToggle = true, isChecked = DataManager.isProjectNotificationEnabled) {
            DataManager.isProjectNotificationEnabled = !DataManager.isProjectNotificationEnabled
        })

        settings.add(ConfigItem("Workspace", "Receive workspace alerts", isToggle = true, isChecked = DataManager.isWorkspaceNotificationEnabled) {
            DataManager.isWorkspaceNotificationEnabled = !DataManager.isWorkspaceNotificationEnabled
        })
        
        settings.add(ConfigItem("Finance", "Receive finance alerts", isToggle = true, isChecked = DataManager.isFinanceNotificationEnabled) {
            DataManager.isFinanceNotificationEnabled = !DataManager.isFinanceNotificationEnabled
        })

        settingsList.adapter = ConfigAdapter(settings) { DataManager.saveData(this) }
    }

    private fun showTimePicker(currentTime: String, onTimeSelected: (String) -> Unit) {
        val parts = currentTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        }, hour, minute, true).show()
    }
}
