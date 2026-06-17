package fr.csp.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import fr.csp.app.data.EventRepository
import fr.csp.app.data.FavoriteLocation
import fr.csp.app.data.FavoriteRepository
import fr.csp.app.data.TraceRepository
import fr.csp.app.ui.trace.Trace
import fr.csp.app.ui.trace.formatKm
import fr.csp.app.data.LocationSuggestion
import fr.csp.app.data.NominatimService
import fr.csp.app.ui.frenchCalendarLocale
import fr.csp.app.ui.event.MapPicker
import fr.csp.app.ui.home.ClubEvent
import fr.csp.app.ui.home.IconBack
import fr.csp.app.ui.home.IconChevronRight
import fr.csp.app.ui.home.IconPin
import fr.csp.app.ui.theme.CspColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn

private const val DEFAULT_LAT = 48.84470902674985
private const val DEFAULT_LON = 2.4364809007277324

private fun dateSortToFr(dateSort: String): String {
    val parts = dateSort.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else ""
}

private fun dateFrToMillis(dateStr: String): Long? = try {
    val (d, m, y) = dateStr.split("/")
    LocalDate(y.toInt(), m.toInt(), d.toInt()).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
} catch (_: Exception) { null }

private fun parseTimeFr(timeStr: String): Pair<Int, Int>? = try {
    val (h, m) = timeStr.split("h")
    Pair(h.toInt(), m.toInt())
} catch (_: Exception) { null }

private val EVENT_TYPES = listOf(
    "Sortie hebdo CSP",
    "Evenement CSP",
    "Sortie annuelle CSP",
    "Evenement autre club",
    "Cyclosportive",
)

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.clickable(onClick = onBack).padding(4.dp)) {
                IconBack(tint = CspColors.Ink, modifier = Modifier.size(22.dp))
            }
            Text(
                "Créer un événement",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
            )
        }
        CreateEventForm(onDone = onBack)
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun EditEventScreen(event: ClubEvent, onDone: () -> Unit) {
    var editScope by remember { mutableStateOf<EditScope?>(null) }

    // Si l'événement appartient à une série, demander la portée avant d'afficher le formulaire
    if (event.seriesId != null && editScope == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CspColors.Bg)
                .systemBarsPadding(),
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
                Text(
                    "Modifier la série",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                )
                Text(
                    "Cet événement fait partie d'une série. Que souhaitez-vous modifier ?",
                    style = TextStyle(fontSize = 14.sp, color = CspColors.Ink2, lineHeight = (14 * 1.5).sp),
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Red)
                        .clickable { editScope = EditScope.SERIES }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cet événement et les suivants", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Surface3)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                        .clickable { editScope = EditScope.SINGLE }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Cet événement uniquement", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink2))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CspColors.Surface3)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                        .clickable { onDone() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Annuler", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(modifier = Modifier.clickable(onClick = onDone).padding(4.dp)) {
                IconBack(tint = CspColors.Ink, modifier = Modifier.size(22.dp))
            }
            Column {
                Text(
                    "Modifier l'événement",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
                )
                if (editScope == EditScope.SERIES) {
                    Text(
                        "Cet événement et les suivants seront modifiés",
                        style = TextStyle(fontSize = 12.sp, color = CspColors.Red, fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
        CreateEventForm(
            initialEvent = event,
            onDone = onDone,
            seriesScope = editScope,
        )
        Spacer(Modifier.height(40.dp))
    }
}

private enum class EditScope { SINGLE, SERIES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventForm(initialEvent: ClubEvent? = null, onDone: (() -> Unit)? = null, seriesScope: EditScope? = null) {
    val isEditing = initialEvent != null
    val initDate = remember { initialEvent?.let { dateSortToFr(it.dateSort) } ?: "" }
    val initTime = remember { initialEvent?.time?.let { parseTimeFr(it) } }

    var name by remember { mutableStateOf(initialEvent?.title ?: "") }
    var type by remember { mutableStateOf(initialEvent?.type ?: "") }
    var date by remember { mutableStateOf(initDate) }
    var time by remember { mutableStateOf(initialEvent?.time ?: "") }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var locationLat by remember { mutableStateOf(initialEvent?.lat) }
    var locationLon by remember { mutableStateOf(initialEvent?.lon) }
    var mapCenterKey by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var savingFavorite by remember { mutableStateOf(false) }
    var favoriteName by remember { mutableStateOf("") }
    var favoriteSaved by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(initialEvent?.status?.name ?: "OPEN") }
    var isRecurring by remember { mutableStateOf(false) }
    var repeatUntil by remember { mutableStateOf("") }
    var showRepeatDatePicker by remember { mutableStateOf(false) }
    val repeatDatePickerState = remember { DatePickerState(locale = frenchCalendarLocale(), initialSelectedDateMillis = null) }
    val isHebdo = type == "Sortie hebdo CSP"
    var isFirstRender by remember { mutableStateOf(true) }

    // Auto-remplissage de la description selon le type (création uniquement)
    LaunchedEffect(type) {
        if (isFirstRender) { isFirstRender = false; return@LaunchedEffect }
        if (!isEditing) {
            if (type == "Sortie hebdo CSP") {
                try {
                    val doc = Firebase.firestore
                        .collection("club_training_descriptions")
                        .document("saturday_training")
                        .get()
                    name = doc.get<String?>("title") ?: name
                    description = doc.get<String?>("description") ?: ""
                } catch (_: Exception) { description = "" }
            } else {
                name = ""
                description = ""
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = remember { DatePickerState(locale = frenchCalendarLocale(), initialSelectedDateMillis = dateFrToMillis(initDate)) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(initialHour = initTime?.first ?: 8, initialMinute = initTime?.second ?: 0, is24Hour = true)

    var selectedTraceIds by remember { mutableStateOf<List<String>>(initialEvent?.traceIds ?: emptyList()) }

    val scope = rememberCoroutineScope()
    val repository = remember { EventRepository() }
    val favoriteRepo = remember { FavoriteRepository() }
    val traceRepo = remember { TraceRepository() }
    val favorites by favoriteRepo.getFavorites().collectAsState(initial = emptyList())
    val allTraces by traceRepo.getTraces().collectAsState(initial = emptyList())

    val formValid = name.isNotBlank() && type.isNotBlank() && date.isNotBlank() && time.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CspDropdown(
            label = "Type d'événement",
            selected = type,
            options = EVENT_TYPES,
            onSelect = { type = it; feedback = null },
        )
        CspField(
            label = "Nom de l'événement",
            value = name,
            placeholder = "ex. Sortie du dimanche",
            onValueChange = { name = it; feedback = null },
        )
        CspTextArea(
            label = "Description",
            value = description,
            placeholder = "Programme, infos pratiques… (Markdown : **gras**, - liste)",
            onValueChange = { description = it; feedback = null },
        )

        if (isEditing) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Statut", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("OPEN" to "Ouvert", "CANCELLED" to "Annulé", "FULL" to "Complet").forEach { (value, label) ->
                        val selected = status == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) when (value) { "CANCELLED" -> CspColors.Red; "FULL" -> CspColors.Surface3; else -> CspColors.Green } else CspColors.Surface)
                                .border(1.dp, if (selected) when (value) { "CANCELLED" -> CspColors.Red; "FULL" -> CspColors.Line; else -> CspColors.Green } else CspColors.Line, RoundedCornerShape(10.dp))
                                .clickable { status = value }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) when (value) { "FULL" -> CspColors.Ink; else -> Color.White } else CspColors.Muted,
                                ),
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                CspReadonlyField(
                    label = "Date",
                    value = date,
                    placeholder = "jj/mm/aaaa",
                    onClick = { showDatePicker = true },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                CspReadonlyField(
                    label = "Heure",
                    value = time,
                    placeholder = "08h00",
                    onClick = { showTimePicker = true },
                )
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            date = millisToDateStr(millis)
                            feedback = null
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
                },
            ) {
                DatePicker(
                    state = datePickerState,
                    title = { Text("Sélectionnez une date", modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)) },
                    headline = {
                        Text(
                            text = datePickerState.selectedDateMillis?.let { millisToDateStr(it) } ?: "Date choisie",
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Normal),
                        )
                    },
                )
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        time = timeToStr(timePickerState.hour, timePickerState.minute)
                        feedback = null
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Annuler") }
                },
                text = { TimePicker(state = timePickerState) },
            )
        }

        // Récurrence (hebdo uniquement, création uniquement)
        if (isHebdo && !isEditing) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isRecurring) CspColors.RedSoft else CspColors.Surface)
                        .border(1.dp, if (isRecurring) CspColors.Red else CspColors.Line, RoundedCornerShape(12.dp))
                        .clickable { isRecurring = !isRecurring; if (!isRecurring) repeatUntil = "" }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "Événement récurrent",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CspColors.Ink),
                        )
                        Text(
                            "Crée une sortie chaque semaine jusqu'à la date de fin",
                            style = TextStyle(fontSize = 12.sp, color = CspColors.Muted, lineHeight = (12 * 1.4).sp),
                        )
                    }
                    // Toggle visuel
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 26.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (isRecurring) CspColors.Red else CspColors.Line),
                        contentAlignment = if (isRecurring) Alignment.CenterEnd else Alignment.CenterStart,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Color.White),
                        )
                    }
                }
                if (isRecurring) {
                    CspReadonlyField(
                        label = "Répéter jusqu'au",
                        value = repeatUntil,
                        placeholder = "jj/mm/aaaa",
                        onClick = { showRepeatDatePicker = true },
                    )
                }
            }
        }

        if (showRepeatDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showRepeatDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        repeatDatePickerState.selectedDateMillis?.let { millis ->
                            repeatUntil = millisToDateStr(millis)
                        }
                        showRepeatDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showRepeatDatePicker = false }) { Text("Annuler") }
                },
            ) {
                DatePicker(
                    state = repeatDatePickerState,
                    title = { Text("Date de fin de la série", modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)) },
                    headline = {
                        Text(
                            text = repeatDatePickerState.selectedDateMillis?.let { millisToDateStr(it) } ?: "Date choisie",
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Normal),
                        )
                    },
                )
            }
        }

        // Sélecteur de parcours (désactivé si récurrence activée)
        if (allTraces.isNotEmpty()) {
            TracePicker(
                traces = allTraces,
                selected = selectedTraceIds,
                disabled = isRecurring,
                onToggle = { id ->
                    selectedTraceIds = if (id in selectedTraceIds) selectedTraceIds - id
                    else if (selectedTraceIds.size < 3) selectedTraceIds + id
                    else selectedTraceIds
                },
            )
        }

        // Lieux favoris
        if (favorites.isNotEmpty()) {
            FavoritesRow(
                favorites = favorites,
                onSelect = { fav ->
                    location = fav.address
                    locationLat = fav.lat
                    locationLon = fav.lon
                    savingFavorite = false
                    favoriteSaved = false
                    mapCenterKey++
                    feedback = null
                },
            )
        }

        // Champ lieu (texte + autocomplétion)
        LocationField(
            value = location,
            lat = locationLat,
            lon = locationLon,
            onValueChange = { text ->
                location = text
                locationLat = null
                locationLon = null
                savingFavorite = false
                favoriteSaved = false
            },
            onSuggestionSelected = { suggestion ->
                location = suggestion.displayName
                locationLat = suggestion.lat
                locationLon = suggestion.lon
                savingFavorite = false
                favoriteSaved = false
                mapCenterKey++
                feedback = null
            },
        )

        // Confirmation de position + enregistrement favori
        if (locationLat != null && locationLon != null) {
            if (!savingFavorite) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "✓ Position confirmée",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CspColors.Green),
                    )
                    Spacer(Modifier.weight(1f))
                    if (favoriteSaved) {
                        Text(
                            "★ Enregistré comme favori",
                            style = TextStyle(fontSize = 12.sp, color = CspColors.Green),
                        )
                    } else {
                        Text(
                            "☆ Enregistrer comme favori",
                            style = TextStyle(fontSize = 12.sp, color = CspColors.Muted),
                            modifier = Modifier.clickable { savingFavorite = true; favoriteName = "" },
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CspField(
                        label = "Nom du lieu favori",
                        value = favoriteName,
                        placeholder = "ex. Vélodrome CSP",
                        onValueChange = { favoriteName = it },
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CspColors.Surface3)
                                .clickable { savingFavorite = false }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Annuler", style = TextStyle(fontSize = 14.sp, color = CspColors.Muted))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (favoriteName.isNotBlank()) CspColors.Surface else CspColors.Surface3)
                                .border(
                                    1.dp,
                                    if (favoriteName.isNotBlank()) CspColors.Green else CspColors.Line,
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable(enabled = favoriteName.isNotBlank()) {
                                    scope.launch {
                                        favoriteRepo.addFavorite(
                                            name = favoriteName,
                                            address = location,
                                            lat = locationLat!!,
                                            lon = locationLon!!,
                                        )
                                        savingFavorite = false
                                        favoriteName = ""
                                        favoriteSaved = true
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Enregistrer",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (favoriteName.isNotBlank()) CspColors.Green else CspColors.Muted2,
                                ),
                            )
                        }
                    }
                }
            }
        }

        // Séparateur
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CspColors.Line)
            Text(
                "ou positionner le pointeur",
                style = TextStyle(fontSize = 11.sp, color = CspColors.Muted2),
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = CspColors.Line)
        }

        // Carte de sélection
        MapPicker(
            lat = locationLat ?: DEFAULT_LAT,
            lon = locationLon ?: DEFAULT_LON,
            centerKey = mapCenterKey,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(14.dp)),
            onLocationChanged = { newLat, newLon ->
                locationLat = newLat
                locationLon = newLon
                savingFavorite = false
                favoriteSaved = false
                scope.launch {
                    val suggestion = NominatimService.reverse(newLat, newLon)
                    if (suggestion != null) location = suggestion.displayName
                }
            },
        )

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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (formValid) CspColors.Red else CspColors.Surface3)
                .clickable(enabled = formValid && !isLoading) {
                    scope.launch {
                        isLoading = true
                        feedback = null
                        try {
                            if (isEditing && initialEvent != null) {
                                if (seriesScope == EditScope.SERIES && initialEvent.seriesId != null) {
                                    repository.updateEventSeries(
                                        seriesId = initialEvent.seriesId,
                                        fromDateSort = initialEvent.dateSort,
                                        title = name,
                                        type = type,
                                        time = time,
                                        location = location,
                                        lat = locationLat,
                                        lon = locationLon,
                                        description = description,
                                    )
                                    feedback = "✓ Série modifiée"
                                } else {
                                    repository.updateEvent(
                                        id = initialEvent.id,
                                        title = name,
                                        type = type,
                                        date = date,
                                        time = time,
                                        location = location,
                                        lat = locationLat,
                                        lon = locationLon,
                                        description = description,
                                        traceIds = selectedTraceIds,
                                        status = status,
                                    )
                                    feedback = "✓ Événement modifié"
                                }
                                onDone?.invoke()
                            } else {
                                repository.createEvent(
                                    title = name,
                                    type = type,
                                    date = date,
                                    time = time,
                                    location = location,
                                    lat = locationLat,
                                    lon = locationLon,
                                    description = description,
                                    repeatUntil = if (isRecurring && repeatUntil.isNotBlank()) repeatUntil else null,
                                    traceIds = selectedTraceIds,
                                )
                                onDone?.invoke()
                            }
                        } catch (e: Exception) {
                            feedback = "Erreur : ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CspColors.Ink, strokeWidth = 2.dp)
            } else {
                Text(
                    if (isEditing) "Enregistrer les modifications" else "Créer l'événement",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Ink),
                )
            }
        }
    }
}

@Composable
private fun FavoritesRow(
    favorites: List<FavoriteLocation>,
    onSelect: (FavoriteLocation) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Lieux favoris",
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted),
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            favorites.forEach { fav ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(CspColors.Surface)
                        .border(1.dp, CspColors.Line, RoundedCornerShape(999.dp))
                        .clickable { onSelect(fav) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "★ ${fav.name}",
                        style = TextStyle(fontSize = 13.sp, color = CspColors.Ink),
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationField(
    value: String,
    lat: Double?,
    lon: Double?,
    onValueChange: (String) -> Unit,
    onSuggestionSelected: (LocationSuggestion) -> Unit,
) {
    var suggestions by remember { mutableStateOf<List<LocationSuggestion>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (lat != null || value.length < 3) {
            suggestions = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(500)
        try {
            suggestions = NominatimService.search(value)
        } catch (_: Exception) {
            suggestions = emptyList()
        } finally {
            isSearching = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Lieu", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))

        val hasDropdown = suggestions.isNotEmpty()
        val borderColor = when {
            lat != null && lon != null -> CspColors.Green
            hasDropdown -> CspColors.Red
            else -> CspColors.Line
        }
        val fieldShape = RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomEnd = if (hasDropdown) 0.dp else 12.dp,
            bottomStart = if (hasDropdown) 0.dp else 12.dp,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(fieldShape)
                .border(1.dp, borderColor, fieldShape)
                .background(CspColors.Surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconPin(
                    tint = if (lat != null) CspColors.Green else CspColors.Muted2,
                    modifier = Modifier.size(16.dp),
                )
                androidx.compose.material3.OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = {
                        Text(
                            "ex. Parking Colette, Pantin",
                            style = TextStyle(fontSize = 15.sp, color = CspColors.Muted2),
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedTextColor = CspColors.Ink,
                        unfocusedTextColor = CspColors.Ink,
                        cursorColor = CspColors.Red,
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    textStyle = TextStyle(fontSize = 15.sp, color = CspColors.Ink),
                )
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = CspColors.Muted,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .border(1.dp, CspColors.Line, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .background(CspColors.Surface),
            ) {
                suggestions.forEachIndexed { i, suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionSelected(suggestion) }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        IconPin(tint = CspColors.Red, modifier = Modifier.size(14.dp))
                        Text(
                            text = suggestion.displayName,
                            style = TextStyle(fontSize = 13.5.sp, color = CspColors.Ink, lineHeight = (13.5 * 1.4).sp),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (i < suggestions.lastIndex) {
                        HorizontalDivider(color = CspColors.Line2, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CspTextArea(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = TextStyle(fontSize = 14.sp, color = CspColors.Muted2)) },
            singleLine = false,
            minLines = 4,
            maxLines = 10,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CspColors.Red,
                unfocusedBorderColor = CspColors.Line,
                focusedTextColor = CspColors.Ink,
                unfocusedTextColor = CspColors.Ink,
                cursorColor = CspColors.Red,
                focusedContainerColor = CspColors.Surface,
                unfocusedContainerColor = CspColors.Surface,
            ),
            textStyle = TextStyle(fontSize = 14.sp, color = CspColors.Ink, lineHeight = (14 * 1.6).sp),
        )
    }
}

@Composable
private fun CspField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = TextStyle(fontSize = 15.sp, color = CspColors.Muted2)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CspColors.Red,
                unfocusedBorderColor = CspColors.Line,
                focusedTextColor = CspColors.Ink,
                unfocusedTextColor = CspColors.Ink,
                cursorColor = CspColors.Red,
                focusedContainerColor = CspColors.Surface,
                unfocusedContainerColor = CspColors.Surface,
            ),
            textStyle = TextStyle(fontSize = 15.sp, color = CspColors.Ink),
        )
    }
}

private fun millisToDateStr(millis: Long): String {
    val local = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC)
    @Suppress("DEPRECATION")
    val d = local.dayOfMonth.toString().padStart(2, '0')
    @Suppress("DEPRECATION")
    val m = local.monthNumber.toString().padStart(2, '0')
    return "$d/$m/${local.year}"
}

private fun timeToStr(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}h${minute.toString().padStart(2, '0')}"

@Composable
private fun CspReadonlyField(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                .background(CspColors.Surface)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 16.dp),
        ) {
            Text(
                text = value.ifEmpty { placeholder },
                style = TextStyle(
                    fontSize = 15.sp,
                    color = if (value.isEmpty()) CspColors.Muted2 else CspColors.Ink,
                ),
            )
        }
    }
}

@Composable
private fun TracePicker(
    traces: List<Trace>,
    selected: List<String>,
    disabled: Boolean,
    onToggle: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Parcours associés",
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (disabled) CspColors.Muted2 else CspColors.Muted),
            )
            Text(
                "${selected.size}/3",
                style = TextStyle(fontSize = 12.sp, color = if (disabled) CspColors.Muted2 else CspColors.Muted),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CspColors.Line, RoundedCornerShape(12.dp))
                .background(if (disabled) CspColors.Surface else CspColors.Surface),
        ) {
            traces.forEachIndexed { index, trace ->
                val isSelected = trace.id in selected
                val isDisabled = disabled || (!isSelected && selected.size >= 3)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { if (!isDisabled) it.clickable { onToggle(trace.id) } else it }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Checkbox visuelle
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isSelected && !disabled -> CspColors.Red
                                    else -> CspColors.Surface3
                                }
                            )
                            .border(1.dp, if (isSelected && !disabled) CspColors.Red else CspColors.Line, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSelected && !disabled) {
                            Text("✓", style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            trace.title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (disabled || isDisabled) CspColors.Muted2 else CspColors.Ink,
                            ),
                        )
                        Text(
                            "${trace.distanceKm.formatKm()} km · D+ ${trace.elevationUp} m",
                            style = TextStyle(fontSize = 12.sp, color = CspColors.Muted2),
                        )
                    }
                }
                if (index < traces.lastIndex) {
                    HorizontalDivider(color = CspColors.Line, modifier = Modifier.padding(horizontal = 14.dp))
                }
            }
        }
        if (disabled) {
            Text(
                "Non disponible pour les événements récurrents",
                style = TextStyle(fontSize = 11.sp, color = CspColors.Muted2),
            )
        }
    }
}

@Composable
private fun CspDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val borderColor = if (expanded) CspColors.Red else CspColors.Line

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CspColors.Muted))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .background(CspColors.Surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = selected.ifEmpty { "Choisir un type" },
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = if (selected.isEmpty()) CspColors.Muted2 else CspColors.Ink,
                    ),
                )
                IconChevronRight(
                    tint = CspColors.Muted,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationZ = if (expanded) 270f else 90f },
                )
            }

            if (expanded) {
                HorizontalDivider(color = CspColors.Line, thickness = 1.dp)
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option); expanded = false }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(option, style = TextStyle(fontSize = 15.sp, color = CspColors.Ink))
                    }
                    if (index < options.lastIndex) {
                        HorizontalDivider(color = CspColors.Line2, thickness = 1.dp)
                    }
                }
            }
        }
    }
}
