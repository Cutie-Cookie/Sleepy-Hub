package top.niunaijun.blackboxa.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import top.niunaijun.blackbox.BlackBoxCore
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
    private val fragmentList = mutableListOf<AppsFragment>()
    private val profileAppCounts = mutableMapOf<Int, Int>()

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
                        fragmentList.firstOrNull { it.userID == userId }?.installApk(source)
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
            fragmentList.forEach { it.refreshGridColumns() }
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
                    val fragment = fragmentList.getOrNull(position) ?: return
                    activeUserId = fragment.userID
                    ProfileAppearance.saveLastProfileId(activeContainerId, activeUserId)
                    refreshProfiles()
                }
            }
        )
        loadContainerProfiles(activeContainerId, ProfileAppearance.lastProfileId())
    }

    private fun loadContainerProfiles(containerId: String, preferredUserId: Int? = null) {
        val users = BlackBoxCore.get().users.sortedBy { it.id }
        ProfileAppearance.ensureContainers(users.map { it.id })
        val members = users.filter { ProfileAppearance.profileContainer(it.id) == containerId }

        fragmentList.clear()
        members.forEach { fragmentList.add(AppsFragment.newInstance(it.id)) }
        pagerAdapter.replaceData(fragmentList)

        val selected = preferredUserId?.takeIf { id -> members.any { it.id == id } }
            ?: ProfileAppearance.lastProfileId(containerId).takeIf { id -> members.any { it.id == id } }
            ?: ProfileAppearance.lastProfileId().takeIf { id -> members.any { it.id == id } }
            ?: members.firstOrNull()?.id
        activeUserId = selected ?: -1
        refreshProfiles()

        val index = fragmentList.indexOfFirst { it.userID == activeUserId }
        if (index >= 0) viewPager.setCurrentItem(index, false)
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
                )
            }
            profiles = allProfiles.filter {
                ProfileAppearance.profileContainer(it.userId) == activeContainerId
            }
            containers = ProfileAppearance.containers().map { container ->
                ContainerItem(
                    id = container.id,
                    name = container.name,
                    avatarUri = container.avatarUri,
                    backgroundUri = container.backgroundUri,
                    backgroundOpacity = container.backgroundOpacity,
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

    fun onWorkspaceScrollChanged(scrolled: Boolean) {
        workspaceScrolled = scrolled
    }

    fun onProfileAppCountChanged(userId: Int, count: Int) {
        profileAppCounts[userId] = count
        refreshProfiles()
    }

    fun selectProfile(userId: Int) {
        val index = fragmentList.indexOfFirst { it.userID == userId }
        if (index >= 0) {
            viewPager.setCurrentItem(index, true)
            activeUserId = userId
            ProfileAppearance.saveLastProfileId(activeContainerId, userId)
            refreshProfiles()
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
            fragmentList.add(AppsFragment.newInstance(nextId))
            pagerAdapter.replaceData(fragmentList)
            refreshProfiles()
            selectProfile(nextId)
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
        refreshProfiles()
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
            refreshProfiles()
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
        if (current.backgroundUri != updated.backgroundUri) releaseContainerUri(current.backgroundUri)
        ProfileAppearance.updateContainer(
            HubContainer(
                id = current.id,
                name = updated.name.trim().ifBlank { current.name },
                avatarUri = updated.avatarUri,
                backgroundUri = updated.backgroundUri,
                backgroundOpacity = updated.backgroundOpacity.coerceIn(0f, 1f),
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
                BlackBoxCore.get().deleteUser(userId)
                ProfileAppearance.clearProfile(userId)
                profileAppCounts.remove(userId)
            }

            val removed = allContainers.firstOrNull { it.id == containerId }
            releaseContainerUri(removed?.avatarUri)
            releaseContainerUri(removed?.backgroundUri)
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

    private fun releaseContainerUri(value: String?) {
        if (value.isNullOrBlank()) return
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
            BlackBoxCore.get().deleteUser(userId)
            ProfileAppearance.clearProfile(userId)
            profileAppCounts.remove(userId)
            if (wasActive) {
                loadContainerProfiles(activeContainerId, sameContainer.firstOrNull()?.id)
            } else {
                refreshProfiles()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not delete profile $userId: ${e.message}", e)
            Toast.makeText(this, "Couldn't delete profile", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInstallPicker() {
        if (activeUserId < 0) {
            Toast.makeText(this, "Add a profile before installing apps", Toast.LENGTH_SHORT).show()
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
        if (activeUserId < 0) return
        startActivity(Intent(this, FakeManagerActivity::class.java).putExtra("userID", activeUserId))
    }

    fun openGmsManager() {
        if (BlackBoxCore.get().isSupportGms) {
            GmsManagerActivity.start(this)
        } else {
            Toast.makeText(this, "Google services are not supported", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshCurrentApps() {
        fragmentList.forEach { it.refreshGridColumns() }
        if (activeUserId >= 0) {
            fragmentList.firstOrNull { it.userID == activeUserId }?.refreshApps()
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
