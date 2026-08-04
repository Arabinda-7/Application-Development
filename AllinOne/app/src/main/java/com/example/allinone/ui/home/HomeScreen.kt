package com.example.allinone.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.allinone.core.utils.UIUtils
import com.example.allinone.*
import com.example.allinone.data.model.*
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.ui.home.components.*
import java.text.SimpleDateFormat
import java.util.*

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
    onNavigateToAssistant: () -> Unit,
    onNavigateToWorkspace: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPerformanceHistory: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onQuickAddTodo: () -> Unit = {},
    onQuickAddExpense: () -> Unit = {},
    onQuickAddNote: () -> Unit = {},
    onColorSelected: (String, Int) -> Unit = { _, _ -> },
    onMoodSelected: (String) -> Unit = {},
    onSearchRequested: (String) -> Unit = {},
    onNotificationsMarkedAsViewed: (String) -> Unit = {},
    onVoiceAssistantRequested: () -> Unit = {}
) {
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSpeedDial by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val style = LocalAppStyle.current
    val scrollState = rememberScrollState()

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
        if (state.currentMood != null && moodTheme.second.isNotEmpty()) moodTheme.second
        else "$timePrefix, ${if (state.overallProgress >= 70) "Crushing it! 🔥" else if (state.overallProgress >= 30) "Great start! ⚡" else "Ready?"}"
    }

    val footerHeightPx = with(LocalDensity.current) { 83.dp.toPx() }
    val fabOffset by remember(scrollState.value, scrollState.maxValue) {
        derivedStateOf {
            if (scrollState.maxValue > 0) {
                val distanceToBottom = scrollState.maxValue - scrollState.value
                if (distanceToBottom < footerHeightPx) (footerHeightPx - distanceToBottom).coerceAtLeast(0f) else 0f
            } else 0f
        }
    }

    val todayDateString = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            QuickActionsFab(
                showSpeedDial = showSpeedDial,
                onToggleSpeedDial = { showSpeedDial = !showSpeedDial },
                onQuickAddTodo = { onQuickAddTodo(); showSpeedDial = false },
                onQuickAddExpense = { onQuickAddExpense(); showSpeedDial = false },
                onQuickAddNote = { onQuickAddNote(); showSpeedDial = false },
                offsetY = fabOffset
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
                .padding(padding)
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { keyboardController?.hide() }) }
                    .verticalScroll(scrollState)
            ) {
                Column(modifier = Modifier.heightIn(min = minHeight)) {
                    HomeHeader(
                        state = state,
                        isSearchVisible = isSearchVisible,
                        onSearchToggle = { isSearchVisible = !isSearchVisible },
                        onNotificationsClick = { 
                            showNotificationsDialog = true
                            onNotificationsMarkedAsViewed(todayDateString)
                        },
                        onProfileClick = onNavigateToProfile,
                        onMoodSelected = onMoodSelected,
                        onSearchRequested = onSearchRequested,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        moodTheme = moodTheme,
                        smartGreeting = smartGreeting,
                        showRedDot = state.todayAgenda.isNotEmpty() && (state.lastViewedNotificationDate != todayDateString || state.hasNewTodayNotifications)
                    )

                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        ExecutiveSummaryCard(
                            overallProgress = state.overallProgress,
                            safeSpendAmount = state.safeSpendAmount,
                            showPerformance = state.showPerformanceSection,
                            showFinance = state.showFinanceSection,
                            onPerformanceClick = onNavigateToPerformanceHistory
                        )

                        PulseActivitySection(recentActions = state.recentActions)

                        GrowthDisciplineSection(
                            showHabit = state.showHabitSection,
                            showWorkout = state.showWorkoutSection,
                            habitProgress = state.habitProgress,
                            workoutProgress = state.workoutProgress,
                            habitColor = state.habitColor,
                            workoutColor = state.workoutColor,
                            habitIcon = state.habitIcon,
                            workoutIcon = state.workoutIcon,
                            auraAlpha = auraAlpha,
                            onHabitClick = onNavigateToHabits,
                            onWorkoutClick = onNavigateToWorkout,
                            onHabitColorClick = { showColorPicker = "HABIT" },
                            onWorkoutColorClick = { showColorPicker = "WORKOUT" }
                        )

                        if (state.currentMood != null && (state.showHabitSection || state.showWorkoutSection)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AdviceBanner(text = state.growthAdvice, emoji = "✨")
                        }

                        ManagementNotesSection(
                            showTask = state.showTaskSection,
                            showNote = state.showNoteSection,
                            showProject = state.showProjectSection,
                            showFinance = state.showFinanceSection,
                            safeSpendAmount = state.safeSpendAmount,
                            taskColor = state.taskColor,
                            noteColor = state.noteColor,
                            projectColor = state.projectColor,
                            financeColor = state.financeColor,
                            taskIcon = state.taskIcon,
                            noteIcon = state.noteIcon,
                            projectIcon = state.projectIcon,
                            financeIcon = state.financeIcon,
                            auraAlpha = auraAlpha,
                            onTaskClick = onNavigateToTodos,
                            onNoteClick = onNavigateToNotes,
                            onProjectClick = onNavigateToProjects,
                            onFinanceClick = onNavigateToFinance,
                            onTaskColorClick = { showColorPicker = "TASK" },
                            onNoteColorClick = { showColorPicker = "NOTE" },
                            onProjectColorClick = { showColorPicker = "PROJECT" },
                            onFinanceColorClick = { showColorPicker = "FINANCE" }
                        )

                        if (state.currentMood != null && (state.showTaskSection || state.showNoteSection || state.showProjectSection || state.showFinanceSection)) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AdviceBanner(text = state.managementAdvice, icon = Icons.Default.Star, borderColor = Color(0xFF2EC4B6), backgroundColor = Color(0xFF2EC4B6).copy(alpha = 0.1f))
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(32.dp))

                    HomeFooter(
                        selectedTab = state.selectedTab,
                        isAiEnabled = state.isAiAssistantEnabled,
                        onTabSelected = onTabSelected,
                        onNavigateToAssistant = onNavigateToAssistant,
                        onNavigateToSettings = onNavigateToSettings,
                        onVoiceAssistantRequested = onVoiceAssistantRequested
                    )
                }
            }
        }

        if (showColorPicker != null) {
            ColorPickerDialog(
                section = showColorPicker!!,
                onColorSelected = onColorSelected,
                onDismiss = { showColorPicker = null }
            )
        }

        if (showNotificationsDialog) {
            AgendaDialog(
                agenda = state.todayAgenda,
                onDismiss = { showNotificationsDialog = false },
                onNavigateToTodos = onNavigateToTodos,
                onNavigateToWorkspace = onNavigateToWorkspace,
                onNavigateToNotes = onNavigateToNotes
            )
        }
    }
}
