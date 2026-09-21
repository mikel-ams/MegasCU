package com.ams.megascu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PlanStatusEntity::class, SmsLogEntity::class, UsageHistoryEntity::class], version = 8, exportSchema = true)
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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Retain parsed values while erasing previously stored raw USSD responses.
                db.execSQL("UPDATE plan_status SET rawLastResponse = ''")
            }
        }

        fun getDatabase(context: Context): MegasDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MegasDatabase::class.java,
                    "megascu_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
