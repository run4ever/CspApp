package fr.csp.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.HorizontalDivider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.csp.app.ui.admin.AdminValidationScreen
import fr.csp.app.ui.auth.AuthScreen
import fr.csp.app.ui.auth.AuthViewModel
import fr.csp.app.ui.profile.ProfileScreen
import fr.csp.app.ui.theme.CspColors

// ── Bannières ─────────────────────────────────────────────────

@Composable
private fun SignupSuccessBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CspColors.Green.copy(alpha = 0.12f))
            .border(1.dp, CspColors.Green.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconCheck(tint = CspColors.Green, modifier = Modifier.size(17.dp))
        Text(
            "Votre demande d'inscription a bien été transmise.",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Green),
        )
    }
}

@Composable
private fun AdminPendingBanner(count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CspColors.Blue.copy(alpha = 0.12f))
            .border(1.dp, CspColors.Blue.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        IconBell(tint = CspColors.Blue, modifier = Modifier.size(17.dp))
        Text(
            "$count compte${if (count > 1) "s" else ""} en attente de validation.",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Blue),
            modifier = Modifier.weight(1f),
        )
        IconChevronRight(tint = CspColors.Blue, modifier = Modifier.size(14.dp))
    }
}

// ── Salutation ────────────────────────────────────────────────

@Composable
fun Greeting(prenom: String? = null, nom: String? = null, onAvatarClick: () -> Unit = {}) {
    val hour = currentHour()
    val salut = if (hour >= 16) "Bonsoir" else "Bonjour"
    val greeting = if (prenom.isNullOrBlank()) salut else "$salut, $prenom !"
    val isLoggedIn = !prenom.isNullOrBlank() || !nom.isNullOrBlank()
    val initials = if (isLoggedIn) {
        "${prenom?.firstOrNull() ?: ""}${nom?.firstOrNull() ?: ""}".uppercase()
    } else {
        "XY"
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (isLoggedIn) CspColors.Blue else CspColors.Surface2)
                .border(1.dp, if (isLoggedIn) CspColors.Blue else CspColors.Line, CircleShape)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                initials,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isLoggedIn) CspColors.CyanInk else CspColors.Muted,
                ),
            )
        }
        Column {
            Text(
                text = greeting,
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Black, color = CspColors.Ink, lineHeight = (28 * 1.02).sp),
            )
            Text(
                text = "Prêt·e à rouler ?",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CspColors.Muted),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ── Profil utilisateur ────────────────────────────────────────

@Composable
private fun ProfileRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted),
        )
        Text(
            value.ifEmpty { "—" },
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Ink),
        )
    }
}

@Composable
private fun UserProfileTab(user: UserDoc, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Mon profil",
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CspColors.Surface)
                .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProfileRow(label = "Prénom", value = user.prenom)
            HorizontalDivider(color = CspColors.Line2)
            ProfileRow(label = "Nom", value = user.nom)
            HorizontalDivider(color = CspColors.Line2)
            ProfileRow(label = "Date de naissance", value = user.dateNaissance)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, CspColors.Red.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable(onClick = onSignOut)
                .padding(vertical = 15.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Me déconnecter",
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Red),
            )
        }
    }
}

// ── Carte "Prochaine sortie" ──────────────────────────────────

@Composable
fun NextRideHero(event: ClubEvent, onClick: () -> Unit) {
    val heroGradient = Brush.linearGradient(listOf(CspColors.Red, CspColors.RedDeep))
    val textMeasurer = rememberTextMeasurer()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
    ) {
        // Haut rouge — le filigrane "CSP" est dessiné sur le canvas (drawBehind)
        // pour ne pas gonfler la hauteur de la Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .drawBehind {
                    drawRect(heroGradient)
                    val csp = textMeasurer.measure(
                        "CSP",
                        TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.13f), lineHeight = 96.sp),
                    )
                    drawText(csp, topLeft = Offset(size.width - csp.size.width + 8.dp.toPx(), (-16).dp.toPx()))
                }
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 11.dp, vertical = 5.dp),
            ) {
                IconRoute(tint = Color.White, modifier = Modifier.size(13.dp), strokeWidth = 2.2f)
                Text(
                    text = "PROCHAINE SORTIE",
                    style = TextStyle(fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (11.5 * 0.06).sp),
                )
            }
            Spacer(Modifier.height(13.dp))
            Text(
                text = event.title,
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White, lineHeight = (22 * 1.1).sp),
                modifier = Modifier.fillMaxWidth(0.94f),
            )
        }

        // Bas cyan
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CspColors.Blue)
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconCalendar(tint = CspColors.CyanInk, modifier = Modifier.size(15.dp), strokeWidth = 2f)
                    Text("${event.weekday} ${event.day} ${event.month}", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.CyanInk))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconClock(tint = CspColors.CyanInk, modifier = Modifier.size(15.dp), strokeWidth = 2f)
                    Text(event.time, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.CyanInk))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarStack(count = 3)
                    Spacer(Modifier.width(10.dp))
                    Text("${event.participants} participants", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CspColors.CyanInk))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White)
                        .padding(horizontal = 15.dp, vertical = 9.dp),
                ) {
                    Text("Voir", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.RedDeep))
                    IconChevronRight(tint = CspColors.RedDeep, modifier = Modifier.size(14.dp), strokeWidth = 2.4f)
                }
            }
        }
    }
}

@Composable
private fun AvatarStack(count: Int) {
    val avatarColors = listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE), Color(0xFF78909C))
    Row {
        repeat(count.coerceAtMost(avatarColors.size)) { i ->
            Box(
                modifier = Modifier
                    .offset(x = (-10 * i).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(avatarColors[i])
                    .border(2.dp, CspColors.Blue, CircleShape),
            )
        }
    }
}

// ── Ligne agenda / timeline ───────────────────────────────────

@Composable
fun AgendaItem(event: ClubEvent, isLast: Boolean, onClick: () -> Unit) {
    val cancelled = event.status == EventStatus.CANCELLED
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            .then(if (!cancelled) Modifier.clickable(onClick = onClick) else Modifier),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Colonne date
        Column(
            modifier = Modifier.width(44.dp).padding(top = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(event.wd3, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted, lineHeight = 11.sp))
            Text(event.day.toString(), style = TextStyle(fontSize = 21.sp, fontWeight = FontWeight.Black, color = CspColors.Ink, lineHeight = (21 * 1.1).sp), modifier = Modifier.padding(vertical = 2.dp))
            Text(event.monthShort, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted, letterSpacing = (10 * 0.04).sp))
        }

        // Rail timeline
        Box(modifier = Modifier.width(14.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(CspColors.Line))
            if (cancelled) {
                CrossMarker(modifier = Modifier.padding(top = 11.dp))
            } else {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(CspColors.Bg))
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(CspColors.Blue))
                }
            }
        }

        // Carte événement
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 2.dp else 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CspColors.Surface)
                .border(1.dp, CspColors.Line2, RoundedCornerShape(14.dp))
                .padding(horizontal = 13.dp, vertical = 12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    event.title,
                    style = TextStyle(
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink,
                        lineHeight = (15 * 1.25).sp,
                        textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    IconClock(tint = if (cancelled) CspColors.Red else CspColors.Muted, modifier = Modifier.size(14.dp))
                    Text(
                        event.time,
                        style = TextStyle(
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (cancelled) CspColors.Red else CspColors.Muted,
                            textDecoration = if (cancelled) TextDecoration.LineThrough else TextDecoration.None,
                        ),
                    )
                    if (cancelled) {
                        Text("· Annulée", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Red))
                    }
                }
            }
            if (!cancelled) {
                IconChevronRight(tint = CspColors.Muted2, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun CrossMarker(modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(20.dp).clip(CircleShape).background(CspColors.Bg), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(12.dp)) {
            val w = 2.4.dp.toPx()
            drawLine(CspColors.Red, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = w, cap = StrokeCap.Round)
            drawLine(CspColors.Red, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = w, cap = StrokeCap.Round)
        }
    }
}

// ── Barre de navigation ───────────────────────────────────────

@Composable
fun BottomNav(activeTab: Int = 0, onTabSelected: (Int) -> Unit = {}) {
    data class Tab(val label: String, val index: Int, val icon: @Composable (Color, Modifier) -> Unit)
    val tabs = listOf(
        Tab("Accueil", 0) { c, m -> IconHome(c, m) },
        Tab("Événements", 1) { c, m -> IconCalendar(c, m) },
        Tab("Profil", 2) { c, m -> IconUser(c, m) },
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CspColors.Bg.copy(alpha = 0.92f))
            .border(width = 1.dp, color = CspColors.Line, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
            .navigationBarsPadding(),
    ) {
        tabs.forEach { tab ->
            val active = tab.index == activeTab
            val tint = if (active) CspColors.Red else CspColors.Muted2
            Column(
                modifier = Modifier.weight(1f).clickable { onTabSelected(tab.index) }.padding(top = 10.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tab.icon(tint, Modifier.size(23.dp))
                Text(
                    tab.label,
                    style = TextStyle(fontSize = 10.5.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold, color = tint),
                )
            }
        }
    }
}

// ── Écran complet ─────────────────────────────────────────────

@Composable
fun HomeScreen(onEventClick: (ClubEvent) -> Unit = {}) {
    val vm = viewModel { HomeViewModel() }
    val events by vm.events.collectAsStateWithLifecycle()
    val userDoc by vm.userDoc.collectAsStateWithLifecycle()
    val isAdmin by vm.isAdmin.collectAsStateWithLifecycle()
    val pendingCount by vm.pendingCount.collectAsStateWithLifecycle()
    val authVm = viewModel { AuthViewModel() }
    val scope = rememberCoroutineScope()
    var showSignupBanner by remember { mutableStateOf(false) }
    var showAdminValidation by remember { mutableStateOf(false) }
    val today = remember { currentDateIso() }
    val future = remember(events, today) {
        events.filter { it.dateSort.isEmpty() || it.dateSort >= today }
    }
    val featuredIndex = remember(future) {
        future.indexOfFirst { it.status != EventStatus.CANCELLED }
    }
    val featured = remember(future, featuredIndex) { future.getOrNull(featuredIndex) }
    val cancelledBefore = remember(future, featuredIndex) {
        if (featuredIndex > 0) future.take(featuredIndex) else emptyList()
    }
    val afterFeatured = remember(future, featuredIndex) {
        if (featuredIndex >= 0) future.drop(featuredIndex + 1) else future
    }
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding(),
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (showAdminValidation) {
                AdminValidationScreen(onBack = { showAdminValidation = false })
            } else when (selectedTab) {
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 16.dp, start = 18.dp, end = 18.dp, bottom = 22.dp),
                ) {
                    if (showSignupBanner) {
                        SignupSuccessBanner()
                        Spacer(Modifier.height(16.dp))
                    }
                    if (isAdmin && pendingCount > 0) {
                        AdminPendingBanner(count = pendingCount, onClick = { showAdminValidation = true })
                        Spacer(Modifier.height(16.dp))
                    }
                    Greeting(
                        prenom = userDoc?.prenom,
                        nom = userDoc?.nom,
                        onAvatarClick = { selectedTab = 2 },
                    )
                    cancelledBefore.forEachIndexed { index, event ->
                        AgendaItem(
                            event = event,
                            isLast = index == cancelledBefore.lastIndex && featured == null,
                            onClick = { onEventClick(event) },
                        )
                    }
                    if (featured != null) {
                        if (cancelledBefore.isNotEmpty()) Spacer(Modifier.height(16.dp))
                        NextRideHero(event = featured, onClick = { onEventClick(featured) })
                    }
                    if (afterFeatured.isNotEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Sorties suivantes",
                            style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        afterFeatured.forEachIndexed { index, event ->
                            AgendaItem(
                                event = event,
                                isLast = index == afterFeatured.lastIndex,
                                onClick = { onEventClick(event) },
                            )
                        }
                    }
                }
                1 -> ProfileScreen()
                2 -> if (userDoc != null && userDoc!!.status == "VALIDATED") {
                    UserProfileTab(user = userDoc!!, onSignOut = { scope.launch { authVm.signOut() } })
                } else {
                    AuthScreen(
                        vm = authVm,
                        onLoginSuccess = { selectedTab = 0 },
                        onSignupSuccess = { showSignupBanner = true; selectedTab = 0 },
                    )
                }
            }
        }
        BottomNav(activeTab = selectedTab, onTabSelected = { selectedTab = it })
    }
}
