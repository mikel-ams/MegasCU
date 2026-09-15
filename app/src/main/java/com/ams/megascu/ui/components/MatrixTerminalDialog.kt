package com.ams.megascu.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ams.megascu.BuildConfig
import com.ams.megascu.ui.theme.SpaceMono
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 8-Bit Retro Terminal Easter Egg Overlay.
 * Características:
 * 1. Simulación de escritura humana aleatoria con pausas, micro-vacilaciones y autocorrección de errores tipográficos (Typo + Backspace).
 * 2. Visualización 100% fondo negro puro sin dependencias de insets edge-to-edge que causen recortes en capas del sistema.
 * 3. Margen seguro inferior estricto y elevado para que los botones y la tarjeta queden siempre 100% visibles.
 * 4. Tipografía permanente Space Mono en todos los textos de la terminal.
 * 5. Crónica hacker de telecomunicaciones incluyendo las reformas tarifarias / tarifazo de 2025 en sintaxis de código/terminal.
 * 6. Post-procesado CRT: Scanlines, viñeta curva de cristal de tubo y micro-glitch aleatorio.
 */
@Composable
fun MatrixTerminalDialog(
    onAmoledChange: ((Boolean) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    // Manejo de botón Atrás del sistema para cerrar el huevo de pascua
    BackHandler(onBack = onDismiss)

    // Activa el tema AMOLED puro mientras el huevo de pascua esté abierto y lo restaura automáticamente al salir
    DisposableEffect(Unit) {
        onAmoledChange?.invoke(true)
        onDispose {
            onAmoledChange?.invoke(false)
        }
    }

    var remainingSeconds by remember { mutableIntStateOf(10) }
    var countdownStarted by remember { mutableStateOf(false) }

    val primaryGreen = Color(0xFF00FF66)
    val containerGreen = Color(0xFF081C0E)

    // Micro-glitch aleatorio para distorsión CRT
    var glitchShiftX by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(1800, 4200))
            glitchShiftX = if (Random.nextBoolean()) Random.nextFloat() * 7f - 3.5f else 0f
            delay(Random.nextLong(50, 130))
            glitchShiftX = 0f
        }
    }

    val terminalLines = remember {
        listOf(
            "[BOOT] MegasCU Core Engine v${BuildConfig.VERSION_NAME} ... [OK]",
            "[INIT] Room DB: megascu.db | USSD Engine: Direct Dial ... [OK]",
            "[MODEM] LTE/4G Baseband Driver (Cubacel/ETECSA) ... [LOADED]",
            "[SEC] SMS Interceptor & Balance Parser ... [ACTIVE]",
            "",
            "C:\\MS-DOS\\MEGAS> EXEC_ANTENA_E-TCS.EXE --HISTORIC-BOOST --OVERRIDE-TARIFF-2025",
            "",
            "[CORE] Escaneando nodos de red y tarifas...",
            "[SYS_1991] Red analógica AMPS/TDMA inicial...",
            "[SYS_2003] Migración a GSM 900 MHz...",
            "[DATA_2014] Activación enlace @nauta.cu...",
            "[NET_2019] Expansión a 4G/LTE 1800 MHz...",
            "[SYS_2021] Enlace Pasarela Transfermóvil...",
            "[ALERT_2025] Detectada Resolución Tarifaria 2025...",
            "[SEC_OVERRIDE] Inyectando bypass en cuotas y paquetes 2025...",
            "[OK] Conexión interceptada en repetidor E-TCS.",
            "",
            ">>> ¡OVERCLOCK DE RED 4G-LTE ACTIVADO! <<<",
            "Velocidad aumentada al máximo por 10 segundos.",
            "¡Aprovecha para consumir tus megas a la velocidad de la luz!"
        )
    }

    var typedLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentTypingLine by remember { mutableStateOf("") }
    var activeLineIndex by remember { mutableIntStateOf(0) }
    var showCursor by remember { mutableStateOf(true) }

    // Simulación de escritura humana aleatoria con pausas, micro-vacilaciones y autocorrección de errores tipográficos
    LaunchedEffect(Unit) {
        // Pausa inicial de 2 segundos al abrir el huevo de pascua
        delay(2000)

        val toneGen = try {
            ToneGenerator(AudioManager.STREAM_SYSTEM, 22)
        } catch (e: Exception) {
            null
        }
        val completed = mutableListOf<String>()

        for (i in terminalLines.indices) {
            activeLineIndex = i
            val lineText = terminalLines[i]

            if (lineText.isEmpty()) {
                completed.add("")
                typedLines = completed.toList()
                currentTypingLine = ""
                delay(120)
                continue
            }

            val sb = StringBuilder()

            // 0. Inicio rápido de booteo con datos de la app
            if (lineText.startsWith("[BOOT]") || lineText.startsWith("[INIT]") || lineText.startsWith("[MODEM]") || (lineText.startsWith("[SEC]") && !lineText.startsWith("[SEC_OVERRIDE]"))) {
                for (char in lineText) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 5)
                    delay(Random.nextLong(10, 22))
                }
            }
            // 1. Error de tipeo simulado en la línea de comando MS-DOS
            else if (lineText.startsWith("C:\\MS-DOS\\MEGAS>")) {
                val typoPart = "C:\\MS-DOS\\MEGAS> EXEC_ANMTEN"
                for (char in typoPart) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)
                    delay(Random.nextLong(35, 70))
                }
                delay(240) // Pausa de vacilación humana al notar el error
                // Borrado con Backspace de "MTEN" (4 caracteres)
                for (b in 0..3) {
                    sb.deleteAt(sb.length - 1)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 12)
                    delay(Random.nextLong(55, 95))
                }
                delay(150) // Pausa antes de continuar correctamente
                // Resto de la línea
                val remaining = lineText.substring("C:\\MS-DOS\\MEGAS> EXEC_AN".length)
                for (char in remaining) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)
                    delay(Random.nextLong(28, 58))
                }
            }
            // 2. Error de tipeo simulado en la detección del tarifazo 2025
            else if (lineText.startsWith("[ALERT_2025]")) {
                val typoPart = "[ALERT_2025] Detectada RESOLUXI"
                for (char in typoPart) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)
                    delay(Random.nextLong(32, 65))
                }
                delay(220) // Pausa de vacilación humana al notar el error
                // Borrado con Backspace de "XI" (2 caracteres)
                for (b in 0..1) {
                    sb.deleteAt(sb.length - 1)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 12)
                    delay(Random.nextLong(60, 95))
                }
                delay(120) // Pausa antes de continuar correctamente
                // Resto de la línea ("ción Tarifaria 2025...")
                val remaining = lineText.substring(29)
                for (char in remaining) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)
                    delay(Random.nextLong(30, 60))
                }
            }
            // 3. Error de tipeo simulado en el bypass de seguridad
            else if (lineText.startsWith("[SEC_OVERRIDE]")) {
                val typoPart = "[SEC_OVERRIDE] Inyectando bipass"
                for (char in typoPart) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)
                    delay(Random.nextLong(30, 65))
                }
                delay(200) // Pausa de vacilación humana al notar el error
                // Borrado con Backspace de "ipass" (5 caracteres)
                for (b in 0..4) {
                    sb.deleteAt(sb.length - 1)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 12)
                    delay(Random.nextLong(50, 85))
                }
                delay(130) // Pausa antes de continuar correctamente
                // Resto de la línea ("ypass en cuotas y paquetes 2025...")
                val remaining = lineText.substring(27)
                for (char in remaining) {
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)
                    delay(Random.nextLong(28, 55))
                }
            }
            // Escritura normal con cadencia humana variable
            else {
                var charIndex = 0
                while (charIndex < lineText.length) {
                    val char = lineText[charIndex]
                    sb.append(char)
                    currentTypingLine = sb.toString()
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 8)

                    // Retardo realista por carácter
                    val charDelay = when {
                        char == '.' || char == '!' || char == '?' || char == ':' -> Random.nextLong(140, 260)
                        char == ',' || char == ';' -> Random.nextLong(80, 150)
                        char == ' ' -> Random.nextLong(25, 50)
                        char.isUpperCase() || char.isDigit() -> Random.nextLong(32, 65)
                        lineText.startsWith(">>>") -> Random.nextLong(18, 38)
                        else -> Random.nextLong(28, 55)
                    }

                    // Micro-pausa de pensamiento humano ocasional cada 20-30 caracteres
                    if (charIndex > 0 && charIndex % 25 == 0 && Random.nextFloat() < 0.3f) {
                        delay(Random.nextLong(120, 240))
                    } else {
                        delay(charDelay)
                    }

                    charIndex++
                }
            }

            completed.add(lineText)
            typedLines = completed.toList()
            currentTypingLine = ""
            delay(Random.nextLong(160, 320)) // Pausa al finalizar cada línea
        }

        // Iniciar cuenta regresiva de 10 segundos al culminar la terminal
        countdownStarted = true
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
        }
        toneGen?.release()
    }

    // Cursor parpadeante estilo DOS
    LaunchedEffect(Unit) {
        while (true) {
            delay(320)
            showCursor = !showCursor
        }
    }

    // Animación de flicker CRT
    val infiniteTransition = rememberInfiniteTransition(label = "CrtEffects")
    val crtFlickerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(90, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crtFlickerAlpha"
    )

    // Contenedor Full-Screen Nativo dentro de la Activity
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 48.dp)
                .alpha(crtFlickerAlpha)
                .graphicsLayer {
                    translationX = glitchShiftX
                }
        ) {
            // --- HEADER DE TERMINAL ---
            ContainerHeader(containerGreen = containerGreen, primaryGreen = primaryGreen)

            Spacer(modifier = Modifier.height(10.dp))

            // --- CUERPO DEL TEXTO (TERMINAL SCROLLABLE) ---
            val scrollState = rememberScrollState()
            LaunchedEffect(typedLines.size, currentTypingLine) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 4.dp)
            ) {
                typedLines.forEach { line ->
                    RenderTerminalLine(line = line, primaryGreen = primaryGreen)
                }

                if (activeLineIndex < terminalLines.size && currentTypingLine.isNotEmpty()) {
                    RenderTerminalLine(
                        line = currentTypingLine + if (showCursor) "█" else "",
                        primaryGreen = primaryGreen
                    )
                } else if (activeLineIndex >= terminalLines.size - 1 && currentTypingLine.isEmpty()) {
                    if (showCursor) {
                        Text(
                            text = "█",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = SpaceMono,
                                fontSize = 13.sp
                            ),
                            color = primaryGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- CAJA DE ESTADO Y TEMPORIZADOR ---
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = containerGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (countdownStarted) "ESTADO: 4G-LTE OVERCLOCK" else "ESTADO: EJECUTANDO SCRIPT...",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = primaryGreen
                    )
                    if (countdownStarted) {
                        Text(
                            text = "${remainingSeconds}s",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = SpaceMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- BOTÓN CERRAR CON MARGEN SEGURO INFERIOR ELEVADO (+40dp) ---
            ExpressiveButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = "[ CERRAR TERMINAL ]",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // --- POST-PROCESADO DE IMAGEN CRT (Scanlines + Edge Vignette + Glitch) ---
        CrtPostProcessingOverlay(
            primaryGreen = primaryGreen,
            glitchShiftX = glitchShiftX
        )
    }
}

/**
 * Superposición de monitor CRT con líneas de barrido horizontal, viñeta curva de cristal y glitch aleatorio.
 */
@Composable
private fun BoxScope.CrtPostProcessingOverlay(
    primaryGreen: Color,
    glitchShiftX: Float
) {
    Canvas(
        modifier = Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(20.dp))
    ) {
        val w = size.width
        val h = size.height

        // 1. Horizontal Scanlines (Textura de líneas CRT)
        val lineSpacing = 3.dp.toPx()
        var y = 0f
        while (y < h) {
            drawLine(
                color = Color.Black.copy(alpha = 0.22f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.2f
            )
            y += lineSpacing
        }

        // 2. Viñeta de cristal curvo CRT (Oscurecimiento realista en los bordes de la pantalla)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.15f),
                    Color.Black.copy(alpha = 0.55f),
                    Color.Black.copy(alpha = 0.88f)
                ),
                center = Offset(w / 2f, h / 2f),
                radius = Math.max(w, h) * 0.62f
            )
        )

        // 3. Línea de interferencia / glitch aleatorio
        if (glitchShiftX != 0f) {
            val glitchY = (h * 0.4f) + (glitchShiftX * 12f)
            drawRect(
                color = primaryGreen.copy(alpha = 0.35f),
                topLeft = Offset(0f, glitchY),
                size = Size(w, 3.dp.toPx())
            )
            drawRect(
                color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                topLeft = Offset(glitchShiftX * 4f, glitchY + 3.dp.toPx()),
                size = Size(w, 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun ContainerHeader(
    containerGreen: Color,
    primaryGreen: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerGreen,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> MegasCU v${BuildConfig.VERSION_NAME} [KERNEL]",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = SpaceMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = primaryGreen
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(primaryGreen, shape = CircleShape)
            )
        }
    }
}

@Composable
private fun RenderTerminalLine(
    line: String,
    primaryGreen: Color
) {
    if (line.isEmpty()) {
        Spacer(modifier = Modifier.height(5.dp))
        return
    }

    val textColor = when {
        line.startsWith("C:\\") -> Color(0xFF00FFCC)
        line.startsWith("[SYS") -> Color(0xFF00FF99)
        line.startsWith("[CORE") -> Color(0xFF00FF88)
        line.startsWith("[KERNEL") -> Color(0xFF00FF99)
        line.startsWith("[PATCH") -> Color(0xFF33FFCC)
        line.startsWith("[DATA") -> Color(0xFF00FFCC)
        line.startsWith("[NET") -> Color(0xFF33FF99)
        line.startsWith("[ALERT") -> Color(0xFFFF5555)
        line.startsWith("[SEC") -> Color(0xFF66FF99)
        line.startsWith("[EXPLOIT") -> Color(0xFF00FFCC)
        line.startsWith("[OK]") -> Color(0xFF00FF66)
        line.startsWith("[WARN") -> Color(0xFFFFCC00)
        line.startsWith(">>>") -> Color(0xFF00FF66)
        else -> primaryGreen
    }

    val isHighlight = line.startsWith(">>>") || line.startsWith("Velocidad") || line.startsWith("¡Aprovecha")

    Text(
        text = line,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = SpaceMono,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (isHighlight) 12.sp else 11.2.sp,
            lineHeight = 15.5.sp
        ),
        color = textColor,
        modifier = Modifier.padding(vertical = 0.7.dp)
    )
}
