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
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // hold splash N ms
        var keep = true
        splash.setKeepOnScreenCondition { keep }
        lifecycleScope.launch {
            kotlinx.coroutines.delay(1500) // change duration as you like
            keep = false
        }

        // Optional: nice exit
        splash.setOnExitAnimationListener { provider ->
            provider.iconView.animate()
                .alpha(0f).scaleX(0.9f).scaleY(0.9f)
                .setDuration(200)
                .withEndAction { provider.remove() }
                .start()
        }

        val prefs = Prefs(this)
        lifecycleScope.launch { prefs.setLanguageTag(prefs.languageTag.first()) }
        setContent { SmartRootsApp(prefs) }
    }
}

