package com.ams.megascu

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ams.megascu.ui.components.*
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChangelogCollapseAnimationTest {

    @get:Rule val compose = createComposeRule()

    @Test fun collapsingCardMovesNextCardThroughIntermediatePositions() {
        val item = ChangelogVersion("test-version", "2026-10-04", listOf(
            ChangelogSection(ChangeCategory.FIXED, listOf("Consulta: Mensaje de error", "Historial: Registro coherente", "Widgets: Actualización real"))
        ))
        compose.setContent { MegasTheme { Column { PreviousChangelogCard(item); Text("Tarjeta siguiente") } } }

        fun nextY() = compose.onNodeWithText("Tarjeta siguiente").fetchSemanticsNode().boundsInRoot.top

        val collapsed = nextY()
        compose.onNodeWithText("vtest-version").performClick()
        compose.waitForIdle()
        val expanded = nextY()
        assertTrue(expanded > collapsed)

        compose.onNodeWithText("vtest-version").performClick()
        compose.waitForIdle()
        val finalCollapsed = nextY()
        assertEquals(collapsed, finalCollapsed, 1f)
    }
}
