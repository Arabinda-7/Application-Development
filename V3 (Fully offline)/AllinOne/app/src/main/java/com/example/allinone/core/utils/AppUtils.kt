package com.example.allinone.core.utils

import android.content.Context
import com.example.allinone.data.model.ProjectFeature
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {
    
    fun getTrackingDateString(timestamp: Long = System.currentTimeMillis()): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(timestamp))
    }

    fun getTrackingCalendar(timestamp: Long = System.currentTimeMillis()): Calendar {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal
    }

    fun getUniqueFeatureName(baseName: String, existing: List<ProjectFeature>): String {
        var name = baseName
        var count = 2
        while (existing.any { it.name.equals(name, ignoreCase = true) }) {
            name = "$baseName $count"
            count++
        }
        return name
    }

    fun getResourceId(context: Context, name: String, fallbackId: Int): Int {
        return try {
            val id = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (id != 0 && UIUtils.isDrawableResource(context, id)) id else fallbackId
        } catch (e: Exception) {
            fallbackId
        }
    }

    fun getResourceName(context: Context, id: Int): String {
        return try {
            context.resources.getResourceEntryName(id)
        } catch (e: Exception) {
            "ic_launcher_foreground"
        }
    }

    fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return start to end
    }
}
