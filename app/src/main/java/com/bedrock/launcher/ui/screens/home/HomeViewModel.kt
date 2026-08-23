package com.bedrock.launcher.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bedrock.launcher.data.repository.LauncherRepository
import com.bedrock.launcher.domain.model.BedrockVersion
import com.bedrock.launcher.domain.model.GameProfile
import com.bedrock.launcher.installer.InstallerType
import com.bedrock.launcher.installer.ProfileSwitcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeVersion: BedrockVersion? = null,
    val activeProfile: GameProfile? = null,
    val installedVersionName: String? = null,
    val isMinecraftInstalled: Boolean = false,
    val isSwitching: Boolean = false,
    val statusMessage: String = "Готов к игре",
    val errorMessage: String? = null,
    val preferredInstaller: InstallerType = InstallerType.SHIZUKU
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.activeVersion,
                repository.activeProfile
            ) { version, profile ->
                val installedVer = repository.launchManager.getInstalledVersionName()
                val isInstalled = repository.launchManager.isMinecraftInstalled()
                _uiState.update { current ->
                    current.copy(
                        activeVersion = version,
                        activeProfile = profile,
                        installedVersionName = installedVer,
                        isMinecraftInstalled = isInstalled
                    )
                }
            }.collect()
        }
    }

    fun launchGame() {
        val success = repository.launchManager.launchMinecraft()
        if (!success) {
            _uiState.update { it.copy(errorMessage = "Не удалось запустить Minecraft. Убедитесь, что версия установлена.") }
        }
    }

    fun switchAndLaunch() {
        val version = _uiState.value.activeVersion
        val profile = _uiState.value.activeProfile
        if (version == null || profile == null) {
            _uiState.update { it.copy(errorMessage = "Выберите версию и профиль для запуска") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSwitching = true, statusMessage = "Подготовка к запуску...") }
            val installer = repository.getInstaller(_uiState.value.preferredInstaller)
            
            repository.profileSwitcher.switchProfileAndVersion(
                targetProfile = profile,
                targetVersion = version,
                installerEngine = installer
            ) { progress ->
                when (progress) {
                    is ProfileSwitcher.SwitchProgress.Progress -> {
                        _uiState.update { it.copy(statusMessage = progress.step) }
                    }
                    is ProfileSwitcher.SwitchProgress.Completed -> {
                        _uiState.update { it.copy(isSwitching = false, statusMessage = progress.message) }
                        repository.launchManager.launchMinecraft()
                    }
                    is ProfileSwitcher.SwitchProgress.Failed -> {
                        _uiState.update { it.copy(isSwitching = false, errorMessage = progress.error) }
                    }
                }
            }
        }
    }

    fun updateProfileFps(maxFps: Int) {
        val profile = _uiState.value.activeProfile ?: return
        val updated = profile.copy(maxFps = maxFps)
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    fun updateProfileChunks(chunks: Int) {
        val profile = _uiState.value.activeProfile ?: return
        val updated = profile.copy(renderDistanceChunks = chunks)
        viewModelScope.launch {
            repository.updateProfile(updated)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
