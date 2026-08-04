package com.example.allinone.assistant.response

import android.content.Context
import android.util.Log
import com.example.allinone.assistant.model.ResponseItem
import com.example.allinone.DataManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantResponseProvider @Inject constructor() {

    private val categorizedResponses = mutableMapOf<String, MutableList<ResponseItem>>()
    private var isInitialized = false

    private val STOP_WORDS = setOf(
        "a", "an", "the", "and", "or", "but", "if", "then", "so", "for", "with", "about", 
        "at", "by", "from", "to", "in", "on", "of", "is", "am", "are", "was", "were", 
        "be", "been", "being", "do", "does", "did", "doing", "i", "me", "my", "you", 
        "your", "it", "its", "this", "that", "can", "will", "would", "should", "could"
    )

    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            val gson = Gson()
            val resType = object : TypeToken<List<ResponseItem>>() {}.type
            val assetManager = context.applicationContext.assets
            val assetList = assetManager.list("assistant") ?: return

            assetList.forEach { fileName ->
                if (fileName.endsWith(".json")) {
                    try {
                        val category = fileName.removeSuffix(".json")
                        val json = assetManager.open("assistant/$fileName").bufferedReader().use { it.readText() }
                        val items: List<ResponseItem>? = gson.fromJson(json, resType)
                        items?.let { 
                            categorizedResponses.getOrPut(category) { mutableListOf() }.addAll(it)
                        }
                    } catch (e: Exception) { 
                        Log.e("AssistantResponseProvider", "Error loading asset: $fileName", e) 
                    }
                }
            }
            isInitialized = true
            Log.d("AssistantResponseProvider", "Initialized with ${categorizedResponses.size} categories")
        } catch (e: Exception) { 
            Log.e("AssistantResponseProvider", "Initialization failed", e)
        }
    }

    fun getLoadedCount(): Int = categorizedResponses.values.sumOf { it.size }

    fun getChatResponse(command: String): String? {
        val sanitizedCmd = command.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")
        if (sanitizedCmd.isEmpty()) return null
        
        val commandWords = sanitizedCmd.split(" ").filter { it.isNotEmpty() && !STOP_WORDS.contains(it) }.toSet()

        // 1. Define Search Priority Tiers (to improve response quality and avoid noise)
        val tiers = listOf(
            // Tier 1: Functional & App Help (Most important)
            listOf("appdata", "general", "task", "habits", "workouts", "finance", "projects", "notes", "workspace"),
            // Tier 2: Persona & Interaction
            listOf("identity", "personality", "greeting", "farewell", "starters", "clarification", "confirmation"),
            // Tier 3: Support & Feedback
            listOf("acknowledgement", "appreciation", "apologies", "encouragement", "compliments"),
            // Tier 4: Knowledge & Chatter (Broad matches)
            listOf("casual_chat", "humor", "health", "mindset", "emotions", "daily_life", "motivation", "productivity", "curiosity")
        )

        // 2. Iterate through tiers and find the best match
        for (tier in tiers) {
            val bestMatchInTier = findBestMatchInCategories(sanitizedCmd, commandWords, tier)
            if (bestMatchInTier != null) return bestMatchInTier
        }

        // 3. Ultimate Fallback
        return findBestMatchInCategories(sanitizedCmd, commandWords, listOf("fallback")) ?: getFallbackResponse(sanitizedCmd)
    }

    private fun findBestMatchInCategories(cmd: String, cmdWords: Set<String>, categories: List<String>): String? {
        var overallBestMatch: ResponseItem? = null
        var highestScore = 0

        categories.forEach { cat ->
            categorizedResponses[cat]?.forEach { item ->
                var itemBestScore = 0
                item.keys?.forEach { key ->
                    val sanitizedKey = key.lowercase().replace(Regex("[^a-z0-9 ]"), "")
                    if (sanitizedKey.isNotEmpty()) {
                        val keyWords = sanitizedKey.split(" ").filter { it.isNotEmpty() && !STOP_WORDS.contains(it) }
                        
                        val score = when {
                            cmd == sanitizedKey -> 100 // Perfect phrase match
                            cmdWords.isNotEmpty() && keyWords.isNotEmpty() && cmdWords.containsAll(keyWords) -> 80 + keyWords.size // Command covers key intent
                            cmd.contains(sanitizedKey) || sanitizedKey.contains(cmd) -> 30 + sanitizedKey.length // Context match
                            else -> 0
                        }
                        if (score > itemBestScore) itemBestScore = score
                    }
                }
                
                if (itemBestScore > highestScore) {
                    highestScore = itemBestScore
                    overallBestMatch = item
                }
            }
        }

        overallBestMatch?.let { match ->
            if (highestScore >= 40) {
                val possibleResponses = mutableListOf<String>()
                match.values?.let { possibleResponses.addAll(it) }
                match.value?.let { possibleResponses.add(it) }
                if (possibleResponses.isNotEmpty()) return possibleResponses.random()
            }
        }
        
        return null
    }

    private fun generateImpactSummary(): String {
        val habits = DataManager.habits.count { it.isCompleted }
        val projects = DataManager.projects.count { it.status == "Completed" }
        
        return "You've mastered $habits habits and completed $projects major projects today. Overall, your momentum is looking strong!"
    }

    private fun getFallbackResponse(cmd: String): String? {
        // Special case for impact summary
        if (cmd.contains("impact") || cmd.contains("summary") || cmd.contains("progress")) {
            return generateImpactSummary()
        }

        return when {
            cmd.contains("hello") || cmd.contains("hi there") -> "Hello! I'm your local AI assistant. How can I help you today?"
            cmd.contains("thanks") || cmd.contains("thank you") -> "You're very welcome! Let me know if you need anything else."
            else -> null
        }
    }
}
