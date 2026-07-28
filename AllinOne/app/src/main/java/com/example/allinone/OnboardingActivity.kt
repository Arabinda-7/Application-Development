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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import com.example.allinone.onboarding.*
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
                OnboardingSection("HABITS", "Habit Tracker", "Daily rituals and streaks", Icons.Default.SelfImprovement, mutableStateOf(true), emptyList()),
                OnboardingSection("WORKOUTS", "Workouts", "Fitness and progression", Icons.Default.FitnessCenter, mutableStateOf(true), emptyList()),
                OnboardingSection("TASKS", "To-Do List", "Tasks and prioritization", Icons.Default.Checklist, mutableStateOf(true), listOf(SubFeatureConfig("list", "Tasks List", mutableStateOf(true)))),
                OnboardingSection("NOTES", "Notes", "Writing and templates", Icons.Default.Description, mutableStateOf(true), listOf(
                    SubFeatureConfig("daily", "Daily Logs", mutableStateOf(false)),
                    SubFeatureConfig("questions", "Questions", mutableStateOf(false)),
                    SubFeatureConfig("stories", "Stories", mutableStateOf(false))
                )),
                OnboardingSection("PROJECTS", "Projects", "Roadmaps and milestones", Icons.Default.AccountTree, mutableStateOf(true), listOf(SubFeatureConfig("ideas", "Ideas", mutableStateOf(true)))),
                OnboardingSection("FINANCE", "Finance", "Budget and savings", Icons.Default.AccountBalanceWallet, mutableStateOf(true), emptyList())
            )
        }

        val isNameFilled = userName.value.isNotBlank()

        val pages = remember(sections.map { it.isEnabled.value }, isNameFilled) {
            val list = mutableListOf(OnboardingPageType.OVERVIEW, OnboardingPageType.PROFILE)
            
            // Only add subsequent pages if name is filled
            if (isNameFilled) {
                list.add(OnboardingPageType.GLOBAL_HUB)
                sections.forEach { if (it.isEnabled.value) list.add(OnboardingPageType.FEATURE_DEEP_DIVE) }
                list.add(OnboardingPageType.ACTIVATION)
            }
            list
        }

        val pagerState = rememberPagerState(pageCount = { pages.size })
        val profilePageIndex = pages.indexOf(OnboardingPageType.PROFILE)

        Box(modifier = Modifier.fillMaxSize()) {
            val scrollOffset = pagerState.currentPage + pagerState.currentPageOffsetFraction
            LiquidBackground(accentColor, scrollOffset)
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true
            ) { index ->
                val pageType = pages[index]
                Box(modifier = Modifier.graphicsLayer {
                    val pageOffset = (
                            (pagerState.currentPage - index) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    alpha = lerp(
                        start = 0.4f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                    
                    val scale = lerp(
                        start = 0.85f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                    scaleX = scale
                    scaleY = scale
                }) {
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
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.BottomCenter) {
                OnboardingFooter(
                    pagerState = pagerState,
                    accentColor = accentColor,
                    isLastPage = pagerState.currentPage == pages.size - 1,
                    isNextEnabled = if (pagerState.currentPage == profilePageIndex) isNameFilled else true,
                    onDotClick = { target ->
                        val canJump = target <= profilePageIndex || isNameFilled
                        if (canJump) {
                            scope.launch { pagerState.animateScrollToPage(target) }
                        }
                    }
                ) { current ->
                    if (current < pages.size - 1) {
                        if (current == profilePageIndex && !isNameFilled) {
                            // Validation failed, don't move
                        } else {
                            scope.launch { pagerState.animateScrollToPage(current + 1) }
                        }
                    } else {
                        completeOnboarding(userName.value, selectedAvatar.intValue, sections)
                    }
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
                "NOTES" -> {
                    DataManager.showNoteSection = section.isEnabled.value
                    if (section.isEnabled.value) {
                        val visibleSections = mutableListOf("Notes")
                        section.subFeatures.forEach { sub ->
                            when (sub.id) {
                                "daily" -> if (sub.isEnabled.value) visibleSections.add("Daily")
                                "questions" -> if (sub.isEnabled.value) visibleSections.add("Questions")
                                "stories" -> if (sub.isEnabled.value) visibleSections.add("Stories")
                            }
                        }
                        DataManager.noteVisibleSections = visibleSections
                    }
                }
                "PROJECTS" -> {
                    DataManager.showProjectSection = section.isEnabled.value
                    if (section.isEnabled.value) {
                        section.subFeatures.forEach { sub ->
                            if (sub.id == "ideas") DataManager.projectIdeasEnabled = sub.isEnabled.value
                        }
                    }
                }
                "FINANCE" -> DataManager.showFinanceSection = section.isEnabled.value
            }
        }
        DataManager.isOnboardingCompleted = true
        DataManager.saveData(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
