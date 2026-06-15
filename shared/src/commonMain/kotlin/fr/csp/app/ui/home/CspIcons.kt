package fr.csp.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

private fun DrawScope.iconStroke(width: Float = 1.9f) = Stroke(
    width = width * density,
    cap = StrokeCap.Round,
    join = StrokeJoin.Round,
)

// scale from 24-unit viewBox to actual canvas size
private fun DrawScope.s(v: Float) = v / 24f * size.minDimension

private fun DrawScope.drawCircleStroke(color: Color, cx: Float, cy: Float, r: Float, sw: Float = 1.9f) =
    drawCircle(color = color, radius = s(r), center = Offset(s(cx), s(cy)), style = iconStroke(sw))

@Composable
fun IconCalendar(tint: Color, modifier: Modifier = Modifier, strokeWidth: Float = 1.9f) {
    Canvas(modifier = modifier) {
        val st = iconStroke(strokeWidth)
        val p = Path().apply {
            // rect x=3 y=4.5 w=18 h=16.5 rx=3
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = Rect(Offset(s(3f), s(4.5f)), Size(s(18f), s(16.5f))),
                    radiusX = s(3f), radiusY = s(3f),
                )
            )
        }
        drawPath(p, color = tint, style = st)
        drawLine(tint, Offset(s(3f), s(9.5f)), Offset(s(21f), s(9.5f)), strokeWidth = st.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(s(8f), s(2.5f)), Offset(s(8f), s(6.5f)), strokeWidth = st.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(s(16f), s(2.5f)), Offset(s(16f), s(6.5f)), strokeWidth = st.width, cap = StrokeCap.Round)
    }
}

@Composable
fun IconClock(tint: Color, modifier: Modifier = Modifier, strokeWidth: Float = 1.9f) {
    Canvas(modifier = modifier) {
        val st = iconStroke(strokeWidth)
        drawCircleStroke(tint, 12f, 12f, 8.5f, strokeWidth)
        val path = Path().apply {
            moveTo(s(12f), s(7.5f))
            lineTo(s(12f), s(12f))
            lineTo(s(15f), s(13.8f))
        }
        drawPath(path, color = tint, style = st)
    }
}

@Composable
fun IconChevronRight(tint: Color, modifier: Modifier = Modifier, strokeWidth: Float = 1.9f) {
    Canvas(modifier = modifier) {
        val st = iconStroke(strokeWidth)
        val path = Path().apply {
            moveTo(s(9f), s(4f))
            lineTo(s(17f), s(12f))
            lineTo(s(9f), s(20f))
        }
        drawPath(path, color = tint, style = st)
    }
}

@Composable
fun IconRoute(tint: Color, modifier: Modifier = Modifier, strokeWidth: Float = 1.9f) {
    Canvas(modifier = modifier) {
        val st = iconStroke(strokeWidth)
        // Bottom circle at (6, 18.5, r=2.4)
        drawCircleStroke(tint, 6f, 18.5f, 2.4f, strokeWidth)
        // Top circle at (18, 5.5, r=2.4)
        drawCircleStroke(tint, 18f, 5.5f, 2.4f, strokeWidth)
        // S-curve: M8.4 18.5 H14 A3 3 0 0 0 14 12.5 H10 A3 3 0 0 1 10 6.5 H15.6
        val path = Path().apply {
            moveTo(s(8.4f), s(18.5f))
            lineTo(s(14f), s(18.5f))
            arcTo(Rect(Offset(s(11f), s(12.5f)), Size(s(6f), s(6f))), 90f, -180f, false)
            lineTo(s(10f), s(12.5f))
            arcTo(Rect(Offset(s(7f), s(6.5f)), Size(s(6f), s(6f))), 270f, 180f, false)
            lineTo(s(15.6f), s(6.5f))
        }
        drawPath(path, color = tint, style = st)
    }
}

@Composable
fun IconHome(tint: Color, modifier: Modifier = Modifier, strokeWidth: Float = 1.9f) {
    Canvas(modifier = modifier) {
        val st = iconStroke(strokeWidth)
        val roof = Path().apply {
            moveTo(s(4f), s(11.5f))
            lineTo(s(12f), s(4f))
            lineTo(s(20f), s(11.5f))
        }
        drawPath(roof, color = tint, style = st)
        val body = Path().apply {
            moveTo(s(6f), s(10f))
            lineTo(s(6f), s(19f))
            lineTo(s(18f), s(19f))
            lineTo(s(18f), s(10f))
        }
        drawPath(body, color = tint, style = st)
    }
}

@Composable
fun IconUser(tint: Color, modifier: Modifier = Modifier, strokeWidth: Float = 1.9f) {
    Canvas(modifier = modifier) {
        val st = iconStroke(strokeWidth)
        drawCircleStroke(tint, 12f, 8f, 4f, strokeWidth)
        val arc = Path().apply {
            moveTo(s(4.5f), s(20f))
            arcTo(Rect(Offset(s(4.5f), s(12.5f)), Size(s(15f), s(15f))), 180f, -180f, false)
        }
        drawPath(arc, color = tint, style = st)
    }
}
