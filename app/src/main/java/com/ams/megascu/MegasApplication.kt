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
import com.ams.megascu.service.GitHubUpdateWorker
import com.ams.megascu.ui.components.ChangelogRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MegasApplication : Application(), Configuration.Provider {
    val appScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            android.util.Log.e("MegasApplication", "Unhandled application coroutine", throwable)
        }
    )
    val database by lazy { MegasDatabase.getDatabase(this) }
    val repository by lazy { MegasRepository(this, database.planDao(), database.smsLogDao(), database.usageHistoryDao()) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        ChangelogRepository.getChangelogList(this)
        createNotificationChannel()
        scheduleExpirationWorker()
        scheduleGitHubUpdateWorker()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val name = "Alertas ETECSA"
            val descriptionText = "Notificaciones de consumo y expiración de datos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)

            val updateChannelName = "Actualizaciones de la App"
            val updateChannelDesc = "Avisos de nuevas versiones y mejoras disponibles en GitHub"
            val updateChannel = NotificationChannel(CHANNEL_UPDATES_ID, updateChannelName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = updateChannelDesc
            }
            notificationManager.createNotificationChannel(updateChannel)
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

    private fun scheduleGitHubUpdateWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val updateWorkRequest = PeriodicWorkRequestBuilder<GitHubUpdateWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "GitHubUpdateCheck",
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            updateWorkRequest
        )
    }

    companion object {
        const val CHANNEL_ID = "etecsa_alerts"
        const val CHANNEL_UPDATES_ID = "megas_updates"
    }
}
