package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.PlatformRepository
import com.example.model.AppScreen
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SettingsScreen() {
    var darkModeEnabled by remember { mutableStateOf(true) }
    var streamResponseEnabled by remember { mutableStateOf(true) }
    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var persianNumeralsEnabled by remember { mutableStateOf(true) }
    var defaultModel by remember { mutableStateOf("GPT-4o") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = ActiveBorder
            ) {
                Text(
                    text = "تنظیمات سامانه هوش مصنوعی (Settings)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "سفارشی‌سازی رابط کاربری، تم سایبرنتیک، مدل پیش‌فرض و رفتار استریم پاسخ‌ها",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "ترجیحات بصری و زبان:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingToggleRow(
                    title = "محیط تاریک و سایبرنتیک (Obsidian OLED Dark)",
                    subtitle = "کاهش مصرف باتری و حذف خستگی چشم در کار طولانی",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )

                HorizontalDivider(color = TranslucentBorder, modifier = Modifier.padding(vertical = 10.dp))

                SettingToggleRow(
                    title = "تایپ زنده و استریم پاسخ‌ها (Token Streaming)",
                    subtitle = "نمایش بی‌درنگ کلمات هوش مصنوعی همراه با کرسر نئونی",
                    checked = streamResponseEnabled,
                    onCheckedChange = { streamResponseEnabled = it }
                )

                HorizontalDivider(color = TranslucentBorder, modifier = Modifier.padding(vertical = 10.dp))

                SettingToggleRow(
                    title = "اعداد فارسی بومی (Persian Numerals)",
                    subtitle = "نمایش مقادیر و ارقام به زبان فارسی با فونت وزیرمتن",
                    checked = persianNumeralsEnabled,
                    onCheckedChange = { persianNumeralsEnabled = it }
                )
            }
        }

        // Backup and Data Protection Card
        item {
            var backupJsonState by remember { mutableStateOf<String?>(null) }
            var restoreStatus by remember { mutableStateOf<String?>(null) }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "پشتیبان‌گیری و محافظت از داده‌ها (Backup & Security):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "استخراج کلیه سوابق، فاکتورها و تنظیمات کاربر با امضای دیجیتال و امکان بازیابی آنی",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberButton(
                        text = "تهیه نسخه پشتیبان (JSON)",
                        onClick = {
                            val json = PlatformRepository.generateBackupJsonString()
                            backupJsonState = json
                            restoreStatus = "نسخه پشتیبان با ${json.length} بایت آماده شد."
                        },
                        icon = Icons.Default.Backup,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            val mockBackup = PlatformRepository.generateBackupJsonString()
                            val ok = PlatformRepository.restoreBackupFromJson(mockBackup)
                            restoreStatus = if (ok) "اعتبارسنجی و بازیابی فایل پشتیبان تایید شد." else "خطا در بازیابی"
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("آزمون بازیابی", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (restoreStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = restoreStatus!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusConnected
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}


@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricIndigo
            )
        )
    }
}

@Composable
fun HelpScreen() {
    val faqs = listOf(
        "نحوه کسر اعتبار در استودیو ویدیو چگونه است؟" to "اعتبارات ویدیو بر اساس مدل انتخابی و مدت زمان ویدیو محاسبه می‌شوند. برای نمونه، مدل Runway Gen-3 به ازای هر ۵ ثانیه ۸۰ اعتبار و مدل Sora Ultra به ازای هر ۵ ثانیه ۱۲۰ اعتبار کسر می‌کند. چنانچه پردازش با خطا مواجه شود، اعتبار بلافاصله به حسابتان بازگردانده می‌شود (Rollback).",
        "تفاوت توکن متنی با اعتبار ویدیو چیست؟" to "توکن‌های متنی برای مدل‌های زبانی مانند GPT-4o و Claude 3.5 Sonnet استفاده می‌شوند، در حالی که اعتبارات ویدیو در استخر جداگانه‌ای جهت تامین منابع سنگین کارت‌های گرافیک کلاستر ویدیو تخصیص یافته است.",
        "آیا بسته‌های اشتراک به صورت خودکار تمدید می‌شوند؟" to "خیر، جهت حفظ شفافیت مالی و رضایت کاربران، تمدید هر دوره فقط با تایید مجدد شما از طریق درگاه پرداخت صورت می‌پذیرد.",
        "چگونه می‌توان از کلید API در پایتون استفاده کرد؟" to "با مراجعه به بخش پروفایل، کلید اختصاصی sk-aura را کپی نموده و در کتابخانه استاندارد OpenAI یا Aura-SDK به عنوان Authorization Bearer قرار دهید."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("help_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = ActiveBorder
            ) {
                Text(
                    text = "مرکز راهنما و سوالات متداول (Help & FAQ)",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "پاسخ به متداول‌ترین پرسش‌های فنی، مالی و راهنمای استودیوها",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        items(faqs) { (q, a) ->
            var expanded by remember { mutableStateOf(false) }
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                backgroundColor = SurfaceElevation1,
                borderColor = if (expanded) ElectricIndigo else TranslucentBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = q,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ElectricIndigo
                    )
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = a,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    var authMode by remember { mutableIntStateOf(0) } // 0: Login, 1: Signup, 2: Forgot Password
    var emailOrPhone by remember { mutableStateOf("ali.radmanesh@aura.ai") }
    var password by remember { mutableStateOf("••••••••••••") }
    var name by remember { mutableStateOf("علی رادمنش") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidCanvas)
            .padding(16.dp)
            .testTag("auth_screen_content"),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SurfaceElevation1,
            borderColor = ActiveBorder
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (authMode) {
                        0 -> "ورود به حساب کاربری پلتفرم آئورا"
                        1 -> "ثبت نام حساب جدید"
                        else -> "بازیابی رمز عبور"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Auth Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberChip(
                        text = "ورود",
                        isSelected = authMode == 0,
                        onClick = { authMode = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    CyberChip(
                        text = "ثبت‌نام",
                        isSelected = authMode == 1,
                        onClick = { authMode = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    CyberChip(
                        text = "فراموشی رمز",
                        isSelected = authMode == 2,
                        onClick = { authMode = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (authMode == 1) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("نام و نام خانوادگی", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = TranslucentBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("ایمیل یا شماره موبایل", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = TranslucentBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                if (authMode != 2) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = TranslucentBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                CyberButton(
                    text = when (authMode) {
                        0 -> "ورود امن به سامانه"
                        1 -> "ایجاد حساب کاربری"
                        else -> "ارسال لینک بازیابی"
                    },
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auth_submit_button"
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onNavigate: (AppScreen) -> Unit
) {
    val videoCredits by PlatformRepository.videoCredits.collectAsState()
    val textTokens by PlatformRepository.textTokensRemaining.collectAsState()
    val userPlan by PlatformRepository.userPlan.collectAsState()
    val videoJobs by PlatformRepository.videoJobs.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.9f),
                borderColor = ActiveBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "داشبورد محاسباتی ORION AI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "نمای تحلیلی مصرف منابع، کلاسترها و فعالیت‌های اخیر شما",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    StatusPill(label = "طرح: ${userPlan.nameFa}", statusColor = ElectricIndigo)
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceElevation2
                ) {
                    Text("اعتبار ویدیو فعال", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("$videoCredits", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = CyberViolet, fontFamily = OutfitFontFamily)
                    Text("در استخر اختصاصی", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SurfaceElevation2
                ) {
                    Text("توکن متنی باقیمانده", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("${textTokens / 1000}K", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ElectricIndigo, fontFamily = OutfitFontFamily)
                    Text("آماده پردازش", style = MaterialTheme.typography.labelSmall, color = StatusConnected)
                }
            }
        }

        // Quick actions
        item {
            Text(
                text = "دستورات و استودیوهای سریع:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CyberButton(
                    text = "رندر ویدیو جدید",
                    onClick = { onNavigate(AppScreen.VIDEO_STUDIO) },
                    icon = Icons.Default.Videocam,
                    modifier = Modifier.weight(1f)
                )
                CyberButton(
                    text = "گفتگو با GPT-4o",
                    onClick = { onNavigate(AppScreen.CHAT) },
                    icon = Icons.Default.ChatBubble,
                    isSecondary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Jobs Status
        item {
            Text(
                text = "آخرین جاب‌های در جریان:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(videoJobs.take(3)) { job ->
            VideoJobTrackingCard(job = job)
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
