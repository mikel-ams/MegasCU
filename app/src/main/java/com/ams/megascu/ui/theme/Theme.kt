package com.ams.megascu.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkExpressiveColorScheme = darkColorScheme(
    primary = VioletTone080,               // #BFA3FF - Color Primario más claro para mejor legibilidad de textos sobre fondos oscuros
    onPrimary = VioletTone010,             // #160040 - Texto oscuro sobre botón primario
    primaryContainer = VioletTone020,      // #23006B - Contenedor primario en modo oscuro
    onPrimaryContainer = VioletTone090,    // #DFD1FF - Texto claro sobre contenedor primario
    secondary = VioletTone070,             // #A175FF - Tonos de transición, indicadores de progreso o iconos secundarios activos
    onSecondary = VioletTone010,
    secondaryContainer = VioletTone020,    // #23006B - Contenedores oscuros secundarios y tarjetas secundarias flotantes
    onSecondaryContainer = VioletTone090,  // #DFD1FF - Texto claro sobre contenedores secundarios
    tertiary = VioletTone090,              // #DFD1FF - Elementos de énfasis destacados (textos)
    onTertiary = VioletTone010,
    tertiaryContainer = VioletTone030,     // #3600A6 - Bordes activos o estados seleccionados
    onTertiaryContainer = VioletTone090,
    background = VioletTone010,            // #160040 - Fondo original de las ventanas en modo oscuro
    onBackground = VioletTone100,          // #FFFFFF - Blanco puro para textos principales sobre fondos oscuros
    surface = VioletTone010,               // #160040 - Superficies muy oscuras, barras de navegación o contenedores principales
    onSurface = VioletTone100,             // #FFFFFF - Blanco puro
    surfaceVariant = VioletTone020,        // #23006B - Tarjetas y contenedores elevados en modo oscuro
    onSurfaceVariant = VioletTone090,      // #DFD1FF - Texto claro
    outline = VioletTone060,               // #8347FF - Bordes activos visibles en modo oscuro
    outlineVariant = VioletTone030,        // #3600A6 - Elementos de énfasis bajo
    error = AlertRed,
    onError = Color(0xFF450A0A)
)

private val LightExpressiveColorScheme = lightColorScheme(
    primary = VioletTone040,               // #4A00DF - Color Primario Principal (Botones principales, Top App Bar, interactivos)
    onPrimary = VioletTone100,             // #FFFFFF - Blanco puro
    primaryContainer = VioletTone080,      // #BFA3FF - Color para Contenedores Primarios y tarjeta especial de gran énfasis en modo claro
    onPrimaryContainer = VioletTone010,    // #160040 - Texto oscuro sobre contenedor primario
    secondary = VioletTone050,             // #6515FF - Acentos secundarios
    onSecondary = VioletTone100,
    secondaryContainer = VioletTone090,    // #DFD1FF - Superficies contenedoras secundarias claras y fondos modales
    onSecondaryContainer = VioletTone010,  // #160040
    tertiary = VioletTone070,              // #A175FF - Elementos de énfasis, enlaces de texto
    onTertiary = VioletTone100,
    tertiaryContainer = VioletTone095,     // #F0EBFF
    onTertiaryContainer = VioletTone020,   // #23006B
    background = VioletTone095,            // #F0EBFF - Fondos claros sutiles para pantallas generales
    onBackground = VioletTone010,          // #160040
    surface = VioletTone100,               // #FFFFFF - Blanco puro para superficies y tarjetas en modo claro absoluto
    onSurface = VioletTone010,             // #160040
    surfaceVariant = VioletTone090,        // #DFD1FF - Superficies y tarjetas secundarias en modo claro
    onSurfaceVariant = VioletTone010,      // #160040
    outline = VioletTone050,               // #6515FF - Bordes y divisores accesibles (Ratio > 6.7:1 sobre fondo claro)
    outlineVariant = VioletTone060,        // #8347FF - Elementos de énfasis y líneas secundarias accesibles (Ratio > 4.8:1 sobre blanco)
    error = Color(0xFFDC2626),
    onError = VioletTone100
)

private val AmoledDarkColorScheme = DarkExpressiveColorScheme.copy(
    background = VioletTone000,            // #000000 - Fondo absoluto para pantallas completas en modo oscuro extremo (OLED)
    surface = VioletTone000,               // #000000
    surfaceVariant = VioletTone010,        // #160040
    primaryContainer = VioletTone020,      // #23006B
    secondaryContainer = VioletTone020
)

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(18.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(44.dp)
)

@Composable
fun animateColorScheme(target: ColorScheme): ColorScheme {
    val spec = spring<Color>(stiffness = Spring.StiffnessLow)
    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "onPrimary")
    val primaryContainer by animateColorAsState(target.primaryContainer, spec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, spec, label = "onPrimaryContainer")
    val secondary by animateColorAsState(target.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(target.onSecondary, spec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, spec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(target.onSecondaryContainer, spec, label = "onSecondaryContainer")
    val tertiary by animateColorAsState(target.tertiary, spec, label = "tertiary")
    val onTertiary by animateColorAsState(target.onTertiary, spec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(target.tertiaryContainer, spec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(target.onTertiaryContainer, spec, label = "onTertiaryContainer")
    val background by animateColorAsState(target.background, spec, label = "background")
    val onBackground by animateColorAsState(target.onBackground, spec, label = "onBackground")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val onSurface by animateColorAsState(target.onSurface, spec, label = "onSurface")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, spec, label = "surfaceVariant")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, spec, label = "onSurfaceVariant")
    val surfaceTint by animateColorAsState(target.surfaceTint, spec, label = "surfaceTint")
    val inverseSurface by animateColorAsState(target.inverseSurface, spec, label = "inverseSurface")
    val inverseOnSurface by animateColorAsState(target.inverseOnSurface, spec, label = "inverseOnSurface")
    val inversePrimary by animateColorAsState(target.inversePrimary, spec, label = "inversePrimary")
    val outline by animateColorAsState(target.outline, spec, label = "outline")
    val outlineVariant by animateColorAsState(target.outlineVariant, spec, label = "outlineVariant")
    val scrim by animateColorAsState(target.scrim, spec, label = "scrim")
    val error by animateColorAsState(target.error, spec, label = "error")
    val onError by animateColorAsState(target.onError, spec, label = "onError")
    val errorContainer by animateColorAsState(target.errorContainer, spec, label = "errorContainer")
    val onErrorContainer by animateColorAsState(target.onErrorContainer, spec, label = "onErrorContainer")

    return target.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer
    )
}

@Composable
fun MegasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    useMonospace: Boolean = true,
    useDynamicColors: Boolean = false,
    paletteStyle: String = "Tonal Spot",
    content: @Composable () -> Unit
) {
    val dynamicColor = useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    var isWcagEnabled by remember {
        val prefs = context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE)
        mutableStateOf(prefs.getBoolean("pref_use_wcag_palette", false))
    }

    androidx.compose.runtime.DisposableEffect(context) {
        val prefs = context.getSharedPreferences("megas_prefs", android.content.Context.MODE_PRIVATE)
        isWcagEnabled = prefs.getBoolean("pref_use_wcag_palette", false)
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
            if (key == "pref_use_wcag_palette") {
                isWcagEnabled = p.getBoolean("pref_use_wcag_palette", false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val baseColorScheme = when {
        dynamicColor && darkTheme -> {
            try {
                dynamicDarkColorScheme(context)
            } catch (e: Exception) {
                if (isAmoled) AmoledDarkColorScheme else DarkExpressiveColorScheme
            }
        }
        dynamicColor && !darkTheme -> {
            try {
                dynamicLightColorScheme(context)
            } catch (e: Exception) {
                LightExpressiveColorScheme
            }
        }
        darkTheme && isAmoled -> AmoledDarkColorScheme
        darkTheme -> DarkExpressiveColorScheme
        else -> LightExpressiveColorScheme
    }

    val styledColorScheme = if (!isWcagEnabled && !dynamicColor) {
        when (paletteStyle) {
            "Vibrante" -> baseColorScheme.copy(
                primary = if (darkTheme) Color(0xFFA1C9FF) else Color(0xFF005AC1),
                tertiary = if (darkTheme) Color(0xFFFFB4AB) else Color(0xFFC00010)
            )
            "Expresivo" -> baseColorScheme.copy(
                primary = if (darkTheme) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                secondary = if (darkTheme) Color(0xFFCCC2DC) else Color(0xFF625B71),
                tertiary = if (darkTheme) Color(0xFFEFB8C8) else Color(0xFF7D5260)
            )
            "Ensalada de frutas" -> baseColorScheme.copy(
                primary = if (darkTheme) Color(0xFF86D2E1) else Color(0xFF006874),
                secondary = if (darkTheme) Color(0xFFFFB4A9) else Color(0xFF904A42),
                tertiary = if (darkTheme) Color(0xFFC4C9A7) else Color(0xFF5A6145)
            )
            else -> baseColorScheme
        }
    } else baseColorScheme

    val rawColorScheme = if (isWcagEnabled) {
        if (darkTheme) {
            styledColorScheme.copy(
                primary = Color(0xFFD0BCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378B),
                onPrimaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFFCCC2DC),
                onSecondary = Color(0xFF332D41),
                secondaryContainer = Color(0xFF4A4458),
                onSecondaryContainer = Color(0xFFE8DEF8),
                tertiary = Color(0xFFEFB8C8),
                onTertiary = Color(0xFF492532),
                tertiaryContainer = Color(0xFF633B48),
                onTertiaryContainer = Color(0xFFFFD8E4),
                surfaceVariant = Color(0xFF49454F),
                onSurfaceVariant = Color(0xFFE2D6FF),
                outline = Color(0xFFD0BCFF),
                outlineVariant = Color(0xFF8B77B7)
            )
        } else {
            styledColorScheme.copy(
                primary = Color(0xFF3800B0),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFEADDFF),
                onPrimaryContainer = Color(0xFF21005D),
                secondary = Color(0xFF7020E0),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFE8DEF8),
                onSecondaryContainer = Color(0xFF1D192B),
                tertiary = Color(0xFF8C0068),
                onTertiary = Color(0xFFFFFFFF),
                tertiaryContainer = Color(0xFFFFD8E4),
                onTertiaryContainer = Color(0xFF31111D),
                surfaceVariant = Color(0xFFE7E0EC),
                onSurfaceVariant = Color(0xFF2C006B),
                outline = Color(0xFF3800B0),
                outlineVariant = Color(0xFF7020E0)
            )
        }
    } else styledColorScheme

    val animatedColorScheme = animateColorScheme(rawColorScheme)
    val typography = if (useMonospace) TypographyMono else TypographyDefault

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = typography,
        shapes = ExpressiveShapes,
        content = content
    )
}

