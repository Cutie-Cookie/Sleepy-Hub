package top.niunaijun.blackboxa.view.main

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import top.niunaijun.blackboxa.app.ProfileAppearance

@Composable
fun SleepyHubTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (ProfileAppearance.globalThemeMode()) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }
    val dynamic = ProfileAppearance.useDynamicColor() &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val accent = Color(ProfileAppearance.globalAccent())
    val baseScheme = when {
        dynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamic -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme(
            primary = accent,
            secondary = accent,
            tertiary = accent,
            background = Color(0xFF111113),
            surface = Color(0xFF1D1D20),
            surfaceVariant = Color(0xFF303034),
            onSurface = Color(0xFFF2F2F4),
            onSurfaceVariant = Color(0xFFCACAD0),
            outline = Color(0xFF77777D),
        )
        else -> lightColorScheme(
            primary = accent,
            secondary = accent,
            tertiary = accent,
            background = Color(0xFFF7F7F8),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE9E9EC),
            onSurface = Color(0xFF202024),
            onSurfaceVariant = Color(0xFF5E5E65),
            outline = Color(0xFF85858B),
        )
    }

    val amoled = darkTheme && ProfileAppearance.useAmoledDarkMode()
    val colorScheme = if (amoled) {
        baseScheme.copy(background = Color.Black, surface = Color.Black)
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
            activity.window.statusBarColor = colorScheme.surface.toArgb()
            activity.window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        content = content,
    )
}
