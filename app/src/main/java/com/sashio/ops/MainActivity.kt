package com.sashio.ops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.sashio.ops.ui.SashioAppContent
import com.sashio.ops.ui.SashioViewModel
import com.sashio.ops.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
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
