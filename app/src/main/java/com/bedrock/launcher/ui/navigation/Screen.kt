package com.bedrock.launcher.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Главная", Icons.Default.Gamepad)
    data object Versions : Screen("versions", "Версии", Icons.Default.ListAlt)
    data object Profiles : Screen("profiles", "Профили", Icons.Default.ManageAccounts)
    data object Mods : Screen("mods", "Моды & Паки", Icons.Default.Extension)
    data object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Versions,
    Screen.Profiles,
    Screen.Mods,
    Screen.Settings
)
