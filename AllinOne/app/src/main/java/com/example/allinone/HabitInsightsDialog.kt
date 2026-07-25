package com.example.allinone

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object HabitInsightsDialog {
    fun show(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_set_budget)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val title = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val etInput = dialog.findViewById<View>(R.id.et_budget_amount)
        val subtext = dialog.findViewById<TextView>(R.id.tv_dialog_subtext)
        val btnSave = dialog.findViewById<View>(R.id.btn_save_budget)

        title.text = "BEHAVIORAL INSIGHTS"
        etInput.visibility = View.GONE
        
        val stats = DataManager.getHabitPerformanceByFrequency()
        val peak = stats.maxByOrNull { it.value }
        val moodInsight = DataManager.getMoodCorrelationData()
        
        val sb = StringBuilder()
        if (peak == null || peak.value <= 0) {
            sb.append("Not enough data yet. Keep tracking your habits to see your peak performance times!\n")
        } else {
            sb.append("Your Peak Performance Time: ${peak.key.uppercase()}\n\n")
            stats.forEach { (freq, score) ->
                if (score >= 0) {
                    sb.append("$freq Habits: $score% Completion\n")
                }
            }
        }

        if (moodInsight != null) {
            sb.append("\n${moodInsight}")
        }
        
        subtext.text = sb.toString()
        
        (btnSave as? TextView)?.text = "CLOSE"
        btnSave.setOnClickListener { dialog.dismiss() }
        
        if (context is BaseActivity) {
            context.showDialogSafe(dialog)
        } else {
            dialog.show()
        }
    }
}
