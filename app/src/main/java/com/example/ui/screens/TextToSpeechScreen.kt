package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSpeechManager
import com.example.data.PlatformRepository
import com.example.model.HistoryItem
import com.example.model.HistoryType
import com.example.model.VoicePersona
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun TextToSpeechScreen() {
    val context = LocalContext.current
    val audioManager = remember { AudioSpeechManager(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioManager.destroy()
        }
    }

    val isTtsReady by audioManager.isTtsReady.collectAsState()
    val isSpeaking by audioManager.isSpeaking.collectAsState()
    val ttsError by audioManager.ttsError.collectAsState()

    var textInput by remember {
        mutableStateOf("درود بر همراهان گرامی. به پلتفرم هوش مصنوعی ORION AI خوش آمدید. این سامانه زرهی و عصبی با استفاده از عمیق‌ترین مدل‌های یادگیری ماشین، امکان سنتز طبیعی گفتار فارسی را با حفظ تنالیته احساسی فراهم می‌آورد.")
    }

    var selectedVoice by remember { mutableStateOf(audioManager.availableVoices.first()) }
    var pitch by remember { mutableFloatStateOf(1.0f) }
    var speechRate by remember { mutableFloatStateOf(1.0f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tts_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with service readiness status
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = CyberViolet.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تبدیل متن به گفتار (Text-to-Speech)",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "سنتز فوق‌العاده روان گفتار فارسی با لحن‌های انسانی و امکان کنترل صوت",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    StatusPill(
                        label = if (isTtsReady) "سرویس TTS متصل" else "در انتظار موتور صوتی...",
                        statusColor = if (isTtsReady) StatusConnected else StatusWarning
                    )
                }
            }
        }

        // Text Input Card with Samples
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "متن فارسی جهت خوانش صوتی:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("tts_input_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberViolet,
                        unfocusedBorderColor = TranslucentBorder,
                        focusedContainerColor = VoidCanvas,
                        unfocusedContainerColor = VoidCanvas,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preset text samples
                Text(
                    text = "نمونه متن‌های پیشنهادی جهت تست:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val samples = listOf(
                        "روایت داستانی و پادکست آرام",
                        "خبر فوری و گویندگی رسمی رادیو",
                        "شعر کهن پارسی: بشنو از نی چون حکایت می‌کند",
                        "متن انگیزشی پرانرژی برای ویدیو"
                    )
                    items(samples) { sample ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    textInput = when (sample) {
                                        "شعر کهن پارسی: بشنو از نی چون حکایت می‌کند" -> "بشنو از نی چون حکایت می‌کند، از جدایی‌ها شکایت می‌کند. کز نیستان تا مرا ببریده‌اند، در نفیرم مرد و زن نالیده‌اند."
                                        "خبر فوری و گویندگی رسمی رادیو" -> "به گزارش روابط عمومی کلاستر هوش مصنوعی ORION AI، نسل جدید پردازنده‌های عصبی با افزایش سرعت چهار برابری در دسترس کاربران قرار گرفت."
                                        else -> "سلام و درود به شنوندگان گرامی. در این قسمت از پادکست، به بررسی چشم‌انداز پیشرفت‌های شگفت‌انگیز هوش مصنوعی در سال پیش رو می‌پردازیم."
                                    }
                                },
                            color = SurfaceElevation2,
                            border = BorderStroke(1.dp, TranslucentBorder)
                        ) {
                            Text(
                                text = sample,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Voice Persona Selection
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "انتخاب گوینده هوش مصنوعی (Voice Persona):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                audioManager.availableVoices.forEach { persona ->
                    val isSelected = selectedVoice.id == persona.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedVoice = persona
                                pitch = persona.pitch
                                speechRate = persona.speechRate
                            },
                        color = if (isSelected) CyberViolet.copy(alpha = 0.16f) else SurfaceElevation2,
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
                                    onClick = {
                                        selectedVoice = persona
                                        pitch = persona.pitch
                                        speechRate = persona.speechRate
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberViolet)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = persona.nameFa, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = persona.titleFa, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }

                            StatusPill(
                                label = persona.gender,
                                statusColor = if (persona.gender == "زن") CyberViolet else ElectricIndigo
                            )
                        }
                    }
                }
            }
        }

        // Sliders: Pitch & Speed Rate
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "تنظیمات آوایی و فرکانس صدا:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "زیر و بمی صدا (Pitch):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(text = String.format("%.2fx", pitch), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CyberViolet, fontFamily = OutfitFontFamily)
                }
                Slider(
                    value = pitch,
                    onValueChange = { pitch = it },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberViolet,
                        activeTrackColor = CyberViolet,
                        inactiveTrackColor = SurfaceElevation3
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Speech Rate Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "سرعت خوانش (Speed Rate):", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text(text = String.format("%.2fx", speechRate), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ElectricIndigo, fontFamily = OutfitFontFamily)
                }
                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricIndigo,
                        activeTrackColor = ElectricIndigo,
                        inactiveTrackColor = SurfaceElevation3
                    )
                )
            }
        }

        // Audio Player Controls & Waveform Visualizer
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation2.copy(alpha = 0.9f),
                borderColor = if (isSpeaking) CyberViolet else ActiveBorder
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animated Speech Wave Bars
                    SpeechWaveBars(isSpeaking = isSpeaking)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSpeaking) "در حال پخش گفتار با صدای ${selectedVoice.nameFa}..." else "آماده پخش گفتار",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpeaking) CyberViolet else TextSecondary
                    )

                    if (ttsError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = ttsError!!, style = MaterialTheme.typography.labelSmall, color = StatusError)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Player Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stop Button
                        IconButton(
                            onClick = { audioManager.stopSpeaking() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevation3)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "توقف", tint = TextPrimary)
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        // Play/Pause Main Button
                        IconButton(
                            onClick = {
                                if (isSpeaking) {
                                    audioManager.stopSpeaking()
                                } else {
                                    audioManager.speakText(
                                        text = textInput,
                                        pitch = pitch,
                                        speechRate = speechRate
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(CyberViolet)
                                .testTag("tts_play_button")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isSpeaking) "مکث" else "پخش گفتار",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        // Save to History Button
                        IconButton(
                            onClick = {
                                val item = HistoryItem(
                                    id = "h-tts-${System.currentTimeMillis()}",
                                    type = HistoryType.AUDIO,
                                    title = "تولید صوت با گوینده ${selectedVoice.nameFa}",
                                    subtitle = textInput.take(30) + "...",
                                    timestamp = "امروز - هم‌اکنون",
                                    modelName = "TTS Engine (${selectedVoice.nameFa})",
                                    tokenOrCredit = "رایگان"
                                )
                                Toast.makeText(context, "فایل صوتی در تاریخچه ذخیره شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevation3)
                        ) {
                            Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = "ذخیره در تاریخچه", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// Waveform visualizer bars
@Composable
fun SpeechWaveBars(isSpeaking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_bars")

    Row(
        modifier = Modifier
            .height(44.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCounts = 14
        for (i in 0 until barCounts) {
            val animDuration = 400 + (i * 60)
            val barHeight by infiniteTransition.animateFloat(
                initialValue = 6f,
                targetValue = if (isSpeaking) (16f + (i % 5) * 6f) else 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(animDuration, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isSpeaking) CyberViolet else SurfaceElevation3)
            )
        }
    }
}
