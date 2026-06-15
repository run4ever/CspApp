package fr.csp.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.ui.home.IconBack
import fr.csp.app.ui.home.IconCheck
import fr.csp.app.ui.home.IconChevronRight
import fr.csp.app.ui.home.IconX
import fr.csp.app.ui.theme.CspColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Modèle ────────────────────────────────────────────────────

data class PendingUser(
    val uid: String,
    val prenom: String,
    val nom: String,
    val email: String,
    val dateNaissance: String,
)

// ── ViewModel ─────────────────────────────────────────────────

class AdminValidationViewModel : ViewModel() {

    val pendingUsers: StateFlow<List<PendingUser>?> = usersWithStatus("PENDING")
    val blockedUsers: StateFlow<List<PendingUser>?> = usersWithStatus("BLOCKED")

    private fun usersWithStatus(status: String): StateFlow<List<PendingUser>?> =
        Firebase.firestore.collection("users")
            .where { "status" equalTo status }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    PendingUser(
                        uid = doc.id,
                        prenom = doc.get<String?>("prenom") ?: "",
                        nom = doc.get<String?>("nom") ?: "",
                        email = doc.get<String?>("email") ?: "",
                        dateNaissance = doc.get<String?>("date_naissance") ?: "",
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun validateUser(uid: String, email: String): String? = try {
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("status" to "VALIDATED"), merge = true)
        Firebase.auth.sendPasswordResetEmail(email)
        null
    } catch (e: Exception) {
        e.message ?: "Erreur lors de la validation"
    }

    suspend fun blockUser(uid: String): String? = try {
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("status" to "BLOCKED"), merge = true)
        null
    } catch (e: Exception) {
        e.message ?: "Erreur lors du blocage"
    }
}

// ── Écran principal (navigation interne) ──────────────────────

@Composable
fun AdminValidationScreen(onBack: () -> Unit) {
    val vm = viewModel { AdminValidationViewModel() }
    var showBlocked by remember { mutableStateOf(false) }

    if (showBlocked) {
        BlockedScreen(vm = vm, onBack = { showBlocked = false })
    } else {
        PendingScreen(vm = vm, onBack = onBack, onShowBlocked = { showBlocked = true })
    }
}

// ── Écran : comptes en attente ────────────────────────────────

@Composable
private fun PendingScreen(
    vm: AdminValidationViewModel,
    onBack: () -> Unit,
    onShowBlocked: () -> Unit,
) {
    val pendingUsers by vm.pendingUsers.collectAsStateWithLifecycle()
    val blockedUsers by vm.blockedUsers.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding(),
    ) {
        ScreenHeader(
            title = "Comptes en attente",
            subtitle = pendingUsers?.size?.let { n -> if (n > 0) "$n demande${if (n > 1) "s" else ""}" else null },
            onBack = onBack,
        )

        when {
            pendingUsers == null -> LoadingBox()
            pendingUsers!!.isEmpty() -> EmptyBox(message = "Aucun compte en attente")
            else -> {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        pendingUsers!!.forEach { user ->
                            var blocking by remember(user.uid) { mutableStateOf(false) }
                            var validating by remember(user.uid) { mutableStateOf(false) }
                            var cardError by remember(user.uid) { mutableStateOf<String?>(null) }
                            PendingUserCard(
                                user = user,
                                validating = validating,
                                blocking = blocking,
                                error = cardError,
                                onValidate = {
                                    scope.launch {
                                        validating = true; cardError = null
                                        val err = vm.validateUser(user.uid, user.email)
                                        if (err != null) { cardError = err; validating = false }
                                    }
                                },
                                onBlock = {
                                    scope.launch {
                                        blocking = true; cardError = null
                                        val err = vm.blockUser(user.uid)
                                        if (err != null) { cardError = err; blocking = false }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        // ── Bouton "Voir les comptes bloqués" ─────────────────
        val blockedCount = blockedUsers?.size ?: 0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CspColors.Bg)
                .border(width = 1.dp, color = CspColors.Line, shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                .clickable(onClick = onShowBlocked)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                if (blockedCount > 0) "Comptes bloqués ($blockedCount)" else "Comptes bloqués",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted),
            )
            IconChevronRight(tint = CspColors.Muted2, modifier = Modifier.size(15.dp))
        }
    }
}

// ── Écran : comptes bloqués ───────────────────────────────────

@Composable
private fun BlockedScreen(vm: AdminValidationViewModel, onBack: () -> Unit) {
    val blockedUsers by vm.blockedUsers.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding(),
    ) {
        ScreenHeader(
            title = "Comptes bloqués",
            subtitle = blockedUsers?.size?.let { n -> if (n > 0) "$n compte${if (n > 1) "s" else ""}" else null },
            onBack = onBack,
        )

        when {
            blockedUsers == null -> LoadingBox()
            blockedUsers!!.isEmpty() -> EmptyBox(message = "Aucun compte bloqué")
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                blockedUsers!!.forEach { user ->
                    var validating by remember(user.uid) { mutableStateOf(false) }
                    var cardError by remember(user.uid) { mutableStateOf<String?>(null) }
                    BlockedUserCard(
                        user = user,
                        validating = validating,
                        error = cardError,
                        onValidate = {
                            scope.launch {
                                validating = true; cardError = null
                                val err = vm.validateUser(user.uid, user.email)
                                if (err != null) { cardError = err; validating = false }
                            }
                        },
                    )
                }
            }
        }
    }
}

// ── Composables partagés ──────────────────────────────────────

@Composable
private fun ScreenHeader(title: String, subtitle: String?, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
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
        Column {
            Text(title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = CspColors.Ink))
            if (subtitle != null) {
                Text(subtitle, style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CspColors.Red, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun EmptyBox(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(CspColors.Surface2),
                contentAlignment = Alignment.Center,
            ) {
                IconCheck(tint = CspColors.Green, modifier = Modifier.size(24.dp))
            }
            Text(message, style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Muted))
        }
    }
}

@Composable
private fun UserCardHeader(user: PendingUser) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(CspColors.Surface3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${user.prenom.firstOrNull() ?: ""}${user.nom.firstOrNull() ?: ""}".uppercase(),
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Muted),
            )
        }
        Column {
            Text("${user.prenom} ${user.nom}", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Ink))
            Text(user.email, style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
        }
    }
    if (user.dateNaissance.isNotBlank()) {
        Text(
            "Né·e le ${user.dateNaissance}",
            style = TextStyle(fontSize = 13.sp, color = CspColors.Muted2),
            modifier = Modifier.padding(start = 54.dp),
        )
    }
}

// ── Carte : compte en attente ─────────────────────────────────

@Composable
private fun PendingUserCard(
    user: PendingUser,
    validating: Boolean,
    blocking: Boolean,
    error: String?,
    onValidate: () -> Unit,
    onBlock: () -> Unit,
) {
    val busy = validating || blocking
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CspColors.Surface)
            .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UserCardHeader(user)
        if (error != null) {
            Text(error, style = TextStyle(fontSize = 12.sp, color = CspColors.Red))
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Bloquer
            ActionButton(
                label = "Bloquer",
                loading = blocking,
                enabled = !busy,
                tint = CspColors.Red,
                modifier = Modifier.weight(1f),
                onClick = onBlock,
                icon = { IconX(tint = CspColors.Red, modifier = Modifier.size(14.dp)) },
            )
            // Valider
            ActionButton(
                label = "Valider le compte",
                loading = validating,
                enabled = !busy,
                tint = CspColors.Green,
                modifier = Modifier.weight(1f),
                onClick = onValidate,
                icon = { IconCheck(tint = CspColors.Green, modifier = Modifier.size(14.dp)) },
            )
        }
    }
}

// ── Carte : compte bloqué ─────────────────────────────────────

@Composable
private fun BlockedUserCard(
    user: PendingUser,
    validating: Boolean,
    error: String?,
    onValidate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CspColors.Surface)
            .border(1.dp, CspColors.Red.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UserCardHeader(user)
        if (error != null) {
            Text(error, style = TextStyle(fontSize = 12.sp, color = CspColors.Red))
        }
        Spacer(Modifier.height(4.dp))
        ActionButton(
            label = "Valider le compte",
            loading = validating,
            enabled = !validating,
            tint = CspColors.Green,
            modifier = Modifier.fillMaxWidth(),
            onClick = onValidate,
            icon = { IconCheck(tint = CspColors.Green, modifier = Modifier.size(14.dp)) },
        )
    }
}

// ── Bouton d'action générique ─────────────────────────────────

@Composable
private fun ActionButton(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.10f))
            .border(1.dp, tint.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = tint, modifier = Modifier.size(17.dp), strokeWidth = 2.dp)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                icon?.invoke()
                Text(label, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = tint))
            }
        }
    }
}
