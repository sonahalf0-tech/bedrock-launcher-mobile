package com.bedrock.launcher.data.repository

import android.content.Context
import com.bedrock.launcher.data.local.LauncherDatabase
import com.bedrock.launcher.data.local.entity.AddonEntity
import com.bedrock.launcher.data.local.entity.ProfileEntity
import com.bedrock.launcher.data.local.entity.VersionEntity
import com.bedrock.launcher.data.util.AddonImporter
import com.bedrock.launcher.data.util.ApkInspector
import com.bedrock.launcher.data.util.StorageHelper
import com.bedrock.launcher.domain.model.BedrockAddon
import com.bedrock.launcher.domain.model.BedrockVersion
import com.bedrock.launcher.domain.model.GameProfile
import com.bedrock.launcher.installer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class LauncherRepository(private val context: Context) {

    private val database = LauncherDatabase.getInstance(context)
    val storageHelper = StorageHelper(context)
    val launchManager = LaunchManager(context)
    val addonImporter = AddonImporter(context, storageHelper)
    
    val shizukuInstaller = ShizukuInstaller(context)
    val rootInstaller = RootInstaller()
    val standardInstaller = StandardInstaller(context)

    val profileSwitcher = ProfileSwitcher(context, database, storageHelper, launchManager)

    // Flow streams for UI
    val allVersions: Flow<List<BedrockVersion>> = database.versionDao().getAllVersions().map { list ->
        list.map { it.toDomain() }
    }

    val activeVersion: Flow<BedrockVersion?> = database.versionDao().getActiveVersion().map { it?.toDomain() }

    val allProfiles: Flow<List<GameProfile>> = database.profileDao().getAllProfiles().map { list ->
        list.map { it.toDomain() }
    }

    val activeProfile: Flow<GameProfile?> = database.profileDao().getActiveProfile().map { it?.toDomain() }

    val allAddons: Flow<List<BedrockAddon>> = database.addonDao().getAllAddons().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun importApkFile(apkFile: File): Result<BedrockVersion> = withContext(Dispatchers.IO) {
        val inspection = ApkInspector.inspectApk(context, apkFile)
        if (!inspection.isMinecraftBedrock) {
            return@withContext Result.failure(
                Exception(inspection.errorMessage ?: "Выбранный файл не является APK Minecraft Bedrock")
            )
        }

        // Save APK into internal storage
        val savedApk = storageHelper.saveVersionApk(apkFile, inspection.versionName)

        val version = BedrockVersion(
            versionName = inspection.versionName,
            versionCode = inspection.versionCode,
            apkFilePath = savedApk.absolutePath,
            packageName = inspection.packageName,
            architecture = inspection.architecture,
            targetSdkVersion = inspection.targetSdk,
            fileSizeMb = inspection.fileSizeMb
        )

        val id = database.versionDao().insertVersion(VersionEntity.fromDomain(version))
        val createdVersion = version.copy(id = id)

        // Automatically create a default profile for this version if no profiles exist
        val existingProfiles = database.profileDao().getActiveProfileSync()
        if (existingProfiles == null) {
            val defaultProfile = GameProfile(
                name = "Профиль ${createdVersion.versionName}",
                targetVersionId = id,
                profileDirectoryName = "profile_${id}_${createdVersion.versionName.replace(".", "_")}",
                isDefault = true
            )
            val profileId = database.profileDao().insertProfile(ProfileEntity.fromDomain(defaultProfile))
            database.versionDao().setActiveVersion(id)
            database.profileDao().setDefaultProfile(profileId)
        }

        Result.success(createdVersion)
    }

    suspend fun createProfile(profile: GameProfile): Long = withContext(Dispatchers.IO) {
        val entity = ProfileEntity.fromDomain(profile)
        database.profileDao().insertProfile(entity)
    }

    suspend fun updateProfile(profile: GameProfile) = withContext(Dispatchers.IO) {
        database.profileDao().updateProfile(ProfileEntity.fromDomain(profile))
    }

    suspend fun deleteProfile(profile: GameProfile) = withContext(Dispatchers.IO) {
        database.profileDao().deleteProfile(ProfileEntity.fromDomain(profile))
    }

    suspend fun deleteVersion(version: BedrockVersion) = withContext(Dispatchers.IO) {
        val apkFile = File(version.apkFilePath)
        if (apkFile.exists()) {
            apkFile.delete()
        }
        database.versionDao().deleteVersion(VersionEntity.fromDomain(version))
    }

    suspend fun importAddonFile(file: File): Result<BedrockAddon> = withContext(Dispatchers.IO) {
        val result = addonImporter.importFile(file)
        if (result.success && result.addon != null) {
            val id = database.addonDao().insertAddon(AddonEntity.fromDomain(result.addon))
            Result.success(result.addon.copy(id = id))
        } else {
            Result.failure(Exception(result.errorMessage ?: "Не удалось импортировать файл"))
        }
    }

    suspend fun toggleAddon(addonId: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        database.addonDao().toggleAddonEnabled(addonId, enabled)
    }

    suspend fun deleteAddon(addon: BedrockAddon) = withContext(Dispatchers.IO) {
        val folder = File(storageHelper.addonsDir, addon.folderName)
        if (folder.exists()) {
            folder.deleteRecursively()
        }
        database.addonDao().deleteAddon(AddonEntity.fromDomain(addon))
    }

    fun getInstaller(type: InstallerType): InstallerEngine {
        return when (type) {
            InstallerType.SHIZUKU -> shizukuInstaller
            InstallerType.ROOT -> rootInstaller
            InstallerType.PACKAGE_INSTALLER,
            InstallerType.CLONE_PARALLEL -> standardInstaller
        }
    }
}
