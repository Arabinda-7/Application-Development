package com.example.allinone

import android.content.Context
import android.content.Intent
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.speech.tts.UtteranceProgressListener
import java.util.*

/**
 * VoiceAssistantHandler: Manages Speech Recognition and Text-to-Speech with robust error handling.
 */
class VoiceAssistantHandler(
    private val context: Context,
    private val onResults: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false
    var isMuted = false

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    init {
        ensureRecognizer()
        tts = TextToSpeech(context, this)
    }

    private fun ensureRecognizer() {
        if (speechRecognizer != null) return
        
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            // Standard recognizer is generally more stable than createOnDeviceSpeechRecognizer
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            applySettings()
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == "follow_up") {
                        (context as? android.app.Activity)?.runOnUiThread {
                            startListening()
                        }
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {}
            })
        }
    }

    private var recognitionActive = false

    fun startListening() {
        if (speechRecognizer == null) {
            ensureRecognizer()
        }
        
        if (speechRecognizer == null) {
            onError("Speech recognition not available on this device")
            return
        }

        // Force reset state
        speechRecognizer?.cancel()
        recognitionActive = false
        
        tts?.stop()
        
        onListeningStateChanged(true)

        // Shorter delay to feel more responsive
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                recognitionActive = true
                performHapticFeedback(VibrationEffect.EFFECT_CLICK)
                
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    
                    // Allow cloud fallback
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false) 
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                recognitionActive = false
                onListeningStateChanged(false)
                onError("Mic Error: ${e.message}")
            }
        }, 300) 
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        recognitionActive = false
        onListeningStateChanged(false)
    }

    fun speak(text: String, isFollowUp: Boolean = false) {
        if (isMuted || !isTtsReady) return
        
        applySettings()
        performHapticFeedback(VibrationEffect.EFFECT_TICK)
        
        val params = Bundle()
        val utteranceId = if (isFollowUp) "follow_up" else "response"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun getAvailableVoices(): List<Voice> {
        val allVoices = tts?.voices ?: emptySet()
        val locale = Locale.getDefault()
        return allVoices.filter { 
            it.locale.language == locale.language && 
            it.locale.country == locale.country 
        }.sortedWith(compareByDescending<Voice> { it.quality }.thenByDescending { !it.isNetworkConnectionRequired })
    }

    fun setVoiceByName(name: String) {
        tts?.voices?.find { it.name == name }?.let { 
            tts?.voice = it
            DataManager.assistantVoiceName = name
        }
    }

    private fun applySettings() {
        tts?.setPitch(DataManager.assistantPitch)
        tts?.setSpeechRate(DataManager.assistantSpeechRate)
        DataManager.assistantVoiceName?.let { name ->
            tts?.voices?.find { it.name == name }?.let { tts?.voice = it }
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun performHapticFeedback(effectId: Int) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        private var speechReceived = false

        override fun onReadyForSpeech(params: Bundle?) {
            speechReceived = false
        }

        override fun onBeginningOfSpeech() {
            speechReceived = true
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
            // We wait for onResults or onError for final state change
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio record error"
                SpeechRecognizer.ERROR_CLIENT -> "Mic client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission denied"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                    recognitionActive = false
                    onListeningStateChanged(false)
                    return
                }
                SpeechRecognizer.ERROR_SERVER -> "Mic server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Mic timeout"
                else -> "Error: $error"
            }
            
            recognitionActive = false
            onListeningStateChanged(false)
            if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                onError(message)
            }
        }

        override fun onResults(results: Bundle?) {
            recognitionActive = false
            onListeningStateChanged(false)
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResults(matches[0])
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Partial results can be handled here for live UI updates
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
