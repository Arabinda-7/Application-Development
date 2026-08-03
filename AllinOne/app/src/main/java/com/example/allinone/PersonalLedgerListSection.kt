package com.example.allinone

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.allinone.data.model.*

class PersonalLedgerListSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val onLedgerClicked: (PersonalLedger) -> Unit
) {
    private val adapter: PersonalLedgerAdapter

    init {
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = PersonalLedgerAdapter(DataManager.personalLedgers, onLedgerClicked)
        recyclerView.adapter = adapter
    }

    fun update() {
        adapter.notifyDataSetChanged()
    }

    class PersonalLedgerAdapter(
        private val items: List<PersonalLedger>,
        private val onClick: (PersonalLedger) -> Unit
    ) : RecyclerView.Adapter<PersonalLedgerAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_person_card, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ledger = items[position]
            holder.tvName.text = ledger.personName
            
            var owe = 0.0
            var owed = 0.0
            for (entry in ledger.entries) {
                if (!entry.isSettled) {
                    val remaining = entry.amount - entry.paidAmount
                    if (entry.type == "Borrowed") owe += remaining
                    else if (entry.type == "Lent") owed += remaining
                }
            }
            val net = owed - owe
            val currency = DataManager.financeCurrency
            val netAbs = if (net < 0.0) -net else net
            holder.tvBalance.text = if (net >= 0.0) "Owes me ${currency}${net.toInt()}" else "I owe ${currency}${netAbs.toInt()}"
            holder.tvBalance.setTextColor(if (net >= 0.0) Color.parseColor("#4CAF50") else Color.parseColor("#FF5252"))

            holder.itemView.setOnClickListener { onClick(ledger) }
        }

        override fun getItemCount() = items.size

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tv_person_name_card)
            val tvBalance: TextView = v.findViewById(R.id.tv_person_balance_summary)
        }
    }
}
