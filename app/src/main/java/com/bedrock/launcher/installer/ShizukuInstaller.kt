package com.bedrock.launcher.installer

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ShizukuInstaller(private val context: Context) : InstallerEngine {

    override val type: InstallerType = InstallerType.SHIZUKU

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!Shizuku.pingBinder()) return@withContext false
            return@withContext Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            return@withContext false
        }
    }

    fun hasShizukuPermission(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(requestCode)
        }
    }

    override suspend fun installApk(apkPath: String): InstallResult = withContext(Dispatchers.IO) {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return@withContext InstallResult.Error("Файл APK не найден: $apkPath")
        }

        if (!isAvailable()) {
            return@withContext InstallResult.Error("Служба Shizuku не запущена или не выданы разрешения")
        }

        try {
            // Run pm install via Shizuku process
            // -r: replace existing application
            // -d: allow version downgrade
            // -g: grant all runtime permissions
            val cmd = arrayOf("pm", "install", "-r", "-d", "-g", apkFile.absolutePath)
            val process = Shizuku.newProcess(cmd, null, null)

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }

            val exitCode = process.waitFor()
            val resultText = output.toString().trim()

            if (exitCode == 0 && (resultText.contains("Success", ignoreCase = true) || resultText.isEmpty())) {
                InstallResult.Success("Версия Bedrock успешно установлена через Shizuku")
            } else {
                InstallResult.Error("Ошибка установки через Shizuku (код $exitCode): $resultText")
            }
        } catch (e: Exception) {
            InstallResult.Error("Исключение при установке Shizuku: ${e.localizedMessage}")
        }
    }
}
