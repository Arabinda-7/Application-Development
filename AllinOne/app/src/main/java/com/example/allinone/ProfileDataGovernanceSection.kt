package com.example.allinone

import android.view.View
import android.widget.ImageView
import android.widget.TextView

class ProfileDataGovernanceSection(
    private val rootView: View,
    private val onExportClicked: () -> Unit
) {
    fun setup() {
        val exportItem = rootView.findViewById<View>(R.id.item_export_data)
        exportItem.findViewById<ImageView>(R.id.iv_item_icon).setImageResource(R.drawable.baseline_tune_24)
        exportItem.findViewById<TextView>(R.id.tv_item_title).text = "Export Data Backup"
        exportItem.findViewById<TextView>(R.id.tv_item_description).text = "Generate a JSON recovery file"
        
        exportItem.setOnClickListener { onExportClicked() }
    }

    fun applyTint(color: Int) {
        val exportItem = rootView.findViewById<View>(R.id.item_export_data)
        exportItem.findViewById<ImageView>(R.id.iv_item_icon).imageTintList = android.content.res.ColorStateList.valueOf(color)
    }
}
