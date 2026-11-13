package top.niunaijun.blackboxa.view.fake

import android.app.Activity
import android.content.res.Configuration as AndroidConfiguration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceManager
import java.io.File

import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import top.niunaijun.blackbox.entity.location.BLocation
import top.niunaijun.blackboxa.app.ProfileAppearance
import top.niunaijun.blackboxa.databinding.ActivityOsmdroidBinding
import top.niunaijun.blackboxa.util.inflate

class FollowMyLocationOverlay : AppCompatActivity() {

    companion object {
        private const val TAG = "FakeLocationPicker"
    }

    private val binding: ActivityOsmdroidBinding by inflate()
    private var mapInitialized = false
    private var selectedPoint: GeoPoint? = null
    private var marker: Marker? = null
    private var resultSent = false
    private var darkTheme = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val systemNight = (resources.configuration.uiMode and AndroidConfiguration.UI_MODE_NIGHT_MASK) ==
            AndroidConfiguration.UI_MODE_NIGHT_YES
        darkTheme = when (ProfileAppearance.globalThemeMode()) {
            "dark" -> true
            "light" -> false
            else -> systemNight
        }
        setTheme(
            when {
                !darkTheme -> top.niunaijun.blackboxa.R.style.Theme_BlackBox
                ProfileAppearance.useAmoledDarkMode() -> top.niunaijun.blackboxa.R.style.Theme_BlackBox_Amoled
                else -> top.niunaijun.blackboxa.R.style.Theme_BlackBox_Dark
            }
        )
        super.onCreate(savedInstanceState)

        try {
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val osmdroidDirectory = File(cacheDir, "osmdroid")
            val tileCacheDirectory = File(osmdroidDirectory, "tiles")
            if (!tileCacheDirectory.exists() && !tileCacheDirectory.mkdirs()) {
                Log.w(TAG, "Could not create private map tile cache at $tileCacheDirectory")
            }

            Configuration.getInstance().apply {
                load(this@FollowMyLocationOverlay, preferences)
                setOsmdroidBasePath(osmdroidDirectory)
                setOsmdroidTileCache(tileCacheDirectory)
                setUserAgentValue(packageName)
            }
            setContentView(binding.root)
        configureSystemBars()

        val savedLocation: BLocation? = intent.getParcelableExtra("location")
        selectedPoint = savedLocation?.let { GeoPoint(it.latitude, it.longitude) }

        val appName = intent.getStringExtra("appName").orEmpty()
        binding.toolbar.title = "Choose location"
        binding.toolbar.subtitle = appName.takeIf { it.isNotBlank() }
        binding.toolbar.setNavigationOnClickListener { cancelSelection() }
        binding.useLocation.isEnabled = selectedPoint != null
        binding.useLocation.setOnClickListener {
            selectedPoint?.let(::finishWithResult)
        }
        binding.instruction.text = if (savedLocation == null) {
            "Tap the map to place a pin. Nothing changes until you press Use this location."
        } else {
            "Move the pin if needed, then press Use this location to apply it."
        }

        marker = savedLocation?.let { location ->
            Marker(binding.map).apply {
                position = GeoPoint(location.latitude, location.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Selected location"
            }
        }
        marker?.let(binding.map.overlays::add)

        val mapEvents = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                selectedPoint = point
                val activeMarker = marker ?: Marker(binding.map).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Selected location"
                    marker = this
                    binding.map.overlays.add(this)
                }
                activeMarker.position = point
                binding.useLocation.isEnabled = true
                binding.map.invalidate()
                binding.instruction.text =
                    "Selected: ${point.latitude}, ${point.longitude}. Press Use this location to apply it."
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean = false
        }
        binding.map.overlays.add(MapEventsOverlay(mapEvents))
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(if (savedLocation == null) 2.5 else 12.5)
        binding.map.controller.setCenter(selectedPoint ?: GeoPoint(20.0, 0.0))
            mapInitialized = true
        } catch (error: Exception) {
            Log.e(TAG, "Could not open the fake-location map", error)
            android.widget.Toast.makeText(
                this,
                "Couldn't open the location map. Please try again.",
                android.widget.Toast.LENGTH_LONG,
            ).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            val extraBottom = (12 * density).toInt()
            binding.toolbar.updatePadding(top = bars.top)
            binding.locationControls.updatePadding(bottom = bars.bottom + extraBottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    override fun onResume() {
        super.onResume()
        if (mapInitialized) {
            runCatching { binding.map.onResume() }
                .onFailure { Log.w(TAG, "Map resume failed", it) }
        }
    }

    override fun onPause() {
        if (mapInitialized) {
            runCatching { binding.map.onPause() }
                .onFailure { Log.w(TAG, "Map pause failed", it) }
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (mapInitialized) {
            runCatching { binding.map.onDetach() }
                .onFailure { Log.w(TAG, "Map detach failed", it) }
        }
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        cancelSelection()
    }

    private fun cancelSelection() {
        if (resultSent) return
        resultSent = true
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun finishWithResult(point: GeoPoint) {
        if (resultSent) return
        resultSent = true
        intent.putExtra("latitude", point.latitude)
        intent.putExtra("longitude", point.longitude)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
