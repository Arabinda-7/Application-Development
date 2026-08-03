package com.example.allinone.workspace.ui

import com.example.allinone.data.model.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.workspace.data.*

@Composable
fun DeleteConfirmationDialog(
    entity: Any,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (entity) {
        is WorkspaceNoteEntity -> "Delete Note"
        is WorkspaceTaskEntity -> "Delete Task"
        is GoalEntity -> "Delete Goal"
        is FeatureEntity -> "Delete Feature"
        is BugEntity -> "Delete Bug"
        is IdeaEntity -> "Delete Idea"
        is ResourceEntity -> "Delete Resource"
        else -> "Delete Item"
    }
    
    val name = when (entity) {
        is WorkspaceNoteEntity -> entity.title
        is WorkspaceTaskEntity -> entity.title
        is GoalEntity -> entity.title
        is FeatureEntity -> entity.title
        is BugEntity -> entity.title
        is IdeaEntity -> entity.title
        is ResourceEntity -> entity.title
        else -> "this item"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Black) },
        text = { Text("Are you sure you want to delete '$name'? This action cannot be undone.", color = Color.White.copy(alpha = 0.7f)) },
        containerColor = Color(0xFF1A1A1A),
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("DELETE", color = Color.Red, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}

@Composable
fun ImportSelectionDialog(onDismiss: () -> Unit, onImport: (Note) -> Unit) {
    val notes = remember { com.example.allinone.DataManager.projects.filter { it.category == "Project" || it.category == "ProjectIdea" || it.subFeatures.isNotEmpty() } }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Import Project or Idea", color = Color.White) }, containerColor = Color(0xFF1A1A1A), text = {
        if (notes.isEmpty()) { Text("No existing projects or ideas found to import.", color = Color.White.copy(alpha = 0.6f)) }
        else { 
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) { 
                items(notes) { note -> 
                    Surface(onClick = { onImport(note) }, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) { 
                        Row(modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) { 
                            Icon(imageVector = if (note.category == "Project") Icons.Default.Folder else Icons.Default.Lightbulb, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)); 
                            Spacer(modifier = Modifier.width(16.dp)); 
                            Column { 
                                Text(note.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis); 
                                Text("${note.subFeatures.size} Milestones | ${note.category}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp) 
                            } 
                        } 
                    } 
                } 
            } 
        }
    }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("CLOSE") } })
}

@Composable
fun TransferCopyChoiceDialog(onDismiss: () -> Unit, onChoice: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Workspace Import", color = Color.White, fontWeight = FontWeight.Black) },
        containerColor = Color(0xFF1A1A1A),
        text = {
            Column {
                Text(
                    "How would you like to bring this project into your workspace?",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    onClick = { onChoice(true) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoveUp, contentDescription = null, tint = Color(0xFFFFB800))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Transfer", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Moves the project to Workspace and removes it from the main Projects tab. (No sync link)",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    onClick = { onChoice(false) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1A73E8))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Copy", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "Creates a duplicate in Workspace. Original project stays in the main tab. (No sync link)",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}
