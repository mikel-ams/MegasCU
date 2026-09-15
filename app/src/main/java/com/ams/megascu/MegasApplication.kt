package com.ams.megascu

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ams.megascu.data.db.MegasDatabase
import com.ams.megascu.data.db.MegasRepository
import com.ams.megascu.service.ExpirationWorker
import java.util.concurrent.TimeUnit

class MegasApplication : Application(), Configuration.Provider {
    val database by lazy { MegasDatabase.getDatabase(this) }
    val repository by lazy { MegasRepository(this, database.planDao(), database.smsLogDao(), database.usageHistoryDao()) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        scheduleExpirationWorker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas ETECSA"
            val descriptionText = "Notificaciones de consumo y expiración de datos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun scheduleExpirationWorker() {
        val workRequest = PeriodicWorkRequestBuilder<ExpirationWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ExpirationCheck",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    companion object {
        const val CHANNEL_ID = "etecsa_alerts"
    }
}
