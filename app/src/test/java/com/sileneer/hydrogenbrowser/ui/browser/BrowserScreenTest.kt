package com.sileneer.hydrogenbrowser.ui.browser

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.sileneer.hydrogenbrowser.ui.theme.HydrogenBrowserTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BrowserScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): BrowserViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        return BrowserViewModel(app)
    }

    @Test
    fun tabCountButtonShowsCorrectCount() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                BrowserScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }

    @Test
    fun tabCountUpdatesAfterAddingTab() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                BrowserScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {}
                )
            }
        }

        viewModel.addTab()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }
}
