package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.model.VoicePersona
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AudioSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    // Service Availability & Health
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _isSttAvailable = MutableStateFlow(false)
    val isSttAvailable: StateFlow<Boolean> = _isSttAvailable.asStateFlow()

    // Real-time audio waveform amplitude (0.0 to 1.0)
    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    // Recognition States
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _sttError = MutableStateFlow<String?>(null)
    val sttError: StateFlow<String?> = _sttError.asStateFlow()

    // TTS States
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsError = MutableStateFlow<String?>(null)
    val ttsError: StateFlow<String?> = _ttsError.asStateFlow()

    // SpeechRecognizer & TextToSpeech instances
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null

    // Voice personas available
    val availableVoices = listOf(
        VoicePersona("voice_niloofar", "نیلوفر", "زن • لحن طبیعی و گرم", "زن", "طبیعی و احساسی", pitch = 1.05f, speechRate = 0.95f),
        VoicePersona("voice_arya", "آریا", "مرد • گوینده رسمی و رادیویی", "مرد", "رسمی و قاطع", pitch = 0.90f, speechRate = 1.0f),
        VoicePersona("voice_sohrab", "سهراب", "مرد • آرام و ادیبانه", "مرد", "آرام و عمیق", pitch = 0.85f, speechRate = 0.88f),
        VoicePersona("voice_tara", "تارا", "زن • پرانرژی و صمیمی", "زن", "جوان و پویا", pitch = 1.15f, speechRate = 1.05f)
    )

    init {
        // 1. Initialize STT Availability
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        _isSttAvailable.value = available

        // 2. Initialize TTS
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            _ttsError.value = "عدم امکان راه‌اندازی موتور تبدیل متن به صوت: ${e.localizedMessage}"
            _isTtsReady.value = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { ttsEngine ->
                // Try to set Persian or Default locale
                val farsiLocale = Locale("fa", "IR")
                val langResult = ttsEngine.setLanguage(farsiLocale)

                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default locale
                    ttsEngine.language = Locale.getDefault()
                }

                ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })

                _isTtsReady.value = true
                _ttsError.value = null
            }
        } else {
            _isTtsReady.value = false
            _ttsError.value = "سرویس موتور صوتی دستگاه فعال نیست."
        }
    }

    // START LISTENING (STT)
    fun startListening(onResult: (String) -> Unit) {
        _sttError.value = null
        _recognizedText.value = ""

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _sttError.value = null
                }

                override fun onBeginningOfSpeech() {
                    _audioRms.value = 0.5f
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize RMS dB typically (-2 to 10 dB) to 0.0 - 1.0
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                    _audioRms.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _audioRms.value = 0f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "خطای ضبط صدا از میکروفون دستگاه"
                        SpeechRecognizer.ERROR_CLIENT -> "خطای کلاینت در پردازش ورودی صوت"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "مجوز دسترسی به ضبط صدای میکروفون اعطا نشده است"
                        SpeechRecognizer.ERROR_NETWORK -> "خطای شبکه هنگام ارتباط با سرور تشخیص گفتار"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "زمان اتصال به سرویس تشخیص گفتار به پایان رسید"
                        SpeechRecognizer.ERROR_NO_MATCH -> "عبارت مشخصی شناسایی نشد، لطفاً مجدداً شفاف‌تر بیان فرمایید"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "سرویس تشخیص گفتار در حال حاضر مشغول است"
                        SpeechRecognizer.ERROR_SERVER -> "خطای پردازش از سمت سرور تشخیص صوت"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "هیچ گفتاری در زمان مجاز دریافت نشد"
                        else -> "خطای ناشناخته در تشخیص صوت (کد $error)"
                    }
                    _sttError.value = errorMsg
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _audioRms.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestText = matches?.firstOrNull() ?: ""
                    if (bestText.isNotBlank()) {
                        _recognizedText.value = bestText
                        onResult(bestText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val partial = matches?.firstOrNull() ?: ""
                    if (partial.isNotBlank()) {
                        _recognizedText.value = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("fa", "en"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true

        } catch (e: Exception) {
            _isListening.value = false
            _sttError.value = "عدم امکان آغاز ضبط صدا: ${e.localizedMessage}"
        }
    }

    // STOP LISTENING
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _audioRms.value = 0f
    }

    // SPEAK TEXT (TTS)
    fun speakText(
        text: String,
        pitch: Float = 1.0f,
        speechRate: Float = 1.0f,
        onDone: (() -> Unit)? = null
    ) {
        if (!_isTtsReady.value) {
            _ttsError.value = "موتور تبدیل متن به صوت هنوز متصل نشده است."
            onDone?.invoke()
            return
        }

        try {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speechRate)

            val utteranceId = "orion_tts_${System.currentTimeMillis()}"
            _isSpeaking.value = true

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            _isSpeaking.value = false
            _ttsError.value = "خطا در پخش گفتار: ${e.localizedMessage}"
            onDone?.invoke()
        }
    }

    // STOP TTS PLAYBACK
    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    // CLEANUP
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (_: Exception) {}

        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}

        _isListening.value = false
        _isSpeaking.value = false
    }
}
