package com.example.allinone.ui.home.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.allinone.LocalAppStyle
import com.example.allinone.ui.home.components.QuickActionItem

@Composable
fun QuickActionsFab(
    showSpeedDial: Boolean,
    onToggleSpeedDial: () -> Unit,
    onQuickAddTodo: () -> Unit,
    onQuickAddExpense: () -> Unit,
    onQuickAddNote: () -> Unit,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    val style = LocalAppStyle.current
    val transition = updateTransition(targetState = showSpeedDial, label = "SpeedDial")
    val dialRotation by transition.animateFloat(label = "Rotation") { if (it) 45f else 0f }

    Box(
        modifier = modifier
            .offset { IntOffset(0, -offsetY.toInt()) }
            .padding(bottom = 2.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        QuickActionItem(
            label = "Task",
            icon = Icons.Default.Add,
            color = Color(0xFF2EC4B6),
            isVisible = showSpeedDial,
            offsetY = 0.dp,
            offsetX = (-85).dp,
            onClick = { onQuickAddTodo() }
        )

        QuickActionItem(
            label = "Cash",
            icon = Icons.Default.ShoppingCart,
            color = Color(0xFFE91E63),
            isVisible = showSpeedDial,
            offsetY = (-60).dp,
            offsetX = (-60).dp,
            onClick = { onQuickAddExpense() }
        )

        QuickActionItem(
            label = "Note",
            icon = Icons.Default.Edit,
            color = Color(0xFF3A86F0),
            isVisible = showSpeedDial,
            offsetY = (-85).dp,
            offsetX = 0.dp,
            onClick = { onQuickAddNote() }
        )

        val context = LocalContext.current
        val config = LocalConfiguration.current
        val standardDensity = remember(context, config) { 
            Density(
                density = context.resources.displayMetrics.density,
                fontScale = config.fontScale
            )
        }

        CompositionLocalProvider(LocalDensity provides standardDensity) {
            FloatingActionButton(
                onClick = onToggleSpeedDial,
                containerColor = style.accentColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(51.dp)
            ) {
                Icon(
                    imageVector = if (showSpeedDial) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Quick Action",
                    modifier = Modifier.size(27.dp).graphicsLayer(rotationZ = dialRotation)
                )
            }
        }
    }
}
