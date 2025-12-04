package com.mapapp.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "problems")
data class ProblemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val userName: String,
    val userEmail: String,
    val photoUrl: String?,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val votesCount: Int,
    val createdAt: Long
)

// Função para converter Entity em Model (para UI)
fun ProblemEntity.toProblem(): Problem {
    return Problem(
        id = id,
        title = title,
        description = description,
        category = category,
        userName = userName,
        userEmail = userEmail,
        photoUrl = photoUrl,
        latitude = latitude,
        longitude = longitude,
        status = status,
        votesCount = votesCount,
        createdAt = createdAt
    )
}

// Função para converter Model em Entity (para salvar)
fun Problem.toEntity(): ProblemEntity {
    return ProblemEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        userName = userName,
        userEmail = userEmail,
        photoUrl = photoUrl,
        latitude = latitude,
        longitude = longitude,
        status = status,
        votesCount = votesCount,
        createdAt = createdAt
    )
}