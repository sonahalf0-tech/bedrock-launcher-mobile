package com.bedrock.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bedrock.launcher.domain.model.GameProfile

@Entity(tableName = "game_profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetVersionId: Long,
    val profileDirectoryName: String,
    val isDefault: Boolean = false,
    val maxFps: Int = 120,
    val renderDistanceChunks: Int = 12,
    val guiScale: Int = 0,
    val fov: Int = 70,
    val enableVsync: Boolean = false,
    val antiAliasing: Int = 1,
    val customOptionsTxtContent: String? = null,
    val lastPlayedTime: Long = 0
) {
    fun toDomain(): GameProfile = GameProfile(
        id = id,
        name = name,
        targetVersionId = targetVersionId,
        profileDirectoryName = profileDirectoryName,
        isDefault = isDefault,
        maxFps = maxFps,
        renderDistanceChunks = renderDistanceChunks,
        guiScale = guiScale,
        fov = fov,
        enableVsync = enableVsync,
        antiAliasing = antiAliasing,
        customOptionsTxtContent = customOptionsTxtContent,
        lastPlayedTime = lastPlayedTime
    )

    companion object {
        fun fromDomain(profile: GameProfile): ProfileEntity = ProfileEntity(
            id = profile.id,
            name = profile.name,
            targetVersionId = profile.targetVersionId,
            profileDirectoryName = profile.profileDirectoryName,
            isDefault = profile.isDefault,
            maxFps = profile.maxFps,
            renderDistanceChunks = profile.renderDistanceChunks,
            guiScale = profile.guiScale,
            fov = profile.fov,
            enableVsync = profile.enableVsync,
            antiAliasing = profile.antiAliasing,
            customOptionsTxtContent = profile.customOptionsTxtContent,
            lastPlayedTime = profile.lastPlayedTime
        )
    }
}
