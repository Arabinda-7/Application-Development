package com.example.allinone.assistant.history

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UndoRedoManager @Inject constructor() {

    private val undoStack = ArrayDeque<ReversibleCommand>()
    private val redoStack = ArrayDeque<ReversibleCommand>()

    suspend fun executeCommand(command: ReversibleCommand): Boolean {
        val success = command.execute()
        if (success) {
            undoStack.addLast(command)
            redoStack.clear()
        }
        return success
    }

    suspend fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        val command = undoStack.removeLast()
        val success = command.undo()
        if (success) {
            redoStack.addLast(command)
        } else {
            undoStack.addLast(command)
        }
        return success
    }

    suspend fun redo(): Boolean {
        if (redoStack.isEmpty()) return false
        val command = redoStack.removeLast()
        val success = command.execute()
        if (success) {
            undoStack.addLast(command)
        } else {
            redoStack.addLast(command)
        }
        return success
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
}
