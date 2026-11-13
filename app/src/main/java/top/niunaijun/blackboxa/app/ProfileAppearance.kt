package top.niunaijun.blackboxa.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Persists Sleepy Hub's container identity, profile membership, and global appearance.
 *
 * A container owns its name, picture, and optional workspace background. BlackBox users
 * remain the durable profile identity; moving one only changes its container membership.
 */
data class HubContainer(
    val id: String,
    val name: String,
    val avatarUri: String? = null,
    val backgroundUri: String? = null,
    val backgroundOpacity: Float = 0.32f,
)

object ProfileAppearance {
    var themeRevision by mutableIntStateOf(0)
        private set

    fun notifyThemeChanged() {
        themeRevision++
    }

    private const val PREFS_NAME = "SleepyHubAppearance"
    private const val CONTAINERS_KEY = "containers_json"
    private const val ACTIVE_CONTAINER_KEY = "active_container_id"

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

    fun userNickname(): String =
        preferences.getString("user_nickname", "Sleepy Hub")?.takeIf { it.isNotBlank() } ?: "Sleepy Hub"

    fun setUserNickname(name: String) {
        preferences.edit { putString("user_nickname", name.trim()) }
    }

    fun containers(): List<HubContainer> {
        val raw = preferences.getString(CONTAINERS_KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                    add(
                        HubContainer(
                            id = id,
                            name = item.optString("name").takeIf { it.isNotBlank() } ?: "Container",
                            avatarUri = item.optString("avatar").takeIf { it.isNotBlank() },
                            backgroundUri = item.optString("background").takeIf { it.isNotBlank() },
                            backgroundOpacity = item.optDouble("opacity", 0.32).toFloat().coerceIn(0f, 1f),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveContainers(items: List<HubContainer>) {
        val array = JSONArray()
        items.forEach { container ->
            array.put(
                JSONObject().apply {
                    put("id", container.id)
                    put("name", container.name)
                    put("avatar", container.avatarUri ?: "")
                    put("background", container.backgroundUri ?: "")
                    put("opacity", container.backgroundOpacity.coerceIn(0f, 1f).toDouble())
                }
            )
        }
        preferences.edit { putString(CONTAINERS_KEY, array.toString()) }
    }

    /**
     * One-time migration: existing virtual profiles are assigned to a single default
     * container. No BlackBox users or installed app data are recreated or modified.
     */
    fun ensureContainers(existingUserIds: List<Int>): List<HubContainer> {
        var items = containers()
        if (items.isEmpty()) {
            items = listOf(HubContainer(id = "default", name = "Default"))
            saveContainers(items)
        }
        val validIds = items.map { it.id }.toSet()
        val fallbackId = items.first().id
        preferences.edit {
            existingUserIds.forEach { userId ->
                val key = profileContainerKey(userId)
                val assigned = preferences.getString(key, null)
                if (assigned == null || assigned !in validIds) putString(key, fallbackId)
            }
        }

        // Migrate the previous prototype's profile-owned wallpaper into the default
        // container once. The profile-specific accent/background keys are then retired.
        if (!preferences.getBoolean("legacy_profile_appearance_migrated", false)) {
            val legacyBackgroundOwner = existingUserIds.firstOrNull {
                !preferences.getString("profile_background_$it", null).isNullOrBlank()
            }
            if (legacyBackgroundOwner != null && items.none { !it.backgroundUri.isNullOrBlank() }) {
                val legacyUri = preferences.getString("profile_background_$legacyBackgroundOwner", null)
                val legacyOpacity = preferences.getFloat("profile_background_opacity_$legacyBackgroundOwner", 0.32f)
                items = items.toMutableList().also { list ->
                    list[0] = list[0].copy(
                        backgroundUri = legacyUri,
                        backgroundOpacity = legacyOpacity.coerceIn(0f, 1f),
                    )
                }
                saveContainers(items)
            }
            preferences.edit {
                existingUserIds.forEach { userId ->
                    remove("profile_accent_$userId")
                    remove("profile_background_$userId")
                    remove("profile_background_opacity_$userId")
                }
                putBoolean("legacy_profile_appearance_migrated", true)
            }
        }

        val active = preferences.getString(ACTIVE_CONTAINER_KEY, null)
        if (active == null || active !in validIds) saveActiveContainerId(fallbackId)
        return items
    }

    fun createContainer(name: String = "New container"): HubContainer {
        val item = HubContainer(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { "New container" },
        )
        saveContainers(containers() + item)
        return item
    }

    fun updateContainer(container: HubContainer) {
        saveContainers(containers().map { if (it.id == container.id) container else it })
    }

    fun removeContainer(containerId: String) {
        saveContainers(containers().filterNot { it.id == containerId })
        preferences.edit {
            preferences.all.keys
                .filter { it.startsWith("profile_container_") }
                .forEach { key ->
                    if (preferences.getString(key, null) == containerId) remove(key)
                }
        }
        if (activeContainerId() == containerId) {
            containers().firstOrNull()?.let { saveActiveContainerId(it.id) }
        }
    }

    fun profileContainer(userId: Int): String? =
        preferences.getString(profileContainerKey(userId), null)

    fun setProfileContainer(userId: Int, containerId: String) {
        preferences.edit { putString(profileContainerKey(userId), containerId) }
    }

    fun activeContainerId(): String =
        preferences.getString(ACTIVE_CONTAINER_KEY, null)
            ?: containers().firstOrNull()?.id
            ?: "default"

    fun saveActiveContainerId(containerId: String) {
        preferences.edit { putString(ACTIVE_CONTAINER_KEY, containerId) }
    }

    fun profileName(userId: Int, index: Int = userId): String {
        return AppManager.mRemarkSharedPreferences.getString("Remark$userId", null)
            ?.takeIf { it.isNotBlank() }
            ?: "Profile ${index + 1}"
    }

    fun setProfileName(userId: Int, name: String) {
        AppManager.mRemarkSharedPreferences.edit { putString("Remark$userId", name.trim()) }
    }

    fun profileFolder(userId: Int): String =
        preferences.getString("profile_folder_$userId", "General")
            ?.trim()?.takeIf { it.isNotBlank() } ?: "General"

    fun setProfileFolder(userId: Int, folder: String) {
        preferences.edit { putString("profile_folder_$userId", folder.trim().ifBlank { "General" }) }
    }

    fun isProfilePinned(userId: Int): Boolean =
        preferences.getBoolean("profile_pinned_$userId", false)

    fun setProfilePinned(userId: Int, pinned: Boolean) {
        preferences.edit { putBoolean("profile_pinned_$userId", pinned) }
    }

    fun globalThemeMode(): String =
        preferences.getString("global_theme_mode", "system") ?: "system"

    fun setGlobalThemeMode(mode: String) {
        preferences.edit { putString("global_theme_mode", mode) }
    }

    fun globalAccent(): Int =
        preferences.getInt("global_accent", 0xFFBDBDBD.toInt())

    fun setGlobalAccent(color: Int) {
        preferences.edit { putInt("global_accent", color) }
    }

    fun useDynamicColor(): Boolean = preferences.getBoolean("dynamic_color", false)

    fun setUseDynamicColor(enabled: Boolean) {
        preferences.edit { putBoolean("dynamic_color", enabled) }
    }

    fun useAmoledDarkMode(): Boolean = preferences.getBoolean("amoled_dark_mode", false)

    fun setUseAmoledDarkMode(enabled: Boolean) {
        preferences.edit { putBoolean("amoled_dark_mode", enabled) }
    }

    fun appGridColumns(): Int = preferences.getInt("app_grid_columns", 4).coerceIn(3, 5)

    fun setAppGridColumns(columns: Int) {
        preferences.edit { putInt("app_grid_columns", columns.coerceIn(3, 5)) }
    }

    /** Remove profile-owned preferences. Container artwork is intentionally separate. */
    fun clearProfile(userId: Int) {
        preferences.edit {
            remove(profileContainerKey(userId))
            remove("profile_folder_$userId")
            remove("profile_pinned_$userId")
            // Remove legacy per-profile appearance values from earlier prototypes.
            remove("profile_accent_$userId")
            remove("profile_background_$userId")
            remove("profile_background_opacity_$userId")
        }
        AppManager.mRemarkSharedPreferences.edit {
            remove("Remark$userId")
            remove("AppList$userId")
        }
    }

    private fun profileContainerKey(userId: Int) = "profile_container_$userId"
}
