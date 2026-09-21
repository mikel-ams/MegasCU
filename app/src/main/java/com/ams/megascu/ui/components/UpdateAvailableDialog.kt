package com.ams.megascu.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ams.megascu.BuildConfig
import com.ams.megascu.utils.ApkDownloadManager
import com.ams.megascu.utils.GitHubUpdateChecker
import com.ams.megascu.utils.PermissionUtils
import com.ams.megascu.utils.UpdateCheckResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

private enum class DownloadStatus {
    IDLE,
    DOWNLOADING,
    COMPLETED,
    FAILED
}

@Composable
fun UpdateAvailableDialog(
    updateResult: UpdateCheckResult,
    isSimulation: Boolean = false,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val dialogScrollState = rememberScrollState()
    val changelogScrollState = rememberScrollState()

    var downloadStatus by remember { mutableStateOf(DownloadStatus.IDLE) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalSizeBytes by remember { mutableLongStateOf(0L) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }
    var downloadErrorMessage by remember { mutableStateOf<String?>(null) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }

    fun startDownload() {
        if (isSimulation) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            downloadStatus = DownloadStatus.DOWNLOADING
            downloadProgress = 0f
            downloadedBytes = 0L
            downloadErrorMessage = null
            val simulatedMb = if (updateResult.apkSizeMb > 0f) updateResult.apkSizeMb else 14.5f
            val simulatedTotal = (simulatedMb * 1024 * 1024).toLong()
            totalSizeBytes = simulatedTotal

            downloadJob = coroutineScope.launch {
                val totalSteps = 35
                val delayMs = 65L
                for (step in 1..totalSteps) {
                    kotlinx.coroutines.delay(delayMs)
                    val progress = step.toFloat() / totalSteps.toFloat()
                    downloadProgress = progress
                    downloadedBytes = (simulatedTotal * progress).toLong()
                }
                downloadProgress = 1f
                downloadedBytes = simulatedTotal
                downloadStatus = DownloadStatus.COMPLETED
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                Toast.makeText(context, "Simulación: Descarga de actualización completada", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val downloadUrl = updateResult.apkDownloadUrl
        if (downloadUrl.isNullOrBlank()) {
            Toast.makeText(context, "URL de descarga no disponible en este release", Toast.LENGTH_LONG).show()
            return
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        downloadStatus = DownloadStatus.DOWNLOADING
        downloadProgress = 0f
        downloadedBytes = 0L
        downloadErrorMessage = null

        val estimatedBytes = if (updateResult.apkSizeMb > 0f) {
            (updateResult.apkSizeMb * 1024 * 1024).toLong()
        } else {
            0L
        }
        totalSizeBytes = estimatedBytes

        downloadJob = coroutineScope.launch {
            val result = ApkDownloadManager.downloadApk(
                context = context,
                downloadUrl = downloadUrl,
                versionName = updateResult.latestVersionName,
                estimatedSizeBytes = estimatedBytes,
                expectedSha256 = updateResult.sha256Checksum
            ) { downloaded, total, progress ->
                downloadedBytes = downloaded
                if (total > 0) totalSizeBytes = total
                if (progress >= 0f) {
                    downloadProgress = progress
                }
            }

            result.fold(
                onSuccess = { file ->
                    downloadedApkFile = file
                    downloadStatus = DownloadStatus.COMPLETED
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    // Launch installer automatically if permission is already granted
                    if (PermissionUtils.canInstallUnknownApps(context)) {
                        ApkDownloadManager.installApk(context, file, updateResult.sha256Checksum.orEmpty())
                    }
                },
                onFailure = { error ->
                    downloadStatus = DownloadStatus.FAILED
                    downloadErrorMessage = error.message ?: "Error desconocido durante la descarga."
                }
            )
        }
    }

    Dialog(
        onDismissRequest = {
            if (downloadStatus != DownloadStatus.DOWNLOADING) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = downloadStatus != DownloadStatus.DOWNLOADING,
            dismissOnClickOutside = downloadStatus != DownloadStatus.DOWNLOADING,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val dialogShape = RoundedCornerShape(28.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = dialogShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                tonalElevation = 8.dp,
                shadowElevation = 14.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(dialogScrollState)
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSimulation) MaterialTheme.colorScheme.tertiaryContainer 
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSimulation) Icons.Rounded.DeveloperMode else Icons.Rounded.SystemUpdate,
                            contentDescription = "Actualización",
                            tint = if (isSimulation) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSimulation) "SIMULADOR DE ACTUALIZACIÓN" else if (updateResult.isPrerelease) "PRE-RELEASE BETA DETECTADA" else "NUEVA VERSIÓN DETECTADA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = if (isSimulation) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = updateResult.releaseTitle.ifBlank { "MegasCU ${updateResult.latestVersionName}" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Version Comparison Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhoneAndroid,
                                    contentDescription = "Versión instalada",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudDownload,
                                    contentDescription = "Nueva versión",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "v${updateResult.latestVersionName}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (updateResult.publishedAt.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Publicado: ${updateResult.publishedAt}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Changelog Section
                    Text(
                        text = "Registro de cambios (GitHub):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp, bottom = 6.dp)
                    )

                    val changelogCardShape = RoundedCornerShape(16.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 160.dp),
                        shape = changelogCardShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(changelogScrollState)
                                .padding(12.dp)
                        ) {
                            val changelogText = if (updateResult.changelog.isNotBlank()) {
                                updateResult.changelog
                            } else {
                                "• Novedades y correcciones de estabilidad.\n• Actualizaciones de la interfaz de usuario.\n• Optimización en descarga e instalación."
                            }
                            MarkdownChangelog(
                                markdown = changelogText,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Calculated Size Badge
                    val effectiveSizeMb = if (totalSizeBytes > 0) {
                        totalSizeBytes / (1024f * 1024f)
                    } else {
                        updateResult.apkSizeMb
                    }

                    if (effectiveSizeMb > 0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Android,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tamaño del instalador: %.1f MB".format(effectiveSizeMb),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (!updateResult.sha256Checksum.isNullOrBlank()) "Validación OTA criptográfica y SHA-256 activa" else "Validación de firma oficial y paquete activa",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Download Manager Interactive Block
                    when (downloadStatus) {
                        DownloadStatus.IDLE -> {
                            if (!updateResult.apkDownloadUrl.isNullOrBlank() || isSimulation) {
                                ExpressiveButton(
                                    onClick = { startDownload() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSimulation) "Simular Descarga e Instalación" else "Descargar e Instalar Actualización",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val url = updateResult.releaseHtmlUrl.ifBlank { "https://github.com/${GitHubUpdateChecker.DEFAULT_REPO}/releases" }
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo abrir GitHub", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.OpenInBrowser,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ver detalles en GitHub",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            TextButton(
                                onClick = {
                                    if (!isSimulation) {
                                        val prefs = context.getSharedPreferences("megas_prefs", Context.MODE_PRIVATE)
                                        prefs.edit().putString(GitHubUpdateChecker.PREF_UPDATE_DISMISSED_VERSION, updateResult.latestVersionName).apply()
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Recordar más tarde",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        DownloadStatus.DOWNLOADING -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (isSimulation) "Simulando descarga..." else "Descargando actualización...",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val percentInt = (downloadProgress * 100).toInt()
                                    Text(
                                        text = if (percentInt > 0) "$percentInt%" else "Calculando...",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Material M3 Expressive Wavy Progress Bar
                                LinearWavyProgressIndicator(
                                    progress = if (downloadProgress > 0f) downloadProgress else 0.05f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    strokeWidth = 4.dp,
                                    amplitude = 3.dp,
                                    waveLength = 20.dp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val downloadedMb = downloadedBytes / (1024f * 1024f)
                                val totalMb = if (totalSizeBytes > 0) totalSizeBytes / (1024f * 1024f) else effectiveSizeMb
                                Text(
                                    text = if (totalMb > 0f) {
                                        "%.1f MB de %.1f MB".format(downloadedMb, totalMb)
                                    } else {
                                        "%.1f MB descargados".format(downloadedMb)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedButton(
                                    onClick = {
                                        downloadJob?.cancel()
                                        downloadStatus = DownloadStatus.IDLE
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Cancelar",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cancelar descarga")
                                }
                            }
                        }

                        DownloadStatus.COMPLETED -> {
                            val hasInstallPermission = PermissionUtils.canInstallUnknownApps(context) || isSimulation

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = if (isSimulation) "¡Simulación de descarga completada!" else "¡Descarga completada con éxito!",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shield,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Firma oficial e integridad verificadas",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                if (!hasInstallPermission && !isSimulation) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = {
                                            PermissionUtils.openInstallUnknownAppsSettings(context)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Security,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Conceder Permiso de Instalación", fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                ExpressiveButton(
                                    onClick = {
                                        if (isSimulation) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            Toast.makeText(context, "Simulación: Lanzando instalador para v${updateResult.latestVersionName}", Toast.LENGTH_LONG).show()
                                            onDismiss()
                                            return@ExpressiveButton
                                        }
                                        downloadedApkFile?.let { file ->
                                            if (PermissionUtils.canInstallUnknownApps(context)) {
                                                ApkDownloadManager.installApk(context, file, updateResult.sha256Checksum.orEmpty())
                                            } else {
                                                Toast.makeText(context, "Por favor autoriza la instalación de aplicaciones desconocidas", Toast.LENGTH_LONG).show()
                                                PermissionUtils.openInstallUnknownAppsSettings(context)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.InstallMobile,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSimulation) "Simular Instalación" else if (hasInstallPermission) "Instalar Actualización Ahora" else "Permitir e Instalar",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cerrar")
                                }
                            }
                        }

                        DownloadStatus.FAILED -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "No se pudo completar la descarga",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )

                                if (!downloadErrorMessage.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = downloadErrorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { downloadStatus = DownloadStatus.IDLE },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Volver")
                                    }
                                    Button(
                                        onClick = { startDownload() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}
