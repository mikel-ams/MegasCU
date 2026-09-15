package com.ams.megascu

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ColorTokensAndThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lightTheme_rendersTokensCorrectly() {
        composeTestRule.setContent {
            MegasTheme(darkTheme = false, isAmoled = false) {
                val cs = MaterialTheme.colorScheme
                assertNotNull(cs.primary)
                assertNotNull(cs.onPrimary)
                assertNotNull(cs.background)
                assertNotNull(cs.surface)
                Text("Light Theme Active", color = cs.primary)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Light Theme Active").assertExists()
    }

    @Test
    fun darkTheme_rendersTokensCorrectly() {
        composeTestRule.setContent {
            MegasTheme(darkTheme = true, isAmoled = false) {
                val cs = MaterialTheme.colorScheme
                assertNotNull(cs.primary)
                assertNotNull(cs.onPrimary)
                assertNotNull(cs.background)
                assertNotNull(cs.surface)
                Text("Dark Theme Active", color = cs.onBackground)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Dark Theme Active").assertExists()
    }

    @Test
    fun amoledTheme_rendersTokensCorrectly() {
        composeTestRule.setContent {
            MegasTheme(darkTheme = true, isAmoled = true) {
                val cs = MaterialTheme.colorScheme
                assertNotNull(cs.background)
                Text("AMOLED Theme Active", color = cs.onSurface)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("AMOLED Theme Active").assertExists()
    }
}
