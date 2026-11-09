package com.example

import com.example.data.PlatformRepository
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun verifySubscriptionTiers() {
        val tiers = PlatformRepository.subscriptionTiers.value
        assertEquals("Must have exactly three subscription tiers", 3, tiers.size)

        // Tier 1: Base - 850,000 Toman
        assertEquals("Base tier price must be 850,000 Toman", 850_000L, tiers[0].priceToman)
        assertEquals("پایه", tiers[0].nameFa)

        // Tier 2: Professional - 2,850,000 Toman
        assertEquals("Professional tier price must be 2,850,000 Toman", 2_850_000L, tiers[1].priceToman)
        assertEquals("حرفه‌ای", tiers[1].nameFa)

        // Tier 3: Advanced - 5,350,000 Toman
        assertEquals("Advanced tier price must be 5,350,000 Toman", 5_350_000L, tiers[2].priceToman)
        assertEquals("پیشرفته", tiers[2].nameFa)
    }

    @Test
    fun verifyVideoCreditPoolRange() {
        val packages = PlatformRepository.videoCreditPackages
        val minCredit = packages.minOf { it.credits }
        val maxCredit = packages.maxOf { it.credits }

        assertEquals("Min video credits must start from 450", 450L, minCredit)
        assertEquals("Max video credits pool must reach 60,000,000", 60_000_000L, maxCredit)
    }

    @Test
    fun verifyVideoModelDeductionRules() {
        val models = PlatformRepository.videoModels.value
        val sora = models.first { it.id == "sora_ultra" }
        val runway = models.first { it.id == "runway_gen3" }
        val kling = models.first { it.id == "kling_v15" }
        val luma = models.first { it.id == "luma_dream" }
        val haiper = models.first { it.id == "haiper_2" }
        val pika = models.first { it.id == "pika_2" }

        assertEquals(120, sora.costPer5Sec)
        assertEquals(80, runway.costPer5Sec)
        assertEquals(60, kling.costPer5Sec)
        assertEquals(50, luma.costPer5Sec)
        assertEquals(40, haiper.costPer5Sec)
        assertEquals(35, pika.costPer5Sec)
    }

    @Test
    fun verifyCustomCreditCalculator() {
        val price450 = PlatformRepository.calculateCreditPrice(450)
        assertTrue(price450 > 0)

        val price60M = PlatformRepository.calculateCreditPrice(60_000_000L)
        assertTrue(price60M > 0)
    }

    @Test
    fun verifyServerSideOrderCreationAndVat() {
        val order = PlatformRepository.createSubscriptionOrder("tier_base", couponCode = null)

        assertEquals("tier_base", order.itemId)
        assertEquals(850_000L, order.subtotalToman)
        assertEquals(0L, order.discountToman)
        assertEquals(76_500L, order.vatToman)
        assertEquals(926_500L, order.payableToman)
        assertEquals(OrderPaymentStatus.PENDING_PAYMENT, order.status)
        assertTrue("Tracking code should be generated", order.trackingCode.isNotBlank())
        assertTrue("Shaparak RRN should be generated", order.rrn.isNotBlank())
    }

    @Test
    fun verifyCouponDiscountCalculation() {
        val order = PlatformRepository.createSubscriptionOrder("tier_pro", couponCode = "ORION20")
        val subtotal = 2_850_000L
        val expectedDiscount = subtotal * 20 / 100
        val discountedSubtotal = subtotal - expectedDiscount
        val expectedVat = discountedSubtotal * 9 / 100
        val expectedPayable = discountedSubtotal + expectedVat

        assertEquals(expectedDiscount, order.discountToman)
        assertEquals(expectedVat, order.vatToman)
        assertEquals(expectedPayable, order.payableToman)
        assertEquals("ORION20", order.couponCodeApplied)
    }

    @Test
    fun verifyPaymentVerificationAndReceiptIssuance() {
        val initialCredits = PlatformRepository.videoCredits.value
        val order = PlatformRepository.createCreditPackageOrder("vc_2500")

        val verified = PlatformRepository.verifyPayment(
            orderId = order.orderId,
            cardMasked = "۶۰۳۷-۹۹۱۸-****-۸۸۳۱",
            isSuccess = true
        )

        assertNotNull(verified)
        assertEquals(OrderPaymentStatus.PAID_SUCCESS, verified?.status)
        assertNotNull(verified?.paidAtFa)
        assertEquals("۶۰۳۷-۹۹۱۸-****-۸۸۳۱", verified?.cardMasked)

        assertEquals(initialCredits + 2500, PlatformRepository.videoCredits.value)

        val tx = PlatformRepository.transactions.value.firstOrNull { it.trackingCode == order.trackingCode }
        assertNotNull("Transaction must be logged", tx)
        assertTrue(tx?.isSuccess == true)
    }

    @Test
    fun verifyIdempotentPaymentVerification() {
        // Test idempotency: Calling verifyPayment twice on same order must NOT double credit
        val order = PlatformRepository.createCreditPackageOrder("vc_450")
        val verifiedFirst = PlatformRepository.verifyPayment(order.orderId, isSuccess = true)
        val creditsAfterFirst = PlatformRepository.videoCredits.value

        // Second duplicate callback attempt
        val verifiedSecond = PlatformRepository.verifyPayment(order.orderId, isSuccess = true)
        val creditsAfterSecond = PlatformRepository.videoCredits.value

        assertEquals("Second verification must not grant double credits", creditsAfterFirst, creditsAfterSecond)
        assertEquals(OrderPaymentStatus.PAID_SUCCESS, verifiedSecond?.status)
    }

    @Test
    fun verifyPromptAndFileValidation() {
        // Blank prompt
        assertNotNull("Blank prompt must fail", PlatformRepository.validatePrompt("   ", minLength = 3))
        // Too short prompt
        assertNotNull("Short prompt must fail", PlatformRepository.validatePrompt("ab", minLength = 3))
        // Valid prompt
        assertNull("Valid prompt must pass", PlatformRepository.validatePrompt("این یک پرامپت معتبر است", minLength = 3))

        // Invalid audio file format
        assertNotNull("Executable file must fail", PlatformRepository.validateAudioFile("malware.exe", 1024))
        // Valid mp3
        assertNull("MP3 under 25MB must pass", PlatformRepository.validateAudioFile("voice_turn.mp3", 2 * 1024 * 1024))
        // Audio file exceeding 25MB
        assertNotNull("Audio over 25MB must fail", PlatformRepository.validateAudioFile("long.wav", 30 * 1024 * 1024))
    }

    @Test
    fun verifyBlockedUserRestrictions() {
        // Set user to blocked
        PlatformRepository.setCurrentUserBlocked(true)
        assertTrue(PlatformRepository.isCurrentUserBlocked.value)

        val model = PlatformRepository.videoModels.value[0]
        val jobSubmitted = PlatformRepository.submitVideoJob(
            prompt = "ویدیوی تستی کاربر مسدود",
            model = model
        )
        assertFalse("Blocked user must not be allowed to submit video jobs", jobSubmitted)

        // Restore unblocked status
        PlatformRepository.setCurrentUserBlocked(false)
        assertFalse(PlatformRepository.isCurrentUserBlocked.value)
    }

    @Test
    fun verifyApiKeyRegenerationAndMasking() {
        val initialKey = PlatformRepository.userApiKey.value
        val masked = PlatformRepository.getMaskedApiKey()
        assertTrue("Masked key must hide sensitive middle characters", masked.contains("••••"))

        val newKey = PlatformRepository.regenerateApiKey()
        assertTrue("New key must start with sk-orion-", newKey.startsWith("sk-orion-"))
        assertTrue("New key must be distinct from initial key", newKey != initialKey)
    }

    @Test
    fun verifyBackupJsonGenerationAndIntegrity() {
        val backupJson = PlatformRepository.generateBackupJsonString()
        assertTrue("Backup must contain appVersion", backupJson.contains("appVersion"))
        assertTrue("Backup must contain checksum", backupJson.contains("checksumSha256"))
        assertTrue("Backup must contain userProfile", backupJson.contains("userProfile"))

        val restoreOk = PlatformRepository.restoreBackupFromJson(backupJson)
        assertTrue("Restoring valid backup JSON must succeed", restoreOk)

        val invalidRestore = PlatformRepository.restoreBackupFromJson("{\"corrupt\": true}")
        assertFalse("Restoring corrupt JSON must fail", invalidRestore)
    }

    @Test
    fun verifyImageGenerationDeduction() {
        val initialCredits = PlatformRepository.videoCredits.value
        if (initialCredits >= 15) {
            val success = PlatformRepository.generateImage(
                prompt = "پرتره سایبرپانک تست یونیت",
                model = "FLUX.1 Pro",
                style = "فتورئال",
                ratio = "1:1"
            )
            assertTrue("Image generation should succeed with sufficient balance", success)
            assertEquals(initialCredits - 15, PlatformRepository.videoCredits.value)
        }
    }

    @Test
    fun verifyRoleAccessControl() {
        PlatformRepository.setUserRole(UserRole.REGULAR_USER)
        assertEquals(UserRole.REGULAR_USER, PlatformRepository.currentUserRole.value)

        PlatformRepository.setUserRole(UserRole.SUPER_ADMIN)
        assertEquals(UserRole.SUPER_ADMIN, PlatformRepository.currentUserRole.value)
    }

    @Test
    fun verifyOrderRefundAndCreditRollback() {
        val order = PlatformRepository.createCreditPackageOrder("vc_450")
        PlatformRepository.verifyPayment(order.orderId, isSuccess = true)

        val balanceBeforeRefund = PlatformRepository.videoCredits.value

        val refundSuccess = PlatformRepository.refundOrder(order.orderId, "درخواست انصراف کاربر در مدت ضمانت ۲۴ ساعته")
        assertTrue(refundSuccess)

        val refundedOrder = PlatformRepository.orders.value.first { it.orderId == order.orderId }
        assertEquals(OrderPaymentStatus.REFUNDED, refundedOrder.status)

        assertEquals(balanceBeforeRefund - 450, PlatformRepository.videoCredits.value)

        val audit = PlatformRepository.auditLogs.value.firstOrNull { it.detailsFa.contains(order.orderId) }
        assertNotNull("Admin audit must record sensitive refund action", audit)
    }

    @Test
    fun verifyAdminAuditLogging() {
        val initialLogCount = PlatformRepository.auditLogs.value.size
        PlatformRepository.logAdminAudit(
            actionFa = "تست امنیت لاگ",
            sectionFa = "واحد آزمون",
            detailsFa = "بررسی ثبت واقعه حساس مدیر"
        )
        val newLogCount = PlatformRepository.auditLogs.value.size
        assertEquals(initialLogCount + 1, newLogCount)
    }

    @Test
    fun verifyPaymentGatewaySandboxMode() {
        val config = PlatformRepository.paymentGatewayConfig.value
        assertTrue("Gateway should default to Sandbox until live merchant credentials are set", config.isSandboxMode || config.merchantId.isBlank())
    }
}
