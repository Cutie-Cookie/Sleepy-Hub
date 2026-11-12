package top.niunaijun.blackboxa.view.apps

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.bean.AppInfo
import top.niunaijun.blackboxa.databinding.FragmentAppsBinding
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.util.ProfileImageLoader
import top.niunaijun.blackboxa.util.ShortcutUtil
import top.niunaijun.blackboxa.util.inflate
import top.niunaijun.blackboxa.util.toast
import top.niunaijun.blackboxa.view.base.LoadingActivity
import top.niunaijun.blackboxa.view.main.MainActivity
import java.util.*



class AppsFragment : Fragment() {

    var userID: Int = 0

    private lateinit var viewModel: AppsViewModel

    private lateinit var mAdapter: RVAdapter<AppInfo>

    private val viewBinding: FragmentAppsBinding by inflate()

    private var popupMenu: PopupMenu? = null

    companion object {
        private const val TAG = "AppsFragment"
        
        fun newInstance(userID:Int): AppsFragment {
            val fragment = AppsFragment()
            val bundle = bundleOf("userID" to userID)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            viewModel =
                ViewModelProvider(this, InjectionUtil.getAppsFactory()).get(AppsViewModel::class.java)
            userID = requireArguments().getInt("userID", 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            viewBinding.stateView.showEmpty()

            mAdapter =
                RVAdapter<AppInfo>(requireContext(), AppsAdapter()).bind(viewBinding.recyclerView)

            viewBinding.recyclerView.adapter = mAdapter
            
            
            val columns = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("app_grid_columns", "4")?.toIntOrNull()?.coerceIn(3, 5) ?: 4
            val layoutManager = GridLayoutManager(requireContext(), columns)
            layoutManager.isItemPrefetchEnabled = true
            layoutManager.initialPrefetchItemCount = 8
            viewBinding.recyclerView.layoutManager = layoutManager
            
            
            viewBinding.recyclerView.setItemViewCacheSize(20)
            viewBinding.recyclerView.setHasFixedSize(true)
            
            
            val touchCallBack = AppsTouchCallBack { from, to ->
                try {
                    onItemMove(from, to)
                    viewModel.updateSortLiveData.postValue(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in touch callback: ${e.message}")
                }
            }

            val itemTouchHelper = ItemTouchHelper(touchCallBack)
            itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

            mAdapter.setItemClickListener { _, data, _ ->
                try {
                    showLoading()
                    viewModel.launchApk(data.packageName, userID)
                } catch (e: Exception) {
                    Log.e(TAG, "Error launching app: ${e.message}")
                    hideLoading()
                }
            }

            setOnLongClick()
            return viewBinding.root
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreateView: ${e.message}")
            
            return View(requireContext())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            initData()
            refreshProfileHeader()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}")
        }
    }

    override fun onStart() {
        try {
            super.onStart()
            
            
            try {
                BlackBoxCore.get().addServiceAvailableCallback {
                    Log.d(TAG, "Services became available, refreshing app list")
                    
                    viewModel.getInstalledAppsWithRetry(userID)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering service available callback: ${e.message}")
            }
            
            viewModel.getInstalledAppsWithRetry(userID)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStart: ${e.message}")
        }
    }

    
    private fun onItemMove(fromPosition: Int, toPosition: Int) {
        try {
            
            val items = mAdapter.getItems()
            if (fromPosition < 0 || toPosition < 0 || 
                fromPosition >= items.size || toPosition >= items.size) {
                Log.w(TAG, "Invalid positions for move: from=$fromPosition, to=$toPosition, size=${items.size}")
                return
            }
            
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    try {
                        Collections.swap(items, i, i + 1)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error swapping items at position $i: ${e.message}")
                        return
                    }
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    try {
                        Collections.swap(items, i, i - 1)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error swapping items at position $i: ${e.message}")
                        return
                    }
                }
            }
            
            try {
                mAdapter.notifyItemMoved(fromPosition, toPosition)
            } catch (e: Exception) {
                Log.e(TAG, "Error notifying item moved: ${e.message}")
                
                mAdapter.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onItemMove: ${e.message}")
        }
    }

    private fun setOnLongClick() {
        try {
            mAdapter.setItemLongClickListener { view, data, _ ->
                try {
                    popupMenu = PopupMenu(requireContext(),view).also {
                        it.inflate(R.menu.app_menu)
                        it.setOnMenuItemClickListener { item ->
                            try {
                                when (item.itemId) {
                                    R.id.app_remove -> {
                                        if (data.isXpModule) {
                                            toast(R.string.uninstall_module_toast)
                                        } else {
                                            unInstallApk(data)
                                        }
                                    }

                                    R.id.app_clear -> {
                                        clearApk(data)
                                    }

                                    R.id.app_clear_cache -> {
                                        clearApkCache(data)
                                    }

                                    R.id.app_info -> {
                                        showAppInfo(data)
                                    }

                                    R.id.app_stop -> {
                                        stopApk(data)
                                    }

                                    R.id.app_shortcut -> {
                                        ShortcutUtil.createShortcut(requireContext(), userID, data)
                                    }
                                }
                                return@setOnMenuItemClickListener true
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in menu item click: ${e.message}")
                                return@setOnMenuItemClickListener false
                            }
                        }
                        it.show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in long click: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setOnLongClick: ${e.message}")
        }
    }
    
    private fun initData() {
        try {
            viewBinding.stateView.showLoading()
            viewModel.appsLiveData.observe(viewLifecycleOwner) {
                try {
                    if (it != null) {
                        mAdapter.setItems(it)
                        ProfileAppearance.setInstalledAppCount(userID, it.size)
                        (requireActivity() as? MainActivity)?.refreshDashboard()
                        viewBinding.installedAppCount.text = it.size.toString() + " apps"
                        viewBinding.profileSubtitle.text = it.size.toString() + " installed apps"
                        if (it.isEmpty()) {
                            viewBinding.stateView.showEmpty()
                        } else {
                            viewBinding.stateView.showContent()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing apps data: ${e.message}")
                }
            }

            viewModel.resultLiveData.observe(viewLifecycleOwner) {
                try {
                    if (!TextUtils.isEmpty(it)) {
                        hideLoading()
                        requireContext().toast(it)
                        viewModel.getInstalledApps(userID)
                        scanUser()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing result data: ${e.message}")
                }
            }

            viewModel.launchLiveData.observe(viewLifecycleOwner) {
                try {
                    it?.run {
                        hideLoading()
                        if (!it) {
                            toast(R.string.start_fail)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing launch data: ${e.message}")
                }
            }

            viewModel.updateSortLiveData.observe(viewLifecycleOwner) {
                try {
                    if (this::mAdapter.isInitialized) {
                        viewModel.updateApkOrder(userID, mAdapter.getItems())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error observing sort data: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in initData: ${e.message}")
        }
    }

    override fun onStop() {
        try {
            super.onStop()
            viewModel.resultLiveData.value = null
            viewModel.launchLiveData.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStop: ${e.message}")
        }
    }

    fun refreshProfileHeader() {
        if (!isAdded || view == null) return
        try {
            viewBinding.profileName.text = ProfileAppearance.profileName(userID)
            viewBinding.profileHeader.setCardBackgroundColor(ProfileAppearance.accent(userID))
            val background = ProfileAppearance.backgroundUri(userID)
            if (background.isNullOrBlank()) {
                ProfileImageLoader.clear(viewBinding.profileBackground)
                val accent = ProfileAppearance.accent(userID)
                viewBinding.profileOverlay.background = android.graphics.drawable.GradientDrawable(
                    android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                    intArrayOf(accent, android.graphics.Color.BLACK)
                )
            } else {
                viewBinding.profileBackground.alpha = ProfileAppearance.backgroundOpacity(userID)
                viewBinding.profileOverlay.setBackgroundResource(R.drawable.profile_header_overlay)
                ProfileImageLoader.load(requireContext(), background, viewBinding.profileBackground)
            }
            viewBinding.profileMenu.setOnClickListener { anchor ->
                (requireActivity() as? MainActivity)?.showProfileOptions(userID, anchor)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing profile header: " + e.message)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshProfileHeader()
    }

    private fun clearApkCache(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_clear_cache)
            message(text = getString(R.string.app_clear_cache_hint, info.name))
            positiveButton(R.string.done) {
                try {
                    showLoading()
                    viewModel.clearApkCache(info.packageName, userID)
                } catch (e: Exception) {
                    Log.e(TAG, "Error clearing app cache: " + e.message)
                    hideLoading()
                }
            }
            negativeButton(R.string.cancel)
        }
    }

    private fun showAppInfo(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_info)
            message(text = getString(R.string.app_info_message, info.name, info.packageName, info.sourceDir))
            positiveButton(R.string.done)
        }
    }

    private fun unInstallApk(info: AppInfo) {
        try {
            MaterialDialog(requireContext()).show {
                title(R.string.uninstall_app)
                message(text = getString(R.string.uninstall_app_hint, info.name))
                positiveButton(R.string.done) {
                    try {
                        showLoading()
                        viewModel.unInstall(info.packageName, userID)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error uninstalling app: ${e.message}")
                        hideLoading()
                    }
                }
                negativeButton(R.string.cancel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing uninstall dialog: ${e.message}")
        }
    }

    
    private fun stopApk(info: AppInfo) {
        try {
            MaterialDialog(requireContext()).show {
                title(R.string.app_stop)
                message(text = getString(R.string.app_stop_hint,info.name))
                positiveButton(R.string.done) {
                    try {
                        BlackBoxCore.get().stopPackage(info.packageName, userID)
                        toast(getString(R.string.is_stop,info.name))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping app: ${e.message}")
                    }
                }
                negativeButton(R.string.cancel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing stop dialog: ${e.message}")
        }
    }

    
    private fun clearApk(info: AppInfo) {
        try {
            MaterialDialog(requireContext()).show {
                title(R.string.app_clear)
                message(text = getString(R.string.app_clear_hint,info.name))
                positiveButton(R.string.done) {
                    try {
                        showLoading()
                        viewModel.clearApkData(info.packageName, userID)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error clearing app data: ${e.message}")
                        hideLoading()
                    }
                }
                negativeButton(R.string.cancel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing clear dialog: ${e.message}")
        }
    }

    fun installApk(source: String) {
        try {
            showLoading()
            viewModel.install(source, userID)
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK: ${e.message}")
            hideLoading()
        }
    }

    private fun scanUser() {
        try {
            (requireActivity() as? MainActivity)?.scanUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning user: ${e.message}")
        }
    }

    private fun showLoading() {
        try {
            if(requireActivity() is LoadingActivity){
                (requireActivity() as LoadingActivity).showLoading()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading: ${e.message}")
        }
    }

    private fun hideLoading() {
        try {
            if(requireActivity() is LoadingActivity){
                (requireActivity() as LoadingActivity).hideLoading()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding loading: ${e.message}")
        }
    }
}
