package com.example.allinone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class ProjectAdapter(
    private val allProjects: MutableList<Project>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    private var displayProjects = allProjects.toMutableList()
    private var currentFilter = "ALL"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.project_list_item, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = displayProjects[position]
        holder.projectName.text = project.name
        holder.projectStatus.text = project.status

        val context = holder.itemView.context
        val cardColor = if (project.color != -1) project.color else ContextCompat.getColor(context, R.color.card_blue)
        holder.projectCard.setCardBackgroundColor(cardColor)

        if (project.iconResId != -1) {
            holder.projectIcon.setImageResource(project.iconResId)
        }

        holder.editButton.setOnClickListener { showCustomMenu(it, project) }
        holder.itemView.setOnClickListener { (context as? ProjectActivity)?.showAddProjectDialog(project) }
    }

    private fun showCustomMenu(anchor: View, project: Project) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)

        val popupWindow = PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? ProjectActivity)?.showAddProjectDialog(project)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            allProjects.remove(project)
            applyFilter()
            onDataChanged()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    fun filter(filterType: String) {
        currentFilter = filterType
        applyFilter()
    }

    private fun applyFilter() {
        displayProjects = if (currentFilter == "ALL") {
            allProjects.toMutableList()
        } else {
            allProjects.filter { it.status.uppercase() == currentFilter }.toMutableList()
        }
        displayProjects.sortByDescending { it.timestamp }
        notifyDataSetChanged()
    }

    override fun getItemCount() = displayProjects.size

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.project_name)
        val projectStatus: TextView = itemView.findViewById(R.id.project_status)
        val projectCard: MaterialCardView = itemView.findViewById(R.id.project_card)
        val projectIcon: ImageView = itemView.findViewById(R.id.project_icon)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_project_button)
    }
}
