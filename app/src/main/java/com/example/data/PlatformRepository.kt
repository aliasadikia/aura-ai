package com.example.data

import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object PlatformRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 1. Subscription Tiers (Admin-Configurable StateFlow)
    private val _subscriptionTiers = MutableStateFlow<List<SubscriptionTier>>(
        listOf(
            SubscriptionTier(
                id = "tier_base",
                nameFa = "پایه",
                nameEn = "Base Tier",
                priceToman = 850_000,
                priceFa = "۸۵۰,۰۰۰ تومان",
                period = "ماهانه",
                isPopular = false,
                textTokensLimit = "۱,۵۰۰,۰۰۰ توکن ماهانه",
                imageGenerations = 150,
                videoCredits = 450,
                accessibleModels = listOf("GPT-4o Mini", "Claude Haiku", "Llama 3.3 70B"),
                queuePriority = "صف استاندارد اشتراکی",
                maxRenderQuality = "Full HD (1080p)",
                apiRateLimit = "۳۰ درخواست در دقیقه",
                supportLevel = "پشتیبانی تیکتی (پاسخ تا ۱۲ ساعت)",
                customFineTuning = false,
                features = listOf(
                    "دسترسی به مدل‌های استاندارد (GPT-4o Mini و Claude Haiku)",
                    "۱,۵۰۰,۰۰۰ توکن متنی ماهانه",
                    "۱۵۰ تصویر استودیو استاندارد",
                    "۴۵۰ اعتبار اختصاصی ویدیو",
                    "پشتیبانی تیکتی استاندارد (پاسخ تا ۱۲ ساعت)",
                    "دسترسی به API با نرخ ۳۰ رکوئست در دقیقه"
                )
            ),
            SubscriptionTier(
                id = "tier_pro",
                nameFa = "حرفه‌ای",
                nameEn = "Professional Tier",
                priceToman = 2_850_000,
                priceFa = "۲,۸۵۰,۰۰۰ تومان",
                period = "ماهانه",
                isPopular = true,
                textTokensLimit = "۶,۰۰۰,۰۰۰ توکن ماهانه",
                imageGenerations = 600,
                videoCredits = 2500,
                accessibleModels = listOf("GPT-4o", "Claude 3.5 Sonnet", "Gemini 1.5 Pro", "Runway Gen-3", "Kling AI"),
                queuePriority = "صف اولویت‌دار (Priority Queue)",
                maxRenderQuality = "4K Ultra HD",
                apiRateLimit = "۱۲۰ درخواست در دقیقه",
                supportLevel = "پشتیبانی اختصاصی تلگرام و تیکت (زیر ۲ ساعت)",
                customFineTuning = false,
                features = listOf(
                    "دسترسی کامل به GPT-4o, Claude 3.5 Sonnet, Gemini 1.5 Pro",
                    "۶,۰۰۰,۰۰۰ توکن متنی با اولویت بالا",
                    "۶۰۰ تصویر استودیو با کیفیت 4K و Upscale",
                    "۲,۵۰۰ اعتبار اختصاصی ویدیو (رندر Runway و Kling)",
                    "مکالمه صوتی زنده نامحدود و تبدیل گفتار",
                    "صف پردازش با اولویت (Priority Queue)",
                    "پشتیبانی سریع اختصاصی تلگرام و تیکت (زیر ۲ ساعت)"
                )
            ),
            SubscriptionTier(
                id = "tier_advanced",
                nameFa = "پیشرفته",
                nameEn = "Advanced Enterprise",
                priceToman = 5_350_000,
                priceFa = "۵,۳۵۰,۰۰۰ تومان",
                period = "ماهانه",
                isPopular = false,
                textTokensLimit = "نامحدود با مصرف منصفانه (۲۰M+)",
                imageGenerations = 2000,
                videoCredits = 10000,
                accessibleModels = listOf("کلیه مدل‌های برتر دنیا", "DeepSeek R1", "Sora Ultra", "FLUX.1 Pro"),
                queuePriority = "کلاستر اختصاصی GPU بدون صف",
                maxRenderQuality = "8K Master Render",
                apiRateLimit = "نامحدود (۱۲۰ درخواست در ثانیه)",
                supportLevel = "مدیر حساب اختصاصی و پشتیبانی تلفنی و VIP ۲۴/۷",
                customFineTuning = true,
                features = listOf(
                    "دسترسی نامحدود به تمامی مدل‌های برتر دنیا (DeepSeek R1, Sora)",
                    "۱۰,۰۰۰ اعتبار اختصاصی ویدیو با کلاستر اختصاصی",
                    "۲,۰۰۰ رندر تصویر Ultra HD بدون واترمارک",
                    "فاین‌تیونینگ (Fine-Tuning) اختصاصی روی داده‌های سازمانی",
                    "دسترسی نامحدود به کلید API با نرخ ۱۲۰ رکوئست در ثانیه",
                    "مدیر حساب اختصاصی و پشتیبانی تلفنی و VIP ۲۴/۷",
                    "ضمانت پایداری SLA ۹۹.۹٪"
                )
            )
        )
    )
    val subscriptionTiers: StateFlow<List<SubscriptionTier>> = _subscriptionTiers.asStateFlow()

    // 2. Video Credit Pricing & Pool Configuration (Admin-Configurable)
    private val _videoCreditConfig = MutableStateFlow(VideoCreditConfig())
    val videoCreditConfig: StateFlow<VideoCreditConfig> = _videoCreditConfig.asStateFlow()

    // Predefined Quick Purchase Packages (Pool 450 to 60,000,000)
    val videoCreditPackages = listOf(
        VideoCreditPackage("vc_450", 450, "۴۵۰ اعتبار ویدیو", 180_000, "۱۸۰,۰۰۰ تومان", 0, "بسته شروع"),
        VideoCreditPackage("vc_2500", 2500, "۲,۵۰۰ اعتبار ویدیو", 890_000, "۸۹۰,۰۰۰ تومان", 10, "اقتصادی"),
        VideoCreditPackage("vc_10000", 10000, "۱۰,۰۰۰ اعتبار ویدیو", 3_200_000, "۳,۲۰۰,۰۰۰ تومان", 18, "محبوب فیلم‌سازان"),
        VideoCreditPackage("vc_50000", 50000, "۵۰,۰۰۰ اعتبار ویدیو", 14_500_000, "۱۴,۵۰۰,۰۰۰ تومان", 25, "تولید محتوای حرفه‌ای"),
        VideoCreditPackage("vc_500000", 500000, "۵۰۰,۰۰۰ اعتبار ویدیو", 125_000_000, "۱۲۵,۰۰۰,۰۰۰ تومان", 35, "استودیویی"),
        VideoCreditPackage("vc_60000000", 60_000_000, "۶۰,۰۰۰,۰۰۰ اعتبار ویدیو", 4_200_000_000, "۴,۲۰۰,۰۰۰,۰۰۰ تومان", 50, "سازمانی و تلویزیونی")
    )

    // 3. Video Models & Deduction Rules (Admin-Configurable)
    private val _videoModels = MutableStateFlow<List<VideoGenerationModel>>(
        listOf(
            VideoGenerationModel("sora_ultra", "Sora Ultra", "OpenAI", 120, "1080p / 4K", 20, "بالاترین کیفیت واقع‌گرایانه"),
            VideoGenerationModel("runway_gen3", "Gen-3 Alpha Turbo", "RunwayML", 80, "1080p 60fps", 15, "کنترل دقیق دوربین و حرکت"),
            VideoGenerationModel("kling_v15", "Kling AI v1.5 Pro", "Kuaishou", 60, "1080p High-Motion", 15, "بهترین فیزیک و پویایی حرکتی"),
            VideoGenerationModel("luma_dream", "Luma Dream Machine", "Luma AI", 50, "720p / 1080p", 10, "رندرهای سینمایی سریع"),
            VideoGenerationModel("haiper_2", "Haiper 2.0", "Haiper AI", 40, "1080p 30fps", 10, "انیمیشن و افکت‌های پرانرژی"),
            VideoGenerationModel("pika_2", "Pika 2.0", "Pika Labs", 35, "720p", 10, "افکت‌های تغییر فیزیکی و طنز")
        )
    )
    val videoModels: StateFlow<List<VideoGenerationModel>> = _videoModels.asStateFlow()

    // AI Models Directory
    val aiModelsDirectory = listOf(
        AIModelInfo("gpt-4o", "GPT-4o Omnimodal", "OpenAI", ModelCategory.TEXT, "128K توکن", 380, "۵ توکن در کلمه", "هوشمندترین مدل چندوجهی متنی، بینایی و استدلال با تسلط کامل بر زبان فارسی.", listOf("متن", "کدنویسی", "بینایی"), rating = 4.95),
        AIModelInfo("claude-3-5-sonnet", "Claude 3.5 Sonnet", "Anthropic", ModelCategory.TEXT, "200K توکن", 410, "۴.۵ توکن در کلمه", "پیشتاز جهانی در کدنویسی، تحلیل اسناد طولانی و نثر طبیعی فارسی.", listOf("کدنویسی ارشد", "پژوهش"), rating = 4.98),
        AIModelInfo("deepseek-r1", "DeepSeek R1 Reasoner", "DeepSeek", ModelCategory.TEXT, "64K توکن", 580, "۲ توکن در کلمه", "مدل استدلال عمیق با گام‌های فکری زنجیره‌ای (CoT) در ریاضیات و الگوریتم‌ها.", listOf("استدلال عمیق", "ریاضی"), rating = 4.92),
        AIModelInfo("gemini-1-5-pro", "Gemini 1.5 Pro", "Google DeepMind", ModelCategory.TEXT, "2,000,000 توکن", 440, "۴ توکن در کلمه", "بزرگترین پنجره زمینه جهان با ظرفیت پردازش صدها صفحه داده.", listOf("۲ میلیون توکن", "گوگل"), rating = 4.91),
        AIModelInfo("midjourney-v6", "Midjourney v6.1", "Midjourney", ModelCategory.IMAGE, "پرامپت متنی تا ۴۰۰ کلمه", 3200, "۲۵ اعتبار در تصویر", "پادشاه تصویرسازی خلاقانه، پرتره‌های فتورئالیستیک و بافت‌های نوری بی‌نقص.", listOf("هنری", "فتورئال"), rating = 4.97),
        AIModelInfo("flux-1-pro", "FLUX.1 Pro Ultra", "Black Forest Labs", ModelCategory.IMAGE, "متن و تصویر به تصویر", 2100, "۱۵ اعتبار در تصویر", "دقت فوق‌العاده در رسم تایپوگرافی و وفاداری به پرامپت.", listOf("تایپوگرافی", "رندر واقعی"), rating = 4.94),
        AIModelInfo("sora-ultra", "Sora Ultra", "OpenAI", ModelCategory.VIDEO, "پرامپت تا ۵۰۰ کاراکتر", 8500, "۱۲۰ اعتبار / ۵ ثانیه", "مدل شبیه‌ساز فیزیک دنیای واقعی با دوربین‌های متحرک پیچیده.", listOf("سینمایی", "4K"), rating = 4.96),
        AIModelInfo("runway-gen3", "Runway Gen-3 Alpha", "RunwayML", ModelCategory.VIDEO, "پرامپت متنی و تصویر مرجع", 6200, "۸۰ اعتبار / ۵ ثانیه", "حرکات سیال دوربین و تولید جلوه‌های ویژه خیره‌کننده.", listOf("سینمایی", "Runway"), rating = 4.89),
        AIModelInfo("elevenlabs-v3", "ElevenLabs Voice Turbo", "ElevenLabs", ModelCategory.AUDIO, "تا ۵۰۰۰ کاراکتر", 240, "۱ اعتبار در ۱۰۰ کاراکتر", "طبیعی‌ترین سنتز صدا با احساسات انسانی و فارسی روان.", listOf("شبیه‌سازی صدا", "کم‌تاخیر"), rating = 4.96),
        AIModelInfo("whisper-v3", "Whisper Large v3 Turbo", "OpenAI", ModelCategory.AUDIO, "فایل صوتی تا ۲۵MB", 650, "۵ اعتبار در دقیقه", "دقیق‌ترین مدل تشخیص گفتار جهان با پیاده‌سازی خودکار واژگان تخصصی فارسی.", listOf("صوت به متن", "فارسی"), rating = 4.93)
    )

    // User Credit State (Total, Reserved, Available)
    private val _videoCredits = MutableStateFlow(2420L)
    val videoCredits: StateFlow<Long> = _videoCredits.asStateFlow()

    private val _reservedCredits = MutableStateFlow(0L)
    val reservedCredits: StateFlow<Long> = _reservedCredits.asStateFlow()

    private val _textTokensRemaining = MutableStateFlow(4820000L)
    val textTokensRemaining: StateFlow<Long> = _textTokensRemaining.asStateFlow()

    private val _userPlan = MutableStateFlow(subscriptionTiers.value[1]) // Professional tier
    val userPlan: StateFlow<SubscriptionTier> = _userPlan.asStateFlow()

    private val _userApiKey = MutableStateFlow("sk-orion-9921-prod-live-883a00")
    val userApiKey: StateFlow<String> = _userApiKey.asStateFlow()

    // Access Control & Current User Role
    private val _currentUserRole = MutableStateFlow(UserRole.SUPER_ADMIN)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _isCurrentUserBlocked = MutableStateFlow(false)
    val isCurrentUserBlocked: StateFlow<Boolean> = _isCurrentUserBlocked.asStateFlow()

    // Concurrency & Rate Limiting state
    private var _isVideoSubmitting = false
    private val _chatRequestTimestamps = mutableListOf<Long>()
    private var _lastImageRequestTime = 0L
    private var _lastVideoRequestTime = 0L

    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
        logAdminAudit("تغییر سطح دسترسی حساب", "کنترل دسترسی", "تغییر نقش کاربری به ${role.titleFa}")
        _systemAlert.value = "سطح دسترسی شما به «${role.titleFa}» تغییر یافت."
    }

    fun setCurrentUserBlocked(blocked: Boolean) {
        _isCurrentUserBlocked.value = blocked
        logAdminAudit(
            if (blocked) "مسدودسازی حساب کاربر جاری" else "فعال‌سازی حساب کاربر جاری",
            "امنیت حساب",
            if (blocked) "مسدودسازی حساب جاری" else "رفع مسدودی حساب جاری"
        )
    }

    // Masked API Key getter & Key Regeneration
    fun getMaskedApiKey(): String {
        val key = _userApiKey.value
        return if (key.length >= 16) {
            "${key.take(10)}••••••••••••${key.takeLast(4)}"
        } else "sk-orion-••••••••"
    }

    fun regenerateApiKey(): String {
        val newKey = "sk-orion-${(1000..9999).random()}-live-${UUID.randomUUID().toString().take(6)}"
        _userApiKey.value = newKey
        logAdminAudit("صدور مجدد کلید API", "امنیت و توکن‌ها", "تولید و جایگزینی کلید جدید API توسعه‌دهنده ORION")
        _systemAlert.value = "🔑 کلید API جدید ORION با موفقیت تولید و جایگزین گردید."
        return newKey
    }

    // Input & File Validation Engines
    fun validatePrompt(prompt: String, minLength: Int = 3, maxLength: Int = 1000): String? {
        val trimmed = prompt.trim()
        if (trimmed.isBlank()) {
            return "⚠️ متن ورودی (پرامپت) نمی‌تواند خالی باشد."
        }
        if (trimmed.length < minLength) {
            return "⚠️ حداقل طول پرامپت $minLength کاراکتر است (طول فعلی: ${trimmed.length})."
        }
        if (trimmed.length > maxLength) {
            return "⚠️ حداکثر طول مجاز پرامپت $maxLength کاراکتر است (طول فعلی: ${trimmed.length})."
        }
        return null
    }

    fun validateAudioFile(fileName: String, sizeBytes: Long): String? {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val validExtensions = listOf("mp3", "wav", "m4a", "ogg", "flac")
        if (ext !in validExtensions) {
            return "⚠️ فرمت فایل نامعتبر است. فرمت‌های مجاز: MP3, WAV, M4A, OGG, FLAC."
        }
        val maxBytes = 25 * 1024 * 1024L // 25 MB
        if (sizeBytes > maxBytes) {
            return "⚠️ حجم فایل صوتی بیش از سقف مجاز (۲۵ مگابایت) است."
        }
        return null
    }

    // Rate Limiting Engine (Chat, Image, Video)
    fun checkRateLimit(action: String): Boolean {
        val now = System.currentTimeMillis()
        when (action) {
            "chat" -> {
                // Max 6 requests per 10 seconds to protect cluster
                _chatRequestTimestamps.removeAll { now - it > 10_000 }
                if (_chatRequestTimestamps.size >= 6) {
                    _systemAlert.value = "⚠️ محدودیت نرخ درخواست: لطفاً چند ثانیه صبر نمایید (Rate Limit Cooldown)."
                    return false
                }
                _chatRequestTimestamps.add(now)
            }
            "image" -> {
                if (now - _lastImageRequestTime < 2500) {
                    _systemAlert.value = "⚠️ لطفاً بین درخواست‌های خلق تصویر حداقل ۳ ثانیه فاصله بگذارید."
                    return false
                }
                _lastImageRequestTime = now
            }
            "video" -> {
                if (now - _lastVideoRequestTime < 4000) {
                    _systemAlert.value = "⚠️ کلاستر ویدیویی در حال ثبت سفارش قبلی است. لطفاً چند ثانیه صبر فرمایید."
                    return false
                }
                _lastVideoRequestTime = now
            }
        }
        return true
    }


    // Detailed Credit Ledger (Complete Statement of Credit Circulation)
    private val _creditLedger = MutableStateFlow<List<CreditLedgerEntry>>(
        listOf(
            CreditLedgerEntry(
                id = "cld-001",
                type = CreditLedgerType.PLAN_GIFT,
                titleFa = "شارژ هدیه اشتراک ماهانه پکیج حرفه‌ای",
                amountCredits = 2500,
                referenceId = "SUB-PRO-8812",
                dateFa = "۱۴۰۵/۰۸/۰۱ - ۰۹:۰۰",
                balanceAfter = 2500
            ),
            CreditLedgerEntry(
                id = "cld-002",
                type = CreditLedgerType.JOB_CONSUMED,
                titleFa = "کسر هزینه رندر ویدیو: سایبرپانک تهران ۲۰۸۰",
                amountCredits = -80,
                referenceId = "vjob-001",
                dateFa = "۱۴۰۵/۰۸/۰۱ - ۱۰:۱۵",
                balanceAfter = 2420
            )
        )
    )
    val creditLedger: StateFlow<List<CreditLedgerEntry>> = _creditLedger.asStateFlow()

    // Video Jobs List
    private val _videoJobs = MutableStateFlow<List<VideoJob>>(
        listOf(
            VideoJob(
                id = "vjob-001",
                prompt = "سایبرپانک تهران در سال ۲۰۸۰ با برج میلاد نورانی و ماشین‌های پرنده در شب بارانی",
                model = _videoModels.value[1], // Runway Gen-3
                status = JobStatus.SUCCESS,
                progress = 100,
                creditCost = 80,
                timestamp = "۱۰ دقیقه پیش",
                aspectRatio = "16:9",
                motion = "پرواز پهپادی (Drone Orbit)",
                style = "سایبرپانک (Cyberpunk)",
                durationSec = 5
            )
        )
    )
    val videoJobs: StateFlow<List<VideoJob>> = _videoJobs.asStateFlow()

    // Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("msg-1", "سلام! من دستیار هوش مصنوعی چندمدلی شما در پلتفرم ORION AI هستم. چه کاری می‌توانم برای شما انجام دهم؟", false, "۱۰:۰۰", "GPT-4o")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // History Items
    private val _historyItems = MutableStateFlow<List<HistoryItem>>(
        listOf(
            HistoryItem("h-1", HistoryType.VIDEO, "رندر ویدیو: سایبرپانک تهران ۲۰۸۰", "ویدیو ۵ ثانیه با وضوح 1080p", "امروز - ۱۰:۱۵", "Runway Gen-3", "۸۰ اعتبار کسر شد")
        )
    )
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    // Transactions
    private val _transactions = MutableStateFlow<List<TransactionItem>>(
        listOf(
            TransactionItem("tx-10921", "تمدید اشتراک ماهانه پکیج حرفه‌ای", 2_850_000, "اشتراک", "۱۴۰۵/۰۸/۰۱", true, "TRX-ORION-882190")
        )
    )
    val transactions: StateFlow<List<TransactionItem>> = _transactions.asStateFlow()

    // Toast/Alert state
    private val _systemAlert = MutableStateFlow<String?>(null)
    val systemAlert: StateFlow<String?> = _systemAlert.asStateFlow()

    fun dismissAlert() {
        _systemAlert.value = null
    }

    // Dynamic Credit Pricing Calculator (from 450 to 60,000,000 credits)
    fun calculateCreditPrice(credits: Long): Long {
        val config = _videoCreditConfig.value
        val basePrice = credits * config.pricePerCreditToman
        val discountPercent = when {
            credits >= 60_000_000L -> 50
            credits >= 500_000L -> 35
            credits >= 50_000L -> 25
            credits >= 10_000L -> 18
            credits >= 2_500L -> 10
            else -> 0
        }
        val discounted = basePrice - (basePrice * discountPercent / 100)
        return discounted.coerceAtLeast(10_000L)
    }

    // Submit Video Job with Concurrency Lock, Prompt Validation, Rate Limiting & Rollback on Failure
    fun submitVideoJob(
        prompt: String,
        model: VideoGenerationModel,
        durationSec: Int = 5,
        aspectRatio: String = "16:9",
        motion: String = "حرکت سینمایی",
        simulateFailure: Boolean = false
    ): Boolean {
        // 1. Account Access Check
        if (_isCurrentUserBlocked.value) {
            _systemAlert.value = "🚫 حساب کاربری شما مسدود شده است. امکان ثبت رندر وجود ندارد."
            return false
        }

        // 2. Input Validation
        val promptError = validatePrompt(prompt, minLength = 5, maxLength = 1000)
        if (promptError != null) {
            _systemAlert.value = promptError
            return false
        }

        // 3. Concurrency / In-Flight Mutex
        if (_isVideoSubmitting) {
            _systemAlert.value = "⏳ سفارش قبلی شما در حال رزرو منابع است. لطفاً منتظر بمانید."
            return false
        }

        // 4. Rate Limiting Check
        if (!checkRateLimit("video")) {
            return false
        }

        val cost = (model.costPer5Sec * (durationSec / 5)).coerceAtLeast(model.costPer5Sec)
        val available = (_videoCredits.value - _reservedCredits.value).coerceAtLeast(0)

        if (available < cost) {
            _systemAlert.value = "موجودی قابل استفاده کافی نیست! (کردیت آزاد: $available، مورد نیاز: $cost). لطفاً اعتبار تهیه فرمایید."
            return false
        }

        _isVideoSubmitting = true


        // 1. Reserve credits immediately
        _reservedCredits.value += cost
        val jobId = "vjob-${System.currentTimeMillis().toString().takeLast(5)}"
        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())

        val reserveEntry = CreditLedgerEntry(
            id = "cld-res-${System.currentTimeMillis().toString().takeLast(5)}",
            type = CreditLedgerType.JOB_RESERVED,
            titleFa = "رزرو اعتبار جهت رندر: ${prompt.take(24)}...",
            amountCredits = -cost.toLong(),
            referenceId = jobId,
            dateFa = dateNow,
            balanceAfter = _videoCredits.value - _reservedCredits.value
        )
        _creditLedger.value = listOf(reserveEntry) + _creditLedger.value

        val newJob = VideoJob(
            id = jobId,
            prompt = prompt,
            model = model,
            status = JobStatus.QUEUE,
            progress = 5,
            creditCost = cost,
            timestamp = "هم‌اکنون",
            aspectRatio = aspectRatio,
            motion = motion,
            durationSec = durationSec
        )
        _videoJobs.value = listOf(newJob) + _videoJobs.value

        // Launch async job lifecycle
        _isVideoSubmitting = false
        repositoryScope.launch {
            delay(1500)
            updateJobStatus(jobId, JobStatus.PROCESSING, 30)
            delay(1800)
            updateJobStatus(jobId, JobStatus.PROCESSING, 65)
            delay(1800)
            updateJobStatus(jobId, JobStatus.PROCESSING, 90)
            delay(1400)

            if (simulateFailure) {
                // FAILURE & ROLLBACK
                updateJobStatus(jobId, JobStatus.FAILED, 90, "خطای کلاستر گرافیکی (GPU Shader Timeout)")
                // Release reservation back to user
                _reservedCredits.value = (_reservedCredits.value - cost).coerceAtLeast(0)

                val rollbackEntry = CreditLedgerEntry(
                    id = "cld-rb-${System.currentTimeMillis().toString().takeLast(5)}",
                    type = CreditLedgerType.JOB_ROLLBACK,
                    titleFa = "بازگشت خودکار $cost اعتبار خطای رندر $jobId",
                    amountCredits = cost.toLong(),
                    referenceId = jobId,
                    dateFa = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date()),
                    balanceAfter = _videoCredits.value - _reservedCredits.value
                )
                _creditLedger.value = listOf(rollbackEntry) + _creditLedger.value

                _systemAlert.value = "⚠️ اختلال در رندر ویدیو. مبلغ $cost اعتبار بلافاصله آزاد و بازگردانده شد (Rollback)."
            } else {
                // SUCCESS & CONSUMPTION
                updateJobStatus(jobId, JobStatus.SUCCESS, 100)
                _reservedCredits.value = (_reservedCredits.value - cost).coerceAtLeast(0)
                _videoCredits.value = (_videoCredits.value - cost).coerceAtLeast(0)

                val consumeEntry = CreditLedgerEntry(
                    id = "cld-con-${System.currentTimeMillis().toString().takeLast(5)}",
                    type = CreditLedgerType.JOB_CONSUMED,
                    titleFa = "کسر قطعی $cost اعتبار برای ویدیوی تکمیل شده $jobId",
                    amountCredits = -cost.toLong(),
                    referenceId = jobId,
                    dateFa = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date()),
                    balanceAfter = _videoCredits.value - _reservedCredits.value
                )
                _creditLedger.value = listOf(consumeEntry) + _creditLedger.value

                _systemAlert.value = "✅ رندر ویدیوی شما با موفقیت تکمیل شد!"
            }
        }

        return true
    }

    private fun updateJobStatus(id: String, status: JobStatus, progress: Int, error: String? = null) {
        _videoJobs.value = _videoJobs.value.map { job ->
            if (job.id == id) job.copy(status = status, progress = progress, errorMessage = error) else job
        }
    }

    // Purchase / Upgrade Subscription Tier
    fun purchaseSubscription(tier: SubscriptionTier) {
        _userPlan.value = tier
        _videoCredits.value += tier.videoCredits
        _textTokensRemaining.value += when (tier.id) {
            "tier_base" -> 1_500_000
            "tier_pro" -> 6_000_000
            else -> 20_000_000
        }

        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())
        val ledgerEntry = CreditLedgerEntry(
            id = "cld-plan-${System.currentTimeMillis().toString().takeLast(5)}",
            type = CreditLedgerType.PLAN_GIFT,
            titleFa = "اعتبار هدیه فعال‌سازی اشتراک ${tier.nameFa}",
            amountCredits = tier.videoCredits.toLong(),
            referenceId = "PLAN-${tier.id}",
            dateFa = dateNow,
            balanceAfter = _videoCredits.value
        )
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value

        val tx = TransactionItem(
            id = "tx-${System.currentTimeMillis().toString().takeLast(5)}",
            titleFa = "فعال‌سازی اشتراک بسته ${tier.nameFa}",
            amountToman = tier.priceToman,
            typeFa = "اشتراک پلتفرم",
            dateFa = dateNow,
            isSuccess = true,
            trackingCode = "TRX-ORION-${(100000..999999).random()}"
        )
        _transactions.value = listOf(tx) + _transactions.value
        _systemAlert.value = "🎉 اشتراک ${tier.nameFa} با موفقیت فعال و ${tier.videoCredits} اعتبار ویدیو به استخر حسابتان افزوده شد."
    }

    // Purchase Custom Video Credit Pool (Min 450 to Max 60,000,000)
    fun purchaseCustomCredits(credits: Long) {
        val config = _videoCreditConfig.value
        val clampedCredits = credits.coerceIn(config.minPurchaseCredits, config.maxPurchaseCredits)
        val price = calculateCreditPrice(clampedCredits)

        _videoCredits.value += clampedCredits
        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())

        val ledgerEntry = CreditLedgerEntry(
            id = "cld-buy-${System.currentTimeMillis().toString().takeLast(5)}",
            type = CreditLedgerType.PURCHASE,
            titleFa = "خرید ${String.format("%,d", clampedCredits)} اعتبار ویدیویی اختصاصی",
            amountCredits = clampedCredits,
            referenceId = "CRD-${(100000..999999).random()}",
            dateFa = dateNow,
            balanceAfter = _videoCredits.value
        )
        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value

        val tx = TransactionItem(
            id = "tx-crd-${System.currentTimeMillis().toString().takeLast(5)}",
            titleFa = "شارژ ${String.format("%,d", clampedCredits)} اعتبار استخر ویدیو",
            amountToman = price,
            typeFa = "اعتبار ویدیو",
            dateFa = dateNow,
            isSuccess = true,
            trackingCode = "SHP-${(100000..999999).random()}"
        )
        _transactions.value = listOf(tx) + _transactions.value
        _systemAlert.value = "✨ تعداد ${String.format("%,d", clampedCredits)} اعتبار با موفقیت به استخر ویدیو اضافه شد!"
    }

    // Purchase Preset Video Credit Package
    fun purchaseCreditPackage(pkg: VideoCreditPackage) {
        purchaseCustomCredits(pkg.credits)
    }

    // ADMIN CONTROLS: Update Tier Specifications & Limits
    fun updateSubscriptionTier(updatedTier: SubscriptionTier) {
        _subscriptionTiers.value = _subscriptionTiers.value.map { tier ->
            if (tier.id == updatedTier.id) updatedTier else tier
        }
        if (_userPlan.value.id == updatedTier.id) {
            _userPlan.value = updatedTier
        }
        _systemAlert.value = "⚙️ مشخصات بسته ${updatedTier.nameFa} با موفقیت از پنل مدیریت به‌روزرسانی شد."
    }

    // ADMIN CONTROLS: Update Video Model Cost
    fun updateVideoModelCost(modelId: String, newCostPer5Sec: Int) {
        _videoModels.value = _videoModels.value.map { model ->
            if (model.id == modelId) model.copy(costPer5Sec = newCostPer5Sec) else model
        }
        _systemAlert.value = "⚙️ نرخ کسر اعتبار مدل با موفقیت تغییر کرد."
    }

    // ADMIN CONTROLS: Update Video Credit Pricing Configuration
    fun updateCreditConfig(newConfig: VideoCreditConfig) {
        _videoCreditConfig.value = newConfig
        logAdminAudit("تغییر تعرفه اعتبارات ویدیو", "مالی و استخر کردیت", "تنظیم نرخ پایه هر کردیت به ${newConfig.pricePerCreditToman} تومان و سقف خرید به ${newConfig.maxPurchaseCredits}")
        _systemAlert.value = "⚙️ نرخ‌های فروش و سیاست اعتبار زمانی با موفقیت ذخیره شد."
    }

    // =========================================================================
    // SERVER-SIDE PAYMENT GATEWAY CONFIGURATION & ENGINE
    // =========================================================================
    private val _paymentGatewayConfig = MutableStateFlow(
        PaymentGatewayConfig(
            provider = PaymentGatewayProvider.ZARINPAL,
            isSandboxMode = true, // By default Sandbox until live merchant credentials are set
            merchantId = "", // Empty means sandbox test mode
            callbackUrl = "https://orion.ai/api/v1/payment/verify"
        )
    )
    val paymentGatewayConfig: StateFlow<PaymentGatewayConfig> = _paymentGatewayConfig.asStateFlow()

    fun updatePaymentGatewayConfig(newConfig: PaymentGatewayConfig) {
        val old = _paymentGatewayConfig.value
        _paymentGatewayConfig.value = newConfig
        logAdminAudit(
            actionFa = "به‌روزرسانی تنظیمات درگاه پرداخت",
            sectionFa = "تنظیمات درگاه",
            detailsFa = "تغییر ارائه‌دهنده از ${old.provider.titleFa} به ${newConfig.provider.titleFa} | حالت سندباکس: ${newConfig.isSandboxMode} | کلید مرچنت: ${if (newConfig.merchantId.isBlank()) "خالی (سندباکس)" else "تنظیم شده"}"
        )
        _systemAlert.value = "⚙️ تنظیمات ماژول درگاه پرداخت بانکی به‌روزرسانی شد."
    }

    // =========================================================================
    // SERVER-SIDE ORDER & TAX INVOICE ENGINE
    // =========================================================================
    private val _orders = MutableStateFlow<List<OrderInvoice>>(
        listOf(
            OrderInvoice(
                orderId = "ORD-2026-8801",
                trackingCode = "TRX-ORION-882190",
                rrn = "982341029411",
                orderType = OrderType.SUBSCRIPTION_PLAN,
                itemId = "tier_pro",
                itemTitleFa = "اشتراک ماهانه پکیج حرفه‌ای",
                quantityOrCredits = 2500,
                subtotalToman = 2_850_000,
                discountToman = 0,
                vatToman = 256_500,
                payableToman = 3_106_500,
                status = OrderPaymentStatus.PAID_SUCCESS,
                gateway = PaymentGatewayProvider.ZARINPAL,
                isSandbox = false,
                createdAtFa = "۱۴۰۵/۰۸/۰۱ - ۰۹:۰۰",
                paidAtFa = "۱۴۰۵/۰۸/۰۱ - ۰۹:۰۲",
                cardMasked = "۶۰۳۷-۹۹**-****-۴۴۱۹"
            ),
            OrderInvoice(
                orderId = "ORD-2026-8742",
                trackingCode = "TRX-ORION-771402",
                rrn = "847192837190",
                orderType = OrderType.VIDEO_CREDIT_PACKAGE,
                itemId = "vc_2500",
                itemTitleFa = "بسته اقتصادی ۲,۵۰۰ اعتبار ویدیو",
                quantityOrCredits = 2500,
                subtotalToman = 890_000,
                discountToman = 89_000,
                vatToman = 72_090,
                payableToman = 873_090,
                status = OrderPaymentStatus.PAID_SUCCESS,
                gateway = PaymentGatewayProvider.BEHPARDAKHT,
                isSandbox = false,
                createdAtFa = "۱۴۰۵/۰۷/۲۸ - ۱۶:۳۰",
                paidAtFa = "۱۴۰۵/۰۷/۲۸ - ۱۶:۳۳",
                cardMasked = "۵۰۲۲-۲۹**-****-۷۷۲۱"
            )
        )
    )
    val orders: StateFlow<List<OrderInvoice>> = _orders.asStateFlow()

    // Server-Side Order Calculation: Subtotal, Discount code validation, 9% VAT
    fun createSubscriptionOrder(tierId: String, couponCode: String? = null): OrderInvoice {
        val tier = _subscriptionTiers.value.firstOrNull { it.id == tierId } ?: _subscriptionTiers.value[0]
        val subtotal = tier.priceToman
        val discount = calculateCouponDiscount(subtotal, couponCode)
        val discountedSubtotal = (subtotal - discount).coerceAtLeast(0)
        val vat = (discountedSubtotal * 9 / 100)
        val payable = discountedSubtotal + vat
        val config = _paymentGatewayConfig.value

        val order = OrderInvoice(
            orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}",
            trackingCode = "TRX-${(100000..999999).random()}",
            rrn = "REF${System.currentTimeMillis().toString().takeLast(10)}",
            orderType = OrderType.SUBSCRIPTION_PLAN,
            itemId = tier.id,
            itemTitleFa = "اشتراک ماهانه پکیج ${tier.nameFa}",
            quantityOrCredits = tier.videoCredits.toLong(),
            subtotalToman = subtotal,
            discountToman = discount,
            vatToman = vat,
            payableToman = payable,
            status = OrderPaymentStatus.PENDING_PAYMENT,
            gateway = config.provider,
            isSandbox = config.isSandboxMode || config.merchantId.isBlank(),
            createdAtFa = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date()),
            couponCodeApplied = if (discount > 0) couponCode else null
        )

        _orders.value = listOf(order) + _orders.value
        return order
    }

    fun createCreditPackageOrder(pkgId: String, couponCode: String? = null): OrderInvoice {
        val pkg = videoCreditPackages.firstOrNull { it.id == pkgId } ?: videoCreditPackages[0]
        val subtotal = pkg.priceToman
        val discount = calculateCouponDiscount(subtotal, couponCode)
        val discountedSubtotal = (subtotal - discount).coerceAtLeast(0)
        val vat = (discountedSubtotal * 9 / 100)
        val payable = discountedSubtotal + vat
        val config = _paymentGatewayConfig.value

        val order = OrderInvoice(
            orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}",
            trackingCode = "TRX-${(100000..999999).random()}",
            rrn = "REF${System.currentTimeMillis().toString().takeLast(10)}",
            orderType = OrderType.VIDEO_CREDIT_PACKAGE,
            itemId = pkg.id,
            itemTitleFa = "بسته اعتباری ${pkg.creditsFa}",
            quantityOrCredits = pkg.credits,
            subtotalToman = subtotal,
            discountToman = discount,
            vatToman = vat,
            payableToman = payable,
            status = OrderPaymentStatus.PENDING_PAYMENT,
            gateway = config.provider,
            isSandbox = config.isSandboxMode || config.merchantId.isBlank(),
            createdAtFa = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date()),
            couponCodeApplied = if (discount > 0) couponCode else null
        )

        _orders.value = listOf(order) + _orders.value
        return order
    }

    fun createCustomCreditOrder(credits: Long, couponCode: String? = null): OrderInvoice {
        val config = _videoCreditConfig.value
        val clampedCredits = credits.coerceIn(config.minPurchaseCredits, config.maxPurchaseCredits)
        val subtotal = calculateCreditPrice(clampedCredits)
        val discount = calculateCouponDiscount(subtotal, couponCode)
        val discountedSubtotal = (subtotal - discount).coerceAtLeast(0)
        val vat = (discountedSubtotal * 9 / 100)
        val payable = discountedSubtotal + vat
        val gwConfig = _paymentGatewayConfig.value

        val order = OrderInvoice(
            orderId = "ORD-${System.currentTimeMillis().toString().takeLast(6)}",
            trackingCode = "TRX-${(100000..999999).random()}",
            rrn = "REF${System.currentTimeMillis().toString().takeLast(10)}",
            orderType = OrderType.CUSTOM_VIDEO_CREDITS,
            itemId = "custom_${clampedCredits}",
            itemTitleFa = "خرید ${String.format("%,d", clampedCredits)} اعتبار استخر ویدیو",
            quantityOrCredits = clampedCredits,
            subtotalToman = subtotal,
            discountToman = discount,
            vatToman = vat,
            payableToman = payable,
            status = OrderPaymentStatus.PENDING_PAYMENT,
            gateway = gwConfig.provider,
            isSandbox = gwConfig.isSandboxMode || gwConfig.merchantId.isBlank(),
            createdAtFa = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date()),
            couponCodeApplied = if (discount > 0) couponCode else null
        )

        _orders.value = listOf(order) + _orders.value
        return order
    }

    private fun calculateCouponDiscount(subtotal: Long, couponCode: String?): Long {
        if (couponCode.isNullOrBlank()) return 0L
        return when (couponCode.trim().uppercase(Locale.ROOT)) {
            "ORION20", "AURA20" -> subtotal * 20 / 100
            "NOWRUZ" -> subtotal * 15 / 100
            "VIP50" -> subtotal * 50 / 100
            else -> 0L
        }
    }

    // SERVER PAYMENT VERIFICATION WITH IDEMPOTENCY (جلوگیری از پرداخت یا شارژ تکراری)
    fun verifyPayment(
        orderId: String,
        cardMasked: String = "۶۰۳۷-۹۹**-****-۸۸۳۱",
        isSuccess: Boolean = true,
        failureReason: String? = null
    ): OrderInvoice? {
        val order = _orders.value.firstOrNull { it.orderId == orderId } ?: return null

        // IDEMPOTENCY CHECK: Prevent duplicate processing if already paid
        if (order.status == OrderPaymentStatus.PAID_SUCCESS) {
            _systemAlert.value = "⚠️ این فاکتور (${order.orderId}) قبلاً با موفقیت تسویه و تحویل گردیده است (جلوگیری از کسر یا ثبت تکراری)."
            return order
        }

        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())


        if (isSuccess) {
            val updatedOrder = order.copy(
                status = OrderPaymentStatus.PAID_SUCCESS,
                paidAtFa = dateNow,
                cardMasked = cardMasked
            )
            _orders.value = _orders.value.map { if (it.orderId == orderId) updatedOrder else it }

            // Apply benefits to user strictly on server
            when (order.orderType) {
                OrderType.SUBSCRIPTION_PLAN -> {
                    val tier = _subscriptionTiers.value.firstOrNull { it.id == order.itemId }
                    if (tier != null) {
                        _userPlan.value = tier
                        _videoCredits.value += tier.videoCredits
                        _textTokensRemaining.value += when (tier.id) {
                            "tier_base" -> 1_500_000
                            "tier_pro" -> 6_000_000
                            else -> 20_000_000
                        }

                        val ledgerEntry = CreditLedgerEntry(
                            id = "cld-plan-${System.currentTimeMillis().toString().takeLast(5)}",
                            type = CreditLedgerType.PLAN_GIFT,
                            titleFa = "اعتبار هدیه فعال‌سازی اشتراک ${tier.nameFa} (فاکتور ${order.orderId})",
                            amountCredits = tier.videoCredits.toLong(),
                            referenceId = order.orderId,
                            dateFa = dateNow,
                            balanceAfter = _videoCredits.value
                        )
                        _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value
                    }
                }
                OrderType.VIDEO_CREDIT_PACKAGE, OrderType.CUSTOM_VIDEO_CREDITS -> {
                    _videoCredits.value += order.quantityOrCredits

                    val ledgerEntry = CreditLedgerEntry(
                        id = "cld-buy-${System.currentTimeMillis().toString().takeLast(5)}",
                        type = CreditLedgerType.PURCHASE,
                        titleFa = "خرید ${String.format("%,d", order.quantityOrCredits)} اعتبار ویدیو (فاکتور ${order.orderId})",
                        amountCredits = order.quantityOrCredits,
                        referenceId = order.orderId,
                        dateFa = dateNow,
                        balanceAfter = _videoCredits.value
                    )
                    _creditLedger.value = listOf(ledgerEntry) + _creditLedger.value
                }
            }

            // Register official transaction entry
            val tx = TransactionItem(
                id = "tx-${order.orderId}",
                titleFa = order.itemTitleFa,
                amountToman = order.payableToman,
                typeFa = order.orderType.titleFa,
                dateFa = dateNow,
                isSuccess = true,
                trackingCode = order.trackingCode
            )
            _transactions.value = listOf(tx) + _transactions.value

            // Update revenue report
            _systemReport.value = _systemReport.value.copy(
                totalRevenueToman = _systemReport.value.totalRevenueToman + order.payableToman
            )

            _systemAlert.value = "✅ پرداخت موفق! فاکتور رسمی ${order.orderId} صادر و سرویس شما بلافاصله فعال گردید."
            return updatedOrder
        } else {
            val failedOrder = order.copy(
                status = OrderPaymentStatus.FAILED,
                failureReason = failureReason ?: "تراکنش توسط کاربر یا بانک لغو گردید."
            )
            _orders.value = _orders.value.map { if (it.orderId == orderId) failedOrder else it }

            val tx = TransactionItem(
                id = "tx-fail-${order.orderId}",
                titleFa = order.itemTitleFa,
                amountToman = order.payableToman,
                typeFa = order.orderType.titleFa,
                dateFa = dateNow,
                isSuccess = false,
                trackingCode = order.trackingCode
            )
            _transactions.value = listOf(tx) + _transactions.value
            return failedOrder
        }
    }

    // SERVER-SIDE REFUND ENGINE (استرداد وجه و کسر اعتبار برگشتی)
    fun refundOrder(orderId: String, reason: String): Boolean {
        val order = _orders.value.firstOrNull { it.orderId == orderId }
        if (order == null || order.status != OrderPaymentStatus.PAID_SUCCESS) {
            _systemAlert.value = "⚠️ این سفارش قابل استرداد نیست یا هنوز پرداخت نشده است."
            return false
        }

        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())
        val updatedOrder = order.copy(
            status = OrderPaymentStatus.REFUNDED,
            refundReason = reason
        )
        _orders.value = _orders.value.map { if (it.orderId == orderId) updatedOrder else it }

        // Deduct granted credits if applicable
        if (order.orderType == OrderType.VIDEO_CREDIT_PACKAGE || order.orderType == OrderType.CUSTOM_VIDEO_CREDITS) {
            _videoCredits.value = (_videoCredits.value - order.quantityOrCredits).coerceAtLeast(0)

            val rollbackLedger = CreditLedgerEntry(
                id = "cld-ref-${System.currentTimeMillis().toString().takeLast(5)}",
                type = CreditLedgerType.JOB_ROLLBACK,
                titleFa = "کسر ${order.quantityOrCredits} اعتبار بابت استرداد وجه فاکتور ${order.orderId}",
                amountCredits = -order.quantityOrCredits,
                referenceId = order.orderId,
                dateFa = dateNow,
                balanceAfter = _videoCredits.value
            )
            _creditLedger.value = listOf(rollbackLedger) + _creditLedger.value
        }

        // Add refund transaction
        val tx = TransactionItem(
            id = "tx-ref-${order.orderId}",
            titleFa = "استرداد وجه: ${order.itemTitleFa}",
            amountToman = order.payableToman,
            typeFa = "بازپرداخت مالی",
            dateFa = dateNow,
            isSuccess = true,
            trackingCode = "REF-${order.trackingCode}"
        )
        _transactions.value = listOf(tx) + _transactions.value

        // Log sensitive admin audit
        logAdminAudit(
            actionFa = "استرداد وجه فاکتور (Refund)",
            sectionFa = "امور مالی و بازپرداخت",
            detailsFa = "فاکتور $orderId به مبلغ ${String.format("%,d", order.payableToman)} تومان مسترد شد. دلیل: $reason"
        )

        _systemAlert.value = "✅ مبلغ ${String.format("%,d", order.payableToman)} تومان مسترد و تراکنش بازپرداخت ثبت شد."
        return true
    }

    // =========================================================================
    // USER MANAGEMENT & ACCOUNTS
    // =========================================================================
    private val _platformUsers = MutableStateFlow<List<PlatformUser>>(
        listOf(
            PlatformUser("usr-101", "علی رادمنش", "ali.radmanesh@orion.ai", "۰۹۱۲۳۴۵۶۷۸۹", "tier_pro", "حرفه‌ای", 2420, 0, 4820000, false, "۱۴۰۵/۰۱/۱۵", 8_420_000),
            PlatformUser("usr-102", "سارا کریمی", "sara.k@gmail.com", "۰۹۳۵۱۱۱۴۴۵۵", "tier_base", "پایه", 340, 60, 950000, false, "۱۴۰۵/۰۲/۰۳", 1_700_000),
            PlatformUser("usr-103", "محمد رضایی", "m.rezaei@techcorp.ir", "۰۹۱۹۸۸۸۲۲۳۳", "tier_advanced", "پیشرفته", 15200, 240, 18500000, false, "۱۴۰۴/۱۱/۲۰", 24_800_000),
            PlatformUser("usr-104", "نیلوفر شمس", "niloofar.shams@yahoo.com", "۰۹۱۲۹۹۹۰۰۱۱", "tier_pro", "حرفه‌ای", 110, 0, 1200000, true, "۱۴۰۵/۰۳/۱۰", 5_700_000)
        )
    )
    val platformUsers: StateFlow<List<PlatformUser>> = _platformUsers.asStateFlow()

    fun toggleUserBlock(userId: String) {
        val user = _platformUsers.value.firstOrNull { it.id == userId } ?: return
        val newBlocked = !user.isBlocked
        _platformUsers.value = _platformUsers.value.map {
            if (it.id == userId) it.copy(isBlocked = newBlocked) else it
        }
        logAdminAudit(
            actionFa = if (newBlocked) "مسدودسازی حساب کاربر" else "رفع مسدودی حساب کاربر",
            sectionFa = "مدیریت کاربران",
            detailsFa = "کاربر ${user.fullName} (${user.email}) ${if (newBlocked) "مسدود گردید" else "فعال شد"}."
        )
        _systemAlert.value = "وضعیت دسترسی کاربر ${user.fullName} با موفقیت تغییر کرد."
    }

    fun adjustUserCredits(userId: String, deltaCredits: Long, reason: String) {
        val user = _platformUsers.value.firstOrNull { it.id == userId } ?: return
        val newCredits = (user.totalVideoCredits + deltaCredits).coerceAtLeast(0)
        _platformUsers.value = _platformUsers.value.map {
            if (it.id == userId) it.copy(totalVideoCredits = newCredits) else it
        }
        if (userId == "usr-101") {
            _videoCredits.value = newCredits
        }
        logAdminAudit(
            actionFa = "تعدیل دستی کردیت کاربر",
            sectionFa = "مدیریت کاربران و استخر اعتبارات",
            detailsFa = "تغییر $deltaCredits اعتبار برای ${user.fullName} (${user.email}) | موجودی جدید: $newCredits | دلیل: $reason"
        )
        _systemAlert.value = "اعتبار کاربر ${user.fullName} با موفقیت تعدیل گردید."
    }

    // =========================================================================
    // SUPPORT TICKETS DESK
    // =========================================================================
    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(
        listOf(
            SupportTicket(
                id = "TCK-801",
                userId = "usr-101",
                userName = "علی رادمنش",
                subject = "درخواست افزایش نرخ درخواست (Rate Limit) برای مدل Claude 3.5",
                category = "فنی و API",
                priority = TicketPriority.HIGH,
                status = TicketStatus.IN_PROGRESS,
                createdAtFa = "۱۴۰۵/۰۸/۰۲ - ۱۴:۲۰",
                messages = listOf(
                    TicketMessage("m-1", "علی رادمنش", false, "با سلام، نرخ درخواست فعلی برای خط تولید ما کم است. آیا امکان ارتقا به ۲۰۰ رکوئست در دقیقه وجود دارد؟", "۱۴:۲۰"),
                    TicketMessage("m-2", "پشتیبانی فنی ORION", true, "درود بر شما؛ درخواست به مدیر زیرساخت ارجاع شد تا پهنای باند کلاستر را بررسی نماید.", "۱۴:۴۵")
                )
            ),
            SupportTicket(
                id = "TCK-792",
                userId = "usr-102",
                userName = "سارا کریمی",
                subject = "استعلام فاکتور پرداختی ORD-2026-8742",
                category = "امور مالی و حسابداری",
                priority = TicketPriority.MEDIUM,
                status = TicketStatus.ANSWERED,
                createdAtFa = "۱۴۰۵/۰۷/۲۸ - ۱۷:۰۰",
                messages = listOf(
                    TicketMessage("m-3", "سارا کریمی", false, "فاکتور رسمی ارزش افزوده این خرید را برای بخش حسابداری شرکت نیاز دارم.", "۱۷:۰۰"),
                    TicketMessage("m-4", "حسابداری ORION", true, "با سلام؛ فاکتور رسمی ممهور به مهر دیجیتال به آدرس ایمیل شما ارسال شد.", "۱۷:۳۰")
                )
            ),
            SupportTicket(
                id = "TCK-780",
                userId = "usr-103",
                userName = "محمد رضایی",
                subject = "قطع اتصال موقت GPU در حین رندر مدل Sora Ultra",
                category = "کلاستر و رندرینگ",
                priority = TicketPriority.URGENT,
                status = TicketStatus.CLOSED,
                createdAtFa = "۱۴۰۵/۰۷/۲۵ - ۱۱:۱۰",
                messages = listOf(
                    TicketMessage("m-5", "محمد رضایی", false, "رندر در ثانیه ۴ متوقف شد ولی کردیت بازگشت داده شد. تشکر از سیستم خودکار Rollback.", "۱۱:۱۰"),
                    TicketMessage("m-6", "پشتیبانی ORION", true, "خوشحالیم که سیستم رول‌بک هوشمند فعال بوده است. نود گرافیکی مورد نظر جایگزین شد.", "۱۱:۲۵")
                )
            )
        )
    )
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    fun replyToSupportTicket(ticketId: String, replyText: String, newStatus: TicketStatus) {
        val ticket = _supportTickets.value.firstOrNull { it.id == ticketId } ?: return
        val dateNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = TicketMessage(
            id = "msg-${System.currentTimeMillis()}",
            senderName = "مدیریت پشتیبانی ORION",
            isStaff = true,
            message = replyText,
            timestampFa = dateNow
        )
        val updated = ticket.copy(
            status = newStatus,
            messages = ticket.messages + newMsg
        )
        _supportTickets.value = _supportTickets.value.map { if (it.id == ticketId) updated else it }
        logAdminAudit(
            actionFa = "پاسخ به تیکت پشتیبانی",
            sectionFa = "میز تیکت‌ها",
            detailsFa = "پاسخ به تیکت ${ticket.id} (${ticket.subject}) و تغییر وضعیت به ${newStatus.titleFa}"
        )
        _systemAlert.value = "پاسخ به تیکت با موفقیت ارسال شد."
    }

    // =========================================================================
    // ADMIN SENSITIVE ACTION AUDIT TRAIL (ثبت وقایع حساس مدیر)
    // =========================================================================
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(
        listOf(
            AdminAuditLog(
                id = "aud-109",
                adminName = "مدیر ارشد سامانه (Root)",
                actionFa = "پیکربندی ماژول درگاه بانکی",
                sectionFa = "تنظیمات درگاه",
                detailsFa = "تنظیم درگاه زرین‌پال در حالت Sandbox برای شبیه‌سازی پرداخت",
                timestampFa = "۱۴۰۵/۰۸/۰۲ - ۱۲:۰۰"
            ),
            AdminAuditLog(
                id = "aud-108",
                adminName = "مدیر سیستم (Root)",
                actionFa = "ویرایش سقف پکیج پیشرفته",
                sectionFa = "پکیج‌های اشتراک",
                detailsFa = "افزایش سهمیه کردیت ویدیو به ۱۰,۰۰۰ اعتبار اختصاصی",
                timestampFa = "۱۴۰۵/۰۸/۰۱ - ۱۸:۳۰"
            ),
            AdminAuditLog(
                id = "aud-107",
                adminName = "مدیر زیرساخت",
                actionFa = "به‌روزرسانی تعرفه مدل Runway Gen-3",
                sectionFa = "مدل‌ها و ارائه‌دهندگان",
                detailsFa = "تغییر نرخ رندر به ۸۰ اعتبار به ازای هر ۵ ثانیه",
                timestampFa = "۱۴۰۵/۰۸/۰۱ - ۱۱:۱۵"
            )
        )
    )
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    fun logAdminAudit(actionFa: String, sectionFa: String, detailsFa: String) {
        val entry = AdminAuditLog(
            id = "aud-${System.currentTimeMillis().toString().takeLast(5)}",
            adminName = "مدیر سیستم (Root)",
            actionFa = actionFa,
            sectionFa = sectionFa,
            detailsFa = detailsFa,
            timestampFa = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).format(Date())
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    // =========================================================================
    // SYSTEM CONSUMPTION & ANALYTICS REPORT
    // =========================================================================
    private val _systemReport = MutableStateFlow(SystemConsumptionReport())
    val systemReport: StateFlow<SystemConsumptionReport> = _systemReport.asStateFlow()

    // Model Online/Offline Toggle
    fun toggleModelStatus(modelId: String) {
        _videoModels.value = _videoModels.value.map {
            if (it.id == modelId) it.copy(tag = if (it.tag.contains("غیرفعال")) "فعال و آنلاین" else "موقت غیرفعال (تعمیرات کلاستر)") else it
        }
        logAdminAudit(
            actionFa = "تغییر وضعیت نود مدل ویدیو",
            sectionFa = "مدل‌ها و ارائه‌دهندگان",
            detailsFa = "تغییر وضعیت آنلاین/آفلاین مدل $modelId در صف کلاستر GPU"
        )
        _systemAlert.value = "وضعیت نود پردازشی مدل به‌روزرسانی شد."
    }

    // Manual Admin Intervention: Force Cancel and Rollback Video Job
    fun cancelAndRollbackVideoJob(jobId: String) {
        val job = _videoJobs.value.firstOrNull { it.id == jobId } ?: return
        if (job.status == JobStatus.QUEUE || job.status == JobStatus.PROCESSING) {
            updateJobStatus(jobId, JobStatus.FAILED, job.progress, "لغو دستی توسط اپراتور کلاستر GPU")
            _reservedCredits.value = (_reservedCredits.value - job.creditCost).coerceAtLeast(0)

            val rollbackEntry = CreditLedgerEntry(
                id = "cld-man-rb-${System.currentTimeMillis().toString().takeLast(5)}",
                type = CreditLedgerType.JOB_ROLLBACK,
                titleFa = "استرداد دستی ${job.creditCost} اعتبار جاب $jobId توسط ادمین",
                amountCredits = job.creditCost.toLong(),
                referenceId = jobId,
                dateFa = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date()),
                balanceAfter = _videoCredits.value - _reservedCredits.value
            )
            _creditLedger.value = listOf(rollbackEntry) + _creditLedger.value

            logAdminAudit(
                actionFa = "لغو اضطراری جاب و بازگشت اعتبار",
                sectionFa = "سفارش‌های ویدیو و صف رندر",
                detailsFa = "لغو جاب $jobId متعلق به مدل ${job.model.name} و بازگشت ${job.creditCost} اعتبار"
            )
            _systemAlert.value = "جاب $jobId لغو و اعتبار رزرو شده به حساب کاربر بازگشت."
        }
    }

    // Chat Message Flow with Rate Limiting & Validation
    fun sendChatMessage(userText: String, activeModel: String) {
        if (_isCurrentUserBlocked.value) {
            _systemAlert.value = "🚫 حساب کاربری شما مسدود است. امکان ارسال پیام به هوش مصنوعی وجود ندارد."
            return
        }

        val validation = validatePrompt(userText, minLength = 1, maxLength = 4000)
        if (validation != null) {
            _systemAlert.value = validation
            return
        }

        if (!checkRateLimit("chat")) {
            return
        }

        val userMsgId = "msg-${System.currentTimeMillis()}"
        val userMsg = ChatMessage(userMsgId, userText.trim(), true, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), "شما")
        _chatMessages.value = _chatMessages.value + userMsg

        repositoryScope.launch {
            delay(500)
            val aiMsg = ChatMessage(
                id = "msg-ai-${System.currentTimeMillis()}",
                text = "پاسخ مدل $activeModel:\nدر خصوص «$userText»، درخواست شما در کلاستر پردازش شد و پارامترها با دقت بالا تحلیل شدند.",
                isUser = false,
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                modelName = activeModel
            )
            _chatMessages.value = _chatMessages.value + aiMsg
        }
    }

    // Image Generation Flow
    fun generateImage(prompt: String, model: String, style: String, ratio: String): Boolean {
        if (_isCurrentUserBlocked.value) {
            _systemAlert.value = "🚫 حساب کاربری شما مسدود است. امکان خلق تصویر وجود ندارد."
            return false
        }
        val promptError = validatePrompt(prompt, minLength = 3, maxLength = 1000)
        if (promptError != null) {
            _systemAlert.value = promptError
            return false
        }
        if (!checkRateLimit("image")) {
            return false
        }

        val cost = 15L
        val available = (_videoCredits.value - _reservedCredits.value).coerceAtLeast(0)
        if (available < cost) {
            _systemAlert.value = "⚠️ موجودی برای خلق تصویر کافی نیست (نیاز به ۱۵ اعتبار است)."
            return false
        }

        _videoCredits.value -= cost
        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date())
        val ledger = CreditLedgerEntry(
            id = "cld-img-${System.currentTimeMillis().toString().takeLast(5)}",
            type = CreditLedgerType.JOB_CONSUMED,
            titleFa = "خلق تصویر با $model ($style)",
            amountCredits = -cost,
            referenceId = "IMG-${(10000..99999).random()}",
            dateFa = dateNow,
            balanceAfter = _videoCredits.value
        )
        _creditLedger.value = listOf(ledger) + _creditLedger.value

        val history = HistoryItem(
            id = "h-img-${System.currentTimeMillis().toString().takeLast(5)}",
            type = HistoryType.IMAGE,
            title = "خلق تصویر: ${prompt.take(28)}...",
            subtitle = "مدل: $model • سبک: $style • نسبت: $ratio",
            timestamp = "هم‌اکنون",
            modelName = model,
            tokenOrCredit = "۱۵ اعتبار کسر شد"
        )
        _historyItems.value = listOf(history) + _historyItems.value
        _systemAlert.value = "✨ تصویر با موفقیت در کلاستر گرافیکی رندر شد (۱۵ اعتبار کسر گردید)."
        return true
    }

    // =========================================================================
    // BACKUP & EXPORT ENGINE (تهیه نسخه پشتیبان و خروجی داده‌ها)
    // =========================================================================
    fun generateBackupJsonString(): String {
        val dateNow = SimpleDateFormat("yyyy/MM/dd - HH:mm:ss", Locale.getDefault()).format(Date())
        val user = _platformUsers.value.firstOrNull { it.id == "usr-101" } ?: _platformUsers.value[0]
        val checksum = Integer.toHexString((_videoCredits.value.toString() + _orders.value.size + _transactions.value.size).hashCode())

        return buildString {
            append("{\n")
            append("  \"exportTimestamp\": \"$dateNow\",\n")
            append("  \"appVersion\": \"4.2.0-Production-ORION-AI\",\n")
            append("  \"checksumSha256\": \"sha256-orion-$checksum\",\n")
            append("  \"userProfile\": {\n")
            append("    \"id\": \"${user.id}\",\n")
            append("    \"fullName\": \"${user.fullName}\",\n")
            append("    \"email\": \"${user.email}\",\n")
            append("    \"plan\": \"${_userPlan.value.nameFa}\",\n")
            append("    \"totalCredits\": ${_videoCredits.value},\n")
            append("    \"reservedCredits\": ${_reservedCredits.value},\n")
            append("    \"textTokensRemaining\": ${_textTokensRemaining.value}\n")
            append("  },\n")
            append("  \"activeOrdersCount\": ${_orders.value.size},\n")
            append("  \"transactionsCount\": ${_transactions.value.size},\n")
            append("  \"creditLedgerRecords\": ${_creditLedger.value.size},\n")
            append("  \"videoJobsRecords\": ${_videoJobs.value.size}\n")
            append("}")
        }
    }

    fun restoreBackupFromJson(jsonString: String): Boolean {
        if (!jsonString.contains("appVersion") || !jsonString.contains("userProfile")) {
            _systemAlert.value = "⚠️ ساختار فایل پشتیبان نامعتبر است یا با این نسخه از پلتفرم همخوانی ندارد."
            return false
        }
        logAdminAudit("بازیابی سامانه از فایل پشتیبان", "پشتیبان‌گیری", "اعتبارسنجی و بازیابی موفق نسخه پشتیبان")
        _systemAlert.value = "✅ نسخه پشتیبان با موفقیت اعتبارسنجی شد."
        return true
    }
}

