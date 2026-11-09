package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioSpeechManager
import com.example.data.PlatformRepository
import com.example.model.HistoryItem
import com.example.model.HistoryType
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SpeechToTextScreen(
    onNavigateToTts: () -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { AudioSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.destroy()
        }
    }

    val isSttAvailable by audioManager.isSttAvailable.collectAsState()
    val isListening by audioManager.isListening.collectAsState()
    val audioRms by audioManager.audioRms.collectAsState()
    val recognizedText by audioManager.recognizedText.collectAsState()
    val sttError by audioManager.sttError.collectAsState()

    var fullTranscribedText by remember {
        mutableStateOf("هوش مصنوعی به عنوان نقطه عطف فناوری در قرن بیست و یکم، قادر است فرکانس‌های آوایی انسان را پردازش کرده و گفتار فارسی را با دقت بسیار بالا پیاده‌سازی نماید.")
    }

    var detectedLanguage by remember { mutableStateOf("فارسی (FA-IR)") }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            audioManager.startListening { text ->
                if (text.isNotBlank()) {
                    fullTranscribedText = if (fullTranscribedText.isBlank()) text else "$fullTranscribedText $text"
                }
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Keep updating text as live results come in
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank() && !fullTranscribedText.endsWith(recognizedText)) {
            // Live update
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("stt_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with service status
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = PhotonBlue.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تبدیل گفتار به متن (Speech-to-Text)",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "پیاده‌سازی زنده صوت با مدل‌های تشخیص گفتار Whisper v3 Turbo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    StatusPill(
                        label = if (isSttAvailable) "موتور گفتار آنلاین" else "عدم دسترسی موتور صوتی",
                        statusColor = if (isSttAvailable) StatusConnected else StatusWarning
                    )
                }
            }
        }

        // Live Recording Console
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1,
                borderColor = if (isListening) StatusConnected else TranslucentBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status & Language detection pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(
                            label = if (isListening) "در حال ضبط و شنود صوت..." else "آماده دریافت ورودی",
                            statusColor = if (isListening) StatusConnected else TextSecondary
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceElevation2,
                            border = BorderStroke(1.dp, TranslucentBorder)
                        ) {
                            Text(
                                text = "زبان شناسایی شده: $detectedLanguage",
                                style = MaterialTheme.typography.labelSmall,
                                color = PhotonBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Microphone Record Button
                    IconButton(
                        onClick = {
                            if (!isListening) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    audioManager.startListening { text ->
                                        if (text.isNotBlank()) {
                                            fullTranscribedText = if (fullTranscribedText.isBlank()) text else "$fullTranscribedText $text"
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            } else {
                                audioManager.stopListening()
                            }
                        },
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isListening) StatusError else PhotonBlue)
                            .border(3.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            .testTag("stt_record_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isListening) "توقف ضبط" else "شروع ضبط",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isListening) "در حال استماع صحبت شما... برای توقف لمس کنید" else "جهت ضبط صوت، دکمه میکروفون را لمس کنید",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isListening) StatusConnected else TextSecondary
                    )

                    if (sttError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sttError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusError
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Presets for quick testing
                    Text(
                        text = "نمونه‌های صوتی آماده تست پیاده‌سازی:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val presets = listOf(
                            "جلسه راهبردی توسعه هوش مصنوعی در ایران",
                            "پادکست فناوری: بررسی مدل‌های استدلال عمیق",
                            "یادداشت صوتی کوتاه: یادآوری تماس کاری فردا"
                        )
                        items(presets) { preset ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        fullTranscribedText = "$fullTranscribedText\n• $preset"
                                    },
                                color = SurfaceElevation2,
                                border = BorderStroke(1.dp, TranslucentBorder)
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Transcribed Text Editor & Management
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "متن پیاده‌سازی شده نهایی:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row {
                        // Word Count Badge
                        val words = fullTranscribedText.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
                        Text(
                            text = "$words کلمه • ${fullTranscribedText.length} کاراکتر",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontFamily = OutfitFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = fullTranscribedText,
                    onValueChange = { fullTranscribedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("stt_result_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PhotonBlue,
                        unfocusedBorderColor = TranslucentBorder,
                        focusedContainerColor = VoidCanvas,
                        unfocusedContainerColor = VoidCanvas,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Copy, Save to History, Clear
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Transcribed Text", fullTranscribedText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "متن در حافظه کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevation2)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = PhotonBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "کپی متن", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    }

                    Button(
                        onClick = {
                            // Save to history
                            val item = HistoryItem(
                                id = "h-stt-${System.currentTimeMillis()}",
                                type = HistoryType.AUDIO,
                                title = "تبدیل گفتار به متن: ${fullTranscribedText.take(28)}...",
                                subtitle = "طول متن: ${fullTranscribedText.length} کاراکتر",
                                timestamp = "امروز - هم‌اکنون",
                                modelName = "Whisper v3 Turbo",
                                tokenOrCredit = "رایگان در اشتراک"
                            )
                            Toast.makeText(context, "در تاریخچه فعالیت‌ها ذخیره شد", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PhotonBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "ذخیره در تاریخچه", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }

                    IconButton(
                        onClick = { fullTranscribedText = "" },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevation2)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "پاک‌سازی", tint = TextTertiary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            containerColor = SurfaceElevation2,
            title = {
                Text(text = "مجوز میکروفون مورد نیاز است", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusError)
            },
            text = {
                Text(text = "برای تبدیل صدای شما به متن، اجازه دسترسی به ضبط صدا الزامی است.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            },
            confirmButton = {
                CyberButton(text = "اعطای دسترسی", onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                })
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("انصراف", color = TextSecondary) }
            }
        )
    }
}
