package com.example.allinone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class NoteAdapter(
    private var notes: MutableList<Note>,
    private val onProgressChanged: () -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_list_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note.title
        holder.noteContent.text = note.content
        
        val context = holder.itemView.context
        val cardColor = if (note.color != -1) {
            note.color
        } else {
            ContextCompat.getColor(context, R.color.card_blue)
        }
        holder.noteCard.setCardBackgroundColor(cardColor)
        
        holder.editButton.setOnClickListener { showCustomMenu(it, position) }
        holder.itemView.setOnClickListener { (context as? NotesActivity)?.showEditNoteDialog(note) }
    }

    private fun showCustomMenu(anchor: View, position: Int) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.layout_custom_menu, null)
        val note = notes[position]

        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 10f

        // Note doesn't have "Day Off" or "Undo", so hide them
        menuView.findViewById<View>(R.id.menu_take_day_off).visibility = View.GONE
        menuView.findViewById<View>(R.id.menu_undo).visibility = View.GONE

        menuView.findViewById<View>(R.id.menu_edit).setOnClickListener {
            popupWindow.dismiss()
            (context as? NotesActivity)?.showEditNoteDialog(note)
        }

        menuView.findViewById<View>(R.id.menu_delete).setOnClickListener {
            notes.remove(note)
            notifyDataSetChanged()
            onProgressChanged()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, -150, 0)
    }

    override fun getItemCount() = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes.toMutableList()
        notifyDataSetChanged()
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteContent: TextView = itemView.findViewById(R.id.note_content)
        val noteCard: MaterialCardView = itemView.findViewById(R.id.note_card)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_note_button)
    }
}
