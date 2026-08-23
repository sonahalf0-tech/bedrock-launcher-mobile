package com.bedrock.launcher.ui.screens.mods

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bedrock.launcher.data.repository.LauncherRepository
import com.bedrock.launcher.domain.model.AddonType
import com.bedrock.launcher.domain.model.BedrockAddon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ModsUiState(
    val addons: List<BedrockAddon> = emptyList(),
    val selectedTab: AddonType? = null, // null = all
    val isImporting: Boolean = false,
    val errorMessage: String? = null
)

class ModsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)

    private val _uiState = MutableStateFlow(ModsUiState())
    val uiState: StateFlow<ModsUiState> = _uiState.asStateFlow()

    init {
        loadAddons()
    }

    private fun loadAddons() {
        viewModelScope.launch {
            repository.allAddons.collect { list ->
                _uiState.update { it.copy(addons = list) }
            }
        }
    }

    fun selectTab(type: AddonType?) {
        _uiState.update { it.copy(selectedTab = type) }
    }

    fun importAddonFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val context = getApplication<Application>()
                val fileName = getFileName(uri) ?: "addon.mcpack"
                val tempFile = withContext(Dispatchers.IO) {
                    val temp = File(context.cacheDir, fileName)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(temp).use { output ->
                            input.copyTo(output)
                        }
                    }
                    temp
                }

                val result = repository.importAddonFile(tempFile)
                result.onSuccess {
                    _uiState.update { it.copy(isImporting = false) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            errorMessage = error.localizedMessage ?: "Ошибка распаковки аддона"
                        )
                    }
                }

                tempFile.delete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = "Не удалось импортировать: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun toggleAddon(addon: BedrockAddon, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAddon(addon.id, enabled)
        }
    }

    fun deleteAddon(addon: BedrockAddon) {
        viewModelScope.launch {
            repository.deleteAddon(addon)
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) it.getString(nameIndex) else null
            } else null
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
