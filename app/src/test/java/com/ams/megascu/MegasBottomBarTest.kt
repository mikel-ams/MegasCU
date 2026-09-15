package com.ams.megascu

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.ams.megascu.ui.components.MegasBottomBar
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MegasBottomBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun megasBottomBar_rendersWithoutException() {
        composeTestRule.setContent {
            MegasTheme {
                MegasBottomBar(
                    onOpenPlanes = {},
                    onOpenGuide = {},
                    onShowAbout = {},
                    onOpenSettings = {},
                    onRefresh = {},
                    isRefreshing = false
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun megasBottomBar_refreshingState_rendersWithoutException() {
        composeTestRule.setContent {
            MegasTheme {
                MegasBottomBar(
                    onOpenPlanes = {},
                    isRefreshing = true,
                    useWavyProgress = true
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }
}
