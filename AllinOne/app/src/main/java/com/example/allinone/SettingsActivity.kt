package com.example.allinone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Setup Header (Title)
        findViewById<TextView>(R.id.date_textview).text = "SETTINGS"
        findViewById<View>(R.id.btn_main_menu).visibility = View.GONE
        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        val settingsList = findViewById<RecyclerView>(R.id.settings_list)
        settingsList.layoutManager = LinearLayoutManager(this)

        val menuItems = listOf(
            "Habit Tracker" to HabitTrackerActivity::class.java,
            "Workout Routine" to WorkoutRoutineActivity::class.java,
            "To-Do List" to ToDoListActivity::class.java,
            "Notes" to NotesActivity::class.java,
            "Finance" to FinanceActivity::class.java
        )

        settingsList.adapter = SettingsAdapter(menuItems)
    }

    class SettingsAdapter(private val items: List<Pair<String, Class<*>>>) :
        RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.settings_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.title.text = item.first
            holder.itemView.setOnClickListener {
                val intent = Intent(it.context, item.second)
                it.context.startActivity(intent)
            }
        }

        override fun getItemCount() = items.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tv_settings_item_title)
        }
    }
}
