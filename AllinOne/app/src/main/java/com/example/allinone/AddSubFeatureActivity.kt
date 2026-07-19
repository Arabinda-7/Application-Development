package com.example.allinone

import android.content.Context
import android.content.Intent
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class AddSubFeatureActivity : BaseActivity() {

    private var projectIndex: Int = -1
    private var subFeatureId: String? = null
    private var targetFeature: ProjectFeature? = null
    
    private lateinit var etName: EditText
    private lateinit var etDetails: EditText
    private lateinit var tvDeadline: TextView
    private lateinit var containerTags: LinearLayout
    private lateinit var btnSave: TextView
    private lateinit var btnDelete: Button

    private lateinit var swReminder: androidx.appcompat.widget.SwitchCompat
    private lateinit var seekWeight: SeekBar
    private lateinit var tvWeightValue: TextView
    private lateinit var rgPriority: RadioGroup
    private lateinit var tvBlockedBy: TextView
    private lateinit var etResourceUrl: EditText

    private var selectedTag: String = ""
    private var selectedDueDate: Long? = null
    private var blockedByFeatureId: String = ""
    private var selectedWeight: Int = 1
    private var selectedPriority: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sub_feature)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.header_sub_feature)) { v, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val basePadding = (24 * resources.displayMetrics.density).toInt()
            v.setPadding(basePadding, statusBars.top + basePadding, basePadding, basePadding)
            insets
        }

        projectIndex = intent.getIntExtra("PROJECT_INDEX", -1)
        subFeatureId = intent.getStringExtra("SUB_FEATURE_ID")

        if (subFeatureId != null) {
            if (projectIndex != -1) {
                val project = DataManager.notes.getOrNull(projectIndex)
                targetFeature = project?.subFeatures?.find { it.id == subFeatureId }
            } else {
                // Check temp list in AddProjectActivity or ProjectActivity (Ideas)
                targetFeature = AddProjectActivity.currentEditingSubFeatures.find { it.id == subFeatureId }
                    ?: ProjectActivity.currentEditingIdeaSubFeatures.find { it.id == subFeatureId }
            }
        }

        if (targetFeature == null) {
            Toast.makeText(this, "Feature not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupLogic()
    }

    private fun initViews() {
        etName = findViewById(R.id.et_name_input)
        etDetails = findViewById(R.id.et_details_input)
        tvDeadline = findViewById(R.id.tv_feature_deadline)
        containerTags = findViewById(R.id.container_feature_tags)
        btnSave = findViewById(R.id.btn_save_subfeature)
        btnDelete = findViewById(R.id.btn_delete_subfeature)

        swReminder = findViewById(R.id.sw_feature_reminder)
        seekWeight = findViewById(R.id.seek_feature_weight)
        tvWeightValue = findViewById(R.id.tv_weight_value)
        rgPriority = findViewById(R.id.rg_feature_priority)
        tvBlockedBy = findViewById(R.id.tv_blocked_by_selector)
        etResourceUrl = findViewById(R.id.et_resource_url)

        findViewById<View>(R.id.btn_close_subfeature).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        targetFeature?.let { feature ->
            etName.setText(feature.name)
            etDetails.setText(feature.details)
            selectedTag = feature.tag
            selectedDueDate = feature.dueDate
            
            // Milestone Enhancements
            selectedWeight = feature.weight
            selectedPriority = feature.priority
            swReminder.isChecked = feature.hasReminder
            blockedByFeatureId = feature.blockedByNodeId
            etResourceUrl.setText(feature.resourceUrl)
            
            updateDeadlineUI()
            refreshTagsUI()
            updateWeightUI()
            updatePriorityUI()
            updateBlockedByUI()
        }

        tvDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            selectedDueDate?.let { cal.timeInMillis = it }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d)
                selectedDueDate = cal.timeInMillis
                updateDeadlineUI()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        seekWeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedWeight = progress.coerceAtLeast(1)
                updateWeightUI()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        rgPriority.setOnCheckedChangeListener { _, checkedId ->
            selectedPriority = when (checkedId) {
                R.id.rb_prio_med -> 1
                R.id.rb_prio_high -> 2
                else -> 0
            }
        }

        tvBlockedBy.setOnClickListener {
            showBlockedByDialog()
        }

        btnDelete.setOnClickListener {
            val project = if (projectIndex != -1) DataManager.notes.getOrNull(projectIndex) else null
            project?.subFeatures?.remove(targetFeature)
            
            if (projectIndex == -1) {
                AddProjectActivity.currentEditingSubFeatures.remove(targetFeature)
                ProjectActivity.currentEditingIdeaSubFeatures.remove(targetFeature)
            }
            
            DataManager.saveData(this)
            setResult(RESULT_OK)
            finish()
        }

        btnSave.setOnClickListener {
            saveFeature()
        }
    }

    private fun updateWeightUI() {
        tvWeightValue.text = "w$selectedWeight"
        seekWeight.progress = selectedWeight
    }

    private fun updatePriorityUI() {
        when (selectedPriority) {
            1 -> rgPriority.check(R.id.rb_prio_med)
            2 -> rgPriority.check(R.id.rb_prio_high)
            else -> rgPriority.check(R.id.rb_prio_low)
        }
    }

    private fun updateBlockedByUI() {
        val otherFeatures = getOtherFeatures()
        val blockedBy = otherFeatures.find { it.id == blockedByFeatureId }
        tvBlockedBy.text = blockedBy?.name ?: "None (Click to set)"
    }

    private fun getOtherFeatures(): List<ProjectFeature> {
        val project = if (projectIndex != -1) DataManager.notes.getOrNull(projectIndex) else null
        val list = project?.subFeatures ?: (if (AddProjectActivity.currentEditingSubFeatures.isNotEmpty()) AddProjectActivity.currentEditingSubFeatures else ProjectActivity.currentEditingIdeaSubFeatures)
        return list.filter { it.id != subFeatureId }
    }

    private fun showBlockedByDialog() {
        val otherFeatures = getOtherFeatures()
        if (otherFeatures.isEmpty()) {
            Toast.makeText(this, "No other features to depend on", Toast.LENGTH_SHORT).show()
            return
        }

        val names = mutableListOf("None")
        names.addAll(otherFeatures.map { it.name })

        android.app.AlertDialog.Builder(this)
            .setTitle("Blocked By")
            .setItems(names.toTypedArray()) { _, which ->
                blockedByFeatureId = if (which == 0) "" else otherFeatures[which - 1].id
                updateBlockedByUI()
            }
            .show()
    }

    private fun updateDeadlineUI() {
        tvDeadline.text = selectedDueDate?.let { 
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) 
        } ?: "Set Due Date"
    }

    private fun refreshTagsUI() {
        containerTags.removeAllViews()
        DataManager.projectCustomTags.forEach { tagName ->
            val chip = TextView(this).apply {
                text = tagName
                setTextColor(Color.WHITE)
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(24.dpToPx(), 12.dpToPx(), 24.dpToPx(), 12.dpToPx())
                
                val isSelected = selectedTag == tagName
                background = ContextCompat.getDrawable(this@AddSubFeatureActivity, R.drawable.priority_chip_bg)
                backgroundTintList = ColorStateList.valueOf(if (isSelected) Color.parseColor("#1A73E8") else Color.parseColor("#33FFFFFF"))
                
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.marginEnd = 8.dpToPx()
                layoutParams = params

                setOnClickListener {
                    selectedTag = if (selectedTag == tagName) "" else tagName
                    refreshTagsUI()
                }
            }
            containerTags.addView(chip)
        }
    }

    private fun saveFeature() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val project = if (projectIndex != -1) DataManager.notes.getOrNull(projectIndex) else null
        val list = project?.subFeatures ?: (if (AddProjectActivity.currentEditingSubFeatures.isNotEmpty()) AddProjectActivity.currentEditingSubFeatures else ProjectActivity.currentEditingIdeaSubFeatures)
        val isDuplicate = list.any { it.id != subFeatureId && it.name.equals(name, ignoreCase = true) }

        if (isDuplicate) {
            Toast.makeText(this, "A feature with this name already exists", Toast.LENGTH_SHORT).show()
            return
        }

        targetFeature?.let {
            it.name = name
            it.details = etDetails.text.toString().trim()
            it.tag = selectedTag
            it.dueDate = selectedDueDate
            
            // Milestone Enhancements
            it.weight = selectedWeight
            it.priority = selectedPriority
            it.hasReminder = swReminder.isChecked
            it.blockedByNodeId = blockedByFeatureId
            it.resourceUrl = etResourceUrl.text.toString().trim()
        }

        DataManager.saveData(this)
        
        // Schedule Reminder
        if (targetFeature?.hasReminder == true && targetFeature?.dueDate != null) {
            val time = targetFeature?.dueDate!!
            if (time > System.currentTimeMillis()) {
                scheduleMilestoneReminder(targetFeature!!)
            }
        }

        setResult(RESULT_OK)
        finish()
    }

    private fun scheduleMilestoneReminder(feature: ProjectFeature) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                return
            }
        }

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("TASK_NAME", "Milestone: ${feature.name}")
            putExtra("TASK_TIMESTAMP", feature.dueDate)
        }
        
        val requestCode = feature.id.hashCode()
        val pendingIntent = android.app.PendingIntent.getBroadcast(this, requestCode, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, feature.dueDate!!, pendingIntent)
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
