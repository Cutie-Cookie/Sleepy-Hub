package top.niunaijun.blackboxa.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import top.niunaijun.blackbox.BlackBoxCore

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        @JvmStatic
        fun getContext(): Context = mContext
    }

    override fun attachBaseContext(base: Context?) {
        try {
            super.attachBaseContext(base)
            try {
                BlackBoxCore.get().closeCodeInit()
            } catch (e: Exception) {
                Log.e("App", "Error in closeCodeInit: " + e.message)
            }
            try {
                BlackBoxCore.get().onBeforeMainApplicationAttach(this, base)
            } catch (e: Exception) {
                Log.e("App", "Error in onBeforeMainApplicationAttach: " + e.message)
            }
            mContext = base!!
            try {
                AppManager.doAttachBaseContext(base)
            } catch (e: Exception) {
                Log.e("App", "Error in doAttachBaseContext: " + e.message)
            }
            try {
                BlackBoxCore.get().onAfterMainApplicationAttach(this, base)
            } catch (e: Exception) {
                Log.e("App", "Error in onAfterMainApplicationAttach: " + e.message)
            }
        } catch (e: Exception) {
            Log.e("App", "Critical error in attachBaseContext: " + e.message)
            if (base != null) mContext = base
        }
    }

    override fun onCreate() {
        try {
            super.onCreate()
            val appearance = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("appearance_mode", "dark")
            val nightMode = when (appearance) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)
            AppManager.doOnCreate(mContext)
        } catch (e: Exception) {
            Log.e("App", "Error in onCreate: " + e.message)
        }
    }
}
