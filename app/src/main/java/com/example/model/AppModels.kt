package com.example.model

enum class AppScreen(val titleFa: String, val category: String) {
    HOME("خانه", "اصلی"),
    DASHBOARD("داشبورد", "اصلی"),
    CHAT("چت چندمدلی", "استودیو"),
    IMAGE_STUDIO("استودیو تصویر", "استودیو"),
    VIDEO_STUDIO("استودیو ویدیو", "استودیو"),
    VOICE_STUDIO("مکالمه صوتی دوطرفه", "استودیو"),
    TEXT_TO_SPEECH("تبدیل متن به گفتار (TTS)", "استودیو"),
    SPEECH_TO_TEXT("تبدیل گفتار به متن (STT)", "استودیو"),
    MODELS_DIRECTORY("فهرست مدل‌ها", "اکتشاف"),
    HISTORY("تاریخچه فعالیت", "کاربری"),
    PRICING("اشتراک و اعتبار", "مالی"),
    TRANSACTIONS("فاکتورها و تراکنش‌ها", "مالی"),
    PROFILE("پروفایل کاربری", "کاربری"),
    SETTINGS("تنظیمات", "سیستم"),
    HELP("راهنما و پشتیبانی", "پشتیبانی"),
    ADMIN("پنل مدیریت", "مدیریت"),
    AUTH("ورود / ثبت‌نام", "حساب")
}

data class SubscriptionTier(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val priceToman: Long,
    val priceFa: String,
    val period: String = "ماهانه",
    val isPopular: Boolean = false,
    val textTokensLimit: String,
    val imageGenerations: Int,
    val videoCredits: Int,
    val accessibleModels: List<String> = emptyList(),
    val queuePriority: String = "استاندارد",
    val maxRenderQuality: String = "Full HD (1080p)",
    val apiRateLimit: String = "۳۰ درخواست/دقیقه",
    val supportLevel: String = "تیکت استاندارد (۱۲ ساعت)",
    val customFineTuning: Boolean = false,
    val features: List<String>
)

enum class CreditLedgerType(val labelFa: String, val isPositive: Boolean) {
    PURCHASE("خرید بسته اعتبار ویدیو", true),
    PLAN_GIFT("شارژ هدیه اشتراک ماهانه", true),
    JOB_RESERVED("رزرو شده جهت رندر ویدیو", false),
    JOB_CONSUMED("کسر قطعی جاب تکمیل شده", false),
    JOB_ROLLBACK("بازگشت اعتبار خطای سرور (Rollback)", true)
}

data class CreditLedgerEntry(
    val id: String,
    val type: CreditLedgerType,
    val titleFa: String,
    val amountCredits: Long,
    val referenceId: String,
    val dateFa: String,
    val balanceAfter: Long
)

data class VideoCreditConfig(
    val pricePerCreditToman: Int = 400,
    val minPurchaseCredits: Long = 450,
    val maxPurchaseCredits: Long = 60_000_000,
    val validityMonths: Int = 12,
    val isUnlimitedValidity: Boolean = true
)

data class VideoCreditPackage(
    val id: String,
    val credits: Long,
    val creditsFa: String,
    val priceToman: Long,
    val priceFa: String,
    val discountPercent: Int = 0,
    val badge: String? = null
)

data class VideoGenerationModel(
    val id: String,
    val name: String,
    val provider: String,
    val costPer5Sec: Int,
    val resolution: String,
    val maxDurationSeconds: Int,
    val tag: String
)

enum class JobStatus(val labelFa: String) {
    QUEUE("در صف پردازش"),
    PROCESSING("در حال رندر و سنتز"),
    SUCCESS("تکمیل شده"),
    FAILED("خطا در پردازش")
}

data class VideoJob(
    val id: String,
    val prompt: String,
    val model: VideoGenerationModel,
    var status: JobStatus,
    var progress: Int = 0,
    val creditCost: Int,
    val timestamp: String,
    val aspectRatio: String = "16:9",
    val motion: String = "زوم نرم (Smooth Zoom)",
    val style: String = "سینمایی (Cinematic)",
    val durationSec: Int = 5,
    val videoThumbUrl: String? = null,
    val errorMessage: String? = null
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val modelName: String = "GPT-4o",
    val tokensCount: Int = 120,
    val latencyMs: Int = 420,
    val isStreaming: Boolean = false
)

data class ImageGenerationItem(
    val id: String,
    val prompt: String,
    val style: String,
    val ratio: String,
    val modelName: String,
    val timestamp: String,
    val imageUrl: String
)

enum class ModelCategory(val labelFa: String) {
    ALL("همه مدل‌ها"),
    TEXT("متنی و چت"),
    IMAGE("تولید تصویر"),
    VIDEO("سنتز ویدیو"),
    AUDIO("صدا و گفتار")
}

data class AIModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val category: ModelCategory,
    val contextWindow: String,
    val latencyMs: Int,
    val costDesc: String,
    val descriptionFa: String,
    val tags: List<String>,
    val isOnline: Boolean = true,
    val rating: Double = 4.9
)

enum class HistoryType(val labelFa: String) {
    ALL("همه فعالیت‌ها"),
    CHAT("گفتگوها"),
    IMAGE("تصاویر"),
    VIDEO("ویدیوها"),
    AUDIO("صداها")
}

data class HistoryItem(
    val id: String,
    val type: HistoryType,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val modelName: String,
    val tokenOrCredit: String,
    val previewUrl: String? = null
)

data class TransactionItem(
    val id: String,
    val titleFa: String,
    val amountToman: Long,
    val typeFa: String,
    val dateFa: String,
    val isSuccess: Boolean,
    val trackingCode: String
)

data class AdminMetric(
    val activeUsers: Int,
    val runningGpuNodes: Int,
    val dailyRevenueToman: Long,
    val pendingVideoJobs: Int,
    val totalRequestsToday: Long
)

data class VoiceTurn(
    val id: String,
    val speaker: String,
    val text: String,
    val timestamp: String,
    val isUser: Boolean,
    val audioDurationSec: Int = 0
)

data class VoicePersona(
    val id: String,
    val nameFa: String,
    val titleFa: String,
    val gender: String,
    val tone: String,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f
)

// ==========================================
// PAYMENT GATEWAY & CONFIGURATION
// ==========================================
enum class PaymentGatewayProvider(val titleFa: String, val latinName: String, val defaultFeePercent: Double) {
    ZARINPAL("زرین‌پال (ZarinPal)", "zarinpal", 1.0),
    SEP("سامان‌کیش (SEP)", "sep", 0.0),
    BEHPARDAKHT("به‌پرداخت ملت (Mellat)", "behpardakht", 0.0),
    ZIBAL("زیبال (Zibal)", "zibal", 1.0)
}

data class PaymentGatewayConfig(
    val provider: PaymentGatewayProvider = PaymentGatewayProvider.ZARINPAL,
    val isSandboxMode: Boolean = true, // Visually highlighted until merchant key is provided
    val merchantId: String = "", // Empty indicates test/sandbox environment
    val callbackUrl: String = "https://orion.ai/api/v1/payment/verify",
    val autoRedirect: Boolean = true,
    val sandboxSimulatedSuccess: Boolean = true
)

// ==========================================
// REAL ORDERING & INVOICING
// ==========================================
enum class OrderType(val titleFa: String) {
    SUBSCRIPTION_PLAN("خرید / تمدید پکیج اشتراک"),
    VIDEO_CREDIT_PACKAGE("خرید بسته آماده کردیت ویدیو"),
    CUSTOM_VIDEO_CREDITS("خرید اعتبار سفارشی استخر ویدیو")
}

enum class OrderPaymentStatus(val titleFa: String, val badgeColorName: String) {
    PENDING_PAYMENT("در انتظار پرداخت", "warning"),
    VERIFYING("در حال استعلام از شاپرک", "info"),
    PAID_SUCCESS("پرداخت موفق و تحویل شد", "success"),
    FAILED("تراکنش ناموفق", "error"),
    REFUNDED("مبلغ مسترد شد (بازپرداخت)", "refund")
}

data class OrderInvoice(
    val orderId: String,
    val trackingCode: String,
    val rrn: String, // شماره مرجع رسمی شاپرک (Retrieval Reference Number)
    val userId: String = "USR-88219-IR",
    val userFullName: String = "علی رادمنش",
    val userPhone: String = "۰۹۱۲۳۴۵۶۷۸۹",
    val orderType: OrderType,
    val itemId: String,
    val itemTitleFa: String,
    val quantityOrCredits: Long,
    val subtotalToman: Long,
    val discountToman: Long,
    val vatToman: Long, // ۹٪ مالیات بر ارزش افزوده قانونی
    val payableToman: Long,
    val status: OrderPaymentStatus,
    val gateway: PaymentGatewayProvider,
    val isSandbox: Boolean,
    val createdAtFa: String,
    val paidAtFa: String? = null,
    val cardMasked: String? = null,
    val couponCodeApplied: String? = null,
    val failureReason: String? = null,
    val refundReason: String? = null
)

// ==========================================
// USER MANAGEMENT & ACCOUNTS
// ==========================================
data class PlatformUser(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val currentPlanId: String,
    val planNameFa: String,
    val totalVideoCredits: Long,
    val reservedCredits: Long,
    val textTokensBalance: Long,
    val isBlocked: Boolean = false,
    val registeredDateFa: String,
    val totalSpentToman: Long
)

// ==========================================
// SUPPORT TICKETS
// ==========================================
enum class TicketPriority(val titleFa: String) {
    LOW("عادی"),
    MEDIUM("متوسط"),
    HIGH("مهم"),
    URGENT("فوری و بحرانی")
}

enum class TicketStatus(val titleFa: String) {
    OPEN("باز / در انتظار بررسی"),
    IN_PROGRESS("در حال پیگیری تیم فنی"),
    ANSWERED("پاسخ داده شده"),
    CLOSED("بسته شده")
}

data class TicketMessage(
    val id: String,
    val senderName: String,
    val isStaff: Boolean,
    val message: String,
    val timestampFa: String
)

data class SupportTicket(
    val id: String,
    val userId: String,
    val userName: String,
    val subject: String,
    val category: String,
    val priority: TicketPriority,
    val status: TicketStatus,
    val createdAtFa: String,
    val messages: List<TicketMessage>
)

// ==========================================
// ADMIN SENSITIVE ACTION AUDIT LOG
// ==========================================
data class AdminAuditLog(
    val id: String,
    val adminName: String,
    val actionFa: String,
    val sectionFa: String,
    val detailsFa: String,
    val timestampFa: String,
    val ipAddress: String = "192.168.1.104"
)

// ==========================================
// SYSTEM CONSUMPTION REPORT
// ==========================================
data class SystemConsumptionReport(
    val totalGpuHoursConsumed: Double = 1420.5,
    val totalVideoRendersCount: Int = 18450,
    val totalTokensProcessed: Long = 852_400_000L,
    val totalRevenueToman: Long = 184_200_000L,
    val activeOnlineUsers: Int = 412,
    val clusterLoadPercent: Int = 78
)

// ==========================================
// USER ROLE & ACCESS CONTROL
// ==========================================
enum class UserRole(val titleFa: String, val badgeColorName: String) {
    REGULAR_USER("کاربر عادی", "info"),
    SUPER_ADMIN("مدیر ارشد سامانه (Root)", "error")
}

// ==========================================
// SYSTEM BACKUP PAYLOAD
// ==========================================
data class UserBackupPayload(
    val exportTimestamp: String,
    val appVersion: String = "2.4.0-Production",
    val userProfile: PlatformUser,
    val activeSubscription: SubscriptionTier,
    val videoCreditBalance: Long,
    val reservedCreditBalance: Long,
    val creditLedger: List<CreditLedgerEntry>,
    val orders: List<OrderInvoice>,
    val transactions: List<TransactionItem>,
    val videoJobsCount: Int,
    val checksumSha256: String
)




