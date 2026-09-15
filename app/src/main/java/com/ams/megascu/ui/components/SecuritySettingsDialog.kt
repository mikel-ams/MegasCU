package com.ams.megascu.ui.components

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.ams.megascu.ui.viewmodel.MainViewModel

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsBottomSheet(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSecurityUnlocked: () -> Unit = {},
    onProgress: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    
    fun getFragmentActivity(context: android.content.Context): FragmentActivity? {
        var currentContext = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is FragmentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }
    
    val activity = getFragmentActivity(context)

    val securityEnabled by viewModel.securityEnabled.collectAsState()
    val securityPin by viewModel.securityPin.collectAsState()
    val biometricsEnabled by viewModel.biometricsEnabled.collectAsState()
    val protectionScope by viewModel.protectionScope.collectAsState()

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showVerifyPinForDisable by remember { mutableStateOf(false) }
    var showDisableWarning by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val view = androidx.compose.ui.platform.LocalView.current
    SideEffect {
        var parent = view.parent
        while (parent != null) {
            if (parent is androidx.compose.ui.window.DialogWindowProvider) {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(parent.window, false)
                break
            }
            parent = parent.parent
        }
    }

    SheetProgressTracker(sheetState = sheetState, onProgress = onProgress)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier
            .statusBarsPadding()
            .padding(top = 40.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        dragHandle = { ExpressiveDragHandle() },
        scrimColor = Color.Black.copy(alpha = 0.35f),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = ShieldLockIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Seguridad y Control",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                ExpressiveIconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 1. Tarjeta: Protección de la Aplicación
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = ShieldLockIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Protección de la Aplicación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Protege compras y acceso a la app con PIN o biometría",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ExpressiveSwitch(
                                checked = securityEnabled,
                                checkedIcon = Icons.Rounded.Lock,
                                uncheckedIcon = Icons.Rounded.LockOpen,
                                onCheckedChange = { checked ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (checked) {
                                        if (securityPin.isEmpty()) {
                                            showPinSetupDialog = true
                                        } else {
                                            viewModel.setSecurityEnabled(true)
                                            onSecurityUnlocked()
                                        }
                                    } else {
                                        if (securityPin.isNotEmpty()) {
                                            showVerifyPinForDisable = true
                                        } else {
                                            showDisableWarning = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (securityEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Opciones de Autenticación",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )

                        // Tarjeta segmentada Material Expressive para opciones de autenticación
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            // Segmento 1: PIN de Seguridad
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = com.ams.megascu.R.drawable.ic_password_2),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("PIN de Seguridad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        if (securityPin.isNotEmpty()) {
                                            Text("Código de 4 dígitos configurado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Text("Sin PIN configurado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                        }
                                    }
                                    ExpressiveButton(
                                        onClick = {
                                            showPinSetupDialog = true
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.heightIn(min = 40.dp)
                                    ) {
                                        Text("Cambiar")
                                    }
                                }
                            }

                            // Segmento 2: Autenticación Biométrica
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Autenticación Biométrica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Usar huella o Face ID al abrir/comprar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    ExpressiveSwitch(
                                        checked = biometricsEnabled,
                                        onCheckedChange = { enable ->
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (enable) {
                                                if (activity != null) {
                                                    val biometricPrompt = BiometricPrompt(
                                                        activity,
                                                        executor,
                                                        object : BiometricPrompt.AuthenticationCallback() {
                                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                                super.onAuthenticationSucceeded(result)
                                                                viewModel.setBiometricsEnabled(true)
                                                                Toast.makeText(context, "Bloqueo biométrico activado", Toast.LENGTH_SHORT).show()
                                                            }

                                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                                super.onAuthenticationError(errorCode, errString)
                                                                viewModel.setBiometricsEnabled(false)
                                                                Toast.makeText(context, "No se pudo activar: $errString", Toast.LENGTH_SHORT).show()
                                                            }

                                                            override fun onAuthenticationFailed() {
                                                                super.onAuthenticationFailed()
                                                                viewModel.setBiometricsEnabled(false)
                                                                Toast.makeText(context, "Autenticación biométrica fallida", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    )
                                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                                        .setTitle("Verificar huella")
                                                        .setSubtitle("Confirme su huella o Face ID para activar esta función")
                                                        .setNegativeButtonText("Cancelar")
                                                        .build()
                                                    biometricPrompt.authenticate(promptInfo)
                                                } else {
                                                    Toast.makeText(context, "Dispositivo no compatible con biometría", Toast.LENGTH_SHORT).show()
                                                }
                                            } else {
                                                viewModel.setBiometricsEnabled(false)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Alcance de la Protección",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 6.dp, bottom = 4.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val scopeOptions = listOf(
                                    SegmentOption("BOTH", "Ambos", Icons.Rounded.Shield),
                                    SegmentOption("APP_ACCESS", "App", Icons.Rounded.Smartphone),
                                    SegmentOption("PURCHASES", "Compras", Icons.Rounded.ShoppingCart)
                                )
                                ConnectedSegmentedGroup(
                                    items = scopeOptions,
                                    selectedValue = protectionScope,
                                    onItemSelected = { scope ->
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setProtectionScope(scope)
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                val scopeDesc = when (protectionScope) {
                                    "PURCHASES" -> "Bloquea solo las compras de paquetes y planes de datos."
                                    "APP_ACCESS" -> "Bloquea el acceso a la aplicación al abrirla."
                                    else -> "Bloquea el acceso a la aplicación y las compras."
                                }
                                Text(
                                    scopeDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Top fade overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                )
            }

            if (showPinSetupDialog) {
                PinSetupDialog(
                    onPinSet = { newPin ->
                        viewModel.setSecurityPin(newPin)
                        viewModel.setSecurityEnabled(true)
                        onSecurityUnlocked()
                        showPinSetupDialog = false
                    },
                    onDismiss = { showPinSetupDialog = false }
                )
            }

            if (showVerifyPinForDisable) {
                PinVerifyDialog(
                    correctPin = securityPin,
                    onVerified = {
                        showVerifyPinForDisable = false
                        showDisableWarning = true
                    },
                    onDismiss = { showVerifyPinForDisable = false }
                )
            }

            if (showDisableWarning) {
                AlertDialog(
                    onDismissRequest = { showDisableWarning = false },
                    modifier = Modifier.expressiveModalEntrance(),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(34.dp)
                        )
                    },
                    title = {
                        Text(
                            "Desactivar Seguridad",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            "Advertencia: Al desactivar la protección de seguridad se eliminará el PIN de seguridad almacenado en la aplicación.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        ExpressiveButton(
                            onClick = {
                                viewModel.setSecurityEnabled(false)
                                viewModel.setSecurityPin("")
                                viewModel.setBiometricsEnabled(false)
                                showDisableWarning = false
                                Toast.makeText(context, "Seguridad desactivada. PIN eliminado.", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Desactivar y Eliminar")
                        }
                    },
                    dismissButton = {
                        ExpressiveTextButton(
                            onClick = {
                                showDisableWarning = false
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PinVerifyDialog(
    correctPin: String,
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val flashErrorColor = remember { Animatable(0f) }

    LaunchedEffect(error) {
        if (error) {
            delay(5000)
            error = false
        }
    }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .expressiveModalEntrance(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(62.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = ShieldLockIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Confirmar PIN",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 23.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ingresa tu PIN de seguridad para continuar:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        val sizeDp by animateDpAsState(
                            targetValue = if (isFilled) 22.dp else 15.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            label = "pinVerifyIndicatorSize_$i"
                        )

                        val baseColor = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
                        val indicatorColor = lerp(baseColor, MaterialTheme.colorScheme.error, flashErrorColor.value)

                        Surface(
                            shape = CircleShape,
                            color = indicatorColor,
                            modifier = Modifier.size(sizeDp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (error) {
                    Text(
                        "PIN incorrecto",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Expressive Spacious Numpad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    for (row in keys) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            for (key in row) {
                                if (key.isEmpty()) {
                                    Spacer(modifier = Modifier.size(68.dp, 52.dp))
                                } else {
                                    val keyInteraction = remember { MutableInteractionSource() }
                                    val isPressed by keyInteraction.collectIsPressedAsState()
                                    val keyCorner by animateDpAsState(
                                        targetValue = if (isPressed) 8.dp else 18.dp,
                                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                                        label = "verifyKeyCorner"
                                    )
                                    Surface(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            if (key == "DEL") {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    error = false
                                                }
                                            } else {
                                                if (enteredPin.length < 4) {
                                                    val next = enteredPin + key
                                                    enteredPin = next
                                                    error = false
                                                    if (next.length == 4) {
                                                        if (next == correctPin) {
                                                            onVerified()
                                                        } else {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            error = true
                                                            coroutineScope.launch {
                                                                flashErrorColor.snapTo(1f)
                                                                flashErrorColor.animateTo(0f, animationSpec = tween(400))
                                                            }
                                                            coroutineScope.launch {
                                                                delay(200)
                                                                enteredPin = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        interactionSource = keyInteraction,
                                        shape = RoundedCornerShape(keyCorner),
                                        color = if (key == "DEL") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.50f),
                                        modifier = Modifier.size(68.dp, 52.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (key == "DEL") {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                                    contentDescription = "Borrar",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = key,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveTextButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.heightIn(min = 46.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    ExpressiveButton(
                        onClick = {
                            if (enteredPin == correctPin) {
                                onVerified()
                            } else {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                error = true
                            }
                        },
                        enabled = enteredPin.length == 4,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.heightIn(min = 46.dp)
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SecuritySettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSecurityUnlocked: () -> Unit = {},
    onProgress: (Float) -> Unit = {}
) {
    SecuritySettingsBottomSheet(
        viewModel = viewModel,
        onDismiss = onDismiss,
        onSecurityUnlocked = onSecurityUnlocked,
        onProgress = onProgress
    )
}
