package com.example.allinone.domain.usecase.note

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(query: String): Flow<List<Note>> {
        return noteRepository.getAllNotes().map { notes ->
            if (query.isBlank()) {
                notes.filter { !it.isGlobalProject }
            } else {
                notes.filter { note ->
                    !note.isGlobalProject && (
                        note.title.contains(query, ignoreCase = true) || 
                        note.content.contains(query, ignoreCase = true)
                    )
                }
            }
        }
    }
}
