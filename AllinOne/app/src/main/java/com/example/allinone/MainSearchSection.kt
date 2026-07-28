package com.example.allinone

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainSearchSection(private val context: Context) {

    fun performSearch(query: String) {
        val results = mutableListOf<SearchResult>()
        
        // Tasks
        synchronized(DataManager.tasks) {
            DataManager.tasks.filter { it.name.contains(query, true) }.forEach { 
                results.add(SearchResult("TASK", it.name, it.category)) 
            }
        }
        
        // Notes
        synchronized(DataManager.notes) {
            DataManager.notes.filter { it.title.contains(query, true) || it.content.contains(query, true) }.forEach {
                results.add(SearchResult("NOTE", it.title, it.content.take(50)))
            }
        }

        // Projects
        synchronized(DataManager.projects) {
            DataManager.projects.filter { it.title.contains(query, true) || it.content.contains(query, true) }.forEach {
                results.add(SearchResult(if (it.category == "Project") "PROJECT" else "IDEA", it.title, it.content.take(50)))
            }
        }

        if (results.isEmpty()) {
            showNoResultsDialog(query)
        } else {
            showSearchResultsDialog(query, results)
        }
    }

    private fun showNoResultsDialog(query: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_no_search_results)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<TextView>(R.id.tv_no_results_message).text = 
            "No items found matching your search: \"$query\""
        
        dialog.findViewById<View>(R.id.btn_close_no_results).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showSearchResultsDialog(query: String, items: List<SearchResult>) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_search_results)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<TextView>(R.id.tv_search_query).text = "Results for \"$query\""
        
        dialog.findViewById<View>(R.id.btn_close_search).setOnClickListener {
            dialog.dismiss()
        }

        val rv = dialog.findViewById<RecyclerView>(R.id.rv_search_results)
        rv.layoutManager = LinearLayoutManager(context)
        rv.adapter = SearchResultsAdapter(items) { dialog.dismiss() }

        dialog.show()
    }

    data class SearchResult(val type: String, val title: String, val subtitle: String)

    class SearchResultsAdapter(
        private val items: List<SearchResult>,
        private val onSelect: () -> Unit
    ) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

        override fun getItemViewType(position: Int): Int {
            return when (items[position].type) {
                "TASK" -> R.layout.item_search_result_task
                "NOTE" -> R.layout.item_search_result_note
                "PROJECT" -> R.layout.item_search_result_project
                "IDEA" -> R.layout.item_search_result_idea
                else -> R.layout.item_search_result_task
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.title
            
            // Subtitle binding logic remains largely same, but layouts are different
            val subtitleView = holder.itemView.findViewById<TextView>(R.id.tv_result_subtitle)
            subtitleView?.text = "[${item.type}] ${item.subtitle}"
            
            holder.itemView.setOnClickListener { onSelect() }
        }

        override fun getItemCount() = items.size

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_result_title)
        }
    }
}
