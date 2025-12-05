package com.mapapp.app.data.model

data class Problem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val photoUrl: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "active",
    val votesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Construtor vazio necessario para Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
        category = "",
        userName = "",
        userEmail = ""
    )

    fun getTimeAgo(): String {
        val diff = System.currentTimeMillis() - createdAt
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "Ha ${days}d"
            hours > 0 -> "Ha ${hours}h"
            minutes > 0 -> "Ha ${minutes}min"
            else -> "Agora"
        }
    }

    fun getCategoryEmoji(): String {
        return when (category.lowercase()) {
            "buraco na via" -> "🚧"
            "iluminação pública" -> "💡"
            "lixo/limpeza" -> "🗑️"
            "vandalismo" -> "⚠️"
            else -> "📍"
        }
    }
}