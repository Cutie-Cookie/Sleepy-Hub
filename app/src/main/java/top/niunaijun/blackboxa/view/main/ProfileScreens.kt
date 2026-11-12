package top.niunaijun.blackboxa.view.main

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.app.AppManager
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.view.gms.GmsManagerActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagerScreen(
    profiles: List<ProfileItem>,
    activeUserId: Int,
    onBack: () -> Unit,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(profiles, search) {
        profiles.filter { it.name.contains(search, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profiles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search profiles") },
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filtered, key = { it.userId }) { profile ->
                    Card(
                        onClick = { onEdit(profile.userId) },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = if (profile.userId == activeUserId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                            },
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ProfileAvatar(profile.name, profile.accent, 48.dp)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(profile.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (profile.appCount >= 0) "${profile.appCount} installed apps" else "Virtual profile",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { onSelect(profile.userId) }) {
                                Text(if (profile.userId == activeUserId) "Open" else "Switch")
                            }
                            IconButton(onClick = { onEdit(profile.userId) }) {
                                Icon(Icons.Outlined.Settings, contentDescription = "Profile settings")
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
fun ProfileSettingsScreen(
    profile: ProfileItem,
    profileCount: Int,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onAppearanceChanged: () -> Unit,
) {
    val context = LocalContext.current
    var name by remember(profile.userId) { mutableStateOf(profile.name) }
    var accent by remember(profile.userId) { mutableIntStateOf(profile.accent) }
    var backgroundUri by remember(profile.userId) { mutableStateOf(profile.backgroundUri) }
    var opacity by remember(profile.userId) { mutableStateOf(profile.backgroundOpacity) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val backgroundPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: Exception) {
            }
            val previous = ProfileAppearance.backgroundUri(profile.userId)
            if (!previous.isNullOrBlank() && previous != uri.toString()) {
                try {
                    context.contentResolver.releasePersistableUriPermission(
                        android.net.Uri.parse(previous),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                } catch (_: Exception) {
                }
            }
            backgroundUri = uri.toString()
            ProfileAppearance.setBackgroundUri(profile.userId, backgroundUri)
            onAppearanceChanged()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Profile", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Profile name") },
                        )
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    onRename(name.trim())
                                    Toast.makeText(context, "Profile name saved", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = name.isNotBlank() && name.trim() != profile.name,
                        ) {
                            Text("Save name")
                        }
                    }
                }
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.Palette, contentDescription = null)
                            Text("Profile accent", style = MaterialTheme.typography.titleMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProfileAppearance.accentPalette.forEach { color ->
                                val selected = color == accent
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(color), CircleShape)
                                        .then(
                                            if (selected) Modifier.padding(2.dp) else Modifier
                                        )
                                        .clickable {
                                            accent = color
                                            ProfileAppearance.setProfileAccent(profile.userId, color)
                                            onAppearanceChanged()
                                        },
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null)
                            Text("Workspace background", style = MaterialTheme.typography.titleMedium)
                        }
                        Text(
                            "This background belongs to this profile's workspace. It does not appear inside the profile picker.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp),
                        ) {
                            ProfileWorkspaceBackground(
                                profile = profile.copy(
                                    accent = accent,
                                    backgroundUri = backgroundUri,
                                    backgroundOpacity = opacity,
                                ),
                                modifier = Modifier.fillMaxSize(),
                            )
                            Text(
                                "Workspace preview",
                                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Button(onClick = { backgroundPicker.launch(arrayOf("image/*")) }) {
                                Text(if (backgroundUri == null) "Choose image" else "Change image")
                            }
                            if (backgroundUri != null) {
                                TextButton(
                                    onClick = {
                                        try {
                                            context.contentResolver.releasePersistableUriPermission(
                                                android.net.Uri.parse(backgroundUri),
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                            )
                                        } catch (_: Exception) {
                                        }
                                        backgroundUri = null
                                        ProfileAppearance.setBackgroundUri(profile.userId, null)
                                        onAppearanceChanged()
                                    },
                                ) {
                                    Text("Remove")
                                }
                            }
                        }
                        if (backgroundUri != null) {
                            Text(
                                "Background visibility · ${(opacity * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Slider(
                                value = opacity,
                                onValueChange = {
                                    opacity = it
                                    ProfileAppearance.setBackgroundOpacity(profile.userId, it)
                                    onAppearanceChanged()
                                },
                                valueRange = 0f..1f,
                            )
                        }
                    }
                }
            }

            if (profileCount > 1) {
                item {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
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
            text = {
                Text("This permanently removes this virtual profile and its installed apps and data.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var themeMode by remember { mutableStateOf(ProfileAppearance.globalThemeMode()) }
    var dynamicColor by remember { mutableStateOf(ProfileAppearance.useDynamicColor()) }
    var amoled by remember { mutableStateOf(ProfileAppearance.useAmoledDarkMode()) }
    var accent by remember { mutableIntStateOf(ProfileAppearance.globalAccent()) }
    var columns by remember { mutableIntStateOf(ProfileAppearance.appGridColumns()) }
    var hideRoot by remember { mutableStateOf(AppManager.mBlackBoxLoader.hideRoot()) }
    var daemonEnabled by remember { mutableStateOf(AppManager.mBlackBoxLoader.daemonEnable()) }
    var disableFlagSecure by remember { mutableStateOf(AppManager.mBlackBoxLoader.disableFlagSecure()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "Appearance") {
                    Text("Theme mode", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("system", "light", "dark").forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    ProfileAppearance.setGlobalThemeMode(mode)
                                    ProfileAppearance.notifyThemeChanged()
                                },
                                label = {
                                    Text(mode.replaceFirstChar { it.uppercase() })
                                },
                            )
                        }
                    }
                    SettingsSwitch(
                        title = "Dynamic colors",
                        subtitle = "Use Android's wallpaper colors on Android 12 and newer.",
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
                        subtitle = "Use a true-black background in dark mode.",
                        checked = amoled,
                        onCheckedChange = {
                            amoled = it
                            ProfileAppearance.setUseAmoledDarkMode(it)
                            ProfileAppearance.notifyThemeChanged()
                        },
                    )
                    Text("Global accent", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ProfileAppearance.accentPalette.forEach { color ->
                            val selected = color == accent
                            Surface(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        accent = color
                                        ProfileAppearance.setGlobalAccent(color)
                                        ProfileAppearance.notifyThemeChanged()
                                    },
                                shape = CircleShape,
                                color = Color(color),
                                border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
                            ) {}
                        }
                    }
                }
            }

            item {
                SettingsCard(title = "App grid") {
                    Text("Columns", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(3, 4, 5).forEach { value ->
                            FilterChip(
                                selected = columns == value,
                                onClick = {
                                    columns = value
                                    ProfileAppearance.setAppGridColumns(value)
                                },
                                label = { Text(value.toString()) },
                            )
                        }
                    }
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
                SettingsCard(title = "Google services") {
                    val supported = BlackBoxCore.get().isSupportGms
                    Text(
                        if (supported) "Manage Google services for virtual profiles."
                        else "Google services are not supported by this build.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(
                        onClick = { GmsManagerActivity.start(context) },
                        enabled = supported,
                    ) {
                        Text("Google services manager")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
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
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}
