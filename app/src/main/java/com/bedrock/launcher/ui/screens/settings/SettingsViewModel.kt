package com.bedrock.launcher.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bedrock.launcher.data.repository.LauncherRepository
import com.bedrock.launcher.installer.InstallerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedInstaller: InstallerType = InstallerType.SHIZUKU,
    val isShizukuAvailable: Boolean = false,
    val isRootAvailable: Boolean = false,
    val storagePath: String = "",
    val message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        checkStatus()
    }

    fun checkStatus() {
        viewModelScope.launch {
            val shizukuReady = repository.shizukuInstaller.isAvailable()
            val rootReady = repository.rootInstaller.isAvailable()
            val path = repository.storageHelper.getActiveMinecraftDataDir().absolutePath

            _uiState.update {
                it.copy(
                    isShizukuAvailable = shizukuReady,
                    isRootAvailable = rootReady,
                    storagePath = path
                )
            }
        }
    }

    fun setInstallerType(type: InstallerType) {
        _uiState.update { it.copy(selectedInstaller = type) }
    }

    fun requestShizukuPermission() {
        repository.shizukuInstaller.requestPermission()
        checkStatus()
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
