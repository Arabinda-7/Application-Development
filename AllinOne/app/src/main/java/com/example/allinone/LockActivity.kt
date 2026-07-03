package com.example.allinone

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LockActivity : AppCompatActivity() {

    companion object {
        const val MODE_AUTH = 1
        const val MODE_SETUP = 2
        const val MODE_CHANGE = 3
        const val EXTRA_MODE = "lock_mode"
    }

    private var currentMode = MODE_AUTH
    private var enteredPin = ""
    private var firstAttemptPin = ""
    private var isConfirming = false

    private lateinit var tvTitle: TextView
    private lateinit var tvSub: TextView
    private lateinit var dotViews: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        currentMode = intent.getIntExtra(EXTRA_MODE, MODE_AUTH)

        tvTitle = findViewById(R.id.tv_lock_title)
        tvSub = findViewById(R.id.tv_lock_sub)
        dotViews = listOf(
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2),
            findViewById(R.id.dot_3),
            findViewById(R.id.dot_4)
        )

        setupHeader()
        setupKeypad()
    }

    private fun setupHeader() {
        when (currentMode) {
            MODE_SETUP -> {
                tvTitle.text = "CREATE PIN"
                tvSub.text = "Set a 4-digit security code"
            }
            MODE_CHANGE -> {
                tvTitle.text = "CHANGE PIN"
                tvSub.text = "Enter your current PIN"
            }
            else -> {
                tvTitle.text = "ENTER PIN"
                tvSub.text = "Please enter your security code"
            }
        }
    }

    private fun setupKeypad() {
        val keys = listOf(
            R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        )

        keys.forEach { id ->
            findViewById<TextView>(id).setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin += (it as TextView).text
                    updateDots()
                    if (enteredPin.length == 4) {
                        handlePinComplete()
                    }
                }
            }
        }

        findViewById<View>(R.id.btn_backspace).setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updateDots()
            }
        }

        findViewById<View>(R.id.btn_done).setOnClickListener {
            if (enteredPin.length == 4) {
                handlePinComplete()
            } else {
                Toast.makeText(this, "Enter 4 digits", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDots() {
        val activeColor = Color.parseColor("#1A73E8")
        val inactiveColor = Color.parseColor("#33FFFFFF")

        dotViews.forEachIndexed { index, view ->
            val isActive = index < enteredPin.length
            view.backgroundTintList = ColorStateList.valueOf(if (isActive) activeColor else inactiveColor)
        }
    }

    private fun handlePinComplete() {
        when (currentMode) {
            MODE_AUTH -> {
                if (enteredPin == DataManager.appLockPin) {
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showError("Incorrect PIN")
                }
            }
            MODE_SETUP -> {
                if (!isConfirming) {
                    firstAttemptPin = enteredPin
                    enteredPin = ""
                    isConfirming = true
                    updateDots()
                    tvTitle.text = "CONFIRM PIN"
                    tvSub.text = "Enter your PIN again to verify"
                } else {
                    if (enteredPin == firstAttemptPin) {
                        DataManager.appLockPin = enteredPin
                        DataManager.isAppLockEnabled = true
                        DataManager.saveData(this)
                        Toast.makeText(this, "PIN Set Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        showError("PINs do not match. Start over.")
                        resetSetup()
                    }
                }
            }
            MODE_CHANGE -> {
                if (enteredPin == DataManager.appLockPin) {
                    currentMode = MODE_SETUP
                    enteredPin = ""
                    updateDots()
                    setupHeader()
                } else {
                    showError("Incorrect current PIN")
                }
            }
        }
    }

    private fun resetSetup() {
        enteredPin = ""
        firstAttemptPin = ""
        isConfirming = false
        updateDots()
        setupHeader()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        enteredPin = ""
        updateDots()
    }
}
