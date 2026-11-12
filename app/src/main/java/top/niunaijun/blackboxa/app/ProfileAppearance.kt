package top.niunaijun.blackboxa.app

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.preference.PreferenceManager

/**
 * Small, persistent appearance settings for each BlackBox virtual profile.
 */
object ProfileAppearance {
    val ACCENT_NAMES = arrayOf("Lavender", "Rose", "Ocean", "Sage", "Amber", "Lilac")
    val ACCENT_COLORS = intArrayOf(
        Color.rgb(151, 132, 190),
        Color.rgb(194, 124, 145),
        Color.rgb(103, 148, 190),
        Color.rgb(112, 157, 139),
        Color.rgb(190, 145, 91),
        Color.rgb(174, 137, 194)
    )

    private fun prefs() = AppManager.mRemarkSharedPreferences

    fun profileName(userId: Int): String {
        return prefs().getString("Remark$userId", null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: ("Profile " + (userId + 1))
    }

    fun setProfileName(userId: Int, name: String) {
        prefs().edit().putString("Remark$userId", name.trim()).apply()
    }

    fun backgroundUri(userId: Int): String? = prefs().getString("Background$userId", null)

    fun setBackgroundUri(userId: Int, uri: String) {
        prefs().edit().putString("Background$userId", uri).apply()
    }

    fun removeBackground(userId: Int) {
        prefs().edit()
            .remove("Background$userId")
            .remove("BackgroundOpacity$userId")
            .apply()
    }

    fun backgroundOpacity(userId: Int): Float {
        return prefs().getInt("BackgroundOpacity$userId", 62).coerceIn(0, 100) / 100f
    }

    fun setBackgroundOpacity(userId: Int, opacity: Int) {
        prefs().edit().putInt("BackgroundOpacity$userId", opacity.coerceIn(0, 100)).apply()
    }

    fun accentIndex(userId: Int): Int {
        return prefs().getInt("Accent$userId", userId.mod(ACCENT_COLORS.size))
            .coerceIn(0, ACCENT_COLORS.lastIndex)
    }

    fun accent(userId: Int): Int = ACCENT_COLORS[accentIndex(userId)]

    fun setAccent(userId: Int, index: Int) {
        prefs().edit().putInt("Accent$userId", index.coerceIn(0, ACCENT_COLORS.lastIndex)).apply()
    }

    fun globalAccent(context: Context): Int {
        val value = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("global_accent", "#9B8BC0")
        return try {
            Color.parseColor(value ?: "#9B8BC0")
        } catch (_: IllegalArgumentException) {
            ACCENT_COLORS[0]
        }
    }

    fun installedAppCount(userId: Int): Int = prefs().getInt("AppCount$userId", 0).coerceAtLeast(0)

    fun setInstalledAppCount(userId: Int, count: Int) {
        prefs().edit().putInt("AppCount$userId", count.coerceAtLeast(0)).apply()
    }

    fun lastProfileId(): Int = prefs().getInt("LastProfileId", 0)

    fun saveLastProfileId(userId: Int) {
        prefs().edit().putInt("LastProfileId", userId).apply()
    }

    fun clearProfile(userId: Int) {
        prefs().edit()
            .remove("Remark$userId")
            .remove("AppList$userId")
            .remove("Background$userId")
            .remove("BackgroundOpacity$userId")
            .remove("Accent$userId")
            .remove("AppCount$userId")
            .apply()
    }

    fun glassBackground(context: Context, userId: Int, selected: Boolean = false): GradientDrawable {
        val accent = accent(userId)
        val first = withAlpha(accent, if (selected) 118 else 82)
        val second = withAlpha(darken(accent), if (selected) 108 else 74)
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(first, second)
        ).apply {
            cornerRadius = dp(context, 20f)
            setStroke(dp(context, if (selected) 2 else 1), Color.argb(72, 255, 255, 255))
        }
    }

    private fun darken(color: Int): Int {
        val r = (Color.red(color) * 0.58f).toInt()
        val g = (Color.green(color) * 0.58f).toInt()
        val b = (Color.blue(color) * 0.58f).toInt()
        return Color.rgb(r, g, b)
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun dp(context: Context, value: Float): Float {
        return value * context.resources.displayMetrics.density
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }
}
