package com.example.allinone

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AiAssistantIntroActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiIntroScreen()
        }
    }

    @Composable
    fun AiIntroScreen() {
        val style = AppStyle.fromSettings()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Black, Color(0xFF0A0A0A))))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = style.accentColor,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Meet Your AI Assistant",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "A powerful, on-device intelligence designed to help you manage your life through natural conversation.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            FeatureItem(Icons.Default.Mic, "Voice Interaction", "Talk naturally to add tasks, log habits, and get insights.")
            FeatureItem(Icons.Default.Psychology, "Smart Planning", "Automated breakdown of complex projects and goals.")
            FeatureItem(Icons.Default.Security, "Total Privacy", "Your data and voice processing never leave this device.")
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { handleConfirmation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = style.accentColor)
            ) {
                Text("CONFIRM & ENABLE", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
            }
            
            TextButton(
                onClick = { finish() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("NOT NOW", color = Color.Gray)
            }
        }
    }

    @Composable
    private fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(desc, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
    }

    private fun handleConfirmation() {
        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO) {
            setResult(RESULT_OK)
            finish()
        }
    }
}
