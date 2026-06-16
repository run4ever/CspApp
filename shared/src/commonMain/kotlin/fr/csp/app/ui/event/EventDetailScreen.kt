package fr.csp.app.ui.event

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.zIndex
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

// ── Données mockées ───────────────────────────────────────────

private const val MOCK_PLACE = "Le Drapeau — Vincennes"
private const val MOCK_ADDRESS = "18 avenue de Paris, 94300 Vincennes"
private val MOCK_DESC = listOf(
    "Rendez-vous en face de la brasserie Le Drapeau.",
    "Départ à l'heure, retour vers midi. Prévoyez un peu d'avance.",
    "Allure groupe 2 (28–30 km/h), parcours roulant vers la forêt.",
    "Pensez aux bidons et à un coupe-vent, la météo reste fraîche le matin.",
)

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
private fun AvatarCircle(initials: String, isYou: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isYou) CspColors.Blue else CspColors.Surface3)
            .border(2.dp, CspColors.Bg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
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

private fun nameInitials(name: String) =
    name.split(" ").filter { it.isNotBlank() }.mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")

@Composable
private fun ParticipantAvatars(joined: Boolean, count: Int) {
    val shades = listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C), Color(0xFF607D8B))
    val greyTotal = if (joined) (count - 1).coerceAtLeast(0) else count
    val greySlots = if (joined) 4 else 5
    val showOverflow = greyTotal > greySlots
    val greyFaces = if (greyTotal == 0) 0 else if (showOverflow) greySlots - 1 else greyTotal
    val overflowN = greyTotal - greyFaces

    if (count == 0 && !joined) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(shades[0])
                .border(2.dp, CspColors.Bg, CircleShape),
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (joined) {
                AvatarCircle("Moi", isYou = true, modifier = Modifier.zIndex(6f))
            }
            repeat(greyFaces) { i ->
                Box(
                    modifier = Modifier
                        .offset(x = if (i > 0 || joined) (-12).dp else 0.dp)
                        .zIndex((greySlots - i).toFloat())
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(shades[i.coerceAtMost(shades.lastIndex)])
                        .border(2.dp, CspColors.Bg, CircleShape),
                )
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
private fun CommentItem(comment: FirestoreComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        AvatarCircle(initials = nameInitials(comment.authorName).ifEmpty { "?" }, modifier = Modifier.size(36.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(comment.authorName, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink))
                Spacer(Modifier.width(8.dp))
                Text(relativeTime(comment.createdAt), style = TextStyle(fontSize = 12.5.sp, color = CspColors.Muted2))
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.text, style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.5).sp))
        }
    }
}

// ── Écran principal ───────────────────────────────────────────

@Composable
fun EventDetailScreen(event: ClubEvent, onBack: () -> Unit) {
    val vm = viewModel(key = event.id) { EventDetailViewModel(event.id) }
    val participants by vm.participants.collectAsStateWithLifecycle()
    val isJoined by vm.isJoined.collectAsStateWithLifecycle()
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()
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
            commentRepo.addComment(event.id, t, uid, name)
        }
    }

    val cancelled = event.status == EventStatus.CANCELLED
    var showLoginDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val participantCount = participants.size
    val dateLong = "${event.weekday} ${event.day} ${event.month}"

    Box(modifier = Modifier.fillMaxSize()) {
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
                val locationLabel = if (event.location.isNotEmpty()) event.location else MOCK_PLACE
                val locationSub = if (event.lat != null && event.lon != null)
                    "${event.lat.toString().take(8)}, ${event.lon.toString().take(8)}"
                else MOCK_ADDRESS
                DetailInfoRow(
                    iconContent = { IconPin(CspColors.Red, Modifier.size(19.dp)) },
                    title = locationLabel,
                    sub = locationSub,
                    actionContent = { IconMap(CspColors.Ink2, Modifier.size(18.dp)) },
                    isLast = true,
                )

                // C : Participants
                Spacer(Modifier.height(18.dp))
                Text(
                    "$participantCount Participants",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted),
                )
                Spacer(Modifier.height(10.dp))
                ParticipantAvatars(joined = isJoined, count = participantCount)

                // D : Rendez-vous
                SectionTitle("Rendez-vous")
                val visibleDesc = if (expanded) MOCK_DESC else MOCK_DESC.take(2)
                visibleDesc.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("•", style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2))
                        Text(item, style = TextStyle(fontSize = 14.5.sp, color = CspColors.Ink2, lineHeight = (14.5 * 1.6).sp))
                    }
                }
                if (MOCK_DESC.size > 2) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (expanded) "Réduire" else "Lire la suite",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Blue),
                        modifier = Modifier.clickable { expanded = !expanded },
                    )
                }

                // E : Lieu
                SectionTitle("Lieu", action = "Itinéraire")
                if (event.lat != null && event.lon != null) {
                    MapView(
                        lat = event.lat,
                        lon = event.lon,
                        label = locationLabel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(158.dp),
                    )
                } else {
                    MapPlaceholder(locationLabel.substringBefore("—").trim())
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
                        Text(locationLabel, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink))
                        Spacer(Modifier.height(2.dp))
                        Text(locationSub, style = TextStyle(fontSize = 13.5.sp, color = CspColors.Muted))
                    }
                }

                // F : Commentaires
                SectionTitle("Commentaires", count = comments.size)
                comments.forEachIndexed { i, comment ->
                    CommentItem(comment)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AvatarCircle(
                        initials = nameInitials(currentUserName).ifEmpty { "Moi" },
                        isYou = true,
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
                                scope.launch { vm.toggleParticipation() }
                            } else {
                                showLoginDialog = true
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
    } // fin Column principal

    // ── Dialog : connexion requise ────────────────────────────
    if (showLoginDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { showLoginDialog = false },
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
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Red)
                        .clickable { showLoginDialog = false }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Compris !",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    )
                }
            }
        }
    }
    } // fin Box racine
}
