package com.kindred.core.data

/**
 * Razorpay Key ID is a publishable key, safe to embed client-side (unlike the Key
 * Secret, which must never appear in the app — it's only used for server-side payment
 * verification, which this project doesn't have yet; see the note in PremiumRepository).
 *
 * Until both values are filled in, the Premium screen shows a "coming soon" state
 * instead of a broken checkout button.
 */
object PremiumConfig {
    const val RAZORPAY_KEY_ID = "rzp_test_TAdOe9FOiKzq1Y"
    const val PRICE_PAISE = 9900 // ₹99.00 — Razorpay amounts are in paise
    const val PRICE_LABEL = "₹99"

    val isConfigured: Boolean
        get() = RAZORPAY_KEY_ID != "TODO_RAZORPAY_KEY_ID" && PRICE_PAISE > 0
}
