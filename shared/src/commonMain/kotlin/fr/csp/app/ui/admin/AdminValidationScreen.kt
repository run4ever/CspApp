package fr.csp.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import fr.csp.app.ui.home.IconComment
import fr.csp.app.ui.home.IconUserCog
import fr.csp.app.ui.home.IconX
import fr.csp.app.ui.theme.CspColors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── Modèle ────────────────────────────────────────────────────

data class AdminUser(
    val uid: String,
    val prenom: String,
    val nom: String,
    val email: String,
    val dateNaissance: String,
    val role: String,
    val status: String,
    val canComment: Boolean,
    val createdAt: Long,
)

private enum class MemberFilter(val label: String) {
    ALL("Tous"),
    PENDING("En attente"),
    BLOCKED("Bloqués"),
    ADMIN("Admins"),
}

// ── ViewModel ─────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel : ViewModel() {

    private val currentUid: StateFlow<String?> = Firebase.auth.authStateChanged
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val allUsers: StateFlow<List<AdminUser>?> = currentUid.flatMapLatest { myUid ->
        Firebase.firestore.collection("users").snapshots
            .map { snapshot ->
                snapshot.documents
                    .filter { it.id != myUid }
                    .mapNotNull { doc ->
                        try {
                            AdminUser(
                                uid = doc.id,
                                prenom = doc.get<String?>("prenom") ?: "",
                                nom = doc.get<String?>("nom") ?: "",
                                email = doc.get<String?>("email") ?: "",
                                dateNaissance = doc.get<String?>("date_naissance") ?: "",
                                role = doc.get<String?>("role") ?: "member",
                                status = doc.get<String?>("status") ?: "",
                                canComment = doc.get<Boolean?>("canComment") ?: true,
                                createdAt = doc.get<Long?>("createdAt") ?: 0L,
                            )
                        } catch (_: Exception) { null }
                    }
                    .sortedByDescending { it.createdAt }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    suspend fun validateUser(uid: String, email: String): String? = try {
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("status" to "VALIDATED"), merge = true)
        Firebase.auth.sendPasswordResetEmail(email)
        null
    } catch (e: Exception) { e.message ?: "Erreur lors de la validation" }

    suspend fun blockUser(uid: String): String? = try {
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("status" to "BLOCKED", "role" to "member", "canComment" to true), merge = true)
        null
    } catch (e: Exception) { e.message ?: "Erreur lors du blocage" }

    suspend fun setRole(uid: String, role: String): String? = try {
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("role" to role), merge = true)
        null
    } catch (e: Exception) { e.message ?: "Erreur lors de la modification du rôle" }

    suspend fun setCanComment(uid: String, value: Boolean): String? = try {
        Firebase.firestore.collection("users").document(uid)
            .set(mapOf("canComment" to value), merge = true)
        null
    } catch (e: Exception) { e.message ?: "Erreur lors de la modification" }
}

// ── Navigation interne ────────────────────────────────────────

private enum class AdminNav { List, EditUser }

@Composable
fun AdminValidationScreen(onBack: () -> Unit, openWithPendingFilter: Boolean = false) {
    val vm = viewModel { AdminViewModel() }
    var nav by remember { mutableStateOf(AdminNav.List) }
    var selectedUid by remember { mutableStateOf<String?>(null) }

    when (nav) {
        AdminNav.List -> MemberListScreen(
            vm = vm,
            onBack = onBack,
            onUserClick = { uid -> selectedUid = uid; nav = AdminNav.EditUser },
            initialFilter = if (openWithPendingFilter) MemberFilter.PENDING else MemberFilter.ALL,
        )
        AdminNav.EditUser -> UserEditScreen(
            uid = selectedUid ?: return,
            vm = vm,
            onBack = { nav = AdminNav.List },
        )
    }
}

// ── Liste des comptes ─────────────────────────────────────────

@Composable
private fun MemberListScreen(
    vm: AdminViewModel,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    initialFilter: MemberFilter = MemberFilter.ALL,
) {
    val allUsers by vm.allUsers.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf(initialFilter) }

    val filtered = remember(allUsers, filter) {
        allUsers?.let { users ->
            when (filter) {
                MemberFilter.ALL -> users
                MemberFilter.PENDING -> users.filter { it.status == "PENDING" }
                MemberFilter.BLOCKED -> users.filter { it.status == "BLOCKED" }
                MemberFilter.ADMIN -> users.filter { it.role == "admin" }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding(),
    ) {
        ScreenHeader(
            title = "Liste des comptes",
            subtitle = allUsers?.size?.let { n -> "$n compte${if (n > 1) "s" else ""}" },
            onBack = onBack,
        )

        // ── Filtres ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(MemberFilter.ALL, MemberFilter.PENDING, MemberFilter.BLOCKED, MemberFilter.ADMIN).forEach { f ->
                val active = f == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (active) CspColors.Red else CspColors.Surface)
                        .border(1.dp, if (active) CspColors.Red else CspColors.Line, RoundedCornerShape(999.dp))
                        .clickable { filter = f }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                ) {
                    Text(
                        f.label,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (active) Color.White else CspColors.Muted,
                        ),
                    )
                }
            }
        }

        // ── Contenu ───────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when {
                filtered == null -> LoadingBox()
                filtered.isEmpty() -> EmptyBox(message = "Aucun compte")
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    filtered.forEach { user ->
                        var blocking by remember(user.uid) { mutableStateOf(false) }
                        var validating by remember(user.uid) { mutableStateOf(false) }
                        var cardError by remember(user.uid) { mutableStateOf<String?>(null) }
                        AdminUserCard(
                            user = user,
                            validating = validating,
                            blocking = blocking,
                            error = cardError,
                            onClick = { onUserClick(user.uid) },
                            onValidate = if (user.status == "PENDING") {
                                {
                                    scope.launch {
                                        validating = true; cardError = null
                                        val err = vm.validateUser(user.uid, user.email)
                                        if (err != null) { cardError = err; validating = false }
                                    }
                                }
                            } else null,
                            onBlock = if (user.status == "PENDING") {
                                {
                                    scope.launch {
                                        blocking = true; cardError = null
                                        val err = vm.blockUser(user.uid)
                                        if (err != null) { cardError = err; blocking = false }
                                    }
                                }
                            } else null,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Édition d'un compte ───────────────────────────────────────

@Composable
private fun UserEditScreen(uid: String, vm: AdminViewModel, onBack: () -> Unit) {
    val allUsers by vm.allUsers.collectAsStateWithLifecycle()
    val user = allUsers?.find { it.uid == uid }
    val scope = rememberCoroutineScope()
    var feedback by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // En-tête
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
            Column {
                Text(
                    if (user != null) "${user.prenom} ${user.nom}".trim() else "Compte",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                )
                if (user != null && user.email.isNotBlank()) {
                    Text(user.email, style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
                }
            }
        }

        if (user == null) {
            LoadingBox()
            return@Column
        }

        // Infos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CspColors.Surface)
                .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (user.dateNaissance.isNotBlank()) {
                EditInfoRow("Date de naissance", user.dateNaissance)
                HorizontalDivider(color = CspColors.Line2)
            }
            EditInfoRow("Statut") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (user.role == "admin") StatusLabel("Admin", CspColors.Blue)
                    when (user.status) {
                        "PENDING" -> StatusLabel("En attente", Color(0xFFB8860B))
                        "BLOCKED" -> StatusLabel("Bloqué", CspColors.Red)
                        "VALIDATED" -> StatusLabel("Validé", CspColors.Green)
                    }
                }
            }
        }

        // Feedback
        feedback?.let {
            Text(
                it,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (it.startsWith("✓")) CspColors.Green else CspColors.Red,
                ),
            )
        }

        // Section Actions
        EditSection(title = "Actions") {
            if (user.status != "VALIDATED") {
                ActionButton(
                    label = "Valider le compte",
                    loading = busy,
                    enabled = !busy,
                    tint = CspColors.Green,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            busy = true; feedback = null
                            feedback = vm.validateUser(user.uid, user.email) ?: "✓ Compte validé"
                            busy = false
                        }
                    },
                    icon = { IconCheck(tint = CspColors.Green, modifier = Modifier.size(14.dp)) },
                )
            }
            if (user.status != "BLOCKED") {
                ActionButton(
                    label = "Bloquer le compte",
                    loading = busy,
                    enabled = !busy,
                    tint = CspColors.Red,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            busy = true; feedback = null
                            feedback = vm.blockUser(user.uid) ?: "✓ Compte bloqué"
                            busy = false
                        }
                    },
                    icon = { IconX(tint = CspColors.Red, modifier = Modifier.size(14.dp)) },
                )
            }
            if (user.status == "VALIDATED") {
                ActionButton(
                    label = if (user.canComment) "Interdire les commentaires" else "Autoriser les commentaires",
                    loading = busy,
                    enabled = !busy,
                    tint = if (user.canComment) CspColors.Red else CspColors.Green,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            busy = true; feedback = null
                            val next = !user.canComment
                            feedback = vm.setCanComment(user.uid, next)
                                ?: if (next) "✓ Commentaires autorisés" else "✓ Commentaires interdits"
                            busy = false
                        }
                    },
                    icon = {
                        val commentTint = if (user.canComment) CspColors.Red else CspColors.Green
                        IconComment(slashed = user.canComment, tint = commentTint, modifier = Modifier.size(14.dp))
                    },
                )
                ActionButton(
                    label = if (user.role == "admin") "Retirer les droits admin" else "Passer administrateur",
                    loading = busy,
                    enabled = !busy,
                    tint = if (user.role == "admin") CspColors.Muted else CspColors.Blue,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            busy = true; feedback = null
                            feedback = vm.setRole(user.uid, if (user.role == "admin") "member" else "admin")
                                ?: "✓ Rôle mis à jour"
                            busy = false
                        }
                    },
                    icon = {
                        val cogTint = if (user.role == "admin") CspColors.Muted else CspColors.Blue
                        IconUserCog(slashed = user.role == "admin", tint = cogTint, modifier = Modifier.size(14.dp))
                    },
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

// ── Carte utilisateur ─────────────────────────────────────────

@Composable
private fun AdminUserCard(
    user: AdminUser,
    validating: Boolean,
    blocking: Boolean,
    error: String?,
    onClick: () -> Unit,
    onValidate: (() -> Unit)?,
    onBlock: (() -> Unit)?,
) {
    val busy = validating || blocking
    val borderColor = when (user.status) {
        "BLOCKED" -> CspColors.Red.copy(alpha = 0.25f)
        "PENDING" -> Color(0xFFB8860B).copy(alpha = 0.35f)
        else -> CspColors.Line2
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CspColors.Surface)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CspColors.Surface3),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${user.prenom.firstOrNull() ?: ""}${user.nom.firstOrNull() ?: ""}".uppercase(),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Muted),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${user.prenom} ${user.nom}".trim(),
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Ink),
                )
                if (user.email.isNotBlank()) {
                    Text(user.email, style = TextStyle(fontSize = 12.sp, color = CspColors.Muted))
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (user.role == "admin") StatusLabel("Admin", CspColors.Blue)
                when (user.status) {
                    "BLOCKED" -> StatusLabel("Bloqué", CspColors.Red)
                    "PENDING" -> StatusLabel("En attente", Color(0xFFB8860B))
                }
            }
        }
        if (user.dateNaissance.isNotBlank()) {
            Text(
                "Né·e le ${user.dateNaissance}",
                style = TextStyle(fontSize = 12.sp, color = CspColors.Muted2),
            )
        }
        if (error != null) {
            Text(error, style = TextStyle(fontSize = 12.sp, color = CspColors.Red))
        }
        if (onValidate != null || onBlock != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onBlock != null) {
                    ActionButton(
                        label = "Bloquer",
                        loading = blocking,
                        enabled = !busy,
                        tint = CspColors.Red,
                        modifier = Modifier.weight(1f),
                        onClick = onBlock,
                        icon = { IconX(tint = CspColors.Red, modifier = Modifier.size(14.dp)) },
                    )
                }
                if (onValidate != null) {
                    ActionButton(
                        label = "Valider",
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
private fun EditSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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
                .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun EditInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
        Text(value, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Ink))
    }
}

@Composable
private fun EditInfoRow(label: String, content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = TextStyle(fontSize = 13.sp, color = CspColors.Muted))
        content()
    }
}

@Composable
private fun StatusLabel(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(text, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color))
    }
}

@Composable
private fun ActionButton(
    label: String,
    loading: Boolean,
    enabled: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)?,
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
