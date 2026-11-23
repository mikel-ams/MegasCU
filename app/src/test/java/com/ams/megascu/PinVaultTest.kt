package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.security.PinVault
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinVaultTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun emptyVerifierNeverAuthenticates() {
        assertFalse(PinVault.verify(context, "1234", ""))
    }

    @Test
    fun legacyPinRequiresExactMatch() {
        assertTrue(PinVault.verify(context, "1234", "1234"))
        assertFalse(PinVault.verify(context, "3333", "1234"))
    }
}
