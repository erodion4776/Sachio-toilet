package com.sashio.ops

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.ui.SashioAppContent
import com.sashio.ops.ui.theme.SashioTheme
import java.net.URLDecoder

// FragmentActivity (not plain ComponentActivity) is required to host a real
// androidx.biometric.BiometricPrompt for Touch ID / Face ID.
class MainActivity : FragmentActivity() {
    private val viewModel: SashioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SashioTheme {
                SashioAppContent(viewModel = viewModel)
            }
        }
        handleDeepLink(intent)
    }

    // launchMode="singleTask" (see AndroidManifest.xml) ensures the activity isn't
    // recreated when a browser redirect returns to the app - Android delivers
    // the new Intent here instead of going through onCreate.
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
    // ?reference=...&trxref=... automatically.
    private fun handlePaymentDeepLink(uri: Uri) {
        val reference = uri.getQueryParameter("reference") ?: uri.getQueryParameter("trxref")
        if (!reference.isNullOrBlank()) {
            viewModel.simWebpayComplete(reference)
        }
    }

    // Supabase's password recovery email links here (configured via
    // redirectUrl in resetPasswordForEmail). It uses the implicit auth
    // flow, appending session tokens after a # fragment.
    private fun handlePasswordResetDeepLink(uri: Uri) {
        val fragment = uri.fragment ?: uri.encodedQuery ?: return
        try {
            val params = fragment.split("&").mapNotNull { part ->
                val idx = part.indexOf("=")
                if (idx <= 0) null else part.substring(0, idx) to URLDecoder.decode(part.substring(idx + 1), "UTF-8")
            }.toMap()

            val accessToken = params["access_token"]
            val refreshToken = params["refresh_token"]
            val type = params["type"]
            if (type == "recovery" && !accessToken.isNullOrBlank()) {
                viewModel.beginPasswordRecoverySession(accessToken, refreshToken ?: "")
            }
        } catch (e: Exception) {
            // Silently ignore malformed deep link fragments
        }
    }
}
