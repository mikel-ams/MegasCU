package com.ams.megascu

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.ams.megascu.ui.components.CircularWavyProgressIndicator
import com.ams.megascu.ui.components.LinearWavyProgressIndicator
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WavyProgressIndicatorsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun linearWavyProgressIndicator_rendersWithoutException() {
        composeTestRule.setContent {
            MegasTheme {
                LinearWavyProgressIndicator()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun circularWavyProgressIndicator_rendersWithoutException() {
        composeTestRule.setContent {
            MegasTheme {
                CircularWavyProgressIndicator(
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }
}
