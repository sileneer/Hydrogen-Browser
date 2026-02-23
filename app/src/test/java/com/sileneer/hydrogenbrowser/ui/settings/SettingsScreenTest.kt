package com.sileneer.hydrogenbrowser.ui.settings

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.sileneer.hydrogenbrowser.ui.browser.BrowserViewModel
import com.sileneer.hydrogenbrowser.ui.theme.HydrogenBrowserTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): BrowserViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        return BrowserViewModel(app)
    }

    @Test
    fun allSettingsItemsDisplayed() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                SettingsScreen(
                    navController = rememberNavController(),
                    browserViewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Search Engine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Homepage").assertIsDisplayed()
        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
        composeTestRule.onNodeWithText("About").assertIsDisplayed()
    }

    @Test
    fun searchEngineDialogOpensOnClick() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                SettingsScreen(
                    navController = rememberNavController(),
                    browserViewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Search Engine").performClick()
        composeTestRule.onNodeWithText("Please select your search engine:").assertIsDisplayed()
    }

    @Test
    fun homepageDialogOpensOnClick() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                SettingsScreen(
                    navController = rememberNavController(),
                    browserViewModel = viewModel
                )
            }
        }

        composeTestRule.onNodeWithText("Homepage").performClick()
        composeTestRule.onNodeWithText("Input new homepage url below:").assertIsDisplayed()
    }
}
