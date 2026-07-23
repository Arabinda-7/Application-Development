package com.example.allinone

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    initialNotes: List<Note>,
    private val onProgressChanged: () -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var notes = initialNotes.toMutableList()
    private var isDeleteMode = false
    private val selectedNotes = mutableSetOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = UIUtils.formatTitleCase(note.title)
        holder.noteContent.text = note.content
        
        val context = holder.itemView.context
        val color = if (note.color != -1) note.color else ContextCompat.getColor(context, R.color.primary_blue)
        
        // Transparent Aesthetic with Dynamic Stroke
        holder.noteCard.setCardBackgroundColor(Color.TRANSPARENT)
        holder.noteTitle.setTextColor(color)
        holder.accentBar.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        
        val standardStrokeWidth = (1.5 * context.resources.displayMetrics.density).toInt()
        holder.noteCard.strokeColor = color
        holder.noteCard.strokeWidth = standardStrokeWidth

        // Set date
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.noteDate.text = sdf.format(Date(note.timestamp))
        
        // Selection UI
        if (isDeleteMode) {
            holder.noteCard.strokeWidth = if (selectedNotes.contains(note)) (4 * context.resources.displayMetrics.density).toInt() else 0
            holder.noteCard.strokeColor = Color.RED
        } else {
            // Ensure stroke stays visible when not in delete mode
            holder.noteCard.strokeWidth = standardStrokeWidth
            holder.noteCard.strokeColor = color
        }

        holder.itemView.setOnClickListener { 
            if (isDeleteMode) {
                if (selectedNotes.contains(note)) {
                    selectedNotes.remove(note)
                } else {
                    selectedNotes.add(note)
                }
                notifyItemChanged(position)
            } else {
                when (context) {
                    is NotesActivity -> {
                        val intent = Intent(context, AddNoteActivity::class.java).apply {
                            putExtra("NOTE_INDEX", DataManager.notes.indexOf(note))
                        }
                        context.startActivity(intent)
                    }
                    is ProjectActivity -> context.showEditIdeaDialog(note)
                }
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isDeleteMode) {
                if (context is ProjectActivity) {
                    context.showProjectMenu(it, note)
                } else {
                    showCustomMenu(it, note)
                }
            }
            true
        }
    }

    fun setDeleteMode(enabled: Boolean) {
        isDeleteMode = enabled
        selectedNotes.clear()
        notifyDataSetChanged()
    }

    fun deleteSelectedNotes(context: android.content.Context) {
        DataManager.notes.removeAll(selectedNotes)
        selectedNotes.clear()
        DataManager.saveData(context)
        onProgressChanged()
    }

    private fun showCustomMenu(anchor: View, note: Note) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)

        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        val hideUnhideView = menuView.findViewById<View>(R.id.menu_hide_unhide)
        val hideUnhideText = menuView.findViewById<TextView>(R.id.tv_hide_unhide_text)
        val hideUnhideIcon = menuView.findViewById<android.widget.ImageView>(R.id.iv_hide_unhide_icon)
        
        hideUnhideView.visibility = View.VISIBLE
        if (note.isHidden) {
            hideUnhideText.text = "UNHIDE"
            hideUnhideIcon.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            hideUnhideText.text = "HIDE"
            hideUnhideIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }

        hideUnhideView.setOnClickListener {
            note.isHidden = !note.isHidden
            popupWindow.dismiss()
            onProgressChanged() // Re-filter and refresh
            DataManager.saveData(context)
        }

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? NotesActivity)?.showEditNoteDialog(note)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            notes.remove(note)
            notifyDataSetChanged()
            onProgressChanged()
            DataManager.saveData(context)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 150, -100)
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        val oldNotes = notes.toList() // Create a copy for DiffUtil
        val diffCallback = NoteDiffCallback(oldNotes, newNotes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notes.clear()
        notes.addAll(newNotes)
        diffResult.dispatchUpdatesTo(this)
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteContent: TextView = itemView.findViewById(R.id.note_content)
        val noteDate: TextView = itemView.findViewById(R.id.note_date)
        val noteCard: MaterialCardView = itemView.findViewById(R.id.note_card)
        val accentBar: View = itemView.findViewById(R.id.accent_bar_note)
    }

    private class NoteDiffCallback(private val oldList: List<Note>, private val newList: List<Note>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].timestamp == newList[newItemPosition].timestamp
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
