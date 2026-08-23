package com.bedrock.launcher.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bedrock.launcher.ui.screens.home.HomeScreen
import com.bedrock.launcher.ui.screens.mods.ModsScreen
import com.bedrock.launcher.ui.screens.profiles.ProfilesScreen
import com.bedrock.launcher.ui.screens.settings.SettingsScreen
import com.bedrock.launcher.ui.screens.versions.VersionsScreen
import com.bedrock.launcher.ui.theme.EmeraldPrimary
import com.bedrock.launcher.ui.theme.SurfaceDark
import com.bedrock.launcher.ui.theme.TextMuted

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                bottomNavScreens.forEach { screen ->
                    val selected = currentDestination?.route == screen.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToVersions = {
                        navController.navigate(Screen.Versions.route)
                    },
                    onNavigateToProfiles = {
                        navController.navigate(Screen.Profiles.route)
                    }
                )
            }
            composable(Screen.Versions.route) {
                VersionsScreen()
            }
            composable(Screen.Profiles.route) {
                ProfilesScreen()
            }
            composable(Screen.Mods.route) {
                ModsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
