package com.keagan.smartroots.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.R
import com.keagan.smartroots.data.Prefs
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    prefs: Prefs,
    onSelectMode: (String) -> Unit // "veg" | "fodder"
) {
    val scope = rememberCoroutineScope()
    val light by prefs.themeIsLight.collectAsState(initial = false)
    val langTag by prefs.languageTag.collectAsState(initial = "en")

    var logoVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { logoVisible = true }

    Surface(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Logo (PNG in res/drawable/ic_logo.png) ---
            AnimatedVisibility(
                visible = logoVisible,
                enter = fadeIn(animationSpec = tween(700))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified // keep PNG colors
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "SmartRoots",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Hydroponics made simple",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            // --- Theme toggle ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Theme", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (light) "Light" else "Dark")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = light,
                        onCheckedChange = { checked -> scope.launch { prefs.setThemeLight(checked) } }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // --- Language (simple display; wire up your dropdown if you want) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Language", fontWeight = FontWeight.SemiBold)
                Text(
                    langTag.uppercase(),
                    modifier = Modifier.clickable { /* open picker if you have one */ }.padding(8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // --- Mode buttons ---
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSelectMode("veg") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Vegetables")
                }
                OutlinedButton(onClick = { onSelectMode("fodder") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Fodder")
                }
            }
        }
    }
}
