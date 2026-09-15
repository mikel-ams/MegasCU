package com.ams.megascu

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import com.ams.megascu.ui.components.GuideBottomSheet
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GuideBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun guideBottomSheet_rendersWithoutException() {
        var dismissed = false
        composeTestRule.setContent {
            MegasTheme {
                GuideBottomSheet(
                    onDismiss = { dismissed = true }
                )
            }
        }
        composeTestRule.waitForIdle()
        assertTrue(composeTestRule.onAllNodes(isRoot()).fetchSemanticsNodes().isNotEmpty())
    }
}
