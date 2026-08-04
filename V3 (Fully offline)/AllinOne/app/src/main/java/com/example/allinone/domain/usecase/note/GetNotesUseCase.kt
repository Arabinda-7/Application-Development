package com.example.allinone.domain.usecase.note

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(includeProjects: Boolean = false): Flow<List<Note>> {
        return noteRepository.getAllNotes().map { notes ->
            if (includeProjects) notes
            else notes.filter { !it.isGlobalProject }
        }
    }
}
