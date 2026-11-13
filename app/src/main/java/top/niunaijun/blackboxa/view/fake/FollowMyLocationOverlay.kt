package top.niunaijun.blackboxa.view.fake

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import top.niunaijun.blackbox.entity.location.BLocation
import top.niunaijun.blackboxa.databinding.ActivityOsmdroidBinding
import top.niunaijun.blackboxa.util.inflate

class FollowMyLocationOverlay : AppCompatActivity() {

    private val binding: ActivityOsmdroidBinding by inflate()
    private lateinit var selectedPoint: GeoPoint
    private var resultSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this),
        )
        setContentView(binding.root)
        configureSystemBars()

        val savedLocation: BLocation? = intent.getParcelableExtra("location")
        selectedPoint = savedLocation?.let {
            GeoPoint(it.latitude, it.longitude)
        } ?: GeoPoint(20.0, 0.0)

        val appName = intent.getStringExtra("appName").orEmpty()
        binding.toolbar.title = "Choose location"
        binding.toolbar.subtitle = appName.takeIf { it.isNotBlank() }
        binding.toolbar.setNavigationOnClickListener { finishWithResult(selectedPoint) }
        binding.useLocation.setOnClickListener { finishWithResult(selectedPoint) }

        val marker = Marker(binding.map).apply {
            position = selectedPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Selected location"
        }
        binding.map.overlays.add(marker)

        val mapEvents = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                selectedPoint = point
                marker.position = point
                marker.title = "Selected location"
                binding.map.invalidate()
                binding.instruction.text =
                    "Selected: ${point.latitude}, ${point.longitude}. Tap again to move the pin, then press Use this location."
                return true
            }

            override fun longPressHelper(point: GeoPoint): Boolean = false
        }
        binding.map.overlays.add(MapEventsOverlay(mapEvents))
        binding.map.setTileSource(TileSourceFactory.MAPNIK)
        binding.map.setMultiTouchControls(true)
        binding.map.controller.setZoom(if (savedLocation == null) 2.5 else 12.5)
        binding.map.controller.setCenter(selectedPoint)
    }

    private fun configureSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
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
        binding.map.onResume()
    }

    override fun onPause() {
        binding.map.onPause()
        super.onPause()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finishWithResult(selectedPoint)
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
