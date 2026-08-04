package com.example.allinone

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.allinone.data.database.AiChatSessionEntity
import com.example.allinone.ui.assistant.AssistantHistoryScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantHistoryActivity : BaseActivity() {

    private var groupedSessions by mutableStateOf<Map<String, List<AiChatSessionEntity>>>(emptyMap())
    private val tabFlow = MutableStateFlow(0)
    private val searchFlow = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val aiChatRepo = DataManager.getAiChatRepository(this)

        lifecycleScope.launch {
            combine(tabFlow, searchFlow) { tab, query ->
                tab to query
            }.flatMapLatest { (tab, _) ->
                val type = if (tab == 0) "chat" else "voice"
                aiChatRepo?.getSessionsByType(type) ?: flowOf(emptyList())
            }.collect { sessions ->
                val query = searchFlow.value
                groupedSessions = groupSessionsByDate(sessions.filter { 
                    it.title.contains(query, ignoreCase = true) 
                })
            }
        }

        setContent {
            val appStyle = remember { AppStyle.fromSettings() }
            val selectedTabIndex by tabFlow.collectAsState()
            val searchQuery by searchFlow.collectAsState()

            CompositionLocalProvider(LocalAppStyle provides appStyle) {
                AssistantHistoryScreen(
                    groupedSessions = groupedSessions,
                    searchQuery = searchQuery,
                    selectedTabIndex = selectedTabIndex,
                    onTabChange = { tabFlow.value = it },
                    onSearchChange = { searchFlow.value = it },
                    onBack = { finish() },
                    onClearAll = {
                        lifecycleScope.launch { aiChatRepo?.clearEverything() }
                    },
                    onSessionClick = { session ->
                        val intent = Intent(this@AssistantHistoryActivity, AssistantSessionDetailActivity::class.java).apply {
                            putExtra("SESSION_ID", session.id)
                            putExtra("SESSION_TITLE", session.title)
                        }
                        startActivity(intent)
                    },
                    onDeleteSession = { session ->
                        lifecycleScope.launch { aiChatRepo?.deleteSession(session) }
                    }
                )
            }
        }
    }

    private fun groupSessionsByDate(sessions: List<AiChatSessionEntity>): Map<String, List<AiChatSessionEntity>> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

        return sessions.groupBy { session ->
            val sessionCal = Calendar.getInstance().apply { timeInMillis = session.timestamp }
            when {
                isSameDay(sessionCal, today) -> "Today"
                isSameDay(sessionCal, yesterday) -> "Yesterday"
                else -> SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date(session.timestamp))
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
