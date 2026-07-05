package com.kaam.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kaam.app.navigation.KindredNavRoot
import com.kaam.app.update.UpdatePrompt
import com.kindred.core.ui.theme.KindredTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
}
