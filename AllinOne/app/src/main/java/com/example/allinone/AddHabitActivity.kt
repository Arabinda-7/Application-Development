package com.example.allinone

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class AddHabitActivity : BaseActivity() {

    private var habitIndex: Int = -1
    private var existingHabit: Habit? = null
    
    private var tempRepeatDays = mutableListOf(0, 1, 2, 3, 4, 5, 6)
    private var selectedFrequency = "Anytime"
    private var selectedColor: Int = -1
    private var selectedIcon: Int = R.drawable.ic_habit_tracker

    private lateinit var nameInput: EditText
    private lateinit var btnSave: TextView
    private lateinit var iconPreview: ImageView
    private lateinit var colorPreview: View
    private lateinit var headerAccent: View
    private lateinit var tvNameHint: TextView
    private lateinit var tvScheduleHint: TextView
    
    private val colors by lazy {
        listOf(
            ContextCompat.getColor(this, R.color.card_blue),
            ContextCompat.getColor(this, R.color.card_orange),
            ContextCompat.getColor(this, R.color.card_green),
            Color.MAGENTA, Color.RED, Color.CYAN, Color.YELLOW, Color.LTGRAY
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        habitIndex = intent.getIntExtra("HABIT_INDEX", -1)
        if (habitIndex != -1 && habitIndex < DataManager.habits.size) {
            existingHabit = DataManager.habits[habitIndex]
        }

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_habit_root), findViewById(R.id.add_habit_content_container))
    }

    private fun initViews() {
        nameInput = findViewById(R.id.habit_name_input)
        btnSave = findViewById(R.id.btn_save)
        iconPreview = findViewById(R.id.icon_preview)
        colorPreview = findViewById(R.id.color_preview)
        headerAccent = findViewById(R.id.header_bg_accent)
        tvNameHint = findViewById(R.id.tv_name_hint)
        tvScheduleHint = findViewById(R.id.tv_schedule_hint)
        
        findViewById<View>(R.id.btn_close).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        // Initial State
        tempRepeatDays = existingHabit?.repeatDays?.toMutableList() ?: mutableListOf(0, 1, 2, 3, 4, 5, 6)
        selectedFrequency = existingHabit?.frequency ?: "Anytime"
        selectedColor = existingHabit?.color ?: colors[0]
        selectedIcon = existingHabit?.iconResId ?: R.drawable.ic_habit_tracker

        if (existingHabit != null) {
            nameInput.setText(existingHabit?.name)
            btnSave.text = "UPDATE"
            iconPreview.setImageResource(selectedIcon)
        }

        updateThemeVisuals()
        
        // Day Selector
        val dayViews = listOf(R.id.day_0_direct, R.id.day_1_direct, R.id.day_2_direct, R.id.day_3_direct, R.id.day_4_direct, R.id.day_5_direct, R.id.day_6_direct)
            .map { findViewById<TextView>(it) }

        fun refreshDayButtons() {
            dayViews.forEachIndexed { index, tv ->
                val isSelected = tempRepeatDays.contains(index)
                tv.backgroundTintList = ColorStateList.valueOf(if (isSelected) ContextCompat.getColor(this, R.color.chip_selected) else Color.parseColor("#1AFFFFFF"))
                tv.alpha = if (isSelected) 1.0f else 0.5f
            }
            validateInputs()
        }
        refreshDayButtons()

        dayViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                if (tempRepeatDays.contains(index)) {
                    if (tempRepeatDays.size > 1) tempRepeatDays.remove(index)
                } else {
                    tempRepeatDays.add(index)
                }
                refreshDayButtons()
            }
        }

        // Frequency Cards
        val freqCards = mapOf(
            "Morning" to findViewById<View>(R.id.card_morning),
            "Afternoon" to findViewById<View>(R.id.card_afternoon),
            "Evening" to findViewById<View>(R.id.card_evening),
            "Anytime" to findViewById<View>(R.id.card_anytime)
        )

        fun refreshFreqCards() {
            freqCards.forEach { (type, card) ->
                val isActive = type == selectedFrequency
                if (card is com.google.android.material.card.MaterialCardView) {
                    card.setCardBackgroundColor(if (isActive) ContextCompat.getColor(this, R.color.chip_selected) else Color.parseColor("#1AFFFFFF"))
                }
                card.alpha = if (isActive) 1.0f else 0.6f
            }
        }
        refreshFreqCards()

        freqCards.forEach { (type, card) ->
            card.setOnClickListener {
                selectedFrequency = type
                refreshFreqCards()
            }
        }

        nameInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.card_habit_icon).setOnClickListener {
            showIconSelectionDialog { icon ->
                selectedIcon = icon
                iconPreview.setImageResource(selectedIcon)
            }
        }

        findViewById<View>(R.id.card_habit_color).setOnClickListener {
            val currentIndex = colors.indexOf(selectedColor)
            selectedColor = colors[(currentIndex + 1) % colors.size]
            updateThemeVisuals()
        }

        btnSave.setOnClickListener {
            saveHabit()
        }
        
        validateInputs()
    }

    private fun validateInputs() {
        val name = nameInput.text.toString().trim()
        val isNameValid = name.isNotEmpty()
        val isScheduleValid = tempRepeatDays.isNotEmpty()
        val isAllValid = isNameValid && isScheduleValid

        btnSave.alpha = if (isAllValid) 1.0f else 0.3f
        btnSave.isEnabled = isAllValid
        
        if (isAllValid) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
        
        tvNameHint.visibility = if (isNameValid) View.GONE else View.VISIBLE
        tvScheduleHint.visibility = if (isScheduleValid) View.GONE else View.VISIBLE
        
        if (!isNameValid) startPulseAnimation(tvNameHint)
        if (!isScheduleValid) startPulseAnimation(tvScheduleHint)
    }

    private fun updateThemeVisuals() {
        iconPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        headerAccent.backgroundTintList = ColorStateList.valueOf(selectedColor)
        if (btnSave.isEnabled) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
        tvNameHint.setTextColor(selectedColor)
        tvScheduleHint.setTextColor(selectedColor)
    }

    private fun startPulseAnimation(view: View) {
        if (view.tag == "pulsing") return
        view.tag = "pulsing"
        view.animate().alpha(0.4f).setDuration(800).withEndAction {
            view.animate().alpha(1.0f).setDuration(800).withEndAction {
                view.tag = null
                if (view.visibility == View.VISIBLE) startPulseAnimation(view)
            }
        }.start()
    }

    private fun showIconSelectionDialog(onSelected: (Int) -> Unit) {
        val icons = listOf(
            R.drawable.ic_habit_tracker, R.drawable.ic_water, R.drawable.ic_sleep,
            R.drawable.ic_book, R.drawable.ic_meditation, R.drawable.ic_notes,
            R.drawable.ic_fitness, R.drawable.ic_finance, R.drawable.icons8_coffee_100,
            R.drawable.icons8_yoga_100, R.drawable.icons8_clock_100, R.drawable.icons8_laptop_100,
            R.drawable.icons8_typing_100, R.drawable.icons8_sun_50_apng, R.drawable.icons8_health_100_3,
            R.drawable.icons8_clock_100_2, R.drawable.icons8_search_100, R.drawable.icons8_income_100,
            R.drawable.icons8_bookmark_100, R.drawable.icons8_coffee_100_2, R.drawable.icons8_coffee_100_3,
            R.drawable.icons8_coffee_100_4, R.drawable.icons8_coffee_100_5, R.drawable.icons8_drinking_100,
            R.drawable.icons8_drinking_50, R.drawable.icons8_health_100, R.drawable.icons8_health_100_4,
            R.drawable.icons8_health_100_9, R.drawable.icons8_health_100_11, R.drawable.icons8_health_100_12,
            R.drawable.icons8_health_100_13, R.drawable.icons8_heart_health_100, R.drawable.icons8_yoga_100_3,
            R.drawable.icons8_pilates_100, R.drawable.icons8_artistic_gymnastics_100, R.drawable.icons8_walking_100_3
        )
        
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_premium_icon_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val gridLayout = dialog.findViewById<GridLayout>(R.id.premium_icon_grid)
        gridLayout.columnCount = 5
        val btnClose = dialog.findViewById<View>(R.id.btn_close_picker)

        icons.forEach { iconRes ->
            val iconView = ImageView(this)
            val s = (52 * resources.displayMetrics.density).toInt()
            val params = GridLayout.LayoutParams()
            params.width = s; params.height = s; params.setMargins(6, 6, 6, 6)
            iconView.layoutParams = params
            
            iconView.setImageResource(iconRes)
            iconView.setPadding(12, 12, 12, 12)
            iconView.background = ContextCompat.getDrawable(this, R.drawable.circle_selected_bg)
            iconView.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#22FFFFFF"))
            iconView.imageTintList = ColorStateList.valueOf(Color.WHITE)
            
            iconView.setOnClickListener {
                onSelected(iconRes)
                dialog.dismiss()
            }
            gridLayout.addView(iconView)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun saveHabit() {
        val name = nameInput.text.toString().trim()
        if (existingHabit == null) {
            DataManager.habits.add(Habit(name, false, selectedFrequency, color = selectedColor, iconResId = selectedIcon, repeatType = "SPECIFIC_DAYS", repeatDays = tempRepeatDays.toList(), repeatCount = 1))
        } else {
            existingHabit?.let {
                it.name = name
                it.frequency = selectedFrequency
                it.color = selectedColor
                it.iconResId = selectedIcon
                it.repeatDays = tempRepeatDays.toList()
            }
        }
        DataManager.saveData(this)
        setResult(RESULT_OK)
        finish()
    }
}
