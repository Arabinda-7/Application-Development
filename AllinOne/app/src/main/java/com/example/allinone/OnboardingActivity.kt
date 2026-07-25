package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.allinone.onboarding.*
import kotlinx.coroutines.launch

class OnboardingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OnboardingFlow() }
    }

    @Composable
    fun OnboardingFlow() {
        val scope = rememberCoroutineScope()
        val accentColor = Color(0xFF1A73E8)
        
        val userName = remember { mutableStateOf("") }
        val selectedAvatar = remember { mutableIntStateOf(R.drawable.boy_avatar_profile) }
        val selectedRoles = remember { mutableStateOf(setOf<String>()) }

        val sections = remember {
            listOf(
                OnboardingSection("HABITS", "Habit Tracker", "Daily rituals and streaks", Icons.Default.SelfImprovement, mutableStateOf(true), listOf(SubFeatureConfig("streaks", "Streaks", mutableStateOf(true)), SubFeatureConfig("aura", "Aura Themes", mutableStateOf(true)))),
                OnboardingSection("WORKOUTS", "Workouts", "Fitness and progression", Icons.Default.FitnessCenter, mutableStateOf(true), listOf(SubFeatureConfig("timer", "Timer", mutableStateOf(true)), SubFeatureConfig("muscle", "Muscle Balance", mutableStateOf(true)))),
                OnboardingSection("TASKS", "To-Do List", "Tasks and prioritization", Icons.Default.Checklist, mutableStateOf(true), listOf(SubFeatureConfig("reminders", "Reminders", mutableStateOf(true)))),
                OnboardingSection("NOTES", "Notes", "Writing and templates", Icons.Default.Description, mutableStateOf(true), listOf(SubFeatureConfig("voice", "Voice Input", mutableStateOf(true)))),
                OnboardingSection("PROJECTS", "Projects", "Roadmaps and milestones", Icons.Default.AccountTree, mutableStateOf(true), listOf(SubFeatureConfig("ideas", "Ideas", mutableStateOf(true)))),
                OnboardingSection("FINANCE", "Finance", "Budget and savings", Icons.Default.AccountBalanceWallet, mutableStateOf(true), listOf(SubFeatureConfig("heatmap", "Heatmaps", mutableStateOf(true))))
            )
        }

        val pages = remember(sections.map { it.isEnabled.value }) {
            val list = mutableListOf(OnboardingPageType.OVERVIEW, OnboardingPageType.PROFILE, OnboardingPageType.GLOBAL_HUB)
            sections.forEach { if (it.isEnabled.value) list.add(OnboardingPageType.FEATURE_DEEP_DIVE) }
            list.add(OnboardingPageType.ACTIVATION)
            list
        }

        val pagerState = rememberPagerState(pageCount = { pages.size })

        Box(modifier = Modifier.fillMaxSize()) {
            LiquidBackground(accentColor)
            
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { index ->
                val pageType = pages[index]
                when (pageType) {
                    OnboardingPageType.OVERVIEW -> OverviewPage(accentColor)
                    OnboardingPageType.PROFILE -> ProfilePage(userName, selectedAvatar, selectedRoles, accentColor)
                    OnboardingPageType.GLOBAL_HUB -> GlobalHubPage(sections, accentColor)
                    OnboardingPageType.FEATURE_DEEP_DIVE -> {
                        // Find which enabled section this deep dive corresponds to
                        val hubIndex = pages.indexOf(OnboardingPageType.GLOBAL_HUB)
                        val enabledSections = sections.filter { it.isEnabled.value }
                        val sectionIndex = index - hubIndex - 1
                        if (sectionIndex in enabledSections.indices) {
                            FeatureDeepDivePage(enabledSections[sectionIndex], accentColor)
                        }
                    }
                    OnboardingPageType.ACTIVATION -> ActivationPage(accentColor)
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
                OnboardingFooter(pagerState, accentColor, pagerState.currentPage == pages.size - 1) { current ->
                    if (current < pages.size - 1) scope.launch { pagerState.animateScrollToPage(current + 1) }
                    else completeOnboarding(userName.value, selectedAvatar.intValue, sections)
                }
            }
        }
    }

    private fun completeOnboarding(name: String, avatar: Int, sections: List<OnboardingSection>) {
        DataManager.userName = if (name.isBlank()) "User" else name
        DataManager.userAvatarRes = avatar
        sections.forEach { section ->
            when (section.id) {
                "HABITS" -> DataManager.showHabitSection = section.isEnabled.value
                "WORKOUTS" -> DataManager.showWorkoutSection = section.isEnabled.value
                "TASKS" -> DataManager.showTaskSection = section.isEnabled.value
                "NOTES" -> DataManager.showNoteSection = section.isEnabled.value
                "PROJECTS" -> DataManager.showProjectSection = section.isEnabled.value
                "FINANCE" -> DataManager.showFinanceSection = section.isEnabled.value
            }
        }
        DataManager.isOnboardingCompleted = true
        DataManager.saveData(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
