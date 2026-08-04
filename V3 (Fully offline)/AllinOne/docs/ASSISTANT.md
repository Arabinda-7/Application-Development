# Offline AI Assistant Pipeline Documentation

## 🤖 Overview

The **AllinOne AI Assistant** is a 100% **offline**, privacy-first natural language assistant designed to run on-device without internet access or cloud API dependencies.

---

## ⚙️ Architecture & Pipeline Flow

```
                                    +----------------------------------+
                                    |     Natural Language Input       |
                                    |    (Voice Speech-to-Text / Text) |
                                    +----------------------------------+
                                                     |
                                                     v
                                    +----------------------------------+
                                    |       AssistantBrain             |
                                    |        (Orchestrator)            |
                                    +----------------------------------+
                                                     |
                                                     v
                                    +----------------------------------+
                                    |       Offline NLU Engine         |
                                    |  (IntentDetector, DateParser,    |
                                    |   ParameterExtractor)            |
                                    +----------------------------------+
                                                     |
                                                     v
                                    +----------------------------------+
                                    |     Confidence & Context Check   |
                                    |  (ConfidenceTier, ContextMgr)    |
                                    +----------------------------------+
                                        /                          \
                        High Confidence /                            \ Low Confidence
                                       v                              v
                    +-----------------------+              +-------------------------+
                    |  Command Execution    |              |  FollowUpQuestionEngine |
                    |  (Task, Habit, etc.)  |              |  (Request Clarification)|
                    +-----------------------+              +-------------------------+
                                       |
                                       v
                    +-----------------------+
                    |  Undo/Redo History    |
                    |  (ReversibleCommand)  |
                    +-----------------------+
```

---

## 🎯 Modular AI Assistant Components

### 1. Intent Detection & Confidence Scoring (`nlu/IntentConfidence.kt`)
- Measures classification confidence between `0.0f` and `1.0f`.
- Categorizes requests into **Confidence Tiers**:
  - `HIGH`: Confident match $\rightarrow$ Execute immediately.
  - `MEDIUM`: Partial match $\rightarrow$ Prompt for user confirmation.
  - `LOW` / `UNKNOWN`: Insufficient match $\rightarrow$ Delegate to `FollowUpQuestionEngine`.

### 2. Natural Date Parser (`nlu/NaturalDateParser.kt`)
- Converts phrases ("today", "tomorrow", "yesterday", "next Monday", "in 3 days") into epoch timestamps offline using regex and calendar arithmetic.

### 3. Conversation Context & Context Switching (`context/ContextSwitchingManager.kt`)
- Tracks active session domain (`TASK`, `HABIT`, `FINANCE`, `WORKOUT`, `NOTE`, `PROJECT`, `GENERAL`).
- Preserves multi-turn state when switching between feature domains.

### 4. Undo / Redo & Reversible Execution (`history/UndoRedoManager.kt`)
- Implements `ReversibleCommand` interface supporting dual-stack (`undoStack`, `redoStack`) action rollbacks.

### 5. Follow-Up Question Engine (`question/FollowUpQuestionEngine.kt`)
- Formulates helpful clarifying prompts when parameters are missing or intent confidence is low.
