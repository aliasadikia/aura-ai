package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlatformRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ImageStudioScreen() {
    var prompt by remember { mutableStateOf("پرتره زن سایبرنتیک با بازتاب نورهای نئونی در کوچه بارانی تهران با کابل‌های نورانی ظریف") }
    var selectedModel by remember { mutableStateOf("FLUX.1 Pro") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var selectedStyle by remember { mutableStateOf("فتورئال (Photorealistic)") }
    var isGenerating by remember { mutableStateOf(false) }

    val systemAlert by PlatformRepository.systemAlert.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var downloadFeedback by remember { mutableStateOf<String?>(null) }

    val models = listOf("FLUX.1 Pro", "Midjourney v6.1", "DALL-E 3", "Stable Diffusion 3.5")
    val styles = listOf("فتورئال (Photorealistic)", "سایبرپانک (Cyberpunk)", "انیمه (Anime)", "رندر سه‌بعدی (3D Octane)", "مینیاتور ایرانی")
    val ratios = listOf("1:1", "16:9", "9:16", "4:5")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("image_studio_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Alert Banner
        if (systemAlert != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = if (systemAlert!!.contains("کافی نیست") || systemAlert!!.contains("مسدود")) StatusError.copy(alpha = 0.2f) else StatusConnected.copy(alpha = 0.2f),
                    borderColor = if (systemAlert!!.contains("کافی نیست") || systemAlert!!.contains("مسدود")) StatusError else StatusConnected
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = systemAlert!!, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { PlatformRepository.dismissAlert() }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextPrimary)
                        }
                    }
                }
            }
        }

        // Studio Header

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = PhotonBlue.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "استودیو تصویر هوشمند ORION AI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "تولید تصاویر هنری و صنعتی با بالاترین وضوح و جزئیات دقیق",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    StatusPill(label = "کیفیت 4K فعال", statusColor = PhotonBlue)
                }
            }
        }

        // Prompt Input Field
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "توصیف صحنه یا پرتره (Prompt):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("image_prompt_input"),
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

                Spacer(modifier = Modifier.height(12.dp))

                // Models Selector
                Text(
                    text = "موتور تولید تصویر:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(models) { m ->
                        CyberChip(
                            text = m,
                            isSelected = selectedModel == m,
                            onClick = { selectedModel = m }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Style Selector
                Text(
                    text = "سبک بصری (Visual Style):",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(styles) { s ->
                        CyberChip(
                            text = s,
                            isSelected = selectedStyle == s,
                            onClick = { selectedStyle = s }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ratio Selector
                Text(
                    text = "نسبت کادر تصویر:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ratios.forEach { r ->
                        CyberChip(
                            text = r,
                            isSelected = selectedRatio == r,
                            onClick = { selectedRatio = r },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CyberButton(
                    text = if (isGenerating) "در حال پردازش تانسورها..." else "خلق تصویر با $selectedModel (۱۵ اعتبار)",
                    onClick = {
                        isGenerating = true
                        coroutineScope.launch {
                            delay(1200)
                            PlatformRepository.generateImage(prompt, selectedModel, selectedStyle, selectedRatio)
                            isGenerating = false
                        }
                    },
                    enabled = !isGenerating,
                    icon = Icons.Default.AutoFixHigh,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "generate_image_button"
                )
            }
        }

        // Generated Images Gallery Preview
        item {
            Text(
                text = "گالری آخرین رندرهای شما:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                // Show Generated Cybernetic Asset
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoidCanvas)
                        .border(1.dp, ActiveBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aura_app_icon_1793994962108),
                        contentDescription = "رندر شده با هوش مصنوعی",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "هسته کوانتومی سایبرنتیک (Neural Orb)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "موتور: FLUX.1 Pro • وضوح: 2048x2048 • نسبت: 1:1",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            downloadFeedback = "تصویر 4K با موفقیت در حافظه دانلود گردید."
                        }) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "دانلود", tint = ElectricIndigo)
                        }
                        IconButton(onClick = {
                            downloadFeedback = "لینک اشتراک‌گذاری و پرامپت کپی شد."
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "اشتراک", tint = TextSecondary)
                        }
                    }
                }

                if (downloadFeedback != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = downloadFeedback!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusConnected
                    )
                }
            }
        }


        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
