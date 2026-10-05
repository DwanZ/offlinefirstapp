package com.insigniaempresarial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.insigniaempresarial.core.designsystem.theme.InsigniaTheme
import com.insigniaempresarial.feature.accounts.AccountDetailRoute
import com.insigniaempresarial.feature.accounts.AccountsRoute
import com.insigniaempresarial.feature.home.HomeRoute
import com.insigniaempresarial.feature.sync.SyncRoute
import com.insigniaempresarial.feature.transactions.TransactionsRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InsigniaTheme(darkTheme = true) {
                InsigniaAppNav()
            }
        }
    }
}

private data class TopLevelDest(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun InsigniaAppNav() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }
    val destinations = listOf(
        TopLevelDest("home", "Home", Icons.Default.Home),
        TopLevelDest("accounts", "Accounts", Icons.Default.AccountBalance),
        TopLevelDest("transactions", "Activity", Icons.Default.ReceiptLong),
        TopLevelDest("sync", "Sync", Icons.Default.Sync),
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = currentRoute in destinations.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    destinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("home") { HomeRoute(onMessage = showMessage) }
            composable("accounts") {
                AccountsRoute(
                    onAccountClick = { id -> navController.navigate("account/$id") },
                )
            }
            composable(
                route = "account/{accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.StringType }),
            ) {
                AccountDetailRoute(
                    onBack = { navController.popBackStack() },
                    onMessage = showMessage,
                )
            }
            composable("transactions") { TransactionsRoute(onMessage = showMessage) }
            composable("sync") { SyncRoute(onMessage = showMessage) }
        }
    }
}
