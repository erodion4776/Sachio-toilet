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
import org.osmdroid.config.Configuration
import java.net.URLDecoder

class MainActivity : FragmentActivity() {
    private val viewModel: SashioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = applicationContext.packageName

        enableEdgeToEdge()
        setContent {
            SashioTheme {
                SashioAppContent(viewModel = viewModel)
            }
        }
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "sachioexpress") return
        when (uri.host) {
            "payment-complete", "payment-callback" -> handlePaymentDeepLink(uri)
            "payment-cancel", "payment-cancelled" -> handlePaymentCancelled()
            "reset-password" -> handlePasswordResetDeepLink(uri)
        }
    }

    private fun handlePaymentDeepLink(uri: Uri) {
        val reference = uri.getQueryParameter("reference") ?: uri.getQueryParameter("trxref")
        if (!reference.isNullOrBlank()) {
            viewModel.simWebpayComplete(reference)
        } else {
            viewModel.handlePaymentCancelled()
        }
    }

    private fun handlePaymentCancelled() {
        viewModel.handlePaymentCancelled()
    }

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
            // Silently ignore malformed deep links
        }
    }
}
