package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.opus.piano.data.*
import com.opus.piano.ui.admin.AdminDashboardScreen
import com.opus.piano.ui.admin.AdminQueueScreen
import com.opus.piano.ui.admin.AdminAuditScreen
import com.opus.piano.ui.admin.AdminSettingsScreen
import com.opus.piano.ui.admin.AdminProposalScreen
import java.time.*
import java.time.format.DateTimeFormatter

private enum class Destination(val label: String, val icon: ImageVector) {
    OVERVIEW("Home", Icons.Outlined.Home), HOME("Profile", Icons.Outlined.Person), REPERTOIRE("Library", Icons.Outlined.LibraryMusic),
    DISCOVER("Discover", Icons.Outlined.Explore), PRACTICE("Studio", Icons.Outlined.Piano), BADGES("Badges", Icons.Outlined.EmojiEvents), WRAPPED("Wrapped", Icons.Outlined.Insights)
}
@Composable fun OpusApp(model: OpusViewModel, catalog: CatalogViewModel, experience: ExperienceViewModel, profile: ProfileViewModel, admin: com.opus.piano.ui.admin.AdminViewModel) {
    ProfileEditor(profile)
    val context = LocalContext.current
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val motionScale by produceState(1f, context) {
        val resolver = context.contentResolver
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                value = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
            }
        }
        value = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        awaitDispose { resolver.unregisterContentObserver(observer) }
    }
    val appearance = LocalAppearance.current
    val library by model.library.collectAsStateWithLifecycle()
    val message by model.message.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = entry?.destination?.route ?: Destination.HOME.name
    ObserveConnectivity(catalog, current)
    var fullyDrawn by remember { mutableStateOf(false) }
    LaunchedEffect(library.loading) {
        if (!library.loading && !fullyDrawn) { (context as? android.app.Activity)?.reportFullyDrawn(); fullyDrawn = true }
    }
    val openPractice by (context.applicationContext as com.opus.piano.OpusApplication).openPractice.collectAsStateWithLifecycle()
    LaunchedEffect(openPractice) { if (openPractice > 0) nav.selectTopLevel("PRACTICE") }
    val local by profile.local.collectAsStateWithLifecycle()
    var replay by remember { mutableStateOf(false) }
    if (local != null && (!local!!.onboardingDone || replay)) OnboardingScreen({ profile.finishOnboarding(); replay = false })
    val catalogNotice by catalog.notice.collectAsStateWithLifecycle()
    val experienceNotice by experience.notice.collectAsStateWithLifecycle()
    val catalogData by catalog.snapshot.collectAsStateWithLifecycle()
    val badges by experience.badges.collectAsStateWithLifecycle()
    val profileNotice by profile.notice.collectAsStateWithLifecycle()
    var knownUnlocks by remember { mutableStateOf<Set<String>?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(profileNotice) { profileNotice?.let { snackbar.showSnackbar(it); profile.notice.value = null } }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); model.dismissMessage() } }
    LaunchedEffect(catalogNotice) { catalogNotice?.let { snackbar.showSnackbar(it); catalog.dismissNotice() } }
    LaunchedEffect(experienceNotice) { experienceNotice?.let { snackbar.showSnackbar(it); experience.dismissNotice() } }
    // Durable unlock popup: one themed popup per new award; deferred during active play/capture.
    val app = context.applicationContext as com.opus.piano.OpusApplication
    val pendingNotices by app.database.studioDao().pendingNotices().collectAsStateWithLifecycle(initialValue = emptyList())
    val timerState by model.timer.collectAsStateWithLifecycle()
    val recState by model.recordingState.collectAsStateWithLifecycle()
    val profileDraft by profile.editDraft.collectAsStateWithLifecycle()
    val captureActive = timerState.running || recState.phase == com.opus.piano.audio.RecordingController.Phase.Recording
    var shownNotice by remember { mutableStateOf<com.opus.piano.data.BadgeNotice?>(null) }
    val popupScope = rememberCoroutineScope()
    fun acknowledge(notice: com.opus.piano.data.BadgeNotice) { popupScope.launch { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { app.database.studioDao().acknowledge(notice.achievementId, System.currentTimeMillis()) } } }
    LaunchedEffect(pendingNotices, captureActive, profileDraft) {
        if (shownNotice == null && !captureActive && profileDraft == null) shownNotice = pendingNotices.firstOrNull()
    }
    shownNotice?.let { notice ->
        val badge = badges.firstOrNull { it.definition.id == notice.achievementId }
        if (badge != null && !captureActive && profileDraft == null) {
            AlertDialog(onDismissRequest = { shownNotice = null; acknowledge(notice) }, icon = { MedalArtwork(badge, Modifier.size(80.dp)) },
                title = { Text(badge.definition.localizedName) },
                text = { Text("${L.fixed(badge.definition.rarity.label)} · ${badge.definition.localizedDescription}") },
                confirmButton = { JournalTextButton(onClick = { shownNotice = null; acknowledge(notice); nav.navigate("BADGES") }) { Text(L.s(R.string.msg_view_badge)) } },
                dismissButton = { JournalTextButton(onClick = { shownNotice = null; acknowledge(notice) }) { Text(L.s(R.string.msg_dismiss_11a3)) } })
        }
    }
    // Apply keyboard avoidance once, before measuring navigation and child screens.
    Scaffold(
        modifier = Modifier.imePadding(),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            val destinations = listOf(Destination.OVERVIEW, Destination.REPERTOIRE, Destination.DISCOVER, Destination.PRACTICE, Destination.HOME)
            val selected = (if (current == SETTINGS_ROUTE) selectedTab(nav.previousBackStackEntry?.destination?.route ?: PROFILE_ROUTE) else selectedTab(current))
            JournalNavigationBar(
                items = destinations.map { destination ->
                    JournalNavigationItem(destination.name,
                        if (destination == Destination.DISCOVER) L.s(R.string.discover) else L.fixed(destination.label), destination.icon)
                },
                selectedRoute = selected,
                onSelect = { route ->
                    focus.clearFocus()
                    keyboard?.hide()
                    nav.selectTopLevel(route)
                },
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)), horizontalAlignment = Alignment.CenterHorizontally) {
            NavHost(navController = nav, startDestination = Destination.OVERVIEW.name, modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                enterTransition = { fadeIn(tween(if (motionScale == 0f) 0 else 220)) }, exitTransition = { fadeOut(tween(if (motionScale == 0f) 0 else 150)) }) {
                composable(Destination.OVERVIEW.name) {
                    val settings by profile.settings.collectAsStateWithLifecycle()
                    BalancedHome(library, settings.username, { nav.navigate("piece/$it") }, { nav.navigate("history") }, {nav.navigate(SETTINGS_ROUTE)})
                }
                composable(SETTINGS_ROUTE) { SettingsScreen(profile, model, { replay = true }) { nav.popBackStack() } }
                composable(Destination.HOME.name) {
                    val adminCaps by admin.caps.collectAsStateWithLifecycle()
                    val adminFailure by admin.capsFailure.collectAsStateWithLifecycle()
                    val administration: (() -> Unit)? =
                        if (admin.entryVisible(adminCaps, adminFailure)) ({ nav.navigate("admin") }) else null
                    ProfileScreen(library, badges, profile, { nav.navigate(SETTINGS_ROUTE) }, { nav.navigate("BADGES") }, { nav.navigate("history") }, administration = administration, badgeDetail = { nav.navigate("badge/$it") })
                }
                // Stage 7B: role-gated Administration (entry is convenience only — the
                // server always enforces capabilities on every admin endpoint).
                composable("admin") {
                    AdminTheme { AdminDashboardScreen(
                        admin,
                        onQueue = { filter ->
                            nav.navigate(if (filter == null) "admin/queue" else "admin/queue?filter=$filter")
                        },
                        onAudit = { tile ->
                            admin.auditInitialFilter = when (tile) {
                                com.opus.piano.admin.AdminTile.APPROVED_TODAY -> "today_approve"
                                com.opus.piano.admin.AdminTile.REJECTED_TODAY -> "today_reject"
                                else -> "all"
                            }
                            nav.navigate("admin/audit")
                        },
                        onSettings = { nav.navigate("admin/settings") },
                    ) }
                }
                composable(
                    "admin/queue?filter={filter}",
                    arguments = listOf(
                        androidx.navigation.navArgument("filter") {
                            type = androidx.navigation.NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    ),
                ) { route ->
                    AdminTheme { AdminQueueScreen(admin, route.arguments?.getString("filter"), { nav.popBackStack() }, { id -> nav.navigate("admin/proposal/${android.net.Uri.encode(id)}") }) }
                }
                composable("admin/proposal/{id}") { route ->
                    AdminTheme { AdminProposalScreen(admin, route.arguments?.getString("id").orEmpty(), { nav.popBackStack() }, { id ->
                        nav.navigate("admin/proposal/${android.net.Uri.encode(id)}") {
                            popUpTo("admin/proposal/{id}") { inclusive = true }
                        }
                    }) }
                }
                composable("admin/audit") { AdminTheme { AdminAuditScreen(admin) { nav.popBackStack() } } }
                composable("admin/settings") { AdminTheme { AdminSettingsScreen(admin) { nav.popBackStack() } } }
                composable(Destination.REPERTOIRE.name) {
                    RepertoireScreen(library,catalogData,model,{nav.selectTopLevel("DISCOVER")},{nav.navigate("piece/$it")},profile,{nav.navigate("catalog/composer/$it")},catalog,{nav.navigate("catalog/work/$it")},{nav.navigate("discover/entity/$it")},{nav.navigate("previous")})
                }
                composable(Destination.DISCOVER.name) { DiscoverScreen({ nav.navigate("discover/entity/$it") }, { nav.navigate("piece/$it") }, { nav.navigate("catalog/work/$it") }, library) }
                composable("discover/entity/{id}") { route -> DiscoverDetailScreen(route.arguments?.getString("id").orEmpty(), {nav.popBackStack()}, {nav.navigate("discover/entity/$it")}, {nav.navigate("piece/$it")}) }
                composable("catalog") { CatalogScreen(catalog, library, { nav.popBackStack() }, { nav.navigate("catalog/composer/$it") }, { nav.navigate("catalog/work/$it") }, { nav.navigate("piece/$it") },profile=profile) }
                composable("catalog/composer/{id}") { route -> ComposerScreen(route.arguments?.getString("id").orEmpty(), catalog, library, { nav.popBackStack() }, { nav.navigate("catalog/work/$it") },identity=profile) }
                composable("catalog/work/{id}") { route ->
                    CompositionScreen(route.arguments?.getString("id"), null, catalog, experience, library, model, { nav.popBackStack() }, { nav.navigate("catalog/composer/$it") }, { nav.navigate("recordings/$it") }, { id, source -> if (id != null) nav.navigate("history/piece/$id") else if (source != null) nav.navigate("history/catalog/${android.net.Uri.encode(source)}") }) { piece ->
                        model.practice.command {
                            if (state.value.token.isEmpty()) select(piece) else notice.value=L.s(R.string.pending_session)
                        }
                        nav.navigate(Destination.PRACTICE.name) { launchSingleTop = true }
                    }
                }
                composable("piece/{id}") { route ->
                    CompositionScreen(null, route.arguments?.getString("id")?.toLongOrNull(), catalog, experience, library, model, { nav.popBackStack() }, { nav.navigate("catalog/composer/$it") }, { nav.navigate("recordings/$it") }, { id, source -> if (id != null) nav.navigate("history/piece/$id") else if (source != null) nav.navigate("history/catalog/${android.net.Uri.encode(source)}") }) { piece ->
                        model.practice.command {
                            if (state.value.token.isEmpty()) select(piece) else notice.value=L.s(R.string.pending_session)
                        }
                        nav.navigate(Destination.PRACTICE.name) { launchSingleTop = true }
                    }
                }
                composable("recordings/{key}") { route ->
                    val key = route.arguments?.getString("key").orEmpty()
                    val title = if (key.startsWith("catalog:")) catalogData.works.firstOrNull { it.id == key.substringAfter(':') }?.title else library.pieces.firstOrNull { it.id.toString() == key.substringAfter(':') }?.title
                    RecordingsScreen(key, title ?: L.s(R.string.reference_shelf), experience) { nav.popBackStack() }
                }
                composable("history") { PracticeHistoryScreen({ nav.popBackStack() }) }
                composable("previous") { PreviousScreen({ nav.popBackStack() }) }
                composable("history/piece/{id}") { e -> PracticeHistoryScreen({ nav.popBackStack() }, e.arguments?.getString("id")?.toLongOrNull()) }
                composable("history/catalog/{id}") { e -> PracticeHistoryScreen({ nav.popBackStack() }, catalogId=e.arguments?.getString("id")) }
                composable("badge/{id}") { route -> BadgeDetailScreen(route.arguments?.getString("id").orEmpty(), experience) { nav.popBackStack() } }
                composable(Destination.PRACTICE.name) { PracticeScreen(library, model, profile, motionScale != 0f, { nav.navigate("previous") }) }
                composable(Destination.BADGES.name) { BadgesScreen(experience, profile) { nav.navigate("badge/$it") } }
                composable(Destination.WRAPPED.name) { ComingSoon(library, "Wrapped", "Your music.\nYour story.", L.s(R.string.future_wrapped), Icons.Outlined.Insights, listOf("Monthly reflections", "Your year at the piano", "Repertoire highlights")) }
            }
        }
    }
}
@Composable internal fun Heading(eyebrow: String, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(L.fixed(eyebrow).uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable internal fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    JournalCard(modifier,content=content)
}
/** Shared compact text layout for Home, repertoire and catalogue rows. */
@Composable internal fun PieceRowContent(title: String, composer: String, metadata: String = "", modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement=Arrangement.spacedBy(JournalSpace.tiny)) {
        Text(title,style=MaterialTheme.typography.titleMedium,maxLines=3,overflow=TextOverflow.Ellipsis)
        if (composer.isNotBlank()) Text(composer,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if (metadata.isNotBlank()) Text(metadata,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable internal fun SectionTitle(title: String) { Text(title, style = MaterialTheme.typography.titleLarge) }
internal fun duration(millis: Long): String {
    val seconds = millis / 1000
    return "%02d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60)
}
private fun dayOf(timestamp: Long): LocalDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
@Composable internal fun SessionRow(session: PracticeSession, parts: List<SessionPart> = emptyList(), displayMillis: Long = session.durationMillis, untimed:Boolean=false) {
    var editNotes by remember { mutableStateOf(false) }
    if (editNotes) HistoricalReview(session, parts) { editNotes = false }
    val title = if (parts.isNotEmpty()) parts.filter { it.durationMillis > 0 || it.notes.isNotBlank() }
        .joinToString(" · ") { it.displayTitle }.ifBlank { session.pieceTitle }
        else if (session.pieceId == null && session.pieceTitle == "Free practice") L.fixed("Free practice") else session.pieceTitle
    Surface(onClick = { editNotes = true }, modifier = Modifier.fillMaxWidth().semantics {
        contentDescription = L.s(R.string.msg_review_edit_session_notes_c195)
    }, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.heightIn(min = 56.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(Instant.ofEpochMilli(session.completedAt).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("d MMM · HH:mm").withLocale(L.locale)),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if(untimed) L.s(R.string.ui010_untimed_short) else duration(displayMillis), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"))
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable internal fun Choice(label: String, options: List<String>, selected: String, translateOptions: Boolean = true, modifier: Modifier = Modifier, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        JournalSecondaryButton(onClick = { expanded = true },modifier=modifier) { Text("${L.fixed(label)}: ${if(translateOptions || selected=="All") L.fixed(selected) else selected}", Modifier.weight(1f, fill = false), maxLines = 2, overflow = TextOverflow.Ellipsis); Icon(Icons.Outlined.ExpandMore, null) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(if(translateOptions || option=="All") L.fixed(option) else option, maxLines = 3, overflow = TextOverflow.Ellipsis) }, onClick = { choose(option); expanded = false }) }
        }
    }
}
@Composable internal fun PieceEditor(piece: Piece?, dismiss: () -> Unit, save: (Piece, (Boolean) -> Unit) -> Unit) {
    var title by rememberSaveable { mutableStateOf(piece?.title ?: "") }
    var composer by rememberSaveable { mutableStateOf(piece?.composer ?: "") }
    var notes by rememberSaveable { mutableStateOf(piece?.notes ?: "") }
    var difficulty by rememberSaveable { mutableStateOf(piece?.difficulty ?: Difficulty.INTERMEDIATE) }
    var status by rememberSaveable { mutableStateOf(piece?.status ?: LearningStatus.PLANNED) }
    var instrument by rememberSaveable { mutableStateOf(piece?.instrument.orEmpty()) }
    var genre by rememberSaveable { mutableStateOf(piece?.genre.orEmpty()) }
    var era by rememberSaveable { mutableStateOf(piece?.musicalEra.orEmpty()) }
    var metadataOpen by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    Dialog(onDismissRequest = { if (!saving) dismiss() }) {
        Surface(shape = MaterialTheme.shapes.large) {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(L.s(R.string.msg_your_repertoire_a31f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                SectionTitle(if (piece == null) L.s(R.string.msg_a_new_beginning_a8c8) else L.s(R.string.msg_refine_the_details_d143))
                OutlinedTextField(title, { title = it }, label = { Text(L.s(R.string.msg_title_9616)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(composer, { composer = it }, label = { Text(L.s(R.string.msg_composer_5cf5)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Choice(L.s(R.string.msg_difficulty_7945), Difficulty.entries.map { it.label }, difficulty.label) { label -> difficulty = Difficulty.entries.first { it.label == label } }
                Choice(L.s(R.string.msg_status_bae7), LearningStatus.entries.map { it.label }, status.label) { label -> status = LearningStatus.entries.first { it.label == label } }
                OutlinedTextField(notes, { notes = it }, label = { Text(L.s(R.string.msg_notes_optional_4d56)) }, minLines = 3, modifier = Modifier.fillMaxWidth())
                JournalTextButton(onClick = { metadataOpen = !metadataOpen }) { Text(if (metadataOpen) L.s(R.string.msg_hide_musical_metadata_2704) else L.s(R.string.msg_musical_metadata_optional_ff79)) }
                if (metadataOpen) {
                    OutlinedTextField(instrument, { instrument = it }, label = { Text(L.s(R.string.msg_instrument_f05f)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(genre, { genre = it }, label = { Text(L.s(R.string.msg_genre_2aa3)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(era, { era = it }, label = { Text(L.s(R.string.msg_musical_era_d3df)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                Text(L.s(R.string.msg_required_2d1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    JournalTextButton(onClick = dismiss, enabled = !saving) { Text(L.s(R.string.msg_cancel_77df)) }
                    JournalPrimaryButton(enabled = title.isNotBlank() && composer.isNotBlank() && !saving, onClick = {
                        saving = true
                        save(Piece(piece?.id ?: 0, title, composer, difficulty, status, notes, piece?.catalogWorkId, piece?.createdAt ?: 0, instrument.trim(), genre.trim(), era.trim())) { success ->
                            saving = false
                            if (success) dismiss()
                        }
                    }) { Text(L.s(R.string.msg_save_piece_bfeb)) }
                }
            }
        }
    }
}
@Composable private fun ComingSoon(library: LibraryState, eyebrow: String, title: String, description: String, icon: ImageVector, features: List<String>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Heading(eyebrow, title, description)
        Panel {
            Text(L.s(R.string.msg_a_reflection_in_numbers_d5d7), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(L.s(R.string.msg_the_hours_become_nsomething_beautiful_83f3), style = MaterialTheme.typography.headlineMedium)
            Text(L.s(R.string.msg_preview_monthly_and_yearly_reflections_are_c_bbc8), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (eyebrow == "Wrapped" && !library.loading) {
            Text(L.s(R.string.msg_recorded_on_this_device_960b), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            WeeklyActivity(library.sessions)
        }
        Panel {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 16.dp).size(40.dp))
            Text(L.s(R.string.msg_coming_in_a_future_movement_ed7e), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            features.forEach { feature -> Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(12.dp)); Text(feature) } }
        }
        Text(L.s(R.string.msg_for_now_make_music_your_practice_sessions_ar_474c), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun PieceProgress(piece: Piece) {
    val progress = when (piece.status) {
        LearningStatus.PLANNED -> 0f
        LearningStatus.LEARNING -> .33f
        LearningStatus.POLISHING -> .67f
        LearningStatus.READY -> 1f
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(L.s(R.string.msg_learning_stage_piece_status_label_3e17, (piece.status.label)), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f))
    }
}

@Composable private fun WeeklyActivity(sessions: List<PracticeSession>) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val totals = days.map { day -> sessions.filter { AchievementEngine.recordedDay(it, ZoneId.systemDefault()) == day }.sumOf { it.durationMillis } }
    val peak = totals.maxOrNull()?.coerceAtLeast(60000L) ?: 60000L
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(L.s(R.string.msg_the_past_seven_days_584c), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(L.s(R.string.msg_totals_sum_60000_min_cb8a, (totals.sum() / 60000)), color = MaterialTheme.colorScheme.primary)
        }
        Text(L.s(R.string.msg_every_return_to_the_piano_counts_125f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            days.forEachIndexed { index, day ->
                Column(Modifier.weight(1f).semantics(mergeDescendants = true) {
                    contentDescription = L.s(R.string.msg_day_format_datetimeformatter_ofpattern_eeee_a1cb, (day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM").withLocale(L.locale))), (totals[index] / 60000))
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.fillMaxWidth().height(88.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.widthIn(max = 30.dp).fillMaxWidth().height(88.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .04f), RoundedCornerShape(8.dp)))
                        Box(Modifier.widthIn(max = 30.dp).fillMaxWidth().height((4 + 84 * totals[index].toFloat() / peak).dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)))
                    }
                    Text(day.format(DateTimeFormatter.ofPattern("EEEEE").withLocale(L.locale)), color = if (day == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (totals.all { it == 0L }) Text(L.s(R.string.msg_no_practice_recorded_this_week_yet_bf77), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun PracticeDial(timer: TimerState, motion: Boolean) {
    val ink = MaterialTheme.colorScheme.primary
    // Each revolution represents one minute; the session remains open ended.
    val target = (timer.elapsed % 60000) / 60000f
    val progress by animateFloatAsState(target, tween(if (motion && target > .01f) 200 else 0), label = "Practice minute")
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val diameter = minOf(maxWidth, 220.dp)
        Box(Modifier.size(diameter).semantics(mergeDescendants = true) {
            contentDescription = L.s(R.string.msg_practice_time_duration_timer_elapsed_if_time_2674, (duration(timer.elapsed)), (L.fixed(if (timer.running) "Running" else "Paused")))
        }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                drawCircle(ink.copy(alpha = .025f))
                drawCircle(ink.copy(alpha = .12f), style = Stroke(2.dp.toPx()))
                drawArc(ink, -90f, progress * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Piano, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                BasicText(duration(timer.elapsed), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center), maxLines = 1, autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 42.sp))
                Text(if (timer.running) L.s(R.string.msg_in_the_moment_6a2c) else if (timer.elapsed > 0) L.s(R.string.msg_take_a_breath_a71f) else L.s(R.string.msg_your_time_your_tempo_8196), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
            }
        }
    }
    Text(L.s(R.string.msg_one_revolution_one_minute_1c74), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
