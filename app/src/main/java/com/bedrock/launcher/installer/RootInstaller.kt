package com.bedrock.launcher.installer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

class RootInstaller : InstallerEngine {

    override val type: InstallerType = InstallerType.ROOT

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su -c id")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun installApk(apkPath: String): InstallResult = withContext(Dispatchers.IO) {
        val apkFile = File(apkPath)
        if (!apkFile.exists()) {
            return@withContext InstallResult.Error("Файл APK не найден: $apkPath")
        }

        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)

            // -r = reinstall, -d = allow downgrade, -g = grant permissions
            outputStream.writeBytes("pm install -r -d -g \"${apkFile.absolutePath}\"\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()

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
            val result = output.toString().trim()

            if (exitCode == 0 && (result.contains("Success", ignoreCase = true) || result.isEmpty())) {
                InstallResult.Success("Версия Bedrock успешно установлена с Root правами")
            } else {
                InstallResult.Error("Ошибка Root установки (код $exitCode): $result")
            }
        } catch (e: Exception) {
            InstallResult.Error("Исключение при Root установке: ${e.localizedMessage}")
        }
    }
}
