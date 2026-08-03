package com.example.allinone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.example.allinone.assistant.model.ChatMessage
import com.example.allinone.data.model.Habit
import com.example.allinone.data.model.Task
import com.example.allinone.data.model.Subtask
import com.example.allinone.data.model.Note
import com.example.allinone.data.model.Workout
import com.example.allinone.data.model.ProjectHistory
import com.example.allinone.data.model.JournalEntry
import com.example.allinone.data.model.ProjectFeature

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AssistantSessionDetailActivity : BaseActivity() {

    private var sessionId by mutableLongStateOf(-1L)
    private var sessionTitle by mutableStateOf("")
    private var commandInput by mutableStateOf("")
    private var isListening by mutableStateOf(false)
    private var isMuted by mutableStateOf(!DataManager.isAssistantVoiceEnabled)
    private val chatMessages = mutableStateListOf<ChatMessage>()
    private val aiChatRepo = DataManager.getAiChatRepository()
    private var voiceHandler: VoiceAssistantHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionId = intent.getLongExtra("SESSION_ID", -1L)
        sessionTitle = intent.getStringExtra("SESSION_TITLE") ?: "Conversation"

        if (sessionId == -1L) {
            finish()
            return
        }

        voiceHandler = VoiceAssistantHandler(
            context = this,
            onResults = { command -> handleCommand(command) },
            onListeningStateChanged = { listening -> isListening = listening },
            onError = { _ -> isListening = false }
        ).apply {
            isMuted = this@AssistantSessionDetailActivity.isMuted
        }

        lifecycleScope.launch {
            aiChatRepo?.getMessagesBySession(sessionId)?.collect { messages ->
                chatMessages.clear()
                chatMessages.addAll(messages.map { ChatMessage(it.text, it.isUser, it.timestamp) })
            }
        }

        setContent {
            val appStyle = remember { AppStyle.fromSettings() }
            CompositionLocalProvider(LocalAppStyle provides appStyle) {
                AssistantScreen(
                    chatMessages = chatMessages,
                    commandInput = commandInput,
                    isListening = isListening,
                    isThinking = voiceHandler?.isThinking ?: false,
                    isMuted = isMuted,
                    onMuteToggle = { 
                        isMuted = !isMuted
                        voiceHandler?.isMuted = isMuted
                        DataManager.isAssistantVoiceEnabled = !isMuted
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                    },
                    onCommandChange = { commandInput = it },
                    onSendCommand = { handleCommand(commandInput) },
                    onMicClick = { 
                        checkAndRequestPermission(android.Manifest.permission.RECORD_AUDIO) {
                            voiceHandler?.startListening()
                        }
                    },
                    onBack = { finish() },
                    onHistoryClick = { /* Already in history context */ },
                    onSettingsClick = { /* Can add navigation if needed */ },
                    onFeedClick = { /* Can add navigation if needed */ },
                    onNewChatClick = {
                        // Logic to start a new chat could also be here, or just finish and let main handle it
                        finish()
                    }
                )
            }
        }
    }

    private fun handleCommand(command: String) {
        if (command.isBlank()) return
        
        val userMsg = ChatMessage(command, true)
        chatMessages.add(userMsg)
        
        lifecycleScope.launch {
            aiChatRepo?.insertMessage(com.example.allinone.data.database.AiChatEntity(sessionId = sessionId, text = userMsg.text, isUser = userMsg.isUser, timestamp = userMsg.timestamp))
            
            delay(500)
            val action = AssistantBrain.parseCommand(command)
            val response = if (action != null) {
                var res = action.dynamicResponse ?: ""
                when (action.type ?: "CHAT_RESPONSE") {
                    "CHAT_RESPONSE" -> res = action.payload ?: ""
                    "ADD_HABIT" -> {
                        val payload = action.payload ?: ""
                        val habit = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            val name = parts[0]
                            val target = parts.getOrNull(1)?.toIntOrNull() ?: 1
                            val freq = parts.getOrNull(2) ?: "Anytime"
                            Habit(name = name, isCompleted = false, frequency = freq, target = target)
                        } else {
                            Habit(name = payload, isCompleted = false, frequency = "Anytime")
                        }
                        DataManager.habits.add(habit)
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                        if (res.isEmpty()) res = "Created habit: ${habit.name}"
                    }
                    "LOG_HABIT" -> {
                        val habitName = action.payload ?: ""
                        val habit = DataManager.habits.find { it.name.equals(habitName, ignoreCase = true) }
                        if (habit != null) {
                            habit.isCompleted = true
                            if (!habit.completedDates.contains(DataManager.getTrackingDateString())) {
                                habit.completedDates.add(DataManager.getTrackingDateString())
                            }
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                            if (res.isEmpty()) res = "Marked '$habitName' as completed!"
                        } else {
                            res = "Habit '$habitName' not found."
                        }
                    }
                    "ADD_TASK" -> {
                        val payload = action.payload ?: ""
                        val task = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            val name = parts[0]
                            val subsStr = parts.getOrNull(1) ?: ""
                            val reminderStr = parts.getOrNull(2) ?: ""
                            val subtasks = if (subsStr.isNotEmpty()) subsStr.split(",").map { Subtask(it, false) }.toMutableList() else mutableListOf()
                            val reminder = reminderStr.toLongOrNull()
                            Task(name = name, subtasks = subtasks, reminderTime = reminder)
                        } else {
                            Task(name = payload)
                        }
                        DataManager.tasks.add(0, task)
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                        if (res.isEmpty()) res = "Added task: ${task.name}"
                    }
                    "MARK_TASK_COMPLETE" -> {
                        val name = action.payload ?: ""
                        val task = DataManager.tasks.find { it.name.equals(name, ignoreCase = true) }
                        if (task != null) {
                            task.isCompleted = true
                            task.completedTimestamp = System.currentTimeMillis()
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "MARK_SUBTASK_COMPLETE" -> {
                        val payload = action.payload ?: ""
                        val parts = payload.split("|")
                        val taskName = parts.getOrNull(0) ?: ""
                        val subName = parts.getOrNull(1) ?: ""
                        val task = DataManager.tasks.find { it.name.equals(taskName, ignoreCase = true) }
                        val subtask = task?.subtasks?.find { it.name.equals(subName, ignoreCase = true) }
                        if (subtask != null) {
                            subtask.isCompleted = true
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                            res = "Marked '$subName' as completed in '${task.name}'!"
                        }
                    }
                    "ADD_NOTE" -> {
                        val payload = action.payload ?: ""
                        val note = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            Note(title = parts[0], content = parts.getOrNull(1) ?: "")
                        } else {
                            Note(title = payload, content = "")
                        }
                        DataManager.notes.add(0, note)
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                        if (res.isEmpty()) res = "Saved note: ${note.title}"
                    }
                    "ADD_WORKOUT" -> {
                        val payload = action.payload ?: ""
                        val workout = if (payload.contains("|")) {
                            val parts = payload.split("|")
                            val name = parts[0]
                            val mode = parts.getOrNull(1) ?: "Reps"
                            val target = parts.getOrNull(2)?.toIntOrNull() ?: 0
                            val rps = parts.getOrNull(3)?.toIntOrNull() ?: 0
                            val freq = parts.getOrNull(4) ?: "Anytime"
                            Workout(name = name, trackingMode = mode, target = target, repsPerSet = rps, frequency = freq, isCompleted = false)
                        } else {
                            Workout(name = payload, isCompleted = false, frequency = "Anytime")
                        }
                        DataManager.workouts.add(workout)
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                        if (res.isEmpty()) res = "Created workout: ${workout.name}"
                    }
                    "UPDATE_WORKOUT_PROGRESS" -> {
                        val payload = action.payload ?: ""
                        val parts = payload.split("|")
                        val name = parts.getOrNull(0) ?: ""
                        val inc = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val workout = DataManager.workouts.find { it.name.equals(name, ignoreCase = true) }
                        if (workout != null) {
                            workout.progress += inc
                            if (workout.progress >= workout.target) {
                                workout.isCompleted = true
                                if (!workout.completedDates.contains(DataManager.getTrackingDateString())) {
                                    workout.completedDates.add(DataManager.getTrackingDateString())
                                }
                            }
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "COMPLETE_WORKOUT" -> {
                        val name = action.payload ?: ""
                        val workout = DataManager.workouts.find { it.name.equals(name, ignoreCase = true) }
                        if (workout != null) {
                            workout.isCompleted = true
                            workout.progress = workout.target
                            if (!workout.completedDates.contains(DataManager.getTrackingDateString())) {
                                workout.completedDates.add(DataManager.getTrackingDateString())
                            }
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "CREATE_PROJECT" -> {
                        val payload = action.payload ?: ""
                        val parts = payload.split("|")
                        val title = parts[0]
                        val desc = parts.getOrNull(1) ?: ""
                        val status = parts.getOrNull(2) ?: "Not Started"
                        val priority = parts.getOrNull(3)?.toIntOrNull() ?: 1
                        val deadline = parts.getOrNull(4)?.toLongOrNull()
                        val goalsStr = parts.getOrNull(5) ?: ""
                        val featuresStr = parts.getOrNull(6) ?: ""

                        val goals: MutableList<JournalEntry> = if (goalsStr.isNotEmpty()) goalsStr.split(";").map { JournalEntry(it) }.toMutableList() else mutableListOf()
                        val features: MutableList<ProjectFeature> = if (featuresStr.isNotEmpty()) {
                            featuresStr.split(";").map { f ->
                                val fParts = f.split(",")
                                ProjectFeature(
                                    name = fParts[0],
                                    tag = fParts.getOrNull(1) ?: "",
                                    dueDate = fParts.getOrNull(2)?.toLongOrNull()
                                )
                            }.toMutableList()
                        } else mutableListOf<ProjectFeature>()

                        val project = Note(
                            title = title,
                            content = desc,
                            status = status,
                            priority = priority,
                            deadline = deadline,
                            ideaGoals = goals,
                            subFeatures = features,
                            isGlobalProject = true
                        )
                        DataManager.projects.add(0, project)
                        DataManager.saveData(this@AssistantSessionDetailActivity)
                        if (res.isEmpty()) res = "Created project: ${project.title}"
                    }
                    "ADD_PROJECT_FEATURE" -> {
                        val parts = action.payload!!.split("|")
                        val projTitle = parts[0]
                        val featName = parts[1]
                        val tag = parts.getOrNull(2) ?: "General"
                        val deadline = parts.getOrNull(3)?.toLongOrNull()
                        val project = DataManager.projects.find { it.title.equals(projTitle, ignoreCase = true) }
                        project?.let { p ->
                            p.subFeatures.add(ProjectFeature(name = featName, tag = tag, dueDate = deadline))
                            p.changeHistory.add(ProjectHistory(action = "Sub-feature Added", description = "Added feature '$featName'"))
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "UPDATE_PROJECT_PROPERTY" -> {
                        val parts = action.payload!!.split("|")
                        val projTitle = parts[0]
                        val prop = parts[1]
                        val value = parts[2]
                        val project = DataManager.projects.find { it.title.equals(projTitle, ignoreCase = true) }
                        project?.let { p ->
                            when (prop) {
                                "STATUS" -> p.status = value
                                "PRIORITY" -> p.priority = if (value.contains("High", true)) 2 else if (value.contains("Low", true)) 0 else 1
                                "DEADLINE" -> p.deadline = System.currentTimeMillis()
                            }
                            p.changeHistory.add(ProjectHistory(action = "Property Updated", description = "Changed $prop"))
                            DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "MARK_FEATURE_COMPLETE" -> {
                        val parts = action.payload!!.split("|")
                        val projTitle = parts[0]
                        val featName = parts[1]
                        val project = DataManager.projects.find { it.title.equals(projTitle, ignoreCase = true) }
                        project?.let { p ->
                            p.subFeatures.find { it.name.equals(featName, ignoreCase = true) }?.let { f ->
                                f.isCompleted = true
                                p.changeHistory.add(ProjectHistory(action = "Feature Completed", description = "Finished '$featName'"))
                                DataManager.saveData(this@AssistantSessionDetailActivity, true)
                            }
                        }
                    }
                    "ADD_PROJECT_GOAL" -> {
                        val parts = action.payload!!.split("|")
                        val projTitle = parts[0]
                        val goalText = parts[1]
                        val project = DataManager.projects.find { it.title.equals(projTitle, ignoreCase = true) }
                        project?.let { it.ideaGoals.add(JournalEntry(goalText)); DataManager.saveData(this@AssistantSessionDetailActivity, true) }
                    }
                    "CONVERT_NOTE_TO_PROJECT" -> {
                        val noteTitle = action.payload!!
                        DataManager.notes.find { it.title.equals(noteTitle, ignoreCase = true) }?.let { n ->
                            n.isGlobalProject = true; DataManager.notes.remove(n); DataManager.projects.add(0, n); DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "ADD_PROJECT_RESOURCE" -> {
                        val parts = action.payload!!.split("|")
                        DataManager.projects.find { it.title.equals(parts[0], ignoreCase = true) }?.let { p ->
                            p.content += "\nResource: ${parts[1]}"; DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    "ARCHIVE_PROJECT" -> {
                        DataManager.projects.find { it.title.equals(action.payload, ignoreCase = true) }?.let { p ->
                            p.isArchived = true; DataManager.saveData(this@AssistantSessionDetailActivity, true)
                        }
                    }
                    else -> if (res.isEmpty()) res = "Executing ${action.type}: ${action.payload}"
                }
                res
            } else {
                "I'm not sure how to do that yet."
            }
            
            val assistantMsg = ChatMessage(response, false)
            chatMessages.add(assistantMsg)
            aiChatRepo?.insertMessage(com.example.allinone.data.database.AiChatEntity(sessionId = sessionId, text = assistantMsg.text, isUser = assistantMsg.isUser, timestamp = assistantMsg.timestamp))
            
            if (!isMuted) {
                voiceHandler?.speak(response, response.trim().endsWith("?"))
            }
        }
        
        commandInput = ""
    }

    override fun onDestroy() {
        voiceHandler?.shutdown()
        super.onDestroy()
    }
}
