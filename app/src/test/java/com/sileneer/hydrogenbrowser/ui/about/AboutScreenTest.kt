package com.sileneer.hydrogenbrowser.ui.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sileneer.hydrogenbrowser.ui.theme.HydrogenBrowserTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AboutScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appNameDisplayed() {
        composeTestRule.setContent {
            HydrogenBrowserTheme {
                AboutScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Hydrogen Browser").assertIsDisplayed()
    }

    @Test
    fun versionTextDisplayed() {
        composeTestRule.setContent {
            HydrogenBrowserTheme {
                AboutScreen(onBack = {})
            }
        }

        // Version format includes "Version:" prefix
        composeTestRule.onNodeWithText("Version:", substring = true).assertIsDisplayed()
    }

    @Test
    fun copyrightDisplayed() {
        composeTestRule.setContent {
            HydrogenBrowserTheme {
                AboutScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Copyright", substring = true).assertIsDisplayed()
    }

    @Test
    fun aboutTitleDisplayed() {
        composeTestRule.setContent {
            HydrogenBrowserTheme {
                AboutScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("About").assertIsDisplayed()
    }
}
