package com.sileneer.hydrogenbrowser.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sileneer.hydrogenbrowser.ui.about.AboutScreen
import com.sileneer.hydrogenbrowser.ui.browser.BrowserScreen
import com.sileneer.hydrogenbrowser.ui.browser.BrowserViewModel
import com.sileneer.hydrogenbrowser.ui.history.HistoryScreen
import com.sileneer.hydrogenbrowser.ui.history.HistoryViewModel
import com.sileneer.hydrogenbrowser.ui.language.LanguageScreen
import com.sileneer.hydrogenbrowser.ui.settings.SettingsScreen

object Routes {
    const val BROWSER = "browser"
    const val SETTINGS = "settings"
    const val LANGUAGE = "language"
    const val ABOUT = "about"
    const val HISTORY = "history"
}

@Composable
fun HydrogenNavGraph() {
    val navController = rememberNavController()
    // ViewModel scoped to activity so it survives navigation
    val browserViewModel: BrowserViewModel = viewModel()

    val slideDuration = 300

    NavHost(
        navController = navController,
        startDestination = Routes.BROWSER,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(slideDuration))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(slideDuration))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(slideDuration))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(slideDuration))
        }
    ) {
        composable(Routes.BROWSER) {
            BrowserScreen(
                viewModel = browserViewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) }
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
        composable(Routes.HISTORY) {
            val historyViewModel = remember {
                HistoryViewModel(browserViewModel.getHistoryRepository())
            }
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() },
                onNavigate = { url ->
                    navController.popBackStack(Routes.BROWSER, inclusive = false)
                    browserViewModel.getActiveWebView()?.loadUrl(url)
                }
            )
        }
    }
}
