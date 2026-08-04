package com.example.allinone.ui.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.ui.home.*

@Composable
fun ManagementNotesSection(
    showTask: Boolean,
    showNote: Boolean,
    showProject: Boolean,
    showFinance: Boolean,
    safeSpendAmount: Double,
    taskColor: Int,
    noteColor: Int,
    projectColor: Int,
    financeColor: Int,
    taskIcon: Int,
    noteIcon: Int,
    projectIcon: Int,
    financeIcon: Int,
    auraAlpha: Float,
    onTaskClick: () -> Unit,
    onNoteClick: () -> Unit,
    onProjectClick: () -> Unit,
    onFinanceClick: () -> Unit,
    onTaskColorClick: () -> Unit,
    onNoteColorClick: () -> Unit,
    onProjectColorClick: () -> Unit,
    onFinanceColorClick: () -> Unit
) {
    if (!showTask && !showNote && !showProject && !showFinance) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Management & Notes",
            modifier = Modifier.padding(horizontal = 20.dp),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        DashboardPair(
            item1 = if (showTask) {
                {
                    TaskCard(
                        color = Color(if (taskColor == -1) 0xFF2EC4B6 else taskColor.toLong()),
                        icon = taskIcon,
                        onClick = onTaskClick,
                        onColorClick = onTaskColorClick,
                        auraAlpha = auraAlpha
                    )
                }
            } else null,
            item2 = if (showNote) {
                {
                    NoteCard(
                        color = Color(if (noteColor == -1) 0xFF3A86F0 else noteColor.toLong()),
                        icon = noteIcon,
                        onClick = onNoteClick,
                        onColorClick = onNoteColorClick,
                        auraAlpha = auraAlpha
                    )
                }
            } else null
        )

        if ((showTask || showNote) && (showProject || showFinance)) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        DashboardPair(
            item1 = if (showProject) {
                {
                    ProjectCard(
                        color = Color(if (projectColor == -1) 0xFF1A73E8 else projectColor.toLong()),
                        icon = projectIcon,
                        onClick = onProjectClick,
                        onColorClick = onProjectColorClick,
                        auraAlpha = auraAlpha
                    )
                }
            } else null,
            item2 = if (showFinance) {
                {
                    FinanceCard(
                        amount = safeSpendAmount,
                        color = Color(if (financeColor == -1) 0xFFE91E63 else financeColor.toLong()),
                        icon = financeIcon,
                        onClick = onFinanceClick,
                        onColorClick = onFinanceColorClick,
                        auraAlpha = auraAlpha
                    )
                }
            } else null
        )
    }
}
