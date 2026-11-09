package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.PlatformRepository
import com.example.model.AppScreen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (AppScreen) -> Unit
) {
    val videoCredits by PlatformRepository.videoCredits.collectAsState()
    val textTokens by PlatformRepository.textTokensRemaining.collectAsState()
    val userPlan by PlatformRepository.userPlan.collectAsState()
    val subscriptionTiers by PlatformRepository.subscriptionTiers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section with ORION AI Cybernetic Branding
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = CyanTurquoise
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusPill(label = "کلاستر آنلاین • SLA 99.99%", statusColor = StatusConnected)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeuralGoldSpark.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeuralGoldSpark.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeuralGoldSpark,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "جرقه عصبی فعال",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeuralGoldSpark,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = "ORION CORE v4.2",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanTurquoise,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Futuristic Chrome Robot Avatar
                    Box(
                        modifier = Modifier.size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(ChromeMetalLight, ChromeMetalDark))
                                )
                                .border(2.dp, CyanTurquoise, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.aura_app_icon_1793994962108),
                                contentDescription = "ربات زرهی ORION AI",
                                modifier = Modifier
                                    .size(66.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(NeuralGoldSpark)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ORION",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontFamily = OutfitFontFamily,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanTurquoise.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CyanTurquoise)
                            ) {
                                Text(
                                    text = "AI",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = CyanTurquoise,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = OutfitFontFamily,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "سامانه جامع هوش مصنوعی و پردازش عصبی کوانتومی",
                            style = MaterialTheme.typography.bodySmall,
                            color = ChromeMetalLight,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "دسترسی مستقیم و پایدار به قدرتمندترین موتورهای هوش مصنوعی جهان با زره کروم، ویزور نوری فیروزه‌ای و هماهنگی کامل ریالی و فارسی.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CyberButton(
                        text = "شروع چت هوشمند",
                        onClick = { onNavigate(AppScreen.CHAT) },
                        icon = Icons.Default.ChatBubble,
                        modifier = Modifier.weight(1f),
                        testTag = "home_start_chat"
                    )
                    CyberButton(
                        text = "استودیو ویدیو",
                        onClick = { onNavigate(AppScreen.VIDEO_STUDIO) },
                        icon = Icons.Default.Videocam,
                        isSecondary = true,
                        modifier = Modifier.weight(1f),
                        testTag = "home_video_studio"
                    )
                }
            }
        }

        // Live Credit & Account Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Video Credit Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceElevation2.copy(alpha = 0.7f),
                    borderColor = CyberViolet.copy(alpha = 0.4f),
                    onClick = { onNavigate(AppScreen.PRICING) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberViolet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = CyberViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "اعتبار ویدیو",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$videoCredits",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = OutfitFontFamily
                    )
                    Text(
                        text = "+ شارژ استخر ویدیو",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Text Tokens Card
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceElevation2.copy(alpha = 0.7f),
                    borderColor = ElectricIndigo.copy(alpha = 0.4f),
                    onClick = { onNavigate(AppScreen.PRICING) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = ElectricIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "توکن متنی",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${textTokens / 1000}K",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = OutfitFontFamily
                    )
                    Text(
                        text = "طرح فعلی: ${userPlan.nameFa}",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusConnected,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Quick Studios Section
        item {
            Text(
                text = "استودیوهای تخصصی هوش مصنوعی",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudioNavCard(
                    title = "استودیو تولید و رندر ویدیو (Video Studio)",
                    subtitle = "تولید فیلم سینمایی با Sora, Runway Gen-3, Kling با امکان لغو و بازگشت اعتبار (Rollback)",
                    icon = Icons.Default.Videocam,
                    badge = "رهگیری بلادرنگ",
                    accentColor = CyberViolet,
                    onClick = { onNavigate(AppScreen.VIDEO_STUDIO) }
                )
                StudioNavCard(
                    title = "چت هوشمند چندمدلی (Multi-Model AI Chat)",
                    subtitle = "هم‌صحبتی و کدنویسی با GPT-4o, Claude 3.5 Sonnet, DeepSeek R1 با خروجی فارسی",
                    icon = Icons.Default.ChatBubble,
                    badge = "۴ مدل فعال",
                    accentColor = ElectricIndigo,
                    onClick = { onNavigate(AppScreen.CHAT) }
                )
                StudioNavCard(
                    title = "استودیو تصویر (Image Studio)",
                    subtitle = "خلق تصاویر فتورئال و هنری با Midjourney v6.1 و FLUX.1 Pro در ابعاد مختلف",
                    icon = Icons.Default.AutoFixHigh,
                    badge = "Ultra 4K",
                    accentColor = PhotonBlue,
                    onClick = { onNavigate(AppScreen.IMAGE_STUDIO) }
                )
                StudioNavCard(
                    title = "مکالمه صوتی دوطرفه زنده (Live Voice Chat)",
                    subtitle = "گفتگوی زنده با هوش مصنوعی با دریافت صدای میکروفون و پخش بلادرنگ پاسخ صوتی",
                    icon = Icons.Default.RecordVoiceOver,
                    badge = "کم‌تاخیر",
                    accentColor = StatusConnected,
                    onClick = { onNavigate(AppScreen.VOICE_STUDIO) }
                )
                StudioNavCard(
                    title = "تبدیل گفتار به متن (Speech-to-Text)",
                    subtitle = "پیاده‌سازی صدای ضبط شده با تشخیص خودکار زبان و ویرایش متن خروجی",
                    icon = Icons.Default.Hearing,
                    badge = "Whisper v3",
                    accentColor = PhotonBlue,
                    onClick = { onNavigate(AppScreen.SPEECH_TO_TEXT) }
                )
                StudioNavCard(
                    title = "تبدیل متن به گفتار (Text-to-Speech)",
                    subtitle = "سنتز متن به صوت طبیعی فارسی با انتخاب گویندگان نیلوفر، آریا، سهراب و تارا",
                    icon = Icons.Default.VolumeUp,
                    badge = "Neural TTS",
                    accentColor = CyberViolet,
                    onClick = { onNavigate(AppScreen.TEXT_TO_SPEECH) }
                )
            }
        }

        // Subscription Tiers Highlight
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بسته‌های اشتراک پلتفرم",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = { onNavigate(AppScreen.PRICING) }) {
                    Text(
                        text = "مشاهده همه",
                        color = ElectricIndigo,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subscriptionTiers) { tier ->
                    GlassCard(
                        modifier = Modifier
                            .width(260.dp)
                            .testTag("tier_card_${tier.id}"),
                        backgroundColor = if (tier.isPopular) SurfaceElevation2 else SurfaceElevation1,
                        borderColor = if (tier.isPopular) ElectricIndigo else TranslucentBorder,
                        onClick = { onNavigate(AppScreen.PRICING) }
                    ) {
                        if (tier.isPopular) {
                            StatusPill(
                                label = "پیشنهاد کاربران (محبوب‌ترین)",
                                statusColor = ElectricIndigo,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = "بسته ${tier.nameFa}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = tier.nameEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontFamily = OutfitFontFamily
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = tier.priceFa,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• ${tier.videoCredits} اعتبار ویدیو",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "• ${tier.textTokensLimit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "• ${tier.imageGenerations} تصویر با کیفیت بالا",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun StudioNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SurfaceElevation1.copy(alpha = 0.85f),
        borderColor = TranslucentBorder,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge,
                            color = accentColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
