package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Note(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val summary: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val sentToDiscord: Boolean = false,
    val sentAt: Timestamp? = null
)
