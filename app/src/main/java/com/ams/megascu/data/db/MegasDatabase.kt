package com.ams.megascu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PlanStatusEntity::class, SmsLogEntity::class, UsageHistoryEntity::class], version = 7, exportSchema = false)
abstract class MegasDatabase : RoomDatabase() {
    abstract fun planDao(): PlanDao
    abstract fun smsLogDao(): SmsLogDao
    abstract fun usageHistoryDao(): UsageHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MegasDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE plan_status ADD COLUMN subscriptionId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sms_logs ADD COLUMN simSlot INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE sms_logs ADD COLUMN subscriptionId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usage_history ADD COLUMN simSlot INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE usage_history ADD COLUMN subscriptionId INTEGER DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): MegasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MegasDatabase::class.java,
                    "megascu_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
