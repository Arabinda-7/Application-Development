package com.example.allinone.domain.usecase.note

import com.example.allinone.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AutoCleanupNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke() {
        val settings = noteRepository.getNoteSettings().first()
        if (settings.autoCleanupDays <= 0) return
        
        val threshold = System.currentTimeMillis() - (settings.autoCleanupDays * 24L * 60L * 60L * 1000L)
        val allNotes = noteRepository.getAllNotes().first()
        
        allNotes.forEach { note ->
            if (!note.isPinned && !note.isGlobalProject && note.updatedAt < threshold) {
                noteRepository.deleteNote(note)
            }
        }
    }
}
