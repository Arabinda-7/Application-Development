package com.example.allinone.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FooterItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    showBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accentColor else Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Red, CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) accentColor else Color.White.copy(alpha = 0.4f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuickActionItem(
    label: String, 
    icon: ImageVector, 
    color: Color, 
    isVisible: Boolean, 
    offsetY: Dp, 
    offsetX: Dp, 
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val standardDensity = remember(context, config) { 
        Density(
            density = context.resources.displayMetrics.density,
            fontScale = config.fontScale
        )
    }

    val animatedAlpha by animateFloatAsState(targetValue = if (isVisible) 1f else 0f, label = "Alpha")
    val animatedScale by animateFloatAsState(targetValue = if (isVisible) 1f else 0.5f, label = "Scale")
    
    if (animatedAlpha > 0f) {
        CompositionLocalProvider(LocalDensity provides standardDensity) {
            Box(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .graphicsLayer(alpha = animatedAlpha, scaleX = animatedScale, scaleY = animatedScale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardPair(item1: (@Composable () -> Unit)?, item2: (@Composable () -> Unit)?) {
    if (item1 == null && item2 == null) return
    
    Row(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        if (item1 != null) {
            Box(modifier = Modifier.weight(1f)) { item1() }
        }
        
        if (item1 != null && item2 != null) {
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        if (item2 != null) {
            Box(modifier = Modifier.weight(1f)) { item2() }
        }
    }
}
