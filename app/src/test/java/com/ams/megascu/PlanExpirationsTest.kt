package com.ams.megascu

import com.ams.megascu.data.db.PlanStatusEntity
import com.ams.megascu.service.upcomingExpirations
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanExpirationsTest {
    @Test
    fun preservesSeparateDatesAndPriority() {
        val today = LocalDate.of(2026, 11, 23)
        val plan = PlanStatusEntity(dataMb = 8500, dataDays = 6, minutesStr = "0 min",
            minutesDays = 5, smsCount = 82, smsDays = 5,
            lastUpdatedTimestamp = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        assertEquals(listOf("Datos" to today.plusDays(6), "SMS" to today.plusDays(5)),
            plan.upcomingExpirations(today).map { it.label to it.date })
    }
}
