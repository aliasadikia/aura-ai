package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioSpeechManager
import com.example.data.PlatformRepository
import com.example.model.ChatMessage
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val audioManager = remember { AudioSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.destroy()
        }
    }

    val messages by PlatformRepository.chatMessages.collectAsState()
    val textTokens by PlatformRepository.textTokensRemaining.collectAsState()
    val systemAlert by PlatformRepository.systemAlert.collectAsState()

    val isListening by audioManager.isListening.collectAsState()
    val audioRms by audioManager.audioRms.collectAsState()
    val liveRecognizedText by audioManager.recognizedText.collectAsState()
    val sttError by audioManager.sttError.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var activeModel by remember { mutableStateOf("GPT-4o") }
    var showParamsSheet by remember { mutableStateOf(false) }
    var showPermissionAlert by remember { mutableStateOf(false) }

    var temperature by remember { mutableFloatStateOf(0.7f) }
    var topP by remember { mutableFloatStateOf(0.9f) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val models = listOf("GPT-4o", "Claude 3.5 Sonnet", "DeepSeek R1", "Gemini 1.5 Pro")

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            audioManager.startListening { transcribed ->
                if (transcribed.isNotBlank()) {
                    inputText = if (inputText.isBlank()) transcribed else "$inputText $transcribed"
                }
            }
        } else {
            showPermissionAlert = true
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Pulsing animation for active microphone recording
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("chat_screen_content")
    ) {
        // System Alert Banner if any (e.g. Rate limit cooldown or STT error)
        val alertMessage = systemAlert ?: sttError
        if (alertMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                color = StatusError.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, StatusError)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alertMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDirection = TextDirection.ContentOrRtl
                        ),
                        textAlign = TextAlign.Start,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { PlatformRepository.dismissAlert() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Multi-Model Switcher Bar & Token Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceElevation1,
            border = BorderStroke(1.dp, TranslucentBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "هسته عصبی گفتگو:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ORION AI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyanTurquoise,
                            fontFamily = OutfitFontFamily
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(
                            label = "توکن‌های فعال: ${textTokens / 1000}K",
                            statusColor = CyanTurquoise
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { showParamsSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "تنظیمات مدل",
                                tint = CyanTurquoise,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(models) { model ->
                        CyberChip(
                            text = model,
                            isSelected = activeModel == model,
                            onClick = { activeModel = model },
                            icon = Icons.Default.AutoAwesome
                        )
                    }
                }
            }
        }

        // Messages List (RTL optimized)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg)
            }
        }

        // Real-Time Live STT Voice Feedback Panel (Displays during recording)
        AnimatedVisibility(
            visible = isListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = SurfaceElevation2,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, CyanTurquoise)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .scale(micScale)
                                    .clip(CircleShape)
                                    .background(StatusError)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "در حال ضبط و تبدیل زنده گفتار به متن (فارسی)...",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyanTurquoise
                            )
                        }

                        IconButton(
                            onClick = { audioManager.stopListening() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "تایید و ثبت گفتار",
                                tint = StatusConnected,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Audio Waveform Visualizer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val waveHeights = listOf(
                            (audioRms * 22f).coerceIn(4f, 26f),
                            (audioRms * 16f).coerceIn(4f, 20f),
                            (audioRms * 28f).coerceIn(6f, 28f),
                            (audioRms * 18f).coerceIn(4f, 22f),
                            (audioRms * 24f).coerceIn(5f, 26f),
                            (audioRms * 14f).coerceIn(4f, 18f),
                            (audioRms * 22f).coerceIn(4f, 24f)
                        )
                        waveHeights.forEachIndexed { index, heightDp ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .width(4.dp)
                                    .height(heightDp.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (index % 2 == 0) CyanTurquoise else NeuralGoldSpark
                                    )
                            )
                        }
                    }

                    if (liveRecognizedText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "« $liveRecognizedText »",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDirection = TextDirection.ContentOrRtl
                            ),
                            textAlign = TextAlign.Start,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Input Console Bar with STT & Send Action
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidCanvas)
                .border(1.dp, TranslucentBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(12.dp),
            color = VoidCanvas
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Production-Ready Microphone Audio Recording Button
                IconButton(
                    onClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPerm) {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                audioManager.stopListening()
                            } else {
                                audioManager.startListening { transcribedText ->
                                    if (transcribedText.isNotBlank()) {
                                        inputText = if (inputText.isBlank()) transcribedText else "$inputText $transcribedText"
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .scale(if (isListening) micScale else 1.0f)
                        .clip(CircleShape)
                        .background(
                            if (isListening) StatusError.copy(alpha = 0.25f) else SurfaceElevation2
                        )
                        .border(
                            1.5.dp,
                            if (isListening) StatusError else CyanTurquoise,
                            CircleShape
                        )
                        .testTag("chat_voice_stt_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "تبدیل گفتار به متن میکروفون",
                        tint = if (isListening) StatusError else CyanTurquoise,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Box (RTL Supported)
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "پیام خود را تایپ کنید یا دکمه میکروفون را لمس فرمایید...",
                            color = TextTertiary,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDirection = TextDirection.ContentOrRtl
                            ),
                            textAlign = TextAlign.Start
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.ContentOrRtl,
                        textAlign = TextAlign.Start,
                        color = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanTurquoise,
                        unfocusedBorderColor = TranslucentBorder,
                        focusedContainerColor = SurfaceElevation2,
                        unfocusedContainerColor = SurfaceElevation2,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Directional Send Button with AutoMirrored Icon for Persian RTL
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            PlatformRepository.sendChatMessage(inputText.trim(), activeModel)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (inputText.isNotBlank()) CyanTurquoise else SurfaceElevation3
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "ارسال پیام",
                        tint = if (inputText.isNotBlank()) VoidCanvas else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Permission Alert Dialog
    if (showPermissionAlert) {
        AlertDialog(
            onDismissRequest = { showPermissionAlert = false },
            title = {
                Text(
                    text = "دسترسی به میکروفون دستگاه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "جهت تبدیل زنده گفتار شما به متن فارسی در چت ORION AI، اعطای دسترسی به میکروفون الزامی است. لطفاً از طریق تنظیمات دستگاه این دسترسی را مجاز فرمایید.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionAlert = false
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("تلاش مجدد", color = CyanTurquoise, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionAlert = false }) {
                    Text("انصراف", color = TextTertiary)
                }
            },
            containerColor = SurfaceElevation2
        )
    }

    // Model Hyperparameters BottomSheet
    if (showParamsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showParamsSheet = false },
            containerColor = SurfaceElevation1,
            dragHandle = { BottomSheetDefaults.DragHandle(color = TranslucentBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "تنظیمات پارامترهای عصبی $activeModel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Temperature Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "میزان خلاقیت (Temperature):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format("%.2f", temperature),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanTurquoise,
                        fontFamily = OutfitFontFamily
                    )
                }
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0.0f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanTurquoise,
                        activeTrackColor = CyanTurquoise,
                        inactiveTrackColor = SurfaceElevation3
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Top-P Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "تنوع واژگانی (Top-P):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format("%.2f", topP),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeuralGoldSpark,
                        fontFamily = OutfitFontFamily
                    )
                }
                Slider(
                    value = topP,
                    onValueChange = { topP = it },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeuralGoldSpark,
                        activeTrackColor = NeuralGoldSpark,
                        inactiveTrackColor = SurfaceElevation3
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                CyberButton(
                    text = "ذخیره و اعمال پارامترها",
                    onClick = { showParamsSheet = false },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.isUser
    // In Persian RTL: Alignment.End is on Left (User outgoing), Alignment.Start is on Right (AI incoming)
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Model & Metadata Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (isUser) "شما" else "ORION AI • ${msg.modelName}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUser) CyanTurquoise else NeuralGoldSpark
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = msg.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
            if (!isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${msg.latencyMs}ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontFamily = OutfitFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bubble Body with RTL Corner Radius Geometry
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) CyanTurquoise.copy(alpha = 0.18f) else SurfaceElevation2,
            border = BorderStroke(
                1.dp,
                if (isUser) CyanTurquoise.copy(alpha = 0.5f) else TranslucentBorder
            ),
            modifier = Modifier
                .widthIn(max = 340.dp)
                .testTag("chat_bubble_${msg.id}")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDirection = TextDirection.ContentOrRtl
                    ),
                    textAlign = TextAlign.Start,
                    color = TextPrimary,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
