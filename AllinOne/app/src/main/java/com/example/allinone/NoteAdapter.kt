package com.example.allinone

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onProgressChanged: () -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var isDeleteMode = false
    private val selectedNotes = mutableSetOf<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note.title
        holder.noteContent.text = note.content
        
        val context = holder.itemView.context
        val color = if (note.color != -1) note.color else ContextCompat.getColor(context, R.color.primary_blue)
        
        // Match the premium dark aesthetic from the pic
        holder.noteCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.chip_background))
        holder.noteTitle.setTextColor(color)

        // Set date
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        holder.noteDate.text = sdf.format(Date(note.timestamp))
        
        // Selection UI
        if (isDeleteMode) {
            holder.noteCard.strokeWidth = if (selectedNotes.contains(note)) 4 else 0
            holder.noteCard.strokeColor = Color.RED
        } else {
            holder.noteCard.strokeWidth = 0
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
                (context as? NotesActivity)?.showEditNoteDialog(note) 
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isDeleteMode) showCustomMenu(it, note)
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
        notes = newNotes.toMutableList()
        notifyDataSetChanged()
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteContent: TextView = itemView.findViewById(R.id.note_content)
        val noteDate: TextView = itemView.findViewById(R.id.note_date)
        val noteCard: MaterialCardView = itemView.findViewById(R.id.note_card)
    }
}
