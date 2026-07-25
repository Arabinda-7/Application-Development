package com.example.allinone.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allinone.R

@Composable
fun ProfilePage(userName: MutableState<String>, selectedAvatar: MutableIntState, selectedRoles: MutableState<Set<String>>, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Personalize Identity", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, lineHeight = 38.sp)
        Text("Your identity shapes the system's focus.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text("CHOOSE AVATAR", color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceAround) {
            val avatars = listOf(R.drawable.boy_avatar_profile, R.drawable.girl_avatar_profile)
            avatars.forEach { resId -> AvatarItem(resId, selectedAvatar.intValue == resId, accentColor) { selectedAvatar.intValue = resId } }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = userName.value, onValueChange = { userName.value = it }, label = { Text("Display Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accentColor, focusedLabelColor = accentColor, unfocusedTextColor = Color.White, focusedTextColor = Color.White))
    }
}

@Composable
fun OverviewPage(accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Diversity3, contentDescription = null, tint = accentColor, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text("Unified Ecosystem", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Text("One app. Every dimension of your life managed in a seamless flow.", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp))
    }
}

@Composable
fun GlobalHubPage(sections: List<OnboardingSection>, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("The Global Hub", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
        Text("Enable the modules you need for your daily flow.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        LazyColumn(modifier = Modifier.weight(1f).padding(top = 24.dp)) {
            items(sections) { section ->
                Surface(onClick = { section.isEnabled.value = !section.isEnabled.value }, shape = RoundedCornerShape(24.dp), color = if (section.isEnabled.value) accentColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f), border = BorderStroke(1.dp, if (section.isEnabled.value) accentColor else Color.White.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (section.isEnabled.value) accentColor else Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(section.icon, contentDescription = null, tint = if (section.isEnabled.value) Color.White else Color.White.copy(alpha = 0.5f)) }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) { Text(section.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp); Text(section.description, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, lineHeight = 14.sp) }
                        Checkbox(checked = section.isEnabled.value, onCheckedChange = { section.isEnabled.value = it }, colors = CheckboxDefaults.colors(checkedColor = accentColor))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureDeepDivePage(section: OnboardingSection, accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp).copy(bottomStart = CornerSize(0.dp))).background(accentColor), contentAlignment = Alignment.Center) { Icon(section.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Spacer(modifier = Modifier.width(12.dp))
            Text(section.title.uppercase(), color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Deep Dive: ${section.title}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("Configure specific capabilities for this module.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        Text("CORE CAPABILITIES", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        FeatureCapabilitiesGrid(section.id, accentColor)
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("ACTIVE MODULES", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(modifier = Modifier.fillMaxWidth()) { section.subFeatures.forEach { config -> ModuleChip(config, section.isEnabled.value, accentColor) } }
        
        if (!section.isEnabled.value) {
            Spacer(modifier = Modifier.height(24.dp))
            Surface(color = Color.Red.copy(alpha = 0.1f), border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("This section is currently disabled in the Global Hub.", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ActivationPage(accentColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            val infiniteTransition = rememberInfiniteTransition(label = "Activation")
            val scale by infiniteTransition.animateFloat(initialValue = 1f, targetValue = 1.2f, animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "Scale")
            Box(modifier = Modifier.size(100.dp).graphicsLayer(scaleX = scale, scaleY = scale).clip(CircleShape).background(accentColor.copy(alpha = 0.1f)).border(2.dp, accentColor, CircleShape))
            Icon(Icons.Default.Verified, contentDescription = null, tint = accentColor, modifier = Modifier.size(60.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text("System Initialized", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("Your universe is synchronized. Welcome to the future of personal management.", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp))
    }
}
