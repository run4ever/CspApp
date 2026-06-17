package fr.csp.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

actual suspend fun compressImageToJpeg(bytes: ByteArray, maxPx: Int, quality: Int): ByteArray {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
    val scale = minOf(maxPx.toFloat() / bitmap.width, maxPx.toFloat() / bitmap.height, 1f)
    val resized = if (scale < 1f) {
        val w = (bitmap.width * scale).toInt()
        val h = (bitmap.height * scale).toInt()
        Bitmap.createScaledBitmap(bitmap, w, h, true).also { bitmap.recycle() }
    } else bitmap
    return ByteArrayOutputStream().use { out ->
        resized.compress(Bitmap.CompressFormat.JPEG, quality, out)
        resized.recycle()
        out.toByteArray()
    }
}
