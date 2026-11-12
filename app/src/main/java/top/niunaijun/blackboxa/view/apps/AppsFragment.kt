package top.niunaijun.blackboxa.view.apps

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

    private lateinit var viewModel: AppsViewModel
    private var gridColumns by mutableIntStateOf(4)
    private var isLoading by androidx.compose.runtime.mutableStateOf(false)

    companion object {
        private const val TAG = "AppsFragment"

        fun newInstance(userID: Int): AppsFragment {
            return AppsFragment().apply {
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
                SleepyHubTheme {
                    val apps by viewModel.appsLiveData.observeAsState(emptyList())
                    AppGridScreen(
                        apps = apps,
                        columns = gridColumns,
                        loading = isLoading,
                        onLaunch = ::launchApp,
                        onAction = ::handleAppAction,
                        onScrollChange = { (activity as? MainActivity)?.onWorkspaceScrollChanged(it) },
                    )
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
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
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
    }

    override fun onStart() {
        super.onStart()
        try {
            BlackBoxCore.get().addServiceAvailableCallback {
                Log.d(TAG, "BlackBox services are available; refreshing profile $userID")
                if (isAdded) {
                    viewModel.getInstalledAppsWithRetry(userID)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not register service callback: ${e.message}")
        }
        isLoading = true
        viewModel.getInstalledAppsWithRetry(userID)
    }

    override fun onStop() {
        super.onStop()
        isLoading = false
        viewModel.resultLiveData.value = null
        viewModel.launchLiveData.value = null
    }

    fun refreshApps() {
        if (::viewModel.isInitialized) {
            isLoading = true
            viewModel.getInstalledAppsWithRetry(userID)
        }
    }

    fun refreshGridColumns() {
        gridColumns = ProfileAppearance.appGridColumns()
    }

    fun installApk(source: String) {
        try {
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
            hideLoading()
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
                AppAction.STOP -> {
                    BlackBoxCore.get().stopPackage(info.packageName, userID)
                    Toast.makeText(requireContext(), "Stopped ${info.name}", Toast.LENGTH_SHORT).show()
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
            hideLoading()
            Toast.makeText(requireContext(), "Couldn't complete that action", Toast.LENGTH_SHORT).show()
        }
    }


}
