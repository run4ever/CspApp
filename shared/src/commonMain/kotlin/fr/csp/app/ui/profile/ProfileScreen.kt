package fr.csp.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.csp.app.data.EventRepository
import fr.csp.app.ui.home.IconChevronRight
import fr.csp.app.ui.theme.CspColors
import kotlinx.coroutines.launch

private val EVENT_TYPES = listOf(
    "Sortie hebdo CSP",
    "Evenement CSP",
    "Sortie annuelle CSP",
    "Evenement autre club",
    "Cyclosportive",
)

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            "Créer un événement",
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black, color = CspColors.Ink),
        )
        CreateEventForm()
    }
}

@Composable
private fun CreateEventForm() {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val repository = remember { EventRepository() }

    val formValid = name.isNotBlank() && type.isNotBlank() && date.isNotBlank() && time.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CspField(label = "Nom de l'événement", value = name, placeholder = "ex. Sortie du dimanche", onValueChange = { name = it; feedback = null })
        CspDropdown(label = "Type d'événement", selected = type, options = EVENT_TYPES, onSelect = { type = it; feedback = null })
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                CspField(label = "Date", value = date, placeholder = "jj/mm/aaaa", onValueChange = { date = it; feedback = null })
            }
            Box(modifier = Modifier.weight(1f)) {
                CspField(label = "Heure", value = time, placeholder = "08h00", onValueChange = { time = it; feedback = null })
            }
        }
        CspField(label = "Lieu", value = location, placeholder = "ex. Parking Colette, Pantin", onValueChange = { location = it })

        feedback?.let {
            Text(it, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (it.startsWith("✓")) CspColors.Green else CspColors.Red))
        }

        Spacer(Modifier.height(4.dp))

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
                            repository.createEvent(
                                title = name,
                                type = type,
                                date = date,
                                time = time,
                                location = location,
                            )
                            feedback = "✓ Événement créé"
                            name = ""; type = ""; date = ""; time = ""; location = ""
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
                    "Créer l'événement",
                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CspColors.Ink),
                )
            }
        }
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
                            .clickable {
                                onSelect(option)
                                expanded = false
                            }
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
