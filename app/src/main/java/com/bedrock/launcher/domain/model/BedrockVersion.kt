package com.bedrock.launcher.domain.model

data class BedrockVersion(
    val id: Long = 0,
    val versionName: String,        // e.g. "1.21.2", "1.20.81", "1.19.50"
    val versionCode: Long,          // e.g. 963102001
    val apkFilePath: String,        // Local path to stored APK file
    val packageName: String = "com.mojang.minecraftpe",
    val architecture: String,       // "arm64-v8a", "armeabi-v7a", "x86_64"
    val targetSdkVersion: Int,      // e.g. 33, 34
    val fileSizeMb: Double,         // e.g. 245.5 MB
    val isVanilla: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isCurrentlyActive: Boolean = false
)
