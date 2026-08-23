package com.bedrock.launcher.ui.screens.profiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bedrock.launcher.data.repository.LauncherRepository
import com.bedrock.launcher.domain.model.BedrockVersion
import com.bedrock.launcher.domain.model.GameProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfilesUiState(
    val profiles: List<GameProfile> = emptyList(),
    val versions: List<BedrockVersion> = emptyList(),
    val activeProfile: GameProfile? = null,
    val showCreateDialog: Boolean = false,
    val editingProfile: GameProfile? = null,
    val errorMessage: String? = null
)

class ProfilesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LauncherRepository(application)

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.allProfiles,
                repository.allVersions,
                repository.activeProfile
            ) { profiles, versions, active ->
                _uiState.update {
                    it.copy(
                        profiles = profiles,
                        versions = versions,
                        activeProfile = active
                    )
                }
            }.collect()
        }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createProfile(name: String, targetVersionId: Long, maxFps: Int, renderDistance: Int) {
        viewModelScope.launch {
            val sanitized = name.lowercase().replace("[^a-z0-9_-]".toRegex(), "_")
            val profile = GameProfile(
                name = name,
                targetVersionId = targetVersionId,
                profileDirectoryName = "profile_${System.currentTimeMillis()}_$sanitized",
                maxFps = maxFps,
                renderDistanceChunks = renderDistance,
                isDefault = false
            )
            repository.createProfile(profile)
            _uiState.update { it.copy(showCreateDialog = false) }
        }
    }

    fun selectProfile(profile: GameProfile) {
        viewModelScope.launch {
            val db = com.bedrock.launcher.data.local.LauncherDatabase.getInstance(getApplication())
            db.profileDao().setDefaultProfile(profile.id)
            if (profile.targetVersionId > 0) {
                db.versionDao().setActiveVersion(profile.targetVersionId)
            }
        }
    }

    fun deleteProfile(profile: GameProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun openEditProfile(profile: GameProfile) {
        _uiState.update { it.copy(editingProfile = profile) }
    }

    fun closeEditProfile() {
        _uiState.update { it.copy(editingProfile = null) }
    }

    fun saveEditedProfile(updated: GameProfile) {
        viewModelScope.launch {
            repository.updateProfile(updated)
            _uiState.update { it.copy(editingProfile = null) }
        }
    }
}
