package com.sashio.ops

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
    }
}
