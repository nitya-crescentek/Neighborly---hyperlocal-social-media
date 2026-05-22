package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkSlatePrimary,
    secondary = LightSageAccent,
    tertiary = Color(0xFF2E4978),
    background = DarkSlateBackground,
    surface = DarkSlateSurface,
    onPrimary = Color(0xFF002F66),
    onSecondary = DarkGreyText,
    onBackground = DarkGreyText,
    onSurface = DarkGreyText,
    outline = DarkSageDivider,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = LeafGreenPrimary,                  // #005AC1
    secondary = LightSageAccent,                // #44474E
    tertiary = Color(0xFFD3E4FF),               // #D3E4FF
    background = CreamBackground,               // #FDFBFF
    surface = SoftWhiteSurface,                 // #FFFFFF
    onPrimary = SoftWhiteSurface,
    onSecondary = TextDarkGreen,
    onBackground = TextDarkGreen,
    onSurface = TextDarkGreen,
    outline = DividerGreen,                      // #E1E2E9
    error = Color(0xFFBA1A1A),                  // #BA1A1A
    errorContainer = Color(0xFFFFDAD6),         // #FFDAD6
    onErrorContainer = Color(0xFF410002)         // #410002
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful custom neighborhood branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
