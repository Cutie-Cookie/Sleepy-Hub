package top.niunaijun.blackboxa.view.main

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import top.niunaijun.blackboxa.app.AppManager
import top.niunaijun.blackboxa.app.ProfileAppearance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerSettingsScreen(
    container: ContainerItem,
    onBack: () -> Unit,
    onOpenBasic: () -> Unit,
    onOpenBackground: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Container details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        container.name,
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        "${container.profileCount} profiles",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                SettingsCard(title = "Basic information") {
                    SettingsActionRow(
                        icon = { ContainerAvatar(container, 48.dp) },
                        title = "Name and picture",
                        subtitle = "Change this container's name and avatar",
                        onClick = onOpenBasic,
                    )
                }
            }
            item {
                SettingsCard(title = "Workspace") {
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Image, null) },
                        title = "Background",
                        subtitle = if (container.backgroundUri == null) {
                            "No background selected"
                        } else {
                            "Change or remove the workspace background"
                        },
                        onClick = onOpenBackground,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerBasicScreen(
    container: ContainerItem,
    onBack: () -> Unit,
    onSave: (ContainerItem) -> Unit,
) {
    val context = LocalContext.current
    var name by remember(container.id) { mutableStateOf(container.name) }
    var avatarUri by remember(container.id) { mutableStateOf(container.avatarUri) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            persistReadPermission(context, uri)
            avatarUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basic information") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "Container identity") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ContainerAvatar(container.copy(name = name, avatarUri = avatarUri), 64.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name.ifBlank { "Container" }, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            Text(
                                "Container picture",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row {
                                TextButton(onClick = { avatarPicker.launch(arrayOf("image/*")) }) {
                                    Text(if (avatarUri == null) "Choose picture" else "Change picture")
                                }
                                if (avatarUri != null) {
                                    TextButton(onClick = { avatarUri = null }) { Text("Remove") }
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Container name") },
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(container.copy(
                            name = name.trim().ifBlank { container.name },
                            avatarUri = avatarUri,
                        ))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank(),
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerBackgroundScreen(
    container: ContainerItem,
    onBack: () -> Unit,
    onSave: (ContainerItem) -> Unit,
) {
    val context = LocalContext.current
    var backgroundUri by remember(container.id) { mutableStateOf(container.backgroundUri) }
    var opacity by remember(container.id) { mutableStateOf(container.backgroundOpacity) }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            persistReadPermission(context, uri)
            backgroundUri = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspace background") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "Preview") {
                    Text(
                        "The background appears only behind the app workspace for this container. It is not shown in the dashboard list or container picker.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ContainerWorkspaceBackground(
                        container.copy(backgroundUri = backgroundUri, backgroundOpacity = opacity),
                        modifier = Modifier.fillMaxWidth().height(170.dp),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { backgroundPicker.launch(arrayOf("image/*")) }) {
                            Text(if (backgroundUri == null) "Choose image" else "Change image")
                        }
                        if (backgroundUri != null) {
                            TextButton(onClick = { backgroundUri = null }) { Text("Remove") }
                        }
                    }
                    if (backgroundUri != null) {
                        Text("Visibility · ${(opacity * 100).toInt()}%")
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0f..1f,
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSave(container.copy(
                            backgroundUri = backgroundUri,
                            backgroundOpacity = opacity.coerceIn(0f, 1f),
                        ))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Save background")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    profile: ProfileItem,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember(profile.userId) { mutableStateOf(profile.name) }
    var folder by remember(profile.userId) { mutableStateOf(profile.folder) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showTitle by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedVisibility(
                        visible = showTitle,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 },
                    ) { Text(name.ifBlank { "Profile" }, maxLines = 1) }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "Profile") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Profile name") },
                    )
                    OutlinedTextField(
                        value = folder,
                        onValueChange = { folder = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Folder") },
                        supportingText = { Text("Profiles in the same container can share a folder.") },
                    )
                    Button(
                        onClick = {
                            onRename(name.trim())
                            ProfileAppearance.setProfileFolder(profile.userId, folder.trim().ifBlank { "General" })
                            onSaved()
                            Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show()
                        },
                        enabled = name.isNotBlank() &&
                            (name.trim() != profile.name || folder.trim() != profile.folder),
                    ) { Text("Save profile") }
                }
            }
            item {
                SettingsCard(title = "Virtual profile") {
                    Text(
                        "This profile is a BlackBox virtual Android user. Moving it to another container keeps its installed apps and data.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Delete profile")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete ${profile.name}?") },
            text = { Text("This permanently removes this virtual profile, its installed apps, and all of its app data.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) { Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    onBack: () -> Unit,
    onOpenPreferences: () -> Unit,
    onOpenContainers: () -> Unit,
    onOpenFakeLocation: () -> Unit,
    onOpenGmsManager: () -> Unit,
    onOpenStorageAccess: () -> Unit,
    onGridColumnsChanged: () -> Unit,
) {
    var themeMode by remember { mutableStateOf(ProfileAppearance.globalThemeMode()) }
    val context = LocalContext.current
    var hideRoot by remember { mutableStateOf(AppManager.mBlackBoxLoader.hideRoot()) }
    var daemonEnabled by remember { mutableStateOf(AppManager.mBlackBoxLoader.daemonEnable()) }
    var disableFlagSecure by remember { mutableStateOf(AppManager.mBlackBoxLoader.disableFlagSecure()) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "General settings") {
                    Text("Color mode", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("system", "light", "dark").forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    ProfileAppearance.setGlobalThemeMode(mode)
                                    ProfileAppearance.notifyThemeChanged()
                                },
                                label = { Text(mode.replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Tune, null) },
                        title = "Preferences",
                        subtitle = "Theme, app grid, and interface options",
                        onClick = onOpenPreferences,
                    )
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Person, null) },
                        title = "Containers",
                        subtitle = "Manage containers and their profiles",
                        onClick = onOpenContainers,
                    )
                }
            }
            item {
                SettingsCard(title = "Virtual app behavior") {
                    SettingsSwitch(
                        title = "Hide root",
                        subtitle = "Apply the existing BlackBox root-hiding option.",
                        checked = hideRoot,
                        onCheckedChange = {
                            hideRoot = it
                            AppManager.mBlackBoxLoader.invalidHideRoot(it)
                            Toast.makeText(context, "Restart the virtual environment for this change", Toast.LENGTH_SHORT).show()
                        },
                    )
                    SettingsSwitch(
                        title = "Daemon",
                        subtitle = "Keep the existing BlackBox daemon behavior.",
                        checked = daemonEnabled,
                        onCheckedChange = {
                            daemonEnabled = it
                            AppManager.mBlackBoxLoader.invalidDaemonEnable(it)
                            Toast.makeText(context, "Restart the virtual environment for this change", Toast.LENGTH_SHORT).show()
                        },
                    )
                    SettingsSwitch(
                        title = "Disable FLAG_SECURE",
                        subtitle = "Allow screenshots in apps that request secure windows.",
                        checked = disableFlagSecure,
                        onCheckedChange = {
                            disableFlagSecure = it
                            AppManager.mBlackBoxLoader.invalidDisableFlagSecure(it)
                            Toast.makeText(context, "Restart the virtual environment for this change", Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }
            item {
                SettingsCard(title = "Tools") {
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        title = "Fake location",
                        subtitle = "Configure the active virtual profile's location",
                        onClick = onOpenFakeLocation,
                    )
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Cloud, null) },
                        title = "Google services",
                        subtitle = "Manage supported Google services",
                        onClick = onOpenGmsManager,
                    )
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Settings, null) },
                        title = "Storage access",
                        subtitle = "Open Android's All Files Access permission",
                        onClick = onOpenStorageAccess,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPreferencesScreen(
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    onGridColumnsChanged: () -> Unit,
) {
    var columns by remember { mutableIntStateOf(ProfileAppearance.appGridColumns()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "Appearance") {
                    SettingsActionRow(
                        icon = { Icon(Icons.Outlined.Palette, null) },
                        title = "Theme",
                        subtitle = "Global colors, dynamic color, and AMOLED mode",
                        onClick = onOpenTheme,
                    )
                }
            }
            item {
                SettingsCard(title = "Interface") {
                    Text("App grid columns", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 4, 5).forEach { value ->
                            FilterChip(
                                selected = columns == value,
                                onClick = {
                                    columns = value
                                    ProfileAppearance.setAppGridColumns(value)
                                    onGridColumnsChanged()
                                },
                                label = { Text(value.toString()) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(onBack: () -> Unit) {
    var dynamicColor by remember { mutableStateOf(ProfileAppearance.useDynamicColor()) }
    var amoled by remember { mutableStateOf(ProfileAppearance.useAmoledDarkMode()) }
    var accent by remember { mutableIntStateOf(ProfileAppearance.globalAccent()) }
    var customHex by remember { mutableStateOf("#%06X".format(0xFFFFFF and accent)) }
    var showCustom by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Theme") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "Theme color") {
                    Text(
                        "The selected theme applies globally to Sleepy Hub. Containers and profiles do not have separate themes.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ProfileAppearance.accentPalette.forEach { color ->
                            val selected = color == accent
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(color), CircleShape)
                                    .clickable {
                                        accent = color
                                        ProfileAppearance.setGlobalAccent(color)
                                        ProfileAppearance.notifyThemeChanged()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) Icon(Icons.Outlined.Check, null, tint = Color.White)
                            }
                        }
                    }
                    TextButton(onClick = { showCustom = true }) { Text("Custom color") }
                }
            }
            item {
                SettingsCard(title = "Theme options") {
                    SettingsSwitch(
                        title = "Dynamic color",
                        subtitle = "Use Android wallpaper colors on Android 12 and newer.",
                        checked = dynamicColor,
                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                        onCheckedChange = {
                            dynamicColor = it
                            ProfileAppearance.setUseDynamicColor(it)
                            ProfileAppearance.notifyThemeChanged()
                        },
                    )
                    SettingsSwitch(
                        title = "AMOLED dark mode",
                        subtitle = "Use true black for dark surfaces.",
                        checked = amoled,
                        onCheckedChange = {
                            amoled = it
                            ProfileAppearance.setUseAmoledDarkMode(it)
                            ProfileAppearance.notifyThemeChanged()
                        },
                    )
                }
            }
        }
    }

    if (showCustom) {
        AlertDialog(
            onDismissRequest = { showCustom = false },
            title = { Text("Custom theme color") },
            text = {
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { customHex = it },
                    singleLine = true,
                    label = { Text("Hex color") },
                    placeholder = { Text("#BDBDBD") },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val parsed = AndroidColor.parseColor(customHex.trim())
                        accent = parsed
                        ProfileAppearance.setGlobalAccent(parsed)
                        ProfileAppearance.notifyThemeChanged()
                        showCustom = false
                    } catch (_: Exception) {
                        Toast.makeText(context, "Enter a valid color such as #BDBDBD", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showCustom = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = androidx.compose.material3.MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

private fun persistReadPermission(context: android.content.Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) {
    }
}

private fun releaseReadPermission(context: android.content.Context, value: String?) {
    if (value.isNullOrBlank()) return
    try {
        context.contentResolver.releasePersistableUriPermission(
            Uri.parse(value),
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    } catch (_: Exception) {
    }
}
