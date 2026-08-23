package com.bedrock.launcher.domain.model

enum class AddonType {
    RESOURCE_PACK,    // Textures, GUI, Fonts, Shaders (.mcpack)
    BEHAVIOR_PACK,    // Entities, Scripts, Items, Gameplay (.mcpack)
    WORLD,            // Pre-built map / world save (.mcworld)
    ADDON_BUNDLE      // Combined resource + behavior pack (.mcaddon)
}

data class BedrockAddon(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val version: String = "1.0.0",
    val type: AddonType,
    val fileSourcePath: String,
    val folderName: String,
    val iconPath: String? = null,
    val uuid: String = "",
    val isEnabledForDefaultProfile: Boolean = true,
    val dateImported: Long = System.currentTimeMillis()
)
