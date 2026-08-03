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
import com.google.android.material.chip.ChipGroup
import androidx.activity.viewModels
import com.example.allinone.data.model.Workout
import com.example.allinone.domain.repository.WorkoutSettings
import com.example.allinone.core.utils.UIUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class AddWorkoutActivity : BaseActivity() {

    private val viewModel: WorkoutViewModel by viewModels()
    private var workoutId: Long = -1L
    private var existingWorkout: Workout? = null
    
    private var tempRepeatDays = mutableListOf(0, 1, 2, 3, 4, 5, 6)
    private var selectedMode = "Reps"
    private var selectedFrequency = "Anytime"
    private var selectedColor: Int = -1
    private var selectedIcon: Int = R.drawable.icons8_exercise_100
    private var selectedMuscleGroups = mutableListOf("General")

    private lateinit var nameInput: EditText
    private lateinit var targetInput: EditText
    private lateinit var targetSetsInput: EditText
    private lateinit var repsPerSetInput: EditText
    private lateinit var targetTimerInput: EditText
    private lateinit var btnSave: TextView
    private lateinit var iconPreview: ImageView
    private lateinit var colorPreview: View
    private lateinit var tvNameHint: TextView
    private lateinit var tvScheduleHint: TextView
    private lateinit var tvTargetHint: TextView
    private lateinit var tvGoalTitle: TextView
    private lateinit var chipGroup: ChipGroup

    private lateinit var bgModeReps: View
    private lateinit var bgModeSets: View
    private lateinit var bgModeTimer: View

    private lateinit var layoutRepsGoal: View
    private lateinit var layoutSetsGoal: View
    private lateinit var layoutTimerGoal: View
    private lateinit var btnRollerReps: View
    private lateinit var btnRollerSets: View
    private lateinit var btnRollerTimer: View
    private lateinit var tvLabelReps: View
    private lateinit var tvLabelSets: View
    private lateinit var tvLabelRepsPerSet: View
    private lateinit var tvLabelTimer: View
    
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
        setContentView(R.layout.activity_add_workout)

        workoutId = intent.getLongExtra("WORKOUT_ID", -1L)
        if (workoutId != -1L) {
            existingWorkout = viewModel.workouts.value.find { it.timestamp == workoutId }
        }

        initViews()
        setupLogic()
        setupKeyboardHandling(findViewById(R.id.add_workout_root), findViewById(R.id.add_workout_content_container))
    }

    private fun initViews() {
        // ... (unchanged)
        nameInput = findViewById(R.id.workout_name_input)
        targetInput = findViewById(R.id.target_input)
        targetSetsInput = findViewById(R.id.target_sets_input)
        repsPerSetInput = findViewById(R.id.reps_per_set_input)
        targetTimerInput = findViewById(R.id.target_timer_input)

        layoutRepsGoal = findViewById(R.id.layout_reps_goal)
        layoutSetsGoal = findViewById(R.id.layout_sets_goal)
        layoutTimerGoal = findViewById(R.id.layout_timer_goal)

        btnRollerReps = findViewById(R.id.btn_roller_reps)
        btnRollerSets = findViewById(R.id.btn_roller_sets)
        btnRollerTimer = findViewById(R.id.btn_roller_timer)
        tvLabelReps = findViewById(R.id.tv_label_reps)
        tvLabelSets = findViewById(R.id.tv_label_sets)
        tvLabelRepsPerSet = findViewById(R.id.tv_label_reps_per_set)
        tvLabelTimer = findViewById(R.id.tv_label_timer)

        chipGroup = findViewById(R.id.muscle_chip_group)
        btnSave = findViewById(R.id.btn_save_workout)
        iconPreview = findViewById(R.id.icon_preview_workout)
        colorPreview = findViewById(R.id.color_preview_workout)
        tvNameHint = findViewById(R.id.tv_name_hint_workout)
        tvScheduleHint = findViewById(R.id.tv_schedule_hint_workout)
        tvTargetHint = findViewById(R.id.tv_target_hint_workout)
        tvGoalTitle = findViewById(R.id.tv_goal_title)
        
        bgModeReps = findViewById(R.id.bg_mode_reps)
        bgModeSets = findViewById(R.id.bg_mode_sets)
        bgModeTimer = findViewById(R.id.bg_mode_timer)
        
        findViewById<View>(R.id.btn_close_workout).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        val settings = viewModel.workoutSettings.value
        tempRepeatDays = existingWorkout?.repeatDays?.toMutableList() ?: mutableListOf(0, 1, 2, 3, 4, 5, 6)
        selectedMode = existingWorkout?.trackingMode ?: settings.defaultMode
        selectedFrequency = existingWorkout?.frequency ?: "Anytime"
        selectedColor = existingWorkout?.color ?: colors[0]
        selectedIcon = existingWorkout?.iconResId ?: R.drawable.icons8_exercise_100
        selectedMuscleGroups = existingWorkout?.muscleGroups?.toMutableList() ?: mutableListOf("General")

        if (existingWorkout != null) {
            nameInput.setText(existingWorkout?.name)
            when (selectedMode) {
                "Sets" -> {
                    targetSetsInput.setText(existingWorkout?.target.toString())
                    repsPerSetInput.setText(existingWorkout?.repsPerSet.toString())
                }
                "Timer" -> {
                    targetTimerInput.setText(existingWorkout?.target.toString())
                }
                else -> {
                    targetInput.setText(existingWorkout?.target.toString())
                }
            }
            btnSave.text = "UPDATE"
            UIUtils.safeSetImageResource(iconPreview, selectedIcon, R.drawable.icons8_exercise_100)
        }

        updateThemeVisuals()
        setupGoalRollers()
        
        // Day Selector
        val dayViews = listOf(R.id.day_0_direct_workout, R.id.day_1_direct_workout, R.id.day_2_direct_workout, R.id.day_3_direct_workout, R.id.day_4_direct_workout, R.id.day_5_direct_workout, R.id.day_6_direct_workout)
            .map { findViewById<TextView>(it) }

        fun refreshDayButtons() {
            dayViews.forEachIndexed { index, tv ->
                val isSelected = tempRepeatDays.contains(index)
                tv.isSelected = isSelected
                tv.alpha = if (isSelected) 1.0f else 0.7f
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

        // Mode Cards
        val modeCards = mapOf(
            "Reps" to findViewById<View>(R.id.card_mode_reps),
            "Sets" to findViewById<View>(R.id.card_mode_sets),
            "Timer" to findViewById<View>(R.id.card_mode_timer)
        )

        fun refreshModeCards() {
            bgModeReps.visibility = if (selectedMode == "Reps") View.VISIBLE else View.GONE
            bgModeSets.visibility = if (selectedMode == "Sets") View.VISIBLE else View.GONE
            bgModeTimer.visibility = if (selectedMode == "Timer") View.VISIBLE else View.GONE

            layoutRepsGoal.visibility = if (selectedMode == "Reps") View.VISIBLE else View.GONE
            layoutSetsGoal.visibility = if (selectedMode == "Sets") View.VISIBLE else View.GONE
            layoutTimerGoal.visibility = if (selectedMode == "Timer") View.VISIBLE else View.GONE
            validateInputs()
        }
        refreshModeCards()
        modeCards.forEach { (mode, card) -> card.setOnClickListener { selectedMode = mode; refreshModeCards() } }

        // Frequency Cards
        val freqCards = mapOf(
            "Morning" to findViewById<View>(R.id.card_morning_workout),
            "Afternoon" to findViewById<View>(R.id.card_afternoon_workout),
            "Evening" to findViewById<View>(R.id.card_evening_workout),
            "Anytime" to findViewById<View>(R.id.card_anytime_workout)
        )

        fun refreshFreqCards() {
            freqCards.forEach { (type, card) ->
                val isActive = type == selectedFrequency
                if (card is com.google.android.material.card.MaterialCardView) {
                    card.strokeColor = if (isActive) ContextCompat.getColor(this, R.color.gradient_blue_end) else ContextCompat.getColor(this, R.color.card_border)
                    card.strokeWidth = if (isActive) (2 * resources.displayMetrics.density).toInt() else (1 * resources.displayMetrics.density).toInt()
                }
                card.alpha = if (isActive) 1.0f else 0.7f
            }
        }
        refreshFreqCards()
        freqCards.forEach { (type, card) -> card.setOnClickListener { selectedFrequency = type; refreshFreqCards() } }

        // Muscle Chips
        chipGroup.removeAllViews()
        val chipStates = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        
        val strokeColor = ColorStateList(
            chipStates,
            intArrayOf(
                ContextCompat.getColor(this, R.color.gradient_blue_end),
                ContextCompat.getColor(this, R.color.card_border)
            )
        )
        
        val bgColor = ColorStateList(
            chipStates,
            intArrayOf(
                Color.parseColor("#1E293B"),
                Color.parseColor("#0F172A")
            )
        )

        val appSettings = settings.muscleGroups
        appSettings.forEach { group ->
            val chip = com.google.android.material.chip.Chip(this)
            chip.text = group
            chip.isCheckable = true
            chip.isChecked = selectedMuscleGroups.contains(group)
            
            chip.chipBackgroundColor = bgColor
            chip.chipStrokeColor = strokeColor
            chip.chipStrokeWidth = resources.displayMetrics.density * 1.5f
            chip.setTextColor(Color.WHITE)
            chip.rippleColor = ColorStateList.valueOf(Color.parseColor("#22FFFFFF"))
            
            chip.chipStartPadding = 12f * resources.displayMetrics.density
            chip.chipEndPadding = 12f * resources.displayMetrics.density
            
            chip.setCheckedIconVisible(false)
            chip.setOnCheckedChangeListener { _, isChecked -> 
                if (isChecked) { if (!selectedMuscleGroups.contains(group)) selectedMuscleGroups.add(group) } 
                else { selectedMuscleGroups.remove(group) } 
            }
            chipGroup.addView(chip)
        }

        nameInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        targetInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        targetSetsInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        repsPerSetInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        targetTimerInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { validateInputs() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        findViewById<View>(R.id.card_workout_icon).setOnClickListener {
            showIconSelectionDialog { icon ->
                selectedIcon = icon
                UIUtils.safeSetImageResource(iconPreview, selectedIcon, R.drawable.icons8_exercise_100)
            }
        }

        findViewById<View>(R.id.card_workout_color).setOnClickListener {
            val currentIndex = colors.indexOf(selectedColor)
            selectedColor = colors[(currentIndex + 1) % colors.size]
            updateThemeVisuals()
        }

        btnSave.setOnClickListener {
            saveWorkout()
        }
        
        validateInputs()
    }

    private fun validateInputs() {
        val name = nameInput.text.toString().trim()
        val isNameValid = name.isNotEmpty()
        val isScheduleValid = tempRepeatDays.isNotEmpty()
        
        val isTargetValid = when (selectedMode) {
            "Sets" -> {
                val sets = targetSetsInput.text.toString().toIntOrNull() ?: 0
                val reps = repsPerSetInput.text.toString().toIntOrNull() ?: 0
                sets > 0 && reps > 0
            }
            "Timer" -> {
                val timer = targetTimerInput.text.toString().toIntOrNull() ?: 0
                timer > 0
            }
            else -> {
                val reps = targetInput.text.toString().toIntOrNull() ?: 0
                reps > 0
            }
        }
        
        val isAllValid = isNameValid && isScheduleValid && isTargetValid

        btnSave.alpha = if (isAllValid) 1.0f else 0.3f
        btnSave.isEnabled = isAllValid
        
        if (isAllValid) btnSave.setTextColor(selectedColor) else btnSave.setTextColor(Color.GRAY)
        
        tvNameHint.visibility = if (isNameValid) View.GONE else View.VISIBLE
        tvScheduleHint.visibility = if (isScheduleValid) View.GONE else View.VISIBLE
        tvTargetHint.visibility = if (isTargetValid) View.GONE else View.VISIBLE

        if (!isNameValid) startPulseAnimation(tvNameHint)
        if (!isScheduleValid) startPulseAnimation(tvScheduleHint)
        if (!isTargetValid) startPulseAnimation(tvTargetHint)
    }

    private fun updateThemeVisuals() {
        colorPreview.backgroundTintList = ColorStateList.valueOf(selectedColor)
        if (btnSave.isEnabled) btnSave.setTextColor(ContextCompat.getColor(this, R.color.primary_blue)) else btnSave.setTextColor(Color.GRAY)
        tvNameHint.setTextColor(Color.GRAY)
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
            R.drawable.icons8_exercise_100, R.drawable.icons8_exercise_100_2, R.drawable.icons8_exercise_100_3,
            R.drawable.icons8_exercise_100_4, R.drawable.icons8_exercise_100_5, R.drawable.icons8_exercise_100_6,
            R.drawable.icons8_exercise_100_7, R.drawable.icons8_exercise_100_8, R.drawable.icons8_exercise_100_9,
            R.drawable.icons8_exercise_100_10, R.drawable.icons8_exercise_100_11, R.drawable.icons8_exercise_100_12,
            R.drawable.icons8_exercise_100_13, R.drawable.icons8_exercise_100_14, R.drawable.icons8_exercise_100_15,
            R.drawable.icons8_exercise_100_16, R.drawable.icons8_exercise_100_17, R.drawable.icons8_exercise_100_18,
            R.drawable.icons8_exercise_100_20, R.drawable.icons8_exercise_100_21, R.drawable.icons8_exercise_100_22,
            R.drawable.icons8_exercise_100_23, R.drawable.icons8_exercise_100_25, R.drawable.icons8_exercise_100_26,
            R.drawable.icons8_exercise_100_27, R.drawable.icons8_exercise_100_28, R.drawable.icons8_exercise_100_29,
            R.drawable.icons8_exercise_100_30, R.drawable.icons8_exercise_100_31, R.drawable.icons8_exercise_100_32,
            R.drawable.icons8_exercise_100_33, R.drawable.icons8_exercise_100_34, R.drawable.icons8_exercise_100_36,
            R.drawable.icons8_exercise_100_37, R.drawable.icons8_exercise_100_38, R.drawable.icons8_exercise_100_39,
            R.drawable.icons8_exercise_100_40, R.drawable.icons8_exercise_100_41, R.drawable.icons8_exercise_100_43,
            R.drawable.icons8_exercise_100_44, R.drawable.icons8_exercise_100_45, R.drawable.icons8_exercise_100_47,
            R.drawable.icons8_exercise_100_48, R.drawable.icons8_dumbbell_100, R.drawable.icons8_deadlift_100,
            R.drawable.icons8_plank_100, R.drawable.icons8_skipping_rope_100_2, R.drawable.icons8_treadmill_100_2,
            R.drawable.icons8_warm_up_100, R.drawable.icons8_pilates_100, R.drawable.icons8_triceps_100,
            R.drawable.icons8_yoga_100, R.drawable.icons8_hand_grip_100_2, R.drawable.icons8_walking_100_3,
            R.drawable.icons8_artistic_gymnastics_100, R.drawable.icons8_heart_health_100, R.drawable.icons8_dog_training_100
        )
        
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_icon_picker_workout)
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
                UIUtils.safeSetImageResource(iconPreview, iconRes, R.drawable.icons8_exercise_100)
                dialog.dismiss()
            }
            gridLayout.addView(iconView)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun saveWorkout() {
        val name = nameInput.text.toString().trim()
        
        val target = when (selectedMode) {
            "Sets" -> targetSetsInput.text.toString().toIntOrNull() ?: 0
            "Timer" -> targetTimerInput.text.toString().toIntOrNull() ?: 0
            else -> targetInput.text.toString().toIntOrNull() ?: 0
        }
        val repsPerSet = if (selectedMode == "Sets") repsPerSetInput.text.toString().toIntOrNull() ?: 0 else 0
        
        val finalMuscleSelection = if (selectedMuscleGroups.isEmpty()) listOf("General") else selectedMuscleGroups.toList()
        
        if (existingWorkout == null) {
            viewModel.insertWorkout(Workout(name, false, selectedMode, target, repsPerSet = repsPerSet, frequency = selectedFrequency, color = selectedColor, iconResId = selectedIcon, muscleGroups = finalMuscleSelection, repeatType = "SPECIFIC_DAYS", repeatDays = tempRepeatDays.toList(), repeatCount = 1))
        } else {
            existingWorkout?.let {
                val updatedWorkout = it.copy(
                    name = name,
                    target = target,
                    repsPerSet = repsPerSet,
                    trackingMode = selectedMode,
                    frequency = selectedFrequency,
                    color = selectedColor,
                    iconResId = selectedIcon,
                    muscleGroups = finalMuscleSelection,
                    repeatDays = tempRepeatDays.toList()
                )
                viewModel.updateWorkout(updatedWorkout)
            }
        }
        setResult(RESULT_OK)
        finish()
    }

    private fun setupGoalRollers() {
        val onGoalClick = View.OnClickListener {
            when (selectedMode) {
                "Sets" -> showDividedRollerDialog(targetSetsInput, repsPerSetInput)
                "Timer" -> showTimerRollerDialog(targetTimerInput)
                else -> showSingleRollerDialog("REPS", targetInput, 0, 500)
            }
        }
        
        tvGoalTitle.setOnClickListener(onGoalClick)
        btnRollerReps.setOnClickListener(onGoalClick)
        btnRollerSets.setOnClickListener(onGoalClick)
        btnRollerTimer.setOnClickListener(onGoalClick)
        tvLabelReps.setOnClickListener(onGoalClick)
        tvLabelSets.setOnClickListener(onGoalClick)
        tvLabelRepsPerSet.setOnClickListener(onGoalClick)
        tvLabelTimer.setOnClickListener(onGoalClick)
    }

    private fun showTimerRollerDialog(targetEditText: EditText) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_timer_roller)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val npMin = dialog.findViewById<NumberPicker>(R.id.np_minutes)
        val npSec = dialog.findViewById<NumberPicker>(R.id.np_seconds)
        val btnConfirm = dialog.findViewById<View>(R.id.btn_confirm_picker)
        
        npMin.minValue = 0; npMin.maxValue = 60; npMin.wrapSelectorWheel = false
        npSec.minValue = 0; npSec.maxValue = 59; npSec.wrapSelectorWheel = true
        
        val totalSeconds = targetEditText.text.toString().toIntOrNull() ?: 0
        npMin.value = totalSeconds / 60
        npSec.value = totalSeconds % 60
        
        btnConfirm.setOnClickListener {
            val confirmedSeconds = (npMin.value * 60) + npSec.value
            targetEditText.setText(confirmedSeconds.toString())
            validateInputs()
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showSingleRollerDialog(title: String, targetEditText: EditText, min: Int, max: Int) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_number_picker)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_picker_title)
        val picker = dialog.findViewById<NumberPicker>(R.id.number_picker)
        val btnConfirm = dialog.findViewById<View>(R.id.btn_confirm_picker)
        
        tvTitle.text = title
        picker.minValue = min
        picker.maxValue = max
        picker.wrapSelectorWheel = false
        picker.value = targetEditText.text.toString().toIntOrNull() ?: min
        
        btnConfirm.setOnClickListener {
            targetEditText.setText(picker.value.toString())
            validateInputs()
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }

    private fun showDividedRollerDialog(setsEt: EditText, repsEt: EditText) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_divided_roller)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val npSets = dialog.findViewById<NumberPicker>(R.id.np_sets)
        val npReps = dialog.findViewById<NumberPicker>(R.id.np_reps)
        val btnConfirm = dialog.findViewById<View>(R.id.btn_confirm_picker)
        
        npSets.minValue = 1; npSets.maxValue = 100; npSets.wrapSelectorWheel = false
        npReps.minValue = 1; npReps.maxValue = 500; npReps.wrapSelectorWheel = false
        
        npSets.value = setsEt.text.toString().toIntOrNull() ?: 1
        npReps.value = repsEt.text.toString().toIntOrNull() ?: 1
        
        btnConfirm.setOnClickListener {
            setsEt.setText(npSets.value.toString())
            repsEt.setText(npReps.value.toString())
            validateInputs()
            dialog.dismiss()
        }
        showDialogSafe(dialog)
    }
}
