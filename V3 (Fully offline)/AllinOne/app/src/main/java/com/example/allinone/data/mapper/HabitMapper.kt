package com.example.allinone.data.mapper

import com.example.allinone.data.local.entity.HabitEntity
import com.example.allinone.domain.model.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitMapper @Inject constructor(
    private val gson: Gson
) {

    fun toDomain(entity: HabitEntity): Habit {
        val type = object : TypeToken<List<String>>() {}.type
        val dates: List<String> = try {
            gson.fromJson(entity.completedDatesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return Habit(
            id = entity.id,
            title = entity.title,
            targetDaysPerWeek = entity.targetDaysPerWeek,
            completedDates = dates,
            currentStreak = entity.currentStreak,
            bestStreak = entity.bestStreak,
            section = entity.section,
            category = entity.category,
            iconRes = entity.iconRes,
            color = entity.color,
            createdAt = entity.createdAt
        )
    }

    fun toEntity(domain: Habit): HabitEntity {
        return HabitEntity(
            id = domain.id,
            title = domain.title,
            targetDaysPerWeek = domain.targetDaysPerWeek,
            completedDatesJson = gson.toJson(domain.completedDates),
            currentStreak = domain.currentStreak,
            bestStreak = domain.bestStreak,
            section = domain.section,
            category = domain.category,
            iconRes = domain.iconRes,
            color = domain.color,
            createdAt = domain.createdAt
        )
    }

    fun toDomainList(entities: List<HabitEntity>): List<Habit> = entities.map { toDomain(it) }
    fun toEntityList(domains: List<Habit>): List<HabitEntity> = domains.map { toEntity(it) }
}
