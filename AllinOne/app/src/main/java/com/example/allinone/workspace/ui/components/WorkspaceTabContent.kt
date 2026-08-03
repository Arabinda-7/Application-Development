package com.example.allinone.workspace.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.workspace.data.IdeaEntity
import com.example.allinone.workspace.data.ProjectEntity
import com.example.allinone.workspace.ui.WorkspaceAction
import com.example.allinone.workspace.ui.WorkspaceTab
import com.example.allinone.workspace.ui.WorkspaceUIState
import com.example.allinone.workspace.ui.WorkspaceViewModel
import com.example.allinone.workspace.ui.sections.ActivityLogSection
import com.example.allinone.workspace.ui.sections.BugViewSection
import com.example.allinone.workspace.ui.sections.FeatureViewSection
import com.example.allinone.workspace.ui.sections.GoalViewSection
import com.example.allinone.workspace.ui.sections.IdeaViewSection
import com.example.allinone.workspace.ui.sections.NoteViewSection
import com.example.allinone.workspace.ui.sections.ResourceViewSection
import com.example.allinone.workspace.ui.sections.TaskViewSection
import com.example.allinone.workspace.ui.sections.WorkspaceDashboard

/**
 * WorkspaceTabContent: Renders current tab title and tab view content animated transitions.
 */
@Composable
fun WorkspaceTabContent(
    currentTab: WorkspaceTab,
    uiState: WorkspaceUIState,
    viewModel: WorkspaceViewModel,
    onShowStats: (ProjectEntity) -> Unit,
    isStatsShowing: Boolean,
    onEditEntity: (Any, WorkspaceAction) -> Unit,
    onDeleteEntityRequest: (Any) -> Unit,
    onGraduateIdea: (IdeaEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = currentTab.title.uppercase(),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                val d = 250
                fadeIn(tween(d)) togetherWith fadeOut(tween(d))
            },
            label = "TabTransition",
            modifier = Modifier.weight(1f)
        ) { tab ->
            Box(modifier = Modifier.fillMaxSize()) {
                when (tab) {
                    WorkspaceTab.Dashboard -> WorkspaceDashboard(
                        state = uiState,
                        viewModel = viewModel,
                        onShowStats = onShowStats,
                        isStatsShowing = isStatsShowing
                    )
                    WorkspaceTab.Goals -> GoalViewSection(
                        goals = uiState.goals,
                        onViewGoal = { onEditEntity(it, WorkspaceAction.ViewGoal) },
                        onEditGoal = { onEditEntity(it, WorkspaceAction.EditGoal) },
                        onDeleteGoal = { viewModel.deleteGoal(it) }
                    )
                    WorkspaceTab.Notes -> NoteViewSection(
                        notes = uiState.notes,
                        onViewNote = { onEditEntity(it, WorkspaceAction.ViewNote) },
                        onEditNote = { onEditEntity(it, WorkspaceAction.EditNote) },
                        onDeleteNote = onDeleteEntityRequest
                    )
                    WorkspaceTab.Tasks -> TaskViewSection(
                        tasks = uiState.tasks,
                        onUpdateTask = { viewModel.updateTask(it) },
                        onViewTask = { onEditEntity(it, WorkspaceAction.ViewTask) },
                        onEditTask = { onEditEntity(it, WorkspaceAction.EditTask) },
                        onDeleteTask = { viewModel.deleteTask(it) }
                    )
                    WorkspaceTab.Features -> FeatureViewSection(
                        features = uiState.features,
                        viewModel = viewModel,
                        tasks = uiState.tasks,
                        onViewFeature = { onEditEntity(it, WorkspaceAction.ViewFeature) },
                        onEditFeature = { onEditEntity(it, WorkspaceAction.EditFeature) },
                        onDeleteFeature = { viewModel.deleteFeature(it) }
                    )
                    WorkspaceTab.Bugs -> BugViewSection(
                        bugs = uiState.bugs,
                        viewModel = viewModel,
                        onViewBug = { onEditEntity(it, WorkspaceAction.ViewBug) },
                        onEditBug = { onEditEntity(it, WorkspaceAction.EditBug) },
                        onDeleteBug = { viewModel.deleteBug(it) }
                    )
                    WorkspaceTab.Ideas -> IdeaViewSection(
                        ideas = uiState.ideas,
                        onConvert = { onGraduateIdea(it) },
                        onViewIdea = { onEditEntity(it, WorkspaceAction.ViewIdea) },
                        onEditIdea = { onEditEntity(it, WorkspaceAction.EditIdea) },
                        onDeleteIdea = { viewModel.deleteIdea(it) }
                    )
                    WorkspaceTab.Resources -> ResourceViewSection(
                        resources = uiState.resources,
                        onViewResource = { onEditEntity(it, WorkspaceAction.ViewResource) },
                        onEditResource = { onEditEntity(it, WorkspaceAction.EditResource) },
                        onDeleteResource = { viewModel.deleteResource(it) }
                    )
                    WorkspaceTab.ActivityLog -> ActivityLogSection(uiState.logs)
                }
            }
        }
    }
}
