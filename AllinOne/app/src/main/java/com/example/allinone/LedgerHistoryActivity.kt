package com.example.allinone

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels

class LedgerHistoryActivity : BaseActivity() {

    private val viewModel: LedgerHistoryViewModel by viewModels()
    
    private lateinit var listSection: LedgerHistoryListSection
    private lateinit var themeManager: LedgerHistoryThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ledger_history)

        initSections()
        setupLogic()
    }

    private fun initSections() {
        listSection = LedgerHistoryListSection(
            findViewById(R.id.history_list),
            onLongClick = { anchor, entry -> showCustomLedgerMenu(anchor, entry) },
            onDelete = { entry ->
                showConfirmationDialog("DELETE ENTRY", "Are you sure you want to permanently delete this record?", "DELETE") {
                    DataManager.ledgerEntries.remove(entry)
                    DataManager.saveData(this)
                    listSection.update()
                }
            }
        )

        themeManager = LedgerHistoryThemeManager(findViewById(R.id.ledger_history_aura_background))
    }

    private fun setupLogic() {
        listSection.update()
        themeManager.applyTheme()

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_history_options).setOnClickListener { showHistoryOptionsMenu(it) }
    }

    private fun showHistoryOptionsMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_menu_ledger, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        val btnDeleteMode = menuView.findViewById<View>(R.id.menu_clear_completed)
        btnDeleteMode.visibility = View.VISIBLE
        (btnDeleteMode as? ViewGroup)?.let { group ->
            (group.getChildAt(1) as? TextView)?.text = if (viewModel.isDeleteMode) "EXIT DELETE MODE" else "DELETE MODE"
        }
        
        btnDeleteMode.setOnClickListener {
            toggleDeleteMode()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_toggle_completed).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_action_primary).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_activity_settings).visibility = View.GONE

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun toggleDeleteMode() {
        viewModel.isDeleteMode = !viewModel.isDeleteMode
        listSection.setDeleteMode(viewModel.isDeleteMode)
        val btnOptions = findViewById<ImageButton>(R.id.btn_history_options)
        btnOptions.setImageResource(if (viewModel.isDeleteMode) android.R.drawable.ic_menu_delete else R.drawable.baseline_tune_24)
    }

    private fun showCustomLedgerMenu(anchor: View, entry: LedgerEntry) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        menuView.findViewById<View>(R.id.menu_undo).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                entry.isSettled = false
                DataManager.saveData(this@LedgerHistoryActivity)
                listSection.update()
                popupWindow.dismiss()
            }
        }

        menuView.findViewById<View>(R.id.menu_edit).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_hide_unhide).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            DataManager.ledgerEntries.remove(entry)
            DataManager.saveData(this)
            listSection.update()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    private fun showConfirmationDialog(title: String, message: String, positiveButtonText: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.findViewById<TextView>(R.id.tv_confirm_title).text = title
        dialog.findViewById<TextView>(R.id.tv_confirm_message).text = message
        dialog.findViewById<TextView>(R.id.btn_confirm_positive).apply { text = positiveButtonText; setOnClickListener { onConfirm(); dialog.dismiss() } }
        dialog.findViewById<View>(R.id.btn_confirm_negative).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        listSection.update()
        themeManager.applyTheme()
    }
}
