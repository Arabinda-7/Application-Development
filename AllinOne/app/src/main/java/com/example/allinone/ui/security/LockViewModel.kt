package com.example.allinone.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allinone.DataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LockViewModel : ViewModel() {

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _firstAttemptPin = MutableStateFlow("")
    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

    private val _uiState = MutableStateFlow<LockUiState>(LockUiState.Idle)
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    fun appendDigit(digit: String) {
        if (_enteredPin.value.length < 4) {
            _enteredPin.value += digit
            if (_enteredPin.value.length == 4) {
                processPin()
            }
        }
    }

    fun removeDigit() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
        }
    }

    private fun processPin() {
        // Logic will be handled in Activity for now to minimize massive refactor of DataManager static calls,
        // but we'll provide callbacks.
        _uiState.value = LockUiState.PinComplete(_enteredPin.value)
    }

    fun resetPinEntry() {
        _enteredPin.value = ""
        _uiState.value = LockUiState.Idle
    }

    fun setConfirming(pin: String) {
        _firstAttemptPin.value = pin
        _enteredPin.value = ""
        _isConfirming.value = true
    }

    fun resetSetup() {
        _enteredPin.value = ""
        _firstAttemptPin.value = ""
        _isConfirming.value = false
    }

    sealed class LockUiState {
        object Idle : LockUiState()
        data class PinComplete(val pin: String) : LockUiState()
    }
}
