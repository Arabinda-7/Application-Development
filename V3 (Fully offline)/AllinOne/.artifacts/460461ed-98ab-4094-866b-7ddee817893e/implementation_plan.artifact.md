# Fix Today's Agenda Task Visibility

This plan addresses the issue where tasks are not showing up in the "Today's Agenda" notification section. The current logic is too restrictive, only showing items with a reminder exactly for today, and ignoring overdue items or items created today without a specific reminder time.

## Proposed Changes

### Domain Layer (Use Cases)

#### [MODIFY] [GetTodayAgendaUseCase.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/domain/usecase/agenda/GetTodayAgendaUseCase.kt)
- Expand filtering logic to include:
    - **Tasks**: Uncompleted tasks with a reminder for today or anytime in the past (Overdue), AND uncompleted tasks created today even if they have no reminder.
    - **Projects/Milestones**: Uncompleted items with a deadline today or anytime in the past (Overdue).
- Ensure consistent logic across Global and Workspace sections.

### Data Layer (Room)

#### [MODIFY] [WorkspaceDao.kt](file:///C:/Users/arabi/OneDrive/Desktop/App Development/AllinOne/app/src/main/java/com/example/allinone/workspace/data/WorkspaceDao.kt)
- Update `getProjectsDueBetween`, `getGoalsDueBetween`, and `getTasksDueBetween` to include all uncompleted items due on or before the current day (Overdue support).
- Change the queries from `deadline >= :start AND deadline < :end` to `deadline < :end`.

## Verification Plan

### Manual Verification
1.  **Add Task for Today**: Create a task with a reminder for today. Verify it appears.
2.  **Add Task for Yesterday (Overdue)**: Create a task with a reminder for yesterday. Verify it appears.
3.  **Add Task Today (No Reminder)**: Create a task today without setting a reminder. Verify it appears in the agenda.
4.  **Add Task for Tomorrow**: Create a task with a reminder for tomorrow. Verify it does NOT appear.
5.  **Workspace Items**: Repeat the above for Workspace projects, goals, and tasks to verify overdue items are now included.
