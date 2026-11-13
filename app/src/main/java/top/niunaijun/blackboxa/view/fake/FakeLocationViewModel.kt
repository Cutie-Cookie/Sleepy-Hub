package top.niunaijun.blackboxa.view.fake

import androidx.lifecycle.MutableLiveData
import top.niunaijun.blackbox.entity.location.BLocation
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.bean.FakeLocationBean
import top.niunaijun.blackboxa.bean.FakeLocationBeanInstallBean
import top.niunaijun.blackboxa.util.getString
import top.niunaijun.blackboxa.data.FakeLocationRepository
import top.niunaijun.blackboxa.view.base.BaseViewModel


class FakeLocationViewModel(private val mRepo: FakeLocationRepository) : BaseViewModel() {

    val appsLiveData = MutableLiveData<List<FakeLocationBean>>()
    val actionLiveData = MutableLiveData<FakeLocationBeanInstallBean?>()

    fun clearActionResult() {
        actionLiveData.value = null
    }


    fun getInstallAppList(userID: Int) {
        launchOnUI {
            mRepo.getInstalledAppList(userID, appsLiveData)
        }
    }

    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        launchOnUI {
            mRepo.setPattern(userId, pkg, pattern)
        }
    }

    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        launchOnUI {
            mRepo.setLocation(userId, pkg, location)
        }
    }

    fun applyFakeLocation(userId: Int, pkg: String, location: BLocation) {
        launchOnUI {
            val success = runCatching {
                mRepo.applyFakeLocation(userId, pkg, location)
            }.getOrDefault(false)
            val message = if (success) {
                getString(R.string.set_location, location.latitude.toString(), location.longitude.toString())
            } else {
                getString(R.string.set_location_failed)
            }
            actionLiveData.postValue(FakeLocationBeanInstallBean(userId, success, message))
            mRepo.getInstalledAppList(userId, appsLiveData)
        }
    }

    fun disableFakeLocation(userId: Int, pkg: String, appName: String) {
        launchOnUI {
            val success = runCatching {
                mRepo.disableFakeLocation(userId, pkg)
            }.getOrDefault(false)
            val message = if (success) {
                getString(R.string.close_fake_location_success, appName)
            } else {
                getString(R.string.close_fake_location_failed, appName)
            }
            actionLiveData.postValue(FakeLocationBeanInstallBean(userId, success, message))
            mRepo.getInstalledAppList(userId, appsLiveData)
        }
    }

}