package com.bedrock.launcher.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.bedrock.launcher.installer.InstallerType
import com.bedrock.launcher.ui.components.GamerButton
import com.bedrock.launcher.ui.components.GlassCard
import com.bedrock.launcher.ui.components.SectionHeader
import com.bedrock.launcher.ui.components.StatusBadge
import com.bedrock.launcher.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.checkStatus()
    }

    Scaffold(
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SectionHeader(
                title = "Настройки лаунчера",
                subtitle = "Метод установки, права доступа и хранилище"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Method of installation
            Text(
                text = "Метод переключения версий",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            InstallerType.entries.forEach { installer ->
                val isSelected = state.selectedInstaller == installer
                GlassCard(
                    modifier = Modifier.padding(vertical = 4.dp),
                    borderColor = if (isSelected) EmeraldPrimary else CardBorder,
                    backgroundColor = if (isSelected) SurfaceVariantDark else CardDark,
                    onClick = { viewModel.setInstallerType(installer) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setInstallerType(installer) },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = installer.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = installer.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Shizuku Status Section
            SectionHeader(
                title = "Служба Shizuku (Без Root)",
                subtitle = "Позволяет менять версии Bedrock в 1 клик"
            )

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Статус подключения",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.isShizukuAvailable) "Активна и доступна" else "Не подключена или нет прав",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.isShizukuAvailable) EmeraldLight else AccentRed
                        )
                    }

                    StatusBadge(
                        text = if (state.isShizukuAvailable) "Готово" else "Отключено",
                        color = if (state.isShizukuAvailable) EmeraldPrimary else AccentRed
                    )
                }

                if (!state.isShizukuAvailable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.requestShizukuPermission() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                        border = BorderStroke(1.dp, EmeraldPrimary)
                    ) {
                        Text("Запросить права Shizuku")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Storage Section
            SectionHeader(
                title = "Папки игры",
                subtitle = "Путь к игровым данным com.mojang"
            )

            GlassCard {
                Text(
                    text = "Каталог данных Minecraft:",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.storagePath.ifEmpty { "Android/data/com.mojang.minecraftpe/files/games/com.mojang" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
