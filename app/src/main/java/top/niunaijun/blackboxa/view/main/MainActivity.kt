package top.niunaijun.blackboxa.view.main

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.google.android.material.card.MaterialCardView
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.databinding.ActivityMainBinding
import top.niunaijun.blackboxa.util.ProfileImageLoader
import top.niunaijun.blackboxa.util.Resolution
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.view.apps.AppsFragment
import top.niunaijun.blackboxa.view.base.LoadingActivity
import top.niunaijun.blackboxa.view.fake.FakeManagerActivity
import top.niunaijun.blackboxa.view.list.ListActivity
import top.niunaijun.blackboxa.view.setting.SettingActivity
import kotlin.math.abs

class MainActivity : LoadingActivity() {

    private val viewBinding: ActivityMainBinding by inflate()
    private lateinit var mViewPagerAdapter: ViewPagerAdapter
    private val fragmentList = mutableListOf<AppsFragment>()
    private var currentUser = 0
    private var pendingBackgroundUserId = -1

    companion object {
        private const val TAG = "MainActivity"
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001

        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private val profileBackgroundPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            val userId = pendingBackgroundUserId
            pendingBackgroundUserId = -1
            if (uri != null && userId >= 0) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Could not persist wallpaper permission: " + e.message)
                }
                val previous = ProfileAppearance.backgroundUri(userId)
                if (previous != null && previous != uri.toString()) {
                    releaseWallpaperPermission(previous)
                }
                ProfileAppearance.setBackgroundUri(userId, uri.toString())
                refreshDashboard()
                fragmentList.firstOrNull { it.userID == userId }?.refreshProfileHeader()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            try {
                BlackBoxCore.get().onBeforeMainActivityOnCreate(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error before MainActivity creation: " + e.message)
            }

            setContentView(viewBinding.root)
            initToolbar(viewBinding.toolbarLayout.toolbar, R.string.app_name)
            initViewPager()
            initFab()
            initDashboard()

            try {
                BlackBoxCore.get().onAfterMainActivityOnCreate(this)
            } catch (e: Exception) {
                Log.e(TAG, "Error after MainActivity creation: " + e.message)
            }
            checkStoragePermission()
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in onCreate: " + e.message)
            showErrorDialog("Failed to initialize app: " + e.message)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::mViewPagerAdapter.isInitialized) {
            refreshDashboard()
            fragmentList.forEach {
                it.refreshProfileHeader()
                it.refreshGridColumns()
            }
            updateUserRemark(currentUser)
            applyGlobalAccent()
        }
    }

    private fun initDashboard() {
        viewBinding.toolbarLayout.toolbar.setNavigationIcon(R.drawable.ic_menu)
        viewBinding.toolbarLayout.toolbar.navigationContentDescription = getString(R.string.open_dashboard)
        viewBinding.toolbarLayout.toolbar.navigationIcon?.setTint(
            ContextCompat.getColor(this, R.color.primary_text)
        )
        viewBinding.toolbarLayout.toolbar.setNavigationOnClickListener {
            viewBinding.drawerLayout.openDrawer(Gravity.START)
        }
        viewBinding.drawerLayout.setScrimColor(Color.argb(150, 0, 0, 0))
        viewBinding.addProfileButton.setOnClickListener { createProfile() }
        viewBinding.settingsButton.setOnClickListener {
            viewBinding.drawerLayout.closeDrawer(Gravity.START)
            SettingActivity.start(this)
        }
        viewBinding.fakeLocationButton.setOnClickListener {
            viewBinding.drawerLayout.closeDrawer(Gravity.START)
            startActivity(Intent(this, FakeManagerActivity::class.java).putExtra("userID", currentUser))
        }
        applyGlobalAccent()
        refreshDashboard()
    }

    private fun applyGlobalAccent() {
        val accent = ProfileAppearance.globalAccent(this)
        viewBinding.addProfileButton.backgroundTintList = ColorStateList.valueOf(accent)
        viewBinding.fab.backgroundTintList = ColorStateList.valueOf(accent)
    }

    private fun initViewPager() {
        try {
            var users = BlackBoxCore.get().users.sortedBy { it.id }
            if (users.isEmpty()) {
                BlackBoxCore.get().createUser(0)
                users = BlackBoxCore.get().users.sortedBy { it.id }
            }
            fragmentList.clear()
            users.forEach { fragmentList.add(AppsFragment.newInstance(it.id)) }

            val savedProfileId = ProfileAppearance.lastProfileId()
            currentUser = users.firstOrNull { it.id == savedProfileId }?.id
                ?: users.firstOrNull()?.id
                ?: 0

            mViewPagerAdapter = ViewPagerAdapter(this)
            mViewPagerAdapter.replaceData(fragmentList)
            viewBinding.viewPager.adapter = mViewPagerAdapter
            viewBinding.viewPager.offscreenPageLimit = 1
            viewBinding.viewPager.setPageTransformer { page, position ->
                val distance = abs(position).coerceIn(0f, 1f)
                page.alpha = 1f - (distance * 0.14f)
                page.scaleY = 1f - (distance * 0.018f)
                page.translationX = -position * 16f
            }
            viewBinding.viewPager.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        val fragment = fragmentList.getOrNull(position) ?: return
                        currentUser = fragment.userID
                        ProfileAppearance.saveLastProfileId(currentUser)
                        updateUserRemark(currentUser)
                        refreshDashboard()
                    }
                }
            )
            val initialIndex = fragmentList.indexOfFirst { it.userID == currentUser }.coerceAtLeast(0)
            viewBinding.viewPager.setCurrentItem(initialIndex, false)
            updateUserRemark(currentUser)
        } catch (e: Exception) {
            Log.e(TAG, "Error in initViewPager: " + e.message)
        }
    }

    private fun initFab() {
        viewBinding.fab.setOnClickListener {
            try {
                val intent = Intent(this, ListActivity::class.java)
                intent.putExtra("userID", currentUser)
                apkPathResult.launch(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error launching installed-app picker: " + e.message)
            }
        }
    }

    fun refreshDashboard() {
        if (!::mViewPagerAdapter.isInitialized) return
        val profiles = try {
            BlackBoxCore.get().users.sortedBy { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "Could not load profiles: " + e.message)
            emptyList()
        }
        viewBinding.profileCount.text = getString(R.string.profiles_count, profiles.size)
        viewBinding.profileList.removeAllViews()

        profiles.forEachIndexed { index, profile ->
            val userId = profile.id
            val selected = userId == currentUser
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(88)
                ).apply { bottomMargin = dp(9) }
                radius = dp(19).toFloat()
                cardElevation = 0f
                strokeWidth = dp(1)
                strokeColor = Color.argb(62, 255, 255, 255)
                setCardBackgroundColor(Color.TRANSPARENT)
                isClickable = true
                isFocusable = true
                alpha = 0f
                translationY = dp(7).toFloat()
            }

            val frame = FrameLayout(this)
            val wallpaper = ImageView(this).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = ProfileAppearance.backgroundOpacity(userId) * 0.78f
            }
            val tint = View(this).apply {
                background = ProfileAppearance.glassBackground(this@MainActivity, userId, selected)
            }
            val shade = View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    intArrayOf(Color.argb(35, 0, 0, 0), Color.argb(125, 0, 0, 0))
                )
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(10), dp(8), dp(10))
            }
            val avatar = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(42), dp(42))
                gravity = Gravity.CENTER
                text = (index + 1).toString()
                textSize = 15f
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(ProfileAppearance.accent(userId))
                    setStroke(dp(1), Color.argb(90, 255, 255, 255))
                }
            }
            val labels = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { marginStart = dp(12) }
            }
            val title = TextView(this).apply {
                text = ProfileAppearance.profileName(userId)
                textSize = 15f
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setTextColor(Color.WHITE)
                setTypeface(typeface, Typeface.BOLD)
            }
            val subtitle = TextView(this).apply {
                val appCount = ProfileAppearance.installedAppCount(userId)
                val countLabel = if (appCount == 0) getString(R.string.profile_no_apps)
                    else getString(R.string.profile_apps_count, appCount)
                text = if (selected) getString(R.string.profile_current) + " · " + countLabel else countLabel
                textSize = 11f
                maxLines = 1
                setTextColor(Color.argb(220, 255, 255, 255))
            }
            labels.addView(title)
            labels.addView(subtitle)

            val more = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(38), dp(38))
                setImageResource(R.drawable.ic_more)
                setColorFilter(Color.WHITE)
                background = android.graphics.drawable.RippleDrawable(
                    ColorStateList.valueOf(Color.argb(55, 255, 255, 255)),
                    null,
                    GradientDrawable().apply { shape = GradientDrawable.OVAL }
                )
                contentDescription = getString(R.string.profile_settings)
                setOnClickListener { showProfileOptions(userId, this) }
            }

            row.addView(avatar)
            row.addView(labels)
            row.addView(more)
            frame.addView(wallpaper, FrameLayout.LayoutParams(-1, -1))
            frame.addView(tint, FrameLayout.LayoutParams(-1, -1))
            frame.addView(shade, FrameLayout.LayoutParams(-1, -1))
            frame.addView(row, FrameLayout.LayoutParams(-1, -1))
            card.addView(frame)
            card.setOnClickListener { switchProfile(userId) }
            viewBinding.profileList.addView(card)

            wallpaper.post {
                ProfileImageLoader.load(this, ProfileAppearance.backgroundUri(userId), wallpaper)
            }
            card.animate().alpha(1f).translationY(0f).setDuration(190L + index * 25L).start()
        }
        applyGlobalAccent()
    }

    private fun switchProfile(userId: Int) {
        val index = fragmentList.indexOfFirst { it.userID == userId }
        if (index >= 0) {
            currentUser = userId
            ProfileAppearance.saveLastProfileId(userId)
            viewBinding.viewPager.setCurrentItem(index, true)
            updateUserRemark(userId)
        }
        viewBinding.drawerLayout.closeDrawer(Gravity.START)
    }

    private fun createProfile() {
        try {
            val users = BlackBoxCore.get().users
            val nextId = (users.maxOfOrNull { it.id } ?: -1) + 1
            BlackBoxCore.get().createUser(nextId)
            ProfileAppearance.setProfileName(nextId, "Profile " + (users.size + 1))
            syncProfiles(nextId)
            viewBinding.drawerLayout.closeDrawer(Gravity.START)
            Toast.makeText(this, R.string.profile_created, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Could not create profile: " + e.message)
            Toast.makeText(this, getString(R.string.profile_create_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun syncProfiles(selectUserId: Int? = null) {
        try {
            var users = BlackBoxCore.get().users.sortedBy { it.id }
            if (users.isEmpty()) {
                BlackBoxCore.get().createUser(0)
                users = BlackBoxCore.get().users.sortedBy { it.id }
            }
            val ids = users.map { it.id }
            val oldIds = fragmentList.map { it.userID }
            if (ids != oldIds) {
                fragmentList.clear()
                users.forEach { fragmentList.add(AppsFragment.newInstance(it.id)) }
                mViewPagerAdapter.replaceData(fragmentList)
            }
            val preferred = selectUserId ?: currentUser
            currentUser = ids.firstOrNull { it == preferred } ?: ids.first()
            ProfileAppearance.saveLastProfileId(currentUser)
            val index = fragmentList.indexOfFirst { it.userID == currentUser }.coerceAtLeast(0)
            viewBinding.viewPager.setCurrentItem(index, false)
            updateUserRemark(currentUser)
            refreshDashboard()
        } catch (e: Exception) {
            Log.e(TAG, "Could not synchronize profiles: " + e.message)
        }
    }

    fun scanUser() {
        syncProfiles()
    }

    fun showProfileOptions(userId: Int, anchor: View) {
        val dialog = BottomSheetDialog(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(10), dp(22), dp(24))
            background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@MainActivity, R.color.dashboard_background))
                cornerRadii = floatArrayOf(
                    dp(26).toFloat(), dp(26).toFloat(), dp(26).toFloat(), dp(26).toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
        }
        root.addView(View(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.argb(85, 130, 130, 140))
                cornerRadius = dp(3).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(dp(38), dp(4)).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(14)
            }
        })
        val heading = TextView(this).apply {
            text = ProfileAppearance.profileName(userId)
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.primary_text))
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(4), dp(4), dp(4), dp(14))
        }
        root.addView(heading)

        fun addAction(label: Int, destructive: Boolean = false, action: () -> Unit) {
            val row = TextView(this).apply {
                text = getString(label)
                textSize = 14f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), 0, dp(12), 0)
                setTextColor(if (destructive) Color.rgb(205, 92, 105) else ContextCompat.getColor(this@MainActivity, R.color.primary_text))
                background = android.graphics.drawable.RippleDrawable(
                    ColorStateList.valueOf(Color.argb(28, 130, 120, 150)),
                    GradientDrawable().apply {
                        setColor(Color.TRANSPARENT)
                        cornerRadius = dp(14).toFloat()
                    },
                    null
                )
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50))
                setOnClickListener {
                    dialog.dismiss()
                    action()
                }
            }
            root.addView(row)
        }

        addAction(R.string.rename_profile) { showRenameProfileDialog(userId) }
        addAction(R.string.change_profile_background) {
            pendingBackgroundUserId = userId
            profileBackgroundPicker.launch(arrayOf("image/*"))
        }
        if (ProfileAppearance.backgroundUri(userId) != null) {
            addAction(R.string.background_visibility) { showBackgroundOpacityDialog(userId) }
            addAction(R.string.remove_profile_background) { removeProfileBackground(userId) }
        }
        addAction(R.string.profile_accent) { showProfileAccentDialog(userId) }
        addAction(R.string.delete_profile, true) { confirmDeleteProfile(userId) }

        dialog.setContentView(root)
        dialog.show()
    }

    private fun showRenameProfileDialog(userId: Int) {
        MaterialDialog(this).show {
            title(text = getString(R.string.rename_profile))
            input(
                hintRes = R.string.userRemark,
                prefill = ProfileAppearance.profileName(userId)
            ) { _, input ->
                val name = input.toString().trim()
                if (name.isNotEmpty()) {
                    ProfileAppearance.setProfileName(userId, name)
                    updateUserRemark(currentUser)
                    refreshDashboard()
                    fragmentList.firstOrNull { it.userID == userId }?.refreshProfileHeader()
                }
            }
            positiveButton(res = R.string.done)
            negativeButton(res = R.string.cancel)
        }
    }

    private fun showProfileAccentDialog(userId: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.profile_accent)
            .setSingleChoiceItems(ProfileAppearance.ACCENT_NAMES, ProfileAppearance.accentIndex(userId)) { dialog, which ->
                ProfileAppearance.setAccent(userId, which)
                refreshDashboard()
                fragmentList.firstOrNull { it.userID == userId }?.refreshProfileHeader()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showBackgroundOpacityDialog(userId: Int) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(12), dp(24), dp(4))
        }
        val valueLabel = TextView(this).apply {
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.primary_text))
        }
        val slider = SeekBar(this).apply {
            max = 100
            progress = (ProfileAppearance.backgroundOpacity(userId) * 100).toInt()
        }
        valueLabel.text = getString(R.string.background_opacity_value, slider.progress)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                valueLabel.text = getString(R.string.background_opacity_value, progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        layout.addView(valueLabel)
        layout.addView(slider)
        AlertDialog.Builder(this)
            .setTitle(R.string.background_visibility)
            .setView(layout)
            .setPositiveButton(R.string.done) { _, _ ->
                ProfileAppearance.setBackgroundOpacity(userId, slider.progress)
                fragmentList.firstOrNull { it.userID == userId }?.refreshProfileHeader()
                refreshDashboard()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun removeProfileBackground(userId: Int) {
        ProfileAppearance.backgroundUri(userId)?.let { releaseWallpaperPermission(it) }
        ProfileAppearance.removeBackground(userId)
        fragmentList.firstOrNull { it.userID == userId }?.refreshProfileHeader()
        refreshDashboard()
    }

    private fun releaseWallpaperPermission(uriString: String) {
        try {
            contentResolver.releasePersistableUriPermission(
                Uri.parse(uriString),
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            Log.d(TAG, "Wallpaper permission was not persistable: " + e.message)
        }
    }

    private fun confirmDeleteProfile(userId: Int) {
        val users = BlackBoxCore.get().users
        if (users.size <= 1) {
            Toast.makeText(this, R.string.keep_one_profile, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_profile)
            .setMessage(getString(R.string.delete_profile_hint, ProfileAppearance.profileName(userId)))
            .setPositiveButton(R.string.delete_profile) { _, _ -> deleteProfile(userId) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteProfile(userId: Int) {
        try {
            val previousWallpaper = ProfileAppearance.backgroundUri(userId)
            BlackBoxCore.get().deleteUser(userId)
            if (previousWallpaper != null) releaseWallpaperPermission(previousWallpaper)
            ProfileAppearance.clearProfile(userId)
            val next = BlackBoxCore.get().users.sortedBy { it.id }.firstOrNull()?.id
            syncProfiles(next)
            Toast.makeText(this, R.string.profile_deleted, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Could not delete profile: " + e.message)
            Toast.makeText(this, getString(R.string.profile_delete_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUserRemark(userId: Int) {
        try {
            viewBinding.toolbarLayout.toolbar.title = getString(R.string.app_name)
            viewBinding.toolbarLayout.toolbar.subtitle = ProfileAppearance.profileName(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating current profile: " + e.message)
        }
    }

    private fun checkStoragePermission() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                if (!android.os.Environment.isExternalStorageManager()) {
                    Log.w(TAG, "MANAGE_EXTERNAL_STORAGE permission not granted")
                    showStoragePermissionDialog()
                }
            } else if (
                androidx.core.content.ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestLegacyStoragePermission()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking storage permission: " + e.message)
        }
    }

    private fun requestLegacyStoragePermission() {
        try {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting storage permission: " + e.message)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }
            ) {
                Log.d(TAG, "Storage permissions granted")
            } else {
                Log.w(TAG, "Storage permissions denied")
            }
        }
    }

    private fun showStoragePermissionDialog() {
        try {
            MaterialDialog(this).show {
                title(text = "Storage Permission Required")
                message(
                    text = "This app needs 'All Files Access' permission to properly run sandboxed apps. Without this permission, some apps may not work correctly.\n\nPlease grant permission in the next screen."
                )
                positiveButton(text = "Grant Permission") { openAllFilesAccessSettings() }
                negativeButton(text = "Later")
                cancelable(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing storage permission dialog: " + e.message)
        }
    }

    private fun openAllFilesAccessSettings() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                storagePermissionResult.launch(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening storage settings: " + e.message)
            try {
                storagePermissionResult.launch(
                    Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                )
            } catch (e2: Exception) {
                Log.e(TAG, "Error opening fallback storage settings: " + e2.message)
            }
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
                Log.e(TAG, "Error handling storage permission result: " + e.message)
            }
        }

    fun showFloatButton(show: Boolean) {
        try {
            val translation = Resolution.convertDpToPixel(120F, this)
            if (show) {
                viewBinding.fab.animate().translationY(0f).alpha(1f).setDuration(200L).start()
            } else {
                viewBinding.fab.animate().translationY(translation).alpha(0f).setDuration(200L).start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in showFloatButton: " + e.message)
        }
    }

    private val apkPathResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == RESULT_OK) {
                    result.data?.let { data ->
                        val userId = data.getIntExtra("userID", 0)
                        val source = data.getStringExtra("source")
                        if (source != null) {
                            fragmentList.firstOrNull { it.userID == userId }?.installApk(source)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling APK path result: " + e.message)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return try {
            menuInflater.inflate(R.menu.menu_main, menu)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating options menu: " + e.message)
            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            when (item.itemId) {
                R.id.main_git -> startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ALEX5402/NewBlackbox"))
                )
                R.id.main_setting -> SettingActivity.start(this)
                R.id.main_tg -> startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/newblackboxa"))
                )
                R.id.fake_location -> startActivity(
                    Intent(this, FakeManagerActivity::class.java).putExtra("userID", currentUser)
                )
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error handling menu item selection: " + e.message)
            false
        }
    }

    private fun showErrorDialog(message: String) {
        try {
            MaterialDialog(this).show {
                title(text = "Error")
                message(text = message)
                positiveButton(text = "OK") { finish() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error dialog: " + e.message)
            finish()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (viewBinding.drawerLayout.isDrawerOpen(Gravity.START)) {
            viewBinding.drawerLayout.closeDrawer(Gravity.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
