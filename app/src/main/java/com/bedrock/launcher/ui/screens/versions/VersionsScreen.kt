package com.bedrock.launcher.ui.screens.versions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedrock.launcher.domain.model.BedrockVersion
import com.bedrock.launcher.ui.components.GamerButton
import com.bedrock.launcher.ui.components.GlassCard
import com.bedrock.launcher.ui.components.SectionHeader
import com.bedrock.launcher.ui.components.StatusBadge
import com.bedrock.launcher.ui.theme.*

@Composable
fun VersionsScreen(
    viewModel: VersionsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importApkFromUri(it) }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { filePickerLauncher.launch("application/vnd.android.package-archive") },
                containerColor = EmeraldPrimary,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Импорт APK", fontWeight = FontWeight.Bold) }
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
                title = "Каталог версий",
                subtitle = "Установленные и импортированные версии Bedrock"
            )

            if (state.isImporting) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = state.importMessage ?: "Импорт версии...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.versions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нет добавленных версий",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Нажмите «Импорт APK», чтобы добавить любую версию Minecraft Bedrock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.versions, key = { it.id }) { version ->
                        val isActive = state.activeVersion?.id == version.id
                        VersionCard(
                            version = version,
                            isActive = isActive,
                            onSelect = { viewModel.selectVersion(version) },
                            onDelete = { viewModel.deleteVersion(version) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VersionCard(
    version: BedrockVersion,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        borderColor = if (isActive) EmeraldPrimary else CardBorder,
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
                        text = "Minecraft ${version.versionName}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    if (isActive) {
                        StatusBadge(text = "Активна", color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Архитектура: ${version.architecture}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Размер: ${version.fileSizeMb} МБ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить",
                    tint = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!isActive) {
            OutlinedButton(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmeraldLight
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
            ) {
                Text(text = "Выбрать для запуска", fontWeight = FontWeight.Bold)
            }
        }
    }
}
