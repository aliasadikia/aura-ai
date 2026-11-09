package com.example.ui.screens

import androidx.compose.animation.*
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
import com.example.model.CreditLedgerEntry
import com.example.model.CreditLedgerType
import com.example.model.OrderInvoice
import com.example.model.SubscriptionTier
import com.example.model.VideoCreditPackage
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun PricingScreen(
    onNavigateToTransactions: () -> Unit
) {
    val subscriptionTiers by PlatformRepository.subscriptionTiers.collectAsState()
    val currentPlan by PlatformRepository.userPlan.collectAsState()
    val totalCredits by PlatformRepository.videoCredits.collectAsState()
    val reservedCredits by PlatformRepository.reservedCredits.collectAsState()
    val creditConfig by PlatformRepository.videoCreditConfig.collectAsState()
    val creditLedger by PlatformRepository.creditLedger.collectAsState()
    val systemAlert by PlatformRepository.systemAlert.collectAsState()

    val availableCredits = (totalCredits - reservedCredits).coerceAtLeast(0)

    var activeTab by remember { mutableIntStateOf(0) } // 0: بسته‌های اشتراک و جدول مقایسه, 1: استخر اعتبارات ویدیو (۴۵۰ تا ۶۰M), 2: گردش حساب و کردیت‌های رزروشده
    var purchaseTierDialog by remember { mutableStateOf<SubscriptionTier?>(null) }
    var activeCheckoutOrder by remember { mutableStateOf<OrderInvoice?>(null) }
    var customCreditInput by remember { mutableStateOf("2500") }
    var customCreditSlider by remember { mutableFloatStateOf(2500f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("pricing_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // System Notification Banner
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

        // Header Card with Balances (Total, Reserved, Available)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1.copy(alpha = 0.95f),
                borderColor = ActiveBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تعرفه‌ها و بسته‌های اشتراک ORION AI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "مدیریت تعرفه‌ها، خرید کردیت ویدیو (۴۵۰ تا ۶۰M) و تفکیک کردیت‌های رزرو شده",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    StatusPill(
                        label = "طرح جاری: ${currentPlan.nameFa}",
                        statusColor = ElectricIndigo
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = TranslucentBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Video Credit Pool Summary Cards (Total, Reserved, Available)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Balance
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = SurfaceElevation2,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, TranslucentBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "موجودی کل کردیت", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%,d", totalCredits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = OutfitFontFamily)
                        }
                    }

                    // Reserved Balance
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = SurfaceElevation2,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (reservedCredits > 0) StatusWarning else TranslucentBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "کردیت رزرو شده (صف)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%,d", reservedCredits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = StatusWarning, fontFamily = OutfitFontFamily)
                        }
                    }

                    // Available / Spendable Balance
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = SurfaceElevation2,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberViolet.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "کردیت آزاد و قابل مصرف", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = String.format("%,d", availableCredits), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyberViolet, fontFamily = OutfitFontFamily)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CyberChip(
                        text = "۱. پلن‌های اشتراک و جدول مقایسه",
                        isSelected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = Icons.Default.Loyalty,
                        modifier = Modifier.weight(1f)
                    )
                    CyberChip(
                        text = "۲. خرید کردیت ویدیو (۴۵۰ تا ۶۰M)",
                        isSelected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = Icons.Default.Videocam,
                        modifier = Modifier.weight(1f)
                    )
                    CyberChip(
                        text = "۳. گردش حساب و لاگ کردیت",
                        isSelected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = Icons.Default.ReceiptLong,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ==========================================
        // TAB 0: 3 SUBSCRIPTION TIERS & COMPARISON TABLE
        // ==========================================
        if (activeTab == 0) {
            item {
                Text(
                    text = "دقیقاً سه پکیج اشتراک پلتفرم هوش مصنوعی (قیمت‌ها به تومان):",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Tiers Cards
            items(subscriptionTiers) { tier ->
                val isCurrent = currentPlan.id == tier.id
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tier_card_${tier.id}"),
                    backgroundColor = if (tier.isPopular) SurfaceElevation2 else SurfaceElevation1,
                    borderColor = if (tier.isPopular) ElectricIndigo else TranslucentBorder,
                    borderWidth = if (tier.isPopular) 2.dp else 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "بسته ${tier.nameFa}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tier.nameEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextSecondary,
                                    fontFamily = OutfitFontFamily
                                )
                            }
                            if (tier.isPopular) {
                                StatusPill(
                                    label = "پیشنهاد پلتفرم • بهترین نسبت ارزش به قیمت",
                                    statusColor = ElectricIndigo,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(9999.dp),
                                color = StatusConnected.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, StatusConnected)
                            ) {
                                Text(
                                    text = "پلن فعال شما",
                                    color = StatusConnected,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = tier.priceFa,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "/ ماهانه", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = TranslucentBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Explicit Limits Display (Configured by Admin)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سقف توکن متنی ماهانه:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(tier.textTokensLimit, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سهمیه تصاویر استودیو:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${tier.imageGenerations} تصویر با کیفیت", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("کردیت هدیه ساخت ویدیو:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${tier.videoCredits} اعتبار ویدیو", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CyberViolet, fontFamily = OutfitFontFamily)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("اولویت صف پردازش:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(tier.queuePriority, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Features configured
                    tier.features.forEach { feat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusConnected, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = feat, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        text = if (isCurrent) "تمدید اشتراک فعلی (${tier.priceFa})" else "خرید و فعال‌سازی (${tier.priceFa})",
                        onClick = { activeCheckoutOrder = PlatformRepository.createSubscriptionOrder(tier.id) },
                        isSecondary = isCurrent,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "tier_buy_btn_${tier.id}"
                    )
                }
            }

            // DETAILED COMPARISON TABLE
            item {
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = TranslucentBorder
                ) {
                    Text(
                        text = "جدول جامع مقایسه مشخصات و محدودیت پکیج‌ها:",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مقادیر دقیق سقف مصرف و امکانات هر بسته بر اساس تنظیمات تایید شده پنل مدیریت:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comparison Rows
                    val base = subscriptionTiers[0]
                    val pro = subscriptionTiers[1]
                    val adv = subscriptionTiers[2]

                    ComparisonRow(title = "هزینه اشتراک ماهانه", baseVal = base.priceFa, proVal = pro.priceFa, advVal = adv.priceFa, isHeader = false)
                    ComparisonRow(title = "سقف توکن متنی ماهانه", baseVal = base.textTokensLimit, proVal = pro.textTokensLimit, advVal = adv.textTokensLimit)
                    ComparisonRow(title = "سهمیه تصویرسازی", baseVal = "${base.imageGenerations} تصویر", proVal = "${pro.imageGenerations} تصویر", advVal = "${adv.imageGenerations} تصویر")
                    ComparisonRow(title = "کردیت ویدیو هدیه در بسته", baseVal = "${base.videoCredits} اعتبار", proVal = "${pro.videoCredits} اعتبار", advVal = "${adv.videoCredits} اعتبار")
                    ComparisonRow(title = "اولویت صف رندر", baseVal = base.queuePriority, proVal = pro.queuePriority, advVal = adv.queuePriority)
                    ComparisonRow(title = "حداکثر وضوح رندر", baseVal = base.maxRenderQuality, proVal = pro.maxRenderQuality, advVal = adv.maxRenderQuality)
                    ComparisonRow(title = "نرخ مجاز API", baseVal = base.apiRateLimit, proVal = pro.apiRateLimit, advVal = adv.apiRateLimit)
                    ComparisonRow(title = "سطح پشتیبانی فنی", baseVal = base.supportLevel, proVal = pro.supportLevel, advVal = adv.supportLevel)
                    ComparisonRow(
                        title = "فاین‌تیونینگ اختصاصی",
                        baseVal = if (base.customFineTuning) "فعال" else "غیرفعال در این بسته",
                        proVal = if (pro.customFineTuning) "فعال" else "غیرفعال در این بسته",
                        advVal = if (adv.customFineTuning) "کلاستر اختصاصی" else "غیرفعال در این بسته"
                    )
                }
            }
        }

        // ==========================================
        // TAB 1: DEDICATED VIDEO CREDITS (450 to 60,000,000)
        // ==========================================
        if (activeTab == 1) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = CyberViolet.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "خرید اعتبار اختصاصی ویدیو (از ۴۵۰ تا ۶۰,۰۰۰,۰۰۰)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "مستقل از سهمیه متنی • بدون تاریخ انقضا و با قابلیت رزرو و بازگشت (Rollback)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        StatusPill(
                            label = if (creditConfig.isUnlimitedValidity) "اعتبار دائمی" else "اعتبار ۱۲ ماهه",
                            statusColor = StatusConnected
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Custom Credit Input
                    Text(
                        text = "مقدار کردیت دلخواه را وارد یا با اسلایدر انتخاب کنید:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customCreditInput,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }
                            customCreditInput = clean
                            val num = clean.toLongOrNull() ?: 450L
                            customCreditSlider = num.coerceIn(creditConfig.minPurchaseCredits, creditConfig.maxPurchaseCredits).toFloat()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_credit_input"),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text("اعتبار ویدیو", style = MaterialTheme.typography.labelSmall, color = CyberViolet, modifier = Modifier.padding(end = 12.dp))
                        },
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

                    // Range Slider (450 to 60,000,000 on logarithmic feel or tiered scale)
                    val currentCredits = customCreditInput.toLongOrNull() ?: 450L
                    Slider(
                        value = customCreditSlider,
                        onValueChange = { value ->
                            customCreditSlider = value
                            customCreditInput = value.toLong().toString()
                        },
                        valueRange = 450f..100_000f, // interactive slider up to 100k, direct input supports up to 60,000,000
                        colors = SliderDefaults.colors(
                            thumbColor = CyberViolet,
                            activeTrackColor = CyberViolet,
                            inactiveTrackColor = SurfaceElevation3
                        )
                    )

                    // Calculated Price & Discount Breakdown
                    val calculatedPrice = PlatformRepository.calculateCreditPrice(currentCredits)
                    val discountPercent = when {
                        currentCredits >= 60_000_000L -> 50
                        currentCredits >= 500_000L -> 35
                        currentCredits >= 50_000L -> 25
                        currentCredits >= 10_000L -> 18
                        currentCredits >= 2_500L -> 10
                        else -> 0
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = VoidCanvas,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, TranslucentBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("نرخ پایه هر کردیت:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text("${creditConfig.pricePerCreditToman} تومان", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("تخفیف حجم خرید:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(if (discountPercent > 0) "$discountPercent٪ تخفیف اعمال شد" else "بدون تخفیف", style = MaterialTheme.typography.bodySmall, color = if (discountPercent > 0) StatusConnected else TextTertiary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("مبلغ نهایی قابل پرداخت:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${String.format("%,d", calculatedPrice)} تومان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CyberButton(
                        text = "خرید آنی ${String.format("%,d", currentCredits)} کردیت (${String.format("%,d", calculatedPrice)} تومان)",
                        onClick = {
                            activeCheckoutOrder = PlatformRepository.createCustomCreditOrder(currentCredits)
                        },
                        icon = Icons.Default.ShoppingCart,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_buy_custom_credits"
                    )
                }
            }

            // Quick Predefined Packages (450 to 60,000,000)
            item {
                Text(
                    text = "بسته‌های پیش‌فرض آماده خرید سریع:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(PlatformRepository.videoCreditPackages) { pkg ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = if (pkg.discountPercent > 20) CyberViolet else TranslucentBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pkg.creditsFa, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                if (pkg.badge != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(shape = RoundedCornerShape(4.dp), color = ElectricIndigo.copy(alpha = 0.2f)) {
                                        Text(pkg.badge, color = ElectricIndigo, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text("تخفیف پکیج: ${pkg.discountPercent}٪", style = MaterialTheme.typography.labelSmall, color = if (pkg.discountPercent > 0) StatusConnected else TextSecondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(pkg.priceFa, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Button(
                                onClick = { activeCheckoutOrder = PlatformRepository.createCreditPackageOrder(pkg.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("خرید بسته", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 2: CREDIT STATEMENT & RESERVATIONS (گردش حساب کامل)
        // ==========================================
        if (activeTab == 2) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceElevation1,
                    borderColor = ActiveBorder
                ) {
                    Text(
                        text = "صورتحساب و گردش کامل کردیت‌های ویدیو (Credit Statement):",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "شفافیت صددرصدی در ثبت هرگونه افزایش موجودی، رزرو در صف جاب‌ها، کسر رندر و بازگشت خودکار (Rollback)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (creditLedger.isEmpty()) {
                item {
                    Text("هنوز تراکنشی در استخر کردیت ثبت نشده است.", color = TextSecondary)
                }
            } else {
                items(creditLedger) { entry ->
                    CreditLedgerRow(entry = entry)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Real Payment Checkout & Verification Flow
    if (activeCheckoutOrder != null) {
        PaymentCheckoutDialog(
            initialOrder = activeCheckoutOrder!!,
            onDismiss = { activeCheckoutOrder = null },
            onOrderCompleted = { /* Order finished and applied strictly on server */ }
        )
    }
}

// Comparison Row Composable
@Composable
fun ComparisonRow(
    title: String,
    baseVal: String,
    proVal: String,
    advVal: String,
    isHeader: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = if (isHeader) SurfaceElevation2 else Color.Transparent,
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ElectricIndigo
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Base
                Surface(
                    modifier = Modifier.weight(1f),
                    color = SurfaceElevation2.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("پایه", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Text(baseVal, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
                // Pro
                Surface(
                    modifier = Modifier.weight(1f),
                    color = ElectricIndigo.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("حرفه‌ای", style = MaterialTheme.typography.labelSmall, color = ElectricIndigo)
                        Text(proVal, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
                // Adv
                Surface(
                    modifier = Modifier.weight(1f),
                    color = SurfaceElevation2.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        Text("پیشرفته", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        Text(advVal, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
        }
    }
}

// Credit Ledger Entry Row
@Composable
fun CreditLedgerRow(entry: CreditLedgerEntry) {
    val isPositive = entry.type.isPositive
    val sign = if (isPositive) "+" else ""
    val amountColor = if (isPositive) StatusConnected else StatusWarning

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ledger_row_${entry.id}"),
        backgroundColor = SurfaceElevation1,
        borderColor = TranslucentBorder
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.titleFa,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ارجاع: ${entry.referenceId} • ${entry.dateFa}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = OutfitFontFamily
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$sign${entry.amountCredits} اعتبار",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    fontFamily = OutfitFontFamily
                )
                Text(
                    text = "مانده پس از تراکنش: ${entry.balanceAfter}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    fontFamily = OutfitFontFamily
                )
            }
        }
    }
}
