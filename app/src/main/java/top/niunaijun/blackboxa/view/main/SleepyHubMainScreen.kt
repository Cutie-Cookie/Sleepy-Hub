package top.niunaijun.blackboxa.view.main

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsets.Companion.safeDrawing
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
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val backgroundUri: String?,
    val backgroundOpacity: Float,
    val profileCount: Int,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SleepyHubMainScreen(
    profiles: List<ProfileItem>,
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
    var editingProfileId by rememberSaveable { mutableStateOf(-1) }
    var editingContainerId by rememberSaveable { mutableStateOf("") }
    var showContainerPicker by rememberSaveable { mutableStateOf(false) }
    var moveProfileId by rememberSaveable { mutableStateOf(-1) }
    var deleteProfileId by rememberSaveable { mutableStateOf(-1) }
    var folderProfileId by rememberSaveable { mutableStateOf(-1) }
    var folderDraft by rememberSaveable { mutableStateOf("") }

    val activeProfile = profiles.firstOrNull { it.userId == activeUserId }
    val activeContainer = containers.firstOrNull { it.id == activeContainerId }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(animationSpec = tween(300)) { width -> width / 18 }) togetherWith
                (fadeOut(animationSpec = tween(200)) +
                    slideOutHorizontally(animationSpec = tween(200)) { width -> -width / 24 })
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
            onBack = { page = "workspace" },
            onAdd = onAddProfile,
            onEdit = { userId -> editingProfileId = userId; page = "profile-settings" },
            onClone = onCloneProfile,
            onDelete = onDeleteProfile,
            onReorder = onReorderProfiles,
        )
        "profile-settings" -> {
            val profile = profiles.firstOrNull { it.userId == editingProfileId }
            if (profile == null) {
                page = "workspace"
            } else {
                ProfileDetailScreen(
                    profile = profile,
                    onBack = { page = "workspace" },
                    onOpenBasic = { page = "profile-basic" },
                    onOpenBackground = { page = "profile-background" },
                    onDelete = { onDeleteProfile(profile.userId); page = "workspace" },
                )
            }
        }
        "profile-basic" -> {
            val profile = profiles.firstOrNull { it.userId == editingProfileId }
            if (profile == null) {
                page = "workspace"
            } else {
                ProfileBasicScreen(
                    profile = profile,
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
                page = "workspace"
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
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var selectedFolder by rememberSaveable(activeContainerId) { mutableStateOf("All") }
            val folders = remember(profiles) {
                profiles.map { it.folder }.distinct().filter { it != "All" }.sorted()
            }
            val visibleProfiles = remember(profiles, selectedFolder) {
                if (selectedFolder == "All") profiles else profiles.filter { it.folder == selectedFolder }
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

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                DrawerQuickAction(
                                    icon = { Icon(Icons.Outlined.LocationOn, null) },
                                    label = "Fake location",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        onOpenFakeLocation()
                                    },
                                )
                                DrawerQuickAction(
                                    icon = { Icon(Icons.Outlined.Cloud, null) },
                                    label = "Google services",
                                    modifier = Modifier.weight(1f),
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

                            if (folders.isNotEmpty()) {
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    items(listOf("All") + folders) { folder ->
                                        androidx.compose.material3.FilterChip(
                                            selected = selectedFolder == folder,
                                            onClick = { selectedFolder = folder },
                                            label = { Text(folder) },
                                        )
                                    }
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                items(visibleProfiles, key = { it.userId }) { profile ->
                                    ProfileDrawerRow(
                                        profile = profile,
                                        selected = profile.userId == activeUserId,
                                        onSelect = {
                                            onSelectProfile(profile.userId)
                                            scope.launch { drawerState.close() }
                                        },
                                        onMove = { moveProfileId = profile.userId },
                                        onMoveToFolder = {
                                            folderProfileId = profile.userId
                                            folderDraft = profile.folder
                                        },
                                        onTogglePinned = { onToggleProfilePinned(profile.userId, !profile.pinned) },
                                        onRename = {
                                            editingProfileId = profile.userId
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
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
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
                                        if (activeProfile?.googlePlayEnabled == true) GooglePlayBadge(size = 16.dp)
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
        AlertDialog(
            onDismissRequest = { folderProfileId = -1 },
            title = { Text("Move to folder") },
            text = {
                OutlinedTextField(
                    value = folderDraft,
                    onValueChange = { folderDraft = it },
                    singleLine = true,
                    label = { Text("Folder name") },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetProfileFolder(folderProfileId, folderDraft)
                    folderProfileId = -1
                }, enabled = folderDraft.isNotBlank()) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { folderProfileId = -1 }) { Text("Cancel") }
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
            "container-basic", "container-background" -> "container-settings"
            "profile-basic", "profile-background" -> "profile-settings"
            "profile-settings", "profile-manager" -> "workspace"
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
                if (profile?.googlePlayEnabled == true) GooglePlayBadge(size = 15.dp)
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
    NavigationDrawerItem(
        label = { Text(label, maxLines = 1) },
        icon = icon,
        selected = false,
        onClick = onClick,
        modifier = modifier,
    )
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
) {
    var expanded by remember { mutableStateOf(false) }
    val rowColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        animationSpec = tween(220),
        label = "profile-row-selection",
    )
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onSelect,
                    onLongClick = { expanded = true },
                ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            color = rowColor,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileAvatar(profile.name, profile.avatarUri, 34.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            profile.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (profile.googlePlayEnabled) GooglePlayBadge(size = 14.dp)
                    }
                    if (profile.appCount >= 0) {
                        Text(
                            "${profile.appCount} apps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                if (profile.pinned) {
                    Icon(
                        Icons.Outlined.PushPin,
                        contentDescription = "Pinned profile",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            onClick = { onSelect(container.id) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            color = if (container.id == activeContainerId) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else Color.Transparent,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ContainerAvatar(container, 34.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(container.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                    Text(
                                        "${container.profileCount} profiles",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        shape = MaterialTheme.shapes.medium,
                        color = if (container.id == activeContainerId) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        modifier = Modifier.fillMaxWidth(),
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
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = CircleShape,
                label = { Text("Search containers") },
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
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
private fun rememberUriImage(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        value = if (uriString.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(uriString)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                var sample = 1
                while (bounds.outWidth / sample > 1800 || bounds.outHeight / sample > 1800) sample *= 2
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
    return image
}

@Composable
internal fun ContainerWorkspaceBackground(
    container: ContainerItem?,
    modifier: Modifier = Modifier,
) {
    val image = rememberUriImage(container?.backgroundUri)
    val alpha by animateFloatAsState(
        targetValue = if (image != null) container?.backgroundOpacity ?: 0f else 0f,
        label = "ContainerWorkspaceBackground",
    )
    Box(modifier = modifier.background(Color.Black)) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = alpha,
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)))
        }
    }
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
