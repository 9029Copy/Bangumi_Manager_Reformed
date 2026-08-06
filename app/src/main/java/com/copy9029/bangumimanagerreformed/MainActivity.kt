package com.copy9029.bangumimanagerreformed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.copy9029.bangumimanagerreformed.ui.theme.BangumiManagerReformedTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BangumiManagerReformedTheme {
                AppNavigation()
            }
        }
    }
}
