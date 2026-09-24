package com.ams.megascu.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Facebook
import compose.icons.fontawesomeicons.brands.Github
import compose.icons.fontawesomeicons.brands.Instagram
import compose.icons.fontawesomeicons.brands.Telegram
import compose.icons.fontawesomeicons.brands.Whatsapp
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.PathParser
import com.ams.megascu.BuildConfig
import com.ams.megascu.R
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.rounded.BugReport
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedAboutLogo(playAnimation: Boolean, isSuperSaiyan: Boolean = false) {
    val shieldScale = remember { Animatable(0f) }
    val bar1Alpha = remember { Animatable(0f) }
    val bar2Alpha = remember { Animatable(0f) }
    val bar3Alpha = remember { Animatable(0f) }
    val bar4Alpha = remember { Animatable(0f) }

    val auraTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by auraTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )
    val auraAlpha by auraTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    LaunchedEffect(playAnimation) {
        if (playAnimation) {
            shieldScale.snapTo(0f)
            bar1Alpha.snapTo(0f)
            bar2Alpha.snapTo(0f)
            bar3Alpha.snapTo(0f)
            bar4Alpha.snapTo(0f)

            // 1. Escudo aparece con rebote acelerado
            shieldScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )

            // 2. Barras de señal se activan con animación ágil
            val spec = tween<Float>(durationMillis = 30, easing = LinearEasing)
            bar1Alpha.animateTo(1f, animationSpec = spec)
            bar2Alpha.animateTo(1f, animationSpec = spec)
            bar3Alpha.animateTo(1f, animationSpec = spec)
            bar4Alpha.animateTo(1f, animationSpec = spec)
        }
    }

    val shieldPath = remember { PathParser.createPathFromPathData("M12,3.5 L5,6.2 V12 C5,16.5 8.1,20.2 12,21.5 C15.9,20.2 19,16.5 19,12 V6.2 L12,3.5 Z") }
    val bar1Path = remember { PathParser.createPathFromPathData("M 8.25,15.5 V 13.5") }
    val bar2Path = remember { PathParser.createPathFromPathData("M 10.75,15.5 V 11.5") }
    val bar3Path = remember { PathParser.createPathFromPathData("M 13.25,15.5 V 9.5") }
    val bar4Path = remember { PathParser.createPathFromPathData("M 15.75,15.5 V 7.5") }

    val logoColor = Color.White

    Box(contentAlignment = Alignment.Center) {
        if (isSuperSaiyan) {
            // Aura Dorada Radiante
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        scaleX = auraScale
                        scaleY = auraScale
                        alpha = auraAlpha
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFF9900),
                                Color(0xFFFF4500),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Surface(
            shape = CircleShape,
            color = if (isSuperSaiyan) Color(0xFF1E1600) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isSuperSaiyan) {
                    // Renderizado 8-Bit Retro Pixel Art
                    Canvas(modifier = Modifier.size(72.dp)) {
                        val gridSize = 16f
                        val px = size.width / gridSize
                        val goldColor = Color(0xFFFFD700)
                        val cyanAccent = Color(0xFF00E5FF)
                        val darkShieldBg = Color(0xFF0D1B2A)
                        val barColor = Color(0xFFFFFFFF)

                        fun drawPixel(x: Int, y: Int, color: Color) {
                            drawRect(
                                color = color,
                                topLeft = Offset(x * px, y * px),
                                size = androidx.compose.ui.geometry.Size(px, px)
                            )
                        }

                        // 8-bit Shield Outer Border (Gold)
                        val shieldBorderPixels = listOf(
                            3 to 2, 4 to 2, 5 to 2, 6 to 2, 7 to 2, 8 to 2, 9 to 2, 10 to 2, 11 to 2, 12 to 2,
                            2 to 3, 13 to 3,
                            2 to 4, 13 to 4,
                            2 to 5, 13 to 5,
                            2 to 6, 13 to 6,
                            2 to 7, 13 to 7,
                            2 to 8, 13 to 8,
                            2 to 9, 13 to 9,
                            3 to 10, 12 to 10,
                            4 to 11, 11 to 11,
                            5 to 12, 10 to 12,
                            6 to 13, 9 to 13,
                            7 to 14, 8 to 14
                        )
                        shieldBorderPixels.forEach { (x, y) -> drawPixel(x, y, goldColor) }

                        // Inner Cyan Accent Line
                        val innerBorder = listOf(
                            3 to 3, 4 to 3, 5 to 3, 6 to 3, 7 to 3, 8 to 3, 9 to 3, 10 to 3, 11 to 3, 12 to 3,
                            3 to 4, 12 to 4, 3 to 5, 12 to 5, 3 to 6, 12 to 6, 3 to 7, 12 to 7, 3 to 8, 12 to 8, 3 to 9, 12 to 9,
                            4 to 10, 11 to 10, 5 to 11, 10 to 11, 6 to 12, 9 to 12, 7 to 13, 8 to 13
                        )
                        innerBorder.forEach { (x, y) -> drawPixel(x, y, cyanAccent) }

                        // 8-bit Shield Fill (Dark Shield Interior)
                        for (y in 4..12) {
                            for (x in 4..11) {
                                if ((x to y) !in shieldBorderPixels && (x to y) !in innerBorder) {
                                    drawPixel(x, y, darkShieldBg)
                                }
                            }
                        }

                        // 8-Bit 4 Cellular Signal Coverage Bars
                        val fourSignalBars = listOf(
                            4 to listOf(9, 10),              // Bar 1 (shortest - height 2)
                            6 to listOf(8, 9, 10),           // Bar 2 (height 3)
                            8 to listOf(7, 8, 9, 10),        // Bar 3 (height 4)
                            10 to listOf(6, 7, 8, 9, 10)     // Bar 4 (tallest - height 5)
                        )
                        fourSignalBars.forEach { (col, rows) ->
                            rows.forEach { row ->
                                drawPixel(col, row, barColor)
                            }
                        }
                    }
                } else {
                    Canvas(modifier = Modifier.size(72.dp)) {
                        val scaleFactor = size.width / 24f
                        withTransform({
                            scale(scaleFactor, scaleFactor, pivot = Offset.Zero)
                        }) {
                            // Escudo animado blanco
                            withTransform({
                                scale(shieldScale.value, shieldScale.value, pivot = Offset(12f, 12f))
                            }) {
                                drawPath(
                                    path = shieldPath.asComposePath(),
                                    color = logoColor,
                                    style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }

                            // Barras animadas blancas
                            drawPath(
                                path = bar1Path.asComposePath(),
                                color = logoColor.copy(alpha = bar1Alpha.value),
                                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawPath(
                                path = bar2Path.asComposePath(),
                                color = logoColor.copy(alpha = bar2Alpha.value),
                                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawPath(
                                path = bar3Path.asComposePath(),
                                color = logoColor.copy(alpha = bar3Alpha.value),
                                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            drawPath(
                                path = bar4Path.asComposePath(),
                                color = logoColor.copy(alpha = bar4Alpha.value),
                                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutBottomSheet(
    onDismiss: () -> Unit,
    onOpenChangelog: () -> Unit = {},
    onProgress: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var playLogoAnim by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var isSuperSaiyan by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(200) // Pausa inicial breve al abrir la ventana antes de animar el logo
        playLogoAnim = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "aboutModalProgress"
    )

    LaunchedEffect(animatedProgress) {
        onProgress(animatedProgress)
        if (animatedProgress == 0f && !isVisible) {
            val action = pendingAction
            if (action != null) {
                action()
            } else {
                onDismiss()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onProgress(0f)
        }
    }

    val requestDismiss: () -> Unit = {
        if (isVisible) {
            pendingAction = null
            isVisible = false
        }
    }

    val requestOpenChangelog: () -> Unit = {
        if (isVisible) {
            pendingAction = onOpenChangelog
            isVisible = false
        }
    }

    BackHandler(enabled = true, onBack = requestDismiss)

    LaunchedEffect(isPressed) {
        if (isPressed && !isSuperSaiyan) {
            delay(3000)
            isSuperSaiyan = true
        }
    }

    if (animatedProgress > 0f || isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = animatedProgress * 0.40f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = requestDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .graphicsLayer {
                        val scale = 0.90f + (0.10f * animatedProgress)
                        scaleX = scale
                        scaleY = scale
                        alpha = animatedProgress.coerceIn(0f, 1f)
                        translationY = (1f - animatedProgress) * 45f
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Evita que clicks dentro de la tarjeta descarten el modal
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Logo con animación calcada del Splash Screen
                    Box(
                        modifier = Modifier.pointerInput(isSuperSaiyan) {
                            detectTapGestures(
                                onPress = {
                                    if (!isSuperSaiyan) {
                                        isPressed = true
                                        tryAwaitRelease()
                                        isPressed = false
                                    }
                                }
                            )
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedAboutLogo(playAnimation = playLogoAnim, isSuperSaiyan = isSuperSaiyan)
                    }

                    if (isSuperSaiyan) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFD700)
                        ) {
                            Text(
                                text = "⚡ SÚPER SAYAYIN 8-BIT ⚡",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append("Megas")
                            }
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                append("CU")
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.app_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    val prefs = remember { context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE) }
                    var devTapCount by remember { mutableIntStateOf(0) }
                    var lastTapTime by remember { mutableLongStateOf(0L) }
                    var isDevModeEnabled by remember { mutableStateOf(prefs.getBoolean("developer_mode_enabled", false)) }

                    Spacer(modifier = Modifier.height(18.dp))

                    val devByText = stringResource(R.string.developed_by)
                    val devNameText = stringResource(R.string.developer_name)
                    // Tarjeta de Desarrollado por (sin línea de contorno)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append("$devByText\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                                        append(devNameText)
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.copyright_text),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón de Versión (colocado debajo de Desarrollado por con animación expressive)
                    val devInteraction = remember { MutableInteractionSource() }
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = if (isDevModeEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            if (!isDevModeEnabled) {
                                val now = System.currentTimeMillis()
                                if (now - lastTapTime > 2000L) {
                                    devTapCount = 0
                                }
                                lastTapTime = now
                                devTapCount++
                                val remaining = 7 - devTapCount
                                if (devTapCount in 3..6) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "¡Estás a $remaining toque(s) de activar el Modo de Pruebas!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else if (devTapCount >= 7) {
                                    isDevModeEnabled = true
                                    prefs.edit().putBoolean("developer_mode_enabled", true).apply()
                                    android.widget.Toast.makeText(
                                        context,
                                        "¡Modo de Pruebas Activado! Revisa la Configuración.",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                    devTapCount = 0
                                }
                            }
                        },
                        interactionSource = devInteraction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .expressivePressEffect(interactionSource = devInteraction)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isDevModeEnabled) {
                                    Icon(
                                        imageVector = Icons.Rounded.BugReport,
                                        contentDescription = "Modo Desarrollador",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = "Versión ${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    color = if (isDevModeEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ExpressiveOutlinedButton(
                        onClick = requestOpenChangelog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.btn_changelog),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.community_and_support),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialIcon(
                            icon = FontAwesomeIcons.Brands.Whatsapp,
                            contentDescription = "WhatsApp",
                            backgroundColor = Color(0xFF25D366)
                        ) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/5353226526")))
                        }
                        SocialIcon(
                            icon = FontAwesomeIcons.Brands.Facebook,
                            contentDescription = "Facebook",
                            backgroundColor = Color(0xFF1877F2)
                        ) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/appmovilshop.cu")))
                        }
                        SocialIcon(
                            icon = FontAwesomeIcons.Brands.Telegram,
                            contentDescription = "Telegram",
                            backgroundColor = Color(0xFF229ED9)
                        ) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/migue_appmovilshop")))
                        }
                        SocialIcon(
                            icon = FontAwesomeIcons.Brands.Instagram,
                            contentDescription = "Instagram",
                            backgroundColor = Color(0xFFE4405F)
                        ) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/appmovilshop")))
                        }
                        SocialIcon(
                            icon = FontAwesomeIcons.Brands.Github,
                            contentDescription = "GitHub",
                            backgroundColor = Color(0xFF24292E)
                        ) {
                            val repoUrl = "https://github.com/${com.ams.megascu.utils.GitHubUpdateChecker.DEFAULT_REPO}"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl)))
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    ExpressiveButton(
                        onClick = requestDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_close),
                            modifier = Modifier.padding(vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// Alias for backwards compatibility if needed
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AboutBottomSheet(onDismiss = onDismiss)
}

@Composable
fun SocialIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    contentDescription: String,
    backgroundColor: Color = Color(0xFF4A00E0),
    onClick: () -> Unit
) {
    val socialInteraction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = socialInteraction,
        modifier = Modifier
            .size(46.dp)
            .expressivePressEffect(interactionSource = socialInteraction),
        color = backgroundColor,
        shape = CircleShape,
        shadowElevation = 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
