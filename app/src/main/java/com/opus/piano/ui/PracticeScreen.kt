package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.service.SmartPracticeService

@Composable internal fun PracticeScreen(library: LibraryState, model: OpusViewModel, profile: ProfileViewModel, motion: Boolean = true) {
    val timer by model.timer.collectAsStateWithLifecycle()
    val settings by profile.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val app=context.applicationContext as com.opus.piano.OpusApplication
    val completedId by model.practice.completedSession.collectAsStateWithLifecycle()
    val completedSession by produceState<com.opus.piano.data.PracticeSession?>(null,completedId) {
        value=completedId?.let {app.database.practiceDao().session(it)}
    }
    completedSession?.let {HistoricalReview(it,emptyList(),completion=true) {model.practice.dismissCompletion()}}
    val request by app.practiceRequest.collectAsStateWithLifecycle()
    val issue by model.practice.captureIssue.collectAsStateWithLifecycle()
    var smart by remember { mutableStateOf(timer.smart || settings.smartEnabled) }
    var privacy by remember { mutableStateOf(false) }
    var convert by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    var choose by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val failedRecovery by model.practice.recoveryFailed.collectAsStateWithLifecycle()
    val unsaved by model.practice.unsaved.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var exported by remember { mutableStateOf<ByteArray?>(null) }
    var fresh by remember { mutableStateOf(false) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val raw = requireNotNull(model.practice.recoveryBytes())
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    requireNotNull(context.contentResolver.openOutputStream(uri, "wt")).use { it.write(raw) }
                }
                exported = raw
            } catch (c: kotlinx.coroutines.CancellationException) { throw c }
            catch (_: Exception) { model.practice.notice.value = L.s(R.string.practice_unsaved) }
        }
    }
    if (fresh) AlertDialog(onDismissRequest={fresh=false},title={Text(L.s(R.string.recovery_fresh))},text={Text(L.s(R.string.recovery_confirm))},
        confirmButton={JournalTextButton(onClick={exported?.let { model.practice.freshAfterExport(it) };fresh=false}) { Text(L.s(R.string.recovery_fresh)) }},
        dismissButton={JournalTextButton(onClick={fresh=false}) { Text(L.s(R.string.msg_cancel_77df)) }})
    fun startSmart() {
        SmartPracticeService.start(context)
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result[Manifest.permission.RECORD_AUDIO] == true || ContextCompat.checkSelfPermission(context,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startSmart()
        else { model.practice.captureIssue.value="PermissionDenied";model.practice.notice.value = L.s(R.string.msg_microphone_permission_denied_manual_practice_bac5) }
    }
    fun start() {
        if (smart) privacy = true else model.practice.command { start(false) }
    }
    LaunchedEffect(request,timer.ready,failedRecovery) {
        if(request.isNotEmpty() && (timer.ready || failedRecovery)) {
            app.practiceRequest.value=""
            when {
                failedRecovery || timer.status == "Review" -> model.practice.notice.value=L.s(R.string.practice_request_unavailable)
                request=="choose" -> { expanded=true;choose=true }
                timer.token.isNotEmpty() -> model.practice.notice.value=L.s(R.string.pending_session)
                settings.smartEnabled -> { smart=true;privacy=true }
                else -> model.practice.command { start(false) }
            }
        }
    }
    if (timer.status == "Review") { PracticeReviewScreen(timer,model); return }
    Scaffold(bottomBar={
        Column(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(4.dp)) {
            JournalPrimaryButton(onClick={
                if(timer.running) model.practice.command { pause() }
                else if(timer.token.isNotEmpty() && timer.smart!=smart) convert=true else start()
            },modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),enabled=timer.ready && !timer.saving) { Text(if(timer.running) L.s(R.string.msg_pause_7819) else if(timer.token.isEmpty()) L.s(R.string.msg_start_practice_ae1b) else L.s(R.string.msg_resume_b3bd)) }
            JournalTextButton(onClick=model::finish,enabled=timer.token.isNotEmpty() || timer.notes.isNotBlank() || timer.parts.any { it.notes.isNotBlank() },modifier=Modifier.fillMaxWidth()) { Text(L.s(R.string.msg_finish_review_04ae)) }
        }
    }) { padding ->
    Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        if (failedRecovery) {
            Text(L.s(R.string.recovery_failed))
            JournalPrimaryButton(onClick={model.practice.retryRecovery()}) { Text(L.s(R.string.practice_retry)) }
            JournalSecondaryButton(onClick={export.launch("Gould-recovery.json")}) { Text(L.s(R.string.recovery_export)) }
            if (exported != null) JournalTextButton(onClick={fresh=true}) { Text(L.s(R.string.recovery_fresh)) }
            return@Column
        }
        if(!timer.ready) { Text(L.s(R.string.msg_recovering_your_practice_e75c));return@Column }
        if (unsaved) {
            Text(L.s(R.string.practice_unsaved), color=MaterialTheme.colorScheme.error)
            JournalTextButton(onClick={model.practice.retryPersistence()}) { Text(L.s(R.string.practice_retry)) }
        }
        EditorialHeader(L.fixed("Practice"))
        JournalCard {
            Column(Modifier.fillMaxWidth().padding(vertical=16.dp),horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(timer.parts[timer.active].displayTitle,style=MaterialTheme.typography.headlineMedium)
        Text(duration(timer.elapsed),style=MaterialTheme.typography.displayLarge,fontFamily=MaterialTheme.typography.bodyLarge.fontFamily)
        Text(L.s(R.string.msg_credited_practice_elapsed_duration_timer_wal_0c94, (duration(timer.wallElapsed))),style=MaterialTheme.typography.bodyMedium)
        if(timer.mode!="Manual") Text(L.s(R.string.msg_detected_active_duration_timer_detectedactiv_764f, (duration(timer.detectedActive))))
        ActivityIndicator(if(smart && issue.isNotEmpty()) issue else if(timer.running && timer.smart && timer.status!="Automatically paused") timer.signal else timer.status,timer.running && timer.smart,motion)
            }
        }
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
            Switch(smart,{ value -> if(!timer.running) { smart=value;profile.smartPreference(value) } },enabled=!timer.running,modifier=Modifier.semantics { contentDescription=L.fixed("Smart Practice") })
            Text(L.s(R.string.msg_smart_practice_experimental_34d8),Modifier.padding(start=12.dp))
        }
        if(timer.token.isNotEmpty()) JournalTextButton(onClick={discard=true}) { Text(L.s(R.string.msg_discard_session_38ed)) }
        JournalTextButton(onClick={expanded=!expanded}) { Text(L.s(R.string.practice_expand)) }
        if (expanded) {
        Panel {
            Text(L.s(R.string.msg_pieces_in_this_session_6dbf),style = MaterialTheme.typography.labelSmall)
            JournalSecondaryButton(onClick = { choose = true }) { Text(L.s(R.string.msg_select_add_piece_3abf)) }
            DropdownMenu(choose,{ choose = false }) {
                DropdownMenuItem(text = { Text(L.s(R.string.msg_free_practice_524f)) },onClick = { model.select(null); choose = false })
                library.pieces.forEach { piece -> DropdownMenuItem(text = { Text("${piece.title} · ${piece.composer}") },onClick = { model.select(piece); choose = false }) }
            }
            timer.parts.forEachIndexed { i,p -> if (p.pieceId !in timer.removed) {
                Row {
                    JournalTextButton(onClick = { model.selectPart(i) },Modifier.weight(1f)) { Text(L.s(R.string.msg_if_i_timer_active_else_p_title_duration_p_du_9c1d, (if (i == timer.active) "▶ " else ""), (p.displayTitle), (duration(p.durationMillis)))) }
                    if (p.pieceId != null) JournalTextButton(onClick = { model.remove(i) }) { Text(L.s(R.string.msg_remove_e963)) }
                }
            } }
        }
        Text(L.s(R.string.msg_repertoire_notes_are_separate_session_notes_97f9))
        }
    }
    }
    if (privacy) AlertDialog(onDismissRequest = { privacy = false },title = { Text(L.s(R.string.msg_listen_during_practice_cc91)) },text = { Text(L.s(R.string.msg_goldberg_analyzes_ambient_microphone_sound_l_7f65)) },confirmButton = { JournalTextButton(onClick = {
        privacy = false
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        permission.launch(permissions.toTypedArray())
    }) { Text(L.s(R.string.msg_enable_listening_d7de)) } },dismissButton = { JournalTextButton(onClick = { privacy = false }) { Text(L.s(R.string.msg_cancel_77df)) } })
    if (convert) AlertDialog(onDismissRequest = { convert = false },title = { Text(L.s(R.string.msg_change_timing_mode_38a6)) },text = { Text(L.s(R.string.msg_this_becomes_a_mixed_session_existing_credit_768a)) },confirmButton = { JournalTextButton(onClick = { convert = false; start() }) { Text(L.s(R.string.msg_convert_continue_2747)) } },dismissButton = { JournalTextButton(onClick = { convert = false }) { Text(L.s(R.string.msg_cancel_77df)) } })
    if (discard) AlertDialog(onDismissRequest = { discard = false },title = { Text(L.s(R.string.msg_discard_unfinished_practice_c660)) },text = { Text(L.s(R.string.msg_this_deletes_the_unfinished_draft_and_its_no_875d)) },confirmButton = { JournalTextButton(onClick = { model.reset(); discard = false }) { Text(L.s(R.string.msg_discard_36ff)) } },dismissButton = { JournalTextButton(onClick = { discard = false }) { Text(L.s(R.string.msg_keep_466f)) } })
}

@Composable private fun ActivityIndicator(status:String,active:Boolean,motion:Boolean) {
    val ink=MaterialTheme.colorScheme.primary
    val muted=MaterialTheme.colorScheme.onSurfaceVariant
    val playing=active && status=="Playing detected"
    val level by androidx.compose.animation.core.animateFloatAsState(if(playing) 1f else 0f,androidx.compose.animation.core.tween(if(motion) 180 else 0),label="Detection state")
    Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        repeat(5) { index -> androidx.compose.foundation.Canvas(Modifier.width(4.dp).height(28.dp)) {
            val h=(.2f+level*(if(index==2) .8f else .5f))*size.height
            drawRoundRect(color=if(active) ink else muted,topLeft=androidx.compose.ui.geometry.Offset(0f,(size.height-h)/2),size=androidx.compose.ui.geometry.Size(size.width,h),cornerRadius=androidx.compose.ui.geometry.CornerRadius(4f))
        } }
        Text(L.fixed(statusLabel(status)),Modifier.padding(start=8.dp),style=MaterialTheme.typography.bodyMedium)
    }
}
internal fun statusLabel(status:String)=when(status) { "PermissionDenied"->"Microphone permission denied";"Unavailable"->"Microphone unavailable";"DetectionError"->"Detection error";"Automatically paused"->"Silence · automatically paused";else->status }
