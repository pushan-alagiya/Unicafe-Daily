package fi.pushan.unicafedaily.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFAFAFA),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF242428),
    onPrimaryContainer = Color(0xFFF4F4F6),
    secondary = BrandCoral,
    onSecondary = Color.White,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardBorder,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = DarkCanvas,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF2A2A2E),
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF333338)
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueBg,
    onPrimaryContainer = BrandBlue,
    secondary = BrandCoral,
    onSecondary = Color.White,
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightCard,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardBorder,
    onSurfaceVariant = LightTextSecondary
)

@Composable
fun UniCafeDailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
