package com.example.overdrivexandroid

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.overdrivexandroid.core.network.NetworkMonitor
import com.example.overdrivexandroid.core.ui.OfflineScreen
import com.example.overdrivexandroid.feature_friends.ui.FriendsScreen
import com.example.overdrivexandroid.feature_home.ui.DashboardScreen
import com.example.overdrivexandroid.feature_matchmaking.ui.MatchmakingScreen
import com.example.overdrivexandroid.feature_characters.ui.CharactersScreen
import com.example.overdrivexandroid.feature_stats.ui.StatsScreen

private const val ROUTE_DASHBOARD = "dashboard"
private const val ROUTE_FRIENDS = "friends"
private const val ROUTE_MATCHMAKING = "matchmaking"
private const val ROUTE_CHARACTERS = "characters"
private const val ROUTE_TOURNAMENTS = "tournaments"
private const val ROUTE_STATS = "stats"

@Composable
fun MainAppScreen(userId: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()
    val isMatchmaking = currentRoute?.destination?.route == ROUTE_MATCHMAKING

    Scaffold(
        bottomBar = {
            if (!isMatchmaking) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROUTE_DASHBOARD) {
                DashboardScreen(
                    userId = userId,
                    onLogout = onLogout,
                    onNavigateToMatchmaking = {
                        navController.navigate(ROUTE_MATCHMAKING)
                    }
                )
            }
            composable(ROUTE_FRIENDS) {
                FriendsScreen()
            }
            composable(ROUTE_MATCHMAKING) {
                MatchmakingScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(ROUTE_CHARACTERS) {
                CharactersScreen()
            }
            composable(ROUTE_TOURNAMENTS) {
                val context = LocalContext.current
                if (!NetworkMonitor.isOnline(context)) {
                    OfflineScreen()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "TORNEOS",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Esta función estará disponible en un futuro. Actualmente se encuentra en desarrollo.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
            composable(ROUTE_STATS) {
                StatsScreen(userId = userId)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        ROUTE_DASHBOARD to Icons.Default.Home,
        ROUTE_FRIENDS to Icons.Default.Group,
        ROUTE_CHARACTERS to Icons.Default.Person,
        ROUTE_STATS to Icons.Default.BarChart,
        ROUTE_TOURNAMENTS to Icons.Default.EmojiEvents
    )
    val labels = listOf("Inicio", "Amigos", "Personajes", "Stats", "Torneos")

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEachIndexed { index, pair ->
            val route = pair.first
            val icon = pair.second
            val label = labels[index]

            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
