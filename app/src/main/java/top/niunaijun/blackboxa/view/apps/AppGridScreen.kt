package top.niunaijun.blackboxa.view.apps

import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DragHandle

import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import top.niunaijun.blackboxa.bean.AppInfo

enum class AppAction {
    INFO,
    CLEAR_CACHE,
    CLEAR_DATA,
    SHORTCUT,
    UNINSTALL,
}

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AppGridScreen(
    apps: List<AppInfo>,
    columns: Int,
    loading: Boolean,
    onLaunch: (AppInfo) -> Unit,
    onAction: (AppInfo, AppAction) -> Unit,
    onReorder: (List<AppInfo>) -> Unit,
    onScrollChange: (Boolean) -> Unit,
) {
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var displayedApps by remember(apps) { mutableStateOf(apps) }
    var didReorder by remember { mutableStateOf(false) }
    var confirmingApp by remember { mutableStateOf<AppInfo?>(null) }
    var confirmingAction by remember { mutableStateOf<AppAction?>(null) }
    var infoApp by remember { mutableStateOf<AppInfo?>(null) }
    val gridState = rememberLazyGridState()
    val reorderableState = rememberReorderableLazyGridState(gridState) { from, to ->
        if (from.index in displayedApps.indices && to.index in displayedApps.indices) {
            val updated = displayedApps.toMutableList()
            val moved = updated.removeAt(from.index)
            updated.add(to.index, moved)
            displayedApps = updated
            didReorder = true
        }
    }
    val isScrolled by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0 }
    }
    LaunchedEffect(isScrolled) { onScrollChange(isScrolled) }

    Box(modifier = Modifier.fillMaxSize()) {
    if (displayedApps.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.size(14.dp))
                    Text(
                        "Loading apps…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.76f),
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.White.copy(alpha = 0.72f),
                    )
                    Spacer(Modifier.size(12.dp))
                    Text("No apps in this profile", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        "Install an app to get started.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns.coerceIn(3, 5)),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp,
                bottom = 20.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(displayedApps, key = { it.packageName }) { app ->
                ReorderableItem(reorderableState, key = app.packageName) { isDragging ->
                    Column(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { onLaunch(app) },
                                onLongClick = { selectedApp = app },
                            )
                            .scale(if (isDragging) 1.04f else 1f)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                    Box(
                        modifier = Modifier.size(56.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                }
                            },
                            update = { imageView ->
                                imageView.setImageDrawable(app.icon)
                                imageView.setOnClickListener { onLaunch(app) }
                                imageView.setOnLongClickListener {
                                    selectedApp = app
                                    true
                                }
                            },
                            modifier = Modifier.size(52.dp),
                        )
                        if (app.icon == null) {
                            Icon(
                                Icons.Outlined.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (app.isXpModule) {
                            Surface(
                                modifier = Modifier.align(Alignment.TopStart),
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Text(
                                    "XP",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 8.sp,
                                )
                            }
                        }
                        if (displayedApps.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                                    .draggableHandle(
                                        onDragStarted = { didReorder = false },
                                        onDragStopped = {
                                            if (didReorder) onReorder(displayedApps)
                                            didReorder = false
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Outlined.DragHandle,
                                    contentDescription = "Reorder app",
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            minLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
    AnimatedVisibility(
        visible = loading && displayedApps.isNotEmpty(),
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.padding(18.dp))
            }
        }
    }
    }

    selectedApp?.let { app ->
        ModalBottomSheet(onDismissRequest = { selectedApp = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(app.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.size(8.dp))
                AppActionRow(Icons.Outlined.Info, "App info") {
                    infoApp = app
                    selectedApp = null
                }
                AppActionRow(Icons.Outlined.CleaningServices, "Clear cache") {
                    selectedApp = null
                    onAction(app, AppAction.CLEAR_CACHE)
                }
                AppActionRow(Icons.Outlined.Delete, "Clear data") {
                    selectedApp = null
                    confirmingApp = app
                    confirmingAction = AppAction.CLEAR_DATA
                }
                AppActionRow(Icons.Outlined.Launch, "Create shortcut") {
                    selectedApp = null
                    onAction(app, AppAction.SHORTCUT)
                }
                AppActionRow(Icons.Outlined.DeleteForever, "Uninstall", destructive = true) {
                    selectedApp = null
                    confirmingApp = app
                    confirmingAction = AppAction.UNINSTALL
                }
            }
        }
    }

    val appToConfirm = confirmingApp
    val actionToConfirm = confirmingAction
    if (appToConfirm != null && actionToConfirm != null) {
        val clearData = actionToConfirm == AppAction.CLEAR_DATA
        AlertDialog(
            onDismissRequest = {
                confirmingApp = null
                confirmingAction = null
            },
            title = {
                Text(if (clearData) "Clear ${appToConfirm.name} data?" else "Uninstall ${appToConfirm.name}?")
            },
            text = {
                Text(
                    if (clearData) {
                        "This resets this app inside the selected profile. Its local data and sign-in state will be lost."
                    } else {
                        "This removes the app from this profile. Other profiles are not affected."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(appToConfirm, actionToConfirm)
                        confirmingApp = null
                        confirmingAction = null
                    },
                ) {
                    Text(if (clearData) "Clear data" else "Uninstall", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        confirmingApp = null
                        confirmingAction = null
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    infoApp?.let { app ->
        AlertDialog(
            onDismissRequest = { infoApp = null },
            title = { Text(app.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(app.packageName, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        app.sourceDir,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { infoApp = null }) { Text("Done") }
            },
        )
    }
}

@Composable
private fun AppActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
