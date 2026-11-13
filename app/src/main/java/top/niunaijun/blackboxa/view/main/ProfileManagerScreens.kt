package top.niunaijun.blackboxa.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileManagerScreen(
    profiles: List<ProfileItem>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onClone: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onReorder: (List<Int>) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFolder by rememberSaveable { mutableStateOf("All") }
    var pendingDelete by remember { mutableStateOf<ProfileItem?>(null) }
    var pendingClone by remember { mutableStateOf<ProfileItem?>(null) }
    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val folders = remember(profiles) { profiles.map { it.folder }.distinct().sorted() }
    val filteredProfiles = remember(profiles, query, selectedFolder) {
        profiles.filter { profile ->
            (selectedFolder == "All" || profile.folder == selectedFolder) &&
                (query.isBlank() || profile.name.contains(query, ignoreCase = true))
        }
    }
    val canReorder = query.isBlank() && selectedFolder == "All"

    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        if (canReorder && from.index in filteredProfiles.indices && to.index in filteredProfiles.indices) {
            val updated = filteredProfiles.toMutableList()
            val moved = updated.removeAt(from.index)
            updated.add(to.index, moved)
            onReorder(updated.map { it.userId })
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            LargeFlexibleTopAppBar(
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
                shape = RoundedCornerShape(28.dp),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search profiles") },
            )
            if (folders.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(listOf("All") + folders) { folder ->
                        FilterChip(
                            selected = selectedFolder == folder,
                            onClick = { selectedFolder = folder },
                            label = { Text(folder) },
                        )
                    }
                }
            }
            if (!canReorder) {
                Text(
                    "Clear search and folder filters to reorder profiles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (filteredProfiles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (profiles.isEmpty()) "No profiles yet" else "No matching profiles",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(filteredProfiles, key = { _, profile -> profile.userId }) { _, profile ->
                        ReorderableItem(reorderableState, key = profile.userId) { isDragging ->
                            val dragModifier = if (canReorder) Modifier.longPressDraggableHandle() else Modifier
                            ProfileManagerRow(
                                profile = profile,
                                isDragging = isDragging,
                                onEdit = { onEdit(profile.userId) },
                                onClone = { pendingClone = profile },
                                onDelete = { pendingDelete = profile },
                                modifier = Modifier
                                    .animateItem()
                                    .then(dragModifier)
                                    .scale(if (isDragging) 1.015f else 1f),
                            )
                        }
                    }
                }
            }
        }
    }

    pendingClone?.let { profile ->
        AlertDialog(
            onDismissRequest = { pendingClone = null },
            title = { Text("Clone ${profile.name}?") },
            text = {
                Text("The installed app list and profile appearance will be copied. App data, accounts and sign-ins start fresh in the new profile.")
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingClone = null
                    onClone(profile.userId)
                }) { Text("Clone profile") }
            },
            dismissButton = { TextButton(onClick = { pendingClone = null }) { Text("Cancel") } },
        )
    }

    pendingDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${profile.name}?") },
            text = { Text("This permanently deletes this virtual profile, its installed apps and all app data.") },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    onDelete(profile.userId)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ProfileManagerRow(
    profile: ProfileItem,
    isDragging: Boolean,
    onEdit: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileAvatar(profile.name, profile.avatarUri, 48.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        profile.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (profile.googlePlayEnabled) GooglePlayBadge()
                }
                Text(
                    buildString {
                        append(if (profile.appCount >= 0) "${profile.appCount} apps" else "Virtual profile")
                        append(" · ")
                        append(profile.folder)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Outlined.MoreHoriz, contentDescription = "Profile actions")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Edit profile") },
                    onClick = { menuExpanded = false; onEdit() },
                )
                DropdownMenuItem(
                    text = { Text("Clone profile") },
                    leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) },
                    onClick = { menuExpanded = false; onClone() },
                )
                DropdownMenuItem(
                    text = { Text("Delete profile") },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null) },
                    onClick = { menuExpanded = false; onDelete() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    profile: ProfileItem,
    onBack: () -> Unit,
    onOpenBasic: () -> Unit,
    onOpenBackground: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showTitle by remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showTitle,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
                    ) { Text(profile.name, maxLines = 1) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ProfileAvatar(profile.name, profile.avatarUri, 60.dp)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(profile.name, style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (profile.googlePlayEnabled) GooglePlayBadge(size = 18.dp)
                        }
                        Text(
                            if (profile.appCount >= 0) "${profile.appCount} installed apps" else "Virtual Android profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                CardGroup(title = "Profile") {
                    CardGroupItem(
                        title = "Basic information",
                        subtitle = "Name, picture and folder",
                        icon = Icons.Outlined.Person,
                        trailing = { Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = onOpenBasic,
                    )
                }
            }
            item {
                CardGroup(title = "Workspace") {
                    CardGroupItem(
                        title = "Background",
                        subtitle = if (profile.backgroundUri.isNullOrBlank()) "No background selected" else "Change or remove this profile's wallpaper",
                        icon = Icons.Outlined.Image,
                        trailing = { Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = onOpenBackground,
                    )
                }
            }
            item {
                CardGroup(title = "Virtual profile") {
                    CardGroupItem(
                        title = "Delete profile",
                        subtitle = "Permanently remove its apps and data",
                        icon = Icons.Outlined.Delete,
                        trailing = { Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { confirmDelete = true },
                    )
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${profile.name}?") },
            text = { Text("This permanently removes this virtual profile, its installed apps and all app data.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBasicScreen(
    profile: ProfileItem,
    onBack: () -> Unit,
    onSave: (name: String, folder: String, avatarUri: String?) -> Unit,
) {
    val context = LocalContext.current
    var name by remember(profile.userId) { mutableStateOf(profile.name) }
    var folder by remember(profile.userId) { mutableStateOf(profile.folder) }
    var avatarUri by remember(profile.userId) { mutableStateOf(profile.avatarUri) }
    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            persistProfileImagePermission(context, uri)
            avatarUri = uri.toString()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CardGroup(title = "Profile picture") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ProfileAvatar(name, avatarUri, 64.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Profile picture", style = MaterialTheme.typography.titleSmall)
                            Text("Shown beside this profile's name", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row {
                                TextButton(onClick = { avatarPicker.launch(arrayOf("image/*")) }) {
                                    Text(if (avatarUri.isNullOrBlank()) "Choose picture" else "Change")
                                }
                                if (!avatarUri.isNullOrBlank()) {
                                    TextButton(onClick = { avatarUri = null }) { Text("Remove") }
                                }
                            }
                        }
                    }
                }
            }
            item {
                CardGroup(title = "Profile details") {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            supportingText = { Text("Folders organize profiles inside this container.") },
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { onSave(name.trim(), folder.trim().ifBlank { "General" }, avatarUri) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save changes") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileBackgroundScreen(
    profile: ProfileItem,
    onBack: () -> Unit,
    onSave: (uri: String?, opacity: Float) -> Unit,
) {
    val context = LocalContext.current
    var backgroundUri by remember(profile.userId) { mutableStateOf(profile.backgroundUri) }
    var opacity by remember(profile.userId) { mutableStateOf(profile.backgroundOpacity) }
    val backgroundPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            persistProfileImagePermission(context, uri)
            backgroundUri = uri.toString()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(190.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        ProfileWorkspaceBackground(
                            profile.copy(backgroundUri = backgroundUri, backgroundOpacity = opacity),
                        )
                        Column(
                            modifier = Modifier.fillMaxSize().padding(18.dp),
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Text(profile.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("Workspace preview", color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                CardGroup(title = "Background image") {
                    CardGroupItem(
                        title = if (backgroundUri.isNullOrBlank()) "Choose an image" else "Change image",
                        subtitle = "Only appears behind this profile's app workspace",
                        icon = Icons.Outlined.Image,
                        onClick = { backgroundPicker.launch(arrayOf("image/*")) },
                    )
                    if (!backgroundUri.isNullOrBlank()) {
                        CardGroupDivider()
                        CardGroupItem(
                            title = "Remove background",
                            icon = Icons.Outlined.Delete,
                            onClick = { backgroundUri = null },
                        )
                    }
                }
            }
            item {
                CardGroup(title = "Image opacity") {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Opacity", modifier = Modifier.weight(1f))
                            Text("${(opacity * 100).toInt()}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Slider(
                            value = opacity,
                            onValueChange = { opacity = it },
                            valueRange = 0.05f..1f,
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { onSave(backgroundUri, opacity) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save background") }
            }
        }
    }
}

private fun persistProfileImagePermission(context: Context, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) {
    }
}
