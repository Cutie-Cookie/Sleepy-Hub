package top.niunaijun.blackboxa.data

import android.content.pm.ApplicationInfo
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.io.File
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.core.GmsCore
import top.niunaijun.blackbox.core.env.BEnvironment
import top.niunaijun.blackbox.utils.AbiUtils
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.AppManager
import top.niunaijun.blackboxa.bean.AppInfo
import top.niunaijun.blackboxa.bean.InstalledAppBean
import top.niunaijun.blackboxa.util.MemoryManager
import top.niunaijun.blackboxa.util.getString


class AppsRepository {
    val TAG: String = "AppsRepository"
    private var mInstalledList = mutableListOf<AppInfo>()

    
    private fun safeLoadAppLabel(applicationInfo: ApplicationInfo): String {
        return try {
            BlackBoxCore.getPackageManager().getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load label for ${applicationInfo.packageName}: ${e.message}")
            applicationInfo.packageName 
        }
    }

    
    private fun safeLoadAppIcon(
            applicationInfo: ApplicationInfo
    ): android.graphics.drawable.Drawable? {
        return try {
            
            val icon = BlackBoxCore.getPackageManager().getApplicationIcon(applicationInfo)

            
            if (icon is android.graphics.drawable.BitmapDrawable) {
                val bitmap = icon.bitmap
                
                if (bitmap.width > 96 || bitmap.height > 96) {
                    try {
                        val scaledBitmap =
                                android.graphics.Bitmap.createScaledBitmap(bitmap, 96, 96, true)
                        android.graphics.drawable.BitmapDrawable(
                                BlackBoxCore.getPackageManager()
                                        .getResourcesForApplication(applicationInfo.packageName),
                                scaledBitmap
                        )
                    } catch (e: Exception) {
                        Log.w(
                                TAG,
                                "Failed to scale icon for ${applicationInfo.packageName}: ${e.message}"
                        )
                        icon
                    }
                } else {
                    icon
                }
            } else {
                icon
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load icon for ${applicationInfo.packageName}: ${e.message}")
            null 
        }
    }

    fun previewInstallList() {
        try {
            synchronized(mInstalledList) {
                val installedApplications: List<ApplicationInfo> =
                        BlackBoxCore.getPackageManager().getInstalledApplications(0)
                val installedList = mutableListOf<AppInfo>()

                for (installedApplication in installedApplications) {
                    try {
                        val file = File(installedApplication.sourceDir)

                        if ((installedApplication.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue
                        if (GmsCore.isGoogleAppOrService(installedApplication.packageName)) continue
                        if (!AbiUtils.isSupport(file)) continue

                        
                        if (BlackBoxCore.get().isBlackBoxApp(installedApplication.packageName)) {
                            Log.d(
                                    TAG,
                                    "Filtering out BlackBox app: ${installedApplication.packageName}"
                            )
                            continue
                        }

                        val isXpModule = false

                        val info =
                                AppInfo(
                                        safeLoadAppLabel(installedApplication),
                                        safeLoadAppIcon(
                                                installedApplication
                                        ), 
                                        installedApplication.packageName,
                                        installedApplication.sourceDir,
                                        isXpModule
                                )
                        installedList.add(info)
                    } catch (e: Exception) {
                        Log.e(
                                TAG,
                                "Error processing app ${installedApplication.packageName}: ${e.message}"
                        )
                    }
                }
                this.mInstalledList.clear()
                this.mInstalledList.addAll(installedList)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in previewInstallList: ${e.message}")
        }
    }

    fun getInstalledAppList(
            userID: Int,
            loadingLiveData: MutableLiveData<Boolean>,
            appsLiveData: MutableLiveData<List<InstalledAppBean>>
    ) {
        try {
            loadingLiveData.postValue(true)
            synchronized(mInstalledList) {
                val blackBoxCore = BlackBoxCore.get()
                Log.d(TAG, mInstalledList.joinToString(","))
                val newInstalledList =
                        mInstalledList.map {
                            InstalledAppBean(
                                    it.name,
                                    it.icon, 
                                    it.packageName,
                                    it.sourceDir,
                                    blackBoxCore.isInstalled(it.packageName, userID)
                            )
                        }
                appsLiveData.postValue(newInstalledList)
                loadingLiveData.postValue(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getInstalledAppList: ${e.message}")
            loadingLiveData.postValue(false)
            appsLiveData.postValue(emptyList())
        }
    }

    fun getVmInstallList(userId: Int, appsLiveData: MutableLiveData<List<AppInfo>>) {
        try {
            
            val blackBoxCore = BlackBoxCore.get()

            
            val users = blackBoxCore.users
            Log.d(TAG, "getVmInstallList: userId=$userId, total users=${users.size}")
            users.forEach { user -> Log.d(TAG, "User: id=${user.id}, name=${user.name}") }

            val sortListData = AppManager.mRemarkSharedPreferences.getString("AppList$userId", "")
            val sortList = sortListData?.split(",")

            
            var applicationList: List<ApplicationInfo>? = null
            var retryCount = 0
            val maxRetries = 3

            while (applicationList == null && retryCount < maxRetries) {
                try {
                    applicationList = blackBoxCore.getInstalledApplications(0, userId)
                    if (applicationList == null) {
                        Log.w(
                                TAG,
                                "getVmInstallList: Attempt ${retryCount + 1} returned null, retrying..."
                        )
                        retryCount++
                        Thread.sleep(100) 
                    }
                } catch (e: Exception) {
                    Log.e(
                            TAG,
                            "getVmInstallList: Error getting applications on attempt ${retryCount + 1}: ${e.message}"
                    )
                    retryCount++
                    if (retryCount < maxRetries) {
                        Thread.sleep(200) 
                    }
                }
            }

            
            if (applicationList == null) {
                Log.e(
                        TAG,
                        "getVmInstallList: applicationList is null for userId=$userId after $maxRetries attempts"
                )
                appsLiveData.postValue(emptyList())
                return
            }

            
            Log.d(
                    TAG,
                    "getVmInstallList: userId=$userId, applicationList.size=${applicationList.size}"
            )
            if (applicationList.isNotEmpty()) {
                Log.d(TAG, "First app: ${applicationList.first().packageName}")
            } else {
                Log.w(TAG, "getVmInstallList: No applications found for userId=$userId")
            }

            val appInfoList = mutableListOf<AppInfo>()

            
            val sortedApplicationList =
                    if (!sortList.isNullOrEmpty()) {
                        try {
                            applicationList.sortedWith(AppsSortComparator(sortList))
                        } catch (e: Exception) {
                            Log.e(TAG, "getVmInstallList: Error sorting applications: ${e.message}")
                            applicationList 
                        }
                    } else {
                        applicationList
                    }

            
            sortedApplicationList.forEachIndexed { index, applicationInfo ->
                try {
                    
                    if (index > 0 && index % 25 == 0) {
                    }

                    
                    if (applicationInfo == null) {
                        Log.w(
                                TAG,
                                "getVmInstallList: Skipping null applicationInfo at index $index"
                        )
                        return@forEachIndexed
                    }

                    
                    if (applicationInfo.packageName.isNullOrBlank()) {
                        Log.w(
                                TAG,
                                "getVmInstallList: Skipping app with null/blank package name at index $index"
                        )
                        return@forEachIndexed
                    }
                    if (GmsCore.isGoogleAppOrService(applicationInfo.packageName)) {
                        return@forEachIndexed
                    }

                    val info =
                            AppInfo(
                                    safeLoadAppLabel(applicationInfo),
                                    safeLoadAppIcon(
                                            applicationInfo
                                    ), 
                                    applicationInfo.packageName,
                                    applicationInfo.sourceDir ?: "",
                                    false
                            )

                    appInfoList.add(info)

                    
                    if (index > 0 && index % 50 == 0) {
                        Log.d(
                                TAG,
                                "getVmInstallList: Processed $index/${sortedApplicationList.size} apps - ${MemoryManager.getMemoryInfo()}"
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                            TAG,
                            "getVmInstallList: Error processing app at index $index (${applicationInfo?.packageName}): ${e.message}"
                    )
                    
                }
            }

            Log.d(
                    TAG,
                    "getVmInstallList: processed ${appInfoList.size} apps - ${MemoryManager.getMemoryInfo()}"
            )

            
            
            if (appInfoList.isEmpty()) {
                Log.d(
                        TAG,
                        "getVmInstallList: No virtual apps found for userId=$userId, showing empty list (correct for new users)"
                )
            } else {
                Log.d(
                        TAG,
                        "getVmInstallList: Showing ${appInfoList.size} virtual apps for userId=$userId"
                )
            }

            
            try {
                appsLiveData.postValue(appInfoList)
            } catch (e: Exception) {
                Log.e(TAG, "getVmInstallList: Error posting to LiveData: ${e.message}")
                
                try {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        try {
                            appsLiveData.postValue(appInfoList)
                        } catch (e2: Exception) {
                            Log.e(
                                    TAG,
                                    "getVmInstallList: Fallback posting also failed: ${e2.message}"
                            )
                        }
                    }
                } catch (e3: Exception) {
                    Log.e(
                            TAG,
                            "getVmInstallList: Could not schedule fallback posting: ${e3.message}"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getVmInstallList: ${e.message}")
            try {
                appsLiveData.postValue(emptyList())
            } catch (e2: Exception) {
                Log.e(TAG, "getVmInstallList: Error posting empty list: ${e2.message}")
            }
        }
    }

    fun installApk(source: String, userId: Int, resultLiveData: MutableLiveData<String>) {
        var temporaryApk: File? = null
        try {
            val blackBoxCore = BlackBoxCore.get()
            val hostPackageName = BlackBoxCore.getHostPkg()
            val parsedUri = Uri.parse(source)
            val sourceFile = when {
                parsedUri.scheme == "file" -> parsedUri.path?.let(::File)
                File(source).isFile -> File(source)
                else -> null
            }

            // Never allow Sleepy Hub to install itself into its own virtual container.
            val archivePackageName = sourceFile?.takeIf { it.isFile }?.let { file ->
                runCatching {
                    BlackBoxCore.getPackageManager().getPackageArchiveInfo(file.absolutePath, 0)?.packageName
                }.getOrNull()
            }
            if (source == hostPackageName || archivePackageName == hostPackageName) {
                resultLiveData.postValue(
                    "Sleepy Hub cannot be installed inside its own virtual profile."
                )
                return
            }

            if (sourceFile != null) {
                temporaryApk = sourceFile.takeIf {
                    it.absolutePath.contains("/cache/apk-imports/")
                }
            }

            Log.i(
                TAG,
                "Installing into profile=$userId sourceType=${when {
                    sourceFile != null -> "apk-file"
                    parsedUri.scheme == "content" -> "content-uri"
                    parsedUri.scheme == "http" || parsedUri.scheme == "https" -> "remote-uri"
                    else -> "host-package"
                }} source=$source",
            )

            val installResult = when {
                parsedUri.scheme == "content" ||
                    parsedUri.scheme == "http" ||
                    parsedUri.scheme == "https" -> blackBoxCore.installPackageAsUser(parsedUri, userId)
                sourceFile != null && sourceFile.isFile -> blackBoxCore.installPackageAsUser(sourceFile, userId)
                else -> {
                    // A package name means "clone this host-installed app into the virtual user".
                    blackBoxCore.installPackageAsUser(source, userId)
                }
            }

            if (installResult == null) {
                Log.e(TAG, "The virtual package service returned no install result for profile=$userId")
                resultLiveData.postValue(
                    "Installation failed: the virtual package service returned no result. Refresh the profile and try again.",
                )
                return
            }

            if (installResult.success) {
                installResult.packageName?.takeIf { it.isNotBlank() }?.let {
                    updateAppSortList(userId, it, true)
                }
                resultLiveData.postValue(getString(R.string.install_success))
            } else {
                Log.e(
                    TAG,
                    "Install failed for profile=$userId package=${installResult.packageName} reason=${installResult.msg}",
                )
                resultLiveData.postValue(
                    getString(R.string.install_fail, installResult.msg?.takeIf { it.isNotBlank() } ?: "Unknown install error"),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error installing APK from '$source': ${e.message}", e)
            resultLiveData.postValue("Installation failed: ${e.message ?: "Unknown error"}")
        } finally {
            temporaryApk?.delete()
        }
    }

    fun unInstall(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        try {
            BlackBoxCore.get().uninstallPackageAsUser(packageName, userID)
            updateAppSortList(userID, packageName, false)
            resultLiveData.postValue(getString(R.string.uninstall_success))
        } catch (e: Exception) {
            Log.e(TAG, "Error uninstalling APK: ${e.message}")
            resultLiveData.postValue("Uninstallation failed: ${e.message}")
        }
    }

    fun launchApk(packageName: String, userId: Int, launchLiveData: MutableLiveData<Boolean>) {
        try {
            val result = BlackBoxCore.get().launchApk(packageName, userId)
            launchLiveData.postValue(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching APK: ${e.message}")
            launchLiveData.postValue(false)
        }
    }

    fun clearApkData(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        try {
            BlackBoxCore.get().clearPackage(packageName, userID)
            resultLiveData.postValue(getString(R.string.clear_success))
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing APK data: ${e.message}")
            resultLiveData.postValue("Clear failed: ${e.message}")
        }
    }

    fun clearApkCache(packageName: String, userId: Int, resultLiveData: MutableLiveData<String>) {
        try {
            val cacheDirectories = listOf(
                BEnvironment.getDataCacheDir(packageName, userId),
                BEnvironment.getExternalDataCacheDir(packageName, userId),
            )
            cacheDirectories.forEach { directory ->
                if (directory.exists()) {
                    directory.deleteRecursively()
                }
                directory.mkdirs()
            }
            resultLiveData.postValue("Cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing APK cache: ${e.message}")
            resultLiveData.postValue("Cache clear failed: ${e.message}")
        }
    }

    
    private fun updateAppSortList(userID: Int, pkg: String, isAdd: Boolean) {
        try {
            val savedSortList = AppManager.mRemarkSharedPreferences.getString("AppList$userID", "")

            val sortList = linkedSetOf<String>()
            if (savedSortList != null) {
                sortList.addAll(savedSortList.split(","))
            }

            if (isAdd) {
                sortList.add(pkg)
            } else {
                sortList.remove(pkg)
            }

            AppManager.mRemarkSharedPreferences.edit().apply {
                putString("AppList$userID", sortList.joinToString(","))
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app sort list: ${e.message}")
        }
    }

    
    fun updateApkOrder(userID: Int, dataList: List<AppInfo>) {
        try {
            AppManager.mRemarkSharedPreferences.edit().apply {
                putString("AppList$userID", dataList.joinToString(",") { it.packageName })
                apply()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating APK order: ${e.message}")
        }
    }
}
