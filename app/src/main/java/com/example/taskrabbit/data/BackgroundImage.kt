package com.example.taskrabbit.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backgrounds")
data class BackgroundImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val uri: String? = null, // Store the URI as a string
    val assetResId: Int? = null, // Resource ID for asset images
    val isAsset: Boolean = false,
    val isPremium: Boolean = false
)