package com.ams.megascu

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.ams.megascu.ui.components.AuthScreen
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authScreen_emptyPin_triggersAuthSuccess() {
        var success = false
        composeTestRule.setContent {
            MegasTheme {
                AuthScreen(
                    correctPin = "",
                    biometricsEnabled = false,
                    onAuthSuccess = { success = true }
                )
            }
        }
        composeTestRule.waitForIdle()
        assertTrue("Empty PIN should trigger auth success immediately", success)
    }

    @Test
    fun authScreen_withPin_rendersKeypadAndAcceptsInput() {
        var success = false
        composeTestRule.setContent {
            MegasTheme {
                AuthScreen(
                    correctPin = "1234",
                    biometricsEnabled = false,
                    onAuthSuccess = { success = true }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()

        // Keypad buttons 1, 2, 3, 4
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.onNodeWithText("2").performClick()
        composeTestRule.onNodeWithText("3").performClick()
        composeTestRule.onNodeWithText("4").performClick()
        composeTestRule.waitForIdle()

        assertTrue("Entering correct PIN should trigger auth success", success)
    }
}
