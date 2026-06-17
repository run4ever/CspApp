package fr.csp.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.random.Random
import kotlin.time.Clock

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    val isLoggedIn = auth.authStateChanged
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun signOut() = auth.signOut()

    suspend fun sendPasswordReset(email: String): String? = try {
        auth.sendPasswordResetEmail(email)
        null
    } catch (e: Exception) {
        e.message?.toFriendlyAuthError() ?: "Erreur lors de l'envoi"
    }

    suspend fun signIn(email: String, password: String): String? = try {
        auth.signInWithEmailAndPassword(email, password)
        null
    } catch (e: Exception) {
        e.message?.toFriendlyAuthError() ?: "Erreur de connexion"
    }

    suspend fun requestAccount(
        nom: String,
        prenom: String,
        email: String,
        dateNaissance: String,
    ): String? = try {
        val tempPassword = generateTempPassword()
        val result = auth.createUserWithEmailAndPassword(email, tempPassword)
        val uid = result.user?.uid ?: return "Erreur : impossible de récupérer l'identifiant"
        Firebase.firestore.collection("users").document(uid).set(
            mapOf(
                "nom" to nom,
                "prenom" to prenom,
                "email" to email,
                "date_naissance" to dateNaissance,
                "role" to "member",
                "status" to "PENDING",
                "createdAt" to Clock.System.now().toEpochMilliseconds(),
            )
        )
        auth.signOut()
        null
    } catch (e: Exception) {
        e.message?.toFriendlyAuthError() ?: "Erreur lors de la création du compte"
    }

    private fun generateTempPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..24).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}

private fun String.toFriendlyAuthError(): String = when {
    contains("already in use", ignoreCase = true) ||
    contains("already-in-use", ignoreCase = true) ->
        "Un compte existe déjà pour cet e-mail."
    contains("invalid email", ignoreCase = true) ||
    contains("invalid-email", ignoreCase = true) ->
        "Adresse e-mail invalide."
    contains("wrong password", ignoreCase = true) ||
    contains("invalid credential", ignoreCase = true) ||
    contains("credential is incorrect", ignoreCase = true) ||
    contains("malformed or has expired", ignoreCase = true) ->
        "E-mail ou mot de passe incorrect."
    contains("user not found", ignoreCase = true) ||
    contains("user-not-found", ignoreCase = true) ->
        "Aucun compte associé à cet e-mail."
    contains("too many requests", ignoreCase = true) ->
        "Trop de tentatives. Réessayez dans quelques minutes."
    else -> this
}
