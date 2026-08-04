# Implementation Plan - Continuous AI Conversation

The goal is to make the AI Assistant context-aware so it remembers previous parts of the current conversation. This allows for follow-up questions and more natural interaction.

## Proposed Changes

### 1. [MODIFY] [AssistantBrain.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantBrain.kt)
- Update `parseCommand` signature to `parseCommand(command: String, history: List<ChatMessage>)`.
- Implement **Context Extraction**:
    - If the user uses pronouns (e.g., "it", "that project", "those habits"), the brain will look back at previous assistant responses to identify what was being discussed.
    - Store a `lastMentionedProject`, `lastMentionedHabit`, etc., in the `activeSession` state.
- Update **Intent Recognition**:
    - If a command is ambiguous (e.g., "show status"), and a project was recently discussed, default to showing that project's status.
- Improve **Response Variety**:
    - Add logic to avoid repeating the same greeting or transitional phrases within the same session.

### 2. [MODIFY] [AssistantActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantActivity.kt)
- Pass the `chatMessages` list to `AssistantBrain.parseCommand`.
- Ensure the welcome message doesn't trigger context lookups.

### 3. [MODIFY] [MainActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/MainActivity.kt)
- Update the voice assistant command handling to also pass conversation history for continuous voice-to-voice interaction.

## Verification Plan

### Manual Verification
1.  **Project Context:**
    - Ask: "Tell me about project X."
    - Follow up: "What is its status?" (AI should know you mean project X).
2.  **Habit Context:**
    - Ask: "How many times should I do Drink Water?"
    - Follow up: "Mark it as done." (AI should mark "Drink Water").
3.  **General Flow:**
    - Have a 3-4 turn conversation and ensure the AI doesn't lose track of the subject.
4.  **Session Reset:**
    - Click "New Chat" and verify that context is cleared (e.g., "Mark it as done" should now ask "Which habit?").
