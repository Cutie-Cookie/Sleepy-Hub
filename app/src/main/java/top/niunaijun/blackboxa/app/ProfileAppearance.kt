package top.niunaijun.blackboxa.app

import android.content.Context
import androidx.core.content.edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Sleepy Hub appearance preferences.
 *
 * Profile data is keyed by the BlackBox virtual user ID so each workspace keeps
 * its own appearance when the user switches profiles or restarts the app.
 */
object ProfileAppearance {
    var themeRevision by mutableIntStateOf(0)
        private set

    fun notifyThemeChanged() {
        themeRevision++
    }

    private const val PREFS_NAME = "SleepyHubAppearance"
    private val preferences by lazy {
        App.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val accentPalette: List<Int> = listOf(
        0xFFBDBDBD.toInt(),
        0xFF9E9E9E.toInt(),
        0xFFB39DDB.toInt(),
        0xFF80CBC4.toInt(),
        0xFF90CAF9.toInt(),
        0xFFF48FB1.toInt(),
        0xFFFFCC80.toInt(),
        0xFFA5D6A7.toInt(),
    )

    fun userNickname(): String {
        return preferences.getString("user_nickname", "Sleepy Hub")?.takeIf { it.isNotBlank() }
            ?: "Sleepy Hub"
    }

    fun setUserNickname(name: String) {
        preferences.edit { putString("user_nickname", name.trim()) }
    }

    fun profileName(userId: Int, index: Int = userId): String {
        return AppManager.mRemarkSharedPreferences.getString("Remark$userId", null)
            ?.takeIf { it.isNotBlank() }
            ?: "Profile ${index + 1}"
    }

    fun setProfileName(userId: Int, name: String) {
        AppManager.mRemarkSharedPreferences.edit {
            putString("Remark$userId", name.trim())
        }
    }

    fun profileFolder(userId: Int): String {
        return preferences.getString("profile_folder_$userId", "General")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "General"
    }

    fun setProfileFolder(userId: Int, folder: String) {
        preferences.edit {
            putString("profile_folder_$userId", folder.trim().ifBlank { "General" })
        }
    }

    fun profileAccent(userId: Int): Int {
        return preferences.getInt("profile_accent_$userId", accentPalette.first())
    }

    fun setProfileAccent(userId: Int, color: Int) {
        preferences.edit { putInt("profile_accent_$userId", color) }
    }

    fun backgroundUri(userId: Int): String? {
        return preferences.getString("profile_background_$userId", null)
    }

    fun setBackgroundUri(userId: Int, uri: String?) {
        preferences.edit {
            if (uri.isNullOrBlank()) remove("profile_background_$userId")
            else putString("profile_background_$userId", uri)
        }
    }

    fun backgroundOpacity(userId: Int): Float {
        return preferences.getFloat("profile_background_opacity_$userId", 0.32f)
            .coerceIn(0f, 1f)
    }

    fun setBackgroundOpacity(userId: Int, opacity: Float) {
        preferences.edit {
            putFloat("profile_background_opacity_$userId", opacity.coerceIn(0f, 1f))
        }
    }

    fun lastProfileId(): Int {
        return preferences.getInt("last_profile_id", 0)
    }

    fun saveLastProfileId(userId: Int) {
        preferences.edit { putInt("last_profile_id", userId) }
    }

    fun globalThemeMode(): String {
        return preferences.getString("global_theme_mode", "system") ?: "system"
    }

    fun setGlobalThemeMode(mode: String) {
        preferences.edit { putString("global_theme_mode", mode) }
    }

    fun globalAccent(): Int {
        return preferences.getInt("global_accent", 0xFFBDBDBD.toInt())
    }

    fun setGlobalAccent(color: Int) {
        preferences.edit { putInt("global_accent", color) }
    }

    fun useDynamicColor(): Boolean {
        return preferences.getBoolean("dynamic_color", false)
    }

    fun setUseDynamicColor(enabled: Boolean) {
        preferences.edit { putBoolean("dynamic_color", enabled) }
    }

    fun useAmoledDarkMode(): Boolean {
        return preferences.getBoolean("amoled_dark_mode", false)
    }

    fun setUseAmoledDarkMode(enabled: Boolean) {
        preferences.edit { putBoolean("amoled_dark_mode", enabled) }
    }

    fun appGridColumns(): Int {
        return preferences.getInt("app_grid_columns", 4).coerceIn(3, 5)
    }

    fun setAppGridColumns(columns: Int) {
        preferences.edit { putInt("app_grid_columns", columns.coerceIn(3, 5)) }
    }

    fun clearProfile(userId: Int) {
        preferences.edit {
            remove("profile_accent_$userId")
            remove("profile_folder_$userId")
            remove("profile_background_$userId")
            remove("profile_background_opacity_$userId")
        }
        AppManager.mRemarkSharedPreferences.edit {
            remove("Remark$userId")
            remove("AppList$userId")
        }
    }
}
