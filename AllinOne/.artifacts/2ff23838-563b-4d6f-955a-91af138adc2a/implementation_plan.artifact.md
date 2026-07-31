# Implementation Plan - Enhancing AI Responsiveness with NLU Dataset

I will integrate the intelligence from the provided `nlu_training_dataset.json` to expand the AI assistant's capabilities. This involves moving beyond simple keyword matching to a more robust parameter-aware command parsing system.

## Proposed Changes

### [Data Integration]

#### [NEW] [nlu_training_dataset.json](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/assets/nlu_training_dataset.json)
Copy the provided dataset to assets (sampled/optimized if needed) to allow the `AssistantBrain` to use it for intent recognition.

### [Core Logic]

#### [MODIFY] [AssistantBrain.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantBrain.kt)
- **Enhanced Parsing**: Update `parseCommand` to handle new intent types from the dataset:
    - `LOG_INCOME`: Handle salary and dividend logging.
    - `CHECK_PROJECT_DEADLINE`: Query project dates.
    - `LOG_MOOD`: Record ratings and tags.
    - `CREATE_NESTED_TASK`: Support hierarchical task creation.
    - `SEARCH_NOTES`: Implement keyword search logic.
    - `LOG_WORKOUT` (Advanced): Parse sets, reps, and weights using regex/parameter extraction.
- **Dynamic Response Selection**: If an exact match is found in the dataset, use its specific response.

#### [MODIFY] [DataManager.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/DataManager.kt)
Add logic to handle the new action types (e.g., adding income, searching notes).

### [UI Interaction]

#### [MODIFY] [AssistantActivity.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/AssistantActivity.kt)
Update the `handleCommand` loop to handle a wider variety of responses and UI navigations.

## Verification Plan

### Manual Verification
1.  **Complex Log**: Say "Log 5000 income from salary" -> Verify response and data update.
2.  **Deadline Query**: Say "When is the Mobile App Launch project due?" -> Verify it queries the project database.
3.  **Mood Log**: Say "Log today's mood as Energized" -> Verify mood tracking.
4.  **Hierarchy**: Say "Create task Home Clean with subtasks Vacuum and Mop" -> Verify nested tasks.
5.  **Search**: Say "Search notes for 'Refactor'" -> Verify results shown in chat or notes section.

### Automated Tests
- Run `app:assembleDebug` to verify compilation.
