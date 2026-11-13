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
 * Containers own their name and picture. Each BlackBox user keeps its own name, avatar,
 * workspace background, folder and ordering; moving one only changes its container membership.
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

        // Keep profile-owned wallpapers. Only retire the old per-profile accent because
        // appearance/theme is global in Sleepy Hub.
        if (!preferences.getBoolean("legacy_profile_appearance_migrated", false)) {
            preferences.edit {
                existingUserIds.forEach { userId -> remove("profile_accent_$userId") }
                putBoolean("legacy_profile_appearance_migrated", true)
            }
        }

        // Migrate the previous test build's container-owned wallpaper to every profile
        // in that container. A profile that already has its own image keeps it.
        // Container wallpaper is then cleared; workspace backgrounds are profile-owned.
        if (!preferences.getBoolean("container_background_to_profiles_v2_migrated", false)) {
            val migratedItems = items.map { container ->
                val legacyUri = container.backgroundUri?.takeIf { it.isNotBlank() }
                if (legacyUri != null) {
                    existingUserIds
                        .filter { profileContainer(it) == container.id }
                        .forEach { userId ->
                            if (profileBackgroundUri(userId).isNullOrBlank()) {
                                setProfileBackgroundUri(userId, legacyUri)
                                setProfileBackgroundOpacity(userId, container.backgroundOpacity)
                            }
                        }
                }
                container.copy(backgroundUri = null, backgroundOpacity = 0.32f)
            }
            if (migratedItems != items) {
                saveContainers(migratedItems)
                items = migratedItems
            }
            preferences.edit { putBoolean("container_background_to_profiles_v2_migrated", true) }
        }

        // Repair or initialize each container's explicit profile order.
        items.forEach { container ->
            val members = existingUserIds.filter { profileContainer(it) == container.id }
            val savedOrder = profileOrder(container.id)
            val repairedOrder = savedOrder.filter { it in members }.distinct() +
                members.filterNot { it in savedOrder }
            if (savedOrder != repairedOrder) setProfileOrder(container.id, repairedOrder)
            profileFolders(container.id, existingUserIds)
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
            remove("last_profile_id_$containerId")
            remove(profileOrderKey(containerId))
            remove(profileFoldersKey(containerId))
        }
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
        val previousContainer = profileContainer(userId)
        preferences.edit { putString(profileContainerKey(userId), containerId) }
        if (previousContainer != null && previousContainer != containerId) {
            setProfileOrder(previousContainer, profileOrder(previousContainer).filterNot { it == userId })
        }
        setProfileOrder(containerId, profileOrder(containerId).filterNot { it == userId } + userId)
    }

    fun activeContainerId(): String =
        preferences.getString(ACTIVE_CONTAINER_KEY, null)
            ?: containers().firstOrNull()?.id
            ?: "default"

    fun saveActiveContainerId(containerId: String) {
        preferences.edit { putString(ACTIVE_CONTAINER_KEY, containerId) }
    }

    /** Keep the last active profile independently for each container. */
    fun lastProfileId(containerId: String): Int =
        preferences.getInt("last_profile_id_$containerId", -1)

    /** The global key is retained as a migration fallback for existing installs. */
    fun lastProfileId(): Int =
        preferences.getInt("last_profile_id", -1)

    fun saveLastProfileId(containerId: String, userId: Int) {
        preferences.edit {
            putInt("last_profile_id_$containerId", userId)
            putInt("last_profile_id", userId)
        }
    }

    fun profileName(userId: Int, index: Int = userId): String {
        return AppManager.mRemarkSharedPreferences.getString("Remark$userId", null)
            ?.takeIf { it.isNotBlank() }
            ?: "New Profile"
    }

    fun setProfileName(userId: Int, name: String) {
        AppManager.mRemarkSharedPreferences.edit { putString("Remark$userId", name.trim()) }
    }

    fun profileAvatarUri(userId: Int): String? =
        preferences.getString("profile_avatar_$userId", null)?.takeIf { it.isNotBlank() }

    fun setProfileAvatarUri(userId: Int, uri: String?) {
        preferences.edit {
            if (uri.isNullOrBlank()) remove("profile_avatar_$userId")
            else putString("profile_avatar_$userId", uri)
        }
    }

    fun profileBackgroundUri(userId: Int): String? =
        preferences.getString("profile_background_$userId", null)?.takeIf { it.isNotBlank() }

    fun profileBackgroundOpacity(userId: Int): Float =
        preferences.getFloat("profile_background_opacity_$userId", 0.32f).coerceIn(0f, 1f)

    fun setProfileBackgroundUri(userId: Int, uri: String?) {
        preferences.edit {
            if (uri.isNullOrBlank()) remove("profile_background_$userId")
            else putString("profile_background_$userId", uri)
        }
    }

    fun setProfileBackgroundOpacity(userId: Int, opacity: Float) {
        preferences.edit {
            putFloat("profile_background_opacity_$userId", opacity.coerceIn(0f, 1f))
        }
    }

    fun profileOrder(containerId: String): List<Int> =
        preferences.getString(profileOrderKey(containerId), null)
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.distinct()
            ?: emptyList()

    fun setProfileOrder(containerId: String, userIds: List<Int>) {
        preferences.edit {
            putString(profileOrderKey(containerId), userIds.distinct().joinToString(","))
        }
    }

    fun profileFolder(userId: Int): String =
        preferences.getString("profile_folder_$userId", "General")
            ?.trim()?.takeIf { it.isNotBlank() } ?: "General"

    fun setProfileFolder(userId: Int, folder: String) {
        preferences.edit { putString("profile_folder_$userId", folder.trim().ifBlank { "General" }) }
    }

    fun profileFolders(containerId: String, existingUserIds: List<Int>): List<String> {
        val key = profileFoldersKey(containerId)
        val saved = runCatching {
            val array = JSONArray(preferences.getString(key, "[]") ?: "[]")
            (0 until array.length()).mapNotNull { index ->
                array.optString(index).trim().takeIf { it.isNotBlank() }
            }
        }.getOrDefault(emptyList())
        val assigned = existingUserIds
            .filter { profileContainer(it) == containerId }
            .map { profileFolder(it) }
        val merged = (listOf("General") + saved + assigned)
            .distinctBy { it.lowercase() }
            .let { folders -> listOf("General") + folders.filterNot { it.equals("General", ignoreCase = true) } }
        if (saved != merged) saveProfileFolders(containerId, merged)
        return merged
    }

    fun createProfileFolder(
        containerId: String,
        name: String,
        existingUserIds: List<Int>,
    ): Boolean {
        val cleaned = name.trim()
        if (cleaned.isBlank()) return false
        val current = profileFolders(containerId, existingUserIds)
        if (current.any { it.equals(cleaned, ignoreCase = true) }) return false
        saveProfileFolders(containerId, current + cleaned)
        return true
    }

    fun renameProfileFolder(
        containerId: String,
        oldName: String,
        newName: String,
        existingUserIds: List<Int>,
    ): Boolean {
        val cleaned = newName.trim()
        if (oldName.equals("General", ignoreCase = true) || cleaned.isBlank()) return false
        val current = profileFolders(containerId, existingUserIds)
        if (oldName !in current) return false
        if (oldName.equals(cleaned, ignoreCase = true)) return true
        if (current.any { it.equals(cleaned, ignoreCase = true) }) return false
        saveProfileFolders(containerId, current.map { if (it == oldName) cleaned else it })
        existingUserIds
            .filter { profileContainer(it) == containerId && profileFolder(it) == oldName }
            .forEach { setProfileFolder(it, cleaned) }
        return true
    }

    fun deleteProfileFolder(
        containerId: String,
        name: String,
        existingUserIds: List<Int>,
    ): Boolean {
        if (name.equals("General", ignoreCase = true)) return false
        val current = profileFolders(containerId, existingUserIds)
        if (name !in current) return false
        existingUserIds
            .filter { profileContainer(it) == containerId && profileFolder(it) == name }
            .forEach { setProfileFolder(it, "General") }
        saveProfileFolders(containerId, current.filterNot { it == name })
        return true
    }

    private fun saveProfileFolders(containerId: String, folders: List<String>) {
        val array = JSONArray()
        folders.forEach { array.put(it) }
        preferences.edit { putString(profileFoldersKey(containerId), array.toString()) }
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

    fun autoStopIdleApps(): Boolean = preferences.getBoolean("auto_stop_idle_apps", true)

    fun setAutoStopIdleApps(enabled: Boolean) {
        preferences.edit { putBoolean("auto_stop_idle_apps", enabled) }
    }

    fun appGridColumns(): Int = preferences.getInt("app_grid_columns", 4).coerceIn(3, 5)

    fun setAppGridColumns(columns: Int) {
        preferences.edit { putInt("app_grid_columns", columns.coerceIn(3, 5)) }
    }

    /** Remove profile-owned preferences. URI permission release is handled by MainActivity. */
    fun clearProfile(userId: Int) {
        containers().forEach { container ->
            val order = profileOrder(container.id)
            if (userId in order) setProfileOrder(container.id, order.filterNot { it == userId })
        }
        preferences.edit {
            remove(profileContainerKey(userId))
            remove("profile_folder_$userId")
            remove("profile_pinned_$userId")
            remove("profile_avatar_$userId")
            remove("profile_background_$userId")
            remove("profile_background_opacity_$userId")
            remove("profile_accent_$userId")
        }
        AppManager.mRemarkSharedPreferences.edit {
            remove("Remark$userId")
            remove("AppList$userId")
        }
    }

    private fun profileOrderKey(containerId: String) = "profile_order_$containerId"

    private fun profileFoldersKey(containerId: String) = "profile_folders_$containerId"

    private fun profileContainerKey(userId: Int) = "profile_container_$userId"
}
