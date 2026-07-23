package com.example.allinone

import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class AppStyle(
    val borderRadius: Dp = 16.dp,
    val accentColor: Color = Color(0xFF1A73E8),
    val surfaceColor: Color = Color(0xFF1A1A1A),
    val backgroundColor: Color = Color(0xFF121212),
    val isOled: Boolean = false,
    val showShadows: Boolean = true,
    val fontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.Default,
    val cardStyle: String = "GLASS"
)

val LocalAppStyle = staticCompositionLocalOf { AppStyle() }


@Composable
fun HomeScreen(
    state: DashboardState,
    onNavigateToHabits: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToTodos: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToWorkspace: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPerformanceHistory: () -> Unit = {},
    onQuickAddTodo: () -> Unit = {},
    onQuickAddExpense: () -> Unit = {},
    onQuickAddNote: () -> Unit = {},
    onColorSelected: (String, Int) -> Unit = { _, _ -> },
    onMoodSelected: (String) -> Unit = {},
    onSearchRequested: (String) -> Unit = {}
) {
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSpeedDial by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    
    // New Feature States
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showVoiceComingSoon by remember { mutableStateOf(false) }
    var isMessageExpanded by remember { mutableStateOf(false) }

    // Aura Animation
    val infiniteTransition = rememberInfiniteTransition(label = "Aura")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    // Keyboard Controller
    val keyboardController = LocalSoftwareKeyboardController.current
    val style = LocalAppStyle.current
    val interactionSource = remember { MutableInteractionSource() }

    val moodTheme = remember(state.currentMood, style.accentColor) {
        val colorInt = UIUtils.getMoodColor(state.currentMood, style.accentColor.toArgb())
        Color(colorInt) to UIUtils.getMoodMessage(state.currentMood)
    }

    val smartGreeting = remember(state.overallProgress, state.userName, state.currentMood) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timePrefix = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
        
        if (state.currentMood != null && moodTheme.second.isNotEmpty()) {
            moodTheme.second
        } else {
            val icon = when (hour) {
                in 5..11 -> "☕"
                in 12..16 -> "🚀"
                in 17..20 -> "🧘"
                else -> "🌙"
            }
            val milestone = when {
                state.overallProgress >= 100 -> "Elite Momentum! 🏆"
                state.overallProgress >= 70 -> "Crushing it! 🔥"
                state.overallProgress >= 30 -> "Great start! ⚡"
                else -> icon
            }
            "$timePrefix, $milestone"
        }
    }

    // Animation for Speed Dial
    val transition = updateTransition(targetState = showSpeedDial, label = "SpeedDial")
    val dialRotation by transition.animateFloat(label = "Rotation") { if (it) 45f else 0f }

    val todayDateString = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    val showRedDot = state.todayAgenda.isNotEmpty() && DataManager.lastViewedNotificationDate != todayDateString

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                // 1. New Task Shortcut (Moves LEFT)
                QuickActionItem(
                    label = "Task",
                    icon = Icons.Default.Add,
                    color = Color(0xFF2EC4B6),
                    isVisible = showSpeedDial,
                    offsetY = 0.dp,
                    offsetX = (-85).dp, // Adjusted for 50.dp FAB
                    onClick = { onQuickAddTodo(); showSpeedDial = false }
                )

                // 2. Add Expense Shortcut (Moves DIAGONAL)
                QuickActionItem(
                    label = "Cash",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFFE91E63),
                    isVisible = showSpeedDial,
                    offsetY = (-60).dp, // Adjusted for 50.dp FAB
                    offsetX = (-60).dp, // Adjusted for 50.dp FAB
                    onClick = { onQuickAddExpense(); showSpeedDial = false }
                )

                // 3. Quick Note Shortcut (Moves UP)
                QuickActionItem(
                    label = "Note",
                    icon = Icons.Default.Edit,
                    color = Color(0xFF3A86F0),
                    isVisible = showSpeedDial,
                    offsetY = (-85).dp, // Adjusted for 50.dp FAB
                    offsetX = 0.dp,
                    onClick = { onQuickAddNote(); showSpeedDial = false }
                )

                val context = LocalContext.current
                val config = LocalConfiguration.current
                val standardDensity = remember(context, config) { 
                    Density(
                        density = context.resources.displayMetrics.density,
                        fontScale = config.fontScale
                    )
                }

                CompositionLocalProvider(LocalDensity provides standardDensity) {
                    FloatingActionButton(
                        onClick = { showSpeedDial = !showSpeedDial },
                        containerColor = style.accentColor,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(50.dp) // Requested 50.dp
                    ) {
                        Icon(
                            imageVector = if (showSpeedDial) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Quick Action",
                            modifier = Modifier.size(26.dp).graphicsLayer(rotationZ = dialRotation) // Proportional to 50.dp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        isMessageExpanded = false
                    })
                }
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. Aura Header with Controls ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(moodTheme.first.copy(alpha = 0.6f), Color.Black)
                        )
                    )
                    .statusBarsPadding()
                    .padding(top = 0.dp, bottom = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Profile + Personal Greeting Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { isMessageExpanded = !isMessageExpanded },
                            horizontalAlignment = Alignment.Start
                        ) {
                            Box(
                                contentAlignment = Alignment.BottomEnd,
                                modifier = Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onNavigateToProfile(); isMessageExpanded = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(style.surfaceColor)
                                        .border(1.5.dp, moodTheme.first.copy(alpha = 0.4f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.userProfileImageUri != null) {
                                        val context = androidx.compose.ui.platform.LocalContext.current
                                        val bitmap = remember(state.userProfileImageUri) {
                                            try {
                                                context.contentResolver.openInputStream(Uri.parse(state.userProfileImageUri))?.use {
                                                    BitmapFactory.decodeStream(it)
                                                }
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Profile",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = state.userAvatarRes),
                                                contentDescription = "Profile",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        }
                                    } else {
                                        Image(
                                            painter = painterResource(id = state.userAvatarRes),
                                            contentDescription = "Profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                }
                                Box(modifier = Modifier.size(12.dp).background(Color(0xFF2EC4B6), CircleShape).border(2.dp, Color.Black, CircleShape))
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = if (state.currentMood != null) "Current Vibe" else UIUtils.formatTitleCase(smartGreeting.split(",")[0]), 
                                color = Color.White.copy(alpha = 0.4f), 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Medium, 
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (state.currentMood != null) UIUtils.formatTitleCase(smartGreeting) else UIUtils.formatTitleCase(state.userName),
                                color = Color.White, 
                                fontSize = 24.sp, 
                                fontWeight = FontWeight.Black, 
                                letterSpacing = (-0.5).sp,
                                maxLines = if (isMessageExpanded) Int.MAX_VALUE else 1,
                                overflow = if (isMessageExpanded) androidx.compose.ui.text.style.TextOverflow.Visible else androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            
                            val formattedName = UIUtils.formatTitleCase(state.userName)
                            val rawMilestone = smartGreeting.split(",").getOrNull(1)?.trim() ?: ""
                            val milestoneText = if (state.currentMood != null) formattedName else UIUtils.formatTitleCase(rawMilestone)
                            if (milestoneText.length > 2) { // Ensure it's more than just an emoji
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (state.currentMood != null) "Active: $formattedName" else milestoneText,
                                    color = moodTheme.first,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Refined Action Row (Search Toggle + Notifications + Settings)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { isSearchVisible = !isSearchVisible; isMessageExpanded = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Toggle Search",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { 
                                        showNotificationsDialog = true
                                        DataManager.lastViewedNotificationDate = todayDateString
                                        DataManager.saveData(null)
                                        isMessageExpanded = false 
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                if (showRedDot) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.Red, CircleShape)
                                            .border(1.5.dp, Color.Black, CircleShape)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (2).dp, y = (-2).dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onNavigateToSettings(); isMessageExpanded = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // --- 2. Executive Command Bar (Etched Aesthetic - Optional) ---
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSearchVisible,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search your ecosystem...", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = style.accentColor.copy(alpha = 0.4f),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                                    focusedContainerColor = Color.White.copy(alpha = 0.03f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                                    cursorColor = style.accentColor,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(18.dp)) },
                                trailingIcon = { 
                                    IconButton(onClick = { 
                                        if (searchQuery.isNotEmpty()) {
                                            onSearchRequested(searchQuery)
                                            keyboardController?.hide()
                                        } else {
                                            showVoiceComingSoon = true 
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (searchQuery.isEmpty()) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send, 
                                            "Action", 
                                            tint = style.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    if (searchQuery.isNotEmpty()) {
                                        onSearchRequested(searchQuery)
                                        keyboardController?.hide()
                                    }
                                }),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 3. Sentiment Tracker (Mood Log) Header ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Focus", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(state.dateString, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Mood Icons Row
                    val moodDensityValue = remember(state.isSystemAppearanceEnabled, state.globalDisplaySize, state.homeDisplaySize, state.homeFocusSize) {
                        UIUtils.getIsolatedMoodDensity(state)
                    }
                    // Isolated from app-level font scaling as well
                    val systemFontScale = android.content.res.Resources.getSystem().configuration.fontScale
                    val moodDensity = remember(moodDensityValue, systemFontScale) {
                        Density(density = moodDensityValue, fontScale = systemFontScale)
                    }
                    
                    CompositionLocalProvider(LocalDensity provides moodDensity) {
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val moods = listOf("🔥", "⚡", "🧘", "💼", "😴", "🧠")
                            val circleSize = 50.dp
                            val emojiSize = 22.sp

                            items(moods.size) { index ->
                                val mood = moods[index]
                                val isSelected = state.currentMood == mood
                                val backgroundColor = if (isSelected) {
                                    Color(UIUtils.darkenColor(style.accentColor.toArgb(), 0.5f))
                                } else {
                                    style.surfaceColor
                                }

                                Box(
                                    modifier = Modifier
                                        .size(circleSize)
                                        .clip(CircleShape)
                                        .background(backgroundColor)
                                        .clickable { onMoodSelected(mood); isMessageExpanded = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(mood, fontSize = emojiSize)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. Executive Summary Card (Safe Spend & Performance) ---
            val showExecutiveCard = state.showHabitSection || state.showWorkoutSection || state.showFinanceSection
            if (showExecutiveCard) {
                Card(
                    shape = RoundedCornerShape(style.borderRadius),
                    colors = CardDefaults.cardColors(containerColor = style.surfaceColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.showHabitSection || state.showWorkoutSection) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Daily Performance", 
                                        color = style.accentColor, 
                                        fontSize = 10.sp, 
                                        fontWeight = FontWeight.Black, 
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.clickable { onNavigateToPerformanceHistory() }
                                    )
                                    Text("${state.overallProgress}% Completed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            
                            if (state.showFinanceSection) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Safe Spend", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(String.format(Locale.getDefault(), "₹%.0f", state.safeSpendAmount), color = Color(0xFF2EC4B6), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        
                        if (state.showHabitSection || state.showWorkoutSection) {
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { state.overallProgress / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = style.accentColor,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // --- 5. Pulse Activity Feed (Motivational Mindset) ---
            if (state.recentActions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Pulse Activity", modifier = Modifier.padding(horizontal = 20.dp), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Recent Actions History (Shown Finished)
                    items(state.recentActions) { action ->
                        Surface(color = style.surfaceColor, shape = RoundedCornerShape(12.dp), modifier = Modifier.height(40.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(style.accentColor.copy(alpha = 0.5f), CircleShape))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(action, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // --- 7-12. Diversified Growth & Management Sections ---
            if (state.showHabitSection || state.showWorkoutSection) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Growth & Discipline",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                DashboardPair(
                    item1 = if (state.showHabitSection) {
                        {
                            HabitCard(
                                progress = state.habitProgress,
                                color = Color(if (state.habitColor == -1) 0xFFFF7A59 else state.habitColor.toLong()),
                                icon = state.habitIcon,
                                onClick = { onNavigateToHabits(); isMessageExpanded = false },
                                onColorClick = { showColorPicker = "HABIT" },
                                auraAlpha = auraAlpha)
                        }
                    } else null,
                    item2 = if (state.showWorkoutSection) {
                        {
                            WorkoutCard(
                                progress = state.workoutProgress,
                                color = Color(if (state.workoutColor == -1) 0xFFFFB800 else state.workoutColor.toLong()),
                                icon = state.workoutIcon,
                                onClick = { onNavigateToWorkout(); isMessageExpanded = false },
                                onColorClick = { showColorPicker = "WORKOUT" },
                                auraAlpha = auraAlpha)
                        }
                    } else null
                )
            }

            // --- Growth Advice (Blue Card) ---
            if (state.currentMood != null && (state.showHabitSection || state.showWorkoutSection)) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = style.accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(style.borderRadius),
                    border = BorderStroke(0.5.dp, style.accentColor.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp).clickable { isMessageExpanded = false }, verticalAlignment = Alignment.CenterVertically) {
                        Text("✨", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = state.growthAdvice,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            if (state.showTaskSection || state.showNoteSection || state.showProjectSection || state.showFinanceSection) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Management & Notes",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                DashboardPair(
                    item1 = if (state.showTaskSection) {
                        {
                            TaskCard(
                                color = Color(if (state.taskColor == -1) 0xFF2EC4B6 else state.taskColor.toLong()),
                                icon = state.taskIcon,
                                onClick = { onNavigateToTodos(); isMessageExpanded = false },
                                onColorClick = { showColorPicker = "TASK" },
                                auraAlpha = auraAlpha)
                        }
                    } else null,
                    item2 = if (state.showNoteSection) {
                        {
                            NoteCard(
                                color = Color(if (state.noteColor == -1) 0xFF3A86F0 else state.noteColor.toLong()),
                                icon = state.noteIcon,
                                onClick = { onNavigateToNotes(); isMessageExpanded = false },
                                onColorClick = { showColorPicker = "NOTE" },
                                auraAlpha = auraAlpha)
                        }
                    } else null
                )

                if ((state.showTaskSection || state.showNoteSection) && (state.showProjectSection || state.showFinanceSection)) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                DashboardPair(
                    item1 = if (state.showProjectSection) {
                        {
                            ProjectCard(
                                color = Color(if (state.projectColor == -1) 0xFF1A73E8 else state.projectColor.toLong()),
                                icon = state.projectIcon,
                                onClick = { onNavigateToProjects(); isMessageExpanded = false },
                                onColorClick = { showColorPicker = "PROJECT" },
                                auraAlpha = auraAlpha)
                        }
                    } else null,
                    item2 = if (state.showFinanceSection) {
                        {
                            FinanceCard(
                                amount = state.safeSpendAmount,
                                color = Color(if (state.financeColor == -1) 0xFFE91E63 else state.financeColor.toLong()),
                                icon = state.financeIcon,
                                onClick = { onNavigateToFinance(); isMessageExpanded = false },
                                onColorClick = { showColorPicker = "FINANCE" },
                                auraAlpha = auraAlpha)
                        }
                    } else null
                )
            }

            // --- Management Advice (Green Card) ---
            if (state.currentMood != null && (state.showTaskSection || state.showNoteSection || state.showProjectSection || state.showFinanceSection)) {
                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFF2EC4B6).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF2EC4B6).copy(alpha = 0.2f)),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFF2EC4B6), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = state.managementAdvice, 
                            color = Color.White.copy(alpha = 0.8f), 
                            fontSize = 11.sp, 
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- Overlay Dialogs ---
    if (showColorPicker != null) {
        AlertDialog(
            onDismissRequest = { showColorPicker = null },
            title = { Text("Choose Theme Color", color = Color.White) },
            containerColor = Color(0xFF1A1A1A),
            text = {
                val colors = listOf(0xFFFF7A59, 0xFFFFB800, 0xFF2EC4B6, 0xFF3A86F0, 0xFF1A73E8, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF4CAF50)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    colors.forEach { colorInt ->
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(colorInt)).clickable { onColorSelected(showColorPicker!!, colorInt.toInt()); showColorPicker = null }.border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showColorPicker = null }) { Text("CLOSE", color = Color(0xFF1A73E8)) } }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Today's Agenda", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
            containerColor = Color(0xFF1A1A1A),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (state.todayAgenda.isEmpty()) {
                        Text("Your agenda is clear for today!", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    } else {
                        state.todayAgenda.forEach { (section, items) ->
                            Text(
                                text = section,
                                color = style.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            items.forEach { item ->
                                Surface(
                                    onClick = {
                                        when (item.navigationTarget) {
                                            "TASK_ACTIVITY" -> onNavigateToTodos()
                                            "PROJECT_ACTIVITY" -> onNavigateToProjects()
                                            "WORKSPACE" -> onNavigateToWorkspace()
                                            "NOTE_ACTIVITY" -> onNavigateToNotes()
                                        }
                                        showNotificationsDialog = false
                                    },
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(6.dp).background(style.accentColor, CircleShape))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(text = item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if (item.details.isNotEmpty()) {
                                            Text(
                                                text = item.details,
                                                color = Color.White.copy(alpha = 0.5f),
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(start = 18.dp, top = 2.dp)
                                            )
                                        }
                                        if (item.path.isNotEmpty()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(start = 18.dp, top = 4.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Place,
                                                    null,
                                                    tint = style.accentColor.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.path,
                                                    color = style.accentColor.copy(alpha = 0.7f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = { showNotificationsDialog = false }) { 
                    Text("DISMISS", color = Color(0xFF1A73E8), fontWeight = FontWeight.Bold) 
                } 
            }
        )
    }

    if (showVoiceComingSoon) {
        AlertDialog(
            onDismissRequest = { showVoiceComingSoon = false },
            containerColor = Color(0xFF1A1A1A),
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Mic, null, tint = Color(0xFF1A73E8), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Voice Assistant Coming Soon", color = Color.White, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text("We are currently training the AI to recognize your specific productivity commands.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            },
            confirmButton = { TextButton(onClick = { showVoiceComingSoon = false }) { Text("UNDERSTOOD", color = Color(0xFF1A73E8)) } }
        )
    }
}

// --- Specialized Section Cards ---

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
        Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            // Text Info at Top-Left
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text("HABITS", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                Text("Daily Rituals", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }

            // Icon at Bottom-Left
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp).align(Alignment.BottomStart)
            )

            // Progress Indicator at Bottom-Right
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

            // Color Picker at Top-Right
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
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NOTES", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Canvas", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
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
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(painter = painterResource(id = icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).border(1.dp, color, CircleShape).clickable { onColorClick() })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("VAULT", color = color, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.weight(1f))
            Text(String.format(Locale.getDefault(), "₹%.0f", amount), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, color: Color, isVisible: Boolean, offsetY: Dp, offsetX: Dp, onClick: () -> Unit) {
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
                        indication = null // Custom circle will handle visuals
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(44.dp) // Adjusted for 50.dp FAB
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp) // Proportional to 44.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        label,
                        color = Color.White,
                        fontSize = 10.sp, // Slightly larger for 44.dp item
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationItem(title: String, body: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(body, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
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
