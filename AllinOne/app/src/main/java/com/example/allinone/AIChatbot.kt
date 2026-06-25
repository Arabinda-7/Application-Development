package com.example.allinone

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AIChatbot(apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text("You are a personal dashboard assistant. Process natural language commands into JSON format. " +
                    "Return ONLY valid JSON. " +
                    "Supported Intents: ADD_EXPENSE, ADD_TASK, ADD_HABIT. " +
                    "Response Schema: { \"intent\": \"...\", \"parameters\": { ... } }")
        }
    )

    suspend fun processCommand(command: String): String? {
        return try {
            val response = model.generateContent(command)
            response.text
        } catch (e: Exception) {
            null
        }
    }
}
