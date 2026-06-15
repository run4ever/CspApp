package fr.csp.app

import androidx.compose.runtime.Composable
import fr.csp.app.data.DatabaseDriverFactory
import fr.csp.app.ui.home.HomeScreen

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    HomeScreen()
}
