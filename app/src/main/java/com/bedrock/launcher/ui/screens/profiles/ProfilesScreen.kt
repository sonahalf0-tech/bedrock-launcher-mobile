package com.bedrock.launcher.ui.screens.profiles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedrock.launcher.domain.model.GameProfile
import com.bedrock.launcher.ui.components.GamerButton
import com.bedrock.launcher.ui.components.GlassCard
import com.bedrock.launcher.ui.components.SectionHeader
import com.bedrock.launcher.ui.components.StatusBadge
import com.bedrock.launcher.ui.theme.*

@Composable
fun ProfilesScreen(
    viewModel: ProfilesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openCreateDialog() },
                containerColor = EmeraldPrimary,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Новый профиль", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SectionHeader(
                title = "Игровые профили",
                subtitle = "Каждый профиль имеет свои изолированные миры и настройки"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.profiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет созданных профилей",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.profiles, key = { it.id }) { profile ->
                        val isDefault = state.activeProfile?.id == profile.id
                        val targetVer = state.versions.find { it.id == profile.targetVersionId }
                        ProfileCard(
                            profile = profile,
                            versionName = targetVer?.versionName ?: "Любая",
                            isActive = isDefault,
                            onSelect = { viewModel.selectProfile(profile) },
                            onEdit = { viewModel.openEditProfile(profile) },
                            onDelete = { viewModel.deleteProfile(profile) }
                        )
                    }
                }
            }
        }
    }

    if (state.showCreateDialog) {
        CreateProfileDialog(
            versions = state.versions,
            onDismiss = { viewModel.closeCreateDialog() },
            onCreate = { name, verId, fps, chunks ->
                viewModel.createProfile(name, verId, fps, chunks)
            }
        )
    }

    if (state.editingProfile != null) {
        EditProfileDialog(
            profile = state.editingProfile!!,
            onDismiss = { viewModel.closeEditProfile() },
            onSave = { viewModel.saveEditedProfile(it) }
        )
    }
}

@Composable
fun ProfileCard(
    profile: GameProfile,
    versionName: String,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        borderColor = if (isActive) AccentGold else CardBorder,
        backgroundColor = if (isActive) SurfaceVariantDark else CardDark
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    if (isActive) {
                        StatusBadge(text = "Выбран", color = AccentGold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Версия: $versionName • FPS: ${if (profile.maxFps == 0) "MAX" else profile.maxFps} • Чанки: ${profile.renderDistanceChunks}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Настройки",
                        tint = TextSecondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить",
                        tint = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isActive) {
            OutlinedButton(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentGold
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
                Text(text = "Сделать активным", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CreateProfileDialog(
    versions: List<com.bedrock.launcher.domain.model.BedrockVersion>,
    onDismiss: () -> Unit,
    onCreate: (String, Long, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedVerId by remember { mutableStateOf(versions.firstOrNull()?.id ?: 0L) }
    var maxFps by remember { mutableIntStateOf(120) }
    var renderDistance by remember { mutableIntStateOf(12) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Новый профиль",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (например: Выживание 1.21)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Лимит FPS: ${if (maxFps == 0) "Без ограничений" else maxFps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Slider(
                    value = maxFps.toFloat(),
                    onValueChange = { maxFps = it.toInt() },
                    valueRange = 30f..144f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name, selectedVerId, maxFps, renderDistance)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Создать", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    profile: GameProfile,
    onDismiss: () -> Unit,
    onSave: (GameProfile) -> Unit
) {
    var maxFps by remember { mutableIntStateOf(profile.maxFps) }
    var renderDistance by remember { mutableIntStateOf(profile.renderDistanceChunks) }
    var fov by remember { mutableIntStateOf(profile.fov) }
    var vsync by remember { mutableStateOf(profile.enableVsync) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Настройки: ${profile.name}",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Макс. FPS: ${if (maxFps == 0) "MAX" else maxFps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Slider(
                    value = maxFps.toFloat(),
                    onValueChange = { maxFps = it.toInt() },
                    valueRange = 30f..144f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Дальность прорисовки: $renderDistance чанков",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Slider(
                    value = renderDistance.toFloat(),
                    onValueChange = { renderDistance = it.toInt() },
                    valueRange = 6f..32f,
                    steps = 12,
                    colors = SliderDefaults.colors(thumbColor = EmeraldPrimary, activeTrackColor = EmeraldPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Вертикальная синхронизация (VSync)", color = TextPrimary)
                    Switch(
                        checked = vsync,
                        onCheckedChange = { vsync = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                profile.copy(
                                    maxFps = maxFps,
                                    renderDistanceChunks = renderDistance,
                                    fov = fov,
                                    enableVsync = vsync
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Сохранить", color = BackgroundDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
