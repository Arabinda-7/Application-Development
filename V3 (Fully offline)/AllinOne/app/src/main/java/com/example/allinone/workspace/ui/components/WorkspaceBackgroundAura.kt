package com.example.allinone.workspace.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.allinone.DataManager
import com.example.allinone.workspace.data.ProjectEntity

/**
 * WorkspaceBackgroundAura: Renders a smooth animated radial background gradient based on selected project color.
 */
@Composable
fun WorkspaceBackgroundAura(selectedProject: ProjectEntity?) {
    val targetColor = remember(selectedProject) {
        val projectColor = selectedProject?.color ?: -1
        val globalColor = DataManager.globalProjectColor
        if (projectColor != -1) Color(projectColor)
        else if (globalColor != -1) Color(globalColor)
        else Color(0xFF1A73E8)
    }

    val baseColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "BaseColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.25f),
                        baseColor.copy(alpha = 0.12f),
                        baseColor.copy(alpha = 0.06f),
                        baseColor.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    center = Offset(x = 600f, y = 100f),
                    radius = 2500f
                )
            )
    )
}
