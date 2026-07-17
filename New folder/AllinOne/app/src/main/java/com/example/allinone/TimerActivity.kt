package com.example.allinone

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator

class TimerActivity : AppCompatActivity() {

    private lateinit var countDownTimer: CountDownTimer
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        val workoutName = intent.getStringExtra("WORKOUT_NAME")
        val timerDuration = intent.getIntExtra("TIMER_DURATION", 0)
        timeLeftInMillis = timerDuration * 1000L

        val workoutNameTextView = findViewById<TextView>(R.id.workout_name_textview)
        val timerTextView = findViewById<TextView>(R.id.timer_textview)
        val timerProgressIndicator = findViewById<CircularProgressIndicator>(R.id.timer_progress_indicator)
        val startPauseButton = findViewById<Button>(R.id.start_pause_button)
        val finishButton = findViewById<Button>(R.id.finish_button)

        workoutNameTextView.text = workoutName
        timerProgressIndicator.max = timerDuration

        findViewById<android.view.View>(R.id.btn_back).setOnClickListener { finish() }

        updateTimer(timerTextView, timerProgressIndicator)

        startPauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer(timerTextView, timerProgressIndicator)
            }
        }

        finishButton.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun startTimer(timerTextView: TextView, timerProgressIndicator: CircularProgressIndicator) {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimer(timerTextView, timerProgressIndicator)
            }

            override fun onFinish() {
                setResult(RESULT_OK)
                finish()
            }
        }.start()

        isTimerRunning = true
        findViewById<Button>(R.id.start_pause_button).text = "Pause"
    }

    private fun pauseTimer() {
        countDownTimer.cancel()
        isTimerRunning = false
        findViewById<Button>(R.id.start_pause_button).text = "Start"
    }

    private fun updateTimer(timerTextView: TextView, timerProgressIndicator: CircularProgressIndicator) {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = timeFormatted
        timerProgressIndicator.progress = (timeLeftInMillis / 1000).toInt()
    }
}