package com.example.allinone.data

import com.example.allinone.data.model.ProjectFeature
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WorkspaceDataManager: Manages active project editing states, subfeature tracking,
 * and unique workspace naming operations.
 */
@Singleton
class WorkspaceDataManager @Inject constructor() {

    val currentEditingIdeaSubFeatures: MutableList<ProjectFeature> = Collections.synchronizedList(mutableListOf())

    fun getUniqueFeatureName(baseName: String, existingList: List<ProjectFeature>): String {
        var name = baseName
        var counter = 1
        val existingNames = existingList.map { it.name.lowercase(Locale.getDefault()) }.toSet()
        while (existingNames.contains(name.lowercase(Locale.getDefault()))) {
            name = "$baseName $counter"
            counter++
        }
        return name
    }
}
