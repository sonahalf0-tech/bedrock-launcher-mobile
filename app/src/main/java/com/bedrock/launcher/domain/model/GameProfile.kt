package com.bedrock.launcher.domain.model

data class GameProfile(
    val id: Long = 0,
    val name: String,                    // e.g. "Vanilla 1.21", "PvP Settings", "Survival World"
    val targetVersionId: Long,           // References BedrockVersion.id
    val profileDirectoryName: String,    // Unique folder in launcher storage for isolated data
    val isDefault: Boolean = false,
    val maxFps: Int = 120,               // 30, 60, 90, 120, 0 (unlimited)
    val renderDistanceChunks: Int = 12,  // 6, 8, 12, 16, 24, 32, 64
    val guiScale: Int = 0,               // -1, 0, 1, 2
    val fov: Int = 70,                   // 30..110
    val enableVsync: Boolean = false,
    val antiAliasing: Int = 1,           // 1, 2, 4
    val customOptionsTxtContent: String? = null,
    val lastPlayedTime: Long = 0
)
