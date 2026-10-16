package com.opus.piano.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opus.piano.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class CatalogBrowseState(
    val query: String = "", val activeQuery: String = "", val loading: Boolean = false,
    val error: String? = null, val warning: String? = null, val next: Int? = null,
    val cached: Boolean = false, val fetchedAt: Long = 0L
)
data class CatalogResults(val composers: List<CatalogComposer> = emptyList(), val works: List<CatalogWork> = emptyList())
data class ComposerPageState(val id: String = "", val loading: Boolean = true, val profileLoading: Boolean = true,
    val profile: ComposerProfile? = null, val error: String? = null, val profileError: String? = null,
    val warning: String? = null, val fetchedAt: Long = 0L, val totalWorks: Int? = null)
data class WorkPageState(val id: String = "", val loading: Boolean = true, val error: String? = null, val warning: String? = null, val fetchedAt: Long = 0L)

class CatalogViewModel(val repository: CatalogRepository, private val saved: SavedStateHandle) : ViewModel() {
    val snapshot = repository.snapshot.stateIn(viewModelScope, SharingStarted.Eagerly, CatalogSnapshot(emptyList(), emptyList()))
    private val _browse = MutableStateFlow(CatalogBrowseState(query = saved["catalogQuery"] ?: ""))
    val browse = _browse.asStateFlow()
    val results = combine(snapshot, browse.map { it.activeQuery }.distinctUntilChanged()) { data, query ->
        withContext(Dispatchers.Default) {
            val tokens = catalogText(query).split(' ').filter { it.isNotBlank() }
            val composersById = data.composers.associateBy { it.id }
            CatalogResults(
                data.composers.filter { c -> tokens.all { catalogText(c.name).contains(it) } },
                if (tokens.isEmpty()) emptyList() else data.works.filter { w ->
                    val searchable = catalogText("${w.title} ${w.subtitle} ${w.searchTerms} ${w.catalogLabel} ${composersById[w.composerId]?.name.orEmpty()}")
                    tokens.all { searchable.contains(it) }
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogResults())
    private val _composer = MutableStateFlow(ComposerPageState())
    val composer = _composer.asStateFlow()
    private val _work = MutableStateFlow(WorkPageState())
    val work = _work.asStateFlow()
    private val _adding = MutableStateFlow<Set<String>>(emptySet())
    val adding = _adding.asStateFlow()
    private val _notice = MutableStateFlow<String?>(null)
    val notice = _notice.asStateFlow()
    private var searchJob: Job? = null
    private var composerJob: Job? = null
    private var workJob: Job? = null

    private var browseStarted = false
    fun startBrowsing() {
        if (!browseStarted) { browseStarted = true; query(_browse.value.query) }
    }
    fun query(value: String) {
        saved["catalogQuery"] = value
        _browse.update { it.copy(query = value, loading = true, error = null, warning = null, next = null, fetchedAt = 0L) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            _browse.update { it.copy(activeQuery = value) }
            loadSearch(value, 0, false)
        }
    }
    fun refreshSearch() {
        searchJob?.cancel()
        val value = _browse.value.query
        searchJob = viewModelScope.launch { _browse.update { it.copy(activeQuery = value) }; loadSearch(value, 0, true) }
    }
    fun nextPage() {
        val page = _browse.value.next ?: return
        if (_browse.value.loading) return
        searchJob = viewModelScope.launch { loadSearch(_browse.value.activeQuery, page, false) }
    }
    private suspend fun loadSearch(query: String, offset: Int, force: Boolean) {
        _browse.update { it.copy(loading = true, error = null) }
        try {
            if (catalogText(query).isBlank()) {
                val load = repository.featured(force)
                _browse.update { it.copy(loading = false, cached = load.cached, warning = load.warning, fetchedAt = load.fetchedAt, next = null) }
            } else if (catalogText(query).length < 2) {
                _browse.update { it.copy(loading = false, cached = true, next = null) }
            } else {
                val load = repository.search(query, offset, force)
                _browse.update { it.copy(loading = false, next = load.value, cached = load.cached, warning = load.warning, fetchedAt = load.fetchedAt) }
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) {
            _browse.update { it.copy(loading = false, cached = true, error = "Catalogue unavailable. Showing matching saved items; connect to the internet to discover more.") }
        }
    }
    fun loadComposer(id: String, force: Boolean = false) {
        composerJob?.cancel()
        _composer.value = ComposerPageState(id)
        composerJob = viewModelScope.launch {
            // Biography and works are independent; neither failure hides the other.
            launch {
                try {
                    val load = repository.composerWorks(id, force)
                    _composer.update { it.copy(loading = false, warning = load.warning, fetchedAt = load.fetchedAt, totalWorks = load.value) }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { _composer.update { it.copy(loading = false, error = "Works could not be refreshed. Any saved works are shown below.") } }
            }
            launch {
                try {
                    val c = withTimeoutOrNull(20000) {
                        snapshot.first { data -> data.composers.any { it.id == id && it.birth.isNotBlank() } }.composers.first { it.id == id }
                    }
                    if (c == null) {
                        _composer.update { it.copy(profileLoading = false, profileError = "A reliable birth year is needed to verify this biography. Refresh when online.") }
                        return@launch
                    }
                    val profile = repository.profile(c, force)
                    _composer.update { it.copy(profileLoading = false, profile = profile.value, profileError = profile.warning) }
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { _composer.update { it.copy(profileLoading = false, profileError = "Biography and portrait are unavailable. Please try again when online.") } }
            }
        }
    }
    fun loadWork(id: String, force: Boolean = false) {
        workJob?.cancel()
        _work.value = WorkPageState(id)
        workJob = viewModelScope.launch {
            try {
                val load = repository.detail(id, force)
                _work.update { it.copy(loading = false, warning = load.warning, fetchedAt = load.fetchedAt) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _work.update { it.copy(loading = false, error = "Full detail is unavailable. Showing saved information.") } }
        }
    }
    fun add(work: CatalogWork, composer: CatalogComposer) {
        if (work.id in _adding.value) return
        _adding.update { it + work.id }
        viewModelScope.launch {
            try {
                val added = repository.add(work, composer)
                _notice.value = if (added) "Added to your repertoire." else "This work is already in your repertoire."
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _notice.value = "Could not add this work. Please try again." }
            finally { _adding.update { it - work.id } }
        }
    }
    fun retryVisible(route: String) {
        when {
            route == "catalog" && _browse.value.error != null && !_browse.value.loading -> query(_browse.value.query)
            route.startsWith("catalog/composer/") && !_composer.value.loading && (_composer.value.error != null || _composer.value.profileError != null) -> loadComposer(route.substringAfterLast('/'))
            route.startsWith("catalog/work/") && !_work.value.loading && _work.value.error != null -> loadWork(route.substringAfterLast('/'))
        }
    }
    fun dismissNotice() { _notice.value = null }
}
