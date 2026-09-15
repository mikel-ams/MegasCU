package com.ams.megascu

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.ui.components.PriorityStatusCard
import com.ams.megascu.ui.components.computeSimStatusState
import com.ams.megascu.ui.theme.MegasTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSubscriptionManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PriorityStatusCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun priorityStatusCard_nullPlan_rendersWithoutCrash() {
        composeTestRule.setContent {
            MegasTheme {
                PriorityStatusCard(
                    planStatus = null,
                    onSelectSimSlot = {},
                    onExecuteConsulta = { _, _ -> },
                    onCardClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun priorityStatusCard_withPlan_rendersWithoutCrash() {
        val dummyPlan = PlanStatusEntity(
            id = 1,
            balanceCup = 150.0,
            dataMb = 2500,
            dataLteMb = 1200,
            bonusDataMb = 300,
            minutesStr = "45 min",
            smsCount = 120,
            dataDays = 15,
            minutesDays = 10,
            smsDays = 10,
            dataExpirationTimestamp = System.currentTimeMillis() + 86400000L * 15,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )

        composeTestRule.setContent {
            MegasTheme {
                PriorityStatusCard(
                    planStatus = dummyPlan,
                    selectedSimSlot = 1,
                    dualSimEnabled = false,
                    onSelectSimSlot = {},
                    onExecuteConsulta = { _, _ -> },
                    onCardClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun simStatusState_dynamicDetection_singleVsDual() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager = shadowOf(subManager)

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        shadowOf(telephonyManager).setPhoneCount(2)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            shadowOf(telephonyManager).setActiveModemCount(2)
        }

        val subInfo1 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(101)
            .setSimSlotIndex(0)
            .setIccId("89530000000000000001")
            .setDisplayName("Cubacel 1")
            .buildSubscriptionInfo()

        val subInfo2 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(102)
            .setSimSlotIndex(1)
            .setIccId("89530000000000000002")
            .setDisplayName("Cubacel 2")
            .buildSubscriptionInfo()

        // 1. Single SIM in Slot 0
        shadowSubManager.setActiveSubscriptionInfos(subInfo1)
        var state = computeSimStatusState(context)
        assertTrue(state.isSim1Active)
        assertFalse(state.isSim2Active)
        assertTrue(state.hideSelector)

        // 2. Dual SIM (Insert SIM 2)
        shadowSubManager.setActiveSubscriptionInfos(subInfo1, subInfo2)
        state = computeSimStatusState(context)
        assertTrue(state.isSim1Active)
        assertTrue(state.isSim2Active)
        assertFalse(state.hideSelector)

        // 3. Remove SIM 1 (Leaving SIM 2)
        shadowSubManager.setActiveSubscriptionInfos(subInfo2)
        state = computeSimStatusState(context)
        assertFalse(state.isSim1Active)
        assertTrue(state.isSim2Active)
        assertTrue(state.hideSelector)
    }

    @Test
    fun priorityStatusCard_selectedSimRemoved_switchesToActiveSim() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val shadowSubManager = shadowOf(subManager)

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
        shadowOf(telephonyManager).setPhoneCount(2)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            shadowOf(telephonyManager).setActiveModemCount(2)
        }

        val subInfo2 = ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(102)
            .setSimSlotIndex(1)
            .setIccId("89530000000000000002")
            .setDisplayName("Cubacel 2")
            .buildSubscriptionInfo()

        // Only SIM 2 in slot 1 is active
        shadowSubManager.setActiveSubscriptionInfos(subInfo2)

        val dummyPlan = PlanStatusEntity(id = 2, balanceCup = 50.0)
        var selectedSlotCallback = -1

        composeTestRule.setContent {
            MegasTheme {
                PriorityStatusCard(
                    planStatus = dummyPlan,
                    selectedSimSlot = 1,
                    dualSimEnabled = true,
                    onSelectSimSlot = { newSlot ->
                        selectedSlotCallback = newSlot
                    },
                    onExecuteConsulta = { _, _ -> },
                    onCardClick = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        // Slot 1 was selected but SIM 1 is not active. Auto-switches to Slot 2!
        assertEquals(2, selectedSlotCallback)
    }
}
