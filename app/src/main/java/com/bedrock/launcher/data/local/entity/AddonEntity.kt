package com.bedrock.launcher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bedrock.launcher.domain.model.AddonType
import com.bedrock.launcher.domain.model.BedrockAddon

@Entity(tableName = "bedrock_addons")
data class AddonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val version: String = "1.0.0",
    val type: String,               // RESOURCE_PACK, BEHAVIOR_PACK, WORLD, ADDON_BUNDLE
    val fileSourcePath: String,
    val folderName: String,
    val iconPath: String? = null,
    val uuid: String = "",
    val isEnabledForDefaultProfile: Boolean = true,
    val dateImported: Long = System.currentTimeMillis()
) {
    fun toDomain(): BedrockAddon = BedrockAddon(
        id = id,
        name = name,
        description = description,
        version = version,
        type = try { AddonType.valueOf(type) } catch (e: Exception) { AddonType.RESOURCE_PACK },
        fileSourcePath = fileSourcePath,
        folderName = folderName,
        iconPath = iconPath,
        uuid = uuid,
        isEnabledForDefaultProfile = isEnabledForDefaultProfile,
        dateImported = dateImported
    )

    companion object {
        fun fromDomain(addon: BedrockAddon): AddonEntity = AddonEntity(
            id = addon.id,
            name = addon.name,
            description = addon.description,
            version = addon.version,
            type = addon.type.name,
            fileSourcePath = addon.fileSourcePath,
            folderName = addon.folderName,
            iconPath = addon.iconPath,
            uuid = addon.uuid,
            isEnabledForDefaultProfile = addon.isEnabledForDefaultProfile,
            dateImported = addon.dateImported
        )
    }
}
