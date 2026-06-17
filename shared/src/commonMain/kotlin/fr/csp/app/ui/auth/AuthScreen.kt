package fr.csp.app.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.csp.app.resources.Res
import fr.csp.app.resources.logo_csp
import fr.csp.app.ui.home.IconBell
import fr.csp.app.ui.theme.CspColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private fun String.toNameCase(): String =
    split("-").joinToString("-") { part ->
        part.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

// ── Champ de formulaire ───────────────────────────────────────

@Composable
private fun CspField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) CspColors.Blue else CspColors.Line,
        animationSpec = tween(200),
        label = "border",
    )

    Column(modifier = modifier) {
        Text(
            label,
            style = TextStyle(
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = CspColors.Muted,
                letterSpacing = (12.5 * 0.01).sp,
            ),
        )
        Spacer(Modifier.height(7.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(CspColors.Surface)
                .border(1.dp, borderColor, RoundedCornerShape(13.dp))
                .padding(horizontal = 15.dp, vertical = 13.dp),
            textStyle = TextStyle(fontSize = 15.sp, color = CspColors.Ink),
            singleLine = true,
            interactionSource = interactionSource,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text(placeholder, style = TextStyle(fontSize = 15.sp, color = CspColors.Muted2))
                    }
                    inner()
                }
            },
        )
    }
}

// ── Switch segmenté ───────────────────────────────────────────

@Composable
private fun SegmentedSwitch(isLogin: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CspColors.Surface)
            .border(1.dp, CspColors.Line2, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf(true to "Se connecter", false to "S'inscrire").forEach { (isLoginTab, label) ->
            val active = isLogin == isLoginTab
            val bgColor by animateColorAsState(
                targetValue = if (active) CspColors.Red else Color.Transparent,
                animationSpec = tween(220),
                label = "tabBg",
            )
            val textColor by animateColorAsState(
                targetValue = if (active) Color.White else CspColors.Muted,
                animationSpec = tween(220),
                label = "tabText",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(bgColor)
                    .clickable { onToggle(isLoginTab) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = TextStyle(fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = textColor),
                )
            }
        }
    }
}

// ── Écran principal ───────────────────────────────────────────

@Composable
fun AuthScreen(vm: AuthViewModel, onLoginSuccess: () -> Unit = {}, onSignupSuccess: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf<String?>(null) }
    var resetSending by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CspColors.Bg)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        // Logo + accroche
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_csp),
                contentDescription = "Cyclo Sport Pantin",
                modifier = Modifier
                    .size(84.dp)
                    .shadow(elevation = 24.dp, shape = RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Cyclo Sport Pantin",
                style = TextStyle(
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = CspColors.Ink,
                    lineHeight = (23 * 1.1).sp,
                ),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Venez rouler avec nous",
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CspColors.Muted),
            )
        }

        Spacer(Modifier.height(26.dp))

        // Switch segmenté
        SegmentedSwitch(isLogin = isLogin, onToggle = { tab ->
            isLogin = tab
            error = null
        })

        Spacer(Modifier.height(24.dp))

        if (isLogin) {
            // ── Connexion ─────────────────────────────────────
            CspField(
                label = "Email",
                value = email,
                onValueChange = { email = it.lowercase(); error = null },
                placeholder = "vous@exemple.fr",
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(14.dp))
            CspField(
                label = "Mot de passe",
                value = password,
                onValueChange = { password = it; error = null },
                placeholder = "••••••••",
                isPassword = true,
            )
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 22.dp)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Text(
                        if (resetSending) "Envoi…" else "Mot de passe oublié ?",
                        style = TextStyle(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (resetSending) CspColors.Muted else CspColors.Blue,
                        ),
                        modifier = Modifier.clickable(enabled = !resetSending) {
                            if (email.isBlank()) {
                                resetMessage = "⚠ Saisissez votre e-mail ci-dessus."
                            } else {
                                resetMessage = null
                                resetSending = true
                                scope.launch {
                                    val err = vm.sendPasswordReset(email)
                                    resetMessage = err
                                        ?: "✓ Un e-mail de réinitialisation a été envoyé à $email."
                                    resetSending = false
                                }
                            }
                        },
                    )
                }
                if (resetMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        resetMessage!!,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = if (resetMessage!!.startsWith("✓")) CspColors.Green else CspColors.Red,
                            lineHeight = (13 * 1.5).sp,
                        ),
                    )
                }
            }
        } else {
            // ── Inscription ───────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color(0xFF4CCBEE).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF4CCBEE).copy(alpha = 0.22f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                verticalAlignment = Alignment.Top,
            ) {
                IconBell(CspColors.Blue, Modifier.size(18.dp).padding(top = 1.dp))
                Text(
                    "La création de compte est soumise à validation d'un administrateur du club. " +
                        "Une fois votre compte créé, le mot de passe vous sera envoyé.",
                    style = TextStyle(fontSize = 13.sp, color = CspColors.Ink2, lineHeight = (13 * 1.5).sp),
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CspField(
                    label = "Prénom",
                    value = prenom,
                    onValueChange = { prenom = it.toNameCase(); error = null },
                    placeholder = "Marie",
                    modifier = Modifier.weight(1f),
                )
                CspField(
                    label = "Nom",
                    value = nom,
                    onValueChange = { nom = it.toNameCase(); error = null },
                    placeholder = "Dupont",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(14.dp))
            CspField(
                label = "Email",
                value = email,
                onValueChange = { email = it.lowercase(); error = null },
                placeholder = "vous@exemple.fr",
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(14.dp))
            CspField(
                label = "Date de naissance",
                value = dateNaissance,
                onValueChange = { dateNaissance = it; error = null },
                placeholder = "JJ/MM/AAAA",
            )
            Spacer(Modifier.height(8.dp))
        }

        // Message d'erreur
        if (error != null) {
            Text(
                error!!,
                style = TextStyle(fontSize = 13.sp, color = CspColors.Red),
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }

        // Bouton d'action principal
        val canSubmit = if (isLogin) {
            email.isNotBlank() && password.isNotBlank()
        } else {
            nom.isNotBlank() && prenom.isNotBlank() && email.isNotBlank()
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (canSubmit && !loading) CspColors.Red else CspColors.Surface2)
                .then(
                    if (!canSubmit || loading)
                        Modifier.border(1.dp, CspColors.Line, RoundedCornerShape(14.dp))
                    else Modifier
                )
                .clickable(enabled = canSubmit && !loading) {
                    scope.launch {
                        loading = true
                        error = null
                        if (isLogin) {
                            val err = vm.signIn(email, password)
                            if (err == null) onLoginSuccess() else error = err
                        } else {
                            val err = vm.requestAccount(nom, prenom, email, dateNaissance)
                            if (err == null) onSignupSuccess() else error = err
                        }
                        loading = false
                    }
                }
                .padding(15.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = if (isLogin) "Se connecter" else "Demander un compte",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (canSubmit) Color.White else CspColors.Muted,
                    ),
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
