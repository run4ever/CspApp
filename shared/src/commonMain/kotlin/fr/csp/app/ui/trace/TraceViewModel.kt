package fr.csp.app.ui.trace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.csp.app.data.TraceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TraceViewModel : ViewModel() {
    private val repo = TraceRepository()

    val traces = repo.getTraces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTrace(
        title: String,
        distanceKm: Double,
        elevationUp: Int,
        elevationDown: Int,
        durationMin: Int,
        url: String,
        order: Int = traces.value.size,
    ) {
        viewModelScope.launch {
            repo.addTrace(title, distanceKm, elevationUp, elevationDown, durationMin, url, order)
        }
    }

    fun deleteTrace(id: String) {
        viewModelScope.launch { repo.deleteTrace(id) }
    }
}
