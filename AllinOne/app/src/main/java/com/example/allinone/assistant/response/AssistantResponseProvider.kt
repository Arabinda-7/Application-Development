package com.example.allinone.assistant.response

import android.content.Context
import android.util.Log
import com.example.allinone.assistant.model.ResponseItem
import com.example.allinone.data.model.Task
import com.example.allinone.data.model.Habit
import com.example.allinone.data.model.Workout
import com.example.allinone.DataManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantResponseProvider @Inject constructor() {

    private var loadedResponses: List<ResponseItem> = emptyList()

    fun initialize(context: Context) {
        try {
            val gson = Gson()
            val resType = object : TypeToken<List<ResponseItem>>() {}.type
            val allResponses = mutableListOf<ResponseItem>()
            val assetList = context.assets.list("assistant")
            
            assetList?.forEach { fileName ->
                if (fileName.endsWith(".json")) {
                    try {
                        val json = context.assets.open("assistant/$fileName").bufferedReader().use { it.readText() }
                        android.util.Log.i("AssistantResponseProvider", "Loaded asset: $fileName, Content length: ${json.length}")
                        val resItems: List<ResponseItem>? = gson.fromJson(json, resType)
                        resItems?.let { items -> 
                            allResponses.addAll(items.filter { it.keys != null && (it.values != null || it.value != null) }) 
                        }
                    } catch (e: Exception) { 
                        Log.e("AssistantResponseProvider", "Error loading asset: $fileName", e) 
                    }
                }
            }
            loadedResponses = allResponses
            Log.d("AssistantResponseProvider", "Loaded responses size: ${loadedResponses.size}")
        } catch (e: Exception) { 
            e.printStackTrace() 
        }
    }

    fun getChatResponse(command: String): String? {
        val sanitizedCmd = command.lowercase().trim().replace(Regex("[^a-z0-9 ]"), "")
        if (sanitizedCmd.isEmpty()) return null
        
        // 1. Check knowledge base
        val match = loadedResponses.find { item -> 
            item.keys?.any { key -> 
                val sanitizedKey = key.lowercase().replace(Regex("[^a-z0-9 ]"), "")
                if (sanitizedKey.isEmpty()) false
                else sanitizedCmd.contains(sanitizedKey) || sanitizedKey.contains(sanitizedCmd)
            } == true 
        }
        
        if (match != null) {
            val possibleResponses = mutableListOf<String>()
            match.values?.let { possibleResponses.addAll(it) }
            match.value?.let { possibleResponses.add(it) }
            if (possibleResponses.isNotEmpty()) return possibleResponses.random()
        }

        // 2. Impact Summary / Progress queries
        if (sanitizedCmd.contains("impact") || sanitizedCmd.contains("summary") || sanitizedCmd.contains("progress")) {
            return generateImpactSummary()
        }

        // 3. Static Fallbacks
        return getFallbackResponse(sanitizedCmd)
    }

    private fun generateImpactSummary(): String {
        val habits = DataManager.habits.count { it.isCompleted }
        val projects = DataManager.projects.count { it.status == "Completed" }
        
        return "You've mastered $habits habits and completed $projects major projects today. Overall, your momentum is looking strong!"
    }

    private fun getFallbackResponse(cmd: String): String? {
        return when {
            cmd.contains("hello") || cmd.contains("hi there") -> "Hello! I'm your local AI assistant. How can I help you today?"
            cmd.contains("thanks") || cmd.contains("thank you") -> "You're very welcome! Let me know if you need anything else."
            else -> null
        }
    }
}
