package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.absoluteValue

enum class OnboardingPageType {
    PROFILE, OVERVIEW, GLOBAL_HUB, FEATURE_DEEP_DIVE, ACTIVATION
}

data class SubFeatureConfig(
    val id: String,
    val label: String,
    val isEnabled: MutableState<Boolean>
)

data class OnboardingSection(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isEnabled: MutableState<Boolean>,
    val subFeatures: List<SubFeatureConfig> = emptyList()
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
        
        val userName = remember { mutableStateOf("") }
        val selectedAvatar = remember { mutableIntStateOf(R.drawable.boy_avatar_profile) }
        val selectedFocus = remember { mutableStateOf(setOf<String>()) }
        
        val sections = remember {
            listOf(
                OnboardingSection("HABITS", "Habits", "Daily rituals", Icons.Default.CheckCircle, mutableStateOf(true)),
                OnboardingSection("WORKOUTS", "Workouts", "Log exercises", Icons.Default.Star, mutableStateOf(true)),
                OnboardingSection("TASKS", "Tasks", "Manage priorities", Icons.AutoMirrored.Filled.List, mutableStateOf(true), listOf(
                    SubFeatureConfig("TASKS", "Tasks", mutableStateOf(true)),
                    SubFeatureConfig("LIST", "Work List", mutableStateOf(false))
                )),
                OnboardingSection("NOTES", "Notes", "Capture ideas", Icons.Default.Edit, mutableStateOf(true), listOf(
                    SubFeatureConfig("NOTES", "Notes", mutableStateOf(true)),
                    SubFeatureConfig("QUESTIONS", "Questions", mutableStateOf(false)),
                    SubFeatureConfig("DAILY", "Daily Log", mutableStateOf(false)),
                    SubFeatureConfig("STORIES", "Stories", mutableStateOf(false))
                )),
                OnboardingSection("PROJECTS", "Projects", "Plan roadmaps", Icons.Default.Build, mutableStateOf(true), listOf(
                    SubFeatureConfig("ROADMAPS", "Roadmaps", mutableStateOf(true)),
                    SubFeatureConfig("IDEAS", "Project Ideas", mutableStateOf(true))
                )),
                OnboardingSection("FINANCE", "Vault", "Track savings", Icons.Default.AccountBalanceWallet, mutableStateOf(true))
            )
        }

        val visiblePages = remember(sections.map { it.isEnabled.value }) {
            val pages = mutableListOf<OnboardingPageType>()
            pages.add(OnboardingPageType.PROFILE)
            pages.add(OnboardingPageType.OVERVIEW)
            pages.add(OnboardingPageType.GLOBAL_HUB)
            sections.filter { it.isEnabled.value }.forEach { pages.add(OnboardingPageType.FEATURE_DEEP_DIVE) }
            pages.add(OnboardingPageType.ACTIVATION)
            pages
        }

        val pagerState = rememberPagerState(pageCount = { visiblePages.size })
        val hasVisitedLastPage = remember { mutableStateOf(false) }
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

        LaunchedEffect(pagerState.currentPage) {
            // Dismiss keyboard when page changes
            focusManager.clearFocus()
            
            if (pagerState.currentPage == visiblePages.size - 1) {
                hasVisitedLastPage.value = true
            }
        }
        
        val themeColor = remember(pagerState.targetPage, sections.map { it.isEnabled.value }) {
            val targetPageType = visiblePages.getOrNull(pagerState.targetPage)
            when (targetPageType) {
                OnboardingPageType.PROFILE -> Color(0xFF1A73E8)
                OnboardingPageType.GLOBAL_HUB -> Color(0xFF673AB7)
                OnboardingPageType.ACTIVATION -> Color(0xFF2EC4B6)
                OnboardingPageType.FEATURE_DEEP_DIVE -> {
                    val deepDiveIndex = visiblePages.take(pagerState.targetPage).count { it == OnboardingPageType.FEATURE_DEEP_DIVE }
                    val activeSection = sections.filter { it.isEnabled.value }.getOrNull(deepDiveIndex)
                    when (activeSection?.id) {
                        "HABITS" -> Color(0xFFFF7A59)
                        "WORKOUTS" -> Color(0xFFFFB800)
                        "TASKS" -> Color(0xFF2EC4B6)
                        "NOTES" -> Color(0xFF3A86F0)
                        "PROJECTS" -> Color(0xFF1A73E8)
                        "FINANCE" -> Color(0xFFE91E63)
                        else -> Color(0xFF1A73E8)
                    }
                }
                else -> Color(0xFF1A73E8)
            }
        }
        val animatedThemeColor by animateColorAsState(themeColor, tween(800), label = "color")

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
        ) {
            LiquidBackground(animatedThemeColor)

            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Box(modifier = Modifier.weight(1f)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = userName.value.isNotEmpty(),
                        beyondViewportPageCount = 1
                    ) { pageIndex ->
                        val type = visiblePages[pageIndex]
                        
                        var activeSection: OnboardingSection? = null
                        if (type == OnboardingPageType.FEATURE_DEEP_DIVE) {
                            val deepDiveIndex = visiblePages.take(pageIndex).count { it == OnboardingPageType.FEATURE_DEEP_DIVE }
                            activeSection = sections.filter { it.isEnabled.value }.getOrNull(deepDiveIndex)
                        }

                        // --- 2. Scroll-Linked Transformations ---
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .graphicsLayer {
                                val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                                alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                                val scale = 0.85f + (1f - pageOffset.absoluteValue.coerceIn(0f, 1f)) * 0.15f
                                scaleX = scale
                                scaleY = scale
                                translationX = pageOffset * size.width * 0.2f
                            }
                        ) {
                            when (type) {
                                OnboardingPageType.PROFILE -> ProfilePage(userName, selectedAvatar, selectedFocus, animatedThemeColor)
                                OnboardingPageType.OVERVIEW -> OverviewPage(animatedThemeColor)
                                OnboardingPageType.GLOBAL_HUB -> GlobalHubPage(sections, animatedThemeColor)
                                OnboardingPageType.FEATURE_DEEP_DIVE -> activeSection?.let { FeatureDeepDivePage(it, animatedThemeColor) }
                                OnboardingPageType.ACTIVATION -> ActivationPage(animatedThemeColor)
                            }
                        }
                    }

                    // --- Floating Next/Launch Button (Repositioned) ---
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 4.dp, end = 24.dp) 
                            .offset(y = (-40).dp) // Floating between footer and content
                            .zIndex(1f)
                    ) {
                        val isLastPage = pagerState.currentPage == visiblePages.size - 1
                        if (isLastPage) {
                            Button(
                                onClick = { completeOnboarding(userName.value, selectedAvatar.intValue, sections) },
                                colors = ButtonDefaults.buttonColors(containerColor = animatedThemeColor),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text("LAUNCH", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            }
                        } else {
                            val isProfileValid = userName.value.isNotEmpty()
                            IconButton(
                                onClick = { 
                                    scope.launch { 
                                        pagerState.animateScrollToPage(
                                            page = pagerState.currentPage + 1,
                                            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                                        ) 
                                    } 
                                },
                                enabled = isProfileValid,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isProfileValid) animatedThemeColor else Color.White.copy(alpha = 0.05f))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    tint = if (isProfileValid) Color.White else Color.Gray
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OnboardingFooter(
                        pagerState = pagerState,
                        themeColor = animatedThemeColor,
                        canJump = hasVisitedLastPage.value,
                        onDotClick = { page ->
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = page,
                                    animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun LiquidBackground(color: Color) {
        val infiniteTransition = rememberInfiniteTransition(label = "liquid")
        val blobOffset1 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 80f,
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse), label = "b1"
        )
        val blobOffset2 by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = -80f,
            animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse), label = "b2"
        )

        Box(modifier = Modifier.fillMaxSize().blur(30.dp)) {
            Box(modifier = Modifier
                .offset(x = blobOffset1.dp, y = blobOffset2.dp)
                .size(300.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.3f), Color.Transparent)))
            )
            Box(modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = blobOffset2.dp, y = blobOffset1.dp)
                .size(400.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)))
            )
        }
    }

    @Composable
    fun ProfilePage(name: MutableState<String>, avatar: MutableIntState, selectedFocus: MutableState<Set<String>>, themeColor: Color) {
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Text("Create Identity", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            Text("Your journey begins with a personal touch.", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                placeholder = { Text("Your Name", color = Color.White.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = themeColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))
            Text("CORE FOCUS", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
            
            FlowRow(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Productivity", "Health", "Knowledge", "Finance").forEach { g ->
                    val isSelected = selectedFocus.value.contains(g)
                    Surface(
                        modifier = Modifier.clickable { 
                            selectedFocus.value = if (isSelected) selectedFocus.value - g else selectedFocus.value + g
                            focusManager.clearFocus()
                        },
                        color = if (isSelected) themeColor else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                    ) {
                        Text(g, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AvatarItem(R.drawable.boy_avatar_profile, avatar.intValue == R.drawable.boy_avatar_profile, themeColor) { 
                    avatar.intValue = R.drawable.boy_avatar_profile 
                    focusManager.clearFocus()
                }
                AvatarItem(R.drawable.girl_avatar_profile, avatar.intValue == R.drawable.girl_avatar_profile, themeColor) { 
                    avatar.intValue = R.drawable.girl_avatar_profile 
                    focusManager.clearFocus()
                }
            }
        }
    }

    @Composable
    fun AvatarItem(resId: Int, isSelected: Boolean, themeColor: Color, onClick: () -> Unit) {
        val scale by animateFloatAsState(if (isSelected) 1.1f else 0.9f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "s")
        val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, label = "a")
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                .clip(CircleShape)
                .background(if (isSelected) themeColor.copy(alpha = 0.15f) else Color.Transparent)
                .border(2.dp, if (isSelected) themeColor else Color.White.copy(alpha = 0.05f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(resId), contentDescription = null, modifier = Modifier.size(56.dp))
        }
    }

    @Composable
    fun OverviewPage(themeColor: Color) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = themeColor.copy(alpha = 0.1f), border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))) {
                Icon(Icons.Default.Dashboard, null, tint = themeColor, modifier = Modifier.padding(24.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("One Dashboard.", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("Total Control.", color = themeColor, fontSize = 32.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Experience the synergy of habits, tasks, and wealth tracking in a single interface.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }

    @Composable
    fun GlobalHubPage(sections: List<OnboardingSection>, themeColor: Color) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("The Ecosystem", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text("Enable core modules to build your dashboard.", color = Color.White.copy(alpha = 0.5f), fontSize = 15.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sections.forEach { section ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { section.isEnabled.value = !section.isEnabled.value },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = if (section.isEnabled.value) 0.1f else 0.02f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(themeColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(section.icon, null, tint = if (section.isEnabled.value) themeColor else Color.Gray, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(section.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(section.description, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                            }
                            Switch(
                                checked = section.isEnabled.value,
                                onCheckedChange = { section.isEnabled.value = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = themeColor)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FeatureDeepDivePage(section: OnboardingSection, themeColor: Color) {
        var isExpanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = themeColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.2f))
                ) {
                    Icon(section.icon, null, tint = themeColor, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(section.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Personalize your ${section.title.lowercase()} modules to fit your workflow perfectly.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            val enabledSubFeatures = section.subFeatures.filter { it.isEnabled.value }
            val disabledSubFeatures = section.subFeatures.filter { !it.isEnabled.value }

            // --- Unified Module Configuration ---
            if (section.subFeatures.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "MODULE CONFIGURATION",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        if (!isExpanded) {
                            Text(
                                text = "Customize this section to fit your workflow",
                                color = themeColor.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (enabledSubFeatures.isNotEmpty()) {
                            Text(
                                "ACTIVE",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            enabledSubFeatures.forEach { sub ->
                                ModuleChip(sub, true, themeColor)
                            }
                        }
                        
                        if (disabledSubFeatures.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "AVAILABLE",
                                color = Color.White.copy(alpha = 0.2f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            disabledSubFeatures.forEach { sub ->
                                ModuleChip(sub, false, themeColor)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Note: These features can also be adjusted later in settings.",
                            color = themeColor.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- Global Feature Highlights ---
            Text(
                "FEATURE HIGHLIGHTS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FeatureCapabilitiesGrid(section.id, themeColor)
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    @Composable
    fun ModuleChip(sub: SubFeatureConfig, isEnabled: Boolean, themeColor: Color) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { sub.isEnabled.value = !sub.isEnabled.value },
            shape = RoundedCornerShape(12.dp),
            color = if (isEnabled) themeColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f),
            border = BorderStroke(1.dp, if (isEnabled) themeColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = if (isEnabled) Color(0xFF2EC4B6) else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(sub.label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Text(if (isEnabled) "ENABLED" else "DISABLED", color = if (isEnabled) Color(0xFF2EC4B6) else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    fun FeatureCapabilitiesGrid(sectionId: String, themeColor: Color) {
        val capabilities = when(sectionId) {
            "HABITS" -> listOf(
                Triple("Flexible Logic", "Create complex weekly schedules", Icons.Default.CalendarMonth),
                Triple("Streak Engine", "Visualize and maintain momentum", Icons.Default.Timeline),
                Triple("Progress Analytics", "Track completion in real-time", Icons.Default.BarChart),
                Triple("Theme Engine", "Personalize icons and colors", Icons.Default.Palette)
            )
            "WORKOUTS" -> listOf(
                Triple("Smart Timer", "Precision rest periods between sets", Icons.Default.Timer),
                Triple("Performance Tracking", "Log every rep and duration", Icons.Default.History),
                Triple("Muscle Balance", "Analyze growth across body parts", Icons.Default.Accessibility),
                Triple("Calorie Burn", "Automatic activity estimation", Icons.Default.LocalFireDepartment)
            )
            "TASKS" -> listOf(
                Triple("Priority 2.0", "Intelligent sorting by urgency", Icons.Default.PriorityHigh),
                Triple("Subtask Tree", "Break goals into manageable steps", Icons.Default.AccountTree),
                Triple("Universal Search", "Find any task instantly", Icons.Default.Search),
                Triple("Smart Reminders", "Never miss a deadline", Icons.Default.NotificationsActive)
            )
            "NOTES" -> listOf(
                Triple("AI Templates", "Pre-filled structured logs", Icons.Default.Description),
                Triple("Auto-Cleanup", "Keep your canvas lean", Icons.Default.DeleteSweep),
                Triple("Voice Sync", "Note-taking via speech", Icons.Default.Mic),
                Triple("Privacy Vault", "Hide sensitive information", Icons.Default.Lock)
            )
            "PROJECTS" -> listOf(
                Triple("Roadmaps", "Visual feature planning", Icons.Default.Map),
                Triple("Visual Boards", "Kanban-style project tracking", Icons.Default.Dashboard),
                Triple("Change History", "Log structural modifications", Icons.Default.Update),
                Triple("Deadline Alerts", "Stay ahead of project delivery", Icons.Default.Alarm)
            )
            "FINANCE" -> listOf(
                Triple("Safe-Spend", "Real-time daily budget calculation", Icons.Default.MonetizationOn),
                Triple("Vault Ledger", "Strict transaction logging", Icons.Default.Security),
                Triple("Monthly Goals", "Track savings targets", Icons.Default.Flag),
                Triple("Spending Trends", "Analyze performance over 7 days", Icons.AutoMirrored.Filled.TrendingUp)
            )
            else -> emptyList()
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            capabilities.forEach { (title, desc, icon) ->
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(32.dp).background(themeColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = themeColor, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(desc, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ActivationPage(themeColor: Color) {
        var startAnim by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            if (startAnim) 1f else 0.5f, 
            tween(1000, easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)), 
            label = "s"
        )
        val alpha by animateFloatAsState(if (startAnim) 1f else 0f, tween(1000), label = "a")
        
        LaunchedEffect(Unit) { startAnim = true }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)) {
                Surface(modifier = Modifier.size(160.dp), shape = CircleShape, color = themeColor.copy(alpha = 0.05f)) {}
                Surface(modifier = Modifier.size(120.dp), shape = CircleShape, color = themeColor.copy(alpha = 0.1f), border = BorderStroke(2.dp, themeColor.copy(alpha = 0.3f))) {}
                Icon(Icons.Default.RocketLaunch, null, tint = themeColor, modifier = Modifier.size(80.dp))
            }
            Spacer(modifier = Modifier.height(48.dp))
            Text("Engine Activated", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            Text("Your personal ecosystem is now online.", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }

    @Composable
    fun OnboardingFooter(pagerState: PagerState, themeColor: Color, canJump: Boolean, onDotClick: (Int) -> Unit) {
        Column(modifier = Modifier.padding(top = 8.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row {
                    repeat(pagerState.pageCount) { i ->
                        val isSelected = pagerState.currentPage == i
                        val width by animateDpAsState(if (isSelected) 32.dp else 8.dp, spring(stiffness = Spring.StiffnessMediumLow), label = "w")
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(if (isSelected) themeColor else Color.White.copy(alpha = 0.1f))
                                .clickable(enabled = canJump) { onDotClick(i) }
                        )
                    }
                }
            }
        }
    }

    private fun completeOnboarding(name: String, avatarRes: Int, sections: List<OnboardingSection>) {
        DataManager.userName = name
        DataManager.userAvatarRes = avatarRes
        
        // Visibility
        DataManager.showHabitSection = sections.find { it.id == "HABITS" }?.isEnabled?.value ?: true
        DataManager.showWorkoutSection = sections.find { it.id == "WORKOUTS" }?.isEnabled?.value ?: true
        DataManager.showTaskSection = sections.find { it.id == "TASKS" }?.isEnabled?.value ?: true
        DataManager.showNoteSection = sections.find { it.id == "NOTES" }?.isEnabled?.value ?: true
        DataManager.showProjectSection = sections.find { it.id == "PROJECTS" }?.isEnabled?.value ?: true
        DataManager.showFinanceSection = sections.find { it.id == "FINANCE" }?.isEnabled?.value ?: true

        // Sub-features mapping
        sections.find { it.id == "NOTES" }?.let { s ->
            DataManager.noteVisibleSections.clear()
            if (s.subFeatures.find { it.id == "NOTES" }?.isEnabled?.value == true) DataManager.noteVisibleSections.add("Notes")
            if (s.subFeatures.find { it.id == "QUESTIONS" }?.isEnabled?.value == true) DataManager.noteVisibleSections.add("Questions")
            if (s.subFeatures.find { it.id == "DAILY" }?.isEnabled?.value == true) DataManager.noteVisibleSections.add("Daily")
            if (s.subFeatures.find { it.id == "STORIES" }?.isEnabled?.value == true) DataManager.noteVisibleSections.add("Stories")
        }

        sections.find { it.id == "TASKS" }?.let { s ->
            DataManager.taskVisibleSections.clear()
            if (s.subFeatures.find { it.id == "TASKS" }?.isEnabled?.value == true) DataManager.taskVisibleSections.add("Tasks")
            if (s.subFeatures.find { it.id == "LIST" }?.isEnabled?.value == true) DataManager.taskVisibleSections.add("List")
        }
        
        sections.find { it.id == "PROJECTS" }?.let { s ->
            DataManager.projectRoadmapsEnabled = s.subFeatures.find { it.id == "ROADMAPS" }?.isEnabled?.value ?: true
            DataManager.projectIdeasEnabled = s.subFeatures.find { it.id == "IDEAS" }?.isEnabled?.value ?: true
        }

        DataManager.isOnboardingCompleted = true
        DataManager.saveData(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
