package com.example.allinone.workspace.ui

import androidx.compose.runtime.Composable
import com.example.allinone.workspace.data.*
import com.example.allinone.workspace.ui.sections.*

@Composable
fun WorkspaceDetailRouter(
    action: WorkspaceAction,
    viewModel: WorkspaceViewModel,
    selectedProjectId: String,
    editingEntity: Any? = null,
    onBack: () -> Unit,
    onEditEntity: (Any, WorkspaceAction) -> Unit,
    onDeleteEntity: (Any) -> Unit
) {
    when (action) {
        WorkspaceAction.AddProject, WorkspaceAction.EditProject -> {
            ProjectAddEditSection(
                project = editingEntity as? ProjectEntity,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        
        WorkspaceAction.AddGoal, WorkspaceAction.EditGoal -> {
            GoalAddEditSection(
                goal = editingEntity as? GoalEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewGoal -> {
            (editingEntity as? GoalEntity)?.let {
                GoalDetailSection(goal = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditGoal) })
            }
        }

        WorkspaceAction.AddTask, WorkspaceAction.EditTask -> {
            TaskAddEditSection(
                task = editingEntity as? TaskEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewTask -> {
            (editingEntity as? TaskEntity)?.let {
                TaskDetailSection(
                    task = it,
                    onBack = onBack,
                    onEdit = { onEditEntity(it, WorkspaceAction.EditTask) },
                    onUpdateTask = { viewModel.updateTask(it) }
                )
            }
        }

        WorkspaceAction.AddFeature, WorkspaceAction.EditFeature -> {
            FeatureAddEditSection(
                feature = editingEntity as? FeatureEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewFeature -> {
            (editingEntity as? FeatureEntity)?.let {
                FeatureDetailSection(feature = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditFeature) })
            }
        }

        WorkspaceAction.AddBug, WorkspaceAction.EditBug -> {
            BugAddEditSection(
                bug = editingEntity as? BugEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewBug -> {
            (editingEntity as? BugEntity)?.let {
                BugDetailSection(bug = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditBug) })
            }
        }

        WorkspaceAction.AddIdea, WorkspaceAction.EditIdea -> {
            IdeaAddEditSection(
                idea = editingEntity as? IdeaEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewIdea -> {
            (editingEntity as? IdeaEntity)?.let {
                IdeaDetailSection(idea = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditIdea) })
            }
        }

        WorkspaceAction.AddNote, WorkspaceAction.EditNote -> {
            NoteAddEditSection(
                note = editingEntity as? NoteEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack,
                onDelete = { (editingEntity as? NoteEntity)?.let { onDeleteEntity(it) } }
            )
        }
        WorkspaceAction.ViewNote -> {
            (editingEntity as? NoteEntity)?.let {
                NoteDetailSection(
                    note = it, 
                    onBack = onBack, 
                    onEdit = { onEditEntity(it, WorkspaceAction.EditNote) },
                    onDelete = { onDeleteEntity(it) }
                )
            }
        }

        WorkspaceAction.AddResource, WorkspaceAction.EditResource -> {
            ResourceAddEditSection(
                resource = editingEntity as? ResourceEntity,
                projectId = selectedProjectId,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        WorkspaceAction.ViewResource -> {
            (editingEntity as? ResourceEntity)?.let {
                ResourceDetailSection(resource = it, onBack = onBack, onEdit = { onEditEntity(it, WorkspaceAction.EditResource) })
            }
        }
        
        else -> {}
    }
}
