package top.niunaijun.blackboxa.view.apps

import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.bean.AppInfo
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.util.ShortcutUtil
import top.niunaijun.blackboxa.view.main.MainActivity
import top.niunaijun.blackboxa.view.main.SleepyHubTheme

class AppsFragment : Fragment() {

    var userID: Int = 0
        private set
    var isWorkspaceScrolled: Boolean = false
        private set

    private lateinit var viewModel: AppsViewModel
    private var gridColumns by mutableIntStateOf(4)
    private var isLoading by androidx.compose.runtime.mutableStateOf(true)
    private var serviceCallbackRegistered = false
    private var pendingInstallSource: String? = null
    private var installErrorMessage by androidx.compose.runtime.mutableStateOf<String?>(null)
    private val serviceAvailableCallback = Runnable {
        Log.d(TAG, "BlackBox services are available; refreshing profile $userID")
        if (isAdded && ::viewModel.isInitialized) {
            viewModel.getInstalledApps(userID)
        }
    }

    companion object {
        private const val TAG = "AppsFragment"

        fun newInstance(userID: Int): AppsFragment {
            return AppsFragment().apply {
                this.userID = userID
                arguments = Bundle().apply { putInt("userID", userID) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userID = requireArguments().getInt("userID", 0)
        gridColumns = ProfileAppearance.appGridColumns()
        viewModel = ViewModelProvider(
            this,
            InjectionUtil.getAppsFactory(),
        )[AppsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                SleepyHubTheme(workspace = true) {
                    val apps by viewModel.appsLiveData.observeAsState(emptyList())
                    Box(modifier = Modifier.fillMaxSize()) {
                        AppGridScreen(
                            apps = apps,
                            columns = gridColumns,
                            loading = isLoading,
                            onLaunch = ::launchApp,
                            onAction = ::handleAppAction,
                            onReorder = { viewModel.updateApkOrder(userID, it) },
                            onScrollChange = {
                                isWorkspaceScrolled = it
                                (activity as? MainActivity)?.onWorkspaceScrollChanged(userID, it)
                            },
                        )
                        installErrorMessage?.let { message ->
                            AlertDialog(
                                onDismissRequest = { installErrorMessage = null },
                                title = { Text(if (message.contains("install", ignoreCase = true)) "Couldn't install app" else "Couldn't complete action") },
                                text = { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                confirmButton = {
                                    TextButton(onClick = { installErrorMessage = null }) { Text("OK") }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.appsLiveData.observe(viewLifecycleOwner) { apps ->
            isLoading = false
            (activity as? MainActivity)?.onProfileAppCountChanged(userID, apps?.size ?: 0)
        }
        viewModel.resultLiveData.observe(viewLifecycleOwner) { result ->
            if (!result.isNullOrBlank()) {
                isLoading = false
                if (result.contains("failed", ignoreCase = true)) {
                    installErrorMessage = result
                } else {
                    Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
                }
                viewModel.resultLiveData.value = null
                viewModel.getInstalledApps(userID)
                (activity as? MainActivity)?.scanUser()
            }
        }
        viewModel.launchLiveData.observe(viewLifecycleOwner) { launched ->
            if (launched != null) {
                isLoading = false
                if (!launched) {
                    Toast.makeText(requireContext(), "Couldn't start this app", Toast.LENGTH_SHORT).show()
                }
            }
        }

        pendingInstallSource?.let { source ->
            pendingInstallSource = null
            installApk(source)
        }
    }

    override fun onStart() {
        super.onStart()
        if (!serviceCallbackRegistered) {
            try {
                BlackBoxCore.get().addServiceAvailableCallback(serviceAvailableCallback)
                serviceCallbackRegistered = true
            } catch (e: Exception) {
                Log.w(TAG, "Could not register service callback: ${e.message}")
            }
        }
        isLoading = true
        viewModel.getInstalledApps(userID)
    }

    override fun onStop() {
        super.onStop()
        isLoading = false
        viewModel.resultLiveData.value = null
        viewModel.launchLiveData.value = null
    }

    override fun onDestroy() {
        try {
            BlackBoxCore.get().removeServiceAvailableCallback(serviceAvailableCallback)
        } catch (_: Exception) {
        }
        serviceCallbackRegistered = false
        super.onDestroy()
    }

    fun refreshApps() {
        if (::viewModel.isInitialized) {
            isLoading = true
            viewModel.getInstalledApps(userID)
        }
    }

    fun refreshGridColumns() {
        gridColumns = ProfileAppearance.appGridColumns()
    }

    fun installApk(source: String) {
        if (!::viewModel.isInitialized) {
            pendingInstallSource = source
            return
        }
        try {
            installErrorMessage = null
            isLoading = true
            viewModel.install(source, userID)
        } catch (e: Exception) {
            Log.e(TAG, "Could not install APK: ${e.message}", e)
            isLoading = false
        }
    }

    private fun launchApp(info: AppInfo) {
        try {
            isLoading = true
            viewModel.launchApk(info.packageName, userID)
        } catch (e: Exception) {
            Log.e(TAG, "Could not launch ${info.packageName}: ${e.message}", e)
            isLoading = false
        }
    }

    private fun handleAppAction(info: AppInfo, action: AppAction) {
        try {
            when (action) {
                AppAction.INFO -> Unit
                AppAction.CLEAR_CACHE -> {
                    isLoading = true
                    viewModel.clearApkCache(info.packageName, userID)
                }
                AppAction.CLEAR_DATA -> {
                    isLoading = true
                    viewModel.clearApkData(info.packageName, userID)
                }
                AppAction.SHORTCUT -> {
                    ShortcutUtil.createShortcut(requireContext(), userID, info)
                }
                AppAction.UNINSTALL -> {
                    if (info.isXpModule) {
                        Toast.makeText(requireContext(), "Uninstall this module from its module manager", Toast.LENGTH_LONG).show()
                    } else {
                        isLoading = true
                        viewModel.unInstall(info.packageName, userID)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "App action failed for ${info.packageName}: ${e.message}", e)
            isLoading = false
            Toast.makeText(requireContext(), "Couldn't complete that action", Toast.LENGTH_SHORT).show()
        }
    }


}
