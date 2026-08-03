package com.example.allinone.backup.providers

import com.example.allinone.backup.BackupProvider
import com.example.allinone.data.database.AppNoteDao
import com.example.allinone.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.*
import javax.inject.Inject

class NoteBackupProvider @Inject constructor(
    private val noteDao: AppNoteDao,
    private val noteRepository: NoteRepository,
    private val json: Json
) : BackupProvider {
    override val featureKey: String = "notes_module"

    override suspend fun exportData(): JsonElement {
        val notes = noteDao.getAllNotesSync()
        val settings = noteRepository.getNoteSettings().first()
        
        return buildJsonObject {
            put("entities", json.encodeToJsonElement(notes))
            put("settings", json.encodeToJsonElement(settings))
        }
    }

    override suspend fun importData(data: JsonElement) {
        val root = data as? JsonObject ?: return
        
        root["entities"]?.let {
            val entities: List<com.example.allinone.data.database.GlobalNoteEntity> = json.decodeFromJsonElement(it)
            noteDao.deleteAll()
            noteDao.insertAllNotes(entities)
        }
        
        root["settings"]?.let {
            val settings: com.example.allinone.domain.repository.NoteSettings = json.decodeFromJsonElement(it)
            noteRepository.updateSettings(settings)
        }
    }
}
