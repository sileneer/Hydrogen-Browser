package com.sileneer.hydrogenbrowser.ui.language

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.sileneer.hydrogenbrowser.ui.theme.HydrogenBrowserTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LanguageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allLanguageOptionsDisplayed() {
        composeTestRule.setContent {
            HydrogenBrowserTheme {
                LanguageScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Follow System").assertIsDisplayed()
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
        composeTestRule.onNodeWithText("简体中文").assertIsDisplayed()
    }

    @Test
    fun languageTitleDisplayed() {
        composeTestRule.setContent {
            HydrogenBrowserTheme {
                LanguageScreen(onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Language").assertIsDisplayed()
    }
}
