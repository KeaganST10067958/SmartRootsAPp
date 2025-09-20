package com.keagan.smartroots

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.keagan.smartroots.data.Prefs
import com.keagan.smartroots.model.AppState
import com.keagan.smartroots.nav.SmartRootsNavGraph
import com.keagan.smartroots.ui.theme.SmartRootsTheme
import com.keagan.smartroots.ui.AppBackdrop   // <<— IMPORTANT import

@Composable
fun SmartRootsApp(prefs: Prefs) {
    val appState = remember { AppState() }
    val light by prefs.themeIsLight.collectAsState(initial = false)

    SmartRootsTheme(light = light) {
        val nav = rememberNavController()
        AppBackdrop(lightMode = light) {
            SmartRootsNavGraph(
                nav = nav,
                prefs = prefs,
                app = appState
            )
        }
    }
}
