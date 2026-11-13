package top.niunaijun.blackboxa.view.gms

import androidx.lifecycle.MutableLiveData
import top.niunaijun.blackboxa.bean.GmsBean
import top.niunaijun.blackboxa.bean.GmsInstallBean
import top.niunaijun.blackboxa.data.GmsRepository
import top.niunaijun.blackboxa.view.base.BaseViewModel

class GmsViewModel(private val repository: GmsRepository) : BaseViewModel() {

    val installedLiveData = MutableLiveData<List<GmsBean>>()
    val updateInstalledLiveData = MutableLiveData<GmsInstallBean>()

    fun getInstalledProfiles(containerId: String) {
        launchOnUI {
            repository.getGmsInstalledList(containerId, installedLiveData)
        }
    }

    fun installGms(userID: Int) {
        launchOnUI {
            repository.installGms(userID, updateInstalledLiveData)
        }
    }

    fun uninstallGms(userID: Int) {
        launchOnUI {
            repository.uninstallGms(userID, updateInstalledLiveData)
        }
    }
}
