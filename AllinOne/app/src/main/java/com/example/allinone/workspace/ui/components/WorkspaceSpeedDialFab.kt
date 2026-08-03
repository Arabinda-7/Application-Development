package com.example.allinone.workspace.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.allinone.AppStyle
import com.example.allinone.workspace.ui.WorkspaceAction
import com.example.allinone.workspace.ui.WorkspaceSpeedDial
import com.example.allinone.workspace.ui.WorkspaceTab

/**
 * WorkspaceSpeedDialFab: Renders floating action buttons for workspace sections.
 */
@Composable
fun WorkspaceSpeedDialFab(
    currentTab: WorkspaceTab,
    style: AppStyle,
    onAction: (WorkspaceAction) -> Unit,
    onShowImportDialog: () -> Unit
) {
    if (currentTab == WorkspaceTab.Dashboard) {
        WorkspaceSpeedDial(onAction = { if (it == WorkspaceAction.ImportProject) onShowImportDialog() else onAction(it) })
    } else {
        val action = when (currentTab) {
            WorkspaceTab.Goals -> WorkspaceAction.AddGoal
            WorkspaceTab.Tasks -> WorkspaceAction.AddTask
            WorkspaceTab.Notes -> WorkspaceAction.AddNote
            WorkspaceTab.Features -> WorkspaceAction.AddFeature
            WorkspaceTab.Bugs -> WorkspaceAction.AddBug
            WorkspaceTab.Ideas -> WorkspaceAction.AddIdea
            WorkspaceTab.Resources -> WorkspaceAction.AddResource
            else -> null
        }
        action?.let { a ->
            FloatingActionButton(
                onClick = { onAction(a) },
                containerColor = style.accentColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}
