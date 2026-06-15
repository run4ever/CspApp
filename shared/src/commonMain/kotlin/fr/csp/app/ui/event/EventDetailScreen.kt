package fr.csp.app.ui.event

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.csp.app.resources.Res
import fr.csp.app.resources.logo_csp
import fr.csp.app.ui.home.*
import fr.csp.app.ui.theme.CspColors
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

private data class Comment(val name: String, val time: String, val text: String, val likes: Int = 0)

private val MOCK_COMMENTS = listOf(
    Comment("Emma Carena", "Il y a 3 h", "Super, j'avais hâte de voir le programme ! Je passe avec deux amis intéressés par le club.", 2),
    Comment("Alain Proviste", "Il y a 8 h", "On se retrouve au local comme d'habitude.", 0),
)

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
    val mockNames = listOf("Léa Bart", "Marc Dauphin", "Tom Roy")
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (joined) {
            AvatarCircle("Moi", isYou = true, modifier = Modifier.zIndex(4f))
        }
        mockNames.forEachIndexed { i, name ->
            AvatarCircle(
                initials = nameInitials(name),
                modifier = Modifier
                    .offset(x = if (i > 0 || joined) (-12).dp else 0.dp)
                    .zIndex((3 - i).toFloat()),
            )
        }
        Box(
            modifier = Modifier
                .offset(x = (-12).dp)
                .zIndex(0f)
                .size(40.dp)
                .clip(CircleShape)
                .background(CspColors.Surface3)
                .border(2.dp, CspColors.Bg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "+${maxOf(0, count - 3)}",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink2),
            )
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
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        AvatarCircle(initials = nameInitials(comment.name), modifier = Modifier.size(36.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(comment.name, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink))
                Spacer(Modifier.width(8.dp))
                Text(comment.time, style = TextStyle(fontSize = 12.5.sp, color = CspColors.Muted2))
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.text, style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.5).sp))
            Spacer(Modifier.height(7.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.clickable { },
                ) {
                    IconHeart(tint = CspColors.Muted, modifier = Modifier.size(14.dp))
                    Text("${comment.likes}", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted))
                }
                Text(
                    "Répondre",
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted),
                    modifier = Modifier.clickable { },
                )
            }
        }
    }
}

// ── Écran principal ───────────────────────────────────────────

@Composable
fun EventDetailScreen(event: ClubEvent, onBack: () -> Unit) {
    val cancelled = event.status == EventStatus.CANCELLED
    var joined by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf(MOCK_COMMENTS) }

    val participantCount = event.participants + if (joined) 1 else 0
    val dateLong = "${event.weekday} ${event.day} ${event.month}"

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
                DetailInfoRow(
                    iconContent = { IconPin(CspColors.Red, Modifier.size(19.dp)) },
                    title = MOCK_PLACE,
                    sub = MOCK_ADDRESS,
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
                ParticipantAvatars(joined = joined, count = participantCount)

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
                MapPlaceholder(MOCK_PLACE.substringBefore("—").trim())
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
                        Text(MOCK_PLACE, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink))
                        Spacer(Modifier.height(2.dp))
                        Text(MOCK_ADDRESS, style = TextStyle(fontSize = 13.5.sp, color = CspColors.Muted))
                    }
                }

                // F : Commentaires
                SectionTitle("Commentaires", count = comments.size)
                comments.forEachIndexed { i, comment ->
                    CommentItem(comment)
                    if (i < comments.lastIndex) Spacer(Modifier.height(16.dp))
                }

                // Saisie commentaire
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AvatarCircle("Moi", isYou = true, modifier = Modifier.size(36.dp))
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
                                .clickable {
                                    val t = commentText.trim()
                                    if (t.isNotEmpty()) {
                                        comments = listOf(Comment("Moi", "À l'instant", t)) + comments
                                        commentText = ""
                                    }
                                },
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
        }

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
                        .background(if (joined) CspColors.Surface2 else CspColors.Red)
                        .then(
                            if (joined) Modifier.border(1.dp, CspColors.Line, RoundedCornerShape(14.dp)) else Modifier
                        )
                        .clickable { joined = !joined }
                        .padding(15.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        if (joined) {
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
}
