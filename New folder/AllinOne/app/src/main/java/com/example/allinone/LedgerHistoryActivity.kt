package com.example.allinone

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class LedgerHistoryActivity : AppCompatActivity() {

    private val allEntries = DataManager.ledgerEntries
    private val settledEntries = mutableListOf<LedgerEntry>()
    private lateinit var historyAdapter: HistoryAdapter
    private var isDeleteMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ledger_history)

        val historyList = findViewById<RecyclerView>(R.id.history_list)
        historyList.layoutManager = LinearLayoutManager(this)

        updateHistoryList()
        historyAdapter = HistoryAdapter(settledEntries, 
            onLongClick = { anchor, entry ->
                showCustomLedgerMenu(anchor, entry)
            },
            onDeleteClick = { entry ->
                allEntries.remove(entry)
                DataManager.saveData(this)
                updateHistoryList()
            }
        )
        historyList.adapter = historyAdapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        
        val btnOptions = findViewById<ImageButton>(R.id.btn_history_options)
        btnOptions.setOnClickListener {
            showHistoryOptionsMenu(it)
        }
    }

    private fun showHistoryOptionsMenu(anchor: View) {
        if (isDeleteMode) {
            // Feature 1: Clicking the icon while in delete mode returns the page to normal
            toggleDeleteMode()
            return
        }

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_ledger_history_settings)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<TextView>(R.id.tv_delete_mode_title).text = if (isDeleteMode) "Disable Delete Mode" else "Enable Delete Mode"

        dialog.findViewById<View>(R.id.option_toggle_delete).setOnClickListener {
            toggleDeleteMode()
            dialog.dismiss()
        }

        dialog.findViewById<View>(R.id.option_clear_all).setOnClickListener {
            dialog.dismiss()
            if (settledEntries.isNotEmpty()) {
                showConfirmationDialog(
                    title = "DELETE ALL HISTORY",
                    message = "This will permanently remove all settled ledger entries. This action cannot be undone.",
                    positiveButtonText = "DELETE ALL",
                    onConfirm = {
                        allEntries.removeAll { it.isSettled }
                        DataManager.saveData(this)
                        updateHistoryList()
                        Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "History is already empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.findViewById<View>(R.id.btn_close_settings).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun toggleDeleteMode() {
        isDeleteMode = !isDeleteMode
        findViewById<ImageButton>(R.id.btn_history_options).imageTintList = ColorStateList.valueOf(
            if (isDeleteMode) Color.parseColor("#FF5252") else Color.WHITE
        )
        historyAdapter.setDeleteMode(isDeleteMode)
    }

    private fun updateHistoryList() {
        settledEntries.clear()
        settledEntries.addAll(allEntries.filter { it.isSettled }.sortedByDescending { it.timestamp })
        if (::historyAdapter.isInitialized) {
            historyAdapter.notifyDataSetChanged()
        }
    }

    private fun showCustomLedgerMenu(anchor: View, entry: LedgerEntry) {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 20f

        val btnUndo = menuView.findViewById<View>(R.id.menu_undo)
        val btnDelete = menuView.findViewById<View>(R.id.menu_delete)
        val btnEdit = menuView.findViewById<View>(R.id.menu_edit)
        val btnDayOff = menuView.findViewById<View>(R.id.menu_take_day_off)
        val btnHide = menuView.findViewById<View>(R.id.menu_hide_unhide)

        btnDayOff.visibility = View.GONE
        btnHide.visibility = View.GONE
        btnEdit.visibility = View.GONE
        btnUndo.visibility = View.VISIBLE

        btnUndo.setOnClickListener {
            entry.isSettled = false
            DataManager.saveData(this)
            updateHistoryList()
            popupWindow.dismiss()
            Toast.makeText(this, "Entry moved back to active", Toast.LENGTH_SHORT).show()
        }

        btnDelete.setOnClickListener {
            allEntries.remove(entry)
            DataManager.saveData(this)
            updateHistoryList()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButtonText: String = "PROCEED",
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_confirm_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_confirm_message)
        val btnNegative = dialog.findViewById<TextView>(R.id.btn_confirm_negative)
        val btnPositive = dialog.findViewById<TextView>(R.id.btn_confirm_positive)

        tvTitle.text = title
        tvMessage.text = message
        btnPositive.text = positiveButtonText

        btnNegative.setOnClickListener { dialog.dismiss() }
        btnPositive.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }
        dialog.show()
    }

    class HistoryAdapter(
        private val entries: List<LedgerEntry>,
        private val onLongClick: (View, LedgerEntry) -> Unit,
        private val onDeleteClick: (LedgerEntry) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        private var isDeleteMode = false

        fun setDeleteMode(enabled: Boolean) {
            isDeleteMode = enabled
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = entries[position]
            holder.tvName.text = entry.personName
            holder.tvName.paintFlags = holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            
            holder.tvType.text = entry.type.uppercase()
            holder.tvAmount.text = "${DataManager.financeCurrency}${entry.amount.toInt()}"
            holder.tvDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(entry.timestamp))
            
            if (entry.settlementTimestamp != null) {
                holder.tvSettledDate.text = "Settled: ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(entry.settlementTimestamp!!))}"
                holder.tvSettledDate.visibility = View.VISIBLE
            } else {
                holder.tvSettledDate.visibility = View.GONE
            }
            
            val typeColor = if (entry.type == "Borrowed") Color.parseColor("#FF5252") else Color.parseColor("#4CAF50")
            holder.tvAmount.setTextColor(typeColor)

            // Feature 2: Toggle between 'done' and 'cross' icons based on Delete Mode
            if (isDeleteMode) {
                holder.ivStatus.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                holder.ivStatus.imageTintList = ColorStateList.valueOf(Color.parseColor("#FF5252"))
                holder.ivStatus.setOnClickListener { onDeleteClick(entry) }
                holder.ivStatus.isClickable = true
            } else {
                holder.ivStatus.setImageResource(R.drawable.icons8_done_100)
                holder.ivStatus.imageTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                holder.ivStatus.setOnClickListener(null)
                holder.ivStatus.isClickable = false
            }

            holder.itemView.setOnLongClickListener {
                onLongClick(it, entry)
                true
            }
        }

        override fun getItemCount() = entries.size

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tv_person_name)
            val tvType: TextView = v.findViewById(R.id.tv_ledger_type)
            val tvAmount: TextView = v.findViewById(R.id.tv_ledger_amount)
            val tvDate: TextView = v.findViewById(R.id.tv_ledger_date)
            val tvSettledDate: TextView = v.findViewById(R.id.tv_settled_date)
            val ivStatus: ImageView = v.findViewById(R.id.iv_ledger_status)
        }
    }
}
