package com.example.allinone

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PersonalLedgerHubActivity : AppCompatActivity() {

    private lateinit var adapter: PersonalLedgerAdapter
    private val ledgers = DataManager.personalLedgers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_ledger_hub)

        findViewById<TextView>(R.id.tv_title).text = "Personal Ledgers"

        val rv = findViewById<RecyclerView>(R.id.rv_people_list)
        rv.layoutManager = LinearLayoutManager(this)
        
        adapter = PersonalLedgerAdapter(ledgers) { ledger ->
            val intent = Intent(this, PersonalLedgerBookActivity::class.java)
            intent.putExtra("ledgerId", ledger.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_add_person_full).setOnClickListener {
            startActivity(Intent(this, AddPersonActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    inner class PersonalLedgerAdapter(
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
            
            val activeEntries = ledger.entries.filter { !it.isSettled }
            val owe = activeEntries.filter { it.type == "Borrowed" }.sumOf { it.amount - it.paidAmount }
            val owed = activeEntries.filter { it.type == "Lent" }.sumOf { it.amount - it.paidAmount }
            val net = owed - owe
            
            val currency = DataManager.financeCurrency
            holder.tvBalance.text = if (net >= 0) "Owes me ${currency}${net.toInt()}" else "I owe ${currency}${Math.abs(net).toInt()}"
            holder.tvBalance.setTextColor(if (net >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#FF5252"))

            holder.itemView.setOnClickListener { onClick(ledger) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tv_person_name_card)
            val tvBalance: TextView = v.findViewById(R.id.tv_person_balance_summary)
        }
    }
}
