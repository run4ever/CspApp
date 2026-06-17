package fr.csp.app.ui.trace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.csp.app.ui.home.*
import fr.csp.app.ui.theme.CspColors

@Composable
fun TraceScreen(isAdmin: Boolean = false, isLoggedIn: Boolean = false, onLogin: () -> Unit = {}) {
    val vm = viewModel { TraceViewModel() }
    val traces by vm.traces.collectAsStateWithLifecycle()
    var showAddForm by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CspColors.Bg)
                .systemBarsPadding()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                IconRoute(tint = CspColors.Muted2, modifier = Modifier.size(48.dp), strokeWidth = 1.4f)
                Text(
                    "Vous devez être connecté.e pour accéder aux traces du club.",
                    style = TextStyle(fontSize = 15.sp, color = CspColors.Muted, lineHeight = (15 * 1.6).sp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Red)
                        .clickable(onClick = onLogin)
                        .padding(horizontal = 28.dp, vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Me connecter",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White),
                    )
                }
            }
        }
        return
    }

    // null = ordre par défaut, true = distance croissante, false = distance décroissante
    var distSort by remember { mutableStateOf<Boolean?>(null) }
    val sortedTraces = remember(traces, distSort) {
        when (distSort) {
            true -> traces.sortedBy { it.distanceKm }
            false -> traces.sortedByDescending { it.distanceKm }
            null -> traces
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CspColors.Bg)
                .systemBarsPadding(),
        ) {
            // En-tête
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Nos parcours",
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                )
                if (isAdmin) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CspColors.Red)
                            .clickable { showAddForm = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        IconCalPlus(tint = Color.White, modifier = Modifier.size(17.dp))
                    }
                }
            }

            // Tri par distance
            Row(
                modifier = Modifier
                    .padding(start = 18.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, if (distSort != null) CspColors.Blue else CspColors.Line, RoundedCornerShape(8.dp))
                    .background(if (distSort != null) CspColors.Blue.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable {
                        distSort = when (distSort) {
                            null -> true
                            true -> false
                            false -> null
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                IconMilestone(
                    tint = if (distSort != null) CspColors.Blue else CspColors.Muted,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    when (distSort) {
                        true -> "Distance ↑"
                        false -> "Distance ↓"
                        null -> "Distance"
                    },
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = if (distSort != null) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (distSort != null) CspColors.Blue else CspColors.Muted,
                    ),
                )
            }

            if (traces.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aucun parcours pour l'instant.",
                        style = TextStyle(fontSize = 14.sp, color = CspColors.Muted2),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    sortedTraces.forEach { trace ->
                        TraceCard(
                            trace = trace,
                            isAdmin = isAdmin,
                            onDelete = { vm.deleteTrace(trace.id) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        // Formulaire d'ajout (overlay)
        if (showAddForm) {
            AddTraceForm(
                onDismiss = { showAddForm = false },
                onConfirm = { title, dist, up, down, dur, url ->
                    vm.addTrace(title, dist, up, down, dur, url)
                    showAddForm = false
                },
            )
        }
    }
}

@Composable
private fun TraceCard(trace: Trace, isAdmin: Boolean, onDelete: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Supprimer ce parcours ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showConfirm = false; onDelete() }) {
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CspColors.Surface)
            .border(1.dp, CspColors.Line2, RoundedCornerShape(16.dp))
            .clickable(enabled = trace.url.isNotBlank()) {
                if (trace.url.isNotBlank()) uriHandler.openUri(trace.url)
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Titre + bouton supprimer
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                trace.title,
                modifier = Modifier.weight(1f),
                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink, lineHeight = (15 * 1.35).sp),
            )
            if (trace.url.isNotBlank()) {
                IconChevronRight(tint = CspColors.Muted2, modifier = Modifier.size(16.dp))
            }
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CspColors.Surface2)
                        .border(1.dp, CspColors.Line, CircleShape)
                        .clickable { showConfirm = true },
                    contentAlignment = Alignment.Center,
                ) {
                    IconTrash(tint = CspColors.Muted, modifier = Modifier.size(13.dp))
                }
            }
        }

        // Stats (on n'affiche que les données renseignées)
        val hasStats = trace.distanceKm > 0 || trace.elevationUp > 0 || trace.elevationDown > 0 || trace.durationMin > 0
        if (hasStats) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (trace.distanceKm > 0) StatChip(
                    icon = { IconMilestone(CspColors.Blue, Modifier.size(15.dp)) },
                    label = "${trace.distanceKm.formatKm()} km",
                )
                if (trace.elevationUp > 0) StatChip(icon = { IconElevUp(CspColors.Green, Modifier.size(15.dp)) }, label = "${trace.elevationUp} m")
                if (trace.elevationDown > 0) StatChip(icon = { IconElevDown(CspColors.Muted, Modifier.size(15.dp)) }, label = "${trace.elevationDown} m")
                if (trace.durationMin > 0) StatChip(icon = { IconClock(CspColors.Muted, Modifier.size(14.dp)) }, label = trace.durationMin.toHhMm())
            }
        }
    }
}

@Composable
private fun StatChip(icon: @Composable () -> Unit, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon()
        Text(label, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Ink2))
    }
}

// ── Formulaire d'ajout ────────────────────────────────────────

@Composable
private fun AddTraceForm(onDismiss: () -> Unit, onConfirm: (String, Double, Int, Int, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var elevUp by remember { mutableStateOf("") }
    var elevDown by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    val canSave = title.isNotBlank() && distance.isNotBlank() && url.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(CspColors.Bg)
                .clickable(enabled = false) {}
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("Ajouter un parcours", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink))

            FormField("Nom du parcours", title, { title = it })
            FormField("Distance (km)", distance, { distance = it }, KeyboardType.Decimal)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField("D+ (m)", elevUp, { elevUp = it }, KeyboardType.Number, Modifier.weight(1f))
                FormField("D- (m)", elevDown, { elevDown = it }, KeyboardType.Number, Modifier.weight(1f))
            }
            FormField("Durée (minutes)", duration, { duration = it }, KeyboardType.Number)
            FormField("Lien (Strava, Komoot…)", url, { url = it }, KeyboardType.Uri)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Surface2)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Annuler", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink2))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canSave) CspColors.Red else CspColors.Surface2)
                        .then(if (!canSave) Modifier.border(1.dp, CspColors.Line, RoundedCornerShape(12.dp)) else Modifier)
                        .clickable(enabled = canSave) {
                            onConfirm(
                                title,
                                distance.replace(",", ".").toDoubleOrNull() ?: 0.0,
                                elevUp.toIntOrNull() ?: 0,
                                elevDown.toIntOrNull() ?: 0,
                                duration.toIntOrNull() ?: 0,
                                url,
                            )
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Enregistrer",
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (canSave) Color.White else CspColors.Muted),
                    )
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CspColors.Surface)
                .border(1.dp, CspColors.Line, RoundedCornerShape(10.dp))
                .padding(horizontal = 13.dp, vertical = 11.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = CspColors.Ink),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        )
    }
}
