package top.niunaijun.blackboxa.view.main

import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import android.os.Build
import android.graphics.Color as AndroidColor
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Check

import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.launch
import top.niunaijun.blackboxa.app.ProfileAppearance

data class ProfileItem(
    val userId: Int,
    val name: String,
    val folder: String,
    val appCount: Int,
    val pinned: Boolean,
    val avatarUri: String? = null,
    val backgroundUri: String? = null,
    val backgroundOpacity: Float = 0.32f,
    val googlePlayEnabled: Boolean = false,
)

data class ContainerItem(
    val id: String,
    val name: String,
    val avatarUri: String?,
    val profileCount: Int,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepyHubMainScreen(
    profiles: List<ProfileItem>,
    folderNames: List<String>,
    containers: List<ContainerItem>,
    activeUserId: Int,
    activeContainerId: String,
    isWorkspaceScrolled: Boolean,
    viewPager: ViewPager2,
    storageRationaleVisible: Boolean,
    onDismissStorageRationale: () -> Unit,
    onGrantStoragePermission: () -> Unit,
    onSelectProfile: (Int) -> Unit,
    onAddProfile: () -> Unit,
    onRenameProfile: (Int, String) -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onMoveProfile: (Int, String) -> Unit,
    onSetProfileFolder: (Int, String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onToggleProfilePinned: (Int, Boolean) -> Unit,
    onCloneProfile: (Int) -> Unit,
    onReorderProfiles: (List<Int>) -> Unit,
    onUpdateProfileAvatar: (Int, String?) -> Unit,
    onUpdateProfileBackground: (Int, String?, Float) -> Unit,
    onCreateContainer: (String) -> Unit,
    onSelectContainer: (String) -> Unit,
    onUpdateContainer: (ContainerItem) -> Unit,
    onDeleteContainer: (String) -> Unit,
    onInstallApps: () -> Unit,
    onOpenFakeLocation: () -> Unit,
    onOpenGmsManager: () -> Unit,
    onOpenStorageAccess: () -> Unit,
    onRefreshApps: () -> Unit,
    onRefreshProfiles: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf("workspace") }
    var returnToPage by rememberSaveable { mutableStateOf("workspace") }
    var containerSettingsReturnPage by rememberSaveable { mutableStateOf("workspace") }
    var profileSettingsReturnPage by rememberSaveable { mutableStateOf("workspace") }
    var editingProfileId by rememberSaveable { mutableStateOf(-1) }
    var editingContainerId by rememberSaveable { mutableStateOf("") }
    var showContainerPicker by rememberSaveable { mutableStateOf(false) }
    var moveProfileId by rememberSaveable { mutableStateOf(-1) }
    var deleteProfileId by rememberSaveable { mutableStateOf(-1) }
    var folderProfileId by rememberSaveable { mutableStateOf(-1) }
    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }
    var folderToRename by rememberSaveable { mutableStateOf<String?>(null) }
    var folderToDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var folderNameDraft by rememberSaveable { mutableStateOf("") }
    var selectedFolder by rememberSaveable(activeContainerId) { mutableStateOf("All") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val folderSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val activity = context as? Activity
    val systemBarView = LocalView.current
    val surfaceIsLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val surfaceBarColor = MaterialTheme.colorScheme.surface.toArgb()

    SideEffect {
        if (activity != null && !systemBarView.isInEditMode) {
            activity.window.navigationBarColor = AndroidColor.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.window.navigationBarDividerColor = AndroidColor.TRANSPARENT
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                activity.window.isNavigationBarContrastEnforced = false
            }
            val controller = WindowCompat.getInsetsController(activity.window, systemBarView)
            val isWorkspace = page == "workspace"
            controller.isAppearanceLightNavigationBars = !isWorkspace && surfaceIsLight
            if (isWorkspace) {
                activity.window.statusBarColor = AndroidColor.TRANSPARENT
                controller.isAppearanceLightStatusBars = false
            } else {
                activity.window.statusBarColor = surfaceBarColor
                controller.isAppearanceLightStatusBars = surfaceIsLight
            }
        }
    }

    LaunchedEffect(page) {
        if (page != "workspace" && drawerState.isOpen) drawerState.close()
    }

    val activeProfile = profiles.firstOrNull { it.userId == activeUserId }
    val activeContainer = containers.firstOrNull { it.id == activeContainerId }

    AnimatedContent(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        targetState = page,
        transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith
                fadeOut(animationSpec = tween(140))
        },
        label = "sleepy-hub-page-transition",
    ) { currentPage ->
        when (currentPage) {
        "container-manager" -> ContainerManagerScreen(
            containers = containers,
            activeContainerId = activeContainerId,
            onBack = { page = returnToPage },
            onAdd = onCreateContainer,
            onSelect = {
                onSelectContainer(it)
                page = "workspace"
            },
            onEdit = {
                editingContainerId = it
                containerSettingsReturnPage = "container-manager"
                page = "container-settings"
            },
            onDelete = onDeleteContainer,
        )
        "container-settings" -> {
            val container = containers.firstOrNull { it.id == editingContainerId }
            if (container == null) {
                page = "container-manager"
            } else {
                ContainerSettingsScreen(
                    container = container,
                    onBack = { page = containerSettingsReturnPage },
                    onOpenBasic = { page = "container-basic" },
                )
            }
        }
        "container-basic" -> {
            val container = containers.firstOrNull { it.id == editingContainerId }
            if (container == null) {
                page = "container-manager"
            } else {
                ContainerBasicScreen(
                    container = container,
                    onBack = { page = "container-settings" },
                    onSave = {
                        onUpdateContainer(it)
                        page = "container-settings"
                    },
                )
            }
        }
        "profile-manager" -> ProfileManagerScreen(
            profiles = profiles,
            folders = folderNames,
            onBack = { page = "workspace" },
            onAdd = onAddProfile,
            onEdit = {
                editingProfileId = it
                profileSettingsReturnPage = "profile-manager"
                page = "profile-settings"
            },
            onClone = onCloneProfile,
            onDelete = onDeleteProfile,
            onReorder = onReorderProfiles,
            onCreateFolder = onCreateFolder,
            onRenameFolder = onRenameFolder,
            onDeleteFolder = onDeleteFolder,
        )
        "profile-settings" -> {
            val profile = profiles.firstOrNull { it.userId == editingProfileId }
            if (profile == null) {
                page = profileSettingsReturnPage
            } else {
                ProfileDetailScreen(
                    profile = profile,
                    onBack = { page = profileSettingsReturnPage },
                    onOpenBasic = { page = "profile-basic" },
                    onOpenBackground = { page = "profile-background" },
                    onDelete = { onDeleteProfile(profile.userId); page = profileSettingsReturnPage },
                )
            }
        }
        "profile-basic" -> {
            val profile = profiles.firstOrNull { it.userId == editingProfileId }
            if (profile == null) {
                page = profileSettingsReturnPage
            } else {
                ProfileBasicScreen(
                    profile = profile,
                    folders = folderNames,
                    onBack = { page = "profile-settings" },
                    onSave = { name, folder, avatarUri ->
                        onRenameProfile(profile.userId, name)
                        onSetProfileFolder(profile.userId, folder)
                        onUpdateProfileAvatar(profile.userId, avatarUri)
                        onRefreshProfiles()
                        page = "profile-settings"
                    },
                )
            }
        }
        "profile-background" -> {
            val profile = profiles.firstOrNull { it.userId == editingProfileId }
            if (profile == null) {
                page = profileSettingsReturnPage
            } else {
                ProfileBackgroundScreen(
                    profile = profile,
                    onBack = { page = "profile-settings" },
                    onSave = { uri, opacity ->
                        onUpdateProfileBackground(profile.userId, uri, opacity)
                        onRefreshProfiles()
                        page = "profile-settings"
                    },
                )
            }
        }
        "settings" -> GlobalSettingsScreen(
            onBack = { page = "workspace" },
            onOpenPreferences = { page = "preferences" },
            onOpenContainers = {
                returnToPage = "settings"
                page = "container-manager"
            },
            onOpenFakeLocation = onOpenFakeLocation,
            onOpenGmsManager = onOpenGmsManager,
            onOpenStorageAccess = onOpenStorageAccess,
            onGridColumnsChanged = onRefreshApps,
        )
        "preferences" -> SettingsPreferencesScreen(
            onBack = { page = "settings" },
            onOpenTheme = { page = "theme" },
            onGridColumnsChanged = onRefreshApps,
        )
        "theme" -> ThemeSettingsScreen(onBack = { page = "preferences" })
        else -> {
            val scope = rememberCoroutineScope()
            var profileSortMode by rememberSaveable(activeContainerId) { mutableStateOf("custom") }
            var sortMenuExpanded by rememberSaveable(activeContainerId) { mutableStateOf(false) }
            val folders = folderNames
            val visibleProfiles = remember(profiles, selectedFolder, profileSortMode) {
                val filtered = if (selectedFolder == "All") profiles else profiles.filter { it.folder == selectedFolder }
                when (profileSortMode) {
                    "name-asc" -> filtered.sortedBy { it.name.lowercase() }
                    "name-desc" -> filtered.sortedByDescending { it.name.lowercase() }
                    else -> filtered
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(300.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                        ) {
                            DrawerHeader(profile = activeProfile)

                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                DrawerQuickAction(
                                    icon = { Icon(Icons.Outlined.LocationOn, null) },
                                    label = "Fake location",
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        onOpenFakeLocation()
                                    },
                                )
                                DrawerQuickAction(
                                    icon = { Icon(Icons.Outlined.Cloud, null) },
                                    label = "Google services",
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        onOpenGmsManager()
                                    },
                                )
                            }

                            Spacer(Modifier.size(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "Profiles",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                Box {
                                    IconButton(onClick = { sortMenuExpanded = true }) {
                                        Icon(Icons.Outlined.Sort, contentDescription = "Sort profiles")
                                    }
                                    DropdownMenu(
                                        expanded = sortMenuExpanded,
                                        onDismissRequest = { sortMenuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Custom order") },
                                            onClick = { profileSortMode = "custom"; sortMenuExpanded = false },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Name (A–Z)") },
                                            onClick = { profileSortMode = "name-asc"; sortMenuExpanded = false },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Name (Z–A)") },
                                            onClick = { profileSortMode = "name-desc"; sortMenuExpanded = false },
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        page = "profile-manager"
                                    },
                                ) {
                                    Icon(Icons.Outlined.Tune, contentDescription = "Manage profiles")
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        onAddProfile()
                                    },
                                ) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Add profile")
                                }
                            }

                            LazyRow(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                item {
                                    ProfileFolderChip(
                                        label = "All",
                                        selected = selectedFolder == "All",
                                        onClick = { selectedFolder = "All" },
                                        onLongClick = {},
                                    )
                                }
                                items(folders, key = { it }) { folder ->
                                    if (folder.equals("General", ignoreCase = true)) {
                                        ProfileFolderChip(
                                            label = folder,
                                            icon = Icons.Outlined.Folder,
                                            selected = selectedFolder == folder,
                                            onClick = { selectedFolder = folder },
                                            onLongClick = {},
                                        )
                                    } else {
                                        var menuExpanded by remember(folder) { mutableStateOf(false) }
                                        Box {
                                            ProfileFolderChip(
                                                label = folder,
                                                icon = Icons.Outlined.Folder,
                                                selected = selectedFolder == folder,
                                                onClick = { selectedFolder = folder },
                                                onLongClick = { menuExpanded = true },
                                            )
                                            DropdownMenu(
                                                expanded = menuExpanded,
                                                onDismissRequest = { menuExpanded = false },
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Rename") },
                                                    leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                                                    onClick = {
                                                        folderToRename = folder
                                                        folderNameDraft = folder
                                                        menuExpanded = false
                                                    },
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Delete") },
                                                    leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                                                    onClick = {
                                                        folderToDelete = folder
                                                        menuExpanded = false
                                                    },
                                                )
                                            }
                                        }
                                    }
                                }
                                item {
                                    ProfileFolderChip(
                                        label = "Add",
                                        icon = Icons.Outlined.Add,
                                        selected = false,
                                        onClick = {
                                            folderNameDraft = ""
                                            showCreateFolderDialog = true
                                        },
                                        onLongClick = {},
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                items(visibleProfiles, key = { it.userId }) { profile ->
                                    ProfileDrawerRow(
                                        profile = profile,
                                        modifier = Modifier.animateItem(),
                                        selected = profile.userId == activeUserId,
                                        onSelect = {
                                            onSelectProfile(profile.userId)
                                            scope.launch { drawerState.close() }
                                        },
                                        onMove = { moveProfileId = profile.userId },
                                        onMoveToFolder = {
                                            folderProfileId = profile.userId
                                        },
                                        onTogglePinned = { onToggleProfilePinned(profile.userId, !profile.pinned) },
                                        onRename = {
                                            editingProfileId = profile.userId
                                            profileSettingsReturnPage = "workspace"
                                            scope.launch { drawerState.close() }
                                            page = "profile-settings"
                                        },
                                        onClone = { onCloneProfile(profile.userId) },
                                        onDelete = { deleteProfileId = profile.userId },
                                    )
                                }
                                if (visibleProfiles.isEmpty()) {
                                    item {
                                        Text(
                                            "No profiles in this folder",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    onClick = { showContainerPicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.large,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        ContainerAvatar(activeContainer, 34.dp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                activeContainer?.name ?: "Container",
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                "Switch container",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Icon(Icons.Outlined.ArrowForward, contentDescription = "Choose container")
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        returnToPage = "workspace"
                                        page = "container-manager"
                                    },
                                ) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Manage containers")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                IconButton(onClick = {
                                    scope.launch { drawerState.close() }
                                    onRefreshApps()
                                }) {
                                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh apps")
                                }
                                TextButton(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        page = "settings"
                                    },
                                ) {
                                    Icon(Icons.Outlined.Settings, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Settings")
                                }
                            }
                        }
                    }
                },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                ) {
                    ProfileWorkspaceBackground(
                        profile = activeProfile,
                        modifier = Modifier.fillMaxSize(),
                    )
                    val topBarColor by animateColorAsState(
                        targetValue = if (isWorkspaceScrolled) Color.Black.copy(alpha = 0.76f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 320),
                        label = "workspace-topbar-color",
                    )
                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            activeProfile?.name ?: "Sleepy Hub",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (activeProfile?.googlePlayEnabled == true) GooglePlayBadge(userId = activeProfile.userId, size = 16.dp)
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Outlined.Menu, contentDescription = "Open dashboard", tint = Color.White)
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = onInstallApps,
                                        enabled = activeUserId >= 0,
                                    ) {
                                        Icon(Icons.Outlined.Add, contentDescription = "Add app", tint = Color.White)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = topBarColor,
                                    scrolledContainerColor = topBarColor,
                                    titleContentColor = Color.White,
                                    navigationIconContentColor = Color.White,
                                    actionIconContentColor = Color.White,
                                ),
                            )
                        },
                    ) { innerPadding ->
                        if (activeUserId >= 0) {
                            AndroidView(
                                factory = { viewPager },
                                modifier = Modifier.fillMaxSize().padding(innerPadding),
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(innerPadding),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "This container is empty",
                                    color = Color.White.copy(alpha = 0.72f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            if (showContainerPicker) {
                ContainerPickerSheet(
                    containers = containers,
                    activeContainerId = activeContainerId,
                    onDismiss = { showContainerPicker = false },
                    onSelect = {
                        showContainerPicker = false
                        onSelectContainer(it)
                    },
                    onEdit = {
                        showContainerPicker = false
                        editingContainerId = it
                        containerSettingsReturnPage = "workspace"
                        page = "container-settings"
                    },
                    onManage = {
                        showContainerPicker = false
                        returnToPage = "workspace"
                        page = "container-manager"
                    },
                )
            }
        }
        }
    }

    if (moveProfileId >= 0) {
        MoveProfileSheet(
            containers = containers,
            activeContainerId = activeContainerId,
            onDismiss = { moveProfileId = -1 },
            onMove = { targetId ->
                onMoveProfile(moveProfileId, targetId)
                moveProfileId = -1
            },
        )
    }

    if (deleteProfileId >= 0) {
        val target = profiles.firstOrNull { it.userId == deleteProfileId }
        AlertDialog(
            onDismissRequest = { deleteProfileId = -1 },
            title = { Text("Delete ${target?.name ?: "profile"}?") },
            text = { Text("This permanently deletes this virtual profile and its installed apps and data.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProfile(deleteProfileId)
                    deleteProfileId = -1
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteProfileId = -1 }) { Text("Cancel") }
            },
        )
    }

    if (folderProfileId >= 0) {
        val movingProfile = profiles.firstOrNull { it.userId == folderProfileId }
        ModalBottomSheet(
            onDismissRequest = { folderProfileId = -1 },
            sheetState = folderSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Move to folder", style = MaterialTheme.typography.titleLarge)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(folderNames, key = { it }) { folder ->
                        val isCurrent = movingProfile?.folder == folder
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetProfileFolder(folderProfileId, folder)
                                    folderProfileId = -1
                                },
                            shape = MaterialTheme.shapes.medium,
                            color = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.surface,
                            tonalElevation = if (isCurrent) 2.dp else 0.dp,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(Icons.Outlined.Folder, contentDescription = null)
                                Text(
                                    folder,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (isCurrent) {
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = "Current folder",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create folder") },
            text = {
                OutlinedTextField(
                    value = folderNameDraft,
                    onValueChange = { folderNameDraft = it },
                    singleLine = true,
                    label = { Text("Folder name") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateFolder(folderNameDraft)
                        showCreateFolderDialog = false
                    },
                    enabled = folderNameDraft.isNotBlank(),
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            },
        )
    }

    folderToRename?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToRename = null },
            title = { Text("Rename folder") },
            text = {
                OutlinedTextField(
                    value = folderNameDraft,
                    onValueChange = { folderNameDraft = it },
                    singleLine = true,
                    label = { Text("Folder name") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newName = folderNameDraft.trim()
                        if (selectedFolder == folder) selectedFolder = "All"
                        onRenameFolder(folder, newName)
                        folderToRename = null
                    },
                    enabled = folderNameDraft.isNotBlank(),
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { folderToRename = null }) { Text("Cancel") }
            },
        )
    }

    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Delete $folder?") },
            text = {
                Text("Profiles in this folder will be moved to General. Their installed apps and data will stay intact.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedFolder == folder) selectedFolder = "All"
                        onDeleteFolder(folder)
                        folderToDelete = null
                    },
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) { Text("Cancel") }
            },
        )
    }

    if (storageRationaleVisible) {
        StorageRationaleDialog(
            onDismiss = onDismissStorageRationale,
            onGrant = onGrantStoragePermission,
        )
    }

    BackHandler(enabled = page != "workspace") {
        page = when (page) {
            "container-manager" -> returnToPage
            "container-settings" -> containerSettingsReturnPage
            "container-basic" -> "container-settings"
            "profile-basic", "profile-background" -> "profile-settings"
            "profile-settings" -> profileSettingsReturnPage
            "profile-manager" -> "workspace"
            "preferences" -> "settings"
            "theme" -> "preferences"
            else -> "workspace"
        }
    }
}

@Composable
private fun DrawerHeader(profile: ProfileItem?) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Hope your day is going well"
        else -> "It's late, take care"
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ProfileAvatar(
            name = profile?.name ?: "Sleepy Hub",
            uri = profile?.avatarUri,
            size = 50.dp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    profile?.name ?: "Sleepy Hub",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (profile?.googlePlayEnabled == true) GooglePlayBadge(userId = profile.userId, size = 15.dp)
            }
            Text(
                if (profile == null) "Choose a profile below" else greeting,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
@Composable
private fun DrawerQuickAction(
    icon: @Composable () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileDrawerRow(
    profile: ProfileItem,
    selected: Boolean,
    onSelect: () -> Unit,
    onMove: () -> Unit,
    onMoveToFolder: () -> Unit,
    onTogglePinned: () -> Unit,
    onRename: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val rowColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        } else {
            Color.Transparent
        },
        animationSpec = tween(280),
        label = "profile-row-selection",
    )
    Box {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onSelect,
                    onLongClick = { expanded = true },
                ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            color = rowColor,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (profile.googlePlayEnabled) GooglePlayBadge(userId = profile.userId, size = 14.dp)
                if (profile.pinned) {
                    Icon(
                        Icons.Outlined.PushPin,
                        contentDescription = "Pinned profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Move to container") }, onClick = { expanded = false; onMove() })
            DropdownMenuItem(text = { Text("Move to folder") }, onClick = { expanded = false; onMoveToFolder() })
            DropdownMenuItem(text = { Text(if (profile.pinned) "Unpin" else "Pin") }, onClick = { expanded = false; onTogglePinned() })
            DropdownMenuItem(text = { Text("Edit profile") }, onClick = { expanded = false; onRename() })
            DropdownMenuItem(text = { Text("Clone profile") }, onClick = { expanded = false; onClone() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { expanded = false; onDelete() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContainerPickerSheet(
    containers: List<ContainerItem>,
    activeContainerId: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    onEdit: (String) -> Unit,
    onManage: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Containers", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onManage) { Text("Manage") }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(containers, key = { it.id }) { container ->
                    Row(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = { onSelect(container.id) },
                            modifier = Modifier.weight(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            color = if (container.id == activeContainerId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else MaterialTheme.colorScheme.surfaceContainerLow,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ContainerAvatar(container, 34.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        container.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (container.id == activeContainerId) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                    )
                                    Text(
                                        "${container.profileCount} profiles",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (container.id == activeContainerId) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { onEdit(container.id) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit container")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveProfileSheet(
    containers: List<ContainerItem>,
    activeContainerId: String,
    onDismiss: () -> Unit,
    onMove: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.72f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Move profile to container", style = MaterialTheme.typography.titleLarge)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(containers, key = { it.id }) { container ->
                    Surface(
                        onClick = { if (container.id != activeContainerId) onMove(container.id) },
                        enabled = container.id != activeContainerId,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        color = if (container.id == activeContainerId) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        modifier = Modifier.fillMaxWidth().animateItem(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ContainerAvatar(container, 36.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(container.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (container.id == activeContainerId) "Current container" else "${container.profileCount} profiles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContainerManagerScreen(
    containers: List<ContainerItem>,
    activeContainerId: String,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onSelect: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<ContainerItem?>(null) }
    val filtered = remember(containers, query) {
        containers.filter { it.name.contains(query, ignoreCase = true) }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Containers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { newName = ""; showCreate = true }) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add container")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RikkaSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search containers",
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { container ->
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        onClick = { onSelect(container.id) },
                        modifier = Modifier.animateItem(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = if (container.id == activeContainerId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, top = 10.dp, end = 4.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ContainerAvatar(container, 48.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(container.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                Text(
                                    "${container.profileCount} profiles",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Box {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Outlined.MoreHoriz, contentDescription = "Container actions")
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = { expanded = false; onEdit(container.id) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = { expanded = false; deleteTarget = container },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreate) {
        ModalBottomSheet(
            onDismissRequest = { showCreate = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("New container", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    label = { Text("Name") },
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { showCreate = false }) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            onAdd(newName)
                            showCreate = false
                        },
                        enabled = newName.isNotBlank(),
                    ) { Text("Create") }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete ${target.name}?") },
            text = {
                Text(
                    "This permanently deletes this container, all ${target.profileCount} profiles inside it, " +
                        "and every app and all app data belonging to those profiles. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(target.id)
                    deleteTarget = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel") } },
        )
    }
}

@Composable
internal fun ContainerAvatar(container: ContainerItem?, size: androidx.compose.ui.unit.Dp) {
    ProfileAvatar(
        name = container?.name ?: "Container",
        uri = container?.avatarUri,
        size = size,
    )
}

@Composable
private fun StorageRationaleDialog(
    onDismiss: () -> Unit,
    onGrant: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text("Storage access", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Sleepy Hub needs All Files Access so BlackBox can create and manage the files used by virtual apps. Android will open its permission settings next.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) { Text("Later") }
                    TextButton(onClick = onGrant) { Text("Grant permission") }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileFolderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    icon: ImageVector? = null,
) {
    Surface(
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        },
        modifier = Modifier
            .clip(CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
