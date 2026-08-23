package com.bedrock.launcher.data.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.bedrock.launcher.domain.model.BedrockVersion
import java.io.File
import java.util.zip.ZipFile

object ApkInspector {

    data class ApkInspectionResult(
        val isMinecraftBedrock: Boolean,
        val versionName: String,
        val versionCode: Long,
        val packageName: String,
        val architecture: String,
        val targetSdk: Int,
        val fileSizeMb: Double,
        val errorMessage: String? = null
    )

    fun inspectApk(context: Context, apkFile: File): ApkInspectionResult {
        if (!apkFile.exists() || !apkFile.canRead()) {
            return ApkInspectionResult(
                isMinecraftBedrock = false,
                versionName = "",
                versionCode = 0,
                packageName = "",
                architecture = "Unknown",
                targetSdk = 0,
                fileSizeMb = 0.0,
                errorMessage = "Файл APK не найден или недоступен для чтения"
            )
        }

        val fileSizeMb = apkFile.length().toDouble() / (1024.0 * 1024.0)

        return try {
            val pm = context.packageManager
            val flags = PackageManager.GET_META_DATA
            val packageInfo: PackageInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
            }

            if (packageInfo == null) {
                return ApkInspectionResult(
                    isMinecraftBedrock = false,
                    versionName = "",
                    versionCode = 0,
                    packageName = "",
                    architecture = "Unknown",
                    targetSdk = 0,
                    fileSizeMb = fileSizeMb,
                    errorMessage = "Не удалось прочитать PackageInfo из APK"
                )
            }

            val packageName = packageInfo.packageName ?: ""
            val versionName = packageInfo.versionName ?: "Unknown"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }

            val targetSdk = packageInfo.applicationInfo?.targetSdkVersion ?: 0

            // Inspect internal native architecture (.so files inside APK)
            val architecture = detectApkArchitecture(apkFile)

            val isMinecraft = packageName == "com.mojang.minecraftpe" ||
                    packageName.contains("minecraft", ignoreCase = true) ||
                    architecture.contains("minecraftpe")

            ApkInspectionResult(
                isMinecraftBedrock = isMinecraft,
                versionName = versionName,
                versionCode = versionCode,
                packageName = packageName,
                architecture = architecture.ifEmpty { "arm64-v8a" },
                targetSdk = targetSdk,
                fileSizeMb = String.format(java.util.Locale.US, "%.1f", fileSizeMb).toDouble(),
                errorMessage = null
            )
        } catch (e: Exception) {
            ApkInspectionResult(
                isMinecraftBedrock = false,
                versionName = "",
                versionCode = 0,
                packageName = "",
                architecture = "Unknown",
                targetSdk = 0,
                fileSizeMb = fileSizeMb,
                errorMessage = "Ошибка анализа APK: ${e.localizedMessage}"
            )
        }
    }

    private fun detectApkArchitecture(apkFile: File): String {
        try {
            ZipFile(apkFile).use { zip ->
                val entries = zip.entries()
                var hasArm64 = false
                var hasArmeabi = false
                var hasX86_64 = false
                var hasX86 = false

                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name
                    if (name.startsWith("lib/arm64-v8a/")) hasArm64 = true
                    if (name.startsWith("lib/armeabi-v7a/")) hasArmeabi = true
                    if (name.startsWith("lib/x86_64/")) hasX86_64 = true
                    if (name.startsWith("lib/x86/")) hasX86 = true
                }

                return when {
                    hasArm64 -> "arm64-v8a"
                    hasArmeabi -> "armeabi-v7a"
                    hasX86_64 -> "x86_64"
                    hasX86 -> "x86"
                    else -> "Universal"
                }
            }
        } catch (e: Exception) {
            return "Universal"
        }
    }
}
