package com.ams.megascu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.utils.PurchaseAlertHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PurchaseAlertHelperTest {
    private val prefs = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("purchase-alert-test", Context.MODE_PRIVATE)

    @Test fun availableRechargeWithHealthyBalanceDoesNotWarn() {
        prefs.edit().clear().commit()
        val plan = PlanStatusEntity(balanceCup = 1030.64, nextRechargeDateStr = "Puede recargar saldo", nextRechargeDays = 0)
        assertFalse(PurchaseAlertHelper.calculateAlertState(plan, prefs).hasAlert)
    }

    @Test fun missingRechargeDateOrUnknownBalanceDoesNotWarn() {
        prefs.edit().clear().commit()
        assertFalse(PurchaseAlertHelper.calculateAlertState(PlanStatusEntity(), prefs).hasAlert)
    }

    @Test fun exhaustedBalanceWithAvailableRechargeWarns() {
        prefs.edit().clear().commit()
        val plan = PlanStatusEntity(balanceCup = 0.0, nextRechargeDateStr = "Puede recargar saldo", nextRechargeDays = 0)
        val alert = PurchaseAlertHelper.calculateAlertState(plan, prefs)
        assertTrue(alert.isRechargeAlert)
        assertEquals("Saldo Agotado", alert.title)
    }

    @Test fun expiringDataStillWarnsWhenBalanceIsHealthy() {
        prefs.edit().clear().commit()
        val plan = PlanStatusEntity(balanceCup = 1030.64, dataMb = 2000, dataDays = 3,
            nextRechargeDateStr = "Puede recargar saldo", nextRechargeDays = 0)
        val alert = PurchaseAlertHelper.calculateAlertState(plan, prefs)
        assertTrue(alert.isDataAlert)
        assertFalse(alert.isRechargeAlert)
    }

    @Test fun simulatedRechargeWarningStillWorksWithPositiveBalance() {
        prefs.edit().clear()
            .putBoolean(PurchaseAlertHelper.KEY_DEV_SIMULATE_ALERT_ENABLED, true)
            .putString(PurchaseAlertHelper.KEY_DEV_SIMULATE_TYPE, "recharge")
            .putInt(PurchaseAlertHelper.KEY_DEV_SIMULATE_RECHARGE_DAYS, 2)
            .commit()
        val alert = PurchaseAlertHelper.calculateAlertState(PlanStatusEntity(balanceCup = 100.0), prefs)
        assertTrue(alert.isRechargeAlert)
        prefs.edit().clear().commit()
    }
}
