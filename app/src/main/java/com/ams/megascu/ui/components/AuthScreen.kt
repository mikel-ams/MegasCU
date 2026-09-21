package com.ams.megascu.ui.components

import com.ams.megascu.security.PinVault
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    correctPin: String,
    biometricsEnabled: Boolean,
    onAuthSuccess: () -> Unit,
    onResetApp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val initialLockout = remember(context) { PinVault.lockoutState(context) }

    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var failedAttempts by remember { mutableIntStateOf(initialLockout.failedAttempts) }
    var lockoutTimeLeft by remember { mutableIntStateOf((initialLockout.remainingMillis / 1000L).toInt()) }
    var showPinMode by remember { mutableStateOf(!biometricsEnabled) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val flashErrorColor = remember { Animatable(0f) }

    fun syncLockoutState() {
        val state = PinVault.lockoutState(context)
        failedAttempts = state.failedAttempts
        lockoutTimeLeft = (state.remainingMillis / 1000L).toInt()
    }

    LaunchedEffect(Unit) {
        syncLockoutState()
        if (correctPin.isBlank()) {
            onAuthSuccess()
            return@LaunchedEffect
        }
        while (true) {
            delay(1000L)
            syncLockoutState()
        }
    }

    fun getFragmentActivity(context: android.content.Context): FragmentActivity? {
        var currentContext: android.content.Context? = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is FragmentActivity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    LaunchedEffect(error) {
        if (error) {
            delay(5000L)
            error = false
        }
    }

    LaunchedEffect(enteredPin, lockoutTimeLeft, correctPin) {
        if (enteredPin.isNotEmpty() && error) error = false
        if (enteredPin.length == 4 && lockoutTimeLeft == 0 && correctPin.isNotBlank()) {
            if (PinVault.verify(context, enteredPin, correctPin)) {
                error = false
                PinVault.clearFailures(context)
                failedAttempts = 0
                lockoutTimeLeft = 0
                onAuthSuccess()
            } else {
                error = true
                val state = PinVault.registerFailedAttempt(context)
                failedAttempts = state.failedAttempts
                lockoutTimeLeft = (state.remainingMillis / 1000L).toInt()

                coroutineScope.launch {
                    flashErrorColor.snapTo(1f)
                    flashErrorColor.animateTo(0f, animationSpec = tween(400))
                }

                delay(200L)
                enteredPin = ""
                if (failedAttempts >= 10) {
                    showResetConfirmDialog = true
                }
            }
        }
    }

    val showBiometricPrompt = remember { mutableStateOf(false) }
    if (showBiometricPrompt.value) {
        showBiometricPrompt.value = false
        val executor = ContextCompat.getMainExecutor(context)
        val activity = getFragmentActivity(context)
        if (activity != null) {
            val biometricPrompt = BiometricPrompt(activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        PinVault.clearFailures(context)
                        failedAttempts = 0
                        lockoutTimeLeft = 0
                        onAuthSuccess()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            val state = PinVault.registerFailedAttempt(context)
                            failedAttempts = state.failedAttempts
                            lockoutTimeLeft = (state.remainingMillis / 1000L).toInt()
                            if (failedAttempts >= 10) showResetConfirmDialog = true
                        }
                    }
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        val state = PinVault.registerFailedAttempt(context)
                        failedAttempts = state.failedAttempts
                        lockoutTimeLeft = (state.remainingMillis / 1000L).toInt()
                        if (failedAttempts >= 10) showResetConfirmDialog = true
                    }
                })
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación Requerida")
                .setSubtitle("Ingrese su huella o Face ID")
                .setNegativeButtonText("Usar PIN")
                .build()
            biometricPrompt.authenticate(promptInfo)
        }
    }

    LaunchedEffect(Unit) {
        if (biometricsEnabled) {
            showBiometricPrompt.value = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 480.dp)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            if (biometricsEnabled && !showPinMode) {
                // Biometric First Unlock Screen
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = "Huella",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Desbloqueo Biométrico",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Usa tu huella dactilar o rostro para acceder a MegasCU",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                ExpressiveButton(
                    onClick = { showBiometricPrompt.value = true },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                ) {
                    Icon(Icons.Rounded.Fingerprint, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Desbloquear con huella", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                ExpressiveOutlinedButton(
                    onClick = { showPinMode = true },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp)
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Ingresar con PIN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

            } else {
                // PIN Entry Screen
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ingresa tu PIN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (lockoutTimeLeft > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Demasiados intentos. Intenta de nuevo en $lockoutTimeLeft s.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Spacer(modifier = Modifier.height(28.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < enteredPin.length
                            val sizeDp by animateDpAsState(
                                targetValue = if (isFilled) 20.dp else 14.dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "pinIndicatorSize_$i"
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

                    if (error) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                        ) {
                            Text(
                                "PIN incorrecto",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Numpad Expressivo
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(if (biometricsEnabled) "BIO" else "", "0", "DEL")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        for (row in keys) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                for (key in row) {
                                    if (key.isEmpty()) {
                                        Spacer(modifier = Modifier.size(72.dp))
                                    } else {
                                        val interactionSource = remember { MutableInteractionSource() }
                                        val keyShape = rememberExpressiveMorphShape(
                                            defaultRadius = 28.dp,
                                            pressedRadius = 12.dp,
                                            interactionSource = interactionSource
                                        )
                                        Surface(
                                            onClick = {
                                                if (lockoutTimeLeft > 0) return@Surface
                                                if (key == "DEL") {
                                                    if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                } else if (key == "BIO") {
                                                    showPinMode = false
                                                    showBiometricPrompt.value = true
                                                } else {
                                                    if (enteredPin.length < 4) enteredPin += key
                                                }
                                            },
                                            interactionSource = interactionSource,
                                            shape = keyShape,
                                            color = if (key == "BIO" || key == "DEL") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                    else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .size(72.dp)
                                                .expressivePressEffect(interactionSource = interactionSource)
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
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                } else if (key == "BIO") {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Fingerprint,
                                                        contentDescription = "Biometria",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(34.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = key,
                                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (biometricsEnabled) {
                        ExpressiveTextButton(onClick = {
                            showPinMode = false
                            showBiometricPrompt.value = true
                        }) {
                            Text("Usar huella")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }

                    if (failedAttempts >= 3) {
                        ExpressiveTextButton(
                            onClick = { showResetConfirmDialog = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                if (failedAttempts >= 10) "Restablecer aplicación ($failedAttempts intentos)"
                                else "¿Olvidaste tu PIN? Restablecer ($failedAttempts/10)"
                            )
                        }
                    }
                }
            }
        }
        }

        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                modifier = Modifier.expressiveModalEntrance(),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Restablecer Configuración",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Has alcanzado $failedAttempts intentos fallidos. ¿Deseas restablecer la aplicación?\n\nSe borrará únicamente la configuración local de la app y el PIN para volver al menú de Bienvenida.\n\nNota: Tu Saldo Principal, paquetes de datos, datos móviles, bonos, SMS y minutos con Cubacel se mantendrán intactos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    ExpressiveButton(
                        onClick = {
                            showResetConfirmDialog = false
                            PinVault.clearFailures(context)
                            failedAttempts = 0
                            lockoutTimeLeft = 0
                            onResetApp?.invoke()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Restablecer App", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    ExpressiveTextButton(
                        onClick = {
                            showResetConfirmDialog = false
                        }
                    ) {
                        Text("Cancelar")
                    }
                },
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}
