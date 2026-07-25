package com.example.allinone

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class FinanceListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val layoutEmptyState: View,
    private val onDataChanged: () -> Unit
) {
    val transactionAdapter: TransactionAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        transactionAdapter = TransactionAdapter(
            mutableListOf(),
            onEdit = { transaction, _ -> 
                val intent = Intent(context, AddFinanceActivity::class.java).apply {
                    putExtra("TRANSACTION_INDEX", DataManager.transactions.indexOf(transaction))
                }
                context.startActivity(intent)
            },
            onDelete = { transaction, _ -> 
                DataManager.transactions.remove(transaction)
                DataManager.saveData(context)
                onDataChanged()
            }
        )
        recyclerView.adapter = transactionAdapter
    }

    fun loadCurrentMonthTransactions(currentFilter: String): List<Transaction> {
        val sdf = SimpleDateFormat("yyyyMM", Locale.getDefault())
        val currentMonth = sdf.format(Date())
        val allMonth = DataManager.transactions.filter {
            sdf.format(Date(it.timestamp)) == currentMonth
        }.sortedByDescending { it.timestamp }

        val filtered = if (currentFilter == "All") {
            allMonth
        } else {
            allMonth.filter { it.type == currentFilter }
        }
        
        transactionAdapter.updateData(filtered)
        updateEmptyState(filtered.isEmpty())
        return allMonth
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}
