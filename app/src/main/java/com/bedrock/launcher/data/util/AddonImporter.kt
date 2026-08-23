package com.bedrock.launcher.data.util

import android.content.Context
import com.bedrock.launcher.domain.model.AddonType
import com.bedrock.launcher.domain.model.BedrockAddon
import net.lingala.zip4j.ZipFile
import org.json.JSONObject
import java.io.File

class AddonImporter(private val context: Context, private val storageHelper: StorageHelper) {

    data class ImportResult(
        val success: Boolean,
        val addon: BedrockAddon? = null,
        val errorMessage: String? = null
    )

    fun importFile(file: File): ImportResult {
        if (!file.exists() || !file.canRead()) {
            return ImportResult(false, null, "Файл не найден или недоступен")
        }

        val extension = file.extension.lowercase()
        return when (extension) {
            "mcpack" -> importMcPack(file)
            "mcaddon" -> importMcAddon(file)
            "mcworld" -> importMcWorld(file)
            "zip" -> importZip(file)
            else -> ImportResult(false, null, "Неподдерживаемый формат: .$extension")
        }
    }

    private fun importMcPack(packFile: File): ImportResult {
        return try {
            val baseName = packFile.nameWithoutExtension.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val targetExtractDir = File(storageHelper.addonsDir, baseName)
            targetExtractDir.mkdirs()

            ZipFile(packFile).extractAll(targetExtractDir.absolutePath)

            // Look for manifest.json
            val manifestFile = File(targetExtractDir, "manifest.json")
            var packName = packFile.nameWithoutExtension
            var packDescription = ""
            var packVersion = "1.0.0"
            var packUuid = ""
            var packType = AddonType.RESOURCE_PACK

            if (manifestFile.exists()) {
                val json = JSONObject(manifestFile.readText())
                val header = json.optJSONObject("header")
                if (header != null) {
                    packName = header.optString("name", packName)
                    packDescription = header.optString("description", "")
                    packUuid = header.optString("uuid", "")
                    val versionArray = header.optJSONArray("version")
                    if (versionArray != null) {
                        packVersion = "${versionArray.optInt(0, 1)}.${versionArray.optInt(1, 0)}.${versionArray.optInt(2, 0)}"
                    }
                }

                val modules = json.optJSONArray("modules")
                if (modules != null && modules.length() > 0) {
                    val firstModule = modules.getJSONObject(0)
                    val moduleType = firstModule.optString("type", "")
                    packType = when (moduleType.lowercase()) {
                        "resources" -> AddonType.RESOURCE_PACK
                        "data", "javascript", "client_data" -> AddonType.BEHAVIOR_PACK
                        "world_template" -> AddonType.WORLD
                        else -> AddonType.RESOURCE_PACK
                    }
                }
            }

            val iconFile = File(targetExtractDir, "pack_icon.png")
            val iconPath = if (iconFile.exists()) iconFile.absolutePath else null

            val addon = BedrockAddon(
                name = packName,
                description = packDescription,
                version = packVersion,
                type = packType,
                fileSourcePath = packFile.absolutePath,
                folderName = targetExtractDir.name,
                iconPath = iconPath,
                uuid = packUuid
            )

            ImportResult(true, addon)
        } catch (e: Exception) {
            ImportResult(false, null, "Ошибка распаковки MCPACK: ${e.localizedMessage}")
        }
    }

    private fun importMcAddon(addonFile: File): ImportResult {
        return try {
            val baseName = addonFile.nameWithoutExtension.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val targetExtractDir = File(storageHelper.addonsDir, baseName)
            targetExtractDir.mkdirs()

            ZipFile(addonFile).extractAll(targetExtractDir.absolutePath)

            val addon = BedrockAddon(
                name = addonFile.nameWithoutExtension,
                description = "Bedrock Addon Bundle",
                version = "1.0.0",
                type = AddonType.ADDON_BUNDLE,
                fileSourcePath = addonFile.absolutePath,
                folderName = targetExtractDir.name
            )
            ImportResult(true, addon)
        } catch (e: Exception) {
            ImportResult(false, null, "Ошибка распаковки MCADDON: ${e.localizedMessage}")
        }
    }

    private fun importMcWorld(worldFile: File): ImportResult {
        return try {
            val baseName = worldFile.nameWithoutExtension.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
            val targetExtractDir = File(storageHelper.addonsDir, "world_$baseName")
            targetExtractDir.mkdirs()

            ZipFile(worldFile).extractAll(targetExtractDir.absolutePath)

            val levelNameFile = File(targetExtractDir, "levelname.txt")
            val worldName = if (levelNameFile.exists()) levelNameFile.readText().trim() else worldFile.nameWithoutExtension

            val addon = BedrockAddon(
                name = worldName,
                description = "Minecraft World Save",
                version = "1.0.0",
                type = AddonType.WORLD,
                fileSourcePath = worldFile.absolutePath,
                folderName = targetExtractDir.name
            )
            ImportResult(true, addon)
        } catch (e: Exception) {
            ImportResult(false, null, "Ошибка импорта мира MCWORLD: ${e.localizedMessage}")
        }
    }

    private fun importZip(zipFile: File): ImportResult {
        return importMcPack(zipFile)
    }
}
