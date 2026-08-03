package com.example.allinone.data.mapper

import com.example.allinone.data.database.WorkoutEntity
import com.example.allinone.data.model.Workout

object WorkoutMapper {
    fun toDomain(entity: WorkoutEntity): Workout {
        return Workout(
            name = entity.name,
            isCompleted = entity.isCompleted,
            trackingMode = entity.trackingMode,
            target = entity.target,
            repsPerSet = entity.repsPerSet,
            progress = entity.progress,
            frequency = entity.frequency,
            isDayOff = entity.isDayOff,
            color = entity.color,
            iconResId = entity.iconResId,
            muscleGroups = entity.muscleGroups,
            repeatType = entity.repeatType,
            repeatDays = entity.repeatDays,
            repeatCount = entity.repeatCount,
            timestamp = entity.timestamp,
            completedDates = entity.completedDates.toMutableList(),
            dailyProgress = entity.dailyProgress.toMutableMap()
        )
    }

    fun toEntity(domain: Workout): WorkoutEntity {
        return WorkoutEntity(
            timestamp = domain.timestamp,
            name = domain.name,
            isCompleted = domain.isCompleted,
            trackingMode = domain.trackingMode,
            target = domain.target,
            repsPerSet = domain.repsPerSet,
            progress = domain.progress,
            frequency = domain.frequency,
            isDayOff = domain.isDayOff,
            color = domain.color,
            iconResId = domain.iconResId,
            muscleGroups = domain.muscleGroups,
            repeatType = domain.repeatType,
            repeatDays = domain.repeatDays,
            repeatCount = domain.repeatCount,
            completedDates = domain.completedDates.toList(),
            dailyProgress = domain.dailyProgress.toMap()
        )
    }
}
