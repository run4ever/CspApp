package fr.csp.app.util

expect suspend fun compressImageToJpeg(bytes: ByteArray, maxPx: Int = 512, quality: Int = 85): ByteArray
