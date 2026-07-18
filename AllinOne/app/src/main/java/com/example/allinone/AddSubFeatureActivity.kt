package com.example.allinone

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

    private var selectedTag: String = ""
    private var selectedDueDate: Long? = null

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
                // Check temp list in AddProjectActivity
                targetFeature = AddProjectActivity.currentEditingSubFeatures.find { it.id == subFeatureId }
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

        findViewById<View>(R.id.btn_close_subfeature).setOnClickListener { finish() }
    }

    private fun setupLogic() {
        targetFeature?.let { feature ->
            etName.setText(feature.name)
            etDetails.setText(feature.details)
            selectedTag = feature.tag
            selectedDueDate = feature.dueDate
            
            updateDeadlineUI()
            refreshTagsUI()
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

        btnDelete.setOnClickListener {
            val project = DataManager.notes.getOrNull(projectIndex)
            project?.subFeatures?.remove(targetFeature)
            DataManager.saveData(this)
            setResult(RESULT_OK)
            finish()
        }

        btnSave.setOnClickListener {
            saveFeature()
        }
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

        // Requirement 1: Prevent duplicate names in the same project
        val project = DataManager.notes.getOrNull(projectIndex)
        val isDuplicate = if (projectIndex != -1) {
            project?.subFeatures?.any { it.id != subFeatureId && it.name.equals(name, ignoreCase = true) } == true
        } else {
            AddProjectActivity.currentEditingSubFeatures.any { it.id != subFeatureId && it.name.equals(name, ignoreCase = true) }
        }

        if (isDuplicate) {
            Toast.makeText(this, "A feature with this name already exists", Toast.LENGTH_SHORT).show()
            return
        }

        targetFeature?.let {
            it.name = name
            it.details = etDetails.text.toString().trim()
            it.tag = selectedTag
            it.dueDate = selectedDueDate
        }

        DataManager.saveData(this)
        setResult(RESULT_OK)
        finish()
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
