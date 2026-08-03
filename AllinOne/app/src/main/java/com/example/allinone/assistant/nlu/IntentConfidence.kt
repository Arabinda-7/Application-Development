package com.example.allinone.assistant.nlu

enum class ConfidenceTier {
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}

data class IntentResult(
    val intentName: String,
    val confidenceScore: Float, // 0.0f to 1.0f
    val confidenceTier: ConfidenceTier,
    val extractedParameters: Map<String, Any> = emptyMap()
)
