package com.example.allinone.domain.usecase.project

import com.example.allinone.data.model.Note
import com.example.allinone.data.model.ProjectFeature
import com.example.allinone.data.model.ProjectHistory
import com.example.allinone.domain.repository.ProjectRepository
import javax.inject.Inject

class AddMilestoneUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Note, milestoneName: String, details: String = "") {
        val newMilestone = ProjectFeature(name = milestoneName, details = details)
        project.subFeatures.add(newMilestone)
        
        val history = ProjectHistory(
            action = "Milestone Added",
            description = "Added milestone: $milestoneName"
        )
        project.changeHistory.add(history)
        
        projectRepository.updateProject(project)
    }
}
