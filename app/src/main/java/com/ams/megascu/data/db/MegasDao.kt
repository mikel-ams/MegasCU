package com.ams.megascu.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_status WHERE id = :simSlot LIMIT 1")
    fun getPlanStatus(simSlot: Int = 1): Flow<PlanStatusEntity?>

    @Query("SELECT * FROM plan_status WHERE id = :simSlot LIMIT 1")
    suspend fun getPlanStatusDirect(simSlot: Int = 1): PlanStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlanStatus(planStatus: PlanStatusEntity)

    @Query("DELETE FROM plan_status WHERE id = :simSlot")
    suspend fun clearPlanStatusForSim(simSlot: Int)

    @Query("DELETE FROM plan_status")
    suspend fun clearPlanStatus()
}

@Dao
interface SmsLogDao {
    @Query("SELECT * FROM sms_logs ORDER BY timestamp DESC")
    fun getAllSmsLogs(): Flow<List<SmsLogEntity>>

    @Query("SELECT * FROM sms_logs WHERE simSlot = :simSlot ORDER BY timestamp DESC")
    fun getSmsLogsForSim(simSlot: Int): Flow<List<SmsLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsLog(smsLog: SmsLogEntity)

    @Query("DELETE FROM sms_logs WHERE simSlot = :simSlot")
    suspend fun clearLogsForSim(simSlot: Int)

    @Query("DELETE FROM sms_logs")
    suspend fun clearLogs()
}
