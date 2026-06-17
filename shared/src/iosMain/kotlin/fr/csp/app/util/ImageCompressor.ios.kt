package fr.csp.app.util

import kotlinx.cinterop.*
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun compressImageToJpeg(bytes: ByteArray, maxPx: Int, quality: Int): ByteArray {
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    val image = UIImage(data = nsData)

    val origW = image.size.useContents { width }
    val origH = image.size.useContents { height }
    if (origW <= 0.0 || origH <= 0.0) return bytes

    val scale = minOf(maxPx / origW, maxPx / origH, 1.0)
    val newW = origW * scale
    val newH = origH * scale

    @Suppress("DEPRECATION")
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(newW, newH), false, 1.0)
    image.drawInRect(CGRectMake(0.0, 0.0, newW, newH))
    val resized = UIGraphicsGetImageFromCurrentImageContext() ?: return bytes
    UIGraphicsEndImageContext()

    val jpegNsData = UIImageJPEGRepresentation(resized, quality / 100.0) ?: return bytes

    val result = ByteArray(jpegNsData.length.toInt())
    result.usePinned { pinned ->
        memcpy(pinned.addressOf(0), jpegNsData.bytes, jpegNsData.length)
        Unit
    }
    return result
}
