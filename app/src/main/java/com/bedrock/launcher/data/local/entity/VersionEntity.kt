package com.bedrock.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bedrock.launcher.domain.model.BedrockVersion

@Entity(tableName = "bedrock_versions")
data class VersionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val versionName: String,
    val versionCode: Long,
    val apkFilePath: String,
    val packageName: String = "com.mojang.minecraftpe",
    val architecture: String,
    val targetSdkVersion: Int,
    val fileSizeMb: Double,
    val isVanilla: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isCurrentlyActive: Boolean = false
) {
    fun toDomain(): BedrockVersion = BedrockVersion(
        id = id,
        versionName = versionName,
        versionCode = versionCode,
        apkFilePath = apkFilePath,
        packageName = packageName,
        architecture = architecture,
        targetSdkVersion = targetSdkVersion,
        fileSizeMb = fileSizeMb,
        isVanilla = isVanilla,
        dateAdded = dateAdded,
        notes = notes,
        isCurrentlyActive = isCurrentlyActive
    )

    companion object {
        fun fromDomain(version: BedrockVersion): VersionEntity = VersionEntity(
            id = version.id,
            versionName = version.versionName,
            versionCode = version.versionCode,
            apkFilePath = version.apkFilePath,
            packageName = version.packageName,
            architecture = version.architecture,
            targetSdkVersion = version.targetSdkVersion,
            fileSizeMb = version.fileSizeMb,
            isVanilla = version.isVanilla,
            dateAdded = version.dateAdded,
            notes = version.notes,
            isCurrentlyActive = version.isCurrentlyActive
        )
    }
}
