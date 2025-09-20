package com.keagan.smartroots.model

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val imageUris: List<String>
)
