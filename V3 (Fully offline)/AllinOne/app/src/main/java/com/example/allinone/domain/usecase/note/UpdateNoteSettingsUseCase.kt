package com.example.allinone.domain.usecase.note

import com.example.allinone.domain.repository.NoteRepository
import com.example.allinone.domain.repository.NoteSettings
import javax.inject.Inject

class UpdateNoteSettingsUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(settings: NoteSettings) = repository.updateSettings(settings)
}
