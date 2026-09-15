package com.ams.megascu.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.roundToInt
import com.ams.megascu.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onRequestPermissions: () -> Unit,
    onRequestPhonePermission: () -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    onRequestUsageStatsPermission: () -> Unit = {},
    hasPhonePermission: Boolean,
    hasNotificationPermission: Boolean,
    hasUsageStatsPermission: Boolean = false,
    themeMode: String = "SYSTEM",
    useDynamicColors: Boolean = true,
    useMonospace: Boolean = false,
    isSimpleMode: Boolean = false,
    dailyLimitAlertsEnabled: Boolean = true,
    dataThresholdAlertEnabled: Boolean = true,
    dataThresholdPercent: Int = 80,
    expirationAlertDays: Int = 5,
    secondExpirationAlertEnabled: Boolean = false,
    secondExpirationAlertDays: Int = 2,
    syncIntervalHours: Int = 6,
    dualSimEnabled: Boolean = false,
    securityEnabled: Boolean = false,
    securityPin: String = "",
    biometricsEnabled: Boolean = false,
    onSelectThemeMode: (String) -> Unit = {},
    onToggleDynamicColors: (Boolean) -> Unit = {},
    onToggleMonospace: (Boolean) -> Unit = {},
    onToggleSimpleMode: (Boolean) -> Unit = {},
    onToggleDailyLimitAlerts: (Boolean) -> Unit = {},
    onToggleDataThresholdAlerts: (Boolean) -> Unit = {},
    onSetDataThresholdPercent: (Int) -> Unit = {},
    onSetExpirationAlertDays: (Int) -> Unit = {},
    onToggleSecondExpirationAlert: (Boolean) -> Unit = {},
    onSetSecondExpirationAlertDays: (Int) -> Unit = {},
    onSetSyncIntervalHours: (Int) -> Unit = {},
    onToggleDualSim: (Boolean) -> Unit = {},
    onToggleSecurity: (Boolean) -> Unit = {},
    onSetSecurityPin: (String) -> Unit = {},
    onToggleBiometrics: (Boolean) -> Unit = {},
    onFinish: () -> Unit
) {
    val totalPages = 8
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    var footerSlotPos by remember { mutableStateOf<Offset?>(null) }
    var footerSlotSize by remember { mutableStateOf<IntSize?>(null) }
    var finishSlotPos by remember { mutableStateOf<Offset?>(null) }
    var finishSlotSize by remember { mutableStateOf<IntSize?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopOnboardingHeader(
                    currentPage = pagerState.currentPage,
                    totalPages = totalPages,
                    onSkip = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(totalPages - 1)
                        }
                    }
                )
            },
            bottomBar = {
                val areAllPermissionsGranted = hasPhonePermission && hasNotificationPermission && hasUsageStatsPermission
                BottomNavigationFooter(
                    currentPage = pagerState.currentPage,
                    totalPages = totalPages,
                    areAllPermissionsGranted = areAllPermissionsGranted,
                    onPrev = {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onFooterSlotPositioned = { pos, size ->
                        footerSlotPos = pos
                        footerSlotSize = size
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                HorizontalPager(
                    state = pagerState,
                    beyondViewportPageCount = 1,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> WelcomePage()
                        1 -> FeaturesShowcasePage()
                        2 -> PermissionsPage(
                            hasPhonePermission = hasPhonePermission,
                            hasNotificationPermission = hasNotificationPermission,
                            hasUsageStatsPermission = hasUsageStatsPermission,
                            onRequestPhonePermission = onRequestPhonePermission,
                            onRequestNotificationPermission = onRequestNotificationPermission,
                            onRequestUsageStatsPermission = onRequestUsageStatsPermission,
                            onRequestAll = {
                                if (!hasPhonePermission || !hasNotificationPermission) {
                                    onRequestPermissions()
                                } else if (!hasUsageStatsPermission) {
                                    onRequestUsageStatsPermission()
                                } else {
                                    onRequestPermissions()
                                }
                            }
                        )
                        3 -> SimpleModePage(
                            isSimpleMode = isSimpleMode,
                            onToggleSimpleMode = onToggleSimpleMode
                        )
                        4 -> CustomizationPage(
                            themeMode = themeMode,
                            useDynamicColors = useDynamicColors,
                            useMonospace = useMonospace,
                            onSelectThemeMode = onSelectThemeMode,
                            onToggleDynamicColors = onToggleDynamicColors,
                            onToggleMonospace = onToggleMonospace
                        )
                        5 -> AlertsAndSyncPage(
                            dataThresholdAlertEnabled = dataThresholdAlertEnabled,
                            dataThresholdPercent = dataThresholdPercent,
                            expirationAlertDays = expirationAlertDays,
                            secondExpirationAlertEnabled = secondExpirationAlertEnabled,
                            secondExpirationAlertDays = secondExpirationAlertDays,
                            dailyLimitAlertsEnabled = dailyLimitAlertsEnabled,
                            syncIntervalHours = syncIntervalHours,
                            onToggleDataThresholdAlerts = onToggleDataThresholdAlerts,
                            onSetDataThresholdPercent = onSetDataThresholdPercent,
                            onSetExpirationAlertDays = onSetExpirationAlertDays,
                            onToggleSecondExpirationAlert = onToggleSecondExpirationAlert,
                            onSetSecondExpirationAlertDays = onSetSecondExpirationAlertDays,
                            onToggleDailyLimitAlerts = onToggleDailyLimitAlerts,
                            onSetSyncIntervalHours = onSetSyncIntervalHours
                        )
                        6 -> ChartsAndSecurityPage(
                            dualSimEnabled = dualSimEnabled,
                            securityEnabled = securityEnabled,
                            securityPin = securityPin,
                            dailyLimitAlertsEnabled = dailyLimitAlertsEnabled,
                            onToggleDualSim = onToggleDualSim,
                            onToggleSecurity = onToggleSecurity,
                            onSetSecurityPin = onSetSecurityPin,
                            onToggleDailyLimitAlerts = onToggleDailyLimitAlerts
                        )
                        7 -> FinishPage(
                            hasAllPermissions = hasPhonePermission && hasNotificationPermission && hasUsageStatsPermission,
                            onFinishSlotPositioned = { pos, size ->
                                finishSlotPos = pos
                                finishSlotSize = size
                            }
                        )
                    }
                }
            }
        }

        // Botón único unificado interactivo desde la pantalla 1 hasta la pantalla 8
        val defaultFooterW = with(density) { 146.dp.toPx() }
        val defaultFooterH = with(density) { 50.dp.toPx() }
        val defaultFooterX = screenWidthPx - with(density) { 24.dp.toPx() } - defaultFooterW
        val defaultFooterY = screenHeightPx - with(density) { 66.dp.toPx() }

        val defaultFinishW = with(density) { 250.dp.toPx() }
        val defaultFinishH = with(density) { 56.dp.toPx() }
        val defaultFinishX = (screenWidthPx - defaultFinishW) / 2f
        val defaultFinishY = with(density) { 340.dp.toPx() }

        val startX = footerSlotPos?.x ?: defaultFooterX
        val startY = footerSlotPos?.y ?: defaultFooterY
        val startW = footerSlotSize?.width?.toFloat() ?: defaultFooterW
        val startH = footerSlotSize?.height?.toFloat() ?: defaultFooterH

        val targetW = finishSlotSize?.width?.toFloat() ?: defaultFinishW
        val targetH = finishSlotSize?.height?.toFloat() ?: defaultFinishH
        val targetX = (finishSlotPos?.x ?: defaultFinishX) + ((finishSlotSize?.width?.toFloat() ?: defaultFinishW) - targetW) / 2f
        val targetY = finishSlotPos?.y ?: defaultFinishY

        val pagePos = pagerState.currentPage + pagerState.currentPageOffsetFraction
        val morphProgress = (pagePos - 6f).coerceIn(0f, 1f)

        val currentX = lerp(startX, targetX, morphProgress)
        val currentY = lerp(startY, targetY, morphProgress)
        val currentW = lerp(startW, targetW, morphProgress)
        val currentH = lerp(startH, targetH, morphProgress)

        val areAllPermissionsGranted = hasPhonePermission && hasNotificationPermission && hasUsageStatsPermission

        ExpressiveButton(
            onClick = {
                if (morphProgress >= 0.5f) {
                    onFinish()
                } else if (pagerState.currentPage == 2 && !areAllPermissionsGranted) {
                    if (!hasPhonePermission || !hasNotificationPermission) {
                        onRequestPermissions()
                    } else if (!hasUsageStatsPermission) {
                        onRequestUsageStatsPermission()
                    } else {
                        onRequestPermissions()
                    }
                } else if (pagerState.currentPage < totalPages - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onFinish()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
            modifier = Modifier
                .layout { measurable, _ ->
                    val w = currentW.roundToInt().coerceAtLeast(1)
                    val h = currentH.roundToInt().coerceAtLeast(1)
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(w, h) {
                        placeable.place(0, 0)
                    }
                }
                .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Estado Pantallas 1 a 7: Siguiente / Otorgar
                val buttonState = if (pagerState.currentPage == 2 && !areAllPermissionsGranted) {
                    FooterButtonState.OTORGAR
                } else {
                    FooterButtonState.SIGUIENTE
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            val p = ((0.5f - morphProgress) / 0.5f).coerceIn(0f, 1f)
                            alpha = p
                            scaleX = 0.85f + 0.15f * p
                            scaleY = 0.85f + 0.15f * p
                        }
                ) {
                    AnimatedContent(
                        targetState = buttonState,
                        transitionSpec = {
                            (fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) +
                             scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(150)) +
                                    scaleOut(targetScale = 0.85f, animationSpec = tween(150))
                                )
                        },
                        label = "unified_button_next_state"
                    ) { state ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (state) {
                                    FooterButtonState.OTORGAR -> "Otorgar"
                                    else -> "Siguiente"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (state == FooterButtonState.OTORGAR) Icons.Rounded.AdminPanelSettings else Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Estado Pantalla 8: Comenzar Experiencia MegasCU
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .graphicsLayer {
                            val p = ((morphProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)
                            alpha = p
                            scaleX = 0.85f + 0.15f * p
                            scaleY = 0.85f + 0.15f * p
                        }
                ) {
                    Text(
                        text = "Comenzar Experiencia\nMegasCU",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

/**
 * Contenedor con difuminado superior e inferior progresivo e inteligente.
 * Solo es visible si el contenido excede la pantalla y requiere desplazamiento,
 * apareciendo y desvaneciéndose armónicamente con el scroll.
 */
@Composable
private fun ProgressiveFadedBox(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    fadeHeight: Dp = 22.dp,
    fadeColor: Color = MaterialTheme.colorScheme.background,
    showScrollIndicator: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        content()

        // Difuminado superior: solo visible si se requiere desplazamiento y el usuario ha scrolleado hacia abajo
        val topAlpha by remember {
            derivedStateOf {
                if (scrollState.maxValue > 0) {
                    (scrollState.value.toFloat() / 40f).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
        }

        if (topAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .graphicsLayer { alpha = topAlpha }
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                fadeColor.copy(alpha = 0.90f),
                                fadeColor.copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            )
        }

        // Difuminado inferior: solo visible si se requiere desplazamiento y queda contenido por scrollear hacia abajo
        val bottomAlpha by remember {
            derivedStateOf {
                if (scrollState.maxValue > 0) {
                    ((scrollState.maxValue - scrollState.value).toFloat() / 40f).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
        }

        if (bottomAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .graphicsLayer { alpha = bottomAlpha }
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                fadeColor.copy(alpha = 0.40f),
                                fadeColor.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )
        }

        // Indicador de bajar integrado estilo Material M3 Expressive
        if (showScrollIndicator) {
            ScrollDownIndicator(
                scrollState = scrollState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            )
        }
    }
}

/**
 * Señalización animada sutil para indicar la presencia de más contenido debajo.
 * Estilo Material M3 Expressive con fondo de color sólido, sin transparencias ni efectos glassmórficos,
 * manteniendo las dimensiones, forma y la animación de rebote en el chevron.
 */
@Composable
private fun ScrollDownIndicator(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = scrollState.value < scrollState.maxValue,
        enter = fadeIn(animationSpec = tween(280)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "ScrollDownBounce")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ScrollDownBounceY"
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(30.dp)
                .width(54.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    shape = CircleShape
                )
        ) {
            // Indicador chevron interno nítido con animación de rebote M3 Expressive
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Hay más contenido debajo",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(22.dp)
                    .offset(y = offsetY.dp)
            )
        }
    }
}

@Composable
private fun TopOnboardingHeader(
    currentPage: Int,
    totalPages: Int,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.Transparent)
            .height(52.dp)
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step Badge (se mantiene totalmente fijo en posición y tamaño sin saltos verticales)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            modifier = Modifier.height(32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "Paso ${currentPage + 1} de $totalPages",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Skip Button con desvanecimiento fluido y sin alterar la altura del contenedor
        AnimatedVisibility(
            visible = currentPage < totalPages - 1,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            ExpressiveTextButton(
                onClick = onSkip,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Omitir",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

private enum class FooterButtonState {
    OTORGAR,
    SIGUIENTE,
    COMENZAR
}

@Composable
private fun BottomNavigationFooter(
    currentPage: Int,
    totalPages: Int,
    areAllPermissionsGranted: Boolean = true,
    onPrev: () -> Unit,
    onFooterSlotPositioned: (Offset, IntSize) -> Unit = { _, _ -> }
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicators Expressivos
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            for (i in 0 until totalPages) {
                val isSelected = currentPage == i
                val width by animateDpAsState(
                    targetValue = if (isSelected) 36.dp else 10.dp,
                    label = "indicatorWidth"
                )
                val color by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    label = "indicatorColor"
                )

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(10.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Buttons Row Expressivo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                ExpressiveOutlinedButton(
                    onClick = onPrev,
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                    modifier = Modifier.height(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Atrás", fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Slot de anclaje para el botón Siguiente unificado e interactivo
            Box(
                modifier = Modifier
                    .height(50.dp)
                    .onGloballyPositioned { coords ->
                        if (coords.isAttached) {
                            onFooterSlotPositioned(coords.positionInRoot(), coords.size)
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp)
                        .graphicsLayer { alpha = 0f }
                ) {
                    Text(
                        text = if (currentPage == 2 && !areAllPermissionsGranted) "Otorgar" else "Siguiente",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentPage == 2 && !areAllPermissionsGranted) Icons.Rounded.AdminPanelSettings else Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private sealed interface AnimatedIconSource {
    data class Vector(val imageVector: ImageVector) : AnimatedIconSource
    data class Resource(val resId: Int) : AnimatedIconSource
}

// ----------------------------------------------------
// PAGE 0: WELCOME
// ----------------------------------------------------
@Composable
private fun WelcomePage() {
    val iconList = remember {
        listOf(
            AnimatedIconSource.Resource(R.drawable.ic_launcher_foreground),
            AnimatedIconSource.Vector(Icons.Rounded.SimCard),
            AnimatedIconSource.Vector(Icons.Rounded.Smartphone),
            AnimatedIconSource.Vector(Icons.Rounded.DataUsage),
            AnimatedIconSource.Vector(Icons.Rounded.RocketLaunch),
            AnimatedIconSource.Vector(Icons.Rounded.Call),
            AnimatedIconSource.Vector(Icons.Rounded.Sms),
            AnimatedIconSource.Vector(Icons.Rounded.Security),
            AnimatedIconSource.Vector(Icons.Rounded.Bolt),
            AnimatedIconSource.Vector(Icons.Rounded.CellTower)
        )
    }

    var currentIconIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2000L)
            currentIconIndex = (currentIconIndex + 1) % iconList.size
        }
    }

    // Animación de rotación spin rápida al cambiar de ícono
    val rotationAngle by animateFloatAsState(
        targetValue = currentIconIndex * 360f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "WelcomeIconRotationZ"
    )

    val scrollState = rememberScrollState()

    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            // Hero Icon Box sin resplandor
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp,
                    modifier = Modifier.size(86.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = currentIconIndex,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)) +
                             scaleIn(initialScale = 0.85f, animationSpec = tween(durationMillis = 350))) togetherWith
                            (fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)) +
                             scaleOut(targetScale = 1.15f, animationSpec = tween(durationMillis = 350)))
                        },
                        label = "WelcomeIconFadeSpinTransition"
                    ) { targetIndex ->
                        val iconSource = iconList[targetIndex]
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(76.dp)
                                .graphicsLayer {
                                    rotationZ = rotationAngle
                                }
                        ) {
                            when (iconSource) {
                                is AnimatedIconSource.Vector -> {
                                    Icon(
                                        imageVector = iconSource.imageVector,
                                        contentDescription = "Icono animado megascu",
                                        tint = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                is AnimatedIconSource.Resource -> {
                                    Icon(
                                        painter = painterResource(id = iconSource.resId),
                                        contentDescription = "Logo principal MegasCU",
                                        tint = Color.White,
                                        modifier = Modifier.requiredSize(106.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Bienvenido a MegasCU",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Gestiona tu saldo, bonos y consumo de datos móviles Cubacel de forma inteligente, automática y rápida.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Segmented Highlights Card Group
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Segment 1 (Top)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    WelcomeHighlightRow(
                        icon = Icons.Rounded.Bolt,
                        title = "Consultas en 1 Toque",
                        subtitle = "Accede a tu saldo y bonos sin marcar códigos USSD manualmente"
                    )
                }
            }

            // Segment 2 (Middle)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    WelcomeHighlightRow(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Alertas de Expiración",
                        subtitle = "Avisos antes de que expiren tus megas o minutos"
                    )
                }
            }

            // Segment 3 (Bottom)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    WelcomeHighlightRow(
                        icon = Icons.Rounded.DataUsage,
                        title = "Medición de Consumo",
                        subtitle = "Calcula la velocidad y el ritmo de uso diario de tus megas"
                    )
                }
            }
        }
    }
}
}
}

@Composable
private fun WelcomeHighlightRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ----------------------------------------------------
// PAGE 1: FEATURES SHOWCASE
// ----------------------------------------------------
@Composable
private fun FeaturesShowcasePage() {
    val scrollState = rememberScrollState()
    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Diseñado para Cubacel",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Herramientas creadas específicamente para optimizar el servicio móvil en Cuba:",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FeatureCard(
                        icon = Icons.Rounded.PhoneInTalk,
                        title = "Atajos USSD Inteligentes",
                        description = "Ejecuta consultas instantáneas de saldo, bolsa de datos (*222*328#), minutos y SMS con un solo toque."
                    )
                    FeatureCard(
                        icon = Icons.Rounded.Shield,
                        title = "Alertas de Consumo y Vencimiento",
                        description = "Monitorea tus paquetes activos, recibe avisos antes de que expiren y alertas personalizadas de límite de datos."
                    )
                    FeatureCard(
                        icon = Icons.Rounded.AutoGraph,
                        title = "Consumo y Registros Detallados",
                        description = "Analiza tu tasa de consumo diario, estadísticas semanales y mantén un historial organizado de todas tus recargas."
                    )
                    FeatureCard(
                        icon = Icons.Rounded.Lock,
                        title = "PIN y Privacidad Local",
                        description = "Bloqueo con PIN o huella biométrica y almacenamiento 100% privado en tu dispositivo."
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}

// ----------------------------------------------------
// PAGE 2: PERMISSIONS
// ----------------------------------------------------
@Composable
private fun PermissionsPage(
    hasPhonePermission: Boolean,
    hasNotificationPermission: Boolean,
    hasUsageStatsPermission: Boolean,
    onRequestPhonePermission: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestUsageStatsPermission: () -> Unit,
    onRequestAll: () -> Unit
) {
    val scrollState = rememberScrollState()
    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
        Text(
            text = "Permisos de la App",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Para brindarte un servicio automático e integral, activa los siguientes permisos:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta Segmentada: Permisos
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Segmento Superior: Notificaciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                PermissionSegmentRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = "Notificaciones",
                    description = "Envío de alertas a tiempo sobre el vencimiento de tus datos.",
                    isGranted = hasNotificationPermission,
                    onClick = onRequestNotificationPermission
                )
            }

            // Segmento Medio: Llamadas (USSD)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                PermissionSegmentRow(
                    icon = Icons.Rounded.Call,
                    title = "Llamadas (USSD)",
                    description = "Ejecución de códigos *222# y compras directas de paquetes.",
                    isGranted = hasPhonePermission,
                    onClick = onRequestPhonePermission
                )
            }

            // Segmento Inferior: Uso de Datos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                PermissionSegmentRow(
                    icon = Icons.Rounded.DataUsage,
                    title = "Uso de Datos",
                    description = "Medición del tráfico consumido por el sistema en tiempo real.",
                    isGranted = hasUsageStatsPermission,
                    onClick = onRequestUsageStatsPermission
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        val areAllGranted = hasPhonePermission && hasNotificationPermission && hasUsageStatsPermission

        AnimatedContent(
            targetState = areAllGranted,
            transitionSpec = {
                (fadeIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) +
                 scaleIn(initialScale = 0.9f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(150)) +
                        scaleOut(targetScale = 0.9f, animationSpec = tween(150))
                    )
            },
            label = "permissions_page_action_transition"
        ) { allGranted ->
            if (allGranted) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "¡Todos los permisos concedidos!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                ExpressiveButton(
                    onClick = onRequestAll,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Solicitar Permisos Requeridos",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
}
}

@Composable
private fun PermissionSegmentRow(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isGranted) Icons.Rounded.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ) {
            Text(
                text = if (isGranted) "Activo" else "Conceder",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

// ----------------------------------------------------
// PAGE 3: CUSTOMIZATION
// ----------------------------------------------------
@Composable
private fun CustomizationPage(
    themeMode: String,
    useDynamicColors: Boolean,
    useMonospace: Boolean,
    onSelectThemeMode: (String) -> Unit,
    onToggleDynamicColors: (Boolean) -> Unit,
    onToggleMonospace: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
        Text(
            text = "Personalización",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Adapta la apariencia visual según tus preferencias:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Theme Options
        Text(
            text = "Modo de Tema",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeCardOption(
                    title = "Sistema",
                    icon = Icons.Rounded.Smartphone,
                    isSelected = themeMode == "SYSTEM",
                    onClick = { onSelectThemeMode("SYSTEM") },
                    modifier = Modifier.weight(1f)
                )
                ThemeCardOption(
                    title = "Claro",
                    icon = Icons.Rounded.LightMode,
                    isSelected = themeMode == "LIGHT",
                    badge = "Recomendado",
                    onClick = { onSelectThemeMode("LIGHT") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeCardOption(
                    title = "Oscuro",
                    icon = Icons.Rounded.DarkMode,
                    isSelected = themeMode == "DARK",
                    onClick = { onSelectThemeMode("DARK") },
                    modifier = Modifier.weight(1f)
                )
                ThemeCardOption(
                    title = "AMOLED",
                    icon = Icons.Rounded.Contrast,
                    isSelected = themeMode == "AMOLED",
                    onClick = { onSelectThemeMode("AMOLED") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Segmented Toggle Cards (M3 Expressive)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Segment 1 (Top) - Colores Dinámicos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Colores Dinámicos",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
                                    "Material You basado en tu fondo de pantalla (Android 12+)"
                                else
                                    "No disponible en esta versión de Android (requiere Android 12+)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    ExpressiveSwitch(
                        checked = useDynamicColors && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
                        enabled = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S,
                        onCheckedChange = {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                onToggleDynamicColors(it)
                            }
                        }
                    )
                }
            }

            // Segment 2 (Bottom) - Fuente personalizada
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Fuente personalizada",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Estilo técnico para números y registros",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    ExpressiveSwitch(
                        checked = useMonospace,
                        onCheckedChange = onToggleMonospace
                    )
                }
            }
        }
    }
}
}
}

@Composable
private fun ThemeCardOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 10.dp else 18.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "theme_corner_anim"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "theme_scale_anim"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        animationSpec = tween(200),
        label = "theme_bg_anim"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "theme_content_anim"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        animationSpec = tween(200),
        label = "theme_border_anim"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 13.sp,
                        color = contentColor
                    )
                )
            }
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 8.5.sp
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// PAGE 4: FINISH
// ----------------------------------------------------
@Composable
private fun FinishPage(
    hasAllPermissions: Boolean,
    onFinishSlotPositioned: (Offset, IntSize) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition(label = "checkAnim")
    val checkScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "checkScale"
    )

    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Success Circle Badge de tamaño armónico
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(46.dp)
                                .scale(checkScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "¡Todo Listo!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "MegasCU está preparado para ofrecerte la mejor experiencia en la gestión de tus datos móviles.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tarjeta Segmentada: Estado Final
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Segmento Superior: Estado de Permisos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (hasAllPermissions) Icons.Rounded.CheckCircle else Icons.Rounded.Info,
                                contentDescription = null,
                                tint = if (hasAllPermissions) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (hasAllPermissions) "Permisos totalmente concedidos" else "Puedes conceder permisos más tarde en Ajustes",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Segmento Inferior: Privacidad
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Procesamiento 100% privado en el dispositivo",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Margen superior aumentado para el botón
                Spacer(modifier = Modifier.height(28.dp))

                // Slot de anclaje para el botón interactivo transformable ajustado al texto con extremos completamente redondos
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(56.dp)
                        .onGloballyPositioned { coords ->
                            if (coords.isAttached) {
                                onFinishSlotPositioned(coords.positionInRoot(), coords.size)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Comenzar Experiencia\nMegasCU",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier
                            .padding(horizontal = 34.dp)
                            .graphicsLayer { alpha = 0f }
                    )
                }

                // Margen inferior aumentado para el botón
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ----------------------------------------------------
// PAGE 2: MODO SIMPLE VS NORMAL
// ----------------------------------------------------
@Composable
private fun SimpleModePage(
    isSimpleMode: Boolean,
    onToggleSimpleMode: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
        Text(
            text = "Modo de Visualización",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Selecciona la experiencia visual que prefieras. Puedes alternar en cualquier momento desde los ajustes.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Opciones lado a lado en Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Opción 1: Modo Normal
            Box(modifier = Modifier.weight(1f)) {
                ModeSelectionCard(
                    title = "Modo Normal",
                    subtitle = "Desglose completo de bolsas, gráficas y accesos directos.",
                    badge = "Normal",
                    isSelected = !isSimpleMode,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSimpleMode(false)
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.modo_normal),
                        contentDescription = "Vista previa Modo Normal",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Opción 2: Modo Simple
            Box(modifier = Modifier.weight(1f)) {
                ModeSelectionCard(
                    title = "Modo Simple",
                    subtitle = "Vista ultra limpia con cifras gigantes y cero distracciones.",
                    badge = "Simple",
                    isSelected = isSimpleMode,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSimpleMode(true)
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.modo_simple),
                        contentDescription = "Vista previa Modo Simple",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
}
}

@Composable
private fun ModeSelectionCard(
    title: String,
    subtitle: String,
    badge: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 14.dp else 22.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "mode_card_corner_anim"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "mode_card_scale_anim"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        animationSpec = tween(200),
        label = "mode_container_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        },
        animationSpec = tween(200),
        label = "mode_border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .expressivePressEffect(),
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                ),
                minLines = 2,
                maxLines = 2
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.Center
            ) {
                previewContent()
            }
        }
    }
}

@Composable
private fun NormalModePreviewGraphic() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mini Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = "MegasCU",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                }
            }

            // Mini Balance Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SALDO PRINCIPAL",
                            fontSize = 6.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "$ 250.00 CUP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "28 días",
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Mini Packages Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text("LTE", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("4.5 GB", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text("TODAS REDES", fontSize = 6.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("3.7 GB", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            // Mini Min/SMS Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Minutos", fontSize = 6.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("35 min", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SMS", fontSize = 6.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("40 SMS", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Mini Bottom Bar Simulation
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape))
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.outlineVariant, CircleShape))
                }
            }
        }
    }
}

@Composable
private fun SimpleModePreviewGraphic() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Header simple
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MegasCU",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }

            // Hero Mega Numbers
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "DATOS TOTALES",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "8.2 GB",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Clean Progress Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(6.dp),
                    shape = RoundedCornerShape(3.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Minimal Footer Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Saldo", fontSize = 6.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$ 250 CUP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "28 días",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PAGE 4: ALERTA Y SINCRONIZACIÓN
// ----------------------------------------------------
@Composable
private fun AlertsAndSyncPage(
    dataThresholdAlertEnabled: Boolean,
    dataThresholdPercent: Int,
    expirationAlertDays: Int,
    secondExpirationAlertEnabled: Boolean,
    secondExpirationAlertDays: Int,
    dailyLimitAlertsEnabled: Boolean,
    syncIntervalHours: Int,
    onToggleDataThresholdAlerts: (Boolean) -> Unit,
    onSetDataThresholdPercent: (Int) -> Unit,
    onSetExpirationAlertDays: (Int) -> Unit,
    onToggleSecondExpirationAlert: (Boolean) -> Unit,
    onSetSecondExpirationAlertDays: (Int) -> Unit,
    onToggleDailyLimitAlerts: (Boolean) -> Unit,
    onSetSyncIntervalHours: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Alerta y Sincronización",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Configura avisos oportunos antes de que venzan tus datos y la frecuencia de actualización.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tarjeta Segmentada: Alertas de Vencimiento
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Segmento Superior: Alerta Principal de Vencimiento
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Alerta de Vencimiento de Plan",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Notificar días antes del vencimiento del paquete",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Text("Avisar con anticipación:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))

                    val expDaysList = listOf(
                        SegmentOption(1, "1d"),
                        SegmentOption(2, "2d"),
                        SegmentOption(3, "3d"),
                        SegmentOption(5, "5d"),
                        SegmentOption(7, "7d")
                    )
                    ConnectedSegmentedGroup(
                        items = expDaysList,
                        selectedValue = expirationAlertDays,
                        onItemSelected = { onSetExpirationAlertDays(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Segmento Inferior: Segunda Alerta de Refuerzo
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Segunda Alerta de Refuerzo",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Aviso urgente de urgencia cercano a expirar",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        ExpressiveSwitch(
                            checked = secondExpirationAlertEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleSecondExpirationAlert(it)
                            }
                        )
                    }

                    if (secondExpirationAlertEnabled) {
                        val secDaysList = listOf(
                            SegmentOption(1, "1d antes"),
                            SegmentOption(2, "2d antes")
                        )
                        ConnectedSegmentedGroup(
                            items = secDaysList,
                            selectedValue = secondExpirationAlertDays,
                            onItemSelected = { onSetSecondExpirationAlertDays(it) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 2: Alerta de Umbral de Plan (Slider)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Alerta de Consumo de Plan",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Aviso al consumir el % especificado de tus datos",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    ExpressiveSwitch(
                        checked = dataThresholdAlertEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleDataThresholdAlerts(it)
                        }
                    )
                }

                if (dataThresholdAlertEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Notificar al consumir:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Text("$dataThresholdPercent%", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                    }
                    Slider(
                        value = dataThresholdPercent.toFloat(),
                        modifier = Modifier.expressivePressEffect(),
                        onValueChange = {
                            val newVal = it.toInt()
                            if (newVal != dataThresholdPercent) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSetDataThresholdPercent(newVal)
                            }
                        },
                        valueRange = 50f..95f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card 3: Sincronización en Segundo Plano
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Frecuencia de Sincronización",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Intervalo de verificación automática en segundo plano",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                val syncOptions = listOf(
                    SegmentOption(1, "1h"),
                    SegmentOption(3, "3h"),
                    SegmentOption(6, "6h"),
                    SegmentOption(12, "12h"),
                    SegmentOption(24, "24h")
                )
                ConnectedSegmentedGroup(
                    items = syncOptions,
                    selectedValue = syncIntervalHours,
                    onItemSelected = { onSetSyncIntervalHours(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
}
}

// ----------------------------------------------------
// PAGE 5: GRÁFICOS E INDICADORES / CONECTIVIDAD Y SEGURIDAD
// ----------------------------------------------------
@Composable
private fun ChartsAndSecurityPage(
    dualSimEnabled: Boolean,
    securityEnabled: Boolean,
    securityPin: String,
    dailyLimitAlertsEnabled: Boolean,
    onToggleDualSim: (Boolean) -> Unit,
    onToggleSecurity: (Boolean) -> Unit,
    onSetSecurityPin: (String) -> Unit,
    onToggleDailyLimitAlerts: (Boolean) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE) }
    val haptic = LocalHapticFeedback.current
    var isLineChart by remember { mutableStateOf(prefs.getBoolean("pref_use_line_chart", true)) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    ProgressiveFadedBox(
        scrollState = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            Text(
                text = "Graficas y Seguridad",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                textAlign = TextAlign.Center
            )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Ajusta las métricas de consumo y las opciones de protección y conectividad.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Gráficos e Indicadores",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Tarjeta Segmentada: Gráficos e Indicadores
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Segmento Superior: Gráfica Lineal con Puntos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Gráfica Lineal con Puntos",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Líneas suaves con puntos e indicadores numéricos",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    ExpressiveSwitch(
                        checked = isLineChart,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isLineChart = it
                            prefs.edit().putBoolean("pref_use_line_chart", it).apply()
                        }
                    )
                }
            }

            // Segmento Inferior: Barra de Límite Diario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Barra de Límite Diario",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Mostrar barra de progreso diario sugerido en pantalla principal",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    ExpressiveSwitch(
                        checked = dailyLimitAlertsEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            prefs.edit().putBoolean("pref_show_daily_recommendation_bar", it).apply()
                            onToggleDailyLimitAlerts(it)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Seguridad y Control",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val pinCardInteraction = remember { MutableInteractionSource() }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .expressivePressEffect(interactionSource = pinCardInteraction)
                .clickable(
                    interactionSource = pinCardInteraction,
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showPinSetupDialog = true
                },
            shape = rememberExpressiveMorphShape(
                defaultRadius = 18.dp,
                pressedRadius = 10.dp,
                interactionSource = pinCardInteraction
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Seguridad y Control",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (securityEnabled && securityPin.isNotEmpty())
                                "PIN configurado. Toca para cambiar o gestionar"
                            else
                                "Toca para establecer PIN nuevo y proteger la app",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (securityEnabled && securityPin.isNotEmpty())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (securityEnabled && securityPin.isNotEmpty()) "Activo" else "Configurar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        var showPinSetupDialog by remember { mutableStateOf(false) }
        var pinSuccessMessage by remember { mutableStateOf(false) }

        if (showPinSetupDialog) {
            PinSetupDialog(
                onPinSet = { newPin ->
                    onSetSecurityPin(newPin)
                    onToggleSecurity(true)
                    showPinSetupDialog = false
                    pinSuccessMessage = true
                },
                onDismiss = {
                    showPinSetupDialog = false
                }
            )
        }

        AnimatedVisibility(
            visible = pinSuccessMessage || (securityEnabled && securityPin.isNotEmpty()),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "PIN de seguridad configurado y aplicado correctamente.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}
}
}
