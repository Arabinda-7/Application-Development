package com.example.allinone

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class NotesHeaderSection(
    private val rootView: View,
    private val onBack: () -> Unit,
    private val onSettingsClicked: (View) -> Unit
) {
    private val btnSettings: ImageButton = rootView.findViewById(R.id.btn_notes_settings)

    fun setup() {
        rootView.findViewById<View>(R.id.btn_back).setOnClickListener { onBack() }
        btnSettings.setOnClickListener { onSettingsClicked(it) }
    }

    fun setSettingsIcon(resId: Int) {
        btnSettings.setImageResource(resId)
    }
}
