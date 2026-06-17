package fr.csp.app.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import fr.csp.app.ui.auth.AuthScreen
import fr.csp.app.ui.auth.AuthViewModel
import fr.csp.app.ui.home.IconBack
import fr.csp.app.ui.home.IconChevronRight
import fr.csp.app.ui.home.IconPencil
import fr.csp.app.ui.home.IconSettings
import fr.csp.app.ui.home.IconShield
import fr.csp.app.ui.home.IconUser
import fr.csp.app.ui.home.IconX
import fr.csp.app.ui.home.UserDoc
import fr.csp.app.ui.theme.CspColors
import fr.csp.app.util.compressImageToJpeg
import fr.csp.app.util.toStorageData
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch

private enum class MenuNav { Main, Profile, Auth }

@Composable
fun MenuScreen(
    userDoc: UserDoc?,
    isAdmin: Boolean,
    authVm: AuthViewModel,
    onClose: () -> Unit,
    onMemberManagement: () -> Unit,
    onSignOut: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSignupSuccess: () -> Unit,
    startOnAuth: Boolean = false,
    onAuthStartHandled: () -> Unit = {},
) {
    val isLoggedIn = userDoc?.status == "VALIDATED"
    val scope = rememberCoroutineScope()
    var nav by remember { mutableStateOf(MenuNav.Main) }

    LaunchedEffect(startOnAuth) {
        if (startOnAuth) {
            nav = MenuNav.Auth
            onAuthStartHandled()
        }
    }

    when {
        nav == MenuNav.Auth -> AuthScreen(
            vm = authVm,
            onLoginSuccess = { nav = MenuNav.Main; onLoginSuccess() },
            onSignupSuccess = { nav = MenuNav.Main; onSignupSuccess() },
        )
        nav == MenuNav.Profile && isLoggedIn -> ProfileSubScreen(
            user = userDoc!!,
            onBack = { nav = MenuNav.Main },
            onSignOut = {
                scope.launch { authVm.signOut() }
                onSignOut()
            },
        )
        else -> MenuContent(
            user = userDoc,
            isAdmin = isAdmin && isLoggedIn,
            onClose = onClose,
            onMemberManagement = onMemberManagement,
            onProfileClick = { nav = MenuNav.Profile },
            onAuthClick = { nav = MenuNav.Auth },
        )
    }
}

// ── Contenu principal ─────────────────────────────────────────

@Composable
private fun MenuContent(
    user: UserDoc?,
    isAdmin: Boolean,
    onClose: () -> Unit,
    onMemberManagement: () -> Unit,
    onProfileClick: () -> Unit,
    onAuthClick: () -> Unit,
) {
    val initials = "${user?.prenom?.firstOrNull() ?: ""}${user?.nom?.firstOrNull() ?: ""}".uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "Menu",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
            )
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CspColors.Surface2)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                IconX(tint = CspColors.Muted, modifier = Modifier.size(14.dp))
            }
        }

        // ── Section Compte ────────────────────────────────────
        MenuSection(title = "Compte") {
            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CspColors.Blue),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!user.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                initials,
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.CyanInk),
                            )
                        }
                    }
                    Column {
                        Text(
                            "${user.prenom} ${user.nom}".trim(),
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Ink),
                        )
                        if (user.email.isNotBlank()) {
                            Text(
                                user.email,
                                style = TextStyle(fontSize = 13.sp, color = CspColors.Muted),
                            )
                        }
                    }
                }
                HorizontalDivider(color = CspColors.Line2)
                MenuRow(
                    label = "Mon profil",
                    icon = { IconUser(tint = CspColors.Muted, modifier = Modifier.size(18.dp)) },
                    onClick = onProfileClick,
                )
            } else {
                MenuRow(
                    label = "Se connecter / Créer un compte",
                    icon = { IconUser(tint = CspColors.Muted, modifier = Modifier.size(18.dp)) },
                    onClick = onAuthClick,
                )
            }
        }

        // ── Section Admin ─────────────────────────────────────
        if (isAdmin) {
            MenuSection(title = "Fonctionnalités Admin") {
                MenuRow(
                    label = "Gestion des membres",
                    icon = { IconShield(tint = CspColors.Muted, modifier = Modifier.size(18.dp)) },
                    onClick = onMemberManagement,
                )
                HorizontalDivider(color = CspColors.Line2)
                MenuRow(
                    label = "Paramètres de l'application",
                    icon = { IconSettings(tint = CspColors.Muted, modifier = Modifier.size(18.dp)) },
                    onClick = null,
                )
            }
        }
    }
}

// ── Page profil ───────────────────────────────────────────────

@Composable
private fun ProfileSubScreen(
    user: UserDoc,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val initials = "${user.prenom.firstOrNull() ?: ""}${user.nom.firstOrNull() ?: ""}".uppercase()

    val launcher = rememberFilePickerLauncher(type = PickerType.Image) { file ->
        file ?: return@rememberFilePickerLauncher
        scope.launch {
            uploading = true
            uploadError = null
            try {
                val uid = Firebase.auth.currentUser?.uid ?: return@launch
                val bytes = file.readBytes()
                val compressed = compressImageToJpeg(bytes, maxPx = 512, quality = 85)
                val ref = Firebase.storage.reference.child("avatars/$uid.jpg")
                ref.putData(compressed.toStorageData())
                val url = ref.getDownloadUrl()
                Firebase.firestore.collection("users").document(uid)
                    .set(mapOf("photoUrl" to url), merge = true)
            } catch (e: Exception) {
                uploadError = "Erreur lors du chargement de la photo"
            } finally {
                uploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CspColors.Surface)
                    .border(1.dp, CspColors.Line2, RoundedCornerShape(12.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                IconBack(tint = CspColors.Ink, modifier = Modifier.size(18.dp))
            }
            Text(
                "Mon profil",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
            )
        }

        // Avatar cliquable
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(CspColors.Blue)
                    .clickable(enabled = !uploading) { launcher.launch() },
                contentAlignment = Alignment.Center,
            ) {
                if (!user.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Text(
                        initials,
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.CyanInk),
                    )
                }
                if (uploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp,
                        )
                    }
                }
            }
            // Badge crayon
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(CspColors.Surface)
                    .border(1.5.dp, CspColors.Line, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                IconPencil(tint = CspColors.Ink, modifier = Modifier.size(13.dp))
            }
        }

        if (uploadError != null) {
            Text(
                uploadError!!,
                style = TextStyle(fontSize = 13.sp, color = CspColors.Red),
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CspColors.Surface)
                .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProfileField("Prénom", user.prenom)
            HorizontalDivider(color = CspColors.Line2)
            ProfileField("Nom", user.nom)
            HorizontalDivider(color = CspColors.Line2)
            ProfileField("Date de naissance", user.dateNaissance)
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

// ── Composables partagés ──────────────────────────────────────

@Composable
private fun MenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title.uppercase(),
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CspColors.Muted,
                letterSpacing = (11 * 0.08).sp,
            ),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CspColors.Surface)
                .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp)),
            content = content,
        )
    }
}

@Composable
private fun MenuRow(
    label: String,
    icon: @Composable () -> Unit,
    onClick: (() -> Unit)?,
) {
    val enabled = onClick != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick!!) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) CspColors.Ink else CspColors.Muted2,
            ),
        )
        IconChevronRight(
            tint = if (enabled) CspColors.Muted2 else CspColors.Line,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
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
