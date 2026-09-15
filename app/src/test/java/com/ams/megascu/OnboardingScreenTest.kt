package com.ams.megascu

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.ams.megascu.ui.components.OnboardingScreen
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnboardingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun onboardingScreen_rendersWithoutException() {
        composeTestRule.setContent {
            MegasTheme {
                OnboardingScreen(
                    onRequestPermissions = {},
                    hasPhonePermission = true,
                    hasNotificationPermission = true,
                    onFinish = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun onboardingScreen_inDarkTheme_rendersWithoutException() {
        composeTestRule.setContent {
            MegasTheme(darkTheme = true) {
                OnboardingScreen(
                    onRequestPermissions = {},
                    hasPhonePermission = false,
                    hasNotificationPermission = false,
                    onFinish = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }
}
