package com.example.allinone.domain.usecase.note

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import javax.inject.Inject

class UpdateNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
        noteRepository.updateNote(updatedNote)
    }
}
