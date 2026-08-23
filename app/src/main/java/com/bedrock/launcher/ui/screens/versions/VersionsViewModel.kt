package com.bedrock.launcher.ui.screens.versions

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bedrock.launcher.data.repository.LauncherRepository
import com.bedrock.launcher.domain.model.BedrockVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class VersionsUiState(
    val versions: List<BedrockVersion> = emptyList(),
    val activeVersion: BedrockVersion? = null,
    val isImporting: Boolean = false,
    val importMessage: String? = null,
    val errorMessage: String? = null
)

class VersionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)

    private val _uiState = MutableStateFlow(VersionsUiState())
    val uiState: StateFlow<VersionsUiState> = _uiState.asStateFlow()

    init {
        loadVersions()
    }

    private fun loadVersions() {
        viewModelScope.launch {
            combine(
                repository.allVersions,
                repository.activeVersion
            ) { versions, active ->
                _uiState.update { it.copy(versions = versions, activeVersion = active) }
            }.collect()
        }
    }

    fun importApkFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importMessage = "Чтение APK файла...") }
            try {
                val context = getApplication<Application>()
                val tempFile = withContext(Dispatchers.IO) {
                    val temp = File(context.cacheDir, "imported_${System.currentTimeMillis()}.apk")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output ->
                            input.copyTo(output)
                        }
                    }
                    temp
                }

                _uiState.update { it.copy(importMessage = "Анализ версии Minecraft Bedrock...") }
                val result = repository.importApkFile(tempFile)

                result.onSuccess { version ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importMessage = null
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            errorMessage = error.localizedMessage ?: "Ошибка импорта APK"
                        )
                    }
                }

                // Cleanup temp file
                tempFile.delete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = "Не удалось прочитать файл: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun selectVersion(version: BedrockVersion) {
        viewModelScope.launch {
            val currentActiveProfile = repository.activeProfile.first()
            if (currentActiveProfile != null) {
                val updatedProfile = currentActiveProfile.copy(targetVersionId = version.id)
                repository.updateProfile(updatedProfile)
            }
            // Update database active version
            val db = com.bedrock.launcher.data.local.LauncherDatabase.getInstance(getApplication())
            db.versionDao().setActiveVersion(version.id)
        }
    }

    fun deleteVersion(version: BedrockVersion) {
        viewModelScope.launch {
            repository.deleteVersion(version)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
