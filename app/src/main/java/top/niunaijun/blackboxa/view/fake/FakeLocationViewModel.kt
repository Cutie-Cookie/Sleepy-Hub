package top.niunaijun.blackboxa.view.fake

import androidx.lifecycle.MutableLiveData
import top.niunaijun.blackbox.entity.location.BLocation
import top.niunaijun.blackboxa.bean.FakeLocationBean
import top.niunaijun.blackboxa.data.FakeLocationRepository
import top.niunaijun.blackboxa.view.base.BaseViewModel


class FakeLocationViewModel(private val mRepo: FakeLocationRepository) : BaseViewModel() {

    val appsLiveData = MutableLiveData<List<FakeLocationBean>>()


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
            // Store coordinates before enabling fake-location mode. A location request
            // cannot briefly observe an enabled mode with no selected point.
            mRepo.setLocation(userId, pkg, location)
            mRepo.setPattern(userId, pkg, top.niunaijun.blackbox.fake.frameworks.BLocationManager.OWN_MODE)
            mRepo.getInstalledAppList(userId, appsLiveData)
        }
    }

    fun disableFakeLocation(userId: Int, pkg: String) {
        launchOnUI {
            mRepo.setPattern(userId, pkg, top.niunaijun.blackbox.fake.frameworks.BLocationManager.CLOSE_MODE)
            mRepo.getInstalledAppList(userId, appsLiveData)
        }
    }

}