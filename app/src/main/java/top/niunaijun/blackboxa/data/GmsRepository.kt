package top.niunaijun.blackboxa.data

import androidx.lifecycle.MutableLiveData
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.bean.GmsBean
import top.niunaijun.blackboxa.bean.GmsInstallBean
import top.niunaijun.blackboxa.util.getString

class GmsRepository {

    fun getGmsInstalledList(
        containerId: String,
        installedLiveData: MutableLiveData<List<GmsBean>>,
    ) {
        val sortedUsers = BlackBoxCore.get().users.sortedBy { it.id }
        val indexByUserId = sortedUsers.withIndex().associate { it.value.id to it.index }
        val profiles = sortedUsers
            .filter { ProfileAppearance.profileContainer(it.id) == containerId }
            .map { user ->
                GmsBean(
                    userID = user.id,
                    userName = ProfileAppearance.profileName(user.id, indexByUserId[user.id] ?: user.id),
                    isInstalledGms = BlackBoxCore.get().isInstallGms(user.id),
                    avatarUri = ProfileAppearance.profileAvatarUri(user.id),
                )
            }
        installedLiveData.postValue(profiles)
    }

    fun installGms(
        userID: Int,
        updateLiveData: MutableLiveData<GmsInstallBean>,
    ) {
        val result = try {
            BlackBoxCore.get().installGms(userID)
        } catch (error: Exception) {
            val detail = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
            updateLiveData.postValue(
                GmsInstallBean(userID, false, getString(R.string.install_fail, detail)),
            )
            return
        }
        val message = if (result.success) {
            getString(R.string.install_success)
        } else {
            getString(R.string.install_fail, result.msg)
        }
        updateLiveData.postValue(GmsInstallBean(userID, result.success, message))
    }

    fun uninstallGms(
        userID: Int,
        updateLiveData: MutableLiveData<GmsInstallBean>,
    ) {
        val success = try {
            BlackBoxCore.get().isInstallGms(userID) &&
                BlackBoxCore.get().uninstallGms(userID)
        } catch (error: Exception) {
            val detail = error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
            updateLiveData.postValue(
                GmsInstallBean(userID, false, "${getString(R.string.uninstall_fail)}: $detail"),
            )
            return
        }
        val message = if (success) {
            getString(R.string.uninstall_success)
        } else {
            getString(R.string.uninstall_fail)
        }
        updateLiveData.postValue(GmsInstallBean(userID, success, message))
    }
}
