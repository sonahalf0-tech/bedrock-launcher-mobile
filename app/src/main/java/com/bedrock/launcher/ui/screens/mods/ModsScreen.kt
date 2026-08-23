package com.bedrock.launcher.ui.screens.mods

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedrock.launcher.domain.model.AddonType
import com.bedrock.launcher.domain.model.BedrockAddon
import com.bedrock.launcher.ui.components.GlassCard
import com.bedrock.launcher.ui.components.SectionHeader
import com.bedrock.launcher.ui.components.StatusBadge
import com.bedrock.launcher.ui.theme.*

@Composable
fun ModsScreen(
    viewModel: ModsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importAddonFromUri(it) }
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
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = EmeraldPrimary,
                contentColor = BackgroundDark,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Импорт .mcpack / .mcaddon", fontWeight = FontWeight.Bold) }
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
                title = "Моды, Текстуры и Миры",
                subtitle = "Поддержка .mcpack, .mcaddon, .mcworld"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = when (state.selectedTab) {
                    null -> 0
                    AddonType.RESOURCE_PACK -> 1
                    AddonType.BEHAVIOR_PACK -> 2
                    AddonType.WORLD -> 3
                    AddonType.ADDON_BUNDLE -> 4
                },
                containerColor = SurfaceDark,
                contentColor = EmeraldPrimary,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = state.selectedTab == null,
                    onClick = { viewModel.selectTab(null) },
                    text = { Text("Все") }
                )
                Tab(
                    selected = state.selectedTab == AddonType.RESOURCE_PACK,
                    onClick = { viewModel.selectTab(AddonType.RESOURCE_PACK) },
                    text = { Text("Текстуры") }
                )
                Tab(
                    selected = state.selectedTab == AddonType.BEHAVIOR_PACK,
                    onClick = { viewModel.selectTab(AddonType.BEHAVIOR_PACK) },
                    text = { Text("Поведения") }
                )
                Tab(
                    selected = state.selectedTab == AddonType.WORLD,
                    onClick = { viewModel.selectTab(AddonType.WORLD) },
                    text = { Text("Миры") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val filteredAddons = if (state.selectedTab == null) {
                state.addons
            } else {
                state.addons.filter { it.type == state.selectedTab }
            }

            if (filteredAddons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Нет установленных пакетов",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAddons, key = { it.id }) { addon ->
                        AddonCard(
                            addon = addon,
                            onToggle = { viewModel.toggleAddon(addon, it) },
                            onDelete = { viewModel.deleteAddon(addon) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddonCard(
    addon: BedrockAddon,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = addon.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(
                        text = when (addon.type) {
                            AddonType.RESOURCE_PACK -> "Ресурс-пак"
                            AddonType.BEHAVIOR_PACK -> "Поведение"
                            AddonType.WORLD -> "Карта"
                            AddonType.ADDON_BUNDLE -> "Аддон"
                        },
                        color = when (addon.type) {
                            AddonType.RESOURCE_PACK -> EmeraldPrimary
                            AddonType.BEHAVIOR_PACK -> AccentBlue
                            AddonType.WORLD -> AccentGold
                            AddonType.ADDON_BUNDLE -> AccentPurple
                        }
                    )
                }

                if (addon.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = addon.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        fontSize = 12.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = addon.isEnabledForDefaultProfile,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить",
                        tint = TextMuted
                    )
                }
            }
        }
    }
}
