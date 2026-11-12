package top.niunaijun.blackboxa.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import java.util.concurrent.Executors

/**
 * Decodes profile wallpapers off the main thread and samples them near their
 * display size to avoid repeatedly decoding full-resolution camera photos.
 */
object ProfileImageLoader {
    private val executor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun clear(target: ImageView) {
        target.tag = null
        target.setImageDrawable(null)
    }

    fun load(context: Context, uriString: String?, target: ImageView) {
        if (uriString.isNullOrBlank()) {
            clear(target)
            return
        }

        target.tag = uriString
        val appContext = context.applicationContext
        val width = target.width.takeIf { it > 0 }
            ?: appContext.resources.displayMetrics.widthPixels
        val height = target.height.takeIf { it > 0 }
            ?: (width * 0.65f).toInt()

        executor.execute {
            val bitmap = decodeSampled(appContext, Uri.parse(uriString), width, height)
            mainHandler.post {
                if (target.tag == uriString) {
                    if (bitmap != null) {
                        target.setImageBitmap(bitmap)
                    } else {
                        target.setImageDrawable(null)
                    }
                }
            }
        }
    }

    private fun decodeSampled(context: Context, uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            } ?: return null

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            var sampleSize = 1
            while (bounds.outWidth / sampleSize > targetWidth * 2 ||
                bounds.outHeight / sampleSize > targetHeight * 2
            ) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } catch (_: Exception) {
            null
        }
    }
}
