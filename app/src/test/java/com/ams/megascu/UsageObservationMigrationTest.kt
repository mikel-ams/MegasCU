package com.ams.megascu

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.ams.megascu.data.db.MegasDatabase
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UsageObservationMigrationTest {
    @Test fun migrationPreservesOldDataAndDoesNotAssumeItWasFreshlyQueried() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).callback(object : SupportSQLiteOpenHelper.Callback(8) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE usage_history (id INTEGER PRIMARY KEY, dataMb INTEGER NOT NULL, subscriptionId INTEGER)")
                    db.execSQL("INSERT INTO usage_history VALUES (1, 2048, 101)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) { }
            }).build()
        )
        try {
            val db = helper.writableDatabase
            MegasDatabase.MIGRATION_8_9.migrate(db)
            db.query("SELECT dataMb, subscriptionId, isDataObservation FROM usage_history WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(2048L, cursor.getLong(0))
                assertEquals(101, cursor.getInt(1))
                assertEquals(0, cursor.getInt(2))
            }
            db.execSQL("INSERT INTO usage_history (id, dataMb, subscriptionId, isDataObservation) VALUES (2, 1536, 101, 1)")
            db.query("SELECT COUNT(*) FROM usage_history WHERE isDataObservation = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
        } finally { helper.close() }
    }
}
