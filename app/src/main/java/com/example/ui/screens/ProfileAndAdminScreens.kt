package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlatformRepository
import com.example.model.AppScreen
import com.example.model.SubscriptionTier
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateToPricing: () -> Unit
) {
    val userPlan by PlatformRepository.userPlan.collectAsState()
    val totalCredits by PlatformRepository.videoCredits.collectAsState()
    val reservedCredits by PlatformRepository.reservedCredits.collectAsState()
    val textTokens by PlatformRepository.textTokensRemaining.collectAsState()
    val apiKey by PlatformRepository.userApiKey.collectAsState()
    val creditLedger by PlatformRepository.creditLedger.collectAsState()

    val availableCredits = (totalCredits - reservedCredits).coerceAtLeast(0)
    val currentUserRole by PlatformRepository.currentUserRole.collectAsState()
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current

    var isKeyVisible by remember { mutableStateOf(false) }
    var keyCopiedMessage by remember { mutableStateOf<String?>(null) }
    var showRegenerateKeyDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJson by remember { mutableStateOf("") }
    var backupCopiedMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Info Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = ActiveBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevation3)
                            .border(2.dp, CyanTurquoise, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "آواتار کاربر ORION",
                            tint = CyanTurquoise,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "علی رادمنش",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusPill(
                                label = currentUserRole.titleFa,
                                statusColor = if (currentUserRole == com.example.model.UserRole.SUPER_ADMIN) StatusError else CyanTurquoise
                            )
                        }
                        Text(
                            text = "ali.radmanesh@orion.ai • ۰۹۱۲۳۴۵۶۷۸۹",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontFamily = OutfitFontFamily
                        )
                        Text(
                            text = "شناسه کاربر: USR-88219-IR • سطح دسترسی: ${currentUserRole.titleFa}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontFamily = OutfitFontFamily
                        )
                    }
                }
            }
        }

        // Active Subscription Plan
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
                    Column {
                        Text(text = "اشتراک فعال شما:", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(
                            text = "بسته ${userPlan.nameFa} (${userPlan.priceFa})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo
                        )
                        Text(
                            text = "سقف ماهانه: ${userPlan.textTokensLimit} • صف: ${userPlan.queuePriority}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }

                    CyberButton(
                        text = "ارتقاء / تمدید",
                        onClick = onNavigateToPricing,
                        modifier = Modifier.height(40.dp)
                    )
                }
            }
        }

        // Video Credit Pool & Balances (Total, Reserved, Available)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1,
                borderColor = CyberViolet.copy(alpha = 0.4f)
            ) {
                Text(
                    text = "استخر اعتبارات ویدیو (تفکیک موجودی و رزروها):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Pool
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = SurfaceElevation2,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, TranslucentBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "کل موجودی استخر", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%,d", totalCredits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = OutfitFontFamily)
                        }
                    }

                    // Reserved Balance
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = SurfaceElevation2,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (reservedCredits > 0) StatusWarning.copy(alpha = 0.5f) else TranslucentBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "کردیت رزرو شده (در صف)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%,d", reservedCredits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (reservedCredits > 0) StatusWarning else TextSecondary, fontFamily = OutfitFontFamily)
                        }
                    }

                    // Available Balance
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = SurfaceElevation2,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "کردیت آزاد و قابل مصرف", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%,d", availableCredits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusConnected, fontFamily = OutfitFontFamily)
                        }
                    }
                }
            }
        }

        // Text & Vision Usage Bar
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(text = "سهمیه توکن متنی و پردازش بینایی:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "باقیمانده دوره جاری:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = "${String.format("%,d", textTokens)} توکن", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = PhotonBlue, fontFamily = OutfitFontFamily)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PhotonBlue,
                    trackColor = SurfaceElevation2
                )
            }
        }

        // API Key Management Card with Masking & Rotation
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
                    Column {
                        Text(
                            text = "کلید اختصاصی API توسعه‌دهندگان (محافظت‌شده):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "برای امنیت بیشتر در محیط سرور از Secrets Panel استفاده کنید.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    StatusPill(label = "رمزنگاری فعال")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = VoidCanvas,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TranslucentBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isKeyVisible) apiKey else PlatformRepository.getMaskedApiKey(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            fontFamily = OutfitFontFamily
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "نمایش کلید",
                                    tint = TextSecondary
                                )
                            }
                            IconButton(onClick = {
                                clipboard.setText(androidx.compose.ui.text.AnnotatedString(apiKey))
                                keyCopiedMessage = "کلید API کپی شد"
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "کپی کلید",
                                    tint = ElectricIndigo
                                )
                            }
                        }
                    }
                }

                if (keyCopiedMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = keyCopiedMessage!!, style = MaterialTheme.typography.labelSmall, color = StatusConnected)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showRegenerateKeyDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("صدور مجدد کلید (Regenerate)", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            backupJson = PlatformRepository.generateBackupJsonString()
                            showBackupDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevation2),
                        border = BorderStroke(1.dp, TranslucentBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricIndigo)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پشتیبان‌گیری (JSON Export)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal: Regenerate API Key
    if (showRegenerateKeyDialog) {
        AlertDialog(
            onDismissRequest = { showRegenerateKeyDialog = false },
            containerColor = SurfaceElevation1,
            title = { Text("صدور مجدد کلید API", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusWarning) },
            text = {
                Text(
                    text = "با صدور کلید جدید، کلید قبلی بلافاصله باطل شده و کلیه درخواست‌های ارسال‌شده با آن مسدود خواهند شد. آیا از ادامه مطمئن هستید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            },
            confirmButton = {
                CyberButton(
                    text = "تایید و تولید کلید جدید",
                    onClick = {
                        PlatformRepository.regenerateApiKey()
                        showRegenerateKeyDialog = false
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRegenerateKeyDialog = false }) { Text("انصراف", color = TextSecondary) }
            }
        )
    }

    // Modal: JSON Backup Export
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            containerColor = SurfaceElevation1,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نسخه پشتیبان حساب (JSON Export)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    StatusPill(label = "رمزنگاری SHA-256", statusColor = StatusConnected)
                }
            },
            text = {
                Column {
                    Text(
                        text = "این بسته شامل سوابق، اعتبارات، فاکتورهای رسمی شاپرک و تنظیمات کاربری شماست:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        color = VoidCanvas,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TranslucentBorder)
                    ) {
                        Text(
                            text = backupJson,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = OutfitFontFamily,
                            color = PhotonBlue,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    if (backupCopiedMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = backupCopiedMessage!!, style = MaterialTheme.typography.labelSmall, color = StatusConnected)
                    }
                }
            },
            confirmButton = {
                CyberButton(
                    text = "کپی فایل پشتیبان به کلیپ‌بورد",
                    onClick = {
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(backupJson))
                        backupCopiedMessage = "محتوای JSON با موفقیت کپی شد!"
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) { Text("بستن", color = TextSecondary) }
            }
        )
    }
}

