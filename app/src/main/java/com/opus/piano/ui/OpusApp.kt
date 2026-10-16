package com.opus.piano.ui

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
import java.time.*
import java.time.format.DateTimeFormatter

private enum class Destination(val label: String, val icon: ImageVector) {
    OVERVIEW("Home", Icons.Outlined.Home), HOME("Profile", Icons.Outlined.Person), REPERTOIRE("Repertoire", Icons.Outlined.LibraryMusic),
    PRACTICE("Practice", Icons.Outlined.Piano), BADGES("Badges", Icons.Outlined.EmojiEvents), WRAPPED("Wrapped", Icons.Outlined.Insights)
}
@Composable fun OpusApp(model: OpusViewModel, catalog: CatalogViewModel, experience: ExperienceViewModel, profile: ProfileViewModel) {
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
    val catalogNotice by catalog.notice.collectAsStateWithLifecycle()
    val experienceNotice by experience.notice.collectAsStateWithLifecycle()
    val catalogData by catalog.snapshot.collectAsStateWithLifecycle()
    val badges by experience.badges.collectAsStateWithLifecycle()
    val profileNotice by profile.notice.collectAsStateWithLifecycle()
    var knownUnlocks by remember { mutableStateOf<Set<String>?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(badges) {
        if (badges.isNotEmpty()) {
            val unlocked = badges.filter { it.unlocked }.map { it.definition.id }.toSet()
            val previous = knownUnlocks; knownUnlocks = unlocked
            if (previous != null) badges.firstOrNull { it.unlocked && it.definition.id !in previous }?.let { snackbar.showSnackbar("Badge unlocked: ${it.definition.name}") }
        }
    }
    LaunchedEffect(profileNotice) { profileNotice?.let { snackbar.showSnackbar(it); profile.notice.value = null } }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); model.dismissMessage() } }
    LaunchedEffect(catalogNotice) { catalogNotice?.let { snackbar.showSnackbar(it); catalog.dismissNotice() } }
    LaunchedEffect(experienceNotice) { experienceNotice?.let { snackbar.showSnackbar(it); experience.dismissNotice() } }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(modifier = Modifier.imePadding(), containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                listOf(Destination.OVERVIEW, Destination.REPERTOIRE, Destination.PRACTICE, Destination.HOME, Destination.BADGES).forEach { destination ->
                    NavigationBarItem(colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant), selected = (if (current == SETTINGS_ROUTE) selectedTab(nav.previousBackStackEntry?.destination?.route ?: PROFILE_ROUTE) else selectedTab(current)) == destination.name, onClick = {
                        focus.clearFocus()
                        keyboard?.hide()
                        nav.selectTopLevel(destination.name)
                    }, icon = { Icon(destination.icon, contentDescription = null) }, label = { Text(destination.label, style = MaterialTheme.typography.labelSmall, maxLines = 2) })
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(when { current == Destination.OVERVIEW.name -> "Your journal"; current == PROFILE_ROUTE -> "Profile"; current == SETTINGS_ROUTE -> "Settings"; current.startsWith("catalog") -> "Discover"; current.startsWith("piece/") -> "Composition"; current.startsWith("badge/") -> "Badges"; current.startsWith("recordings/") -> "Recordings"; else -> Destination.entries.firstOrNull { it.name == current }?.label ?: "Journal" }, style = MaterialTheme.typography.titleLarge, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                if (current == Destination.HOME.name || current == Destination.OVERVIEW.name) {
                    IconButton(onClick = { nav.navigate(SETTINGS_ROUTE) { launchSingleTop = true } }) { Icon(Icons.Outlined.Settings, "Settings") }
                } else if (current != SETTINGS_ROUTE) {
                    IconButton(onClick = { nav.navigate("catalog") { launchSingleTop = true } }) { Icon(Icons.Outlined.Search, "Universal search") }
                }
            }
            NavHost(navController = nav, startDestination = Destination.OVERVIEW.name, modifier = Modifier.widthIn(max = 840.dp).fillMaxSize(),
                enterTransition = { fadeIn(tween(if (motionScale == 0f) 0 else 220)) }, exitTransition = { fadeOut(tween(if (motionScale == 0f) 0 else 150)) }) {
                composable(Destination.OVERVIEW.name) {
                    val settings by profile.settings.collectAsStateWithLifecycle()
                    JournalHome(library, settings.username, { nav.selectTopLevel(Destination.PRACTICE.name) }, { nav.selectTopLevel(Destination.REPERTOIRE.name) }, { nav.selectTopLevel(Destination.HOME.name) }, { nav.navigate(Destination.WRAPPED.name) })
                }
                composable(SETTINGS_ROUTE) { SettingsScreen(profile, model) { nav.popBackStack() } }
                composable(Destination.HOME.name) { ProfileScreen(library, badges, profile) { nav.navigate("badge/$it") } }
                composable(Destination.REPERTOIRE.name) { RepertoireScreen(library, catalogData, model, { nav.navigate("catalog") { launchSingleTop = true } }, { nav.navigate("piece/$it") }) }
                composable("catalog") { CatalogScreen(catalog, library, { nav.popBackStack() }, { nav.navigate("catalog/composer/$it") }, { nav.navigate("catalog/work/$it") }, { nav.navigate("piece/$it") }) }
                composable("catalog/composer/{id}") { route -> ComposerScreen(route.arguments?.getString("id").orEmpty(), catalog, library, { nav.popBackStack() }, { nav.navigate("catalog/work/$it") }) }
                composable("catalog/work/{id}") { route ->
                    CompositionScreen(route.arguments?.getString("id"), null, catalog, experience, library, model, { nav.popBackStack() }, { nav.navigate("catalog/composer/$it") }, { nav.navigate("recordings/$it") }) { piece ->
                        if (model.focus(piece)) nav.navigate(Destination.PRACTICE.name) { launchSingleTop = true }
                    }
                }
                composable("piece/{id}") { route ->
                    CompositionScreen(null, route.arguments?.getString("id")?.toLongOrNull(), catalog, experience, library, model, { nav.popBackStack() }, { nav.navigate("catalog/composer/$it") }, { nav.navigate("recordings/$it") }) { piece ->
                        if (model.focus(piece)) nav.navigate(Destination.PRACTICE.name) { launchSingleTop = true }
                    }
                }
                composable("recordings/{key}") { route ->
                    val key = route.arguments?.getString("key").orEmpty()
                    val title = if (key.startsWith("catalog:")) catalogData.works.firstOrNull { it.id == key.substringAfter(':') }?.title else library.pieces.firstOrNull { it.id.toString() == key.substringAfter(':') }?.title
                    RecordingsScreen(key, title ?: "Your musical reference shelf", experience) { nav.popBackStack() }
                }
                composable("badge/{id}") { route -> BadgeDetailScreen(route.arguments?.getString("id").orEmpty(), experience) { nav.popBackStack() } }
                composable(Destination.PRACTICE.name) { Practice(library, model.timer.collectAsStateWithLifecycle().value, model, motionScale != 0f) }
                composable(Destination.BADGES.name) { BadgesScreen(experience) { nav.navigate("badge/$it") } }
                composable(Destination.WRAPPED.name) { ComingSoon(library, "Wrapped", "Your music.\nYour story.", "A thoughtful look back at your time at the piano. Monthly and yearly insights are coming in a future phase.", Icons.Outlined.Insights, listOf("Monthly reflections", "Your year at the piano", "Repertoire highlights")) }
            }
        }
    }
}
@Composable internal fun Heading(eyebrow: String, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable internal fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .18f)), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
@Composable internal fun SectionTitle(title: String) { Text(title, style = MaterialTheme.typography.titleLarge) }
internal fun duration(millis: Long): String {
    val seconds = millis / 1000
    return "%02d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60)
}
private fun dayOf(timestamp: Long): LocalDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
@Composable internal fun SessionRow(session: PracticeSession, parts: List<SessionPart> = emptyList()) {
    var expanded by rememberSaveable(session.id) { mutableStateOf(false) }
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.MusicNote, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(session.pieceTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(Instant.ofEpochMilli(session.completedAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM · HH:mm")), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(duration(session.durationMillis), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        if (parts.isNotEmpty()) {
            TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide summary" else "Session summary") }
            if (expanded) parts.forEach { part ->
                Text("${part.title} · ${duration(part.durationMillis)}")
                if (part.notes.isNotBlank()) Text(part.notes, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
@Composable internal fun Choice(label: String, options: List<String>, selected: String, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text("$label: $selected", Modifier.weight(1f, fill = false), maxLines = 2, overflow = TextOverflow.Ellipsis); Icon(Icons.Outlined.ExpandMore, null) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option, maxLines = 3, overflow = TextOverflow.Ellipsis) }, onClick = { choose(option); expanded = false }) }
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
        Surface(shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("YOUR REPERTOIRE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                SectionTitle(if (piece == null) "A new beginning" else "Refine the details")
                OutlinedTextField(title, { title = it }, label = { Text("Title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(composer, { composer = it }, label = { Text("Composer *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Choice("Difficulty", Difficulty.entries.map { it.label }, difficulty.label) { label -> difficulty = Difficulty.entries.first { it.label == label } }
                Choice("Status", LearningStatus.entries.map { it.label }, status.label) { label -> status = LearningStatus.entries.first { it.label == label } }
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optional)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = { metadataOpen = !metadataOpen }) { Text(if (metadataOpen) "Hide musical metadata" else "Musical metadata (optional)") }
                if (metadataOpen) {
                    OutlinedTextField(instrument, { instrument = it }, label = { Text("Instrument") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(genre, { genre = it }, label = { Text("Genre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(era, { era = it }, label = { Text("Musical era") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                Text("* Required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = dismiss, enabled = !saving) { Text("Cancel") }
                    Button(enabled = title.isNotBlank() && composer.isNotBlank() && !saving, onClick = {
                        saving = true
                        save(Piece(piece?.id ?: 0, title, composer, difficulty, status, notes, piece?.catalogWorkId, piece?.createdAt ?: 0, instrument.trim(), genre.trim(), era.trim())) { success ->
                            saving = false
                            if (success) dismiss()
                        }
                    }) { Text("Save piece") }
                }
            }
        }
    }
}
@Composable private fun Practice(library: LibraryState, timer: TimerState, model: OpusViewModel, motion: Boolean) {
    var resetDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Heading("One note at a time", "Practice", "Be present. Let the rest fall quiet.")
        Text(timer.parts[timer.active].title, style = MaterialTheme.typography.titleLarge)
        Panel {
            Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                PracticeDial(timer, motion)
                Button(onClick = model::toggle, enabled = !timer.saving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(if (timer.running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null)
                    Text(if (timer.running) " Pause" else if (timer.elapsed > 0) " Resume" else " Start practice")
                }
                OutlinedButton(onClick = model::finish, enabled = timer.elapsed >= 1000 && !timer.saving, modifier = Modifier.fillMaxWidth()) { Text(if (timer.saving) "Saving…" else "Finish & save session") }
                if (timer.elapsed > 0 || timer.running) TextButton(onClick = { resetDialog = true }, enabled = !timer.saving) { Text("Discard session") }
            }
        }
        Panel {
            var expanded by remember { mutableStateOf(false) }
            Text("TODAY’S FOCUS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Box {
                OutlinedButton(onClick = { expanded = true }, enabled = !timer.saving) { Text(timer.parts[timer.active].title, Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis); Icon(Icons.Outlined.ExpandMore, null) }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Free practice") }, onClick = { model.select(null); expanded = false })
                    library.pieces.forEach { piece -> DropdownMenuItem(text = { Text("${piece.title} · ${piece.composer}") }, onClick = { model.select(piece); expanded = false }) }
                }
            }
            Text("Add pieces with the menu, then tap a piece to switch. Time is recorded for the active piece only.", style = MaterialTheme.typography.bodySmall)
            timer.parts.forEachIndexed { index, part ->
                if (part.pieceId !in timer.removed) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { model.selectPart(index) }, enabled = !timer.saving, modifier = Modifier.weight(1f)) { Text("${if (timer.active == index) "▶ " else ""}${part.title} · ${duration(part.durationMillis)}") }
                        if (part.pieceId != null) IconButton(onClick = { model.remove(index) }, enabled = !timer.saving) { Icon(Icons.Outlined.Close, "Remove ${part.title} from active selection; retain recorded time") }
                    }
                    if (timer.active == index) OutlinedTextField(part.notes, { model.note(index, it) }, label = { Text("Notes for ${part.title}") }, enabled = !timer.saving, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        Text("Time for scales, a tricky passage, or simply playing something you love. Your completed sessions stay on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        SectionTitle("Last session")
        AnimatedContent(library.sessions.firstOrNull(), transitionSpec = { fadeIn(tween(if (motion) 200 else 0)) togetherWith fadeOut(tween(if (motion) 120 else 0)) }, label = "Completed session") { session ->
            if (session != null) SessionRow(session, library.parts.filter { it.sessionId == session.id })
            else Text("Your completed practice will appear here.")
        }
    }
    if (resetDialog) AlertDialog(onDismissRequest = { resetDialog = false }, title = { Text("Discard this session?") }, text = { Text("The current timer will be reset without saving.") }, confirmButton = { TextButton(onClick = { model.reset(); resetDialog = false }) { Text("Discard") } }, dismissButton = { TextButton(onClick = { resetDialog = false }) { Text("Keep practicing") } })
}
@Composable private fun ComingSoon(library: LibraryState, eyebrow: String, title: String, description: String, icon: ImageVector, features: List<String>) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
        Heading(eyebrow, title, description)
        Panel {
            Text("A REFLECTION IN NUMBERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text("The hours become\nsomething beautiful.", style = MaterialTheme.typography.headlineMedium)
            Text("Preview · Monthly and yearly reflections are coming in a future movement.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (eyebrow == "Wrapped" && !library.loading) {
            Text("RECORDED ON THIS DEVICE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            WeeklyActivity(library.sessions)
        }
        Panel {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 16.dp).size(40.dp))
            Text("COMING IN A FUTURE MOVEMENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            features.forEach { feature -> Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.RadioButtonUnchecked, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(12.dp)); Text(feature) } }
        }
        Text("For now, make music. Your practice sessions are already being saved for the journey ahead.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text("Learning stage · ${piece.status.label}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp), color = MaterialTheme.colorScheme.primary, trackColor = Gold.copy(alpha = .12f))
    }
}

@Composable private fun WeeklyActivity(sessions: List<PracticeSession>) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val totals = days.map { day -> sessions.filter { AchievementEngine.recordedDay(it, ZoneId.systemDefault()) == day }.sumOf { it.durationMillis } }
    val peak = totals.maxOrNull()?.coerceAtLeast(60000L) ?: 60000L
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("The past seven days", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text("${totals.sum() / 60000} min", color = MaterialTheme.colorScheme.primary)
        }
        Text("Every return to the piano counts.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            days.forEachIndexed { index, day ->
                Column(Modifier.weight(1f).semantics(mergeDescendants = true) {
                    contentDescription = "${day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))}: ${totals[index] / 60000} minutes"
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.fillMaxWidth().height(88.dp), contentAlignment = Alignment.BottomCenter) {
                        Box(Modifier.widthIn(max = 30.dp).fillMaxWidth().height(88.dp).background(Gold.copy(alpha = .04f), RoundedCornerShape(8.dp)))
                        Box(Modifier.widthIn(max = 30.dp).fillMaxWidth().height((4 + 84 * totals[index].toFloat() / peak).dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)))
                    }
                    Text(day.format(DateTimeFormatter.ofPattern("EEEEE")), color = if (day == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        if (totals.all { it == 0L }) Text("No practice recorded this week yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            contentDescription = "Practice time ${duration(timer.elapsed)}. ${if (timer.running) "Running" else "Paused"}"
        }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                drawCircle(ink.copy(alpha = .025f))
                drawCircle(ink.copy(alpha = .12f), style = Stroke(2.dp.toPx()))
                drawArc(ink, -90f, progress * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Piano, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                BasicText(duration(timer.elapsed), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), style = MaterialTheme.typography.headlineMedium.copy(fontFamily = MaterialTheme.typography.bodyLarge.fontFamily, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center), maxLines = 1, autoSize = TextAutoSize.StepBased(minFontSize = 16.sp, maxFontSize = 42.sp))
                Text(if (timer.running) "IN THE MOMENT" else if (timer.elapsed > 0) "TAKE A BREATH" else "YOUR TIME, YOUR TEMPO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
            }
        }
    }
    Text("One revolution · one minute", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
