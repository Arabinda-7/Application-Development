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
import androidx.lifecycle.lifecycleScope
import com.example.allinone.onboarding.*
import kotlinx.coroutines.flow.first
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
        val isAiEnabled = remember { mutableStateOf(false) }

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
                list.add(OnboardingPageType.AI_INTRO)
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
                        OnboardingPageType.AI_INTRO -> AIIntroPage(isAiEnabled, accentColor)
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
                        completeOnboarding(userName.value, selectedAvatar.intValue, sections, isAiEnabled.value)
                    }
                }
            }
        }
    }

    private fun completeOnboarding(name: String, avatar: Int, sections: List<OnboardingSection>, aiEnabled: Boolean) {
        val entryPoint = DataManager.getEntryPoint(this)
        lifecycleScope.launch {
            try {
                // Update Profile
                val currentProfile = entryPoint.userRepository().getUserProfile().first()
                entryPoint.userRepository().updateUserProfile(currentProfile.copy(
                    name = if (name.isBlank()) "User" else name,
                    avatarRes = avatar
                ))

                // Update Settings
                val currentSettings = entryPoint.userRepository().getUserSettings().first()
                entryPoint.userRepository().updateUserSettings(currentSettings.copy(
                    isOnboardingCompleted = true,
                    isAiAssistantEnabled = aiEnabled,
                    showHabitSection = sections.find { it.id == "HABITS" }?.isEnabled?.value ?: true,
                    showWorkoutSection = sections.find { it.id == "WORKOUTS" }?.isEnabled?.value ?: true,
                    showTaskSection = sections.find { it.id == "TASKS" }?.isEnabled?.value ?: true,
                    showNoteSection = sections.find { it.id == "NOTES" }?.isEnabled?.value ?: true,
                    showProjectSection = sections.find { it.id == "PROJECTS" }?.isEnabled?.value ?: true,
                    showFinanceSection = sections.find { it.id == "FINANCE" }?.isEnabled?.value ?: true
                ))
                
                // Force legacy sync for DataManager
                DataManager.refreshLegacyState(this@OnboardingActivity)
                
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            } catch (e: Exception) {
                startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
