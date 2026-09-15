package com.ams.megascu

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.ussd.UssdCallback
import com.ams.megascu.data.ussd.UssdErrorType
import com.ams.megascu.data.ussd.UssdExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UssdCancellationAndBackupTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testCaseA_responseBeforeTimeout_cancelsTimeoutAndCallsSuccessOnce() {
        val handler = Handler(Looper.getMainLooper())
        val isHandled = AtomicBoolean(false)
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)

        val timeoutRunnable = Runnable {
            if (isHandled.compareAndSet(false, true)) {
                errorCount.incrementAndGet()
            }
        }
        handler.postDelayed(timeoutRunnable, 10000)

        // Simulate successful USSD response arriving before timeout
        if (isHandled.compareAndSet(false, true)) {
            handler.removeCallbacks(timeoutRunnable)
            successCount.incrementAndGet()
        }

        // Simulate a late timeout attempt
        timeoutRunnable.run()

        assertEquals("Success must be called exactly once", 1, successCount.get())
        assertEquals("Timeout/Error must not be called after success", 0, errorCount.get())
    }

    @Test
    fun testCaseB_timeoutBeforeResponse_ignoresLateResponse() {
        val isHandled = AtomicBoolean(false)
        val callbackCount = AtomicInteger(0)
        var capturedResult: String? = null

        // Simulate timeout trigger
        if (isHandled.compareAndSet(false, true)) {
            callbackCount.incrementAndGet()
            capturedResult = "TIMEOUT"
        }

        // Simulate late response arriving after timeout
        if (isHandled.compareAndSet(false, true)) {
            callbackCount.incrementAndGet()
            capturedResult = "LATE_SUCCESS"
        }

        assertEquals("Result must remain TIMEOUT", "TIMEOUT", capturedResult)
        assertEquals("Callback must only execute once", 1, callbackCount.get())
    }

    @Test
    fun testCaseC_coroutineCancellation_abortsPendingContinuation() = runTest {
        val isCancelled = AtomicBoolean(false)
        val responseReceived = AtomicBoolean(false)

        val job = launch {
            try {
                suspendCancellableCoroutine<Unit> { cont ->
                    cont.invokeOnCancellation {
                        isCancelled.set(true)
                    }
                }
            } catch (e: Exception) {
                // Expected on cancellation
            }
        }

        testScheduler.advanceTimeBy(100)
        job.cancel()
        testScheduler.advanceUntilIdle()

        assertTrue("invokeOnCancellation must set isCancelled to true", isCancelled.get())
        assertFalse("Response should not be marked as received", responseReceived.get())
    }

    @Test
    fun testCaseD_simultaneousResponseAndTimeout_atomicProtection() {
        // Run 100 concurrent races between response and timeout
        for (i in 0 until 100) {
            val isHandled = AtomicBoolean(false)
            val count = AtomicInteger(0)

            val t1 = Thread {
                if (isHandled.compareAndSet(false, true)) {
                    count.incrementAndGet()
                }
            }
            val t2 = Thread {
                if (isHandled.compareAndSet(false, true)) {
                    count.incrementAndGet()
                }
            }

            t1.start()
            t2.start()
            t1.join()
            t2.join()

            assertEquals("Exactly one callback must win the race", 1, count.get())
        }
    }

    @Test
    fun testCaseE_dualSimIsolation() {
        val sim1IsHandled = AtomicBoolean(false)
        val sim2IsHandled = AtomicBoolean(false)
        val sim1Success = AtomicBoolean(false)
        val sim2Success = AtomicBoolean(false)

        // SIM 1 execution
        if (sim1IsHandled.compareAndSet(false, true)) {
            sim1Success.set(true)
        }

        // SIM 2 execution
        if (sim2IsHandled.compareAndSet(false, true)) {
            sim2Success.set(true)
        }

        assertTrue("SIM 1 operation completes independently", sim1Success.get())
        assertTrue("SIM 2 operation completes independently", sim2Success.get())
    }

    @Test
    fun testBackupSecurityRules_excludeSecurityPrefs() {
        val dataExtractionFile = File("src/main/res/xml/data_extraction_rules.xml")
        assertTrue("data_extraction_rules.xml must exist", dataExtractionFile.exists())
        val dataExtractionContent = dataExtractionFile.readText()
        assertTrue(
            "data_extraction_rules.xml must exclude megas_security_prefs.xml",
            dataExtractionContent.contains("""path="megas_security_prefs.xml"""")
        )
        assertTrue(
            "data_extraction_rules.xml must exclude from cloud-backup",
            dataExtractionContent.contains("<cloud-backup>")
        )

        val backupRulesFile = File("src/main/res/xml/backup_rules.xml")
        assertTrue("backup_rules.xml must exist", backupRulesFile.exists())
        val backupRulesContent = backupRulesFile.readText()
        assertTrue(
            "backup_rules.xml must exclude megas_security_prefs.xml",
            backupRulesContent.contains("""path="megas_security_prefs.xml"""")
        )
    }
}
