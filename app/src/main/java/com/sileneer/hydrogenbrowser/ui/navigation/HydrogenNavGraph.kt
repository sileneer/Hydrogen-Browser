package com.sileneer.hydrogenbrowser.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sileneer.hydrogenbrowser.ui.about.AboutScreen
import com.sileneer.hydrogenbrowser.ui.browser.BrowserScreen
import com.sileneer.hydrogenbrowser.ui.browser.BrowserViewModel
import com.sileneer.hydrogenbrowser.ui.language.LanguageScreen
import com.sileneer.hydrogenbrowser.ui.settings.SettingsScreen

object Routes {
    const val BROWSER = "browser"
    const val SETTINGS = "settings"
    const val LANGUAGE = "language"
    const val ABOUT = "about"
}

@Composable
fun HydrogenNavGraph() {
    val navController = rememberNavController()
    // ViewModel scoped to activity so it survives navigation
    val browserViewModel: BrowserViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.BROWSER) {
        composable(Routes.BROWSER) {
            BrowserScreen(
                viewModel = browserViewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                navController = navController,
                browserViewModel = browserViewModel
            )
        }
        composable(Routes.LANGUAGE) {
            LanguageScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
