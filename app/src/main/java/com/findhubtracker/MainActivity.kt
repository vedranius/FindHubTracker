package com.findhubtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.findhubtracker.ui.components.PermissionHandler
import com.findhubtracker.ui.screens.*
import com.findhubtracker.ui.theme.FindHubTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FindHubTrackerTheme {
                PermissionHandler {
                    FindHubTrackerApp()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Trackeri", Icons.Default.Home)
    data object Map : Screen("map", "Mapa", Icons.Default.Map)
    data object Geofence : Screen("geofence", "Geofence", Icons.Default.LocationOn)
    data object Settings : Screen("settings", "Postavke", Icons.Default.Settings)
    data object TrackerDetail : Screen("tracker/{address}", "Detalji", Icons.Default.Info)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Map,
    Screen.Geofence,
    Screen.Settings
)

@Composable
fun FindHubTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onTrackerClick = { address ->
                        navController.navigate("tracker/$address")
                    }
                )
            }

            composable(Screen.Map.route) {
                MapScreen()
            }

            composable(Screen.Geofence.route) {
                GeofenceScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            composable(
                route = Screen.TrackerDetail.route,
                arguments = listOf(
                    navArgument("address") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val address = backStackEntry.arguments?.getString("address") ?: ""
                TrackerDetailScreen(
                    trackerAddress = address,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
