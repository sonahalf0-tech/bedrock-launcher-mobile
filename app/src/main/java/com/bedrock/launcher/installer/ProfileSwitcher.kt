package com.bedrock.launcher.installer

import android.content.Context
import com.bedrock.launcher.data.local.LauncherDatabase
import com.bedrock.launcher.data.util.OptionsTxtParser
import com.bedrock.launcher.data.util.StorageHelper
import com.bedrock.launcher.domain.model.BedrockVersion
import com.bedrock.launcher.domain.model.GameOptions
import com.bedrock.launcher.domain.model.GameProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProfileSwitcher(
    private val context: Context,
    private val database: LauncherDatabase,
    private val storageHelper: StorageHelper,
    private val launchManager: LaunchManager
) {

    sealed class SwitchProgress {
        data class Progress(val step: String) : SwitchProgress()
        data class Completed(val message: String) : SwitchProgress()
        data class Failed(val error: String) : SwitchProgress()
    }

    suspend fun switchProfileAndVersion(
        targetProfile: GameProfile,
        targetVersion: BedrockVersion,
        installerEngine: InstallerEngine,
        onProgress: (SwitchProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(SwitchProgress.Progress("Сохранение данных текущего профиля..."))
            val currentActiveProfileEntity = database.profileDao().getActiveProfileSync()
            if (currentActiveProfileEntity != null) {
                storageHelper.backupCurrentDataToProfile(currentActiveProfileEntity.profileDirectoryName)
            }

            // Check if installed version matches target version
            val installedVersionName = launchManager.getInstalledVersionName(targetVersion.packageName)
            val needsApkInstall = installedVersionName != targetVersion.versionName

            if (needsApkInstall) {
                onProgress(SwitchProgress.Progress("Установка версии Bedrock ${targetVersion.versionName}..."))
                val installResult = installerEngine.installApk(targetVersion.apkFilePath)
                when (installResult) {
                    is InstallResult.Error -> {
                        onProgress(SwitchProgress.Failed(installResult.errorMessage))
                        return@withContext
                    }
                    is InstallResult.PendingUserAction -> {
                        onProgress(SwitchProgress.Progress("Ожидание подтверждения установки пользователем..."))
                    }
                    is InstallResult.Success -> {
                        onProgress(SwitchProgress.Progress("Версия успешно установлена!"))
                    }
                }
            }

            // Restore target profile data
            onProgress(SwitchProgress.Progress("Восстановление миров и настроек профиля..."))
            storageHelper.restoreProfileToActiveMinecraft(targetProfile.profileDirectoryName)

            // Apply custom options.txt tweaks (FPS, View distance, GUI scale)
            applyProfileOptions(targetProfile)

            // Update database states
            database.versionDao().setActiveVersion(targetVersion.id)
            database.profileDao().setDefaultProfile(targetProfile.id)

            onProgress(SwitchProgress.Completed("Готово! Профиль и версия ${targetVersion.versionName} активированы"))
        } catch (e: Exception) {
            onProgress(SwitchProgress.Failed("Ошибка переключения: ${e.localizedMessage}"))
        }
    }

    private fun applyProfileOptions(profile: GameProfile) {
        try {
            val mcDataDir = storageHelper.getActiveMinecraftDataDir()
            val optionsFile = File(mcDataDir, "minecraftpe/options.txt")
            
            val currentOptions = if (optionsFile.exists()) {
                OptionsTxtParser.parse(optionsFile)
            } else {
                GameOptions()
            }

            // Override with profile settings
            currentOptions.maxFramerate = profile.maxFps
            currentOptions.setChunks(profile.renderDistanceChunks)
            currentOptions.guiScale = profile.guiScale
            currentOptions.fov = profile.fov.toFloat()
            currentOptions.vsync = profile.enableVsync
            currentOptions.antiAliasing = profile.antiAliasing

            OptionsTxtParser.saveToFile(currentOptions, optionsFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
