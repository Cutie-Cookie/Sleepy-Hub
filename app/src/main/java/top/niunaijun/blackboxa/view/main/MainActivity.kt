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
import com.afollestad.materialdialogs.MaterialDialog
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.AppManager
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.databinding.ActivityMainBinding
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.view.apps.AppsFragment
import top.niunaijun.blackboxa.view.base.LoadingActivity
import top.niunaijun.blackboxa.view.fake.FakeManagerActivity
import top.niunaijun.blackboxa.view.list.ListActivity

class MainActivity : LoadingActivity() {

    private val viewBinding: ActivityMainBinding by inflate()
    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ViewPagerAdapter
    private val fragmentList = mutableListOf<AppsFragment>()
    private val profileAppCounts = mutableMapOf<Int, Int>()

    var activeUserId by mutableIntStateOf(0)
        private set

    var profiles by mutableStateOf<List<ProfileItem>>(emptyList())
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
                    if (source != null) {
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
                        activeUserId = activeUserId,
                        viewPager = viewPager,
                        onSelectProfile = ::selectProfile,
                        onAddProfile = ::createProfile,
                        onRenameProfile = ::renameProfile,
                        onDeleteProfile = ::deleteProfile,
                        onInstallApps = ::openInstallPicker,
                        onOpenFakeLocation = ::openFakeLocation,
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
            refreshCurrentApps()
        }
    }

    private fun initializeProfilesAndPager() {
        val core = BlackBoxCore.get()
        var users = core.users.sortedBy { it.id }
        if (users.isEmpty()) {
            core.createUser(0)
            users = core.users.sortedBy { it.id }
        }

        fragmentList.clear()
        users.forEach { user ->
            fragmentList.add(AppsFragment.newInstance(user.id))
        }

        activeUserId = users.firstOrNull { it.id == ProfileAppearance.lastProfileId() }?.id
            ?: users.firstOrNull()?.id
            ?: 0

        viewPager = ViewPager2(this).apply {
            id = android.view.View.generateViewId()
            offscreenPageLimit = 1
            setPageTransformer { page, position ->
                val distance = kotlin.math.abs(position).coerceIn(0f, 1f)
                page.alpha = 1f - distance * 0.08f
                page.scaleY = 1f - distance * 0.015f
                page.translationX = -position * 8f
            }
        }

        pagerAdapter = ViewPagerAdapter(this)
        pagerAdapter.replaceData(fragmentList)
        viewPager.adapter = pagerAdapter
        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val fragment = fragmentList.getOrNull(position) ?: return
                    activeUserId = fragment.userID
                    ProfileAppearance.saveLastProfileId(activeUserId)
                    refreshProfiles()
                }
            }
        )

        val initialIndex = fragmentList.indexOfFirst { it.userID == activeUserId }.coerceAtLeast(0)
        viewPager.setCurrentItem(initialIndex, false)
        refreshProfiles()
    }

    fun refreshProfiles() {
        try {
            val users = BlackBoxCore.get().users.sortedBy { it.id }
            profiles = users.mapIndexed { index, user ->
                ProfileItem(
                    userId = user.id,
                    name = ProfileAppearance.profileName(user.id, index),
                    appCount = profileAppCounts[user.id] ?: -1,
                    accent = ProfileAppearance.profileAccent(user.id),
                    backgroundUri = ProfileAppearance.backgroundUri(user.id),
                    backgroundOpacity = ProfileAppearance.backgroundOpacity(user.id),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not refresh profiles: ${e.message}")
        }
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
            ProfileAppearance.saveLastProfileId(userId)
            refreshProfiles()
        }
    }

    fun createProfile() {
        try {
            val users = BlackBoxCore.get().users
            val nextId = (users.maxOfOrNull { it.id } ?: -1) + 1
            BlackBoxCore.get().createUser(nextId)
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

    fun deleteProfile(userId: Int) {
        try {
            val users = BlackBoxCore.get().users.sortedBy { it.id }
            if (users.size <= 1) {
                Toast.makeText(this, "Keep at least one profile", Toast.LENGTH_SHORT).show()
                return
            }

            val background = ProfileAppearance.backgroundUri(userId)
            if (!background.isNullOrBlank()) {
                try {
                    contentResolver.releasePersistableUriPermission(
                        Uri.parse(background),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
            }

            BlackBoxCore.get().deleteUser(userId)
            ProfileAppearance.clearProfile(userId)
            profileAppCounts.remove(userId)
            fragmentList.removeAll { it.userID == userId }
            pagerAdapter.replaceData(fragmentList)

            val nextUser = users.firstOrNull { it.id != userId }?.id ?: fragmentList.first().userID
            refreshProfiles()
            selectProfile(nextUser)
        } catch (e: Exception) {
            Log.e(TAG, "Could not delete profile $userId: ${e.message}", e)
            Toast.makeText(this, "Couldn't delete profile", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInstallPicker() {
        try {
            val intent = Intent(this, ListActivity::class.java).putExtra("userID", activeUserId)
            apkPathResult.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open app installer: ${e.message}")
            Toast.makeText(this, "Couldn't open app installer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFakeLocation() {
        startActivity(Intent(this, FakeManagerActivity::class.java).putExtra("userID", activeUserId))
    }

    fun refreshCurrentApps() {
        fragmentList.forEach { it.refreshGridColumns() }
        fragmentList.firstOrNull { it.userID == activeUserId }?.refreshApps()
    }

    fun scanUser() {
        refreshProfiles()
    }

    private fun checkStoragePermission() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (!android.os.Environment.isExternalStorageManager()) {
                    showStoragePermissionDialog()
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

    private fun showStoragePermissionDialog() {
        MaterialDialog(this).show {
            title(text = "Storage Permission Required")
            message(
                text = "Sleepy Hub needs All Files Access to run sandboxed apps correctly. You can grant it in the next screen."
            )
            positiveButton(text = "Grant permission") { openAllFilesAccessSettings() }
            negativeButton(text = "Later")
            cancelable(true)
        }
    }

    private fun openAllFilesAccessSettings() {
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
            if (!granted) {
                Log.w(TAG, "Legacy storage permissions were denied")
            }
        }
    }
}
