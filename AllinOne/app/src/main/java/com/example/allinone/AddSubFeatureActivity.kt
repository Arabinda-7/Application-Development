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
    private var isIdeaMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sub_feature)

        projectIndex = intent.getIntExtra("PROJECT_INDEX", -1)
        subFeatureId = intent.getStringExtra("SUB_FEATURE_ID")
        isIdeaMode = intent.getBooleanExtra("IS_IDEA", false)

        if (subFeatureId != null) {
            // Priority 1: Check active editing lists (Real-time fixes)
            targetFeature = DataManager.currentEditingSubFeatures.find { it.id == subFeatureId }
                ?: DataManager.currentEditingIdeaSubFeatures.find { it.id == subFeatureId }
            
            // Priority 2: Fallback to persistent storage
            if (targetFeature == null && projectIndex != -1) {
                val project = DataManager.projects.getOrNull(projectIndex)
                targetFeature = project?.subFeatures?.find { it.id == subFeatureId }
            }
        }

        if (targetFeature == null) {
            Toast.makeText(this, "Feature not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupLogic()
        applyIdeaModeUI()
        setupKeyboardHandling(findViewById(R.id.add_sub_feature_root), findViewById(R.id.add_sub_feature_content_container))
    }

    private fun applyIdeaModeUI() {
        if (isIdeaMode) {
            findViewById<TextView>(R.id.label_tags).visibility = View.GONE
            findViewById<View>(R.id.scroll_tags).visibility = View.GONE
            findViewById<TextView>(R.id.label_due_date).visibility = View.GONE
            findViewById<View>(R.id.container_due_date).visibility = View.GONE
            findViewById<TextView>(R.id.label_weight).visibility = View.GONE
            findViewById<View>(R.id.container_weight).visibility = View.GONE
            findViewById<TextView>(R.id.label_urgency).visibility = View.GONE
            findViewById<View>(R.id.rg_feature_priority).visibility = View.GONE
            findViewById<TextView>(R.id.label_dependency).visibility = View.GONE
            findViewById<View>(R.id.tv_blocked_by_selector).visibility = View.GONE
            findViewById<TextView>(R.id.label_resource).visibility = View.GONE
            findViewById<View>(R.id.et_resource_url).visibility = View.GONE
            findViewById<View>(R.id.btn_delete_subfeature).visibility = View.GONE
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.et_name_input)
        etDetails = findViewById(R.id.et_details_input)
        tvDeadline = findViewById(R.id.tv_feature_deadline)
        containerTags = findViewById(R.id.container_feature_tags)
        btnSave = findViewById(R.id.btn_save_subfeature)
        btnDelete = findViewById(R.id.btn_delete_subfeature)

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
            blockedByFeatureId = feature.blockedByNodeId
            etResourceUrl.setText(feature.resourceUrl)
            
            updateDeadlineUI()
            refreshTagsUI()
            updateWeightUI()
            updatePriorityUI()
            updateBlockedByUI()
            applyTagSpecificLayout(selectedTag)
        }

        tvDeadline.setOnClickListener {
            val cal = Calendar.getInstance()
            if (selectedDueDate != null) {
                cal.timeInMillis = selectedDueDate!!
            } else {
                // Smart Deadline: Default to 7 days from now
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            DatePickerDialog(this, { _, y, m, d ->
                val newCal = Calendar.getInstance()
                newCal.set(y, m, d)
                selectedDueDate = newCal.timeInMillis
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
            // Priority: Remove from active editing lists if present
            val removedFromActive = DataManager.currentEditingSubFeatures.remove(targetFeature) ||
                                   DataManager.currentEditingIdeaSubFeatures.remove(targetFeature)
            
            // Fallback: Remove from persistent storage
            if (!removedFromActive && projectIndex != -1) {
                DataManager.projects.getOrNull(projectIndex)?.subFeatures?.remove(targetFeature)
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
        // Priority 1: Use active editing lists if populated
        val activeList = if (DataManager.currentEditingSubFeatures.isNotEmpty()) {
            DataManager.currentEditingSubFeatures
        } else if (DataManager.currentEditingIdeaSubFeatures.isNotEmpty()) {
            DataManager.currentEditingIdeaSubFeatures
        } else null

        // Priority 2: Fallback to persistent storage
        val list = activeList ?: (if (projectIndex != -1) DataManager.projects.getOrNull(projectIndex)?.subFeatures else null)
        
        return (list ?: emptyList()).filter { it.id != subFeatureId }
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
        val deadlineText = selectedDueDate?.let { 
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(it)) 
        } ?: "Set Due Date"
        
        tvDeadline.text = deadlineText

        // Smart Hint logic: If no deadline and created > 2 days ago
        targetFeature?.let { feature ->
            if (selectedDueDate == null) {
                val ageDays = (System.currentTimeMillis() - feature.createdAt) / (1000 * 60 * 60 * 24)
                if (ageDays >= 2) {
                    tvDeadline.text = "$deadlineText (Suggestion: Set now)"
                    tvDeadline.setTextColor(Color.parseColor("#FFB800"))
                } else {
                    tvDeadline.setTextColor(Color.WHITE)
                }
            } else {
                tvDeadline.setTextColor(Color.WHITE)
            }
        }
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
                    applyTagSpecificLayout(selectedTag)
                }
            }
            containerTags.addView(chip)
        }
    }

    private fun saveFeature() {
        val nameInput = etName.text.toString().trim()
        if (nameInput.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Duplicate check prioritizing active editing lists
        val activeList = if (DataManager.currentEditingSubFeatures.isNotEmpty()) {
            DataManager.currentEditingSubFeatures
        } else if (DataManager.currentEditingIdeaSubFeatures.isNotEmpty()) {
            DataManager.currentEditingIdeaSubFeatures
        } else null
        
        val list = activeList ?: (if (projectIndex != -1) DataManager.projects.getOrNull(projectIndex)?.subFeatures else emptyList())
        val otherFeatures = list?.filter { it.id != subFeatureId } ?: emptyList()
        
        val finalName = DataManager.getUniqueFeatureName(nameInput, otherFeatures)

        targetFeature?.let {
            it.name = finalName
            it.details = etDetails.text.toString().trim()
            it.tag = selectedTag
            it.dueDate = selectedDueDate
            
            // Milestone Enhancements
            it.weight = selectedWeight
            it.priority = selectedPriority
            it.hasReminder = selectedDueDate != null
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

    private fun applyTagSpecificLayout(tag: String) {
        if (isIdeaMode) return // Idea mode already hides most things

        val upperTag = tag.uppercase()
        val showDueDate = upperTag == "TASKS" || upperTag == "FEATURES" || upperTag == ""
        val showWeight = upperTag == "FEATURES" || upperTag == ""
        val showUrgency = upperTag == "TASKS" || upperTag == "FEATURES" || upperTag == "BUGS" || upperTag == ""
        val showDependency = upperTag == "TASKS" || upperTag == "FEATURES" || upperTag == "BUGS" || upperTag == ""
        val showResource = upperTag == "RESOURCES" || upperTag == "FEATURES" || upperTag == ""

        findViewById<View>(R.id.label_due_date).visibility = if (showDueDate) View.VISIBLE else View.GONE
        findViewById<View>(R.id.container_due_date).visibility = if (showDueDate) View.VISIBLE else View.GONE

        findViewById<View>(R.id.label_weight).visibility = if (showWeight) View.VISIBLE else View.GONE
        findViewById<View>(R.id.container_weight).visibility = if (showWeight) View.VISIBLE else View.GONE

        findViewById<View>(R.id.label_urgency).visibility = if (showUrgency) View.VISIBLE else View.GONE
        findViewById<View>(R.id.rg_feature_priority).visibility = if (showUrgency) View.VISIBLE else View.GONE

        findViewById<View>(R.id.label_dependency).visibility = if (showDependency) View.VISIBLE else View.GONE
        findViewById<View>(R.id.tv_blocked_by_selector).visibility = if (showDependency) View.VISIBLE else View.GONE

        findViewById<View>(R.id.label_resource).visibility = if (showResource) View.VISIBLE else View.GONE
        findViewById<View>(R.id.et_resource_url).visibility = if (showResource) View.VISIBLE else View.GONE
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
