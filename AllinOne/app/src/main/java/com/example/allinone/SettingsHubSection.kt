package com.example.allinone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsHubSection(
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val layoutProfileHub: View,
    private val onSectionSelected: (String) -> Unit,
    private val onShowAvatarOptions: () -> Unit
) {
    fun setup() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        updateMiniProfileUI()
        showHub()
    }

    fun showHub() {
        layoutProfileHub.visibility = View.VISIBLE
        val menuItems = listOf(
            SettingsHubItem("Features", isHeader = true),
            SettingsHubItem("AI Assistant", "Manage voice output and history cleanup", R.drawable.ic_launcher_foreground, "AI_ASSISTANT"),
            SettingsHubItem("Habit Tracker", "Manage your daily rituals and streaks", R.drawable.ic_habit_tracker, "HABITS"),
            SettingsHubItem("Workout Routine", "Configure exercises and rest timers", R.drawable.ic_workout_routine, "WORKOUTS"),
            SettingsHubItem("To-Do List", "Organize tasks and prioritization", R.drawable.ic_task, "TASKS"),
            SettingsHubItem("Notes", "Manage categories and writing templates", R.drawable.ic_notes, "NOTES"),
            SettingsHubItem("Finance", "Setup currency and budget goals", R.drawable.ic_finance, "FINANCE"),
            SettingsHubItem("Projects", "Advanced roadmap and project settings", R.drawable.ic_project, "PROJECTS"),
            
            SettingsHubItem("UI & Appearance", isHeader = true),
            SettingsHubItem("Appearance Settings", "Manage section icons and colors", R.drawable.ic_habit_tracker, "APPEARANCE"),
            SettingsHubItem("Others", "Additional app configurations", R.drawable.baseline_tune_24, "OTHERS"),
            
            SettingsHubItem("Security & Support", isHeader = true),
            SettingsHubItem("Lock & Security", "App PIN lock and privacy settings", R.drawable.baseline_settings_24, "SECURITY"),
            SettingsHubItem("Help & Guide", "Support and feature documentation", R.drawable.baseline_settings_24, "HELP"),
            
            SettingsHubItem("System", isHeader = true),
            SettingsHubItem("Notifications", "Daily reminders and summaries", R.drawable.baseline_settings_24, "NOTIFICATIONS")
        )

        recyclerView.adapter = SettingsHubAdapter(menuItems) { section ->
            when (section) {
                "HABITS" -> context.startActivity(Intent(context, HabitSettingsActivity::class.java))
                "WORKOUTS" -> context.startActivity(Intent(context, WorkoutSettingsActivity::class.java))
                "TASKS" -> context.startActivity(Intent(context, TaskSettingsActivity::class.java))
                "NOTES" -> context.startActivity(Intent(context, NoteSettingsActivity::class.java))
                "FINANCE" -> context.startActivity(Intent(context, FinanceSettingsActivity::class.java))
                "PROJECTS" -> context.startActivity(Intent(context, ProjectSettingsActivity::class.java))
                "NOTIFICATIONS" -> context.startActivity(Intent(context, NotificationSettingsActivity::class.java))
                else -> onSectionSelected(section)
            }
        }
    }

    fun updateMiniProfileUI() {
        layoutProfileHub.findViewById<TextView>(R.id.tv_mini_name).text = UIUtils.formatTitleCase(DataManager.userName)
        val ivProfile = layoutProfileHub.findViewById<ImageView>(R.id.iv_profile_pic)
        
        if (DataManager.userProfileImageUri != null) {
            ivProfile.setImageURI(Uri.parse(DataManager.userProfileImageUri))
        } else {
            UIUtils.safeSetImageResource(ivProfile, DataManager.userAvatarRes, R.drawable.ic_launcher_foreground)
        }
        
        layoutProfileHub.findViewById<View>(R.id.card_profile_entry).setOnClickListener {
            context.startActivity(Intent(context, ProfileActivity::class.java))
        }

        ivProfile.setOnClickListener { onShowAvatarOptions() }

        val streak = DataManager.getCurrentStreak()
        val projects = DataManager.projects.count { it.category == "Project" }
        layoutProfileHub.findViewById<TextView>(R.id.tv_mini_stat_streak).text = "$streak Day Streak"
        layoutProfileHub.findViewById<TextView>(R.id.tv_mini_stat_projects).text = "$projects Projects"
    }

    data class SettingsHubItem(
        val title: String,
        val description: String = "",
        val iconRes: Int = 0,
        val sectionKey: String = "",
        val isHeader: Boolean = false
    )

    class SettingsHubAdapter(private val items: List<SettingsHubItem>, private val onSelect: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val TYPE_ITEM = 0
        private val TYPE_HEADER = 1

        override fun getItemViewType(position: Int) = if (items[position].isHeader) TYPE_HEADER else TYPE_ITEM

        override fun onCreateViewHolder(p: ViewGroup, t: Int): RecyclerView.ViewHolder {
            return if (t == TYPE_HEADER) HeaderViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_settings_header, p, false))
            else ItemViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_settings_hub, p, false))
        }

        override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int) {
            val i = items[pos]
            if (h is ItemViewHolder) {
                h.title.text = i.title; h.description.text = i.description; h.icon.setImageResource(i.iconRes)
                h.itemView.setOnClickListener { onSelect(i.sectionKey) }
            } else if (h is HeaderViewHolder) { h.title.text = i.title.uppercase() }
        }

        override fun getItemCount() = items.size
        class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val title: TextView = v.findViewById(R.id.tv_item_title)
            val description: TextView = v.findViewById(R.id.tv_item_description)
            val icon: ImageView = v.findViewById(R.id.iv_item_icon)
        }
        class HeaderViewHolder(v: View) : RecyclerView.ViewHolder(v) { val title: TextView = v.findViewById(R.id.tv_header_title) }
    }
}
