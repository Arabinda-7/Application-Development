@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class
)

package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.NoteEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel

@Composable
fun NoteViewSection(
    notes: List<NoteEntity>,
    onViewNote: (NoteEntity) -> Unit,
    onEditNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    val style = LocalAppStyle.current
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        items(notes, key = { it.id }) { note ->
            var showMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.animateItem()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .combinedClickable(
                            onClick = { onViewNote(note) },
                            onLongClick = { showMenu = true }
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.5.dp, style.accentColor)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Row(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Vertical Accent Bar
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight().clip(CircleShape).background(style.accentColor.copy(alpha = 0.8f)))
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    note.title, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color.White, 
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (note.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        note.content, 
                                        color = Color.White.copy(alpha = 0.6f), 
                                        fontSize = 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        CreatedAtText(
                            timestamp = note.createdAt,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        )
                    }
                }
                WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    WorkspaceDropdownItem(
                        text = "View Details",
                        onClick = { onViewNote(note); showMenu = false },
                        icon = Icons.Default.Description
                    )
                    WorkspaceDropdownItem(
                        text = "Edit",
                        onClick = { onEditNote(note); showMenu = false },
                        icon = Icons.Default.Edit
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    WorkspaceDropdownItem(
                        text = "Delete",
                        onClick = { onDeleteNote(note); showMenu = false },
                        icon = Icons.Default.Delete,
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun NoteDetailSection(
    note: NoteEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val accentColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(accentColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                Row {
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f)) }
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accentColor, modifier = Modifier.size(28.dp)) }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                Text(text = note.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = if (note.content.isNotBlank()) note.content else "Empty note.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun NoteAddEditSection(
    note: NoteEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val projectColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor

    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(projectColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                    if (note != null && onDelete != null) {
                        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f)) }
                    }
                }
                TextButton(
                    onClick = {
                        val updated = note?.copy(title = title, content = content)
                            ?: NoteEntity(projectId = projectId, title = title, content = content)
                        
                        if (note == null) viewModel.addNote(title, content, projectId)
                        else viewModel.updateNote(updated)
                        onBack()
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("SAVE", color = projectColor, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black),
                    cursorBrush = SolidColor(projectColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (title.isEmpty()) { Text("Note Title", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("CONTENT", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp, lineHeight = 24.sp),
                    cursorBrush = SolidColor(projectColor),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text("Start typing your thoughts...", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp)
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
