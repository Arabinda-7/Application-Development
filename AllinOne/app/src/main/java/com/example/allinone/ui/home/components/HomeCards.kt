package com.example.allinone.ui.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.UIUtils
import com.example.allinone.R
import java.util.Locale

@Composable
fun rememberSafePainter(resId: Int, fallbackId: Int): androidx.compose.ui.graphics.painter.Painter {
    val context = LocalContext.current
    val safeId = remember(resId) {
        if (UIUtils.isDrawableResource(context, resId)) resId else fallbackId
    }
    return painterResource(id = safeId)
}

fun Modifier.glassBlur(enabled: Boolean, radius: Float = 20f): Modifier = if (enabled) {
    this.graphicsLayer {
        renderEffect = android.graphics.RenderEffect.createBlurEffect(
            radius, radius, android.graphics.Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }
} else this

@Composable
fun HabitCard(progress: Int, color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit, auraAlpha: Float = 0.6f) {
    val style = LocalAppStyle.current
    val cardBorder = if (style.cardStyle == "GLASS") {
        BorderStroke(1.5.dp, Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = auraAlpha),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha * 0.7f),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha)
            )
        ))
    } else null

    val cardElevation = if (style.showShadows) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(style.borderRadius),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .then(
                if (cardBorder != null) {
                    Modifier.border(cardBorder, RoundedCornerShape(style.borderRadius))
                } else Modifier
            )
            .graphicsLayer {
                if (style.cardStyle == "GLASS") {
                    shadowElevation = 12f
                    ambientShadowColor = color.copy(alpha = 0.4f)
                    spotShadowColor = color.copy(alpha = 0.1f)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (style.cardStyle == "GLASS") {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .glassBlur(enabled = true, radius = 40f)
                )
            }
            
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Text("HABITS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text("Daily Rituals", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }

                Icon(
                    painter = rememberSafePainter(resId = icon, fallbackId = R.drawable.ic_habit_tracker),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp).align(Alignment.BottomStart)
                )

                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp).align(Alignment.BottomEnd)) {
                    CircularProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = -1f),
                        color = color,
                        strokeWidth = 6.dp,
                        trackColor = Color.White.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                    Text("$progress%", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                }

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f))
                        .border(1.dp, color, CircleShape)
                        .align(Alignment.TopEnd)
                        .clickable { onColorClick() }
                )
            }
        }
    }
}

@Composable
fun WorkoutCard(progress: Int, color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit, auraAlpha: Float = 0.6f) {
    val style = LocalAppStyle.current
    val cardBorder = if (style.cardStyle == "GLASS") {
        BorderStroke(1.5.dp, Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = auraAlpha),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha * 0.7f),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha)
            )
        ))
    } else null

    val cardElevation = if (style.showShadows) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(style.borderRadius),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .then(if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(style.borderRadius)) else Modifier)
            .graphicsLayer {
                if (style.cardStyle == "GLASS") {
                    shadowElevation = 12f
                    ambientShadowColor = color.copy(alpha = 0.4f)
                    spotShadowColor = color.copy(alpha = 0.1f)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (style.cardStyle == "GLASS") {
                Box(modifier = Modifier.matchParentSize().glassBlur(enabled = true, radius = 40f))
            }
            Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = rememberSafePainter(resId = icon, fallbackId = R.drawable.ic_workout_routine), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WORKOUT", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Text("ACTIVE MODE", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$progress%", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = color, trackColor = Color.White.copy(alpha = 0.05f), strokeCap = StrokeCap.Round)
            }
        }
    }
}

@Composable
fun TaskCard(color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit, auraAlpha: Float = 0.6f) {
    val style = LocalAppStyle.current
    val cardBorder = if (style.cardStyle == "GLASS") {
        BorderStroke(1.5.dp, Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = auraAlpha),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha * 0.7f),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha)
            )
        ))
    } else null

    val cardElevation = if (style.showShadows) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(style.borderRadius),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .then(if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(style.borderRadius)) else Modifier)
            .graphicsLayer {
                if (style.cardStyle == "GLASS") {
                    shadowElevation = 10f
                    ambientShadowColor = color.copy(alpha = 0.3f)
                    spotShadowColor = color.copy(alpha = 0.1f)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (style.cardStyle == "GLASS") {
                Box(modifier = Modifier.matchParentSize().glassBlur(enabled = true, radius = 40f))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(painter = rememberSafePainter(resId = icon, fallbackId = R.drawable.ic_task), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("TASKS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.width(60.dp).height(4.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(12.dp).border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp)))
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit, auraAlpha: Float = 0.6f) {
    val style = LocalAppStyle.current
    val cardBorder = if (style.cardStyle == "GLASS") {
        BorderStroke(1.5.dp, Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = auraAlpha),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha * 0.7f),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha)
            )
        ))
    } else null

    val cardElevation = if (style.showShadows) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(style.borderRadius),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .then(if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(style.borderRadius)) else Modifier)
            .graphicsLayer {
                if (style.cardStyle == "GLASS") {
                    shadowElevation = 10f
                    ambientShadowColor = color.copy(alpha = 0.3f)
                    spotShadowColor = color.copy(alpha = 0.1f)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (style.cardStyle == "GLASS") {
                Box(modifier = Modifier.matchParentSize().glassBlur(enabled = true, radius = 40f))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text("NOTES", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Canvas", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Icon(painter = rememberSafePainter(resId = icon, fallbackId = R.drawable.ic_notes), contentDescription = null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
                }
            }
        }
    }
}

@Composable
fun ProjectCard(color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit, auraAlpha: Float = 0.6f) {
    val style = LocalAppStyle.current
    val cardBorder = if (style.cardStyle == "GLASS") {
        BorderStroke(1.5.dp, Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = auraAlpha),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha * 0.7f),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha)
            )
        ))
    } else null

    val cardElevation = if (style.showShadows) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(style.borderRadius),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .then(if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(style.borderRadius)) else Modifier)
            .graphicsLayer {
                if (style.cardStyle == "GLASS") {
                    shadowElevation = 12f
                    ambientShadowColor = color.copy(alpha = 0.4f)
                    spotShadowColor = color.copy(alpha = 0.1f)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (style.cardStyle == "GLASS") {
                Box(modifier = Modifier.matchParentSize().glassBlur(enabled = true, radius = 40f))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = rememberSafePainter(resId = icon, fallbackId = R.drawable.ic_project), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PROJECTS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(4) { index -> Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(if (index < 2) color else Color.White.copy(alpha = 0.05f))) }
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
                }
            }
        }
    }
}

@Composable
fun FinanceCard(amount: Double, color: Color, icon: Int, onClick: () -> Unit, onColorClick: () -> Unit, auraAlpha: Float = 0.6f) {
    val style = LocalAppStyle.current
    val cardBorder = if (style.cardStyle == "GLASS") {
        BorderStroke(1.5.dp, Brush.sweepGradient(
            colors = listOf(
                color.copy(alpha = auraAlpha),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha * 0.7f),
                Color.White.copy(alpha = 0.2f),
                color.copy(alpha = auraAlpha)
            )
        ))
    } else null

    val cardElevation = if (style.showShadows) CardDefaults.cardElevation(defaultElevation = 8.dp) else CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(style.borderRadius),
        colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
        elevation = cardElevation,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .then(if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(style.borderRadius)) else Modifier)
            .graphicsLayer {
                if (style.cardStyle == "GLASS") {
                    shadowElevation = 10f
                    ambientShadowColor = color.copy(alpha = 0.3f)
                    spotShadowColor = color.copy(alpha = 0.1f)
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (style.cardStyle == "GLASS") {
                Box(modifier = Modifier.matchParentSize().glassBlur(enabled = true, radius = 40f))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Icon(painter = rememberSafePainter(resId = icon, fallbackId = R.drawable.ic_finance), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("VAULT", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(String.format(Locale.getDefault(), "₹%.0f", amount), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
