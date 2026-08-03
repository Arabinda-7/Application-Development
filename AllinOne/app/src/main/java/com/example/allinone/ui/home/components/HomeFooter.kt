package com.example.allinone.ui.home.components

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.LocalAppStyle
import com.example.allinone.ui.home.components.FooterItem

@Composable
fun HomeFooter(
    selectedTab: Int,
    isAiEnabled: Boolean,
    onTabSelected: (Int) -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onVoiceAssistantRequested: () -> Unit
) {
    val style = LocalAppStyle.current
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .align(Alignment.BottomCenter),
            border = BorderStroke(1.dp, style.accentColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterItem(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = Icons.Default.Home,
                    label = "Home",
                    accentColor = style.accentColor,
                    modifier = Modifier.weight(1f)
                )

                if (isAiEnabled) {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                FooterItem(
                    selected = selectedTab == 2,
                    onClick = { 
                        onTabSelected(2)
                        onNavigateToSettings()
                    },
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    accentColor = style.accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isAiEnabled) {
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color.Black, CircleShape)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(style.accentColor)
                            .combinedClickable(
                                onClick = { onNavigateToAssistant(); onTabSelected(1) },
                                onLongClick = { onVoiceAssistantRequested() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text(
                    text = "AI",
                    color = if (selectedTab == 1) style.accentColor else Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
