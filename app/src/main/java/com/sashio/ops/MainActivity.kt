package com.sashio.ops

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.sashio.ops.ui.SashioAppContent
import com.sashio.ops.ui.SashioViewModel
import com.sashio.ops.ui.theme.MyApplicationTheme

// FragmentActivity (not plain ComponentActivity) is required to host a real
// androidx.biometric.BiometricPrompt for Touch ID / Face ID.
class MainActivity : FragmentActivity() {
    private val viewModel: SashioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SashioAppContent(viewModel = viewModel)
            }
        }
        handlePaymentDeepLink(intent)
    }

    // launchMode="singleTask" (see AndroidManifest.xml) means the app isn't
    // recreated when Paystack's browser redirect brings the user back -
    // Android delivers the new Uri here instead of going through onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentDeepLink(intent)
    }

    // Paystack redirects the browser to sachioexpress://payment-complete
    // (configured via callback_url in initialize-payment), appending
    // ?reference=...&trxref=... automatically. Without this, the user has
    // to manually switch back to the app and tap "I've Paid" themselves.
    private fun handlePaymentDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "sachioexpress" && uri.host == "payment-complete") {
            val reference = uri.getQueryParameter("reference") ?: uri.getQueryParameter("trxref")
            if (!reference.isNullOrBlank()) {
                viewModel.simWebpayComplete(reference)
            }
        }
    }
}
