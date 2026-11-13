package top.niunaijun.blackboxa.view.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import java.io.File
import java.io.IOException
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.databinding.ActivityInstallPickerBinding
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.view.base.BaseActivity
import top.niunaijun.blackboxa.view.main.SleepyHubTheme

class ListActivity : BaseActivity() {

    private val viewBinding: ActivityInstallPickerBinding by inflate()
    private lateinit var viewModel: ListViewModel
    private var copyingApk by mutableStateOf(false)

    private val openDocumentedResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let(::copySelectedApk)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewModel = ViewModelProvider(
            this,
            InjectionUtil.getListFactory(),
        )[ListViewModel::class.java]

        val userId = intent.getIntExtra("userID", 0)
        viewModel.loadInstallApps(userId)

        viewBinding.composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        viewBinding.composeView.setContent {
            SleepyHubTheme {
                val apps by viewModel.appsLiveData.observeAsState(emptyList())
                val loading by viewModel.loadingLiveData.observeAsState(true)
                InstalledAppPickerScreen(
                    apps = apps,
                    profileName = ProfileAppearance.profileName(userId, userId),
                    loading = loading || copyingApk,
                    onBack = { finish() },
                    onChooseApk = {
                        openDocumentedResult.launch("application/vnd.android.package-archive")
                    },
                    onSelect = { app -> finishWithResult(app.packageName) },
                )
            }
        }
    }

    private fun copySelectedApk(uri: Uri) {
        copyingApk = true
        lifecycleScope.launch {
            try {
                val copiedFile = withContext(Dispatchers.IO) {
                    val displayName = contentResolver.query(
                        uri,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null,
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                        } else null
                    } ?: "selected.apk"

                    val safeName = displayName
                        .substringAfterLast('/')
                        .replace(Regex("[^A-Za-z0-9._-]"), "_")
                        .let { if (it.endsWith(".apk", ignoreCase = true)) it else "$it.apk" }

                    val directory = File(cacheDir, "apk-imports").apply { mkdirs() }
                    val target = File(directory, "${System.currentTimeMillis()}-$safeName")
                    val input = contentResolver.openInputStream(uri)
                        ?: throw IOException("The selected file could not be opened.")
                    input.use { source ->
                        target.outputStream().use { destination -> source.copyTo(destination) }
                    }
                    if (!target.isFile || target.length() == 0L) {
                        target.delete()
                        throw IOException("The selected APK is empty.")
                    }
                    target
                }
                copyingApk = false
                finishWithResult(copiedFile.absolutePath)
            } catch (error: Exception) {
                copyingApk = false
                Toast.makeText(
                    this@ListActivity,
                    "Couldn't read that APK: ${error.message ?: "Unknown error"}",
                    Toast.LENGTH_LONG,
                ).show()
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
