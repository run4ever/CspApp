package fr.csp.app

import androidx.compose.ui.window.ComposeUIViewController
import fr.csp.app.data.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }