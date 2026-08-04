package com.example.allinone

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * VoiceInteractionManager: A robust manager for STRICTLY OFFLINE speech interaction.
 * Removed all internet-dependent recovery tiers and permissions.
 */
class VoiceInteractionManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private var onFinalResult: ((String) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null
    
    private var lastActivity: Activity? = null

    init {
        mainHandler.post {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isTtsReady = true
        }
    }

    /**
     * Strictly initializes the On-Device (Offline) recognizer.
     */
    private fun initializeOfflineRecognizer(activityContext: Context) {
        destroyRecognizer()
        
        try {
            // Force On-Device recognizer (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(activityContext)
            } else {
                // Fallback for older devices, still requesting offline via intent
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activityContext)
            }
        } catch (e: Exception) {
            // If explicit on-device fails, try standard but intent will still demand offline
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activityContext)
        }
        
        speechRecognizer?.setRecognitionListener(createListener())
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
            _partialText.value = ""
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { _isListening.value = false }

        override fun onError(error: Int) {
            _isListening.value = false
            
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Mic hardware error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions missing"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Hardware busy"
                12, 13 -> "Offline speech data not found. Please download language packs in Google settings."
                else -> "Offline recognition failed (Error $error)"
            }
            
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onError?.invoke(message)
            }
            
            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                destroyRecognizer()
            }
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onFinalResult?.invoke(matches[0])
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _partialText.value = matches[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {}
    }

    fun startListening(activity: Activity, onResult: (String) -> Unit, onError: (String) -> Unit) {
        this.onFinalResult = onResult
        this.onError = onError
        this.lastActivity = activity
        
        _partialText.value = ""

        mainHandler.post {
            try {
                tts?.stop()
                destroyRecognizer()
                
                mainHandler.postDelayed({
                    initializeOfflineRecognizer(activity)
                    
                    if (speechRecognizer == null) {
                        _isListening.value = false
                        onError("Offline Voice Engine not found")
                        return@postDelayed
                    }

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.packageName)
                        
                        // MANDATORY OFFLINE: Tell the system not to use internet
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

                        putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
                        putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 5000L)
                    }
                    
                    speechRecognizer?.startListening(intent)
                }, 500)
            } catch (e: Exception) {
                _isListening.value = false
                onError("Offline system failed to start")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try { speechRecognizer?.stopListening() } catch (e: Exception) {}
            _isListening.value = false
        }
    }

    fun speak(text: String) {
        mainHandler.post {
            if (isTtsReady) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_response")
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                tts?.shutdown()
                destroyRecognizer()
            } catch (e: Exception) {}
        }
    }
}
