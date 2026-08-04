package com.example.allinone.domain.usecase.note

import com.example.allinone.data.model.Note
import com.example.allinone.domain.repository.NoteRepository
import javax.inject.Inject

class SyncNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(notes: List<Note>) = repository.syncAll(notes)
}
