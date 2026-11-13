package top.niunaijun.blackboxa.view.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import top.niunaijun.blackboxa.databinding.ActivityInstallPickerBinding
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.view.base.BaseActivity
import top.niunaijun.blackboxa.view.main.SleepyHubTheme

class ListActivity : BaseActivity() {

    private val viewBinding: ActivityInstallPickerBinding by inflate()
    private lateinit var viewModel: ListViewModel

    private val openDocumentedResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { finishWithResult(it.toString()) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(
            this,
            InjectionUtil.getListFactory(),
        )[ListViewModel::class.java]

        val userId = intent.getIntExtra("userID", 0)
        viewModel.getInstallAppList(userId)
        viewModel.appsLiveData.observe(this) { apps ->
            if (!apps.isNullOrEmpty()) {
                viewModel.previewInstalledList()
            }
        }

        viewBinding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        viewBinding.composeView.setContent {
            SleepyHubTheme {
                val apps by viewModel.appsLiveData.observeAsState(emptyList())
                val loading by viewModel.loadingLiveData.observeAsState(true)
                InstalledAppPickerScreen(
                    apps = apps,
                    loading = loading,
                    onBack = { finish() },
                    onChooseApk = {
                        openDocumentedResult.launch("application/vnd.android.package-archive")
                    },
                    onSelect = { app -> finishWithResult(app.sourceDir) },
                )
            }
        }
    }

    private fun finishWithResult(source: String) {
        intent.putExtra("source", source)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        fun start(context: Context, userId: Int = 0) {
            val intent = Intent(context, ListActivity::class.java)
                .putExtra("userID", userId)
            context.startActivity(intent)
        }
    }
}
