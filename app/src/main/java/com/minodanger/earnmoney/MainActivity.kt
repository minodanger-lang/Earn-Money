package com.minodanger.earnmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF176B55),
                    secondary = Color(0xFF4C806F),
                    background = Color(0xFFF6F8F7),
                    surface = Color.White
                )
            ) {
                Surface {
                    EarnMoneyApp()
                }
            }
        }
    }
}
