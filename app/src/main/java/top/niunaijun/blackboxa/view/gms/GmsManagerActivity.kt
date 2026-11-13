package top.niunaijun.blackboxa.view.gms

import top.niunaijun.blackboxa.view.main.LargeFlexibleTopAppBar
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsets.Companion.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.bean.GmsBean
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.view.base.BaseActivity
import top.niunaijun.blackboxa.view.main.GooglePlayBadge
import top.niunaijun.blackboxa.view.main.ProfileAvatar
import top.niunaijun.blackboxa.view.main.SleepyHubTheme

class GmsManagerActivity : BaseActivity() {

    private lateinit var viewModel: GmsViewModel
    private var isLoading by mutableStateOf(true)
    private lateinit var containerId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        containerId = intent.getStringExtra(EXTRA_CONTAINER_ID)
            ?.takeIf { it.isNotBlank() }
            ?: ProfileAppearance.activeContainerId()

        viewModel = ViewModelProvider(
            this,
            InjectionUtil.getGmsFactory(),
        )[GmsViewModel::class.java]

        viewModel.installedLiveData.observe(this) {
            isLoading = false
        }
        viewModel.updateInstalledLiveData.observe(this) { result ->
            if (result == null) return@observe
            Toast.makeText(this, result.msg, Toast.LENGTH_SHORT).show()
            viewModel.getInstalledProfiles(containerId)
        }

        setContentView(
            ComposeView(this).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    SleepyHubTheme {
                        val profiles by viewModel.installedLiveData.observeAsState(emptyList())
                        GoogleServicesScreen(
                            profiles = profiles,
                            loading = isLoading,
                            onBack = { finish() },
                            onToggle = { userId, enable ->
                                if (enable) viewModel.installGms(userId)
                                else viewModel.uninstallGms(userId)
                            },
                        )
                    }
                }
            },
        )

        viewModel.getInstalledProfiles(containerId)
    }

    companion object {
        private const val EXTRA_CONTAINER_ID = "sleepy_hub_container_id"

        fun start(context: Context, containerId: String = ProfileAppearance.activeContainerId()) {
            context.startActivity(
                Intent(context, GmsManagerActivity::class.java)
                    .putExtra(EXTRA_CONTAINER_ID, containerId),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoogleServicesScreen(
    profiles: List<GmsBean>,
    loading: Boolean,
    onBack: () -> Unit,
    onToggle: (userId: Int, enable: Boolean) -> Unit,
) {
    var pendingProfile by androidx.compose.runtime.remember { mutableStateOf<GmsBean?>(null) }
    var pendingEnable by androidx.compose.runtime.remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Google services") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        Icons.Outlined.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(12.dp).size(24.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Per-profile Google Play services", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Enable Google services only in the profiles that need them. Their service apps stay out of your workspace; enabled profiles show a small Play Store badge beside their name.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            when {
                loading && profiles.isEmpty() -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                profiles.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "There are no profiles in this container yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(profiles, key = { it.userID }) { profile ->
                            GoogleServicesProfileRow(
                                profile = profile,
                                onToggle = { enabled ->
                                    pendingProfile = profile
                                    pendingEnable = enabled
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { pendingProfile = null },
            title = {
                Text(
                    if (pendingEnable) "Enable Google services?" else "Disable Google services?",
                )
            },
            text = {
                Text(
                    if (pendingEnable) {
                        "Google Play services and the Play Store will be installed inside ${profile.userName}. They will not appear as ordinary workspace apps; the profile will show the Play Store badge."
                    } else {
                        "Google services will be removed from ${profile.userName}. Apps in this profile that depend on them may stop working."
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onToggle(profile.userID, pendingEnable)
                        pendingProfile = null
                    },
                ) {
                    Text(if (pendingEnable) "Enable" else "Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingProfile = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun GoogleServicesProfileRow(
    profile: GmsBean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileAvatar(profile.userName, profile.avatarUri, 44.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        profile.userName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (profile.isInstalledGms) GooglePlayBadge(size = 16.dp)
                }
                Text(
                    if (profile.isInstalledGms) "Google services enabled" else "Google services disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = profile.isInstalledGms,
                onCheckedChange = onToggle,
            )
        }
    }
}
