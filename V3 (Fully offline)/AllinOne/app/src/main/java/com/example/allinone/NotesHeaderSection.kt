package com.example.allinone

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class NotesHeaderSection(
    private val rootView: View,
    private val onBack: () -> Unit,
    private val onSearchChanged: (String) -> Unit,
    private val onSettingsClicked: (View) -> Unit
) {
    private val etSearch: EditText = rootView.findViewById(R.id.et_note_search)
    private val btnSearch: ImageButton = rootView.findViewById(R.id.btn_note_search)
    private val btnSettings: ImageButton = rootView.findViewById(R.id.btn_notes_settings)
    private val tvDate: TextView = rootView.findViewById(R.id.tv_date)

    fun setup() {
        tvDate.text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date())

        rootView.findViewById<View>(R.id.btn_back).setOnClickListener { onBack() }

        btnSearch.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                etSearch.visibility = View.GONE
                etSearch.text.clear()
                onSearchChanged("")
            } else {
                etSearch.visibility = View.VISIBLE
                etSearch.requestFocus()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onSearchChanged(s.toString())
            }
        })

        btnSettings.setOnClickListener { onSettingsClicked(it) }
    }

    fun setSettingsIcon(resId: Int) {
        btnSettings.setImageResource(resId)
    }
}
