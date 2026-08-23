package com.bedrock.launcher.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedrock.launcher.ui.components.GamerButton
import com.bedrock.launcher.ui.components.GlassCard
import com.bedrock.launcher.ui.components.SectionHeader
import com.bedrock.launcher.ui.components.StatusBadge
import com.bedrock.launcher.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToVersions: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Bedrock Launcher",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Клиент с быстрой сменой версий",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SurfaceVariantDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideogameAsset,
                        contentDescription = null,
                        tint = EmeraldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Play Card with Gradient
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E3A2F),
                                    Color(0xFF151922)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "АКТИВНАЯ ВЕРСИЯ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = state.activeVersion?.versionName ?: "Версия не выбрана",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            if (state.activeVersion != null) {
                                StatusBadge(
                                    text = state.activeVersion?.architecture ?: "arm64",
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Профиль: ${state.activeProfile?.name ?: "По умолчанию"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (state.isSwitching) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = EmeraldPrimary,
                                    trackColor = SurfaceVariantDark
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.statusMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldLight
                                )
                            }
                        } else {
                            GamerButton(
                                text = "ИГРАТЬ",
                                icon = Icons.Default.PlayArrow,
                                onClick = { viewModel.switchAndLaunch() },
                                containerColor = EmeraldPrimary,
                                contentColor = BackgroundDark
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Switch Section
            SectionHeader(
                title = "Быстрое переключение",
                subtitle = "Управление версиями и профилями"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToVersions
                ) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Сменить версию",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Каталог APK файлов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToProfiles
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageAccounts,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Профили",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Миры и настройки",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Performance Tweaks (Options.txt)
            SectionHeader(
                title = "Оптимизация FPS & Графика",
                subtitle = "Быстрая подстройка под ваше устройство"
            )

            GlassCard {
                Text(
                    text = "Лимит FPS (options.txt)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val currentFps = state.activeProfile?.maxFps ?: 120
                    listOf(60, 90, 120, 0).forEach { fps ->
                        val isSelected = currentFps == fps
                        OutlinedButton(
                            onClick = { viewModel.updateProfileFps(fps) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Color.Transparent,
                                contentColor = if (isSelected) EmeraldLight else TextSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else CardBorder
                            )
                        ) {
                            Text(
                                text = if (fps == 0) "MAX" else "$fps",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Дальность прорисовки: ${state.activeProfile?.renderDistanceChunks ?: 12} чанков",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Slider(
                    value = (state.activeProfile?.renderDistanceChunks ?: 12).toFloat(),
                    onValueChange = { viewModel.updateProfileChunks(it.toInt()) },
                    valueRange = 6f..32f,
                    steps = 12,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimary,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = SurfaceVariantDark
                    )
                )
            }
        }
    }
}
