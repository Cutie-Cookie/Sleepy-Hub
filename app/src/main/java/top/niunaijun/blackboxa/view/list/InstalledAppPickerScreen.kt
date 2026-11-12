package top.niunaijun.blackboxa.view.list

import android.widget.ImageView
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import top.niunaijun.blackboxa.bean.InstalledAppBean

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppPickerScreen(
    apps: List<InstalledAppBean>,
    loading: Boolean,
    onBack: () -> Unit,
    onChooseApk: () -> Unit,
    onSelect: (InstalledAppBean) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredApps = remember(apps, query) {
        apps.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Install apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onChooseApk) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = "Choose APK file")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                        }
                    }
                },
            )

            when {
                loading && filteredApps.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredApps.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.Apps,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                if (apps.isEmpty()) "No installable apps found" else "No matching apps",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "You can choose an APK file from your device instead.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            Card(
                                onClick = { if (!app.isInstall) onSelect(app) },
                                enabled = !app.isInstall,
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                                ),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    Box(
                                        modifier = Modifier.size(48.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        AndroidView(
                                            factory = { context ->
                                                ImageView(context).apply {
                                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                                }
                                            },
                                            update = { it.setImageDrawable(app.icon) },
                                            modifier = Modifier.size(44.dp),
                                        )
                                        if (app.icon == null) {
                                            Icon(
                                                Icons.Outlined.Apps,
                                                contentDescription = null,
                                                modifier = Modifier.size(36.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp),
                                    ) {
                                        Text(
                                            app.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            app.packageName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Text(
                                        if (app.isInstall) "Installed" else "Add",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (app.isInstall) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
