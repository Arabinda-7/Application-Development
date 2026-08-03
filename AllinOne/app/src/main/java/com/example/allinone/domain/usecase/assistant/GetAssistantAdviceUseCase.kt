package com.example.allinone.domain.usecase.assistant

import javax.inject.Inject

class GetAssistantAdviceUseCase @Inject constructor() {

    fun getGrowthAdvice(mood: String?): String {
        return when (mood) {
            "🔥" -> "You're unstoppable! Channel this fire into your most ambitious projects."
            "⚡" -> "High energy detected! Perfect time for a high-intensity workout or deep work."
            "🧘" -> "Maintaining zen. Focus on quality over quantity today."
            "💼" -> "Execution mode engaged. Your discipline today builds tomorrow's success."
            "😴" -> "Recovery is essential. Listen to your body and prioritize rest."
            "🧠" -> "Deep focus state. Great for learning new skills or solving complex problems."
            else -> "Consistency is the key to long-term growth. Keep showing up!"
        }
    }

    fun getManagementAdvice(mood: String?): String {
        return when (mood) {
            "🔥" -> "Prioritize high-impact tasks while your momentum is at its peak."
            "⚡" -> "Clear your backlog. Your speed today is an asset."
            "🧘" -> "Review your long-term roadmap. Is your current path still the right one?"
            "💼" -> "Systematize your routine. Efficiency is your current superpower."
            "😴" -> "Keep it simple. Handle only the essential tasks today."
            "🧠" -> "Organize your knowledge base. Connect the dots between your projects."
            else -> "Organize your workflow and prioritize your high-impact tasks."
        }
    }
}
