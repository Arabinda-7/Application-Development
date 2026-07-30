package com.example.allinone.workspace.ui.sections

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.workspace.data.BugEntity
import com.example.allinone.workspace.data.FeatureEntity
import com.example.allinone.workspace.data.ProjectEntity
import com.example.allinone.workspace.ui.WorkspaceUIState
import com.example.allinone.workspace.ui.WorkspaceViewModel

@Composable
fun WorkspaceDashboard(
    state: WorkspaceUIState,
    viewModel: WorkspaceViewModel,
    onShowStats: (ProjectEntity) -> Unit,
    isStatsShowing: Boolean
) {
    val style = LocalAppStyle.current
    val blurRadius by animateDpAsState(
        targetValue = if (isStatsShowing) 10.dp else 0.dp,
        label = "BlurRadius"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(if (blurRadius > 0.dp) Modifier.blur(blurRadius) else Modifier),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) { MetricCard("Progress", "${state.selectedProject?.progress ?: 0}%", style.accentColor) }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) { val shipped = state.features.count { it.status == "Shipped" }; val total = state.features.size; MetricCard("Shipped", if (total > 0) "$shipped / $total" else "0/0", Color(0xFF2EC4B6)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) { MetricCard("Health", state.selectedProject?.health ?: "Healthy", Color(0xFFE91E63)) }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) { MetricCard("Active Tasks", state.tasks.count { it.status != "Done" }.toString(), Color(0xFFFFB800)) }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Ecosystem",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "${state.projects.size} Projects",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(state.projects, key = { it.id }) { project ->
            Box(modifier = Modifier.animateItem()) {
                ProjectOverviewItem(
                    project = project,
                    isSelected = project.id == state.selectedProject?.id,
                    onClick = { viewModel.selectProject(project.id) },
                    onLongClick = { onShowStats(project) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
