package top.niunaijun.blackboxa.view.fake

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import top.niunaijun.blackbox.entity.location.BLocation
import top.niunaijun.blackbox.fake.frameworks.BLocationManager
import top.niunaijun.blackboxa.bean.FakeLocationBean
import top.niunaijun.blackboxa.databinding.ActivityFakeLocationBinding
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.util.toast
import top.niunaijun.blackboxa.view.base.BaseActivity
import top.niunaijun.blackboxa.view.main.SleepyHubTheme

class FakeManagerActivity : BaseActivity() {

    private val viewBinding: ActivityFakeLocationBinding by inflate()
    private lateinit var viewModel: FakeLocationViewModel
    private var isLoading by mutableStateOf(true)

    private val locationResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { data ->
                    val latitude = data.getDoubleExtra("latitude", 0.0)
                    val longitude = data.getDoubleExtra("longitude", 0.0)
                    val packageName = data.getStringExtra("pkg") ?: return@let

                    viewModel.applyFakeLocation(
                        currentUserID(),
                        packageName,
                        BLocation(latitude, longitude),
                    )
                    toast(getString(top.niunaijun.blackboxa.R.string.set_location, latitude.toString(), longitude.toString()))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(
            this,
            InjectionUtil.getFakeLocationFactory(),
        )[FakeLocationViewModel::class.java]

        viewModel.appsLiveData.observe(this) {
            if (it != null) isLoading = false
        }

        viewBinding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        viewBinding.composeView.setContent {
            SleepyHubTheme {
                val apps by viewModel.appsLiveData.observeAsState(emptyList())
                FakeLocationScreen(
                    apps = apps,
                    loading = isLoading,
                    onBack = { finish() },
                    onSelect = ::openLocationPicker,
                    onDisable = ::disableFakeLocation,
                )
            }
        }
        loadAppList()
    }

    private fun loadAppList() {
        isLoading = true
        viewModel.getInstallAppList(currentUserID())
    }

    private fun openLocationPicker(app: FakeLocationBean) {
        val intent = Intent(this, FollowMyLocationOverlay::class.java).apply {
            putExtra("location", app.fakeLocation)
            putExtra("pkg", app.packageName)
        }
        locationResult.launch(intent)
    }

    private fun disableFakeLocation(app: FakeLocationBean) {
        viewModel.disableFakeLocation(currentUserID(), app.packageName)
        toast(getString(top.niunaijun.blackboxa.R.string.close_fake_location_success, app.name))
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, FakeManagerActivity::class.java))
        }
    }
}
