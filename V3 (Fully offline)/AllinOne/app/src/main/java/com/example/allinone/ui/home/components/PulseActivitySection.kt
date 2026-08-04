package com.example.allinone.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle

@Composable
fun PulseActivitySection(
    recentActions: List<String>
) {
    if (recentActions.isEmpty()) return
    val style = LocalAppStyle.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Pulse Activity", 
            modifier = Modifier.padding(horizontal = 20.dp), 
            color = Color.White.copy(alpha = 0.4f), 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold, 
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recentActions) { action ->
                Surface(
                    color = style.surfaceColor, 
                    shape = RoundedCornerShape(12.dp), 
                    modifier = Modifier.height(40.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(style.accentColor.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            action, 
                            color = Color.White.copy(alpha = 0.7f), 
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
