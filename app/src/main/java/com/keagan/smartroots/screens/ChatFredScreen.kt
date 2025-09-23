package com.keagan.smartroots.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFredScreen(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf("Fred: Hi! How can I help on the farm today?") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with Fred", fontWeight = FontWeight.Medium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null) } }
            )
        }
    ) { pads ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pads)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                items(messages) { m -> Text(m) }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message…") }
                )
                Button(onClick = {
                    if (input.isNotBlank()) {
                        messages += "You: $input"
                        messages += "Fred: (mock) Noted! 👍"
                        input = ""
                    }
                }) { Text("Send") }
            }
        }
    }
}
