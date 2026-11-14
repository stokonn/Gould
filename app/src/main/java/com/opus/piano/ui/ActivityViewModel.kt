package com.opus.piano.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opus.piano.data.*
import com.opus.piano.activity.domain.DayActivity
import com.opus.piano.R
import com.opus.piano.i18n.L
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
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
    val claimedReward: ActivityRewardGrant? = null,
    val rewardStateLoaded: Boolean = false,
) {
    val cosmeticRewardState: WeeklyCosmeticRewardState
        get() = when {
            !rewardStateLoaded -> WeeklyCosmeticRewardState.Loading
            pendingReward != null -> WeeklyCosmeticRewardState.Claim(pendingReward)
            claimedReward != null -> WeeklyCosmeticRewardState.Claimed(claimedReward)
            snapshot != null -> WeeklyCosmeticRewardState.Locked
            else -> WeeklyCosmeticRewardState.Loading
        }
}

sealed interface WeeklyCosmeticRewardState {
    data object Loading : WeeklyCosmeticRewardState
    data object Locked : WeeklyCosmeticRewardState
    data class Claim(val reward: ActivityRewardGrant) : WeeklyCosmeticRewardState
    data class Claimed(val reward: ActivityRewardGrant) : WeeklyCosmeticRewardState
}

class ActivityViewModel(private val repository: ActivityRepository) : ViewModel() {
    private val _state = MutableStateFlow(ActivityUiState())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch { repository.manualEntries.collect { _state.value = _state.value.copy(manualEntries = it) } }
        viewModelScope.launch { repository.goals.collect { _state.value = _state.value.copy(goalHistory = it) } }
        viewModelScope.launch { repository.ownedCosmetics.collect { _state.value = _state.value.copy(owned = it) } }
        viewModelScope.launch { repository.loadout.collect { _state.value = _state.value.copy(loadout = it) } }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(repository.pendingReward, repository.claimedReward) { _, _ -> Unit }
                .collect {
                    // Room flows can invalidate the claimed and pending queries in
                    // separate emissions. Read both from one transaction so an old
                    // pending row cannot reappear after a successful claim.
                    val (pending, claimed) = repository.rewardState()
                    _state.value = _state.value.copy(
                        pendingReward = pending,
                        claimedReward = claimed,
                        rewardStateLoaded = true,
                    )
                }
        }
        viewModelScope.launch { repository.activityChanges().drop(1).collect { refresh() } }
        viewModelScope.launch {
            var dayAndZone = LocalDate.now() to ZoneId.systemDefault()
            while (true) {
                kotlinx.coroutines.delay(30_000)
                val current = LocalDate.now() to ZoneId.systemDefault()
                if (current != dayAndZone) { dayAndZone = current; refresh() }
            }
        }
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
        try {
            repository.addManualActivity(day, minutes)
        }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_add_failed)) }
    }

    fun deleteManual(id: String) = viewModelScope.launch {
        try { repository.deleteManualActivity(id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_delete_failed)) }
    }

    fun equip(category: String, id: String?) = viewModelScope.launch {
        try { repository.changeLoadout(category, id) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { _state.value = _state.value.copy(error = userMessage(error, R.string.activity_cosmetic_action_failed)) }
    }

    suspend fun claimReward(weekMonday: String): Boolean = try {
        repository.claimReward(weekMonday).also { claimed ->
            if (claimed) {
                val (pending, latestClaimed) = repository.rewardState()
                _state.value = _state.value.copy(
                    pendingReward = pending,
                    claimedReward = latestClaimed,
                    rewardStateLoaded = true,
                )
                refresh()
            }
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        _state.value = _state.value.copy(error = L.s(R.string.activity_reward_claim_failed))
        false
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    private fun userMessage(error: Exception, fallback: Int): String = when (error.message) {
        "Enter a duration from 1 to 1,440 minutes." -> L.s(R.string.activity_duration_invalid)
        "Activity can only be added today or up to two days ago." -> L.s(R.string.activity_date_past_only)
        else -> L.s(fallback)
    }
}
