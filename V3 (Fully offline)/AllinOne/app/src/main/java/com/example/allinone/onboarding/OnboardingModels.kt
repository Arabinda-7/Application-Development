package com.example.allinone.onboarding

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector

enum class OnboardingPageType {
    PROFILE, OVERVIEW, GLOBAL_HUB, FEATURE_DEEP_DIVE, AI_INTRO, ACTIVATION
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
    val subFeatures: List<SubFeatureConfig>
)
