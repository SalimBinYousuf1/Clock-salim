package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.WorldClockEntity
import com.example.data.model.WorldCitiesDirectory
import com.example.data.model.WorldCity
import com.example.data.model.WorldClockDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WorldClockViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as SalimApplication
    private val dao = app.database.salimDao()
    private val settingsRepo = app.settingsRepository

    private val _tick = MutableStateFlow(System.currentTimeMillis())

    val worldClocks: StateFlow<List<WorldClockDisplay>> = combine(
        dao.getAllWorldClocksFlow(),
        settingsRepo.settingsFlow,
        _tick
    ) { clocks, settings, _ ->
        clocks.map { entity ->
            WorldCitiesDirectory.calculateDisplay(
                id = entity.id,
                cityName = entity.cityName,
                countryName = entity.countryName,
                timeZoneId = entity.timeZoneId,
                use24Hour = settings.is24HourFormat
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<WorldCity>>(emptyList())
    val searchResults: StateFlow<List<WorldCity>> = _searchResults.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _tick.value = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    fun openSearch() {
        _searchQuery.value = ""
        _searchResults.value = WorldCitiesDirectory.CITIES
        _isSearchOpen.value = true
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        _searchQuery.value = ""
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = WorldCitiesDirectory.CITIES
        } else {
            val q = query.trim().lowercase()
            _searchResults.value = WorldCitiesDirectory.CITIES.filter {
                it.cityName.lowercase().contains(q) || it.countryName.lowercase().contains(q)
            }
        }
    }

    fun addCity(city: WorldCity) {
        viewModelScope.launch {
            val count = dao.getWorldClockCount()
            dao.insertWorldClock(
                WorldClockEntity(
                    cityName = city.cityName,
                    countryName = city.countryName,
                    timeZoneId = city.timeZoneId,
                    orderIndex = count
                )
            )
            closeSearch()
        }
    }

    fun removeCity(id: Long) {
        viewModelScope.launch {
            dao.deleteWorldClockById(id)
        }
    }
}
