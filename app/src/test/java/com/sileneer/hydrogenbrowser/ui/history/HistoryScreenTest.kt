package com.sileneer.hydrogenbrowser.ui.history

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.sileneer.hydrogenbrowser.data.HydrogenDatabase
import com.sileneer.hydrogenbrowser.data.HistoryRepository
import com.sileneer.hydrogenbrowser.ui.theme.HydrogenBrowserTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createViewModel(): HistoryViewModel {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = HydrogenDatabase.getInstance(app)
        return HistoryViewModel(HistoryRepository(db.historyDao()))
    }

    @Test
    fun historyScreenDisplaysTitle() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                HistoryScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun emptyHistoryShowsMessage() {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HydrogenBrowserTheme {
                HistoryScreen(
                    viewModel = viewModel,
                    onBack = {},
                    onNavigate = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No browsing history").assertIsDisplayed()
    }
}
