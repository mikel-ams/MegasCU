package com.ams.megascu.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageHistoryDao {
    @Query("SELECT * FROM usage_history ORDER BY timestamp DESC")
    fun getUsageHistory(): Flow<List<UsageHistoryEntity>>

    @Query("SELECT * FROM usage_history WHERE simSlot = :simSlot ORDER BY timestamp DESC")
    fun getUsageHistoryForSim(simSlot: Int): Flow<List<UsageHistoryEntity>>

    @Query("SELECT * FROM usage_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistorySync(limit: Int): List<UsageHistoryEntity>

    @Query("SELECT * FROM usage_history WHERE simSlot = :simSlot ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistoryForSimSync(simSlot: Int, limit: Int): List<UsageHistoryEntity>

    @Query("SELECT * FROM usage_history WHERE simSlot = :simSlot AND subscriptionId IS :subscriptionId AND isDataObservation = 1 AND timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getHistoryForSubscriptionSinceSync(simSlot: Int, subscriptionId: Int?, since: Long): List<UsageHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageHistory(history: UsageHistoryEntity)

    @Query("DELETE FROM usage_history")
    suspend fun clearHistory()

    @Query("DELETE FROM usage_history WHERE simSlot = :simSlot")
    suspend fun clearHistoryForSim(simSlot: Int)
}
