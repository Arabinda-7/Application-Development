package com.example.allinone.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.R

@Composable
fun LiquidBackground(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidBg")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart), label = "Phase"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width; val canvasHeight = size.height
            drawCircle(brush = Brush.radialGradient(colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)), center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.2f + (50 * Math.sin(phase.toDouble())).toFloat(), canvasHeight * 0.3f), radius = canvasWidth * 0.8f)
            drawCircle(brush = Brush.radialGradient(colors = listOf(accentColor.copy(alpha = 0.1f), Color.Transparent)), center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.8f, canvasHeight * 0.7f + (40 * Math.cos(phase.toDouble())).toFloat()), radius = canvasWidth * 0.6f)
        }
    }
}

@Composable
fun AvatarItem(resId: Int, isSelected: Boolean, accentColor: Color, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.15f else 1f, label = "AvatarScale")
    val borderAlpha by animateFloatAsState(targetValue = if (isSelected) 1f else 0.2f, label = "AvatarBorder")

    Box(modifier = Modifier.size(100.dp).graphicsLayer(scaleX = scale, scaleY = scale).clip(CircleShape).border(2.dp, accentColor.copy(alpha = borderAlpha), CircleShape).clickable { onClick() }.padding(8.dp), contentAlignment = Alignment.Center) {
        Image(painter = androidx.compose.ui.res.painterResource(resId), contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
        if (isSelected) { Box(modifier = Modifier.fillMaxSize().background(accentColor.copy(alpha = 0.1f))) }
    }
}

@Composable
fun ModuleChip(config: SubFeatureConfig, isSectionEnabled: Boolean, accentColor: Color) {
    val alpha by animateFloatAsState(targetValue = if (isSectionEnabled) 1f else 0.3f, label = "ChipAlpha")
    val bgColor by animateColorAsState(targetValue = if (config.isEnabled.value) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), label = "ChipBg")
    val borderColor by animateColorAsState(targetValue = if (config.isEnabled.value) accentColor else Color.White.copy(alpha = 0.1f), label = "ChipBorder")

    Surface(onClick = { if (isSectionEnabled) config.isEnabled.value = !config.isEnabled.value }, shape = RoundedCornerShape(12.dp), color = bgColor, border = BorderStroke(1.dp, borderColor), modifier = Modifier.padding(4.dp).graphicsLayer(alpha = alpha)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = if (config.isEnabled.value) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline, contentDescription = null, tint = if (config.isEnabled.value) accentColor else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp)); Text(text = config.label, color = if (config.isEnabled.value) Color.White else Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeatureCapabilitiesGrid(sectionId: String, accentColor: Color) {
    val caps = when (sectionId) {
        "HABITS" -> listOf("Heatmaps", "Streaks", "Daily Reset", "Notifications", "Aura Themes", "Logging")
        "TASKS" -> listOf("Priority", "Categories", "Search", "Reminders", "Analytics", "Subtasks")
        "NOTES" -> listOf("Templates", "Voice Input", "Auto-Cleanup", "Pinning", "Voice Memos", "Drafts")
        "FINANCE" -> listOf("Budgeting", "Heatmaps", "Categorization", "Income/Exp", "Savings", "Currencies")
        "PROJECTS" -> listOf("Roadmaps", "Milestones", "Sub-features", "Ideas", "History", "Sync")
        "WORKOUTS" -> listOf("Muscle Grp", "Timer", "Sets/Reps", "Balance", "Logging", "Progression")
        else -> emptyList()
    }

    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(180.dp), contentPadding = PaddingValues(8.dp)) {
        items(caps) { cap ->
            Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp)); Text(cap, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun OnboardingFooter(pagerState: PagerState, accentColor: Color, isLastPage: Boolean, onNext: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row { repeat(pagerState.pageCount) { i -> val active = pagerState.currentPage == i; val width by animateDpAsState(targetValue = if (active) 24.dp else 8.dp, label = "DotWidth"); Box(modifier = Modifier.padding(horizontal = 4.dp).height(8.dp).width(width).clip(CircleShape).background(if (active) accentColor else Color.White.copy(alpha = 0.2f))) } }
        Button(onClick = { onNext(pagerState.currentPage) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)) {
            Text(if (isLastPage) "ACTIVATE SYSTEM" else "CONTINUE", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.width(8.dp)); Icon(if (isLastPage) Icons.Default.PowerSettingsNew else Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}
