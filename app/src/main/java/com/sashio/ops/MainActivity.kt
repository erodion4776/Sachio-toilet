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
        handleDeepLink(intent)
    }

    // launchMode="singleTask" (see AndroidManifest.xml) means the app isn't
    // recreated when a browser redirect brings the user back - Android
    // delivers the new Uri here instead of going through onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "sachioexpress") return
        when (uri.host) {
            "payment-complete" -> handlePaymentDeepLink(uri)
            "reset-password" -> handlePasswordResetDeepLink(uri)
        }
    }

    // Paystack redirects the browser to sachioexpress://payment-complete
    // (configured via callback_url in initialize-payment), appending
    // ?reference=...&trxref=... automatically. Without this, the user has
    // to manually switch back to the app and tap "I've Paid" themselves.
    private fun handlePaymentDeepLink(uri: android.net.Uri) {
        val reference = uri.getQueryParameter("reference") ?: uri.getQueryParameter("trxref")
        if (!reference.isNullOrBlank()) {
            viewModel.simWebpayComplete(reference)
        }
    }

    // Supabase's password recovery email links here (configured via
    // redirectUrl in resetPasswordForEmail). It uses the "implicit" auth
    // flow, which appends the session tokens after a # fragment rather than
    // as normal ?query params (e.g. sachioexpress://reset-password#access_
    // token=...&refresh_token=...&type=recovery) - Uri.getQueryParameter
    // can't see fragment content, so it's parsed manually here.
    private fun handlePasswordResetDeepLink(uri: android.net.Uri) {
        val fragment = uri.fragment ?: uri.encodedQuery ?: return
        val params = fragment.split("&").mapNotNull { part ->
            val idx = part.indexOf("=")
            if (idx <= 0) null else part.substring(0, idx) to java.net.URLDecoder.decode(part.substring(idx + 1), "UTF-8")
        }.toMap()

        val accessToken = params["access_token"]
        val refreshToken = params["refresh_token"]
        val type = params["type"]
        if (type == "recovery" && !accessToken.isNullOrBlank()) {
            viewModel.beginPasswordRecoverySession(accessToken, refreshToken ?: "")
        }
    }
}
