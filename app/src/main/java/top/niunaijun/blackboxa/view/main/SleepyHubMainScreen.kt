package top.niunaijun.blackboxa.view.main

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileItem(
    val userId: Int,
    val name: String,
    val appCount: Int,
    val accent: Int,
    val backgroundUri: String?,
    val backgroundOpacity: Float,
)

@Composable
fun SleepyHubMainScreen(
    profiles: List<ProfileItem>,
    activeUserId: Int,
    viewPager: ViewPager2,
    onSelectProfile: (Int) -> Unit,
    onAddProfile: () -> Unit,
    onRenameProfile: (Int, String) -> Unit,
    onDeleteProfile: (Int) -> Unit,
    onInstallApps: () -> Unit,
    onOpenFakeLocation: () -> Unit,
    onRefreshApps: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf("workspace") }
    var editingProfileId by rememberSaveable { mutableStateOf(-1) }
    var showProfilePicker by rememberSaveable { mutableStateOf(false) }
    val activeProfile = profiles.firstOrNull { it.userId == activeUserId } ?: profiles.firstOrNull()

    when (page) {
        "profiles" -> ProfileManagerScreen(
            profiles = profiles,
            activeUserId = activeUserId,
            onBack = { page = "workspace" },
            onSelect = {
                onSelectProfile(it)
                page = "workspace"
            },
            onAdd = onAddProfile,
            onEdit = {
                editingProfileId = it
                page = "profile-settings"
            },
        )
        "settings" -> GlobalSettingsScreen(onBack = { page = "workspace" })
        "profile-settings" -> {
            val profile = profiles.firstOrNull { it.userId == editingProfileId }
            if (profile == null) {
                page = "profiles"
            } else {
                ProfileSettingsScreen(
                    profile = profile,
                    profileCount = profiles.size,
                    onBack = { page = "profiles" },
                    onRename = { name ->
                        onRenameProfile(profile.userId, name)
                    },
                    onDelete = {
                        onDeleteProfile(profile.userId)
                        page = "profiles"
                    },
                    onAppearanceChanged = {
                        // Activity state is refreshed through its profile list callback.
                    },
                )
            }
        }
        else -> {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            DrawerHeader()

                            DrawerEntry(
                                icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                                label = "Installed apps",
                                onClick = { scope.launch { drawerState.close() } },
                            )
                            DrawerEntry(
                                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                                label = "Install app",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onInstallApps()
                                },
                            )
                            DrawerEntry(
                                icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                                label = "Fake location",
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    onOpenFakeLocation()
                                },
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                NavigationDrawerItem(
                                    label = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = activeProfile?.name ?: "Profiles",
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f),
                                            )
                                            if (activeProfile != null) {
                                                ProfileAvatar(
                                                    name = activeProfile.name,
                                                    accent = activeProfile.accent,
                                                    size = 30.dp,
                                                )
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                                    selected = false,
                                    onClick = { showProfilePicker = true },
                                    modifier = Modifier.weight(1f),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = Color.Transparent,
                                    ),
                                )
                                IconButton(
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        onAddProfile()
                                    },
                                ) {
                                    Icon(Icons.Outlined.PersonAdd, contentDescription = "Add profile")
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                DrawerShortcut(
                                    icon = { Icon(Icons.Outlined.Apps, contentDescription = null) },
                                    label = "Profiles",
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        page = "profiles"
                                    },
                                )
                                DrawerShortcut(
                                    icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                                    label = "Settings",
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        page = "settings"
                                    },
                                )
                                DrawerShortcut(
                                    icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                                    label = "Location",
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        onOpenFakeLocation()
                                    },
                                )
                            }
                        }
                    }
                },
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                            ),
                            navigationIcon = {
                                Surface(
                                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                                ) {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                    ) {
                                        Icon(Icons.Outlined.Menu, contentDescription = "Open dashboard")
                                    }
                                }
                            },
                            title = {
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        text = activeProfile?.name ?: "Sleepy Hub",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = when {
                                            activeProfile == null -> "Your little app spaces"
                                            activeProfile.appCount >= 0 -> "${activeProfile.appCount} installed apps"
                                            else -> "Virtual profile"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = onInstallApps) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Install app")
                                }
                                var menuExpanded by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Profile settings") },
                                            leadingIcon = { Icon(Icons.Outlined.Settings, null) },
                                            onClick = {
                                                menuExpanded = false
                                                editingProfileId = activeUserId
                                                page = "profile-settings"
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Manage profiles") },
                                            leadingIcon = { Icon(Icons.Outlined.Apps, null) },
                                            onClick = {
                                                menuExpanded = false
                                                page = "profiles"
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Refresh apps") },
                                            leadingIcon = { Icon(Icons.Outlined.Refresh, null) },
                                            onClick = {
                                                menuExpanded = false
                                                onRefreshApps()
                                            },
                                        )
                                    }
                                }
                            },
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = onInstallApps,
                            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                            text = { Text("Install app") },
                        )
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        if (activeProfile != null) {
                            ProfileWorkspaceBackground(
                                profile = activeProfile,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        AndroidView(
                            factory = { viewPager },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            if (showProfilePicker) {
                ProfilePickerSheet(
                    profiles = profiles,
                    activeUserId = activeUserId,
                    onDismiss = { showProfilePicker = false },
                    onSelect = {
                        showProfilePicker = false
                        scope.launch { drawerState.close() }
                        onSelectProfile(it)
                    },
                    onEdit = {
                        showProfilePicker = false
                        scope.launch { drawerState.close() }
                        editingProfileId = it
                        page = "profile-settings"
                    },
                    onAdd = {
                        showProfilePicker = false
                        scope.launch { drawerState.close() }
                        onAddProfile()
                    },
                )
            }
        }
    }

    BackHandler(enabled = page != "workspace") {
        page = if (page == "profile-settings") "profiles" else "workspace"
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "S",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Sleepy Hub", style = MaterialTheme.typography.titleMedium)
            Text(
                "Your little app spaces",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DrawerEntry(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                icon()
            }
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DrawerShortcut(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ProfileAvatar(name: String, accent: Int, size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = Color(accent),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = (size.value * 0.38f).sp,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun ProfilePickerSheet(
    profiles: List<ProfileItem>,
    activeUserId: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onAdd: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Profiles",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(profiles, key = { it.userId }) { profile ->
                    val selected = profile.userId == activeUserId
                    Card(
                        onClick = { onSelect(profile.userId) },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ProfileAvatar(profile.name, profile.accent, 36.dp)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    profile.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                )
                                Text(
                                    if (profile.appCount >= 0) "${profile.appCount} installed apps" else "Virtual profile",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { onEdit(profile.userId) }) {
                                Icon(Icons.Outlined.Edit, contentDescription = "Edit profile")
                            }
                        }
                    }
                }
            }
            FilledTonalButton(
                onClick = onAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
            ) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add profile")
            }
        }
    }
}

@Composable
private fun ProfileWorkspaceBackground(
    profile: ProfileItem,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val image by produceState<ImageBitmap?>(initialValue = null, key1 = profile.backgroundUri) {
        val uriString = profile.backgroundUri
        value = if (uriString.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(uriString)
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, bounds)
                    }
                    var sample = 1
                    while (bounds.outWidth / sample > 1800 || bounds.outHeight / sample > 1800) {
                        sample *= 2
                    }
                    val options = BitmapFactory.Options().apply { inSampleSize = sample }
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)?.asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
        }
    }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    Color(profile.accent).copy(alpha = 0.14f),
                    MaterialTheme.colorScheme.background,
                )
            )
        ),
    ) {
        if (image != null) {
            Image(
                bitmap = image!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = profile.backgroundOpacity,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.38f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.76f),
                            )
                        )
                    ),
            )
        }
    }
}
