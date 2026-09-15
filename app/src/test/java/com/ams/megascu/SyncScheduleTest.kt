package com.ams.megascu

import com.ams.megascu.data.db.MegasRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SyncScheduleTest {

    private fun getMockDelay(nowHour: Int, nowMinute: Int, intervalHours: Int): Long {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, nowHour)
            set(Calendar.MINUTE, nowMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val candidate = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetHours = when (intervalHours) {
            12 -> listOf(8, 20)
            6 -> listOf(2, 8, 14, 20)
            24 -> listOf(8)
            3 -> listOf(2, 5, 8, 11, 14, 17, 20, 23)
            1 -> (0..23).toList()
            else -> {
                val list = mutableListOf<Int>()
                for (h in 0..23) {
                    if (Math.floorMod(h - 8, intervalHours) == 0) {
                        list.add(h)
                    }
                }
                list.ifEmpty { listOf(8) }
            }
        }

        var minDiffMs = Long.MAX_VALUE
        for (dayOffset in 0..1) {
            for (targetHour in targetHours) {
                candidate.timeInMillis = now.timeInMillis
                candidate.set(Calendar.MINUTE, 0)
                candidate.set(Calendar.SECOND, 0)
                candidate.set(Calendar.MILLISECOND, 0)
                if (dayOffset > 0) {
                    candidate.add(Calendar.DAY_OF_YEAR, dayOffset)
                }
                candidate.set(Calendar.HOUR_OF_DAY, targetHour)
                val diff = candidate.timeInMillis - now.timeInMillis
                if (diff > 0 && diff < minDiffMs) {
                    minDiffMs = diff
                }
            }
        }
        return if (minDiffMs != Long.MAX_VALUE) minDiffMs else 0L
    }

    @Test
    fun test12HourSyncTargets8And20() {
        // At 07:00 -> next is 08:00 (1 hour delay)
        val delay1 = getMockDelay(7, 0, 12)
        assertEquals(3600000L, delay1)

        // At 08:30 -> next is 20:00 (11.5 hours delay)
        val delay2 = getMockDelay(8, 30, 12)
        assertEquals((11 * 3600 + 30 * 60) * 1000L, delay2)

        // At 20:30 -> next is 08:00 tomorrow (11.5 hours delay)
        val delay3 = getMockDelay(20, 30, 12)
        assertEquals((11 * 3600 + 30 * 60) * 1000L, delay3)
    }

    @Test
    fun test6HourSyncTargets8And14And20And2() {
        // At 07:00 -> next is 08:00 (1 hour delay)
        val delay1 = getMockDelay(7, 0, 6)
        assertEquals(3600000L, delay1)

        // At 08:30 -> next is 14:00 (5.5 hours delay)
        val delay2 = getMockDelay(8, 30, 6)
        assertEquals((5 * 3600 + 30 * 60) * 1000L, delay2)

        // At 15:00 -> next is 20:00 (5 hours delay)
        val delay3 = getMockDelay(15, 0, 6)
        assertEquals(5 * 3600 * 1000L, delay3)

        // At 21:00 -> next is 02:00 tomorrow (5 hours delay)
        val delay4 = getMockDelay(21, 0, 6)
        assertEquals(5 * 3600 * 1000L, delay4)
    }
}
