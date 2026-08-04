package com.example.allinone.data.mapper

import com.example.allinone.data.database.GlobalNoteEntity
import com.example.allinone.data.model.Note

object NoteMapper {
    fun toDomain(entity: GlobalNoteEntity): Note {
        return Note(
            title = entity.title,
            content = entity.content,
            color = entity.color,
            category = entity.category,
            isHidden = entity.isHidden,
            timestamp = entity.timestamp,
            updatedAt = entity.updatedAt,
            status = entity.status,
            progress = entity.progress,
            priority = entity.priority,
            isPinned = entity.isPinned,
            deadline = entity.deadline,
            isArchived = entity.isArchived,
            isDualExist = entity.isDualExist,
            isGlobalProject = entity.isGlobalProject,
            journalEntries = entity.journalEntries.toMutableList(),
            ideaGoals = entity.ideaGoals.toMutableList(),
            subFeatures = entity.subFeatures.toMutableList(),
            changeHistory = entity.changeHistory.toMutableList()
        )
    }

    fun toEntity(domain: Note): GlobalNoteEntity {
        return GlobalNoteEntity(
            timestamp = domain.timestamp,
            title = domain.title,
            content = domain.content,
            color = domain.color,
            category = domain.category,
            isHidden = domain.isHidden,
            updatedAt = domain.updatedAt,
            status = domain.status,
            progress = domain.progress,
            priority = domain.priority,
            isPinned = domain.isPinned,
            deadline = domain.deadline,
            isArchived = domain.isArchived,
            isDualExist = domain.isDualExist,
            isGlobalProject = domain.isGlobalProject,
            journalEntries = domain.journalEntries.toList(),
            ideaGoals = domain.ideaGoals.toList(),
            subFeatures = domain.subFeatures.toList(),
            changeHistory = domain.changeHistory.toList()
        )
    }
}
