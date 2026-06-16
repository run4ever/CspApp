package fr.csp.app.ui.event

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlin.math.*

// Convertit des coordonnées tuile fractionnaires → lat/lon
private fun tileToLat(tileY: Double, zoom: Int): Double {
    val n = (1 shl zoom).toDouble()
    return atan(sinh(PI * (1.0 - 2.0 * tileY / n))) * 180.0 / PI
}

private fun tileToLon(tileX: Double, zoom: Int): Double {
    val n = (1 shl zoom).toDouble()
    return tileX / n * 360.0 - 180.0
}

internal expect fun ByteArray.toImageBitmap(): ImageBitmap

private val tileClient = HttpClient()

private fun lonToTileX(lon: Double, zoom: Int): Int =
    ((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()

private fun latToTileY(lat: Double, zoom: Int): Int {
    val r = lat * PI / 180.0
    return ((1.0 - ln(tan(r) + 1.0 / cos(r)) / PI) / 2.0 * (1 shl zoom)).toInt()
}

@Composable
fun MapView(lat: Double, lon: Double, label: String, modifier: Modifier = Modifier) {
    val zoom = 15
    val n = (1 shl zoom).toDouble()
    val cx = lonToTileX(lon, zoom)
    val cy = latToTileY(lat, zoom)

    val pixX = ((lon + 180.0) / 360.0 * n * 256.0 - cx * 256.0).toFloat()
    val r = lat * PI / 180.0
    val pixY = ((1.0 - ln(tan(r) + 1.0 / cos(r)) / PI) / 2.0 * n * 256.0 - cy * 256.0).toFloat()

    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val tiles = remember { mutableStateMapOf<Pair<Int, Int>, ImageBitmap>() }
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.background(Color(0xFF1E2328)).clipToBounds()) {
        val w = maxWidth.value
        val h = maxHeight.value

        val ox = w / 2f - pixX + panOffset.x
        val oy = h / 2f - pixY + panOffset.y

        // Tuiles visibles + 2 tuiles de marge de chaque côté
        val txMin = cx + floor((-ox - 256f) / 256f).toInt() - 2
        val txMax = cx + floor((w - ox) / 256f).toInt() + 2
        val tyMin = cy + floor((-oy - 256f) / 256f).toInt() - 2
        val tyMax = cy + floor((h - oy) / 256f).toInt() + 2

        // scope (vie = composition) évite que les téléchargements soient annulés
        // quand LaunchedEffect redémarre sur changement de plage.
        LaunchedEffect(txMin, txMax, tyMin, tyMax) {
            for (tx in txMin..txMax) {
                for (ty in tyMin..tyMax) {
                    val key = tx to ty
                    if (tiles.containsKey(key)) continue
                    scope.launch {
                        runCatching {
                            val bytes = tileClient.get("https://tile.openstreetmap.org/$zoom/$tx/$ty.png") {
                                header("User-Agent", "CspApp/1.0 (fr.csp.app)")
                            }.body<ByteArray>()
                            tiles[key] = bytes.toImageBitmap()
                        }
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount -> panOffset += dragAmount }
                },
        ) {
            for (tx in txMin..txMax) {
                for (ty in tyMin..tyMax) {
                    val bmp = tiles[tx to ty] ?: continue
                    drawImage(
                        image = bmp,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bmp.width, bmp.height),
                        dstOffset = IntOffset(
                            (ox + (tx - cx) * 256f).toInt(),
                            (oy + (ty - cy) * 256f).toInt(),
                        ),
                        dstSize = IntSize(256, 256),
                    )
                }
            }

            // Épingle ancrée sur la position géographique
            val px = w / 2f + panOffset.x
            val py = h / 2f + panOffset.y
            drawCircle(Color(0xFFE53935), radius = 14f, center = Offset(px, py - 14f))
            drawCircle(Color.White, radius = 5f, center = Offset(px, py - 14f))
            drawPath(
                path = Path().apply {
                    moveTo(px - 7f, py - 14f)
                    lineTo(px + 7f, py - 14f)
                    lineTo(px, py + 2f)
                    close()
                },
                color = Color(0xFFE53935),
            )
        }
    }
}

// Carte interactive pour le formulaire de création d'événement.
// Le pointeur reste fixe au centre ; l'admin déplace la carte sous lui.
// centerKey doit être incrémenté par le parent pour recentrer la carte
// sur une position saisie depuis l'extérieur (autocomplétion, favori).
@Composable
fun MapPicker(
    lat: Double,
    lon: Double,
    centerKey: Int = 0,
    modifier: Modifier = Modifier,
    onLocationChanged: (lat: Double, lon: Double) -> Unit = { _, _ -> },
) {
    val zoom = 15
    val n = (1 shl zoom).toDouble()
    val cx = lonToTileX(lon, zoom)
    val cy = latToTileY(lat, zoom)

    val pixX = ((lon + 180.0) / 360.0 * n * 256.0 - cx * 256.0).toFloat()
    val latRad = lat * PI / 180.0
    val pixY = ((1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * n * 256.0 - cy * 256.0).toFloat()

    var panOffset by remember { mutableStateOf(Offset.Zero) }
    val tiles = remember { mutableStateMapOf<Pair<Int, Int>, ImageBitmap>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(centerKey) { panOffset = Offset.Zero }

    BoxWithConstraints(modifier = modifier.background(Color(0xFF1E2328)).clipToBounds()) {
        val w = maxWidth.value
        val h = maxHeight.value

        val ox = w / 2f - pixX + panOffset.x
        val oy = h / 2f - pixY + panOffset.y

        val txMin = cx + floor((-ox - 256f) / 256f).toInt() - 2
        val txMax = cx + floor((w - ox) / 256f).toInt() + 2
        val tyMin = cy + floor((-oy - 256f) / 256f).toInt() - 2
        val tyMax = cy + floor((h - oy) / 256f).toInt() + 2

        LaunchedEffect(txMin, txMax, tyMin, tyMax) {
            for (tx in txMin..txMax) {
                for (ty in tyMin..tyMax) {
                    val key = tx to ty
                    if (tiles.containsKey(key)) continue
                    scope.launch {
                        runCatching {
                            val bytes = tileClient.get("https://tile.openstreetmap.org/$zoom/$tx/$ty.png") {
                                header("User-Agent", "CspApp/1.0 (fr.csp.app)")
                            }.body<ByteArray>()
                            tiles[key] = bytes.toImageBitmap()
                        }
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            val newTileX = cx + (pixX - panOffset.x) / 256.0
                            val newTileY = cy + (pixY - panOffset.y) / 256.0
                            onLocationChanged(tileToLat(newTileY, zoom), tileToLon(newTileX, zoom))
                        },
                        onDrag = { _, dragAmount -> panOffset += dragAmount },
                    )
                },
        ) {
            for (tx in txMin..txMax) {
                for (ty in tyMin..tyMax) {
                    val bmp = tiles[tx to ty] ?: continue
                    drawImage(
                        image = bmp,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bmp.width, bmp.height),
                        dstOffset = IntOffset(
                            (ox + (tx - cx) * 256f).toInt(),
                            (oy + (ty - cy) * 256f).toInt(),
                        ),
                        dstSize = IntSize(256, 256),
                    )
                }
            }

            // Réticule centré — le centre exact du canvas est la position sélectionnée
            val px = w / 2f
            val py = h / 2f
            drawCircle(Color(0xFFE53935), radius = 14f, center = Offset(px, py))
            drawCircle(Color.White, radius = 5f, center = Offset(px, py))
            drawPath(
                path = Path().apply {
                    moveTo(px - 6f, py)
                    lineTo(px + 6f, py)
                    lineTo(px, py + 14f)
                    close()
                },
                color = Color(0xFFE53935),
            )
        }
    }
}
