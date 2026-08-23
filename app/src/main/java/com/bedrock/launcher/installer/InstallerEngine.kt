package com.bedrock.launcher.installer

enum class InstallerType(val displayName: String, val description: String) {
    SHIZUKU(
        "Shizuku (Рекомендуется)",
        "Быстрая тихая установка без Root прав в 1 клик"
    ),
    ROOT(
        "Root доступ (su)",
        "Мгновенная замена APK с правами суперпользователя"
    ),
    PACKAGE_INSTALLER(
        "Стандартный установщик",
        "Обычная установка Android с системным диалогом подтверждения"
    ),
    CLONE_PARALLEL(
        "Клонирование (Multi-App)",
        "Параллельная установка с отдельным Package ID"
    )
}

sealed class InstallResult {
    data class Success(val message: String = "Успешно установлено") : InstallResult()
    data class Error(val errorMessage: String) : InstallResult()
    data object PendingUserAction : InstallResult()
}

interface InstallerEngine {
    val type: InstallerType
    suspend fun isAvailable(): Boolean
    suspend fun installApk(apkPath: String): InstallResult
}
