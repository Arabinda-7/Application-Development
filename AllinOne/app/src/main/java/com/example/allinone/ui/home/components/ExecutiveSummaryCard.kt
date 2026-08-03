package com.example.allinone.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.allinone.LocalAppStyle
import java.util.Locale

@Composable
fun ExecutiveSummaryCard(
    overallProgress: Int,
    safeSpendAmount: Double,
    showPerformance: Boolean,
    showFinance: Boolean,
    onPerformanceClick: () -> Unit
) {
    val style = LocalAppStyle.current
    val config = LocalConfiguration.current
    val locale = config.locales[0]
    val showCard = showFinance || (showPerformance && overallProgress >= 0)
    
    if (showCard) {
        Card(
            shape = RoundedCornerShape(style.borderRadius),
            colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showPerformance) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPerformanceClick() }
                        ) {
                            Text(
                                "Daily Performance", 
                                color = style.accentColor, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${overallProgress}% Completed", 
                                color = Color.White, 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    
                    if (showFinance) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Safe Spend", 
                                color = Color.White.copy(alpha = 0.5f), 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold
                            )
                            val safeSpendColor = if (safeSpendAmount < 0) Color.Red else Color(0xFF2EC4B6)
                            Text(
                                String.format(locale, "₹%.0f", safeSpendAmount), 
                                color = safeSpendColor, 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                if (showPerformance) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress / 100f },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = style.accentColor,
                        trackColor = Color.White.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
