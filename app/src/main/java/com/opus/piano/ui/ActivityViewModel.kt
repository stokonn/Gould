package com.opus.piano.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opus.piano.data.*
import com.opus.piano.activity.domain.DayActivity
import com.opus.piano.R
import com.opus.piano.i18n.L
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class ActivityUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val snapshot: ActivitySnapshot? = null,
    val year: Int = LocalDate.now().year,
    val yearDays: List<DayActivity> = emptyList(),
    val manualEntries: List<ManualActivityEntry> = emptyList(),
    val goalHistory: List<ActivityWeeklyGoal> = emptyList(),
    val owned: List<CosmeticOwnership> = emptyList(),
    val loadout: List<CosmeticLoadout> = emptyList(),
    val pendingReward: ActivityRewardGrant? = null,
)

class ActivityViewModel(private val repository: ActivityRepository) : ViewModel() {
    private val _state = MutableStateFlow(ActivityUiState())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch { repository.manualEntries.collect { _state.value = _state.value.copy(manualEntries = it) } }
        viewModelScope.launch { repository.goals.collect { _state.value = _state.value.copy(goalHistory = it) } }
        viewModelScope.launch { repository.ownedCosmetics.collect { _state.value = _state.value.copy(owned = it) } }
        viewModelScope.launch { repository.loadout.collect { _state.value = _state.value.copy(loadout = it) } }
        viewModelScope.launch { repository.pendingReward.collect { _state.value = _state.value.copy(pendingReward = it) } }
        refresh()
    }

    fun selectYear(value: Int) {
        if (value !in 1..LocalDate.now().year) return
        _state.value = _state.value.copy(year = value)
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        val previous = _state.value
        _state.value = previous.copy(loading = true, error = null)
        try {
            val today = LocalDate.now()
            val zone = ZoneId.systemDefault()
            val summary = repository.snapshot(today, zone)
            val dates = repository.yearActivity(_state.value.year, today, zone)
            _state.value = _state.value.copy(loading = false, snapshot = summary, yearDays = dates, error = null)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _state.value = _state.value.copy(loading = false, error = L.s(R.string.activity_load_failed)) }
    }

    fun changeGoals(sessions: Int, minutes: Int?) = viewModelScope.launch {
        try { repository.changeGoals(sessions, minutes) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_goal_save_failed)) }
        refresh()
    }

    fun addManual(day: LocalDate, minutes: Int) = viewModelScope.launch {
        try { repository.addManualActivity(day, minutes) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_add_failed)) }
        refresh()
    }

    fun deleteManual(id: String) = viewModelScope.launch {
        try { repository.deleteManualActivity(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_delete_failed)) }
        refresh()
    }

    fun equip(category: String, id: String?) = viewModelScope.launch {
        try { repository.changeLoadout(category, id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_cosmetic_action_failed)) }
    }

    fun dismissReward(weekMonday: String) = viewModelScope.launch { repository.acknowledgeReward(weekMonday) }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    private fun userMessage(error: Exception, fallback: Int): String = when (error.message) {
        "Enter a duration from 1 to 1,440 minutes." -> L.s(R.string.activity_duration_invalid)
        "Activity can only be added through yesterday." -> L.s(R.string.activity_date_past_only)
        "Activity must be within Gould Activity history." -> L.s(R.string.activity_date_before_start)
        else -> L.s(fallback)
    }
}
