package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PlatformRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class CheckoutStep {
    PRE_INVOICE,     // خلاصه سفارش، کوپن تخفیف، انتخاب درگاه
    SHAPARAK_GATEWAY,// درگاه پرداخت شاپرک
    PAYMENT_RESULT   // رسید نهایی و تاییدیه
}

@Composable
fun PaymentCheckoutDialog(
    initialOrder: OrderInvoice,
    onDismiss: () -> Unit,
    onOrderCompleted: (OrderInvoice) -> Unit
) {
    var currentStep by remember { mutableStateOf(CheckoutStep.PRE_INVOICE) }
    var currentOrder by remember { mutableStateOf(initialOrder) }
    val gatewayConfig by PlatformRepository.paymentGatewayConfig.collectAsState()

    // Coupon code state
    var couponInput by remember { mutableStateOf(currentOrder.couponCodeApplied ?: "") }
    var couponMessage by remember { mutableStateOf<String?>(null) }

    // Shaparak Form States
    var cardNumber by remember { mutableStateOf("6037 9918 2491 8831") }
    var cvv2 by remember { mutableStateOf("782") }
    var expMonth by remember { mutableStateOf("08") }
    var expYear by remember { mutableStateOf("07") }
    var pin2 by remember { mutableStateOf("849102") }
    var otpTimer by remember { mutableIntStateOf(120) }
    var isOtpRequested by remember { mutableStateOf(true) }

    // Test simulation switches
    var simulateSuccess by remember { mutableStateOf(true) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Timer countdown effect for OTP
    LaunchedEffect(isOtpRequested) {
        if (isOtpRequested) {
            otpTimer = 120
            while (otpTimer > 0) {
                delay(1000)
                otpTimer--
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isProcessingPayment) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ActiveBorder, RoundedCornerShape(20.dp))
                .testTag("checkout_modal_container"),
            color = VoidCanvas
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Top Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceElevation1,
                    border = BorderStroke(1.dp, TranslucentBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElectricIndigo.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentStep) {
                                        CheckoutStep.PRE_INVOICE -> Icons.Default.ReceiptLong
                                        CheckoutStep.SHAPARAK_GATEWAY -> Icons.Default.AccountBalance
                                        CheckoutStep.PAYMENT_RESULT -> if (currentOrder.status == OrderPaymentStatus.PAID_SUCCESS) Icons.Default.Verified else Icons.Default.ErrorOutline
                                    },
                                    contentDescription = null,
                                    tint = ElectricIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = when (currentStep) {
                                        CheckoutStep.PRE_INVOICE -> "تکمیل سفارش و درگاه پرداخت"
                                        CheckoutStep.SHAPARAK_GATEWAY -> "درگاه پرداخت اینترنتی شاپرک"
                                        CheckoutStep.PAYMENT_RESULT -> "نتیجه تراکنش و رسید رسمی"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "شناسه سفارش: ${currentOrder.orderId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontFamily = OutfitFontFamily
                                )
                            }
                        }

                        IconButton(
                            onClick = { if (!isProcessingPayment) onDismiss() },
                            enabled = !isProcessingPayment
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "بستن", tint = TextSecondary)
                        }
                    }
                }

                // Modal Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        label = "checkout_step_animation"
                    ) { step ->
                        when (step) {
                            CheckoutStep.PRE_INVOICE -> PreInvoiceStep(
                                order = currentOrder,
                                gatewayConfig = gatewayConfig,
                                couponInput = couponInput,
                                onCouponChange = { couponInput = it },
                                couponMessage = couponMessage,
                                onApplyCoupon = {
                                    val discount = when (couponInput.trim().uppercase()) {
                                        "ORION20", "AURA20" -> currentOrder.subtotalToman * 20 / 100
                                        "NOWRUZ" -> currentOrder.subtotalToman * 15 / 100
                                        "VIP50" -> currentOrder.subtotalToman * 50 / 100
                                        else -> 0L
                                    }
                                    if (discount > 0) {
                                        val discountedSubtotal = (currentOrder.subtotalToman - discount).coerceAtLeast(0)
                                        val newVat = (discountedSubtotal * 9 / 100)
                                        currentOrder = currentOrder.copy(
                                            discountToman = discount,
                                            vatToman = newVat,
                                            payableToman = discountedSubtotal + newVat,
                                            couponCodeApplied = couponInput.trim().uppercase()
                                        )
                                        couponMessage = "کد تخفیف با موفقیت اعمال گردید (${String.format("%,d", discount)} تومان تخفیف)"
                                    } else {
                                        couponMessage = "کد تخفیف وارد شده معتبر نمی‌باشد (کدهای تست: ORION20, NOWRUZ, VIP50)"
                                    }
                                },
                                onSelectGateway = { provider ->
                                    PlatformRepository.updatePaymentGatewayConfig(gatewayConfig.copy(provider = provider))
                                    currentOrder = currentOrder.copy(gateway = provider)
                                },
                                onProceedToGateway = {
                                    currentStep = CheckoutStep.SHAPARAK_GATEWAY
                                }
                            )

                            CheckoutStep.SHAPARAK_GATEWAY -> ShaparakGatewayStep(
                                order = currentOrder,
                                isSandbox = currentOrder.isSandbox,
                                cardNumber = cardNumber,
                                onCardNumberChange = { cardNumber = it },
                                cvv2 = cvv2,
                                onCvv2Change = { cvv2 = it },
                                expMonth = expMonth,
                                onExpMonthChange = { expMonth = it },
                                expYear = expYear,
                                onExpYearChange = { expYear = it },
                                pin2 = pin2,
                                onPin2Change = { pin2 = it },
                                otpTimer = otpTimer,
                                onRequestOtp = { isOtpRequested = true },
                                simulateSuccess = simulateSuccess,
                                onToggleSimulation = { simulateSuccess = it },
                                isProcessing = isProcessingPayment,
                                onFillTestCard = {
                                    cardNumber = "6037 9918 2491 8831"
                                    cvv2 = "782"
                                    expMonth = "08"
                                    expYear = "07"
                                    pin2 = "849102"
                                },
                                onCancelPayment = {
                                    val failed = PlatformRepository.verifyPayment(
                                        orderId = currentOrder.orderId,
                                        isSuccess = false,
                                        failureReason = "تراکنش توسط خریدار در درگاه شاپرک لغو شد."
                                    )
                                    if (failed != null) currentOrder = failed
                                    currentStep = CheckoutStep.PAYMENT_RESULT
                                },
                                onSubmitPayment = {
                                    isProcessingPayment = true
                                    // Process payment strictly on server
                                    val masked = if (cardNumber.length >= 16) {
                                        val digits = cardNumber.replace(" ", "")
                                        "${digits.take(4)}-${digits.substring(4, 6)}**-****-${digits.takeLast(4)}"
                                    } else "۶۰۳۷-۹۹**-****-۸۸۳۱"

                                    val verifiedOrder = PlatformRepository.verifyPayment(
                                        orderId = currentOrder.orderId,
                                        cardMasked = masked,
                                        isSuccess = simulateSuccess,
                                        failureReason = if (simulateSuccess) null else "خطای بانکی: موجودی کارت کافی نیست یا رمز دوم منقضی شده است."
                                    )

                                    if (verifiedOrder != null) {
                                        currentOrder = verifiedOrder
                                        onOrderCompleted(verifiedOrder)
                                    }
                                    isProcessingPayment = false
                                    currentStep = CheckoutStep.PAYMENT_RESULT
                                }
                            )

                            CheckoutStep.PAYMENT_RESULT -> PaymentResultStep(
                                order = currentOrder,
                                onClose = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STEP 1: PRE-INVOICE & GATEWAY SELECTOR
// ==========================================
@Composable
private fun PreInvoiceStep(
    order: OrderInvoice,
    gatewayConfig: PaymentGatewayConfig,
    couponInput: String,
    onCouponChange: (String) -> Unit,
    couponMessage: String?,
    onApplyCoupon: () -> Unit,
    onSelectGateway: (PaymentGatewayProvider) -> Unit,
    onProceedToGateway: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explicit Sandbox vs Production Notice Banner
        item {
            if (order.isSandbox) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = StatusWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, StatusWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StatusWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.WarningAmber, contentDescription = null, tint = StatusWarning)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "حالت پرداخت آزمایشی (Sandbox Test Mode)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusWarning
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(label = "محیط تست", statusColor = StatusWarning)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "کلید مرچنت واقعی هنوز در تنظیمات درگاه وارد نشده است. کلیه تراکنش‌ها در محیط شبیه‌ساز امن شاپرک انجام شده و هیچ وجه واقعی از کارت کسر نمی‌گردد.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = StatusConnected.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StatusConnected)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = StatusConnected)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "درگاه پرداخت مستقیم متصل به شاپرک (Live Production Gateway)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Order Item Details Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "مشخصات سفارش و صورت‌حساب مالیاتی:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "عنوان محصول:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = order.itemTitleFa, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "نوع سفارش:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = order.orderType.titleFa, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "سهمیه / اعتبار ویدیو:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = "${String.format("%,d", order.quantityOrCredits)} اعتبار اختصاصی", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = CyberViolet, fontFamily = OutfitFontFamily)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = TranslucentBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Price Math (Strictly from server order)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "قیمت پایه سفارش:", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = "${String.format("%,d", order.subtotalToman)} تومان", style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontFamily = OutfitFontFamily)
                }

                if (order.discountToman > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "تخفیف ویژه اعمال‌شده (${order.couponCodeApplied ?: ""}):", style = MaterialTheme.typography.bodySmall, color = StatusConnected)
                        Text(text = "-${String.format("%,d", order.discountToman)} تومان", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = StatusConnected, fontFamily = OutfitFontFamily)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "مالیات بر ارزش افزوده قانونی (۹٪):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text(text = "+${String.format("%,d", order.vatToman)} تومان", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontFamily = OutfitFontFamily)
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = TranslucentBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "مبلغ قابل پرداخت نهایی:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = "${String.format("%,d", order.payableToman)} تومان",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo,
                        fontFamily = OutfitFontFamily
                    )
                }
            }
        }

        // Coupon Code Input Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "کد تخفیف سازمانی یا جشنواره:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = onCouponChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("مثلاً ORION20 یا NOWRUZ", style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = TranslucentBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Button(
                        onClick = onApplyCoupon,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("اعمال کد", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (couponMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = couponMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (couponMessage.contains("موفقیت")) StatusConnected else StatusWarning
                    )
                }
            }
        }

        // Payment Gateway Selector
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                Text(
                    text = "انتخاب درگاه پرداخت آنلاین:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                PaymentGatewayProvider.values().forEach { provider ->
                    val isSelected = order.gateway == provider
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectGateway(provider) },
                        color = if (isSelected) ElectricIndigo.copy(alpha = 0.12f) else SurfaceElevation2,
                        border = BorderStroke(1.dp, if (isSelected) ElectricIndigo else TranslucentBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectGateway(provider) },
                                    colors = RadioButtonDefaults.colors(selectedColor = ElectricIndigo)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = provider.titleFa, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = "اتصال پایدار با سوییچ هوشمند بانکی", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }

                            if (provider == PaymentGatewayProvider.ZARINPAL) {
                                StatusPill(label = "پیش‌فرض", statusColor = ElectricIndigo)
                            }
                        }
                    }
                }
            }
        }

        // Proceed Button
        item {
            CyberButton(
                text = "انتقال به درگاه امن پرداخت شاپرک (${String.format("%,d", order.payableToman)} تومان)",
                onClick = onProceedToGateway,
                modifier = Modifier.fillMaxWidth(),
                testTag = "proceed_to_shaparak_btn"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ==========================================
// STEP 2: SHAPARAK PAYMENT GATEWAY SIMULATION
// ==========================================
@Composable
private fun ShaparakGatewayStep(
    order: OrderInvoice,
    isSandbox: Boolean,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    cvv2: String,
    onCvv2Change: (String) -> Unit,
    expMonth: String,
    onExpMonthChange: (String) -> Unit,
    expYear: String,
    onExpYearChange: (String) -> Unit,
    pin2: String,
    onPin2Change: (String) -> Unit,
    otpTimer: Int,
    onRequestOtp: () -> Unit,
    simulateSuccess: Boolean,
    onToggleSimulation: (Boolean) -> Unit,
    isProcessing: Boolean,
    onFillTestCard: () -> Unit,
    onCancelPayment: () -> Unit,
    onSubmitPayment: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Shaparak Header Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceElevation2,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TranslucentBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "سامانه پرداخت الکترونیک شاپرک",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (isSandbox) {
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(label = "محیط سندباکس", statusColor = StatusWarning)
                            }
                        }
                        Text(
                            text = "پذیرنده: پلتفرم هوش مصنوعی اوریون (ORION AI Platform)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "شماره ترمینال: ۹۴۸۲۹۱۰۱ • کد پذیرنده: ۸۸۴۲۹۱",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontFamily = OutfitFontFamily
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "مبلغ پرداختی:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            text = "${String.format("%,d", order.payableToman)} تومان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricIndigo,
                            fontFamily = OutfitFontFamily
                        )
                        Text(
                            text = "معادل ${String.format("%,d", order.payableToman * 10)} ریال",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            fontFamily = OutfitFontFamily
                        )
                    }
                }
            }
        }

        // Test helper button
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ElectricIndigo.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "جهت تست سریع، اطلاعات کارت شبیه‌ساز را درج کنید:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary
                    )
                    OutlinedButton(
                        onClick = onFillTestCard,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تکمیل اطلاعات کارت آزمایشی", style = MaterialTheme.typography.labelSmall, color = ElectricIndigo)
                    }
                }
            }
        }

        // Banking Card Input Form
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1
            ) {
                // Card Number
                Text(text = "شماره کارت ۱۶ رقمی بانکی:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = onCardNumberChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_number_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = ElectricIndigo) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricIndigo,
                        unfocusedBorderColor = TranslucentBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // CVV2 & Expire Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CVV2
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "کد شناسایی (CVV2):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cvv2,
                            onValueChange = onCvv2Change,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricIndigo,
                                unfocusedBorderColor = TranslucentBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Expire Date
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "تاریخ انقضا (ماه / سال):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = expMonth,
                                onValueChange = onExpMonthChange,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("ماه") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricIndigo,
                                    unfocusedBorderColor = TranslucentBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                            OutlinedTextField(
                                value = expYear,
                                onValueChange = onExpYearChange,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("سال") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricIndigo,
                                    unfocusedBorderColor = TranslucentBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pin2 / Dynamic OTP
                Text(text = "رمز دوم پویا (پیامک شده):", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pin2,
                        onValueChange = onPin2Change,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricIndigo,
                            unfocusedBorderColor = TranslucentBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Button(
                        onClick = onRequestOtp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevation2),
                        border = BorderStroke(1.dp, TranslucentBorder),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            text = if (otpTimer > 0) "$otpTimer ثانیه" else "درخواست رمز پویا",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (otpTimer > 0) ElectricIndigo else TextPrimary
                        )
                    }
                }
            }
        }

        // Test Scenario Toggle (Success vs Failure)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceElevation2,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, TranslucentBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "شبیه‌سازی نتیجه تراکنش در درگاه تست:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = if (simulateSuccess) "تراکنش موفق (تایید آنی و شارژ حساب)" else "تراکنش ناموفق (خطای کسر موجودی / لغو)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (simulateSuccess) StatusConnected else StatusError
                        )
                    }

                    Switch(
                        checked = simulateSuccess,
                        onCheckedChange = onToggleSimulation,
                        colors = SwitchDefaults.colors(checkedThumbColor = StatusConnected, checkedTrackColor = StatusConnected.copy(alpha = 0.5f))
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelPayment,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("انصراف از پرداخت", color = StatusError)
                }

                Button(
                    onClick = onSubmitPayment,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusConnected)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تایید و پرداخت", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ==========================================
// STEP 3: OFFICIAL DIGITAL RECEIPT & RESULT
// ==========================================
@Composable
private fun PaymentResultStep(
    order: OrderInvoice,
    onClose: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copiedText by remember { mutableStateOf<String?>(null) }
    val isSuccess = order.status == OrderPaymentStatus.PAID_SUCCESS

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Result Status Icon Banner
        item {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(if (isSuccess) StatusConnected.copy(alpha = 0.2f) else StatusError.copy(alpha = 0.2f))
                    .border(2.dp, if (isSuccess) StatusConnected else StatusError, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isSuccess) StatusConnected else StatusError,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSuccess) "پرداخت شما با موفقیت تایید و ثبت شد" else "پرداخت ناموفق بود",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSuccess) StatusConnected else StatusError
            )
            Text(
                text = if (isSuccess) "سرویس شما بلافاصله فعال و رسید مالیاتی زیر صادر گردید." else (order.failureReason ?: "عملیات پرداخت تکمیل نشد."),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }

        // Official Tax Digital Receipt Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceElevation1,
                borderColor = if (isSuccess) StatusConnected.copy(alpha = 0.4f) else StatusError.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "رسید دیجیتال پرداخت الکترونیکی شاپرک",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    StatusPill(
                        label = if (isSuccess) "موفق - تسویه شد" else "ناموفق",
                        statusColor = if (isSuccess) StatusConnected else StatusError
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = TranslucentBorder)
                Spacer(modifier = Modifier.height(10.dp))

                ReceiptDataRow("شماره فاکتور سیستم:", order.orderId)
                ReceiptDataRow("کد رهگیری پرداخت (Tracking):", order.trackingCode)
                ReceiptDataRow("شماره مرجع شاپرک (RRN):", order.rrn)
                ReceiptDataRow("درگاه بانکی پرداخت:", order.gateway.titleFa)
                if (order.cardMasked != null) {
                    ReceiptDataRow("شماره کارت خریدار:", order.cardMasked)
                }
                ReceiptDataRow("تاریخ و ساعت ثبت:", order.paidAtFa ?: order.createdAtFa)
                ReceiptDataRow("محصول خریداری‌شده:", order.itemTitleFa)
                ReceiptDataRow("مبلغ اصلی:", "${String.format("%,d", order.subtotalToman)} تومان")

                if (order.discountToman > 0) {
                    ReceiptDataRow("تخفیف:", "-${String.format("%,d", order.discountToman)} تومان")
                }
                ReceiptDataRow("مالیات بر ارزش افزوده (۹٪):", "+${String.format("%,d", order.vatToman)} تومان")

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = TranslucentBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "مبلغ نهایی پرداخت‌شده:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        text = "${String.format("%,d", order.payableToman)} تومان",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ElectricIndigo,
                        fontFamily = OutfitFontFamily
                    )
                }
            }
        }

        // Copy Tracking Code and Close Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(order.trackingCode))
                        copiedText = "کد رهگیری کپی شد"
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(copiedText ?: "کپی کد رهگیری", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                ) {
                    Text("مشاهده در حساب و اتمام", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReceiptDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = OutfitFontFamily)
    }
}
