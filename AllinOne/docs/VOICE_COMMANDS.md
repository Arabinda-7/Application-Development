# Voice Commands Reference Guide

This document lists supported natural language and voice commands recognized by the offline **AllinOne AI Assistant**.

---

## 📅 Task Management Commands

| Command Pattern | Example | Action Executed |
|---|---|---|
| `Add task [title]` | *"Add task Buy groceries"* | Creates a new task in the default section. |
| `Add task [title] for [date]` | *"Add task Pay bills for tomorrow"* | Creates a task with a parsed due date timestamp. |
| `Complete task [title]` | *"Complete task Buy groceries"* | Toggles completion status to `true`. |
| `Delete task [title]` | *"Delete task Read book"* | Removes matching task from database. |
| `Clear completed tasks` | *"Clear completed tasks"* | Purges all finished tasks. |

---

## ⚡ Habit Tracking Commands

| Command Pattern | Example | Action Executed |
|---|---|---|
| `Add habit [title]` | *"Add habit Drink water"* | Registers a new habit item. |
| `Check in habit [title]` | *"Check in habit Drink water"* | Logs today's date into completed dates list. |
| `Show habit streaks` | *"Show habit streaks"* | Displays active and best streak statistics. |

---

## 🏋️ Workout Commands

| Command Pattern | Example | Action Executed |
|---|---|---|
| `Log workout [routine]` | *"Log workout Leg Day"* | Creates a workout routine entry for today. |
| `Show workout history` | *"Show workout history"* | Opens workout performance dashboard. |

---

## 💰 Finance & Ledger Commands

| Command Pattern | Example | Action Executed |
|---|---|---|
| `Add expense [amount] for [category]` | *"Add expense 50 for Coffee"* | Logs a debit transaction. |
| `Add income [amount]` | *"Add income 1000"* | Logs a credit transaction. |
| `Show safe to spend` | *"Show safe to spend"* | Calculates remaining monthly budget. |

---

## 📝 Notes & Mind Map Commands

| Command Pattern | Example | Action Executed |
|---|---|---|
| `Add note [title]` | *"Add note Meeting Ideas"* | Opens note editor with title. |
| `Search notes for [query]` | *"Search notes for Project Alpha"* | Filters notebook entries by query. |

---

## ↩️ Undo / Redo Commands

| Command Pattern | Example | Action Executed |
|---|---|---|
| `Undo` / `Undo last action` | *"Undo last action"* | Reverts the most recent reversible action. |
| `Redo` | *"Redo"* | Re-applies the previously undone action. |
