package com.example.allinone

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ProjectHeaderSection(
    private val rootView: View,
    private val onBack: () -> Unit,
    private val onEditModeToggled: () -> Unit,
    private val onSettingsClicked: (View) -> Unit,
    private val onWorkspaceClicked: () -> Unit
) {
    fun setup() {
        val dateTextView = rootView.findViewById<TextView>(R.id.tv_date)
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        dateTextView.text = sdf.format(DataManager.getTrackingCalendar().time)

        rootView.findViewById<View>(R.id.btn_back).setOnClickListener { onBack() }
        rootView.findViewById<View>(R.id.btn_edit_mode).setOnClickListener { onEditModeToggled() }
        rootView.findViewById<View>(R.id.btn_project_settings).setOnClickListener { onSettingsClicked(it) }
        rootView.findViewById<View>(R.id.btn_workspace).setOnClickListener { onWorkspaceClicked() }
    }
}
