
package com.sashio.ops

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sashio.ops.data.SashioViewModel
import com.sashio.ops.data.SupabaseProvider
import com.sashio.ops.ui.SashioAppContent
import com.sashio.ops.ui.theme.SashioTheme
import io.github.jan.supabase.handleDeeplinks
import org.osmdroid.config.Configuration
import java.net.URLDecoder

class MainActivity : FragmentActivity() {

    private val viewModel: SashioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue =
            applicationContext.packageName

        enableEdgeToEdge()

        setContent {
            val themePreference by
                viewModel.themePreference.collectAsStateWithLifecycle()

            SashioTheme(
                darkTheme = themePreference.equals(
                    "dark",
                    ignoreCase = true
                )
            ) {
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
        val callbackIntent = intent ?: return
        val uri = callbackIntent.data ?: return

        if (uri.scheme != "sachioexpress") return

        when (uri.host) {

            "payment-complete",
            "payment-callback" -> {
                handlePaymentDeepLink(uri)
            }

            "payment-cancel",
            "payment-cancelled" -> {
                handlePaymentCancelled()
            }

            "reset-password" -> {
                if (isPasswordRecoveryLink(uri)) {
                    handlePasswordResetDeepLink(uri)
                } else {
                    handleSupabaseAuthDeepLink(callbackIntent)
                }
            }
        }
    }

    private fun handleSupabaseAuthDeepLink(intent: Intent) {
        try {
            SupabaseProvider.client.handleDeeplinks(intent)
        } catch (e: Exception) {
            android.util.Log.e(
                "SashioAuth",
                "Could not process authentication callback",
                e
            )
        }
    }

    private fun handlePaymentDeepLink(uri: Uri) {
        val reference = uri.getQueryParameter("reference")
            ?: uri.getQueryParameter("trxref")

        if (!reference.isNullOrBlank()) {
            viewModel.simWebpayComplete(reference)
        } else {
            viewModel.handlePaymentCancelled()
        }
    }

    private fun handlePaymentCancelled() {
        viewModel.handlePaymentCancelled()
    }

    private fun isPasswordRecoveryLink(uri: Uri): Boolean {
        val params = getDeepLinkParams(uri)
        return params["type"] == "recovery"
    }

    private fun handlePasswordResetDeepLink(uri: Uri) {
        val params = getDeepLinkParams(uri)

        val accessToken = params["access_token"]
        val refreshToken = params["refresh_token"]

        if (!accessToken.isNullOrBlank()) {
            viewModel.beginPasswordRecoverySession(
                accessToken = accessToken,
                refreshToken = refreshToken ?: ""
            )
        }
    }

    private fun getDeepLinkParams(uri: Uri): Map<String, String> {
        val rawParams = uri.fragment
            ?: uri.encodedQuery
            ?: return emptyMap()

        return try {
            rawParams.split("&").mapNotNull { part ->
                val index = part.indexOf("=")

                if (index <= 0) {
                    null
                } else {
                    val key = URLDecoder.decode(
                        part.substring(0, index),
                        "UTF-8"
                    )

                    val value = URLDecoder.decode(
                        part.substring(index + 1),
                        "UTF-8"
                    )

                    key to value
                }
            }.toMap()
        } catch (e: Exception) {
            android.util.Log.e(
                "SashioAuth",
                "Could not read authentication callback parameters",
                e
            )
            emptyMap()
        }
    }
}
