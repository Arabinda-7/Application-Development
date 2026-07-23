package com.example.allinone.workspace.ui.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.ResourceEntity
import com.example.allinone.workspace.ui.WorkspaceViewModel

@Composable
fun ResourceViewSection(
    resources: List<ResourceEntity>,
    onViewResource: (ResourceEntity) -> Unit,
    onEditResource: (ResourceEntity) -> Unit,
    onDeleteResource: (ResourceEntity) -> Unit
) {
    val style = LocalAppStyle.current
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        itemsIndexed(resources, key = { _, res -> res.id }) { index, res ->
            var showMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth().animateItem()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = { onViewResource(res) },
                            onLongClick = { showMenu = true }
                        ),
                    colors = CardDefaults.cardColors(containerColor = style.surfaceColor)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = res.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = res.type, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        }
                    }
                }
                WorkspaceDropdown(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    WorkspaceDropdownItem(
                        text = "View Details",
                        onClick = { onViewResource(res); showMenu = false },
                        icon = Icons.Default.Description
                    )
                    WorkspaceDropdownItem(
                        text = "Edit",
                        onClick = { onEditResource(res); showMenu = false },
                        icon = Icons.Default.Edit
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.1f))
                    WorkspaceDropdownItem(
                        text = "Delete",
                        onClick = { onDeleteResource(res); showMenu = false },
                        icon = Icons.Default.Delete,
                        isDestructive = true
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceDetailSection(
    resource: ResourceEntity,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val accentColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(accentColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = accentColor) }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                Text(text = resource.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                
                DetailInfoItem(label = "TYPE", value = resource.type.uppercase(), color = accentColor)
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("LINK / PATH", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = resource.pathOrUrl,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        IconButton(onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(resource.pathOrUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Invalid link
                            }
                        }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = accentColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ResourceAddEditSection(
    resource: ResourceEntity? = null,
    projectId: String,
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit
) {
    val style = LocalAppStyle.current
    val projectColorHex = com.example.allinone.DataManager.globalProjectColor
    val projectColor = if (projectColorHex != -1) Color(projectColorHex) else style.accentColor

    var title by remember(resource) { mutableStateOf(resource?.title ?: "") }
    var type by remember(resource) { mutableStateOf(resource?.type ?: "URL") }
    var path by remember(resource) { mutableStateOf(resource?.pathOrUrl ?: "") }

    Box(modifier = Modifier.fillMaxSize().background(style.backgroundColor)) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(projectColor.copy(alpha = 0.15f)))
        
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) }
                TextButton(
                    onClick = {
                        val updated = resource?.copy(title = title, type = type, pathOrUrl = path)
                            ?: ResourceEntity(projectId = projectId, title = title, type = type, pathOrUrl = path)
                        
                        if (resource == null) viewModel.addResource(title, type, path, projectId)
                        else viewModel.updateResource(updated)
                        onBack()
                    },
                    enabled = title.isNotBlank() && path.isNotBlank()
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
                        if (title.isEmpty()) { Text("Resource Name", color = Color.White.copy(alpha = 0.2f), fontSize = 32.sp, fontWeight = FontWeight.Black) }
                        innerTextField()
                    }
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("RESOURCE TYPE", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("URL", "File", "Drive", "Design").forEach { t ->
                        val isSel = type == t
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) projectColor else projectColor.copy(alpha = 0.1f)).clickable { type = t }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(t.uppercase(), color = if (isSel) Color.Black else projectColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("LINK OR PATH", color = projectColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    placeholder = { Text("https://...", color = Color.White.copy(alpha = 0.2f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = projectColor, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
