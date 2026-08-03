package com.example.allinone.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.allinone.data.database.AppNoteDao
import com.example.allinone.data.database.GlobalNoteEntity
import com.example.allinone.domain.repository.NoteSettings
import com.example.allinone.security.SecurityManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteDao: AppNoteDao,
    private val gson: Gson
) {
    private val prefs = SecurityManager.getEncryptedPrefs(context)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: Flow<NoteSettings> = _settings.asStateFlow()

    fun getAllNotes(): Flow<List<GlobalNoteEntity>> = noteDao.getAllNotes()

    suspend fun insertNote(note: GlobalNoteEntity) = noteDao.insertNote(note)
    
    suspend fun insertAllNotes(notes: List<GlobalNoteEntity>) = noteDao.insertAllNotes(notes)

    suspend fun deleteNote(note: GlobalNoteEntity) = noteDao.deleteNote(note)
    
    suspend fun deleteOthers(ids: List<Long>) = noteDao.deleteOthers(ids)

    fun updateSettings(newSettings: NoteSettings) {
        prefs.edit().apply {
            putInt("note_auto_cleanup", newSettings.autoCleanupDays)
            putString("note_default_cat", newSettings.defaultCategory)
            putBoolean("note_show_hidden", newSettings.showHidden)
            putBoolean("note_voice_input_enabled", newSettings.voiceInputEnabled)
            putString("note_visible_sections", gson.toJson(newSettings.visibleSections))
            putString("note_templates", gson.toJson(newSettings.noteTemplates))
            putInt("global_note_color", newSettings.globalNoteColor)
            putInt("note_add_theme_color", newSettings.noteAddThemeColor)
            putInt("global_note_icon", newSettings.globalNoteIcon)
            apply()
        }
        _settings.value = newSettings
    }

    private fun loadSettings(): NoteSettings {
        val visibleSectionsJson = prefs.getString("note_visible_sections", "[\"Notes\"]")
        val visibleSections: List<String> = try {
            gson.fromJson(visibleSectionsJson, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) { listOf("Notes") }

        val templatesJson = prefs.getString("note_templates", "{}")
        val templates: Map<String, String> = try {
            gson.fromJson(templatesJson, object : TypeToken<Map<String, String>>() {}.type)
        } catch (e: Exception) { emptyMap() }

        return NoteSettings(
            autoCleanupDays = prefs.getInt("note_auto_cleanup", 0),
            defaultCategory = prefs.getString("note_default_cat", "Notes") ?: "Notes",
            showHidden = prefs.getBoolean("note_show_hidden", false),
            voiceInputEnabled = prefs.getBoolean("note_voice_input_enabled", true),
            visibleSections = if (visibleSections.isEmpty()) listOf("Notes") else visibleSections,
            noteTemplates = if (templates.isEmpty()) mapOf(
                "Daily" to "1. Today I'm grateful for: \n2. Top goal for today: \n3. How I feel: ",
                "Questions" to "Question: \n\nContext: \n\nGoal: ",
                "Stories" to "Theme: \nCharacters: \n\nPlot: "
            ) else templates,
            globalNoteColor = prefs.getInt("global_note_color", -1),
            noteAddThemeColor = prefs.getInt("note_add_theme_color", -1),
            globalNoteIcon = prefs.getInt("global_note_icon", -1)
        )
    }
}
