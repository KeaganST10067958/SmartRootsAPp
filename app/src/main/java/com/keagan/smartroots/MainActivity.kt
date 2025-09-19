package com.keagan.smartroots

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.keagan.smartroots.data.Prefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()   // must be first
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        lifecycleScope.launch { prefs.setLanguageTag(prefs.languageTag.first()) }
        setContent { SmartRootsApp(prefs) }
    }
}
