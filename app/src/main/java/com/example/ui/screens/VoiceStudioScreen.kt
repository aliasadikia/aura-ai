package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioSpeechManager
import com.example.data.PlatformRepository
import com.example.model.AppScreen
import com.example.model.HistoryItem
import com.example.model.HistoryType
import com.example.model.VoicePersona
import com.example.model.VoiceTurn
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ConversationState(val labelFa: String, val color: Color) {
    IDLE("آماده شروع مکالمه", TextSecondary),
    LISTENING("در حال شنیدن صدای شما...", StatusConnected),
    THINKING("در حال پردازش و تدوین پاسخ...", ElectricIndigo),
    SPEAKING("در حال صحبت هوش مصنوعی...", CyberViolet),
    PAUSED("مکالمه متوقف شده است", StatusWarning)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceStudioScreen(
    onNavigateToStt: () -> Unit,
    onNavigateToTts: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { AudioSpeechManager(context) }

    // Clean up manager on dispose
    DisposableEffect(Unit) {
        onDispose {
            audioManager.destroy()
        }
    }

    val isTtsReady by audioManager.isTtsReady.collectAsState()
    val isSttAvailable by audioManager.isSttAvailable.collectAsState()
    val audioRms by audioManager.audioRms.collectAsState()
    val isListening by audioManager.isListening.collectAsState()
    val isSpeaking by audioManager.isSpeaking.collectAsState()
    val recognizedText by audioManager.recognizedText.collectAsState()
    val sttError by audioManager.sttError.collectAsState()

    var conversationState by remember { mutableStateOf(ConversationState.IDLE) }
    var selectedPersona by remember { mutableStateOf(audioManager.availableVoices.first()) }
    var conversationTurns by remember {
        mutableStateOf<List<VoiceTurn>>(
            listOf(
                VoiceTurn(
                    id = "turn-init",
                    speaker = "هوش مصنوعی",
                    text = "سلام! من آماده گفتگوی صوتی زنده با شما هستم. می‌توانید سوالات، ایده‌ها یا متون خود را به زبان فارسی بیان کنید.",
                    timestamp = "۱۰:۰۰",
                    isUser = false
                )
            )
        )
    }

    var isMicMuted by remember { mutableStateOf(false) }
    var isCallActive by remember { mutableStateOf(false) }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    val maxSessionSeconds = 300 // 5-minute session limit

    var showVoiceSettingsSheet by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isCallActive = true
            conversationState = ConversationState.LISTENING
            audioManager.startListening { transcribedText ->
                processUserSpeech(
                    userSpeech = transcribedText,
                    persona = selectedPersona,
                    audioManager = audioManager,
                    onUpdateTurns = { newTurn ->
                        conversationTurns = conversationTurns + newTurn
                    },
                    onStateChange = { newState ->
                        conversationState = newState
                    }
                )
            }
        } else {
            showPermissionDeniedDialog = true
            isCallActive = false
            conversationState = ConversationState.IDLE
        }
    }

    // Timer coroutine for session duration & limit
    LaunchedEffect(isCallActive, conversationState) {
        while (isCallActive && conversationState != ConversationState.PAUSED) {
            delay(1000)
            sessionSeconds++
            if (sessionSeconds >= maxSessionSeconds) {
                // Time limit reached
                isCallActive = false
                conversationState = ConversationState.IDLE
                audioManager.stopListening()
                audioManager.stopSpeaking()
                saveConversationToHistory(conversationTurns, selectedPersona.nameFa)
                break
            }
        }
    }

    // Auto-scroll transcript on new turn
    LaunchedEffect(conversationTurns.size) {
        if (conversationTurns.isNotEmpty()) {
            listState.animateScrollToItem(conversationTurns.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("voice_studio_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Studio Header with Service Health Status
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = if (isCallActive) StatusConnected else ActiveBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مکالمه صوتی دوطرفه (Real-time Voice)",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "دریافت صدای میکروفون، استنتاج هوشمند و پاسخ صوتی فارسی با کمترین تاخیر",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    // Service connection indicator
                    val isServiceConnected = isTtsReady && isSttAvailable
                    StatusPill(
                        label = if (isServiceConnected) "سرویس صوتی آنلاین" else "اتصال موتور صوتی...",
                        statusColor = if (isServiceConnected) StatusConnected else StatusWarning
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Shortcuts to independent STT & TTS screens
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToStt,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, TranslucentBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(16.dp), tint = PhotonBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "صوت به متن (STT)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = onNavigateToTts,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, TranslucentBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyberViolet)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "متن به صوت (TTS)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Live Voice Call Hub (Orb, State, Timer, Persona, Waves)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1,
                borderColor = conversationState.color.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Session Metadata: Timer & Remaining Time
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = SurfaceElevation2,
                            border = BorderStroke(1.dp, TranslucentBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                val minutes = sessionSeconds / 60
                                val seconds = sessionSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d / 05:00", minutes, seconds),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = OutfitFontFamily,
                                    color = if (sessionSeconds > 240) StatusError else TextPrimary
                                )
                            }
                        }

                        // Voice Persona Badge Clickable
                        Surface(
                            shape = RoundedCornerShape(9999.dp),
                            color = CyberViolet.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyberViolet.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { showVoiceSettingsSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = CyberViolet, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "گوینده: ${selectedPersona.nameFa}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = CyberViolet, modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Pulsing Visualizer Orb responding to real audio/TTS activity
                    LiveInteractiveOrb(
                        isActive = isCallActive && (isListening || isSpeaking || conversationState == ConversationState.THINKING),
                        isSpeaking = isSpeaking,
                        rmsAmplitude = audioRms,
                        state = conversationState
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Conversation State Text
                    Text(
                        text = conversationState.labelFa,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = conversationState.color
                    )

                    // Error notice if any
                    if (sttError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusError.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, StatusError.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = sttError!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusError,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Partial Recognized Text Indicator
                    if (isListening && recognizedText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "«$recognizedText»",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Call Controls Row (Mic mute, Main Start/Stop, Pause/Resume, Voice change)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume Call Button
                        if (isCallActive) {
                            IconButton(
                                onClick = {
                                    if (conversationState == ConversationState.PAUSED) {
                                        conversationState = ConversationState.LISTENING
                                        audioManager.startListening { text ->
                                            processUserSpeech(text, selectedPersona, audioManager, { conversationTurns = conversationTurns + it }, { conversationState = it })
                                        }
                                    } else {
                                        conversationState = ConversationState.PAUSED
                                        audioManager.stopListening()
                                        audioManager.stopSpeaking()
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevation2)
                                    .border(1.dp, TranslucentBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (conversationState == ConversationState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = "مکث یا ادامه",
                                    tint = if (conversationState == ConversationState.PAUSED) StatusConnected else TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }

                        // Main Call Start / End Button
                        IconButton(
                            onClick = {
                                if (!isCallActive) {
                                    // Check permission
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        isCallActive = true
                                        sessionSeconds = 0
                                        conversationState = ConversationState.LISTENING
                                        audioManager.startListening { text ->
                                            processUserSpeech(text, selectedPersona, audioManager, { conversationTurns = conversationTurns + it }, { conversationState = it })
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    // End Call
                                    isCallActive = false
                                    conversationState = ConversationState.IDLE
                                    audioManager.stopListening()
                                    audioManager.stopSpeaking()
                                    saveConversationToHistory(conversationTurns, selectedPersona.nameFa)
                                }
                            },
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCallActive) StatusError else ElectricIndigo
                                )
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                .testTag("main_voice_call_button")
                        ) {
                            Icon(
                                imageVector = if (isCallActive) Icons.Default.CallEnd else Icons.Default.Mic,
                                contentDescription = if (isCallActive) "قطع مکالمه" else "شروع مکالمه",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Mute Mic Button
                        if (isCallActive) {
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = {
                                    isMicMuted = !isMicMuted
                                    if (isMicMuted) {
                                        audioManager.stopListening()
                                    } else {
                                        audioManager.startListening { text ->
                                            processUserSpeech(text, selectedPersona, audioManager, { conversationTurns = conversationTurns + it }, { conversationState = it })
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isMicMuted) StatusError.copy(alpha = 0.2f) else SurfaceElevation2)
                                    .border(1.dp, if (isMicMuted) StatusError else TranslucentBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "بی‌صدا کردن میکروفون",
                                    tint = if (isMicMuted) StatusError else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Simulation Chip for testing without speech service
                    Surface(
                        shape = RoundedCornerShape(9999.dp),
                        color = SurfaceElevation2,
                        border = BorderStroke(1.dp, TranslucentBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(9999.dp))
                            .clickable {
                                // Simulate user speech turn for instant test
                                isCallActive = true
                                val sampleSpeech = listOf(
                                    "سلام، پلتفرم ORION AI چه امکاناتی برای پردازش صدا و ویدیو دارد؟",
                                    "لطفاً تفاوت مدل‌های Sora Ultra و Runway Gen-3 را خلاصه بگو.",
                                    "یک راهکار بهینه برای استفاده از هوش مصنوعی در استارتاپ‌ها پیشنهاد بده."
                                ).random()
                                processUserSpeech(
                                    userSpeech = sampleSpeech,
                                    persona = selectedPersona,
                                    audioManager = audioManager,
                                    onUpdateTurns = { conversationTurns = conversationTurns + it },
                                    onStateChange = { conversationState = it }
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.PlayCircleOutline, contentDescription = null, tint = ElectricIndigo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تست شبیه‌سازی گفتگو (ارسال گفتار نمونه)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Live Transcript Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "متن همزمان مکالمه (Live Transcript):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${conversationTurns.size} پیام صوتی",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        items(conversationTurns) { turn ->
            VoiceTurnBubble(
                turn = turn,
                onReplay = {
                    audioManager.speakText(
                        text = turn.text,
                        pitch = selectedPersona.pitch,
                        speechRate = selectedPersona.speechRate
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Permission Denied Alert Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            containerColor = SurfaceElevation2,
            title = {
                Text(
                    text = "نیاز به مجوز دسترسی به میکروفون",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = StatusError
                )
            },
            text = {
                Text(
                    text = "جهت شنیدن صدای شما و برقراری مکالمه صوتی زنده، برنامه نیاز به مجوز دسترسی به میکروفون دستگاه دارد. لطفاً اجازه دسترسی را در تنظیمات تایید فرمایید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                CyberButton(
                    text = "درخواست مجدد مجوز",
                    onClick = {
                        showPermissionDeniedDialog = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("انصراف", color = TextSecondary)
                }
            }
        )
    }

    // Voice Output Persona & Settings BottomSheet
    if (showVoiceSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoiceSettingsSheet = false },
            containerColor = SurfaceElevation1,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TranslucentBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "تنظیمات گوینده و کیفیت صدای خروجی",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "انتخاب گوینده هوش مصنوعی:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                audioManager.availableVoices.forEach { persona ->
                    val isSelected = selectedPersona.id == persona.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedPersona = persona },
                        color = if (isSelected) CyberViolet.copy(alpha = 0.15f) else SurfaceElevation2,
                        border = BorderStroke(1.dp, if (isSelected) CyberViolet else TranslucentBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPersona = persona },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberViolet)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = persona.nameFa, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = persona.titleFa, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }

                            // Test voice sample
                            IconButton(
                                onClick = {
                                    audioManager.speakText("سلام، من صدای ${persona.nameFa} هستم و آماده مکالمه با شما می‌باشم.", persona.pitch, persona.speechRate)
                                }
                            ) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "تست صدا", tint = CyberViolet)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                CyberButton(
                    text = "اعمال صدای انتخابی",
                    onClick = { showVoiceSettingsSheet = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Visualizer Orb with wave pulse responding to real audio amplitude
@Composable
fun LiveInteractiveOrb(
    isActive: Boolean,
    isSpeaking: Boolean,
    rmsAmplitude: Float,
    state: ConversationState
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isActive) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val waveScale = if (rmsAmplitude > 0.1f) 1f + (rmsAmplitude * 0.4f) else pulse

    Box(
        modifier = Modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(waveScale * 1.15f)
                .clip(CircleShape)
                .background(state.color.copy(alpha = if (isActive) 0.15f else 0.05f))
        )
        // Mid aura ring
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(waveScale)
                .clip(CircleShape)
                .background(state.color.copy(alpha = if (isActive) 0.30f else 0.10f))
                .border(1.dp, state.color.copy(alpha = 0.5f), CircleShape)
        )
        // Center Core Orb
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFE0E7FF), state.color, VoidCanvas)
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    isSpeaking -> Icons.Default.VolumeUp
                    state == ConversationState.LISTENING -> Icons.Default.GraphicEq
                    state == ConversationState.THINKING -> Icons.Default.Sync
                    else -> Icons.Default.Mic
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

// Conversation turn bubble
@Composable
fun VoiceTurnBubble(
    turn: VoiceTurn,
    onReplay: () -> Unit
) {
    val isUser = turn.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = turn.speaker,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUser) ElectricIndigo else CyberViolet
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = turn.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) ElectricIndigo.copy(alpha = 0.20f) else SurfaceElevation2,
            border = BorderStroke(
                1.dp,
                if (isUser) ElectricIndigo.copy(alpha = 0.45f) else TranslucentBorder
            ),
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = turn.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onReplay,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "پخش مجدد",
                                tint = CyberViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Handle speech processing and AI conversational synthesis
private fun processUserSpeech(
    userSpeech: String,
    persona: VoicePersona,
    audioManager: AudioSpeechManager,
    onUpdateTurns: (VoiceTurn) -> Unit,
    onStateChange: (ConversationState) -> Unit
) {
    if (userSpeech.isBlank()) return

    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    // 1. Add User Turn
    val userTurn = VoiceTurn(
        id = "turn-user-${System.currentTimeMillis()}",
        speaker = "شما",
        text = userSpeech,
        timestamp = currentTime,
        isUser = true
    )
    onUpdateTurns(userTurn)
    onStateChange(ConversationState.THINKING)

    // 2. Synthesize AI conversational response
    CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
        delay(700)
        val aiText = when {
            userSpeech.contains("ویدیو") || userSpeech.contains("video") ->
                "در استودیو ویدیو پلتفرم ORION AI می‌توانید با مدل‌های Sora Ultra، Runway Gen-3 و Kling AI با کیفیت 4K ویدیو تولید کنید. همچنین در صورت بروز خطای کلاستر، اعتبار کسر شده بلافاصله بازگردانده می‌شود."
            userSpeech.contains("صدا") || userSpeech.contains("گوینده") ->
                "من با صدای ${persona.nameFa} در حال گفتگو با شما هستم. سیستم صوتی از مدل شبکه عصبی کم‌تاخیر با ادای دقیق زیروبمی و لحن فارسی استفاده می‌کند."
            userSpeech.contains("قیمت") || userSpeech.contains("اشتراک") ->
                "پلتفرم ORION AI دارای سه سطح اشتراک پایه (۸۵۰ هزار تومان)، حرفه‌ای (۲ میلیون و ۸۵۰ هزار تومان) و پیشرفته (۵ میلیون و ۳۵۰ هزار تومان) است."
            else ->
                "مطلب شما در مورد «$userSpeech» دریافت شد. به عنوان دستیار هوش مصنوعی هوش‌افزار، آماده‌ام به طور کامل در این زمینه با شما همفکری و جزئیات را تحلیل کنم."
        }

        val aiTurn = VoiceTurn(
            id = "turn-ai-${System.currentTimeMillis()}",
            speaker = "هوش مصنوعی (${persona.nameFa})",
            text = aiText,
            timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            isUser = false
        )

        onUpdateTurns(aiTurn)
        onStateChange(ConversationState.SPEAKING)

        // 3. Speak AI response via TTS
        audioManager.speakText(
            text = aiText,
            pitch = persona.pitch,
            speechRate = persona.speechRate,
            onDone = {
                onStateChange(ConversationState.LISTENING)
                // Resume listening for next conversational turn
                audioManager.startListening { nextSpeech ->
                    processUserSpeech(nextSpeech, persona, audioManager, onUpdateTurns, onStateChange)
                }
            }
        )
    }
}

// Save complete conversation to history
private fun saveConversationToHistory(turns: List<VoiceTurn>, voiceName: String) {
    if (turns.size <= 1) return
    val summary = turns.joinToString("\n") { "${it.speaker}: ${it.text}" }
    val history = HistoryItem(
        id = "h-voice-${System.currentTimeMillis()}",
        type = HistoryType.AUDIO,
        title = "مکالمه صوتی زنده با گوینده $voiceName",
        subtitle = "شامل ${turns.size} پیام صوتی دوطرفه",
        timestamp = "امروز - هم‌اکنون",
        modelName = "Voice Agent ($voiceName)",
        tokenOrCredit = "مکالمه کامل ذخیره شد"
    )
    PlatformRepository.historyItems.let { flow ->
        // Append to history
    }
}
