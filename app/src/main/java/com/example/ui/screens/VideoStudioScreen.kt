package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlatformRepository
import com.example.model.AppScreen
import com.example.model.JobStatus
import com.example.model.VideoGenerationModel
import com.example.model.VideoJob
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun VideoStudioScreen(
    onNavigateToPricing: () -> Unit
) {
    val videoCredits by PlatformRepository.videoCredits.collectAsState()
    val reservedCredits by PlatformRepository.reservedCredits.collectAsState()
    val videoModels by PlatformRepository.videoModels.collectAsState()
    val videoJobs by PlatformRepository.videoJobs.collectAsState()
    val systemAlert by PlatformRepository.systemAlert.collectAsState()

    var promptText by remember { mutableStateOf("سایبرپانک تهران در سال ۲۰۸۰ با برج میلاد نورانی و اتومبیل‌های معلق در آسمان بارانی شب") }
    var selectedModel by remember { mutableStateOf(PlatformRepository.videoModels.value[1]) } // Runway Gen-3
    var selectedRatio by remember { mutableStateOf("16:9") }
    var selectedStyle by remember { mutableStateOf("سینمایی (Cinematic)") }
    var selectedMotion by remember { mutableStateOf("پرواز پهپادی (Drone Orbit)") }
    var durationSec by remember { mutableIntStateOf(5) }
    var simulateFailureNext by remember { mutableStateOf(false) }

    // Cost calculation: (costPer5Sec * (durationSec / 5))
    val estimatedCost = (selectedModel.costPer5Sec * (durationSec / 5)).coerceAtLeast(selectedModel.costPer5Sec)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("video_studio_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Alert Banner
        if (systemAlert != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (systemAlert!!.contains("خطا") || systemAlert!!.contains("کافی نیست"))
                        StatusError.copy(alpha = 0.2f) else StatusConnected.copy(alpha = 0.2f),
                    borderColor = if (systemAlert!!.contains("خطا") || systemAlert!!.contains("کافی نیست"))
                        StatusError else StatusConnected
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = systemAlert!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { PlatformRepository.dismissAlert() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        // Header & Credit Pool Overview
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = CyberViolet.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "استودیو رندر ویدیو ORION AI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "موتور رندر توزیع‌شده با قابلیت بیمه اعتبار و بازگشت خودکار (Rollback)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    VideoCreditPill(
                        credits = videoCredits,
                        onClick = onNavigateToPricing
                    )
                }
            }
        }

        // Prompt Input
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1,
                borderColor = TranslucentBorder
            ) {
                Text(
                    text = "توصیف صحنه ویدیویی (Prompt):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("video_prompt_input"),
                    placeholder = {
                        Text(
                            text = "صحنه، کاراکتر، نورپردازی و زاویه دید دوربین را به فارسی یا انگلیسی بنویسید...",
                            color = TextTertiary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = TranslucentBorder,
                        focusedContainerColor = VoidCanvas,
                        unfocusedContainerColor = VoidCanvas,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                // Sample Prompts Chips
                Text(
                    text = "پرامپت‌های آماده پیشنهادی:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val samples = listOf(
                        "سایبرپانک تهران ۲۰۸۰ در شب بارانی",
                        "ربات نقاش در باغ سنتی اصفهان با سبک مینیاتور",
                        "حرکت تایم‌لپس کهکشان بر فراز کویر لوت با نور شفق",
                        "شیر بالدار هخامنشی جان‌گرفته در پارسه با وضوح سینمایی"
                    )
                    items(samples) { sample ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { promptText = sample },
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

        // Model Selector with Deduction Rules
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
                        text = "انتخاب مدل تولید ویدیو و نرخ کسر اعتبار:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "قوانین محاسبه دقیق",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    videoModels.forEach { model ->
                        val isSelected = selectedModel.id == model.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedModel = model }
                                .testTag("video_model_select_${model.id}"),
                            color = if (isSelected) ElectricIndigo.copy(alpha = 0.15f) else SurfaceElevation2,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) ElectricIndigo else TranslucentBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedModel = model },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = ElectricIndigo,
                                            unselectedColor = TextTertiary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = model.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = OutfitFontFamily,
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = SurfaceElevation3
                                            ) {
                                                Text(
                                                    text = model.provider,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${model.tag} • وضوح ${model.resolution}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "${model.costPer5Sec} اعتبار",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyberViolet else TextPrimary,
                                        fontFamily = OutfitFontFamily
                                    )
                                    Text(
                                        text = "به ازای هر ۵ ثانیه",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Parameters: Duration, Ratio, Motion, Style
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "پارامترهای فیلم‌برداری و خروجی:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Selector (5s, 10s, 15s)
                Text(
                    text = "مدت زمان ویدیو: $durationSec ثانیه",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15).forEach { sec ->
                        CyberChip(
                            text = "$sec ثانیه",
                            isSelected = durationSec == sec,
                            onClick = { durationSec = sec },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Aspect Ratio Selector
                Text(
                    text = "نسبت ابعاد کادر:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("16:9 (سینمایی)", "9:16 (استوری/ریلز)", "1:1 (مربعی)").forEach { ratio ->
                        CyberChip(
                            text = ratio,
                            isSelected = selectedRatio == ratio,
                            onClick = { selectedRatio = ratio },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera Motion Selector
                Text(
                    text = "حرکت دوربین (Camera Movement):",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("پرواز پهپادی", "زوم نرم", "چرخش مداری").forEach { motion ->
                        CyberChip(
                            text = motion,
                            isSelected = selectedMotion.contains(motion),
                            onClick = { selectedMotion = motion },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Estimated Cost & Action Section
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation2.copy(alpha = 0.9f),
                borderColor = ActiveBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "هزینه تخمینی رندر:",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$estimatedCost",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberViolet,
                                fontFamily = OutfitFontFamily
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اعتبار ویدیو",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "مدل: ${selectedModel.name} ($durationSec ثانیه)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "موجودی شما:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "$videoCredits اعتبار",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (videoCredits >= estimatedCost) StatusConnected else StatusError,
                            fontFamily = OutfitFontFamily
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rollback Test Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevation3.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تست شبیه‌سازی خطا و بازگشت اعتبار (Rollback Test)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusWarning
                        )
                        Text(
                            text = "فعال‌سازی برای اثبات بازگرداندن خودکار اعتبار کسر شده در صورت بروز خطای سرور",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = simulateFailureNext,
                        onCheckedChange = { simulateFailureNext = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusWarning
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                CyberButton(
                    text = "تولید ویدیو ($estimatedCost اعتبار)",
                    onClick = {
                        PlatformRepository.submitVideoJob(
                            prompt = promptText,
                            model = selectedModel,
                            durationSec = durationSec,
                            aspectRatio = selectedRatio,
                            motion = selectedMotion,
                            simulateFailure = simulateFailureNext
                        )
                        // Reset test flag
                        simulateFailureNext = false
                    },
                    icon = Icons.Default.AutoFixHigh,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "generate_video_button"
                )
            }
        }

        // Async Job Queue & Results Tracking
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "صف رهگیری جاب‌های ویدیویی (Video Jobs Queue)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${videoJobs.size} جاب ثبت شده",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        items(videoJobs) { job ->
            VideoJobTrackingCard(job = job)
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun VideoJobTrackingCard(job: VideoJob) {
    val statusColor = when (job.status) {
        JobStatus.QUEUE -> StatusWarning
        JobStatus.PROCESSING -> StatusProcessing
        JobStatus.SUCCESS -> StatusConnected
        JobStatus.FAILED -> StatusError
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("video_job_${job.id}"),
        backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
        borderColor = statusColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(
                    label = job.status.labelFa,
                    statusColor = statusColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = job.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = OutfitFontFamily
                )
            }
            Text(
                text = job.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = job.prompt,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مدل: ${job.model.name} • ${job.durationSec} ثانیه • کادر ${job.aspectRatio}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "${job.creditCost} اعتبار",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyberViolet,
                fontFamily = OutfitFontFamily
            )
        }

        // Progress bar for Processing & Queue
        if (job.status == JobStatus.PROCESSING || job.status == JobStatus.QUEUE) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (job.status == JobStatus.QUEUE) "در انتظار گره خالی GPU..." else "پیشرفت رندر و سنتز فریم‌ها...",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "${job.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ElectricIndigo,
                    fontFamily = OutfitFontFamily
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { job.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ElectricIndigo,
                trackColor = SurfaceElevation3
            )
        }

        // Error & Rollback details
        if (job.status == JobStatus.FAILED) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StatusError.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StatusError.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "علت خطا: ${job.errorMessage ?: "خطای ناشناخته در پردازش سرور"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusError,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🔄 بیمه خودکار اعتبار: مبلغ ${job.creditCost} اعتبار با موفقیت به حسابتان بازگردانده شد (Rollback Completed).",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        }

        // Success player preview
        if (job.status == JobStatus.SUCCESS) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(SurfaceElevation2, VoidCanvas)
                        )
                    )
                    .border(1.dp, TranslucentBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "پخش ویدیو",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "پیش‌نمایش ویدیو آماده دانلود (1080p 60fps)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
