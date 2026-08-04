package com.example.allinone.domain.usecase.note

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        if (note.title.isBlank()) {
            note.title = "Untitled Note"
        }
        repository.insertNote(note)
    }
}
