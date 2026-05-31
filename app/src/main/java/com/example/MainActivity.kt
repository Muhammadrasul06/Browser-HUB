package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.BrowserViewModel
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TabsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.AiToolsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    // Shared ViewModel across screen navigations to allow simple settings synchronization
    val viewModel: BrowserViewModel = viewModel(
        factory = BrowserViewModel.Factory(
            application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
        )
    )

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToBrowser = { url ->
                    viewModel.openUrlInCurrentTab(url)
                    val encoded = Uri.encode(url)
                    navController.navigate("browser?url=$encoded")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToTabs = {
                    navController.navigate("tabs")
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToBookmarks = {
                    navController.navigate("bookmarks")
                },
                onNavigateToDownloads = {
                    navController.navigate("downloads")
                },
                onNavigateToAiTools = {
                    navController.navigate("ai_tools")
                }
            )
        }

        composable(
            route = "browser?url={url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = "home"
                }
            )
        ) { backStackEntry ->
            val rawUrl = backStackEntry.arguments?.getString("url") ?: "home"
            val decodedUrl = Uri.decode(rawUrl)
            
            BrowserScreen(
                initialUrl = decodedUrl,
                viewModel = viewModel,
                onNavigateHome = {
                    navController.popBackStack("home", inclusive = false)
                },
                onNavigateToTabs = { navController.navigate("tabs") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToBookmarks = { navController.navigate("bookmarks") },
                onNavigateToDownloads = { navController.navigate("downloads") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("tabs") {
            TabsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBrowser = { url ->
                    viewModel.openUrlInCurrentTab(url)
                    val encoded = Uri.encode(url)
                    navController.navigate("browser?url=$encoded") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    viewModel.openUrlInCurrentTab(url)
                    val encoded = Uri.encode(url)
                    navController.navigate("browser?url=$encoded") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("bookmarks") {
            BookmarksScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    viewModel.openUrlInCurrentTab(url)
                    val encoded = Uri.encode(url)
                    navController.navigate("browser?url=$encoded") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("downloads") {
            DownloadsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("ai_tools") {
            AiToolsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenUrl = { url ->
                    viewModel.openUrlInCurrentTab(url)
                    val encoded = Uri.encode(url)
                    navController.navigate("browser?url=$encoded") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
