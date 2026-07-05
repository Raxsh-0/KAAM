package com.kaam.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.kaam.app.SessionViewModel
import com.kindred.feature.auth.AuthScreen
import com.kindred.feature.chat.ChatScreen
import com.kindred.feature.chat.MatchesScreen
import com.kindred.feature.discovery.DiscoveryScreen
import com.kindred.feature.profile.ProfileScreen

object Routes {
    const val AUTH = "auth"
    const val DISCOVERY = "discovery"
    const val MATCHES = "matches"
    const val PROFILE = "profile"
    const val CHAT = "chat/{profileId}"

    fun chat(profileId: String) = "chat/$profileId"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.DISCOVERY, "Discover", Icons.Filled.Search),
    BottomTab(Routes.MATCHES, "Matches", Icons.Filled.Favorite),
    BottomTab(Routes.PROFILE, "Profile", Icons.Filled.Person),
)

@Composable
fun KindredNavRoot(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (sessionViewModel.startSignedIn) Routes.DISCOVERY else Routes.AUTH,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.AUTH) {
                AuthScreen(
                    onSignedIn = {
                        navController.navigate(Routes.DISCOVERY) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.DISCOVERY) {
                DiscoveryScreen(onOpenChat = { navController.navigate(Routes.chat(it)) })
            }
            composable(Routes.MATCHES) {
                MatchesScreen(onOpenChat = { navController.navigate(Routes.chat(it)) })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSignedOut = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.CHAT) {
                ChatScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
