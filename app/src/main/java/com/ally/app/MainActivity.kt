package com.ally.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ally.app.navigation.KindredNavRoot
import com.ally.app.update.UpdatePrompt
import com.kindred.core.data.RazorpayResultBus
import com.kindred.core.ui.theme.KindredTheme
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KindredTheme {
                KindredNavRoot()
                UpdatePrompt()
            }
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentId ?: return
        RazorpayResultBus.emit(RazorpayResultBus.Result.Success(paymentId))
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        RazorpayResultBus.emit(RazorpayResultBus.Result.Failure(response ?: "Payment failed"))
    }
}
