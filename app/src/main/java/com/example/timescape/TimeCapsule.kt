package com.example.timescape
import com.google.firebase.Timestamp

data class TimeCapsule(
    val userId : String = "",
    val title: String = "",
    val description: String = "",
    val targetDate: Timestamp? = null,
    val contents: List<String> = listOf(),
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val createdAt: Timestamp? = null // Add this field
)

