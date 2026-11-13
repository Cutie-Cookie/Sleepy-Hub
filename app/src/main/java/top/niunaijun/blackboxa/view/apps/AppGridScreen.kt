package top.niunaijun.blackboxa.view.apps

import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Stop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import top.niunaijun.blackboxa.bean.AppInfo

enum class AppAction {
    INFO,
    CLEAR_CACHE,
    CLEAR_DATA,
    STOP,
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
    onScrollChange: (Boolean) -> Unit,
) {
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var confirmingApp by remember { mutableStateOf<AppInfo?>(null) }
    var confirmingAction by remember { mutableStateOf<AppAction?>(null) }
    var infoApp by remember { mutableStateOf<AppInfo?>(null) }
    val gridState = rememberLazyGridState()
    val isScrolled by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0 }
    }
    LaunchedEffect(isScrolled) { onScrollChange(isScrolled) }

    Box(modifier = Modifier.fillMaxSize()) {
    if (apps.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator()
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
                bottom = 104.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            items(apps, key = { it.packageName }) { app ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onLaunch(app) },
                            onLongClick = { selectedApp = app },
                        )
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
    if (loading && apps.isNotEmpty()) {
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
                AppActionRow(Icons.Outlined.Stop, "Stop") {
                    selectedApp = null
                    onAction(app, AppAction.STOP)
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
                        color = Color.White.copy(alpha = 0.72f),
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
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
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
