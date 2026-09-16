package com.ams.megascu.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ams.megascu.ui.viewmodel.MainViewModel

/**
 * Componente que ejecuta una verificación silenciosa en segundo plano
 * en el servidor de GitHub cada vez que se abre la aplicación.
 * Si encuentra una nueva versión disponible, despliega el diálogo de actualización configurado.
 */
@Composable
fun AppLaunchUpdateChecker(
    viewModel: MainViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.checkUpdatesSilentlyOnLaunch()
    }
}
