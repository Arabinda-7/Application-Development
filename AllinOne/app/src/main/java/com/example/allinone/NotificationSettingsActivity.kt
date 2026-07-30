package com.example.allinone

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.example.allinone.DataManager
import java.util.*

class NotificationSettingsActivity : BaseActivity() {

    private lateinit var swMorning: SwitchCompat
    private lateinit var tvMorningTime: TextView
    private lateinit var btnMorningTime: Button

    private lateinit var swNight: SwitchCompat
    private lateinit var tvNightTime: TextView
    private lateinit var btnNightTime: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        initViews()
        setupLogic()
        updateUI()
    }

    private fun initViews() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        
        swMorning = findViewById(R.id.sw_morning_reminder)
        tvMorningTime = findViewById(R.id.tv_morning_time)
        btnMorningTime = findViewById(R.id.btn_set_morning_time)

        swNight = findViewById(R.id.sw_night_reminder)
        tvNightTime = findViewById(R.id.tv_night_time)
        btnNightTime = findViewById(R.id.btn_set_night_time)
    }

    private fun setupLogic() {
        swMorning.setOnCheckedChangeListener { _, isChecked ->
            DataManager.isMorningReminderEnabled = isChecked
            DataManager.saveData(this)
            if (isChecked) {
                NotificationScheduler.scheduleMorningReminder(this, DataManager.morningReminderTime)
            } else {
                NotificationScheduler.cancelReminder(this, NotificationScheduler.MORNING_REMINDER_ID)
            }
        }

        btnMorningTime.setOnClickListener {
            showTimePicker(DataManager.morningReminderTime) { newTime ->
                DataManager.morningReminderTime = newTime
                DataManager.saveData(this)
                tvMorningTime.text = "Scheduled at $newTime"
                if (DataManager.isMorningReminderEnabled) {
                    NotificationScheduler.scheduleMorningReminder(this, newTime)
                }
            }
        }

        swNight.setOnCheckedChangeListener { _, isChecked ->
            DataManager.isNightReminderEnabled = isChecked
            DataManager.saveData(this)
            if (isChecked) {
                NotificationScheduler.scheduleNightReminder(this, DataManager.nightReminderTime)
            } else {
                NotificationScheduler.cancelReminder(this, NotificationScheduler.NIGHT_REMINDER_ID)
            }
        }

        btnNightTime.setOnClickListener {
            showTimePicker(DataManager.nightReminderTime) { newTime ->
                DataManager.nightReminderTime = newTime
                DataManager.saveData(this)
                tvNightTime.text = "Scheduled at $newTime"
                if (DataManager.isNightReminderEnabled) {
                    NotificationScheduler.scheduleNightReminder(this, newTime)
                }
            }
        }
    }

    private fun updateUI() {
        swMorning.isChecked = DataManager.isMorningReminderEnabled
        tvMorningTime.text = "Scheduled at ${DataManager.morningReminderTime}"

        swNight.isChecked = DataManager.isNightReminderEnabled
        tvNightTime.text = "Scheduled at ${DataManager.nightReminderTime}"
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
