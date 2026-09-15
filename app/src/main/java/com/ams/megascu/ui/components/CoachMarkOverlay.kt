package com.ams.megascu.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RefreshCoachMarkOverlay(
    onDismiss: () -> Unit,
    onRefreshClick: () -> Unit,
    isSimpleMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.85f)
                    )
                )
            )
            .clickable { onDismiss() }
    ) {
        if (!isSimpleMode) {
            // NORMAL MODE
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cursive Expressive Handwritten Text in White with dark drop shadow
                Text(
                    text = "Pulsa aquí para obtener datos de saldo, megas y más...",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Cursive,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 0.8.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.95f),
                            offset = Offset(2.5f, 2.5f),
                            blurRadius = 8f
                        )
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { rotationZ = -1.5f }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Discontinuous (dashed) hand-drawn line with loop and arrow head pointing to refresh button
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 450.dp)
                        .height(120.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val cx = w * 0.5f
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 16f), 0f)
                    val curveScale = (h / 120.dp.toPx()).coerceIn(0.8f, 1.15f)

                    val linePath = Path().apply {
                        moveTo(cx, 10f)
                        // Curve down-left entering the loop
                        cubicTo(
                            cx - (35f * curveScale), h * 0.25f,
                            cx - (45f * curveScale), h * 0.45f,
                            cx - (15f * curveScale), h * 0.55f
                        )
                        // Loop (vuelta) around to right and back
                        cubicTo(
                            cx + (25f * curveScale), h * 0.65f,
                            cx + (45f * curveScale), h * 0.40f,
                            cx + (10f * curveScale), h * 0.45f
                        )
                        // Head straight down to bottom bar button
                        cubicTo(
                            cx - (10f * curveScale), h * 0.55f,
                            cx + (5f * curveScale), h * 0.85f,
                            cx, h - 10.dp.toPx()
                        )
                    }

                    // Black shadow outline for contrast over light themes (dashed)
                    drawPath(
                        path = linePath,
                        color = Color.Black.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = dashEffect
                        )
                    )
                    // White hand-drawn dashed stroke
                    drawPath(
                        path = linePath,
                        color = Color.White,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = dashEffect
                        )
                    )

                    // Arrow head wings pointing down
                    val tipY = h - 10.dp.toPx()
                    val arrowHeadPath = Path().apply {
                        moveTo(cx - 12.dp.toPx(), tipY - 16.dp.toPx())
                        lineTo(cx, tipY)
                        lineTo(cx + 12.dp.toPx(), tipY - 16.dp.toPx())
                    }

                    drawPath(
                        path = arrowHeadPath,
                        color = Color.Black.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    drawPath(
                        path = arrowHeadPath,
                        color = Color.White,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Highlighting refresh button at the bottom center
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseScale"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseAlpha"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .background(Color.White, CircleShape)
                )

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(56.dp)
                        .clickable {
                            onRefreshClick()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        } else {
            // SIMPLE MODE (Letra Grande y legible, señalización manuscrita apuntando al botón FAB)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 92.dp)
                    .fillMaxWidth(0.92f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Texto expresivo, legible y con sombra de contraste
                Text(
                    text = "Pulsa aquí para actualizar tus saldos y datos...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Cursive,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        letterSpacing = 0.8.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.95f),
                            offset = Offset(3f, 3f),
                            blurRadius = 12f
                        )
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { rotationZ = -1.2f }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Trazo punteado estilizado con bucle fluido apuntando hacia el botón FAB inferior derecho
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .height(145.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val startX = (w * 0.48f).coerceIn(w * 0.35f, w * 0.55f)
                    val startY = 4f
                    val endX = (w * 0.90f).coerceAtMost(w - 20.dp.toPx())
                    val endY = h - 4.dp.toPx()
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                    val curveScale = (h / 145.dp.toPx()).coerceIn(0.8f, 1.15f)

                    val linePath = Path().apply {
                        moveTo(startX, startY)
                        // Curva descendente suave hacia la izquierda
                        cubicTo(
                            startX - (35f * curveScale), h * 0.22f,
                            startX - (55f * curveScale), h * 0.48f,
                            startX - (15f * curveScale), h * 0.62f
                        )
                        // Bucle (vuelta) estilizado hacia arriba a la derecha
                        cubicTo(
                            startX + (32f * curveScale), h * 0.74f,
                            startX + (58f * curveScale), h * 0.36f,
                            startX + (18f * curveScale), h * 0.42f
                        )
                        // Trayectoria final fluida descendiendo directo al FAB
                        cubicTo(
                            startX + (4f * curveScale), h * 0.58f,
                            endX - (48f * curveScale), endY - (38f * curveScale),
                            endX, endY
                        )
                    }

                    // Sombra / contorno negro grueso para máximo contraste
                    drawPath(
                        path = linePath,
                        color = Color.Black.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = dashEffect
                        )
                    )
                    // Trazo blanco luminoso
                    drawPath(
                        path = linePath,
                        color = Color.White,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = dashEffect
                        )
                    )

                    // Punta de flecha direccional apuntando hacia el FAB
                    val arrowHeadPath = Path().apply {
                        moveTo(endX - 16.dp.toPx(), endY - 8.dp.toPx())
                        lineTo(endX, endY)
                        lineTo(endX - 5.dp.toPx(), endY - 18.dp.toPx())
                    }

                    drawPath(
                        path = arrowHeadPath,
                        color = Color.Black.copy(alpha = 0.85f),
                        style = Stroke(
                            width = 7.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    drawPath(
                        path = arrowHeadPath,
                        color = Color.White,
                        style = Stroke(
                            width = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Botón FAB resaltado en la esquina inferior derecha sin sombra
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, end = 16.dp)
                    .size(68.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulseSimple")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseScaleSimple"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "pulseAlphaSimple"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .background(Color.White, RoundedCornerShape(22.dp))
                )

                Surface(
                    onClick = {
                        onRefreshClick()
                    },
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Actualizar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

data class CoachMarkStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val highlightTag: String,
    val badgeText: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CoachMarkOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        CoachMarkStep(
            title = "Contadores en Tiempo Real",
            description = "Monitorea tu saldo principal, megas de datos (Navegación general, LTE y Bono .CU), minutos y SMS actualizados con lectura automática y soporte para Doble SIM.",
            icon = Icons.Rounded.BarChart,
            highlightTag = "Dashboard",
            badgeText = "Paso 1 de 5"
        ),
        CoachMarkStep(
            title = "Consultas USSD Inteligentes",
            description = "Ejecuta rápido tus consultas de saldo y paquetes (*222#, *222*328#, etc.) con un solo toque y sincronización instantánea de respuestas SMS de Cubacel.",
            icon = Icons.Rounded.FlashOn,
            highlightTag = "Consultas",
            badgeText = "Paso 2 de 5"
        ),
        CoachMarkStep(
            title = "Planes y Combos Cubacel",
            description = "Explora, calcula la tasa de ahorro y compra paquetes de datos, bonos o combos de ETECSA fácilmente sin memorizar ningún código USSD.",
            icon = Icons.Rounded.ShoppingBag,
            highlightTag = "Planes",
            badgeText = "Paso 3 de 5"
        ),
        CoachMarkStep(
            title = "Guía Offline y Herramientas",
            description = "Accede al manual offline, biblioteca de códigos secretos, historial de consumo detallado y widgets para la pantalla de inicio sin consumir megas.",
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            highlightTag = "Guía",
            badgeText = "Paso 4 de 5"
        ),
        CoachMarkStep(
            title = "Seguridad y Control de Consumo",
            description = "Protege la app activando el PIN de seguridad de 4 dígitos o huella dactilar, ajusta alertas de vencimiento y personaliza umbrales de datos bajos.",
            icon = Icons.Rounded.Security,
            highlightTag = "Seguridad",
            badgeText = "Paso 5 de 5"
        )
    )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Badge indicator
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentStep.badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Card content with combined slide, fade and scale transitions for Material 3 Expressive
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(360, easing = FastOutSlowInEasing)) { width -> width / 3 } + 
                         fadeIn(animationSpec = tween(360)) +
                         scaleIn(initialScale = 0.94f, animationSpec = tween(360))).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> -width / 3 } + 
                            fadeOut(animationSpec = tween(300)) +
                            scaleOut(targetScale = 0.94f, animationSpec = tween(300))
                        )
                    } else {
                        (slideInHorizontally(animationSpec = tween(360, easing = FastOutSlowInEasing)) { width -> -width / 3 } + 
                         fadeIn(animationSpec = tween(360)) +
                         scaleIn(initialScale = 0.94f, animationSpec = tween(360))).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300, easing = FastOutSlowInEasing)) { width -> width / 3 } + 
                            fadeOut(animationSpec = tween(300)) +
                            scaleOut(targetScale = 0.94f, animationSpec = tween(300))
                        )
                    }
                },
                label = "coach_mark_step"
            ) { stepIdx ->
                val step = steps[stepIdx]
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Page Dots Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.indices.forEach { index ->
                                val isSelected = index == stepIdx
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isSelected) 32.dp else 8.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "dotWidth"
                                )
                                val dotColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    animationSpec = tween(250),
                                    label = "dotColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(dotWidth, 8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Navigation Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (currentStepIndex > 0) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    ExpressiveOutlinedButton(
                        onClick = { currentStepIndex-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Anterior",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Anterior",
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                ExpressiveButton(
                    onClick = {
                        if (currentStepIndex < steps.size - 1) {
                            currentStepIndex++
                        } else {
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 125.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStepIndex == steps.size - 1) "¡Entendido!" else "Siguiente",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (currentStepIndex < steps.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Siguiente",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UssdTutorialOverlay(onDismiss: () -> Unit) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val tutorialSteps = remember {
        listOf(
            CoachMarkStep(
                title = "Paso 1: Consulta de Saldo USSD",
                description = "Toca el botón flotante circular de actualización (*222# o *222*328#) para iniciar la consulta. No requiere datos móviles ni conexión a internet.",
                icon = Icons.Rounded.Refresh,
                highlightTag = "Consulta Instantánea",
                badgeText = "Paso 1 de 5"
            ),
            CoachMarkStep(
                title = "Paso 2: Procesamiento del Operador",
                description = "El código USSD se ejecuta directamente en la red de Cubacel. La respuesta se procesa de forma transparente en segundo plano en pocos segundos.",
                icon = Icons.Rounded.PhonelinkRing,
                highlightTag = "Modo Offline",
                badgeText = "Paso 2 de 5"
            ),
            CoachMarkStep(
                title = "Paso 3: Sincronización de Bolsas",
                description = "MegasCU analiza automáticamente los mensajes y desglosa tu Saldo Principal, Bolsa Datos (General y LTE), Bono .CU, Minutos y SMS.",
                icon = Icons.Rounded.PieChart,
                highlightTag = "Desglose Inteligente",
                badgeText = "Paso 3 de 5"
            ),
            CoachMarkStep(
                title = "Paso 4: Análisis de Consumo",
                description = "Visualiza el ritmo de consumo con gráficos interactivos y recibe alertas automáticas de vencimiento antes de que caduquen tus paquetes.",
                icon = Icons.AutoMirrored.Rounded.ShowChart,
                highlightTag = "Alertas y Gráficas",
                badgeText = "Paso 4 de 5"
            ),
            CoachMarkStep(
                title = "Paso 5: Compras y Atajos Útiles",
                description = "Explora la tienda de paquetes de datos, calcula tarifas de ahorro, añade widgets a la pantalla de inicio y gestiona Doble SIM fácilmente.",
                icon = Icons.Rounded.ShoppingBag,
                highlightTag = "Herramientas Avanzadas",
                badgeText = "Paso 5 de 5"
            )
        )
    }

    val currentStep = tutorialSteps[currentStepIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.70f),
                        Color.Black.copy(alpha = 0.92f)
                    ),
                    radius = 1200f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Bar: Step Counter Pill + Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentStep.badgeText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                ExpressiveIconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Animated Content Box with M3 Expressive Transitions
            AnimatedContent(
                targetState = currentStepIndex,
                transitionSpec = {
                    val cubicEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                    
                    if (targetState > initialState) {
                        (slideInHorizontally(animationSpec = tween(360, easing = cubicEasing)) { width -> width / 2 } +
                         fadeIn(animationSpec = tween(360)) +
                         scaleIn(initialScale = 0.90f, animationSpec = tween(360))).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300, easing = cubicEasing)) { width -> -width / 2 } +
                            fadeOut(animationSpec = tween(300)) +
                            scaleOut(targetScale = 0.90f, animationSpec = tween(300))
                        )
                    } else {
                        (slideInHorizontally(animationSpec = tween(360, easing = cubicEasing)) { width -> -width / 2 } +
                         fadeIn(animationSpec = tween(360)) +
                         scaleIn(initialScale = 0.90f, animationSpec = tween(360))).togetherWith(
                            slideOutHorizontally(animationSpec = tween(300, easing = cubicEasing)) { width -> width / 2 } +
                            fadeOut(animationSpec = tween(300)) +
                            scaleOut(targetScale = 0.90f, animationSpec = tween(300))
                        )
                    }
                },
                label = "ussd_tutorial_step"
            ) { stepIdx ->
                val stepData = tutorialSteps[stepIdx]
                
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Glowing Ring Icon Container
                        Box(contentAlignment = Alignment.Center) {
                            val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.35f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "pulseScale"
                            )
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.60f,
                                targetValue = 0.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "pulseAlpha"
                            )

                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .graphicsLayer {
                                        scaleX = pulseScale
                                        scaleY = pulseScale
                                        alpha = pulseAlpha
                                    }
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
                            )

                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = stepData.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Category Pill Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = stepData.highlightTag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = stepData.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stepData.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Expressive Page Dots Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tutorialSteps.indices.forEach { index ->
                                val isSelected = index == stepIdx
                                val dotWidth by animateDpAsState(
                                    targetValue = if (isSelected) 32.dp else 8.dp,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "dotWidth"
                                )
                                val dotColor by animateColorAsState(
                                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                    animationSpec = tween(250),
                                    label = "dotColor"
                                )

                                Box(
                                    modifier = Modifier
                                        .size(dotWidth, 8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (currentStepIndex > 0) Arrangement.SpaceBetween else Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    ExpressiveOutlinedButton(
                        onClick = { currentStepIndex-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Anterior",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Anterior",
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                ExpressiveButton(
                    onClick = {
                        if (currentStepIndex < tutorialSteps.size - 1) {
                            currentStepIndex++
                        } else {
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 125.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentStepIndex == tutorialSteps.size - 1) "¡Entendido!" else "Siguiente",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        if (currentStepIndex < tutorialSteps.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Siguiente",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
