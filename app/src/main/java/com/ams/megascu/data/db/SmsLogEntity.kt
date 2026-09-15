package com.ams.megascu.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_logs")
data class SmsLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val messageBody: String,
    val extractedDateStr: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val simSlot: Int = 1,
    val subscriptionId: Int? = null
)
