package com.keagan.smartroots.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keagan.smartroots.R
import com.keagan.smartroots.data.Prefs
import kotlinx.coroutines.launch

@Composable
private fun scrimForTheme(): Pair<Color, Color> {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val container = if (isLight) Color.White.copy(alpha = 0.88f)
    else Color(0xFF0E1A0E).copy(alpha = 0.70f)
    val content = if (isLight) Color.Black else Color.White
    return container to content
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(
    prefs: Prefs,
    onSelectMode: (String) -> Unit // "veg" | "fodder" | "planner_veg" | "planner_fodder" | "harvest"
) {
    val scope = rememberCoroutineScope()
    val light by prefs.themeIsLight.collectAsState(initial = false)
    val langTag by prefs.languageTag.collectAsState(initial = "EN")

    val logoVisible by remember { mutableStateOf(true) }
    val (scrimBg, scrimFg) = scrimForTheme()

    Surface(
        modifier = Modifier.fillMaxSize().systemBarsPadding(),
        color = Color.Transparent      // keep global backdrop visible
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            AnimatedVisibility(visible = logoVisible, enter = fadeIn(animationSpec = tween(700))) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "SmartRoots",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Color.Black else Color.White
            )
            Text(
                "Hydroponics made simple",
                style = MaterialTheme.typography.bodyMedium,
                color = if (MaterialTheme.colorScheme.background.luminance() > 0.5f)
                    Color.Black.copy(alpha = 0.70f)
                else
                    Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(24.dp))

            // Theme row on a scrim
            Surface(
                color = scrimBg,
                contentColor = scrimFg,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Theme", fontWeight = FontWeight.SemiBold, color = scrimFg)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (light) "Light" else "Dark", color = scrimFg)
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = light,
                            onCheckedChange = { checked -> scope.launch { prefs.setThemeLight(checked) } }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Language row on a scrim
            Surface(
                color = scrimBg,
                contentColor = scrimFg,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Language", fontWeight = FontWeight.SemiBold, color = scrimFg)
                    Text(langTag.uppercase(), color = scrimFg, modifier = Modifier.clickable { /* future: open language picker */ })
                }
            }

            Spacer(Modifier.height(20.dp))

            // Primary actions (solid buttons)
            PrimaryActionButton(
                text = "Vegetables",
                onClick = { onSelectMode("veg") },
                color = Color(0xFF2EBE7E) // Green
            )
            Spacer(Modifier.height(14.dp))
            PrimaryActionButton(
                text = "Fodder",
                onClick = { onSelectMode("fodder") },
                color = Color(0xFFF39C12) // Orange
            )

            Spacer(Modifier.height(22.dp))

            // Single “Plan & track” header (no duplicate chip)
            Surface(
                color = scrimBg,
                contentColor = scrimFg,
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    "Plan & track",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scrimFg
                )
            }

            Spacer(Modifier.height(14.dp))

            PrimaryActionButton(
                text = "Veg Planner",
                onClick = { onSelectMode("planner_veg") },
                color = Color(0xFF2EBE7E) // Green
            )
            Spacer(Modifier.height(14.dp))
            PrimaryActionButton(
                text = "Fodder Planner",
                onClick = { onSelectMode("planner_fodder") },
                color = Color(0xFFF39C12) // Orange
            )
            Spacer(Modifier.height(14.dp))
            PrimaryActionButton(
                text = "Harvest Tracker",
                onClick = { onSelectMode("harvest") },
                color = Color(0xFF5057D6) // Indigo
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}
