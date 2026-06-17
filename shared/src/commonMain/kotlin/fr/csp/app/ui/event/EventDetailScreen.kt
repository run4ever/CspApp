package fr.csp.app.ui.event

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.zIndex
import fr.csp.app.ui.trace.Trace
import fr.csp.app.ui.trace.formatKm
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.data.CommentRepository
import fr.csp.app.data.FirestoreComment
import fr.csp.app.resources.Res
import fr.csp.app.resources.logo_csp
import fr.csp.app.ui.home.*
import fr.csp.app.ui.theme.CspColors
import kotlin.time.Clock
import org.jetbrains.compose.resources.painterResource

private fun mdBold(text: String): AnnotatedString = buildAnnotatedString {
    val pattern = Regex("""\*\*(.+?)\*\*""")
    var cursor = 0
    pattern.findAll(text).forEach { m ->
        append(text.substring(cursor, m.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(m.groupValues[1]) }
        cursor = m.range.last + 1
    }
    append(text.substring(cursor))
}

private fun relativeTime(epochMillis: Long): String {
    val diffMin = (Clock.System.now().toEpochMilliseconds() - epochMillis) / 60_000
    return when {
        diffMin < 1 -> "À l'instant"
        diffMin < 60 -> "Il y a ${diffMin} min"
        diffMin < 1440 -> "Il y a ${diffMin / 60} h"
        else -> "Il y a ${diffMin / 1440} j"
    }
}


// ── Composants internes ───────────────────────────────────────

@Composable
private fun TraceEventRow(trace: Trace, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CspColors.Surface2)
            .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(trace.title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Ink))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${trace.distanceKm.formatKm()} km", style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
                Text("D+ ${trace.elevationUp} m", style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
            }
        }
        if (trace.url.isNotBlank()) {
            IconChevronRight(tint = CspColors.Muted2, modifier = Modifier.size(18.dp))
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun GlassButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A0C0E).copy(alpha = 0.52f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun DetailInfoRow(
    iconContent: @Composable () -> Unit,
    title: String,
    sub: String,
    actionContent: (@Composable () -> Unit)? = null,
    isLast: Boolean = false,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(CspColors.RedSoft),
                contentAlignment = Alignment.Center,
            ) { iconContent() }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink, lineHeight = (15 * 1.3).sp))
                Spacer(Modifier.height(2.dp))
                Text(sub, style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
            }
            if (actionContent != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(CspColors.Surface2)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center,
                ) { actionContent() }
            }
        }
        if (!isLast) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CspColors.Line2))
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: Int? = null, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 26.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink))
            if (count != null) {
                Text("($count)", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted))
            }
        }
        if (action != null) {
            Text(
                action,
                style = TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = CspColors.Blue),
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
private fun AvatarCircle(initials: String, isYou: Boolean = false, photoUrl: String? = null, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isYou) CspColors.Blue else CspColors.Surface3)
            .border(2.dp, CspColors.Bg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                initials,
                style = TextStyle(
                    fontSize = if (isYou) 13.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isYou) Color(0xFF08252E) else CspColors.Ink,
                ),
            )
        }
    }
}

private fun nameInitials(name: String) =
    name.split(" ").filter { it.isNotBlank() }.mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")

@Composable
private fun ParticipantAvatars(
    participantIds: List<String>,
    currentUid: String?,
    photoUrlMap: Map<String, String?>,
    userPhotoUrl: String? = null,
) {
    val shades = listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFF607D8B))
    val isJoined = currentUid != null && currentUid in participantIds
    val others = participantIds.filter { it != currentUid }
    val maxOtherSlots = if (isJoined) 4 else 5
    val showOverflow = others.size > maxOtherSlots
    val visibleOthers = if (showOverflow) others.take(maxOtherSlots - 1) else others
    val overflowN = others.size - visibleOthers.size

    if (participantIds.isEmpty()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(shades[0])
                .border(2.dp, CspColors.Bg, CircleShape),
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isJoined) {
                AvatarCircle(
                    initials = "Moi",
                    isYou = true,
                    photoUrl = userPhotoUrl ?: photoUrlMap[currentUid],
                    modifier = Modifier.zIndex(6f),
                )
            }
            visibleOthers.forEachIndexed { i, uid ->
                val photo = photoUrlMap[uid]
                Box(
                    modifier = Modifier
                        .offset(x = if (i > 0 || isJoined) (-12).dp else 0.dp)
                        .zIndex((maxOtherSlots - i).toFloat())
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(shades[i.coerceAtMost(shades.lastIndex)])
                        .border(2.dp, CspColors.Bg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!photo.isNullOrBlank()) {
                        AsyncImage(
                            model = photo,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
            if (showOverflow) {
                Box(
                    modifier = Modifier
                        .offset(x = (-12).dp)
                        .zIndex(0f)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(shades.last())
                        .border(2.dp, CspColors.Bg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+$overflowN", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CspColors.Surface2)
            .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Grille diagonale
            val gridColor = Color.White.copy(alpha = 0.03f)
            val step = 13.dp.toPx()
            var x = -size.height
            while (x < size.width + size.height) {
                drawLine(gridColor, Offset(x, 0f), Offset(x + size.height, size.height), strokeWidth = 1f)
                drawLine(gridColor, Offset(x + size.height, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            // Fausse route horizontale
            drawLine(
                Color(0xFF4CCBEE).copy(alpha = 0.12f),
                Offset(0f, size.height * 0.58f),
                Offset(size.width, size.height * 0.58f),
                strokeWidth = 8.dp.toPx(),
            )
        }
        // Épingle centrale
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-8).dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .rotate(-45f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 50.dp, topEnd = 50.dp,
                            bottomEnd = 50.dp, bottomStart = 0.dp,
                        )
                    )
                    .background(CspColors.Red),
            )
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
        }
        Text(
            "CARTE — $label",
            style = TextStyle(
                fontSize = 10.5.sp,
                color = CspColors.Muted2,
                letterSpacing = (10.5 * 0.08).sp,
                fontFamily = FontFamily.Monospace,
            ),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp),
        )
    }
}

@Composable
private fun CommentItem(comment: FirestoreComment, canDelete: Boolean = false, onDelete: (() -> Unit)? = null) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Supprimer ce commentaire ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showConfirm = false; onDelete?.invoke() }) {
                    Text("Supprimer", color = CspColors.Red)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showConfirm = false }) {
                    Text("Annuler")
                }
            },
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AvatarCircle(initials = nameInitials(comment.authorName).ifEmpty { "?" }, photoUrl = comment.authorPhotoUrl, modifier = Modifier.size(36.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(comment.authorName, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink))
                Spacer(Modifier.width(8.dp))
                Text(relativeTime(comment.createdAt), style = TextStyle(fontSize = 12.5.sp, color = CspColors.Muted2))
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.text, style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.5).sp))
        }
        if (canDelete) {
            Box(
                modifier = Modifier
                    .clickable { showConfirm = true }
                    .padding(4.dp),
            ) {
                IconTrash(tint = CspColors.Muted2, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun GroupBadge(group: String) {
    val bg = when (group) {
        "G1" -> CspColors.Red
        "G2" -> CspColors.Blue
        else -> Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(if (group == "G3") Modifier.border(1.dp, CspColors.Line, RoundedCornerShape(8.dp)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(group, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D0F11)))
    }
}

@Composable
private fun GroupAvatarRow(
    uids: List<String>,
    currentUid: String?,
    photoUrlMap: Map<String, String?>,
    userPhotoUrl: String? = null,
) {
    val shades = listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C))
    val maxShow = 5
    val showOverflow = uids.size > maxShow
    val visible = if (showOverflow) uids.take(maxShow - 1) else uids
    Row(verticalAlignment = Alignment.CenterVertically) {
        visible.forEachIndexed { i, uid ->
            val isYou = uid == currentUid
            val photo = if (isYou) userPhotoUrl ?: photoUrlMap[uid] else photoUrlMap[uid]
            Box(
                modifier = Modifier
                    .offset(x = if (i > 0) (-10).dp else 0.dp)
                    .zIndex((maxShow - i).toFloat())
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isYou) CspColors.Blue else shades[i.coerceAtMost(shades.lastIndex)])
                    .border(2.dp, CspColors.Bg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (!photo.isNullOrBlank()) {
                    AsyncImage(model = photo, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else if (isYou) {
                    Text("Moi", style = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF08252E)))
                }
            }
        }
        if (showOverflow) {
            Box(
                modifier = Modifier
                    .offset(x = (-10).dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(shades.last())
                    .border(2.dp, CspColors.Bg, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("+${uids.size - visible.size}", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White))
            }
        }
    }
}

@Composable
private fun GroupPickerDialog(
    currentGroup: String?,
    onSelect: (String) -> Unit,
    onLeave: () -> Unit,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CspColors.Surface2)
                .border(1.dp, CspColors.Line, RoundedCornerShape(20.dp))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (currentGroup != null) "Changer de groupe" else "Choisissez votre groupe",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
            )
            Text(
                "Choisissez selon votre allure habituelle sur la sortie.",
                style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.5).sp),
            )
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf("G1", "G2", "G3").forEach { g ->
                    val isSelected = g == currentGroup
                    val groupBg = when (g) {
                        "G1" -> CspColors.Red
                        "G2" -> CspColors.Blue
                        else -> Color.White
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) groupBg else groupBg.copy(alpha = 0.18f))
                            .border(
                                1.5.dp,
                                if (isSelected) groupBg else groupBg.copy(alpha = 0.35f),
                                RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelect(g) }
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            g,
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color(0xFF0D0F11) else CspColors.Ink,
                            ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            if (currentGroup != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Surface3)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                        .clickable { onLeave() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Annuler ma participation",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Red),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CspColors.Surface3)
                    .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Fermer",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink2),
                )
            }
        }
    }
}

@Composable
private fun MarkdownBlock(
    content: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lines = content.lines().filter { it.isNotBlank() }
    val threshold = 5
    val visibleLines = if (expanded || lines.size <= threshold) lines else lines.take(threshold)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        visibleLines.forEach { line ->
            val trimmed = line.trimStart()
            val numberedMatch = Regex("""^(\d+)\.\s+(.+)""").matchEntire(trimmed)
            when {
                trimmed.startsWith("# ") -> Text(
                    text = mdBold(trimmed.drop(2)),
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink, lineHeight = (18 * 1.3).sp),
                )
                trimmed.startsWith("## ") -> Text(
                    text = mdBold(trimmed.drop(3)),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink, lineHeight = (16 * 1.3).sp),
                )
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("•", style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2))
                    Text(
                        text = mdBold(trimmed.drop(2)),
                        style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2, lineHeight = (14.5 * 1.6).sp),
                    )
                }
                numberedMatch != null -> Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("${numberedMatch.groupValues[1]}.", style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2))
                    Text(
                        text = mdBold(numberedMatch.groupValues[2]),
                        style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2, lineHeight = (14.5 * 1.6).sp),
                    )
                }
                else -> Text(
                    text = mdBold(line),
                    style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2, lineHeight = (14.5 * 1.6).sp),
                )
            }
        }
        if (lines.size > threshold) {
            Spacer(Modifier.height(2.dp))
            Text(
                if (expanded) "Réduire" else "Lire la suite",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Blue),
                modifier = Modifier.clickable(onClick = onToggle),
            )
        }
    }
}

// ── Écran principal ───────────────────────────────────────────

private enum class LoginPrompt { PARTICIPATE, COMMENT }

@Composable
fun EventDetailScreen(event: ClubEvent, onBack: () -> Unit, isAdmin: Boolean = false, onEdit: (() -> Unit)? = null, onDelete: (() -> Unit)? = null, onPrevious: (() -> Unit)? = null, onNext: (() -> Unit)? = null, userCanComment: Boolean = true, userPhotoUrl: String? = null, onLogin: (() -> Unit)? = null) {
    val vm = viewModel(key = event.id) { EventDetailViewModel(event.id) }
    val participants by vm.participants.collectAsStateWithLifecycle()
    val currentUid by vm.currentUid.collectAsStateWithLifecycle()
    val isJoined by vm.isJoined.collectAsStateWithLifecycle()
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()
    val participantPhotos by vm.participantPhotos.collectAsStateWithLifecycle()
    val participantGroups by vm.participantGroups.collectAsStateWithLifecycle()
    val myGroup by vm.myGroup.collectAsStateWithLifecycle()
    val traces by vm.traces.collectAsStateWithLifecycle()
    val isHebdo = event.type == "Sortie hebdo CSP"
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    val commentRepo = remember { CommentRepository() }
    val comments by commentRepo.getComments(event.id).collectAsStateWithLifecycle(emptyList())
    var currentUserName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            val doc = Firebase.firestore.collection("users").document(uid).get()
            val first = doc.get<String?>("prenom") ?: ""
            val last = doc.get<String?>("nom") ?: ""
            currentUserName = "$first $last".trim()
        } catch (_: Exception) {}
    }

    var commentText by remember { mutableStateOf("") }

    suspend fun resolveUserName(): String {
        if (currentUserName.isNotBlank()) return currentUserName
        val uid = Firebase.auth.currentUser?.uid ?: return ""
        return try {
            val doc = Firebase.firestore.collection("users").document(uid).get()
            val first = doc.get<String?>("prenom") ?: ""
            val last = doc.get<String?>("nom") ?: ""
            "$first $last".trim().also { currentUserName = it }
        } catch (_: Exception) { "" }
    }

    fun sendComment() {
        val t = commentText.trim()
        if (t.isEmpty()) return
        val uid = Firebase.auth.currentUser?.uid ?: return
        commentText = ""
        scope.launch {
            val name = resolveUserName()
            commentRepo.addComment(event.id, t, uid, name, userPhotoUrl)
        }
    }

    val cancelled = event.status == EventStatus.CANCELLED
    var loginPrompt by remember { mutableStateOf<LoginPrompt?>(null) }
    var showGroupPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val swipeThresholdPx = remember { with(density) { 80.dp.toPx() } }
    var totalDrag by remember { mutableFloatStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }

    val participantCount = participants.size
    val dateLong = "${event.weekday} ${event.day} ${event.month}"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onPrevious, onNext) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onDragEnd = {
                        when {
                            totalDrag > swipeThresholdPx -> onPrevious?.invoke()
                            totalDrag < -swipeThresholdPx -> onNext?.invoke()
                        }
                        totalDrag = 0f
                    },
                    onDragCancel = { totalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                )
            },
    ) {
    Column(modifier = Modifier.fillMaxSize().background(CspColors.Bg)) {
        // Scrollable body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── A : Héro ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(212.dp)
                    .clip(RoundedCornerShape(0.dp)),
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo_csp),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                // Dégradé sombre pour lisibilité
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.45f),
                                0.28f to Color.Transparent,
                                0.62f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.75f),
                            )
                        ),
                )
                // Bouton retour (verre)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 14.dp, top = 10.dp),
                ) {
                    GlassButton(onClick = onBack) {
                        IconBack(tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                // Boutons admin (haut droite) : édition + suppression
                if (isAdmin) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(end = 14.dp, top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (onEdit != null) {
                            GlassButton(onClick = onEdit) {
                                IconPencil(tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (onDelete != null) {
                            GlassButton(onClick = { showDeleteConfirm = true }) {
                                IconTrash(tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                // Bannière "Annulé"
                if (cancelled) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(CspColors.RedDeep)
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Annulé",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (14 * 0.02).sp),
                        )
                    }
                }
            }

            // ── B, C, D, E, F ────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))

                // B : Titre
                Text(
                    event.title,
                    style = TextStyle(fontSize = 27.sp, fontWeight = FontWeight.Black, color = CspColors.Ink, lineHeight = (27 * 1.08).sp),
                )

                // B : InfoRows
                Spacer(Modifier.height(16.dp))
                DetailInfoRow(
                    iconContent = { IconCalendar(CspColors.Red, Modifier.size(19.dp)) },
                    title = dateLong,
                    sub = "${event.time} GMT+2",
                    actionContent = { IconCalPlus(CspColors.Ink2, Modifier.size(18.dp)) },
                )
                if (event.location.isNotEmpty()) {
                    val locationSub = if (event.lat != null && event.lon != null)
                        "${event.lat.toString().take(8)}, ${event.lon.toString().take(8)}"
                    else ""
                    DetailInfoRow(
                        iconContent = { IconPin(CspColors.Red, Modifier.size(19.dp)) },
                        title = event.location,
                        sub = locationSub,
                        actionContent = { IconMap(CspColors.Ink2, Modifier.size(18.dp)) },
                        isLast = true,
                    )
                }

                // C : Participants
                Spacer(Modifier.height(18.dp))
                Text(
                    "$participantCount Participant${if (participantCount >= 2) "s" else ""}",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted),
                )
                Spacer(Modifier.height(10.dp))
                if (isHebdo && participantGroups.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("G1", "G2", "G3").forEach { g ->
                            val groupUids = participants.filter { participantGroups[it] == g }
                            if (groupUids.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    GroupBadge(g)
                                    GroupAvatarRow(
                                        uids = groupUids,
                                        currentUid = currentUid,
                                        photoUrlMap = participantPhotos,
                                        userPhotoUrl = userPhotoUrl,
                                    )
                                }
                            }
                        }
                        val ungrouped = participants.filter { participantGroups[it].isNullOrEmpty() }
                        if (ungrouped.isNotEmpty()) {
                            ParticipantAvatars(
                                participantIds = ungrouped,
                                currentUid = currentUid,
                                photoUrlMap = participantPhotos,
                                userPhotoUrl = userPhotoUrl,
                            )
                        }
                    }
                } else {
                    ParticipantAvatars(
                        participantIds = participants,
                        currentUid = currentUid,
                        photoUrlMap = participantPhotos,
                        userPhotoUrl = userPhotoUrl,
                    )
                }

                // D : Programme
                if (event.description.isNotBlank()) {
                    SectionTitle("Détails")
                    MarkdownBlock(
                        content = event.description,
                        expanded = expanded,
                        onToggle = { expanded = !expanded },
                    )
                }

                // E : Parcours
                if (traces.isNotEmpty()) {
                    SectionTitle("Parcours")
                    traces.forEach { trace -> TraceEventRow(trace, onClick = { if (trace.url.isNotBlank()) uriHandler.openUri(trace.url) }) }
                }

                // F : Lieu
                if (event.location.isNotEmpty()) {
                    val locationSub = if (event.lat != null && event.lon != null)
                        "${event.lat.toString().take(8)}, ${event.lon.toString().take(8)}"
                    else ""
                    SectionTitle("Lieu", action = "Itinéraire")
                    if (event.lat != null && event.lon != null) {
                        MapView(
                            lat = event.lat,
                            lon = event.lon,
                            label = event.location,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(158.dp),
                        )
                    } else {
                        MapPlaceholder(event.location.substringBefore("—").trim())
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawLine(
                                    CspColors.Red,
                                    Offset(0f, 0f),
                                    Offset(0f, size.height),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Square,
                                )
                            }
                            .padding(start = 12.dp),
                    ) {
                        Column {
                            Text(event.location, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink))
                            Spacer(Modifier.height(2.dp))
                            Text(locationSub, style = TextStyle(fontSize = 13.5.sp, color = CspColors.Muted))
                        }
                    }
                }

                // F : Commentaires
                SectionTitle("Commentaires", count = comments.size)
                val currentUid = Firebase.auth.currentUser?.uid
                comments.forEachIndexed { i, comment ->
                    CommentItem(
                        comment = comment,
                        canDelete = isAdmin || comment.authorId == currentUid,
                        onDelete = {
                            scope.launch { commentRepo.deleteComment(event.id, comment.id) }
                        },
                    )
                    if (i < comments.lastIndex) Spacer(Modifier.height(16.dp))
                }
                if (comments.isEmpty()) {
                    Text(
                        "Aucun commentaire pour l'instant.",
                        style = TextStyle(fontSize = 13.sp, color = CspColors.Muted2),
                    )
                }

                // Saisie commentaire
                Spacer(Modifier.height(16.dp))
                if (isLoggedIn == true && !userCanComment) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CspColors.Surface)
                            .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                    ) {
                        Text(
                            "Vous ne pouvez pas commenter cet évènement.",
                            style = TextStyle(fontSize = 14.sp, color = CspColors.Muted, fontStyle = FontStyle.Italic),
                        )
                    }
                } else {
                    Box {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            AvatarCircle(
                                initials = nameInitials(currentUserName).ifEmpty { "Moi" },
                                isYou = true,
                                photoUrl = userPhotoUrl,
                                modifier = Modifier.size(36.dp),
                            )
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(CspColors.Surface)
                                    .border(1.dp, CspColors.Line, RoundedCornerShape(999.dp))
                                    .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                BasicTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    modifier = Modifier.weight(1f),
                                    textStyle = TextStyle(fontSize = 14.sp, color = CspColors.Ink),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { sendComment() }),
                                    decorationBox = { inner ->
                                        Box {
                                            if (commentText.isEmpty()) {
                                                Text(
                                                    "Laissez un commentaire…",
                                                    style = TextStyle(fontSize = 14.sp, color = CspColors.Muted2),
                                                )
                                            }
                                            inner()
                                        }
                                    },
                                )
                                val hasText = commentText.isNotBlank()
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (hasText) CspColors.Red else CspColors.Surface3)
                                        .clickable { sendComment() },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    IconSend(
                                        tint = if (hasText) Color.White else CspColors.Muted,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                        // Overlay transparent quand déconnecté — intercepte les clics
                        if (isLoggedIn != true) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) { loginPrompt = LoginPrompt.COMMENT },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        } // fin scroll

        // ── G : CTA fixe ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(CspColors.Line, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                }
                .background(Color(0xFF0D0F11).copy(alpha = 0.94f))
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
        ) {
            if (cancelled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CspColors.Surface2)
                        .padding(15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Événement annulé",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted),
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Bouton "Changer de groupe" (hebdo uniquement, quand déjà inscrit)
                    if (isHebdo && isJoined) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CspColors.Surface2)
                                .border(1.dp, CspColors.Line, RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isLoggedIn == true) showGroupPicker = true
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (myGroup != null) {
                                    GroupBadge(myGroup!!)
                                }
                                Text(
                                    "Changer de groupe",
                                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Ink2),
                                )
                            }
                        }
                    }
                    // CTA principal
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isJoined) CspColors.Surface2 else CspColors.Red)
                            .then(
                                if (isJoined) Modifier.border(1.dp, CspColors.Line, RoundedCornerShape(14.dp)) else Modifier
                            )
                            .clickable {
                                if (isLoggedIn == true) {
                                    when {
                                        isJoined -> scope.launch { vm.leaveEvent() }
                                        isHebdo -> showGroupPicker = true
                                        else -> scope.launch { vm.joinWithGroup("") }
                                    }
                                } else {
                                    loginPrompt = LoginPrompt.PARTICIPATE
                                }
                            }
                            .padding(15.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                        ) {
                            if (isJoined) {
                                IconCheck(CspColors.Green, Modifier.size(19.dp))
                                Text(
                                    "Annuler ma participation",
                                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Green),
                                )
                            } else {
                                Text(
                                    "Participer",
                                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White),
                                )
                            }
                        }
                    }
                }
            }
        }
    } // fin Column principal

    // ── Dialog : confirmation suppression ────────────────────
    if (showDeleteConfirm) {
        if (event.seriesId != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .zIndex(10f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CspColors.Surface2)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(20.dp))
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    androidx.compose.material3.Text(
                        "Supprimer cet événement ?",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                    )
                    androidx.compose.material3.Text(
                        "Cet événement fait partie d'une série. Que souhaitez-vous supprimer ?",
                        style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.5).sp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CspColors.Red)
                            .clickable {
                                showDeleteConfirm = false
                                scope.launch {
                                    vm.deleteSeriesFrom(event.seriesId, event.dateSort)
                                    onDelete?.invoke()
                                }
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Text(
                            "Cet événement et les suivants",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CspColors.Surface3)
                            .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                            .clickable {
                                showDeleteConfirm = false
                                scope.launch {
                                    vm.deleteEvent()
                                    onDelete?.invoke()
                                }
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Text(
                            "Cet événement uniquement",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink2),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CspColors.Surface3)
                            .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                            .clickable { showDeleteConfirm = false }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.material3.Text(
                            "Annuler",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted),
                        )
                    }
                }
            }
        } else {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { androidx.compose.material3.Text("Supprimer cet événement ?") },
                text = { androidx.compose.material3.Text("Cette action est irréversible.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            vm.deleteEvent()
                            onDelete?.invoke()
                        }
                    }) {
                        androidx.compose.material3.Text("Supprimer", color = CspColors.Red)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                        androidx.compose.material3.Text("Annuler")
                    }
                },
            )
        }
    }

    // ── Dialog : connexion requise ────────────────────────────
    if (loginPrompt != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { loginPrompt = null },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CspColors.Surface2)
                    .border(1.dp, CspColors.Line, RoundedCornerShape(20.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { }
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (loginPrompt == LoginPrompt.PARTICIPATE) {
                    Text(
                        "Venez comme vous êtes 😊",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                    )
                    Text(
                        "Vous devez être connecté.e pour signaler votre présence à l'évènement. " +
                            "Si vous n'êtes pas encore adhérent.e au club, n'hésitez pas à venir à l'une de nos sorties " +
                            "sans prévenir, vous êtes bienvenu.e !",
                        style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.6).sp),
                    )
                } else {
                    Text(
                        "Connexion requise",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                    )
                    Text(
                        "Vous devez être connecté.e pour commenter les sorties du CSP.",
                        style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.6).sp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (onLogin != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CspColors.Red)
                            .clickable { loginPrompt = null; onLogin() }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Me connecter",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Surface3)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                        .clickable { loginPrompt = null }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Compris !",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink2),
                    )
                }
            }
        }
    }
    // ── Dialog : choix de groupe ──────────────────────────────
    if (showGroupPicker) {
        GroupPickerDialog(
            currentGroup = myGroup,
            onSelect = { group ->
                showGroupPicker = false
                scope.launch { vm.joinWithGroup(group) }
            },
            onLeave = {
                showGroupPicker = false
                scope.launch { vm.leaveEvent() }
            },
            onDismiss = { showGroupPicker = false },
        )
    }
    } // fin Box racine
}
