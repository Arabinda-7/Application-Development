package com.example.allinone.workspace.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.allinone.DataManager
import com.example.allinone.data.model.Note
import com.example.allinone.workspace.data.*
import com.example.allinone.workspace.ui.DeleteConfirmationDialog
import com.example.allinone.workspace.ui.ImportSelectionDialog
import com.example.allinone.workspace.ui.TransferCopyChoiceDialog
import com.example.allinone.workspace.ui.WorkspaceViewModel

/**
 * WorkspaceDialogs: Integrates import selection, transfer choice, and entity deletion dialogs.
 */
@Composable
fun WorkspaceDialogs(
    showImportDialog: Boolean,
    showTransferChoiceDialog: Boolean,
    pendingImportNote: Note?,
    entityToDelete: Any?,
    context: Context,
    viewModel: WorkspaceViewModel,
    onDismissImport: () -> Unit,
    onShowTransferChoice: (Note) -> Unit,
    onDismissTransferChoice: () -> Unit,
    onDismissDelete: () -> Unit
) {
    if (showImportDialog) {
        ImportSelectionDialog(
            onDismiss = onDismissImport,
            onImport = { note ->
                onShowTransferChoice(note)
            }
        )
    }

    if (showTransferChoiceDialog && pendingImportNote != null) {
        TransferCopyChoiceDialog(
            onDismiss = onDismissTransferChoice,
            onChoice = { isTransfer ->
                pendingImportNote.let { note ->
                    viewModel.importNote(note, isTransfer)
                    if (isTransfer) DataManager.saveData(context)
                }
                onDismissTransferChoice()
            }
        )
    }

    if (entityToDelete != null) {
        DeleteConfirmationDialog(
            entity = entityToDelete,
            onDismiss = onDismissDelete,
            onConfirm = {
                when (entityToDelete) {
                    is WorkspaceNoteEntity -> viewModel.deleteNote(entityToDelete)
                    is WorkspaceTaskEntity -> viewModel.deleteTask(entityToDelete)
                    is GoalEntity -> viewModel.deleteGoal(entityToDelete)
                    is FeatureEntity -> viewModel.deleteFeature(entityToDelete)
                    is BugEntity -> viewModel.deleteBug(entityToDelete)
                    is IdeaEntity -> viewModel.deleteIdea(entityToDelete)
                    is ResourceEntity -> viewModel.deleteResource(entityToDelete)
                }
                onDismissDelete()
            }
        )
    }
}
