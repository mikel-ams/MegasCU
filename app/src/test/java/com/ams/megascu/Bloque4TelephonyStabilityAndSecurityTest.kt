package com.ams.megascu

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.ussd.SimOperatorUtils
import com.ams.megascu.data.ussd.UssdErrorType
import com.ams.megascu.data.ussd.UssdExecutor
import com.ams.megascu.data.ussd.UssdResult
import com.ams.megascu.utils.PermissionUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowSubscriptionManager
import org.robolectric.shadows.ShadowTelephonyManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Bloque4TelephonyStabilityAndSecurityTest {

    private lateinit var context: Context
    private lateinit var shadowApp: ShadowApplication
    private lateinit var shadowSubManager: ShadowSubscriptionManager
    private lateinit var shadowTelephonyManager: ShadowTelephonyManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        shadowApp = shadowOf(context as android.app.Application)

        val subManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        shadowSubManager = shadowOf(subManager)

        val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        shadowTelephonyManager = shadowOf(telManager)
    }

    private fun createSubscriptionInfo(
        id: Int,
        slotIndex: Int,
        displayName: String,
        carrierName: String = "Cubacel",
        mcc: String = "368",
        mnc: String = "01"
    ): SubscriptionInfo {
        return ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
            .setId(id)
            .setSimSlotIndex(slotIndex)
            .setDisplayName(displayName)
            .setCarrierName(carrierName)
            .setCountryIso("cu")
            .setMcc(mcc)
            .setMnc(mnc)
            .buildSubscriptionInfo()
    }

    // =========================================================================
    // 1. SimOperatorUtils Tests (Stability, Telecom, Multi-SIM, Edge cases)
    // =========================================================================

    @Test
    fun testFindActiveSubscriptionForSlot_validAndInvalid() {
        val sub1 = createSubscriptionInfo(10, 0, "Cubacel SIM 1")
        val sub2 = createSubscriptionInfo(20, 1, "Cubacel SIM 2")
        shadowSubManager.setActiveSubscriptionInfos(sub1, sub2)

        val found1 = SimOperatorUtils.findActiveSubscriptionForSlot(context, 1)
        assertNotNull("Slot 1 debe encontrar sub1", found1)
        assertEquals(10, found1?.subscriptionId)

        val found2 = SimOperatorUtils.findActiveSubscriptionForSlot(context, 2)
        assertNotNull("Slot 2 debe encontrar sub2", found2)
        assertEquals(20, found2?.subscriptionId)

        assertNull("Slot 0 debe ser null", SimOperatorUtils.findActiveSubscriptionForSlot(context, 0))
        assertNull("Slot 3 debe ser null", SimOperatorUtils.findActiveSubscriptionForSlot(context, 3))
    }

    @Test
    fun testGetSubscriptionIdForSlot() {
        val sub1 = createSubscriptionInfo(101, 0, "Cubacel")
        shadowSubManager.setActiveSubscriptionInfos(sub1)

        assertEquals(101, SimOperatorUtils.getSubscriptionIdForSlot(context, 1))
        assertNull(SimOperatorUtils.getSubscriptionIdForSlot(context, 2))
    }

    @Test
    fun testGetPhoneAccountHandleForSlot_doesNotCrash() {
        // En entorno de prueba sin cuentas configuradas, debe retornar null sin lanzar excepciones
        val handle1 = SimOperatorUtils.getPhoneAccountHandleForSlot(context, 1)
        val handle2 = SimOperatorUtils.getPhoneAccountHandleForSlot(context, 2)
        // No debe fallar ni lanzar excepciones
        assertNull(handle1)
        assertNull(handle2)
    }

    @Test
    fun testCubacelDetection_byMccMncAndNames() {
        val subCubacel = createSubscriptionInfo(1, 0, "Mi Linea", "ETECSA", "368", "01")
        shadowSubManager.setActiveSubscriptionInfos(subCubacel)

        val details = SimOperatorUtils.checkSimOperator(context, 1, isEmulator = false)
        assertTrue("Debe identificarse como Cubacel", details.isCubacel)
        assertFalse("No debe estar ausente", details.isAbsent)
        assertFalse("No debe estar pendiente", details.isPendingInfo)
        assertTrue(details.operatorName.contains("ETECSA") || details.operatorName.contains("Cubacel"))
    }

    @Test
    fun testForeignCarrierDetection() {
        val subForeign = createSubscriptionInfo(2, 1, "T-Mobile US", "T-Mobile", "310", "260")
        val subCubacel = createSubscriptionInfo(1, 0, "Cubacel", "Cubacel", "368", "01")
        shadowSubManager.setActiveSubscriptionInfos(subCubacel, subForeign)

        val details1 = SimOperatorUtils.checkSimOperator(context, 1, isEmulator = false)
        assertTrue("SIM 1 debe ser Cubacel", details1.isCubacel)

        val details2 = SimOperatorUtils.checkSimOperator(context, 2, isEmulator = false)
        assertFalse("SIM 2 no debe ser Cubacel", details2.isCubacel)
        assertFalse("SIM 2 no está ausente", details2.isAbsent)
        assertEquals("T-Mobile", details2.operatorName)
    }

    @Test
    fun testAbsentSimDetection() {
        val sub1 = createSubscriptionInfo(1, 0, "Cubacel")
        shadowSubManager.setActiveSubscriptionInfos(sub1)

        val details2 = SimOperatorUtils.checkSimOperator(context, 2, isEmulator = false)
        assertFalse(details2.isCubacel)
        assertTrue("Slot 2 sin sub debe reportarse ausente", details2.isAbsent)
    }

    // =========================================================================
    // 2. PermissionUtils Tests (Centralization, Multi-level fallbacks)
    // =========================================================================

    @Test
    fun testPermissionUtils_phoneAndNotificationChecks() {
        // Inicialmente sin otorgar
        shadowApp.denyPermissions(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
        assertFalse(PermissionUtils.hasCallPhonePermission(context))
        assertFalse(PermissionUtils.hasPhoneStatePermission(context))

        // Otorgar permisos
        shadowApp.grantPermissions(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)
        assertTrue(PermissionUtils.hasCallPhonePermission(context))
        assertTrue(PermissionUtils.hasPhoneStatePermission(context))
    }

    @Test
    fun testPermissionUtils_openAppSettings() {
        PermissionUtils.openAppSettings(context)
        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull("Debe lanzar Intent de Ajustes de la Aplicación", startedIntent)
        assertEquals(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, startedIntent.action)
        assertEquals("package:${context.packageName}", startedIntent.dataString)
        assertTrue((startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun testPermissionUtils_openUsageAccessSettings() {
        PermissionUtils.openUsageAccessSettings(context)
        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull("Debe lanzar Intent de Ajustes de Acceso a Datos de Uso", startedIntent)
        assertEquals(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS, startedIntent.action)
        assertTrue((startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    // =========================================================================
    // 3. UssdExecutor Fallback & Intent Security Tests
    // =========================================================================

    @Test
    fun testUssdDialFallback_intentExtrasAndEncoding_Slot1() {
        val sub1 = createSubscriptionInfo(101, 0, "Cubacel Principal")
        shadowSubManager.setActiveSubscriptionInfos(sub1)

        shadowApp.denyPermissions(Manifest.permission.CALL_PHONE)
        val executor = UssdExecutor(context)

        // Ejecutar compra USSD interactiva *133#
        executor.executePurchaseUssd("*133#", simSlot = 1)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull("Debe iniciar actividad del marcador", startedIntent)
        assertEquals(Intent.ACTION_DIAL, startedIntent.action)

        // Verificar codificación de URL (# como %23)
        assertEquals("tel:*133%23", startedIntent.dataString)

        // Verificar extras de ranura y suscripción para Slot 1
        assertEquals(101, startedIntent.getIntExtra("subscription", -1))
        assertEquals(101, startedIntent.getIntExtra("subId", -1))
        assertEquals(101, startedIntent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1))
        assertEquals(0, startedIntent.getIntExtra("simSlot", -1))
        assertEquals(0, startedIntent.getIntExtra("slot", -1))
        assertEquals(0, startedIntent.getIntExtra("android.telephony.extra.SLOT_INDEX", -1))
        assertTrue((startedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }

    @Test
    fun testUssdDialFallback_intentExtrasAndEncoding_Slot2() {
        val sub1 = createSubscriptionInfo(101, 0, "Cubacel Principal")
        val sub2 = createSubscriptionInfo(202, 1, "Cubacel Trabajo")
        shadowSubManager.setActiveSubscriptionInfos(sub1, sub2)

        shadowApp.denyPermissions(Manifest.permission.CALL_PHONE)
        val executor = UssdExecutor(context)

        // Ejecutar compra USSD interactiva para Slot 2
        executor.executePurchaseUssd("*133#", simSlot = 2)

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull("Debe iniciar actividad del marcador", startedIntent)
        assertEquals(Intent.ACTION_DIAL, startedIntent.action)

        // Verificar codificación de URL
        assertEquals("tel:*133%23", startedIntent.dataString)

        // Verificar extras de ranura y suscripción para Slot 2
        assertEquals(202, startedIntent.getIntExtra("subscription", -1))
        assertEquals(202, startedIntent.getIntExtra("subId", -1))
        assertEquals(202, startedIntent.getIntExtra("android.telephony.extra.SUBSCRIPTION_INDEX", -1))
        assertEquals(1, startedIntent.getIntExtra("simSlot", -1))
        assertEquals(1, startedIntent.getIntExtra("slot", -1))
        assertEquals(1, startedIntent.getIntExtra("android.telephony.extra.SLOT_INDEX", -1))
    }

    @Test
    fun testUssdExecution_InvalidCodeRejection() = runTest {
        val executor = UssdExecutor(context)
        val result = executor.executeUssdSuspend("222", simSlot = 1)
        assertTrue(result is UssdResult.Error)
        val error = result as UssdResult.Error
        assertEquals(UssdErrorType.INVALID_CODE, error.errorType)
        assertTrue(error.message.contains("[ERR_CODE_102]"))
    }

    @Test
    fun testUssdExecution_SimulationOnEmulator() = runTest {
        val executor = UssdExecutor(context)
        // En entorno Robolectric isEmulator() es true
        assertTrue(executor.isEmulator())

        val balanceResult = executor.executeUssdSuspend("*222#", simSlot = 1)
        assertTrue(balanceResult is UssdResult.Success)
        val balanceSuccess = balanceResult as UssdResult.Success
        assertTrue(balanceSuccess.response.contains("Saldo"))

        val dataResult = executor.executeUssdSuspend("*222*328#", simSlot = 1)
        assertTrue(dataResult is UssdResult.Success)
        val dataSuccess = dataResult as UssdResult.Success
        assertTrue(dataSuccess.response.contains("MB"))
    }
}
