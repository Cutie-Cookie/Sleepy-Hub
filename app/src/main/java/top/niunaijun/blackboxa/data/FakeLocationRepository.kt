package top.niunaijun.blackboxa.data

import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.core.GmsCore
import top.niunaijun.blackbox.entity.location.BLocation
import top.niunaijun.blackbox.fake.frameworks.BLocationManager
import top.niunaijun.blackboxa.bean.FakeLocationBean
import kotlin.math.abs


class FakeLocationRepository {
    val TAG: String = "FakeLocationRepository"

    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        BLocationManager.get().setPattern(userId, pkg, pattern)
    }

    private fun getPattern(userId: Int, pkg: String): Int {
        return BLocationManager.get().getPattern(userId, pkg)
    }

    private fun getLocation(userId: Int, pkg: String): BLocation? {
        return BLocationManager.get().getLocation(userId, pkg)
    }

    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        BLocationManager.get().setLocation(userId, pkg, location)
    }

    fun applyFakeLocation(userId: Int, pkg: String, location: BLocation): Boolean {
        val manager = BLocationManager.get()
        if (!manager.trySetLocation(userId, pkg, location)) return false
        if (!manager.trySetPattern(userId, pkg, BLocationManager.OWN_MODE)) return false

        val storedLocation = manager.getLocation(userId, pkg) ?: return false
        return manager.getPattern(userId, pkg) == BLocationManager.OWN_MODE &&
            abs(storedLocation.latitude - location.latitude) < 0.000001 &&
            abs(storedLocation.longitude - location.longitude) < 0.000001
    }

    fun disableFakeLocation(userId: Int, pkg: String): Boolean {
        val manager = BLocationManager.get()
        return manager.trySetPattern(userId, pkg, BLocationManager.CLOSE_MODE) &&
            manager.getPattern(userId, pkg) == BLocationManager.CLOSE_MODE
    }

    fun getInstalledAppList(
        userID: Int,
        appsFakeLiveData: MutableLiveData<List<FakeLocationBean>>
    ) {
        try {
            val installedList = mutableListOf<FakeLocationBean>()
            val installedApplications = BlackBoxCore.get().getInstalledApplications(0, userID)

            for (installedApplication in installedApplications) {
                val packageName = installedApplication.packageName ?: continue
                if (packageName.isBlank() || GmsCore.isGoogleAppOrService(packageName)) continue

                try {
                    val packageManager = BlackBoxCore.getPackageManager()
                    val name = runCatching {
                        packageManager.getApplicationLabel(installedApplication).toString()
                    }.getOrDefault(packageName)
                    val icon = runCatching {
                        packageManager.getApplicationIcon(installedApplication)
                    }.getOrElse {
                        ColorDrawable(Color.DKGRAY)
                    }

                    installedList.add(
                        FakeLocationBean(
                            userID,
                            name,
                            icon,
                            packageName,
                            getPattern(userID, packageName),
                            getLocation(userID, packageName)
                        )
                    )
                } catch (error: Exception) {
                    Log.w(TAG, "Skipping app $packageName in fake-location list", error)
                }
            }

            Log.d(TAG, "Loaded ${installedList.size} fake-location targets for profile $userID")
            appsFakeLiveData.postValue(installedList)
        } catch (error: Exception) {
            Log.e(TAG, "Could not load fake-location targets for profile $userID", error)
            appsFakeLiveData.postValue(emptyList())
        }
    }
}