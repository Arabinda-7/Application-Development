package com.example.allinone.workspace.ui.sections.features.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * EmptyFeatureStateView: Placeholder UI when no features match status filter.
 */
@Composable
fun EmptyFeatureStateView(status: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 40.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (status == "Shipped") Icons.Default.RocketLaunch else Icons.Default.Category,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.05f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("No features in $status", color = Color.White.copy(alpha = 0.1f), fontSize = 14.sp)
        }
    }
}
