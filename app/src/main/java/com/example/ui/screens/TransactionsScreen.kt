package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlatformRepository
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun TransactionsScreen() {
    val orders by PlatformRepository.orders.collectAsState()
    val transactions by PlatformRepository.transactions.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: فاکتورهای رسمی شاپرک, 1: ریز تراکنش‌های مالی
    var selectedOrder by remember { mutableStateOf<OrderInvoice?>(null) }
    var selectedTx by remember { mutableStateOf<TransactionItem?>(null) }
    val clipboard = LocalClipboardManager.current
    var copyMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_screen_content"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
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
                            text = "فاکتورها و تراکنش‌های رسمی ORION AI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "سوابق پرداخت‌های بانکی شاپرک، ارزش افزوده ۹٪، کدهای رهگیری و استردادها",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = ElectricIndigo)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subtabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberChip(
                        text = "فاکتورهای پرداخت شاپرک (${orders.size})",
                        isSelected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f)
                    )
                    CyberChip(
                        text = "گردش کلی تراکنش‌ها (${transactions.size})",
                        isSelected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = Icons.Default.SwapHoriz,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // TAB 0: OFFICIAL INVOICES WITH RRN AND VAT
        if (activeTab == 0) {
            items(orders) { order ->
                val isSuccess = order.status == OrderPaymentStatus.PAID_SUCCESS
                val isRefunded = order.status == OrderPaymentStatus.REFUNDED

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_item_${order.orderId}"),
                    backgroundColor = SurfaceElevation1,
                    borderColor = when {
                        isSuccess -> StatusConnected.copy(alpha = 0.35f)
                        isRefunded -> CyberViolet.copy(alpha = 0.35f)
                        else -> StatusError.copy(alpha = 0.35f)
                    },
                    onClick = { selectedOrder = order }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = order.itemTitleFa,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (order.isSandbox) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusPill(label = "سندباکس", statusColor = StatusWarning)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "شناسه: ${order.orderId} • رهگیری: ${order.trackingCode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontFamily = OutfitFontFamily
                            )
                            Text(
                                text = "درگاه: ${order.gateway.titleFa} • تاریخ: ${order.paidAtFa ?: order.createdAtFa}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format("%,d", order.payableToman)} تومان",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) ElectricIndigo else if (isRefunded) CyberViolet else StatusError,
                                fontFamily = OutfitFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusPill(
                                label = order.status.titleFa,
                                statusColor = when (order.status) {
                                    OrderPaymentStatus.PAID_SUCCESS -> StatusConnected
                                    OrderPaymentStatus.REFUNDED -> CyberViolet
                                    OrderPaymentStatus.FAILED -> StatusError
                                    else -> StatusWarning
                                }
                            )
                        }
                    }
                }
            }
        }

        // TAB 1: ALL GENERAL TRANSACTIONS
        if (activeTab == 1) {
            items(transactions) { tx ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_item_${tx.id}"),
                    backgroundColor = SurfaceElevation1,
                    borderColor = TranslucentBorder,
                    onClick = { selectedTx = tx }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = tx.titleFa,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "کد رهگیری: ${tx.trackingCode} • تاریخ: ${tx.dateFa}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontFamily = OutfitFontFamily
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (tx.amountToman > 0) "${String.format("%,d", tx.amountToman)} تومان" else "بازگشت اعتبار",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tx.amountToman > 0) ElectricIndigo else StatusConnected
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusPill(
                                label = if (tx.isSuccess) "موفق" else "ناموفق",
                                statusColor = if (tx.isSuccess) StatusConnected else StatusError
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal: Full Order Digital Tax Receipt
    if (selectedOrder != null) {
        val order = selectedOrder!!
        AlertDialog(
            onDismissRequest = { selectedOrder = null },
            containerColor = SurfaceElevation1,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "صورتحساب رسمی شاپرک",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    StatusPill(
                        label = order.status.titleFa,
                        statusColor = if (order.status == OrderPaymentStatus.PAID_SUCCESS) StatusConnected else StatusError
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = order.itemTitleFa, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = ElectricIndigo)
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = TranslucentBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    ReceiptLine("شماره سفارش:", order.orderId)
                    ReceiptLine("کد رهگیری بانکی:", order.trackingCode)
                    ReceiptLine("شماره مرجع شاپرک (RRN):", order.rrn)
                    ReceiptLine("درگاه واسط:", order.gateway.titleFa)
                    if (order.cardMasked != null) {
                        ReceiptLine("کارت پرداخت:", order.cardMasked)
                    }
                    ReceiptLine("تاریخ پرداخت:", order.paidAtFa ?: order.createdAtFa)
                    ReceiptLine("مبلغ پایه:", "${String.format("%,d", order.subtotalToman)} تومان")
                    if (order.discountToman > 0) {
                        ReceiptLine("تخفیف:", "-${String.format("%,d", order.discountToman)} تومان")
                    }
                    ReceiptLine("مالیات بر ارزش افزوده (۹٪):", "+${String.format("%,d", order.vatToman)} تومان")
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = TranslucentBorder)
                    Spacer(modifier = Modifier.height(6.dp))
                    ReceiptLine("مبلغ کل پرداخت‌شده:", "${String.format("%,d", order.payableToman)} تومان", isBold = true)

                    if (order.refundReason != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "علت استرداد وجه: ${order.refundReason}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberViolet
                        )
                    }
                }
            },
            confirmButton = {
                CyberButton(
                    text = copyMessage ?: "کپی کد رهگیری",
                    onClick = {
                        clipboard.setText(AnnotatedString(order.trackingCode))
                        copyMessage = "کپی شد!"
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { selectedOrder = null }) {
                    Text("بستن", color = TextSecondary)
                }
            }
        )
    }

    // Modal: Simple Transaction Modal
    if (selectedTx != null) {
        val tx = selectedTx!!
        AlertDialog(
            onDismissRequest = { selectedTx = null },
            containerColor = SurfaceElevation2,
            title = {
                Text("جزئیات تراکنش مالی", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(text = tx.titleFa, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "مبلغ: ${String.format("%,d", tx.amountToman)} تومان", style = MaterialTheme.typography.bodyMedium, color = ElectricIndigo)
                    Text(text = "کد رهگیری: ${tx.trackingCode}", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontFamily = OutfitFontFamily)
                    Text(text = "تاریخ: ${tx.dateFa}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                }
            },
            confirmButton = {
                Button(onClick = { selectedTx = null }, colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)) {
                    Text("بستن")
                }
            }
        )
    }
}

@Composable
private fun ReceiptLine(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) ElectricIndigo else TextPrimary,
            fontFamily = OutfitFontFamily
        )
    }
}
