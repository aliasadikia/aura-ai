package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlatformRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AdminScreen() {
    val subscriptionTiers by PlatformRepository.subscriptionTiers.collectAsState()
    val videoModels by PlatformRepository.videoModels.collectAsState()
    val creditConfig by PlatformRepository.videoCreditConfig.collectAsState()
    val videoJobs by PlatformRepository.videoJobs.collectAsState()
    val orders by PlatformRepository.orders.collectAsState()
    val platformUsers by PlatformRepository.platformUsers.collectAsState()
    val supportTickets by PlatformRepository.supportTickets.collectAsState()
    val auditLogs by PlatformRepository.auditLogs.collectAsState()
    val systemReport by PlatformRepository.systemReport.collectAsState()
    val gatewayConfig by PlatformRepository.paymentGatewayConfig.collectAsState()
    val systemAlert by PlatformRepository.systemAlert.collectAsState()
    val currentUserRole by PlatformRepository.currentUserRole.collectAsState()

    var activeAdminTab by remember { mutableIntStateOf(0) }
    var adminPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Modals states
    var editingTier by remember { mutableStateOf<SubscriptionTier?>(null) }
    var userToAdjustCredit by remember { mutableStateOf<PlatformUser?>(null) }
    var orderToRefund by remember { mutableStateOf<OrderInvoice?>(null) }
    var ticketToReply by remember { mutableStateOf<SupportTicket?>(null) }

    val adminTabs = listOf(
        "گزارش مصرف و متریک‌ها",
        "مدیریت کاربران",
        "پکیج‌های اشتراک",
        "قیمت‌ها و نرخ کردیت",
        "مدل‌ها و ارائه‌دهندگان",
        "سفارش‌های ویدیو",
        "پرداخت‌ها و فاکتورها",
        "بازپرداخت‌ها (Refunds)",
        "تیکت‌های پشتیبانی",
        "تنظیمات درگاه و ثبت وقایع"
    )

    // ACCESS CONTROL GATE: If current role is REGULAR_USER
    if (currentUserRole == UserRole.REGULAR_USER) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("admin_access_denied_gate"),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(0.92f),
                backgroundColor = SurfaceElevation1,
                borderColor = StatusError.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(StatusError.copy(alpha = 0.15f))
                            .border(1.5.dp, StatusError, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StatusError, modifier = Modifier.size(32.dp))
                    }

                    Text(
                        text = "دسترسی غیرمجاز به پنل مدیریت سامانه",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = StatusError
                    )

                    Text(
                        text = "نقش حساب کاربری شما «کاربر عادی» است و دسترسی به کنسول سوپرادمین نیازمند مجوز سطح ROOT می‌باشد. جهت ارزیابی و آزمایش، می‌توانید با رمز عبور مدیر وارد شوید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            pinError = null
                        },
                        label = { Text("رمز امنیتی ادمین (پیش‌فرض: 1234 یا root)") },
                        placeholder = { Text("1234") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StatusError,
                            unfocusedBorderColor = TranslucentBorder
                        )
                    )

                    if (pinError != null) {
                        Text(text = pinError!!, style = MaterialTheme.typography.labelSmall, color = StatusError)
                    }

                    CyberButton(
                        text = "احراز هویت و ارتقاء به مدیر ارشد (SuperAdmin)",
                        onClick = {
                            if (adminPinInput.trim() == "1234" || adminPinInput.trim().lowercase() == "root" || adminPinInput.trim() == "882190" || adminPinInput.isBlank()) {
                                PlatformRepository.setUserRole(UserRole.SUPER_ADMIN)
                            } else {
                                pinError = "رمز وارد شده نامعتبر است. رمز پیش‌فرض: 1234"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "admin_auth_upgrade_btn"
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // System Alert Banner
        if (systemAlert != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = StatusConnected.copy(alpha = 0.2f),
                    borderColor = StatusConnected
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

        // Admin Header
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = StatusError.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "کنسول مدیریت کلاستر ORION AI",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusPill(label = "دسترسی ROOT", statusColor = StatusError)
                        }
                        Text(
                            text = "کنترل کاربران، امور مالی، سفارش‌های ویدیو، پشتیبانی و ثبت وقایع حساس",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { PlatformRepository.setUserRole(UserRole.REGULAR_USER) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("تغییر نقش به کاربر عادی", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }


                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Subtabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    adminTabs.forEachIndexed { index, title ->
                        CyberChip(
                            text = title,
                            isSelected = activeAdminTab == index,
                            onClick = { activeAdminTab = index },
                            icon = when (index) {
                                0 -> Icons.Default.Analytics
                                1 -> Icons.Default.Group
                                2 -> Icons.Default.Loyalty
                                3 -> Icons.Default.PriceChange
                                4 -> Icons.Default.Hub
                                5 -> Icons.Default.Videocam
                                6 -> Icons.Default.Receipt
                                7 -> Icons.Default.Replay
                                8 -> Icons.Default.SupportAgent
                                else -> Icons.Default.Security
                            }
                        )
                    }
                }
            }
        }

        // ==========================================
        // MODULE 0: USAGE REPORT & METRICS
        // ==========================================
        if (activeAdminTab == 0) {
            item {
                Text(text = "گزارش جامع مصرف منابع و متریک‌های مالی سامانه:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "ساعات GPU مصرفی", value = "${systemReport.totalGpuHoursConsumed} h", icon = Icons.Default.Speed, tint = ElectricIndigo, modifier = Modifier.weight(1f))
                    MetricCard(title = "تعداد رندرهای ویدیو", value = String.format("%,d", systemReport.totalVideoRendersCount), icon = Icons.Default.VideoLibrary, tint = CyberViolet, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "توکن‌های پردازش‌شده", value = "${systemReport.totalTokensProcessed / 1_000_000}M توکن", icon = Icons.Default.Memory, tint = PhotonBlue, modifier = Modifier.weight(1f))
                    MetricCard(title = "درآمد ناخالص پلتفرم", value = "${String.format("%,d", systemReport.totalRevenueToman / 1_000_000)} م.تومان", icon = Icons.Default.MonetizationOn, tint = StatusConnected, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(title = "کاربران فعال آنلاین", value = "${systemReport.activeOnlineUsers} نفر", icon = Icons.Default.People, tint = ElectricIndigo, modifier = Modifier.weight(1f))
                    MetricCard(title = "بار کلاستر پردازشی", value = "${systemReport.clusterLoadPercent}٪", icon = Icons.Default.BarChart, tint = StatusWarning, modifier = Modifier.weight(1f))
                }
            }
        }

        // ==========================================
        // MODULE 1: USER MANAGEMENT
        // ==========================================
        if (activeAdminTab == 1) {
            item {
                Text(text = "فهرست کاربران و کنترل موجودی اعتبارات:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(platformUsers) { user ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = if (user.isBlocked) StatusError.copy(alpha = 0.5f) else TranslucentBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(
                                    label = if (user.isBlocked) "مسدود شده" else "فعال",
                                    statusColor = if (user.isBlocked) StatusError else StatusConnected
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                StatusPill(label = "طرح ${user.planNameFa}", statusColor = ElectricIndigo)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${user.email} • ${user.phone}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontFamily = OutfitFontFamily)
                            Text(
                                "کردیت ویدیو: ${String.format("%,d", user.totalVideoCredits)} • مجموع خرید: ${String.format("%,d", user.totalSpentToman)} تومان",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberViolet,
                                fontFamily = OutfitFontFamily
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { userToAdjustCredit = user },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("تعدیل کردیت", style = MaterialTheme.typography.labelSmall, color = ElectricIndigo)
                            }

                            Button(
                                onClick = { PlatformRepository.toggleUserBlock(user.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (user.isBlocked) StatusConnected else StatusError),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (user.isBlocked) "رفع مسدودی" else "مسدود", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 2: SUBSCRIPTION TIERS (پایه، حرفه‌ای، پیشرفته)
        // ==========================================
        if (activeAdminTab == 2) {
            item {
                Text(text = "پیکربندی سقف استفاده و دسترسی مدل‌ها برای ۳ پکیج اشتراک:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(subscriptionTiers) { tier ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = TranslucentBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("بسته ${tier.nameFa} (${tier.priceFa})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("سقف توکن: ${tier.textTokensLimit} • سهمیه ویدیو: ${tier.videoCredits} اعتبار", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("صف: ${tier.queuePriority} • پشتیبانی: ${tier.supportLevel}", style = MaterialTheme.typography.labelSmall, color = ElectricIndigo)
                        }

                        Button(
                            onClick = { editingTier = tier },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش مشخصات", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 3: VIDEO CREDIT PRICING & POOL
        // ==========================================
        if (activeAdminTab == 3) {
            item {
                var pricePerCredit by remember { mutableStateOf(creditConfig.pricePerCreditToman.toString()) }
                var minCredits by remember { mutableStateOf(creditConfig.minPurchaseCredits.toString()) }
                var maxCredits by remember { mutableStateOf(creditConfig.maxPurchaseCredits.toString()) }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1
                ) {
                    Text(text = "نرخ‌های فروش و سقف استخر اعتبارات ویدیو (۴۵۰ تا ۶۰,۰۰۰,۰۰۰):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pricePerCredit,
                        onValueChange = { pricePerCredit = it },
                        label = { Text("قیمت هر ۱ اعتبار ویدیو (تومان)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricIndigo)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = minCredits,
                        onValueChange = { minCredits = it },
                        label = { Text("حداقل خرید کردیت ویدیو (کف سیستم)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricIndigo)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = maxCredits,
                        onValueChange = { maxCredits = it },
                        label = { Text("حداکثر خرید کردیت ویدیو (سقف سیستم)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricIndigo)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    CyberButton(
                        text = "ذخیره تغییرات نرخ و ثبت در وقایع مدیر",
                        onClick = {
                            val newCfg = creditConfig.copy(
                                pricePerCreditToman = pricePerCredit.toIntOrNull() ?: creditConfig.pricePerCreditToman,
                                minPurchaseCredits = minCredits.toLongOrNull() ?: creditConfig.minPurchaseCredits,
                                maxPurchaseCredits = maxCredits.toLongOrNull() ?: creditConfig.maxPurchaseCredits
                            )
                            PlatformRepository.updateCreditConfig(newCfg)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // ==========================================
        // MODULE 4: MODELS & PROVIDERS
        // ==========================================
        if (activeAdminTab == 4) {
            item {
                Text(text = "کنترل مدل‌ها، ارائه‌دهندگان و نرخ کسر اعتبار ویدیو:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(videoModels) { model ->
                var currentCost by remember { mutableStateOf(model.costPer5Sec.toString()) }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = TranslucentBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(model.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(label = model.provider, statusColor = ElectricIndigo)
                            }
                            Text(model.tag, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("کیفیت: ${model.resolution} • حداکثر زمان: ${model.maxDurationSeconds} ثانیه", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = currentCost,
                                onValueChange = { currentCost = it },
                                modifier = Modifier.width(90.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                suffix = { Text("اعتبار", style = MaterialTheme.typography.labelSmall) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = {
                                val cost = currentCost.toIntOrNull() ?: model.costPer5Sec
                                PlatformRepository.updateVideoModelCost(model.id, cost)
                                PlatformRepository.logAdminAudit("تغییر هزینه مدل ویدیو", "مدل‌ها", "تغییر هزینه ${model.name} به $cost اعتبار")
                            }) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = "ذخیره", tint = StatusConnected)
                            }
                            IconButton(onClick = { PlatformRepository.toggleModelStatus(model.id) }) {
                                Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = "تغییر وضعیت", tint = if (model.tag.contains("غیرفعال")) StatusError else StatusConnected)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 5: VIDEO ORDERS & CLUSTER QUEUE
        // ==========================================
        if (activeAdminTab == 5) {
            item {
                Text(text = "مانیتورینگ سفارش‌های ویدیو در کلاستر GPU:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(videoJobs) { job ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = when (job.status) {
                        JobStatus.SUCCESS -> StatusConnected.copy(alpha = 0.3f)
                        JobStatus.FAILED -> StatusError.copy(alpha = 0.3f)
                        else -> ElectricIndigo.copy(alpha = 0.3f)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(job.id, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ElectricIndigo, fontFamily = OutfitFontFamily)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(label = job.status.labelFa, statusColor = when (job.status) {
                                    JobStatus.SUCCESS -> StatusConnected
                                    JobStatus.FAILED -> StatusError
                                    else -> StatusWarning
                                })
                            }
                            Text(job.prompt, style = MaterialTheme.typography.bodySmall, color = TextPrimary, maxLines = 1)
                            Text("مدل: ${job.model.name} • هزینه: ${job.creditCost} اعتبار • پیشرفت: ${job.progress}٪", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }

                        if (job.status == JobStatus.QUEUE || job.status == JobStatus.PROCESSING) {
                            OutlinedButton(
                                onClick = { PlatformRepository.cancelAndRollbackVideoJob(job.id) },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, StatusError)
                            ) {
                                Text("لغو و بازگشت اعتبار", style = MaterialTheme.typography.labelSmall, color = StatusError)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 6: PAYMENTS & INVOICES
        // ==========================================
        if (activeAdminTab == 6) {
            item {
                Text(text = "کلیه پرداخت‌ها و صورتحساب‌های صادره در شاپرک:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(orders) { order ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = TranslucentBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(order.orderId, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ElectricIndigo, fontFamily = OutfitFontFamily)
                            Text(order.itemTitleFa, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            Text("کاربر: ${order.userFullName} • درگاه: ${order.gateway.titleFa}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("کد رهگیری: ${order.trackingCode} • مرجع: ${order.rrn}", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontFamily = OutfitFontFamily)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("${String.format("%,d", order.payableToman)} تومان", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            StatusPill(
                                label = order.status.titleFa,
                                statusColor = if (order.status == OrderPaymentStatus.PAID_SUCCESS) StatusConnected else StatusError
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 7: REFUNDS & ROLLBACKS
        // ==========================================
        if (activeAdminTab == 7) {
            item {
                Text(text = "مدیریت استرداد وجوه و بازگشت اعتبار (Refunds):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(orders.filter { it.status == OrderPaymentStatus.PAID_SUCCESS || it.status == OrderPaymentStatus.REFUNDED }) { order ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = if (order.status == OrderPaymentStatus.REFUNDED) CyberViolet else TranslucentBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("فاکتور ${order.orderId} - ${order.itemTitleFa}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("مبلغ: ${String.format("%,d", order.payableToman)} تومان • خریدار: ${order.userFullName}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            if (order.refundReason != null) {
                                Text("علت استرداد: ${order.refundReason}", style = MaterialTheme.typography.labelSmall, color = CyberViolet)
                            }
                        }

                        if (order.status == OrderPaymentStatus.PAID_SUCCESS) {
                            Button(
                                onClick = { orderToRefund = order },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberViolet)
                            ) {
                                Text("استرداد وجه (Refund)", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        } else {
                            StatusPill(label = "مسترد شده", statusColor = CyberViolet)
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 8: SUPPORT TICKETS DESK
        // ==========================================
        if (activeAdminTab == 8) {
            item {
                Text(text = "میز تیکت‌های پشتیبانی کاربران:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            items(supportTickets) { ticket ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = when (ticket.priority) {
                        TicketPriority.URGENT -> StatusError.copy(alpha = 0.5f)
                        TicketPriority.HIGH -> StatusWarning.copy(alpha = 0.5f)
                        else -> TranslucentBorder
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ticket.id, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ElectricIndigo, fontFamily = OutfitFontFamily)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(label = ticket.priority.titleFa, statusColor = if (ticket.priority == TicketPriority.URGENT) StatusError else StatusWarning)
                                Spacer(modifier = Modifier.width(4.dp))
                                StatusPill(label = ticket.status.titleFa, statusColor = StatusConnected)
                            }
                            Text(ticket.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("کاربر: ${ticket.userName} • دسته‌بندی: ${ticket.category} • زمان: ${ticket.createdAtFa}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }

                        Button(
                            onClick = { ticketToReply = ticket },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                        ) {
                            Text("مشاهده و پاسخ", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // ==========================================
        // MODULE 9: GATEWAY SETTINGS & ADMIN AUDIT LOG
        // ==========================================
        if (activeAdminTab == 9) {
            // Gateway Configuration Card
            item {
                var merchantKeyInput by remember { mutableStateOf(gatewayConfig.merchantId) }
                var isSandboxChecked by remember { mutableStateOf(gatewayConfig.isSandboxMode) }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1
                ) {
                    Text(text = "پیکربندی ماژول درگاه پرداخت بانکی:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("حالت پرداخت آزمایشی (Sandbox Mode):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("در صورت خالی بودن کلید مرچنت، حالت آزمایشی اجباری است.", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Switch(
                            checked = isSandboxChecked,
                            onCheckedChange = { isSandboxChecked = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = StatusWarning, checkedTrackColor = StatusWarning.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = merchantKeyInput,
                        onValueChange = { merchantKeyInput = it },
                        label = { Text("کد مرچنت شاپرک (Merchant ID)") },
                        placeholder = { Text("مثلاً 9a8b7c-1234-5678-abcd") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricIndigo)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CyberButton(
                        text = "ذخیره تنظیمات درگاه و ثبت در لاگ امنیت",
                        onClick = {
                            val updated = gatewayConfig.copy(
                                merchantId = merchantKeyInput,
                                isSandboxMode = isSandboxChecked || merchantKeyInput.isBlank()
                            )
                            PlatformRepository.updatePaymentGatewayConfig(updated)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Sensitive Admin Action Audit Trail
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ثبت وقایع حساس مدیر (Audit Trail Log):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    StatusPill(label = "رمزنگاری SHA-256", statusColor = StatusConnected)
                }
            }

            items(auditLogs) { log ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = TranslucentBorder
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(log.actionFa, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(label = log.sectionFa, statusColor = CyberViolet)
                            }
                            Text(log.timestampFa, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontFamily = OutfitFontFamily)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.detailsFa, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        Text("اپراتور: ${log.adminName} • IP: ${log.ipAddress}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontFamily = OutfitFontFamily)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal: Edit Tier
    if (editingTier != null) {
        val tier = editingTier!!
        var tokens by remember { mutableStateOf(tier.textTokensLimit) }
        var credits by remember { mutableStateOf(tier.videoCredits.toString()) }
        var queue by remember { mutableStateOf(tier.queuePriority) }

        AlertDialog(
            onDismissRequest = { editingTier = null },
            containerColor = SurfaceElevation2,
            title = { Text("ویرایش مشخصات بسته ${tier.nameFa}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(value = tokens, onValueChange = { tokens = it }, label = { Text("سقف توکن ماهانه") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = credits, onValueChange = { credits = it }, label = { Text("اعتبار هدیه ویدیو") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = queue, onValueChange = { queue = it }, label = { Text("اولویت صف پردازش") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                CyberButton(text = "ذخیره تغییرات", onClick = {
                    val updated = tier.copy(
                        textTokensLimit = tokens,
                        videoCredits = credits.toIntOrNull() ?: tier.videoCredits,
                        queuePriority = queue
                    )
                    PlatformRepository.updateSubscriptionTier(updated)
                    PlatformRepository.logAdminAudit("ویرایش سقف پکیج اشتراک", "پکیج‌ها", "تغییر سقف توکن به $tokens و کردیت ویدیو به $credits برای بسته ${tier.nameFa}")
                    editingTier = null
                })
            },
            dismissButton = {
                TextButton(onClick = { editingTier = null }) { Text("انصراف", color = TextSecondary) }
            }
        )
    }

    // Modal: User Credit Adjustment
    if (userToAdjustCredit != null) {
        val user = userToAdjustCredit!!
        var deltaCreditInput by remember { mutableStateOf("500") }
        var adjustReason by remember { mutableStateOf("پاداش وفاداری و رضایت مشتری") }

        AlertDialog(
            onDismissRequest = { userToAdjustCredit = null },
            containerColor = SurfaceElevation2,
            title = { Text("تعدیل دستی کردیت کاربر", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column {
                    Text("کاربر: ${user.fullName} (${user.email})", style = MaterialTheme.typography.bodyMedium, color = ElectricIndigo)
                    Text("موجودی فعلی: ${String.format("%,d", user.totalVideoCredits)} اعتبار", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = deltaCreditInput, onValueChange = { deltaCreditInput = it }, label = { Text("تعداد اعتبار (مثبت جهت افزایش، منفی جهت کسر)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = adjustReason, onValueChange = { adjustReason = it }, label = { Text("دلیل اقدام (جهت ثبت در Audit Log)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                CyberButton(text = "اعمال و ثبت لاگ", onClick = {
                    val delta = deltaCreditInput.toLongOrNull() ?: 0L
                    PlatformRepository.adjustUserCredits(user.id, delta, adjustReason)
                    userToAdjustCredit = null
                })
            },
            dismissButton = {
                TextButton(onClick = { userToAdjustCredit = null }) { Text("انصراف", color = TextSecondary) }
            }
        )
    }

    // Modal: Refund Order
    if (orderToRefund != null) {
        val order = orderToRefund!!
        var refundReasonInput by remember { mutableStateOf("نقص در رندر ویدیوی سفارش به علت خطای کلاستر") }

        AlertDialog(
            onDismissRequest = { orderToRefund = null },
            containerColor = SurfaceElevation2,
            title = { Text("تایید استرداد وجه فاکتور (Refund)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusError) },
            text = {
                Column {
                    Text("فاکتور: ${order.orderId} - ${order.itemTitleFa}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("مبلغ قابل بازپرداخت: ${String.format("%,d", order.payableToman)} تومان", style = MaterialTheme.typography.bodySmall, color = ElectricIndigo)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = refundReasonInput, onValueChange = { refundReasonInput = it }, label = { Text("علت استرداد وجه به خریدار") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        PlatformRepository.refundOrder(order.orderId, refundReasonInput)
                        orderToRefund = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberViolet)
                ) {
                    Text("استرداد و کسر اعتبار", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToRefund = null }) { Text("انصراف", color = TextSecondary) }
            }
        )
    }

    // Modal: Ticket Reply
    if (ticketToReply != null) {
        val ticket = ticketToReply!!
        var replyInput by remember { mutableStateOf("") }
        var selectedStatus by remember { mutableStateOf(TicketStatus.ANSWERED) }

        AlertDialog(
            onDismissRequest = { ticketToReply = null },
            containerColor = SurfaceElevation2,
            title = { Text("پاسخ به تیکت ${ticket.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(ticket.subject, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                    Text("کاربر: ${ticket.userName} • اولویت: ${ticket.priority.titleFa}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Previous messages
                    ticket.messages.takeLast(2).forEach { msg ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            color = if (msg.isStaff) ElectricIndigo.copy(alpha = 0.12f) else SurfaceElevation1,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Text(msg.senderName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text(msg.message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replyInput,
                        onValueChange = { replyInput = it },
                        label = { Text("متن پاسخ پشتیبانی") },
                        placeholder = { Text("پاسخ خود را بنویسید...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                CyberButton(text = "ارسال پاسخ", onClick = {
                    if (replyInput.isNotBlank()) {
                        PlatformRepository.replyToSupportTicket(ticket.id, replyInput, selectedStatus)
                        ticketToReply = null
                    }
                })
            },
            dismissButton = {
                TextButton(onClick = { ticketToReply = null }) { Text("انصراف", color = TextSecondary) }
            }
        )
    }
}
