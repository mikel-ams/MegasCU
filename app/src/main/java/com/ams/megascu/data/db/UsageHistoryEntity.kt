package com.ams.megascu.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_history")
data class UsageHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val balanceCup: Double = 0.0,
    val dataMb: Long = 0,
    val dataLteMb: Long = 0,
    val bonusDataMb: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val simSlot: Int = 1,
    val subscriptionId: Int? = null
)
