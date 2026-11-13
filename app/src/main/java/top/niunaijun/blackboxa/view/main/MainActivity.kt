package top.niunaijun.blackboxa.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.viewpager2.widget.ViewPager2
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.core.GmsCore
import top.niunaijun.blackboxa.app.HubContainer
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.databinding.ActivityMainBinding
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.view.apps.AppsFragment
import top.niunaijun.blackboxa.view.base.BaseActivity
import top.niunaijun.blackboxa.view.fake.FakeManagerActivity
import top.niunaijun.blackboxa.view.gms.GmsManagerActivity
import top.niunaijun.blackboxa.view.list.ListActivity

class MainActivity : BaseActivity() {

    private val viewBinding: ActivityMainBinding by inflate()
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val profileAppCounts = mutableMapOf<Int, Int>()
    private var pagerUpdateGeneration = 0
    private var pagerDataUpdateInProgress = false
    private var pendingPagerUserId: Int? = null

    var activeUserId by mutableIntStateOf(-1)
        private set

    var activeContainerId by mutableStateOf("")
        private set

    var profiles by mutableStateOf<List<ProfileItem>>(emptyList())
        private set

    var containers by mutableStateOf<List<ContainerItem>>(emptyList())
        private set

    var workspaceScrolled by mutableStateOf(false)
        private set

    var storageRationaleVisible by mutableStateOf(false)
        private set

    companion object {
        private const val TAG = "MainActivity"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001

        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private val storagePermissionResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    if (android.os.Environment.isExternalStorageManager()) {
                        Log.d(TAG, "Storage permission granted")
                    } else {
                        Log.w(TAG, "Storage permission still not granted")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling storage permission result: ${e.message}")
            }
        }

    private val apkPathResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == RESULT_OK) {
                    val data = result.data ?: return@registerForActivityResult
                    val userId = data.getIntExtra("userID", activeUserId)
                    val source = data.getStringExtra("source")
                    if (source != null && userId >= 0) {
                        selectProfile(userId)
                        val targetFragment = pagerAdapter.fragments().firstOrNull { it.userID == userId }
                        if (targetFragment != null) {
                            targetFragment.installApk(source)
                        } else {
                            Toast.makeText(
                                this,
                                "That profile is no longer available. Choose it again.",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling APK path result: ${e.message}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            try {
                BlackBoxCore.get().onBeforeMainActivityOnCreate(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error before MainActivity creation: ${e.message}")
            }

            setContentView(viewBinding.root)
            initializeProfilesAndPager()

            viewBinding.composeView.setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            viewBinding.composeView.setContent {
                SleepyHubTheme {
                    SleepyHubMainScreen(
                        profiles = profiles,
                        containers = containers,
                        activeUserId = activeUserId,
                        activeContainerId = activeContainerId,
                        isWorkspaceScrolled = workspaceScrolled,
                        viewPager = viewPager,
                        storageRationaleVisible = storageRationaleVisible,
                        onDismissStorageRationale = { storageRationaleVisible = false },
                        onGrantStoragePermission = ::openAllFilesAccessSettings,
                        onSelectProfile = ::selectProfile,
                        onAddProfile = ::createProfile,
                        onRenameProfile = ::renameProfile,
                        onDeleteProfile = ::deleteProfile,
                        onMoveProfile = ::moveProfile,
                        onSetProfileFolder = ::setProfileFolder,
                        onToggleProfilePinned = ::toggleProfilePinned,
                        onCloneProfile = ::cloneProfile,
                        onReorderProfiles = ::reorderProfiles,
                        onUpdateProfileAvatar = ::updateProfileAvatar,
                        onUpdateProfileBackground = ::updateProfileBackground,
                        onCreateContainer = ::createContainer,
                        onSelectContainer = ::switchContainer,
                        onUpdateContainer = ::updateContainer,
                        onDeleteContainer = ::deleteContainer,
                        onInstallApps = ::openInstallPicker,
                        onOpenFakeLocation = ::openFakeLocation,
                        onOpenGmsManager = ::openGmsManager,
                        onOpenStorageAccess = ::openAllFilesAccessSettings,
                        onRefreshApps = ::refreshCurrentApps,
                        onRefreshProfiles = ::refreshProfiles,
                    )
                }
            }

            checkStoragePermission()
            try {
                BlackBoxCore.get().onAfterMainActivityOnCreate(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error after MainActivity creation: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Failed to initialize Sleepy Hub", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::pagerAdapter.isInitialized) {
            refreshProfiles()
            pagerAdapter.fragments().forEach { it.refreshGridColumns() }
        }
    }

    private fun initializeProfilesAndPager() {
        val core = BlackBoxCore.get()
        val users = core.users.sortedBy { it.id }
        ProfileAppearance.ensureContainers(users.map { it.id })
        activeContainerId = ProfileAppearance.activeContainerId()

        viewPager = ViewPager2(this).apply {
            id = android.view.View.generateViewId()
            offscreenPageLimit = 1
        }
        pagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (pagerDataUpdateInProgress) return
                    val fragment = pagerAdapter.fragmentAt(position) ?: return
                    val userId = fragment.userID
                    if (ProfileAppearance.profileContainer(userId) != activeContainerId) return
                    val pendingUserId = pendingPagerUserId
                    if (pendingUserId != null && pendingUserId != userId) return
                    pendingPagerUserId = null
                    activeUserId = userId
                    workspaceScrolled = fragment.isWorkspaceScrolled
                    ProfileAppearance.saveLastProfileId(activeContainerId, userId)
                    refreshProfiles()
                }
            }
        )
        loadContainerProfiles(activeContainerId)
    }

    private fun loadContainerProfiles(containerId: String, preferredUserId: Int? = null) {
        val users = BlackBoxCore.get().users.sortedBy { it.id }
        ProfileAppearance.ensureContainers(users.map { it.id })
        val orderIndex = ProfileAppearance.profileOrder(containerId)
            .withIndex()
            .associate { it.value to it.index }
        val members = users
            .filter { ProfileAppearance.profileContainer(it.id) == containerId }
            .sortedWith(
                compareBy(
                    { if (ProfileAppearance.isProfilePinned(it.id)) 0 else 1 },
                    { orderIndex[it.id] ?: Int.MAX_VALUE },
                    { it.id },
                )
            )

        val selected = preferredUserId?.takeIf { id -> members.any { it.id == id } }
            ?: ProfileAppearance.lastProfileId(containerId).takeIf { id -> members.any { it.id == id } }
            ?: ProfileAppearance.lastProfileId().takeIf { id -> members.any { it.id == id } }
            ?: members.firstOrNull()?.id

        // FragmentStateAdapter can emit onPageSelected for the old numeric index
        // while its stable item IDs are being replaced. Ignore those callbacks until
        // the new target page has been selected and the adapter has settled.
        val generation = ++pagerUpdateGeneration
        pagerDataUpdateInProgress = true
        pendingPagerUserId = selected
        activeUserId = selected ?: -1

        pagerAdapter.replaceData(members.map { AppsFragment.newInstance(it.id) })
        if (selected != null) ProfileAppearance.saveLastProfileId(containerId, selected)
        refreshProfiles()
        val selectedIndex = selected?.let(pagerAdapter::indexOfUserId) ?: -1
        workspaceScrolled = pagerAdapter.fragmentAt(selectedIndex)?.isWorkspaceScrolled ?: false

        viewPager.post {
            if (generation != pagerUpdateGeneration) return@post
            if (activeContainerId == containerId && activeUserId == selected) {
                val index = selected?.let(pagerAdapter::indexOfUserId) ?: -1
                if (index >= 0 && viewPager.adapter === pagerAdapter) {
                    viewPager.setCurrentItem(index, false)
                    workspaceScrolled = pagerAdapter.fragmentAt(index)?.isWorkspaceScrolled ?: false
                }
            }
            pagerDataUpdateInProgress = false
            pendingPagerUserId = null
        }
    }

    fun refreshProfiles() {
        try {
            val users = BlackBoxCore.get().users.sortedBy { it.id }
            ProfileAppearance.ensureContainers(users.map { it.id })
            val allProfiles = users.mapIndexed { index, user ->
                ProfileItem(
                    userId = user.id,
                    name = ProfileAppearance.profileName(user.id, index),
                    folder = ProfileAppearance.profileFolder(user.id),
                    appCount = profileAppCounts[user.id] ?: -1,
                    pinned = ProfileAppearance.isProfilePinned(user.id),
                    avatarUri = ProfileAppearance.profileAvatarUri(user.id),
                    backgroundUri = ProfileAppearance.profileBackgroundUri(user.id),
                    backgroundOpacity = ProfileAppearance.profileBackgroundOpacity(user.id),
                    googlePlayEnabled = runCatching {
                        BlackBoxCore.get().isInstallGms(user.id)
                    }.getOrDefault(false),
                )
            }
            val order = ProfileAppearance.profileOrder(activeContainerId)
            val orderIndex = order.withIndex().associate { it.value to it.index }
            profiles = allProfiles
                .filter { ProfileAppearance.profileContainer(it.userId) == activeContainerId }
                .sortedWith(
                    compareByDescending<ProfileItem> { it.pinned }
                        .thenBy { orderIndex[it.userId] ?: Int.MAX_VALUE }
                        .thenBy { it.userId }
                )
            containers = ProfileAppearance.containers().map { container ->
                ContainerItem(
                    id = container.id,
                    name = container.name,
                    avatarUri = container.avatarUri,
                    profileCount = users.count {
                        ProfileAppearance.profileContainer(it.id) == container.id
                    },
                )
            }
            if (activeUserId >= 0 && profiles.none { it.userId == activeUserId }) {
                activeUserId = profiles.firstOrNull()?.userId ?: -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not refresh profiles: ${e.message}", e)
        }
    }

    fun onWorkspaceScrollChanged(userId: Int, scrolled: Boolean) {
        // Offscreen ViewPager pages stay composed; only the visible profile may
        // control the dashboard's collapsing header.
        if (userId == activeUserId) workspaceScrolled = scrolled
    }

    fun onProfileAppCountChanged(userId: Int, count: Int) {
        profileAppCounts[userId] = count
        refreshProfiles()
    }

    fun selectProfile(userId: Int) {
        if (ProfileAppearance.profileContainer(userId) != activeContainerId) return
        val index = pagerAdapter.indexOfUserId(userId)
        if (index < 0) {
            loadContainerProfiles(activeContainerId, userId)
            return
        }
        activeUserId = userId
        pendingPagerUserId = userId
        workspaceScrolled = pagerAdapter.fragmentAt(index)?.isWorkspaceScrolled ?: false
        ProfileAppearance.saveLastProfileId(activeContainerId, userId)
        refreshProfiles()
        viewPager.setCurrentItem(index, true)
        viewPager.post {
            val currentIndex = pagerAdapter.indexOfUserId(userId)
            if (activeUserId == userId && currentIndex >= 0) {
                if (viewPager.currentItem != currentIndex) {
                    viewPager.setCurrentItem(currentIndex, true)
                }
                if (viewPager.currentItem == currentIndex) pendingPagerUserId = null
            }
        }
    }

    fun switchContainer(containerId: String, preferredUserId: Int? = null) {
        if (containers.none { it.id == containerId } &&
            ProfileAppearance.containers().none { it.id == containerId }
        ) return
        activeContainerId = containerId
        ProfileAppearance.saveActiveContainerId(containerId)
        loadContainerProfiles(containerId, preferredUserId)
    }

    fun createProfile() {
        try {
            if (activeContainerId.isBlank()) {
                Toast.makeText(this, "Choose a container first", Toast.LENGTH_SHORT).show()
                return
            }
            val users = BlackBoxCore.get().users
            val nextId = (users.maxOfOrNull { it.id } ?: -1) + 1
            BlackBoxCore.get().createUser(nextId)
            ProfileAppearance.setProfileContainer(nextId, activeContainerId)
            loadContainerProfiles(activeContainerId, nextId)
        } catch (e: Exception) {
            Log.e(TAG, "Could not create profile: ${e.message}", e)
            Toast.makeText(this, "Couldn't create profile", Toast.LENGTH_SHORT).show()
        }
    }

    fun renameProfile(userId: Int, name: String) {
        if (name.isBlank()) return
        ProfileAppearance.setProfileName(userId, name)
        refreshProfiles()
    }

    fun setProfileFolder(userId: Int, folder: String) {
        ProfileAppearance.setProfileFolder(userId, folder)
        refreshProfiles()
    }

    fun toggleProfilePinned(userId: Int, pinned: Boolean) {
        ProfileAppearance.setProfilePinned(userId, pinned)
        loadContainerProfiles(activeContainerId, activeUserId)
    }

    fun reorderProfiles(userIds: List<Int>) {
        val validIds = profiles.map { it.userId }.toSet()
        val ordered = userIds.filter { it in validIds }.distinct() +
            profiles.map { it.userId }.filterNot { it in userIds }
        ProfileAppearance.setProfileOrder(activeContainerId, ordered)
        loadContainerProfiles(activeContainerId, activeUserId)
    }

    fun updateProfileAvatar(userId: Int, uri: String?) {
        val previous = ProfileAppearance.profileAvatarUri(userId)
        ProfileAppearance.setProfileAvatarUri(userId, uri)
        if (previous != uri) releaseProfileUriIfUnused(previous, userId)
        refreshProfiles()
    }

    fun updateProfileBackground(userId: Int, uri: String?, opacity: Float) {
        val previous = ProfileAppearance.profileBackgroundUri(userId)
        ProfileAppearance.setProfileBackgroundUri(userId, uri)
        ProfileAppearance.setProfileBackgroundOpacity(userId, opacity)
        if (previous != uri) releaseProfileUriIfUnused(previous, userId)
        refreshProfiles()
    }

    fun cloneProfile(sourceUserId: Int) {
        if (ProfileAppearance.profileContainer(sourceUserId) != activeContainerId) return
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val core = BlackBoxCore.get()
                    val orderedUsers = core.users.sortedBy { it.id }
                    val sourceProfileIndex = orderedUsers.indexOfFirst { it.id == sourceUserId }
                        .coerceAtLeast(0)
                    val sourceUser = orderedUsers.firstOrNull { it.id == sourceUserId }
                        ?: throw IllegalStateException("The source profile no longer exists.")
                    val newUserId = (core.users.maxOfOrNull { it.id } ?: -1) + 1
                    core.createUser(newUserId)
                    try {
                        ProfileAppearance.setProfileContainer(newUserId, activeContainerId)
                        ProfileAppearance.setProfileName(
                            newUserId,
                            "${ProfileAppearance.profileName(sourceUserId, sourceProfileIndex)} copy",
                        )
                        ProfileAppearance.setProfileFolder(newUserId, ProfileAppearance.profileFolder(sourceUserId))
                        ProfileAppearance.setProfileAvatarUri(newUserId, ProfileAppearance.profileAvatarUri(sourceUserId))
                        ProfileAppearance.setProfileBackgroundUri(newUserId, ProfileAppearance.profileBackgroundUri(sourceUserId))
                        ProfileAppearance.setProfileBackgroundOpacity(
                            newUserId,
                            ProfileAppearance.profileBackgroundOpacity(sourceUserId),
                        )

                        if (core.isInstallGms(sourceUserId)) {
                            runCatching { core.installGms(newUserId) }
                        }

                        var copied = 0
                        var skipped = 0
                        val sourceApps = core.getInstalledApplications(0, sourceUserId)
                        sourceApps.forEach { app ->
                            val packageName = app.packageName ?: return@forEach
                            if (packageName.isBlank() || GmsCore.isGoogleAppOrService(packageName)) return@forEach
                            val apk = app.sourceDir?.let(::File)
                            val result = if (apk?.isFile == true) {
                                core.installPackageAsUser(apk, newUserId)
                            } else {
                                core.installPackageAsUser(packageName, newUserId)
                            }
                            if (result.success) copied++ else skipped++
                        }
                        Triple(newUserId, copied, skipped)
                    } catch (error: Exception) {
                        runCatching { core.deleteUser(newUserId) }
                        ProfileAppearance.clearProfile(newUserId)
                        throw error
                    }
                }
                loadContainerProfiles(activeContainerId, result.first)
                Toast.makeText(
                    this@MainActivity,
                    "Profile cloned: ${result.second} apps copied, ${result.third} skipped. App data starts fresh.",
                    Toast.LENGTH_LONG,
                ).show()
            } catch (error: Exception) {
                Log.e(TAG, "Could not clone profile $sourceUserId", error)
                Toast.makeText(this@MainActivity, "Couldn't clone that profile", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun moveProfile(userId: Int, targetContainerId: String) {
        if (containers.none { it.id == targetContainerId }) return
        if (ProfileAppearance.profileContainer(userId) == targetContainerId) return
        ProfileAppearance.setProfileContainer(userId, targetContainerId)
        // Folder assignments belong to the old container, just as RikkaHub clears
        // assistant-scoped folders when moving a conversation.
        ProfileAppearance.setProfileFolder(userId, "General")
        if (activeUserId == userId) {
            switchContainer(targetContainerId, userId)
        } else {
            // Rebuild the current container's pager so a moved, non-active profile
            // can no longer be reached by swiping from its previous container.
            loadContainerProfiles(activeContainerId, activeUserId)
        }
    }

    fun createContainer(name: String) {
        val created = ProfileAppearance.createContainer(name)
        refreshProfiles()
        switchContainer(created.id)
    }

    fun updateContainer(updated: ContainerItem) {
        val current = ProfileAppearance.containers().firstOrNull { it.id == updated.id } ?: return
        if (current.avatarUri != updated.avatarUri) releaseContainerUri(current.avatarUri)
        ProfileAppearance.updateContainer(
            HubContainer(
                id = current.id,
                name = updated.name.trim().ifBlank { current.name },
                avatarUri = updated.avatarUri,
                backgroundUri = null,
                backgroundOpacity = 0.32f,
            )
        )
        refreshProfiles()
    }

    fun deleteContainer(containerId: String) {
        val allContainers = ProfileAppearance.containers()
        val existingTarget = allContainers.firstOrNull { it.id != containerId }
        try {
            val userIds = BlackBoxCore.get().users
                .filter { ProfileAppearance.profileContainer(it.id) == containerId }
                .map { it.id }

            userIds.forEach { userId ->
                val oldAvatar = ProfileAppearance.profileAvatarUri(userId)
                val oldBackground = ProfileAppearance.profileBackgroundUri(userId)
                BlackBoxCore.get().deleteUser(userId)
                ProfileAppearance.clearProfile(userId)
                releaseProfileUriIfUnused(oldAvatar, userId)
                releaseProfileUriIfUnused(oldBackground, userId)
                profileAppCounts.remove(userId)
            }

            val removed = allContainers.firstOrNull { it.id == containerId }
            releaseContainerUri(removed?.avatarUri)
            ProfileAppearance.removeContainer(containerId)
            val target = existingTarget ?: ProfileAppearance.createContainer("Default")

            if (activeContainerId == containerId) {
                refreshProfiles()
                switchContainer(target.id)
            } else {
                refreshProfiles()
            }
            Toast.makeText(this, "Container deleted", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Could not delete container $containerId: ${e.message}", e)
            refreshProfiles()
            Toast.makeText(this, "Couldn't completely delete the container", Toast.LENGTH_LONG).show()
        }
    }

    private fun releaseProfileUriIfUnused(value: String?, deletedOrUpdatedUserId: Int) {
        if (value.isNullOrBlank()) return
        val usedByAnotherProfile = BlackBoxCore.get().users.any { user ->
            user.id != deletedOrUpdatedUserId &&
                (ProfileAppearance.profileAvatarUri(user.id) == value ||
                    ProfileAppearance.profileBackgroundUri(user.id) == value)
        }
        val usedByContainer = ProfileAppearance.containers().any { container ->
            container.avatarUri == value || container.backgroundUri == value
        }
        if (!usedByAnotherProfile && !usedByContainer) releaseContainerUri(value)
    }

    private fun releaseContainerUri(value: String?) {
        if (value.isNullOrBlank()) return
        val stillUsedByProfile = runCatching {
            BlackBoxCore.get().users.any { user ->
                ProfileAppearance.profileAvatarUri(user.id) == value ||
                    ProfileAppearance.profileBackgroundUri(user.id) == value
            }
        }.getOrDefault(false)
        val stillUsedByContainer = ProfileAppearance.containers().any { container ->
            container.avatarUri == value || container.backgroundUri == value
        }
        if (stillUsedByProfile || stillUsedByContainer) return
        try {
            contentResolver.releasePersistableUriPermission(
                Uri.parse(value),
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: Exception) {
        }
    }

    fun deleteProfile(userId: Int) {
        try {
            val users = BlackBoxCore.get().users.sortedBy { it.id }
            if (users.size <= 1) {
                Toast.makeText(this, "Keep at least one virtual profile", Toast.LENGTH_SHORT).show()
                return
            }

            val sameContainer = users.filter {
                it.id != userId && ProfileAppearance.profileContainer(it.id) == activeContainerId
            }
            val wasActive = activeUserId == userId
            val oldAvatar = ProfileAppearance.profileAvatarUri(userId)
            val oldBackground = ProfileAppearance.profileBackgroundUri(userId)
            BlackBoxCore.get().deleteUser(userId)
            ProfileAppearance.clearProfile(userId)
            releaseProfileUriIfUnused(oldAvatar, userId)
            releaseProfileUriIfUnused(oldBackground, userId)
            profileAppCounts.remove(userId)
            loadContainerProfiles(
                activeContainerId,
                if (wasActive) sameContainer.firstOrNull()?.id else activeUserId,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Could not delete profile $userId: ${e.message}", e)
            Toast.makeText(this, "Couldn't delete profile", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInstallPicker() {
        val activeProfileExists = activeUserId >= 0 &&
            BlackBoxCore.get().users.any { it.id == activeUserId } &&
            ProfileAppearance.profileContainer(activeUserId) == activeContainerId
        if (!activeProfileExists) {
            Toast.makeText(this, "Create or select a profile before adding apps.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            val intent = Intent(this, ListActivity::class.java).putExtra("userID", activeUserId)
            apkPathResult.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open app installer: ${e.message}")
            Toast.makeText(this, "Couldn't open app installer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFakeLocation() {
        val activeProfileExists = activeUserId >= 0 &&
            BlackBoxCore.get().users.any { it.id == activeUserId } &&
            ProfileAppearance.profileContainer(activeUserId) == activeContainerId
        if (!activeProfileExists) {
            Toast.makeText(this, "Select a profile before setting a fake location.", Toast.LENGTH_LONG).show()
            return
        }
        startActivity(Intent(this, FakeManagerActivity::class.java).putExtra("userID", activeUserId))
    }

    fun openGmsManager() {
        if (activeUserId < 0) {
            Toast.makeText(this, "Create or select a profile first", Toast.LENGTH_SHORT).show()
            return
        }
        if (BlackBoxCore.get().isSupportGms) {
            GmsManagerActivity.start(this, activeContainerId)
        } else {
            Toast.makeText(this, "Google services are not supported", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshCurrentApps() {
        pagerAdapter.fragments().forEach { it.refreshGridColumns() }
        if (activeUserId >= 0) {
            pagerAdapter.fragments().firstOrNull { it.userID == activeUserId && it.isAdded }?.refreshApps()
        }
    }

    fun scanUser() {
        refreshProfiles()
    }

    private fun checkStoragePermission() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (!android.os.Environment.isExternalStorageManager()) {
                    storageRationaleVisible = true
                }
            } else if (
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking storage permission: ${e.message}")
        }
    }

    private fun openAllFilesAccessSettings() {
        storageRationaleVisible = false
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) return
        try {
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:$packageName")
            )
            storagePermissionResult.launch(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Opening app-specific storage settings failed: ${e.message}")
            try {
                storagePermissionResult.launch(
                    Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            } catch (fallback: Exception) {
                Log.e(TAG, "Opening storage settings failed: ${fallback.message}")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() &&
                grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            if (!granted) Log.w(TAG, "Legacy storage permissions were denied")
        }
    }
}
