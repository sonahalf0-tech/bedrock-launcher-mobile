package com.bedrock.launcher.data.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class StorageHelper(private val context: Context) {

    // Launcher internal root directories
    val versionsDir: File
        get() = File(context.getExternalFilesDir(null), "versions").apply { mkdirs() }

    val profilesDir: File
        get() = File(context.getExternalFilesDir(null), "profiles").apply { mkdirs() }

    val addonsDir: File
        get() = File(context.getExternalFilesDir(null), "addons").apply { mkdirs() }

    val backupsDir: File
        get() = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }

    // Official Minecraft data directories
    val primaryMinecraftDataDir: File
        get() = File(
            Environment.getExternalStorageDirectory(),
            "Android/data/com.mojang.minecraftpe/files/games/com.mojang"
        )

    val legacyMinecraftDataDir: File
        get() = File(
            Environment.getExternalStorageDirectory(),
            "games/com.mojang"
        )

    fun getActiveMinecraftDataDir(): File {
        return if (primaryMinecraftDataDir.exists()) {
            primaryMinecraftDataDir
        } else {
            legacyMinecraftDataDir
        }
    }

    fun getProfileDir(profileDirName: String): File {
        return File(profilesDir, profileDirName).apply { mkdirs() }
    }

    fun getProfileWorldsDir(profileDirName: String): File {
        return File(getProfileDir(profileDirName), "minecraftWorlds").apply { mkdirs() }
    }

    fun getProfileResourcePacksDir(profileDirName: String): File {
        return File(getProfileDir(profileDirName), "resource_packs").apply { mkdirs() }
    }

    fun getProfileBehaviorPacksDir(profileDirName: String): File {
        return File(getProfileDir(profileDirName), "behavior_packs").apply { mkdirs() }
    }

    fun getProfileOptionsFile(profileDirName: String): File {
        val mcpeDir = File(getProfileDir(profileDirName), "minecraftpe").apply { mkdirs() }
        return File(mcpeDir, "options.txt")
    }

    /**
     * Backs up active Minecraft data (worlds, options, packs) into the given profile's directory
     */
    fun backupCurrentDataToProfile(profileDirName: String) {
        val mcData = getActiveMinecraftDataDir()
        if (!mcData.exists()) return

        val profileDir = getProfileDir(profileDirName)
        copyDirectory(File(mcData, "minecraftWorlds"), File(profileDir, "minecraftWorlds"))
        copyDirectory(File(mcData, "resource_packs"), File(profileDir, "resource_packs"))
        copyDirectory(File(mcData, "behavior_packs"), File(profileDir, "behavior_packs"))
        
        val srcOptions = File(mcData, "minecraftpe/options.txt")
        if (srcOptions.exists()) {
            val destOptions = File(profileDir, "minecraftpe/options.txt")
            destOptions.parentFile?.mkdirs()
            srcOptions.copyTo(destOptions, overwrite = true)
        }
    }

    /**
     * Deploys profile data (worlds, options, packs) to active Minecraft directory
     */
    fun restoreProfileToActiveMinecraft(profileDirName: String) {
        val profileDir = getProfileDir(profileDirName)
        if (!profileDir.exists()) return

        val mcData = getActiveMinecraftDataDir()
        mcData.mkdirs()

        copyDirectory(File(profileDir, "minecraftWorlds"), File(mcData, "minecraftWorlds"))
        copyDirectory(File(profileDir, "resource_packs"), File(mcData, "resource_packs"))
        copyDirectory(File(profileDir, "behavior_packs"), File(mcData, "behavior_packs"))

        val srcOptions = File(profileDir, "minecraftpe/options.txt")
        if (srcOptions.exists()) {
            val destOptions = File(mcData, "minecraftpe/options.txt")
            destOptions.parentFile?.mkdirs()
            srcOptions.copyTo(destOptions, overwrite = true)
        }
    }

    fun copyDirectory(source: File, destination: File) {
        if (!source.exists()) return
        if (source.isDirectory) {
            if (!destination.exists()) {
                destination.mkdirs()
            }
            val files = source.listFiles() ?: return
            for (file in files) {
                copyDirectory(file, File(destination, file.name))
            }
        } else {
            source.copyTo(destination, overwrite = true)
        }
    }

    fun saveVersionApk(sourceApk: File, versionName: String): File {
        val sanitized = versionName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
        val targetFile = File(versionsDir, "bedrock_$sanitized.apk")
        sourceApk.copyTo(targetFile, overwrite = true)
        return targetFile
    }
}
