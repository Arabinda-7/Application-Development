package com.example.allinone.domain.usecase.note

import com.example.allinone.domain.repository.NoteRepository
import com.example.allinone.domain.repository.NoteSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNoteSettingsUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    operator fun invoke(): Flow<NoteSettings> = repository.getNoteSettings()
}
