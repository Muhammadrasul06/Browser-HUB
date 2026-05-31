package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val downloadId: Long,
    val filename: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String
)
