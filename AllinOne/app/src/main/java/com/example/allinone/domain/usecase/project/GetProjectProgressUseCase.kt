package com.example.allinone.domain.usecase.project

import com.example.allinone.data.model.Note
import javax.inject.Inject

class GetProjectProgressUseCase @Inject constructor() {
    operator fun invoke(project: Note): Int {
        if (project.subFeatures.isEmpty()) return project.progress
        
        val total = project.subFeatures.size
        val completed = project.subFeatures.count { it.isCompleted }
        
        return (completed * 100) / total
    }
}
