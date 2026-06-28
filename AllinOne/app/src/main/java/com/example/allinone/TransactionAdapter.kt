package com.example.allinone

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val onEdit: (Transaction, Int) -> Unit,
    private val onDelete: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_list_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.title.text = transaction.title
        
        val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        holder.category.text = sdf.format(Date(transaction.timestamp))
        
        val prefix = if (transaction.type == "Expense") "-" else "+"
        holder.amount.text = String.format(Locale.US, "%s%s%.2f", prefix, DataManager.financeCurrency, transaction.amount)
        
        // Feature 2: Color Coding & Mini-Icons
        val color = when (transaction.type) {
            "Income" -> Color.parseColor("#4CAF50")
            "Saving" -> Color.parseColor("#1A73E8")
            else -> Color.parseColor("#FF5252")
        }
        holder.amount.setTextColor(color)
        holder.iconCard.setCardBackgroundColor(ColorStateList.valueOf(color).withAlpha(30))
        holder.icon.imageTintList = ColorStateList.valueOf(color)
        
        val iconRes = when (transaction.type) {
            "Income" -> R.drawable.icons8_income_100
            "Saving" -> R.drawable.icons8_savings_100
            else -> R.drawable.expenses
        }
        holder.icon.setImageResource(iconRes)

        holder.itemView.setOnLongClickListener {
            showCustomMenu(it, transaction, position)
            true
        }
    }

    private fun showCustomMenu(anchor: View, transaction: Transaction, position: Int) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            onEdit(transaction, position)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            popupWindow.dismiss()
            onDelete(transaction, position)
        }

        // Show popup in the middle of the item since there's no specific button anymore
        popupWindow.showAsDropDown(anchor, anchor.width / 4, -anchor.height / 2)
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions.toMutableList()
        notifyDataSetChanged()
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_transaction_title)
        val category: TextView = itemView.findViewById(R.id.tv_transaction_category)
        val amount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val icon: ImageView = itemView.findViewById(R.id.iv_transaction_type_icon)
        val iconCard: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.card_transaction_icon)
    }
}

