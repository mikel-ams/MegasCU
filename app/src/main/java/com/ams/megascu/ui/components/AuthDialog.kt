package com.ams.megascu.ui.components
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AuthDialog(
    correctPin: String,
    biometricsEnabled: Boolean,
    onAuthSuccess: () -> Unit,
    onDismiss: () -> Unit,
    onResetApp: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AuthScreen(
                    correctPin = correctPin,
                    biometricsEnabled = biometricsEnabled,
                    onAuthSuccess = onAuthSuccess,
                    onResetApp = onResetApp
                )
                // Add a close button
                ExpressiveIconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar"
                    )
                }
            }
        }
    }
}
