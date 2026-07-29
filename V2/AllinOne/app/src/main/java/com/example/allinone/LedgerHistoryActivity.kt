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
        setupKeyboardHandling(findViewById(R.id.ledger_history_root), findViewById(R.id.ledger_history_content_container), 12)
    }

    private fun initSections() {
        listSection = LedgerHistoryListSection(
            findViewById(R.id.history_list),
            onLongClick = { anchor, entry -> showCustomLedgerMenu(anchor, entry) },
            onDelete = { entry ->
                showConfirmationDialog("DELETE ENTRY", "Are you sure you want to permanently delete this record?", "DELETE") {
                    DataManager.ledgerEntries.removeIf { it.id == entry.id }
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
        
        menuView.findViewById<View>(R.id.menu_clear_completed)?.setOnClickListener {
            toggleDeleteMode()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menu_action_primary)?.apply {
            visibility = View.VISIBLE
            findViewById<TextView>(R.id.tv_action_primary)?.text = "CLEAR ALL"
            findViewById<ImageView>(R.id.iv_action_primary)?.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setOnClickListener {
                showConfirmationDialog("CLEAR HISTORY", "Delete all settled ledger entries permanently?", "CLEAR ALL") {
                    DataManager.ledgerEntries.removeAll { it.isSettled }
                    DataManager.saveData(this@LedgerHistoryActivity)
                    listSection.update()
                }
                popupWindow.dismiss()
            }
        }
        
        menuView.findViewById<View>(R.id.menu_activity_settings)?.visibility = View.GONE

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    private fun toggleDeleteMode() {
        viewModel.isDeleteMode = !viewModel.isDeleteMode
        listSection.setDeleteMode(viewModel.isDeleteMode)
        val btnOptions = findViewById<ImageButton>(R.id.btn_history_options)
        btnOptions.setImageResource(if (viewModel.isDeleteMode) android.R.drawable.ic_menu_delete else R.drawable.ic_trash)
    }

    private fun showCustomLedgerMenu(anchor: View, entry: LedgerEntry) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_ledger_history, null)
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
            DataManager.ledgerEntries.removeIf { it.id == entry.id }
            DataManager.saveData(this)
            listSection.update()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    private fun showConfirmationDialog(title: String, message: String, positiveButtonText: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirm_ledger)
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
