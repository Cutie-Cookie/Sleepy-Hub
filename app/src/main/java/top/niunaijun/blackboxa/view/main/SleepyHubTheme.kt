package top.niunaijun.blackboxa.view.main

import android.app.Activity
import android.graphics.Color as AndroidColor
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import top.niunaijun.blackboxa.app.ProfileAppearance

@Composable
fun SleepyHubTheme(workspace: Boolean = false, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val themeRevision = ProfileAppearance.themeRevision
    val themeMode = remember(themeRevision) { ProfileAppearance.globalThemeMode() }
    val dynamicEnabled = remember(themeRevision) { ProfileAppearance.useDynamicColor() }
    val amoledEnabled = remember(themeRevision) { ProfileAppearance.useAmoledDarkMode() }
    val accentValue = remember(themeRevision) { ProfileAppearance.globalAccent() }
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }
    val dynamic = dynamicEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val accent = Color(accentValue)
    val onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White

    val baseScheme = when {
        dynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = Color(0xFF343438),
            onPrimaryContainer = Color(0xFFF2F2F4),
            inversePrimary = Color(0xFFBDBDC2),
            secondary = Color(0xFFBDBDC2),
            onSecondary = Color(0xFF17171A),
            secondaryContainer = Color(0xFF303034),
            onSecondaryContainer = Color(0xFFE8E8EB),
            tertiary = Color(0xFFD0D0D4),
            onTertiary = Color(0xFF17171A),
            tertiaryContainer = Color(0xFF3A3A3E),
            onTertiaryContainer = Color(0xFFECECEF),
            background = Color(0xFF111113),
            onBackground = Color(0xFFF2F2F4),
            surface = Color(0xFF1D1D20),
            onSurface = Color(0xFFF2F2F4),
            surfaceVariant = Color(0xFF303034),
            onSurfaceVariant = Color(0xFFCACAD0),
            surfaceTint = accent,
            inverseSurface = Color(0xFFECECEF),
            inverseOnSurface = Color(0xFF202024),
            error = Color(0xFFBDBDC2),
            onError = Color(0xFF17171A),
            errorContainer = Color(0xFF3B3B3F),
            onErrorContainer = Color(0xFFF2F2F4),
            outline = Color(0xFF77777D),
            outlineVariant = Color(0xFF48484D),
            scrim = Color.Black,
            surfaceBright = Color(0xFF39393D),
            surfaceDim = Color(0xFF111113),
            surfaceContainerLowest = Color(0xFF0C0C0E),
            surfaceContainerLow = Color(0xFF19191C),
            surfaceContainer = Color(0xFF202024),
            surfaceContainerHigh = Color(0xFF2A2A2E),
            surfaceContainerHighest = Color(0xFF343438),
        )
        else -> lightColorScheme(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = Color(0xFFE1E1E4),
            onPrimaryContainer = Color(0xFF17171A),
            inversePrimary = Color(0xFF5F5F65),
            secondary = Color(0xFF55555B),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFE5E5E8),
            onSecondaryContainer = Color(0xFF202024),
            tertiary = Color(0xFF606066),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFE8E8EB),
            onTertiaryContainer = Color(0xFF202024),
            background = Color(0xFFF7F7F8),
            onBackground = Color(0xFF202024),
            surface = Color.White,
            onSurface = Color(0xFF202024),
            surfaceVariant = Color(0xFFE9E9EC),
            onSurfaceVariant = Color(0xFF5E5E65),
            surfaceTint = accent,
            inverseSurface = Color(0xFF303034),
            inverseOnSurface = Color(0xFFF2F2F4),
            error = Color(0xFF5B5B60),
            onError = Color.White,
            errorContainer = Color(0xFFE5E5E8),
            onErrorContainer = Color(0xFF202024),
            outline = Color(0xFF85858B),
            outlineVariant = Color(0xFFC8C8CD),
            scrim = Color.Black,
            surfaceBright = Color.White,
            surfaceDim = Color(0xFFD6D6DA),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF4F4F6),
            surfaceContainer = Color(0xFFEFEFF2),
            surfaceContainerHigh = Color(0xFFE8E8EB),
            surfaceContainerHighest = Color(0xFFE0E0E4),
        )
    }

    val amoled = darkTheme && amoledEnabled
    val colorScheme = if (amoled) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color(0xFF080808),
            surfaceContainer = Color(0xFF101010),
        )
    } else {
        baseScheme
    }

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

    SideEffect {
        val activity = context as? Activity
        if (activity != null && !view.isInEditMode) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            activity.window.statusBarColor = if (workspace) AndroidColor.TRANSPARENT else colorScheme.surface.toArgb()
            activity.window.navigationBarColor = AndroidColor.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.window.navigationBarDividerColor = AndroidColor.TRANSPARENT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activity.window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme && !workspace
            controller.isAppearanceLightNavigationBars = !darkTheme && !workspace
        }
    }

    MaterialTheme(colorScheme = colorScheme, shapes = shapes, content = content)
}
