package com.example.allinone.assistant.question

import com.example.allinone.assistant.nlu.ConfidenceTier
import com.example.allinone.assistant.nlu.IntentResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowUpQuestionEngine @Inject constructor() {

    fun generateClarification(intentResult: IntentResult): String {
        return when (intentResult.confidenceTier) {
            ConfidenceTier.LOW, ConfidenceTier.UNKNOWN ->
                "I'm not quite sure what you'd like to do. Would you like to create a task, track a workout, or add a note?"
            ConfidenceTier.MEDIUM ->
                "Did you mean to execute '${intentResult.intentName}'?"
            ConfidenceTier.HIGH -> {
                if (!intentResult.extractedParameters.containsKey("title")) {
                    "What title would you like to give to this ${intentResult.intentName}?"
                } else {
                    "Processing your ${intentResult.intentName} command."
                }
            }
        }
    }
}
