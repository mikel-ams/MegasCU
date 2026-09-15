package com.ams.megascu.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plan_status")
data class PlanStatusEntity(
    @PrimaryKey val id: Int = 1,
    val subscriptionId: Int? = null,
    val balanceCup: Double = 0.0,
    val dataMb: Long = 0,
    val dataLteMb: Long = 0,
    val bonusDataMb: Long = 0,
    val minutesStr: String = "",
    val smsCount: Int = 0,
    val dataDays: Int = 0,
    val minutesDays: Int = 0,
    val smsDays: Int = 0,
    val dataExpirationTimestamp: Long = 0,
    val minutesExpirationTimestamp: Long = 0,
    val smsExpirationTimestamp: Long = 0,
    val nextRechargeDateStr: String = "",
    val nextRechargeDays: Int = 0,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val rawLastResponse: String = ""
)
