package com.example.allinone

import android.content.Intent
import android.view.ViewGroup
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.app.AlertDialog

class LockActivity : BaseActivity() {

    companion object {
        const val MODE_AUTH = 1
        const val MODE_SETUP = 2
        const val MODE_CHANGE = 3
        const val MODE_RECOVERY = 4
        const val MODE_SETUP_RECOVERY = 5
        const val MODE_VERIFY_FOR_RECOVERY = 6
        const val EXTRA_MODE = "lock_mode"
    }

    private var currentMode = MODE_AUTH
    private var enteredPin = ""
    private var firstAttemptPin = ""
    private var isConfirming = false

    private lateinit var tvTitle: TextView
    private lateinit var tvSub: TextView
    private lateinit var dotViews: List<View>
    private lateinit var btnForgotPin: TextView
    private lateinit var layoutRecovery: View
    private lateinit var recoveryDimOverlay: View
    private lateinit var etRecoveryAnswer: EditText

    private lateinit var layoutSetupRecovery: View
    private lateinit var tvSetupSelectedQuestion: TextView
    private lateinit var etSetupRecoveryAnswer: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        setupKeyboardHandling(findViewById(R.id.lock_root_layout), findViewById(R.id.lock_content_container))

        currentMode = intent.getIntExtra(EXTRA_MODE, MODE_AUTH)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (currentMode == MODE_AUTH) {
                    finish() // Exit app on back press during auth
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        tvTitle = findViewById(R.id.tv_lock_title)
        tvSub = findViewById(R.id.tv_lock_sub)
        dotViews = listOf(
            findViewById(R.id.dot_1),
            findViewById(R.id.dot_2),
            findViewById(R.id.dot_3),
            findViewById(R.id.dot_4)
        )
        btnForgotPin = findViewById(R.id.btn_forgot_pin)
        layoutRecovery = findViewById(R.id.layout_recovery)
        recoveryDimOverlay = findViewById(R.id.recovery_dim_overlay)
        etRecoveryAnswer = findViewById(R.id.et_recovery_answer)

        layoutSetupRecovery = findViewById(R.id.layout_setup_recovery)
        tvSetupSelectedQuestion = findViewById(R.id.tv_setup_selected_question)
        etSetupRecoveryAnswer = findViewById(R.id.et_setup_recovery_answer)

        setupRecovery()
        setupHeader()
        setupKeypad()

        if (currentMode == MODE_SETUP_RECOVERY) {
            showRecoverySetupUI()
        }
    }

    private fun setupRecovery() {
        btnForgotPin.setOnClickListener {
            if (DataManager.appLockQuestion != null) {
                showRecoveryUI()
            } else {
                Toast.makeText(this, "Recovery not set. Please contact support.", Toast.LENGTH_LONG).show()
            }
        }

        findViewById<View>(R.id.btn_verify_recovery).setOnClickListener {
            val answer = etRecoveryAnswer.text.toString().trim()
            if (answer.equals(DataManager.appLockAnswer, ignoreCase = true)) {
                Toast.makeText(this, "Identity Verified. Reset your PIN.", Toast.LENGTH_SHORT).show()
                currentMode = MODE_SETUP
                isConfirming = false
                enteredPin = ""
                updateDots()
                setupHeader()
                hideRecoveryUI()
            } else {
                Toast.makeText(this, "Incorrect Answer", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btn_back_recovery).setOnClickListener {
            hideRecoveryUI()
        }

        recoveryDimOverlay.setOnClickListener {
            hideRecoveryUI()
        }
    }

    private fun showRecoveryUI() {
        layoutRecovery.visibility = View.VISIBLE
        recoveryDimOverlay.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tv_security_question).text = DataManager.appLockQuestion
        btnForgotPin.visibility = View.GONE
    }

    private fun hideRecoveryUI() {
        layoutRecovery.visibility = View.GONE
        recoveryDimOverlay.visibility = View.GONE
        if (currentMode == MODE_AUTH) btnForgotPin.visibility = View.VISIBLE
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
            MODE_VERIFY_FOR_RECOVERY -> {
                tvTitle.text = "VERIFY PIN"
                tvSub.text = "Enter PIN to modify recovery"
            }
            else -> {
                tvTitle.text = "ENTER PIN"
                tvSub.text = "Please enter your security code"
                btnForgotPin.visibility = if (DataManager.appLockQuestion != null) View.VISIBLE else View.GONE
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
                    overridePendingTransition(0, 0)
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
                        
                        // If recovery is already set, just finish and return to Settings
                        if (DataManager.appLockQuestion != null) {
                            Toast.makeText(this, "PIN Updated Successfully", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            showRecoverySetupUI()
                        }
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
            MODE_VERIFY_FOR_RECOVERY -> {
                if (enteredPin == DataManager.appLockPin) {
                    enteredPin = ""
                    updateDots()
                    showRecoverySetupUI()
                } else {
                    showError("Incorrect PIN")
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

    private fun showRecoverySetupUI() {
        layoutSetupRecovery.visibility = View.VISIBLE
        findViewById<View>(R.id.lock_content_container).visibility = View.GONE
        
        val questions = arrayOf(
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What city were you born in?",
            "What was the name of your first school?",
            "What is your favorite book?"
        )
        
        var selectedQuestion: String? = null

        tvSetupSelectedQuestion.setOnClickListener {
            showQuestionSelectionDialog(questions) { question ->
                selectedQuestion = question
                tvSetupSelectedQuestion.text = question
                tvSetupSelectedQuestion.setTextColor(Color.WHITE)
            }
        }

        findViewById<View>(R.id.btn_back_setup).setOnClickListener {
            if (currentMode == MODE_SETUP_RECOVERY) {
                finish()
            } else {
                layoutSetupRecovery.visibility = View.GONE
                findViewById<View>(R.id.lock_content_container).visibility = View.VISIBLE
            }
        }
        
        findViewById<View>(R.id.btn_save_setup).setOnClickListener {
            val answer = etSetupRecoveryAnswer.text.toString().trim()
            if (selectedQuestion != null && answer.isNotEmpty()) {
                DataManager.appLockQuestion = selectedQuestion
                DataManager.appLockAnswer = answer
                DataManager.saveData(this)
                Toast.makeText(this, "Security Recovery Set", Toast.LENGTH_SHORT).show()
                finish()
            } else if (selectedQuestion == null) {
                Toast.makeText(this, "Please select a question", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showQuestionSelectionDialog(questions: Array<String>, onSelected: (String) -> Unit) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_settings_selection)
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.attributes.blurBehindRadius = 20
            }
        }

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val container = dialog.findViewById<ViewGroup>(R.id.options_container)
        val btnCancel = dialog.findViewById<View>(R.id.btn_cancel)

        title.text = "SELECT SECURITY QUESTION"
        container.removeAllViews()

        questions.forEach { question ->
            val itemView = layoutInflater.inflate(R.layout.item_settings_selection, container, false) as TextView
            itemView.text = question
            
            if (DataManager.appLockQuestion == question) {
                itemView.setBackgroundResource(R.drawable.item_selection_highlight)
                itemView.setTypeface(null, android.graphics.Typeface.BOLD)
                itemView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icons8_checkmark_100, 0)
                itemView.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                
                val drawables = itemView.compoundDrawables
                drawables[2]?.let { 
                    val size = (18 * resources.displayMetrics.density).toInt()
                    it.setBounds(0, 0, size, size)
                    itemView.setCompoundDrawables(null, null, it, null)
                }
            }

            itemView.setOnClickListener {
                onSelected(question)
                dialog.dismiss()
            }
            container.addView(itemView)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
