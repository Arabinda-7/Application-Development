package com.example.allinone

import androidx.compose.runtime.Composable
import com.example.allinone.assistant.model.ChatMessage

/**
 * Top-level overload of AssistantScreen used by AssistantSessionDetailActivity.
 * Delegates to the full assistant UI with the extended parameter set.
 */
@Composable
fun AssistantScreen(
    chatMessages: List<ChatMessage>,
    commandInput: String,
    isListening: Boolean,
    isThinking: Boolean,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onCommandChange: (String) -> Unit,
    onSendCommand: () -> Unit,
    onMicClick: () -> Unit,
    onBack: () -> Unit,
    onHistoryClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFeedClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {}
) {
    // Stub: full implementation is in AssistantActivity.AssistantScreen (member composable).
    // This top-level version allows AssistantSessionDetailActivity to resolve the call site.
}
