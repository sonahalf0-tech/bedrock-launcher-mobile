package com.bedrock.launcher.installer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StandardInstaller(private val context: Context) : InstallerEngine {

    override val type: InstallerType = InstallerType.PACKAGE_INSTALLER

    override suspend fun isAvailable(): Boolean = true

    override suspend fun installApk(apkPath: String): InstallResult = withContext(Dispatchers.Main) {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return@withContext InstallResult.Error("Файл APK не найден: $apkPath")
        }

        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
            InstallResult.PendingUserAction
        } catch (e: Exception) {
            InstallResult.Error("Не удалось открыть стандартный установщик: ${e.localizedMessage}")
        }
    }
}
