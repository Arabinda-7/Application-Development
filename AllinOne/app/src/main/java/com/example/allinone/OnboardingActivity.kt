package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class OnboardingPageType {
    PROFILE, OVERVIEW, GLOBAL_HUB, FEATURE_DEEP_DIVE, ACTIVATION
}

data class OnboardingSection(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    var isEnabled: MutableState<Boolean>,
    val subFeatures: List<SubFeatureConfig> = emptyList()
)

data class SubFeatureConfig(
    val id: String,
    val label: String,
    var isEnabled: MutableState<Boolean>
)

class OnboardingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnboardingFlow()
        }
    }

    @Composable
    fun OnboardingFlow() {
        val scope = rememberCoroutineScope()
        
        // --- State Management ---
        val userName = remember { mutableStateOf("") }
        val selectedAvatar = remember { mutableIntStateOf(R.drawable.boy_avatar_profile) }
        val primaryGoal = remember { mutableStateOf("Productivity") }
        
        val sections = remember {
            listOf(
                OnboardingSection("HABITS", "Habits", "Track daily rituals & rituals", Icons.Default.CheckCircle, mutableStateOf(true)),
                OnboardingSection("WORKOUTS", "Workouts", "Log exercises & routines", Icons.Default.Star, mutableStateOf(true)),
                OnboardingSection("TASKS", "Tasks", "Manage to-do lists & priorities", Icons.AutoMirrored.Filled.List, mutableStateOf(true)),
                OnboardingSection("NOTES", "Notes", "Capture ideas & daily logs", Icons.Default.Edit, mutableStateOf(true)),
                OnboardingSection("PROJECTS", "Projects", "Plan roadmaps & features", Icons.Default.Build, mutableStateOf(true)),
                OnboardingSection("FINANCE", "Vault", "Track expenses & savings", Icons.Default.AccountBalanceWallet, mutableStateOf(true))
            )
        }

        // Dynamic Page Calculation
        val visiblePages = remember(sections.map { it.isEnabled.value }) {
            val pages = mutableListOf<OnboardingPageType>()
            pages.add(OnboardingPageType.PROFILE)
            pages.add(OnboardingPageType.OVERVIEW)
            pages.add(OnboardingPageType.GLOBAL_HUB)
            
            sections.forEach { section ->
                if (section.isEnabled.value) {
                    pages.add(OnboardingPageType.FEATURE_DEEP_DIVE)
                }
            }
            
            pages.add(OnboardingPageType.ACTIVATION)
            pages
        }

        val pagerState = rememberPagerState(pageCount = { visiblePages.size })

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Background Aura
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1A73E8).copy(alpha = 0.15f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(0f, 0f)
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = userName.value.isNotEmpty() // Block until profile started
                ) { pageIndex ->
                    val type = visiblePages[pageIndex]
                    
                    // Logic to find which section this DEEP_DIVE belongs to
                    var activeSection: OnboardingSection? = null
                    if (type == OnboardingPageType.FEATURE_DEEP_DIVE) {
                        val deepDiveIndex = visiblePages.take(pageIndex).count { it == OnboardingPageType.FEATURE_DEEP_DIVE }
                        activeSection = sections.filter { it.isEnabled.value }[deepDiveIndex]
                    }

                    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        when (type) {
                            OnboardingPageType.PROFILE -> ProfilePage(userName, selectedAvatar, primaryGoal)
                            OnboardingPageType.OVERVIEW -> OverviewPage()
                            OnboardingPageType.GLOBAL_HUB -> GlobalHubPage(sections)
                            OnboardingPageType.FEATURE_DEEP_DIVE -> activeSection?.let { FeatureDeepDivePage(it) }
                            OnboardingPageType.ACTIVATION -> ActivationPage()
                        }
                    }
                }

                // Footer Navigation
                OnboardingFooter(
                    pagerState = pagerState,
                    isProfileValid = userName.value.isNotEmpty(),
                    isLastPage = pagerState.currentPage == visiblePages.size - 1,
                    onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    onFinish = { completeOnboarding(userName.value, selectedAvatar.intValue, sections) }
                )
            }
        }
    }

    @Composable
    fun ProfilePage(name: MutableState<String>, avatar: MutableIntState, goal: MutableState<String>) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text("Create Your Identity", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Start your journey with a personal touch", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A73E8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("PRIMARY GOAL", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            
            val goals = listOf("Productivity", "Health", "Knowledge", "Finance")
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                goals.forEach { g ->
                    val isSelected = goal.value == g
                    Surface(
                        modifier = Modifier.clickable { goal.value = g },
                        color = if (isSelected) Color(0xFF1A73E8) else Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.05f))
                    ) {
                        Text(g, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("SELECT AVATAR", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.Center) {
                AvatarItem(R.drawable.boy_avatar_profile, avatar.intValue == R.drawable.boy_avatar_profile) { avatar.intValue = R.drawable.boy_avatar_profile }
                Spacer(modifier = Modifier.width(24.dp))
                AvatarItem(R.drawable.girl_avatar_profile, avatar.intValue == R.drawable.girl_avatar_profile) { avatar.intValue = R.drawable.girl_avatar_profile }
            }
        }
    }

    @Composable
    fun AvatarItem(resId: Int, isSelected: Boolean, onClick: () -> Unit) {
        val alpha by animateFloatAsState(if (isSelected) 1f else 0.3f, label = "alpha")
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF1A73E8).copy(alpha = 0.2f) else Color.Transparent)
                .border(2.dp, if (isSelected) Color(0xFF1A73E8) else Color.Transparent, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(resId),
                contentDescription = null,
                modifier = Modifier.size(60.dp).graphicsLayer(alpha = alpha)
            )
        }
    }

    @Composable
    fun OverviewPage() {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color(0xFF1A73E8), modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text("One Dashboard. Total Control.", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "All-in-One connects your habits, health, tasks, and finances into a single high-performance interface. No more app switching.",
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }

    @Composable
    fun GlobalHubPage(sections: List<OnboardingSection>) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Your Ecosystem", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Enable only what you need. You can always change this later.", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            sections.forEach { section ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(40.dp).background(Color(0xFF1A73E8).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(section.icon, null, tint = Color(0xFF1A73E8), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(section.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(section.description, color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = section.isEnabled.value,
                        onCheckedChange = { section.isEnabled.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1A73E8))
                    )
                }
            }
        }
    }

    @Composable
    fun FeatureDeepDivePage(section: OnboardingSection) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(section.icon, null, tint = Color(0xFF1A73E8), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(section.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Text("Customize how you track your ${section.title.lowercase()}.", color = Color.Gray, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Sub-feature toggle logic could go here as per spec.
            // Simplified for initial implementation:
            Text("CORE CAPABILITIES", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            
            val capabilities = when(section.id) {
                "HABITS" -> listOf("Flexible Schedules", "Time-based Filtering", "Streak Tracking")
                "WORKOUTS" -> listOf("Exercise Timer", "Set Tracking", "Muscle Balance")
                "TASKS" -> listOf("Priority System", "Subtask Management", "Search & Filter")
                "NOTES" -> listOf("Smart Templates", "Category Sorting", "Voice Input")
                "PROJECTS" -> listOf("Feature Roadmaps", "Interactive Boards", "Activity Logs")
                "FINANCE" -> listOf("Safe-Spend Calculation", "Monthly Budgeting", "Transaction Ledger")
                else -> emptyList()
            }

            capabilities.forEach { cap ->
                Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF2EC4B6), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(cap, color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }

    @Composable
    fun ActivationPage() {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF2EC4B6), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("Engine Activated", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your personal ecosystem is ready.", color = Color.Gray, fontSize = 16.sp, textAlign = TextAlign.Center)
        }
    }

    @Composable
    fun OnboardingFooter(pagerState: PagerState, isProfileValid: Boolean, isLastPage: Boolean, onNext: () -> Unit, onFinish: () -> Unit) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Indicators
                Row(modifier = Modifier.weight(1f)) {
                    repeat(pagerState.pageCount) { i ->
                        val isSelected = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(4.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF1A73E8) else Color.White.copy(alpha = 0.1f))
                        )
                    }
                }

                if (isLastPage) {
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("GO TO DASHBOARD", fontWeight = FontWeight.Bold)
                    }
                } else {
                    IconButton(
                        onClick = onNext,
                        enabled = isProfileValid,
                        modifier = Modifier.clip(CircleShape).background(if (isProfileValid) Color(0xFF1A73E8) else Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = if (isProfileValid) Color.White else Color.Gray)
                    }
                }
            }
        }
    }

    private fun completeOnboarding(name: String, avatarRes: Int, sections: List<OnboardingSection>) {
        DataManager.userName = name
        DataManager.userAvatarRes = avatarRes
        
        // Save Visibility States
        DataManager.showHabitSection = sections.find { it.id == "HABITS" }?.isEnabled?.value ?: true
        DataManager.showWorkoutSection = sections.find { it.id == "WORKOUTS" }?.isEnabled?.value ?: true
        DataManager.showTaskSection = sections.find { it.id == "TASKS" }?.isEnabled?.value ?: true
        DataManager.showNoteSection = sections.find { it.id == "NOTES" }?.isEnabled?.value ?: true
        DataManager.showProjectSection = sections.find { it.id == "PROJECTS" }?.isEnabled?.value ?: true
        DataManager.showFinanceSection = sections.find { it.id == "FINANCE" }?.isEnabled?.value ?: true

        DataManager.isOnboardingCompleted = true
        DataManager.saveData(this)

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
