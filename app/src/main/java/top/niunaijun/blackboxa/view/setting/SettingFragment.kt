package top.niunaijun.blackboxa.view.setting

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.AppManager
import top.niunaijun.blackboxa.util.toast
import top.niunaijun.blackboxa.view.gms.GmsManagerActivity

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        initAppearance()
        initGms()
        invalidHideState {
            val preference: Preference = findPreference("root_hide")!!
            preference.setDefaultValue(AppManager.mBlackBoxLoader.hideRoot())
            preference
        }
        invalidHideState {
            val preference: Preference = findPreference("daemon_enable")!!
            preference.setDefaultValue(AppManager.mBlackBoxLoader.daemonEnable())
            preference
        }
        invalidHideState {
            val preference: Preference = findPreference("disable_flag_secure")!!
            preference.setDefaultValue(AppManager.mBlackBoxLoader.disableFlagSecure())
            preference
        }
    }

    private fun initAppearance() {
        findPreference<ListPreference>("appearance_mode")?.setOnPreferenceChangeListener { _, value ->
            val mode = when (value as? String) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            true
        }
        findPreference<ListPreference>("global_accent")?.setOnPreferenceChangeListener { _, _ ->
            Handler(Looper.getMainLooper()).postDelayed({ activity?.recreate() }, 120L)
            true
        }
        findPreference<ListPreference>("app_grid_columns")?.setOnPreferenceChangeListener { _, _ ->
            Handler(Looper.getMainLooper()).postDelayed({ activity?.recreate() }, 120L)
            true
        }
    }

    private fun initGms() {
        val preference: Preference = findPreference("gms_manager")!!
        if (BlackBoxCore.get().isSupportGms) {
            preference.setOnPreferenceClickListener {
                GmsManagerActivity.start(requireContext())
                true
            }
        } else {
            preference.summary = getString(R.string.no_gms)
            preference.isEnabled = false
        }
    }

    private fun invalidHideState(block: () -> Preference) {
        val preference = block()
        preference.setOnPreferenceChangeListener { changedPreference, newValue ->
            val enabled = newValue == true
            when (changedPreference.key) {
                "root_hide" -> AppManager.mBlackBoxLoader.invalidHideRoot(enabled)
                "daemon_enable" -> AppManager.mBlackBoxLoader.invalidDaemonEnable(enabled)
                "disable_flag_secure" -> AppManager.mBlackBoxLoader.invalidDisableFlagSecure(enabled)
            }
            toast(R.string.restart_module)
            true
        }
    }
}
