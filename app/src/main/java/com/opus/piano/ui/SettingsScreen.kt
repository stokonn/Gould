package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable internal fun SettingsScreen(profile: ProfileViewModel, model: OpusViewModel, onboarding: () -> Unit, back: () -> Unit) {
    var page by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    androidx.activity.compose.BackHandler(page.isNotEmpty()) { page="" }
    val context = androidx.compose.ui.platform.LocalContext.current
    val local by profile.local.collectAsStateWithLifecycle()
    val app=context.applicationContext as com.opus.piano.OpusApplication
    val scope=rememberCoroutineScope()
    val metered by app.media.cellular.collectAsStateWithLifecycle()
    var tux by remember {mutableStateOf(false)}
    var legal by remember { mutableStateOf(false) }
    var secret by remember { mutableStateOf(false) }
    var taps by remember { mutableIntStateOf(0) }
    var lastTap by remember { mutableLongStateOf(0) }
    val sessionCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { it?.let { uri -> profile.csv(uri,false) } }
    val partCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { it?.let { uri -> profile.csv(uri,true) } }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) local?.let { profile.reminder(true,it.reminderHour,it.reminderMinute) }
        else profile.notice.value = L.s(R.string.msg_notifications_are_blocked_enable_them_in_and_a3dd)
    }
    val settings by profile.settings.collectAsStateWithLifecycle()
    val busy by profile.busy.collectAsStateWithLifecycle()
    val googleAccount by profile.googleAccount.collectAsStateWithLifecycle()
    val preview by profile.preview.collectAsStateWithLifecycle()
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { it?.let(profile::export) }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(profile::inspect) }
    val usage by profile.cacheUsage.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { profile.measureCache() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CatalogBack(if(page.isEmpty()) L.s(R.string.msg_settings_c7f7) else page) { if(page.isEmpty()) back() else page="" } }
        if(page.isEmpty()) {
            listOf("Appearance & Customize","Language","Smart Practice","Reminders","Data & Storage","Privacy & Permissions","About").forEach { name ->
                item { SettingsRow(L.fixed(name),action={page=name},trailing={Text("›")});HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant) }
            }
        }
        if(page.isEmpty()) item { SettingsRow(L.s(R.string.google_account),action={page="Google"}) }
        when(page) {
            "Google" -> item {
                Text(L.s(R.string.google_identity_only),style=MaterialTheme.typography.bodyMedium)
                if(googleAccount == null) JournalPrimaryButton(enabled=!busy,onClick={profile.googleSignIn(context)}) {Text(L.s(R.string.google_sign_in))}
                else {
                    Text(googleAccount.orEmpty(),style=MaterialTheme.typography.bodyMedium)
                    JournalSecondaryButton(enabled=!busy,onClick=profile::googleSignOut) {Text(L.s(R.string.google_sign_out))}
                }
            }

            "Appearance & Customize" -> {
        item { Panel {
            SectionTitle(L.s(R.string.msg_appearance_41de))
            listOf("System", "Light", "Dark").forEach { theme ->
                Surface(modifier = Modifier.semantics { selected = settings.theme == theme; role = Role.RadioButton }, onClick = { if (!busy) profile.theme(theme) }, enabled = !busy) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(settings.theme == theme, null); Spacer(Modifier.width(12.dp)); Text(if (theme == "System") L.s(R.string.msg_follow_system_1b16) else L.fixed(theme))
                    }
                }
            }
            Text(L.s(R.string.msg_text_size_and_motion_follow_android_accessib_8163), style = MaterialTheme.typography.bodySmall)
        } }
        item { Panel {
            SectionTitle(L.s(R.string.msg_typography_f4be))
            listOf("Editorial" to L.s(R.string.msg_goldberg_typography_6730), "System" to L.s(R.string.msg_system_font_53af)).forEach { (id, label) ->
                Surface(onClick = { profile.typography(id) }, enabled = !busy, modifier = Modifier.semantics { selected = settings.typography == id; role = Role.RadioButton }) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(settings.typography == id, null); Spacer(Modifier.width(12.dp)); Text(L.fixed(label))
                    }
                }
            }
            Text(L.s(R.string.msg_cormorant_garamond_headings_with_inter_text_21da), style = MaterialTheme.typography.bodySmall)
        } }
        item { CustomizePanel(profile) }
            }
            "Language" -> item { Panel {
                SectionTitle(L.s(R.string.msg_language_89b8))
                val current=androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags()
                listOf("" to L.s(R.string.msg_follow_system_1b16),"cs" to L.s(R.string.msg_e_tina_a56c),"en" to L.s(R.string.msg_english_649d)).forEach { (tag,label) ->
                    JournalTextButton(onClick={androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags(tag))}) { RadioButton(current==tag,null);Text(L.fixed(label)) }
                }
            } }
            "Smart Practice" -> item { Panel {
                SectionTitle(L.s(R.string.msg_smart_practice_1d73))
                Row { Switch(settings.smartEnabled,profile::smartPreference,modifier=Modifier.semantics { contentDescription=L.fixed("Smart Practice") });Text(L.s(R.string.msg_enable_smart_practice_eae0),Modifier.padding(12.dp)) }
                var sensitivity by remember(settings.sensitivity) { mutableFloatStateOf(settings.sensitivity.toFloat()) }
                Text(L.s(R.string.msg_sensitivity_sensitivity_toint_cd78, (sensitivity.toInt())))
                Slider(sensitivity,{sensitivity=it},onValueChangeFinished={profile.smartSettings(sensitivity.toInt(),settings.pauseSeconds)},valueRange=0f..100f)
                Choice(L.s(R.string.msg_auto_pause_35c6),(2..30).map { L.s(R.string.msg_it_seconds_cde9, (it)) },L.s(R.string.msg_settings_pauseseconds_seconds_5d01, (settings.pauseSeconds))) { profile.smartSettings(settings.sensitivity,it.substringBefore(' ').toInt()) }
                Text(L.s(R.string.msg_experimental_piano_like_activity_detection_r_19fb))
            } }
            "Reminders" -> {
        item { Panel {
            SectionTitle(L.s(R.string.msg_practice_reminder_d5b9))
            val value = local
            if (value != null) {
                Text(L.s(R.string.msg_around_02d_02d_daily_android_may_delay_deliv_c459).format(value.reminderHour,value.reminderMinute))
                Switch(value.reminderEnabled,{ enabled ->
                    if (enabled && android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) notifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    else profile.reminder(enabled,value.reminderHour,value.reminderMinute)
                },enabled = !busy,modifier=Modifier.semantics { contentDescription=L.fixed("Reminders") })
                JournalSecondaryButton(onClick = { android.app.TimePickerDialog(context,{ _,h,m -> profile.reminder(value.reminderEnabled,h,m) },value.reminderHour,value.reminderMinute,true).show() }) { Text(L.s(R.string.msg_choose_time_c3ee)) }
            }
        } }
            }
            "Data & Storage" -> {
        item {
            SectionTitle(L.s(R.string.downloaded_content))
            SettingsRow(L.s(R.string.mobile_media),L.s(R.string.mobile_media_description),action={scope.launch(kotlinx.coroutines.Dispatchers.IO) {app.media.consent(!metered)}},trailing={Switch(metered,{value->scope.launch(kotlinx.coroutines.Dispatchers.IO) {app.media.consent(value)}},modifier=Modifier.semantics {contentDescription=L.s(R.string.mobile_media)})})
            Text(L.s(R.string.supplementary_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item { Panel {
            SectionTitle(L.s(R.string.msg_cache_management_d246))
            Text(usage?.let { L.s(R.string.msg_disposable_downloads_1f_mib_0ce1).format(it / 1048576.0) } ?: L.s(R.string.msg_measuring_downloaded_cache_2599))
            Text(L.s(R.string.msg_clearing_removes_downloaded_images_and_respo_ebd8), style = MaterialTheme.typography.bodySmall)
            JournalSecondaryButton(onClick = profile::clearCache, enabled = !busy) { Text(L.s(R.string.msg_clear_cache_5221)) }
        } }
        item { Panel {
            SectionTitle(L.s(R.string.msg_backup_restore_3bdd))
            Text(L.s(R.string.msg_keep_a_local_copy_of_your_journal_profile_an_8f6d))
            JournalPrimaryButton(onClick = { export.launch("Goldberg-backup.json") }, enabled = !busy) { Text(L.s(R.string.msg_export_backup_6043)) }
            JournalSecondaryButton(onClick = { restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }, enabled = !busy) { Text(L.s(R.string.msg_import_backup_1b40)) }
            Text(L.s(R.string.pref_backup_note))
            Text(L.s(R.string.msg_format_1_backups_from_0_6_and_0_7_remain_imp_8590), style = MaterialTheme.typography.bodySmall)
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        } }
        item { Panel {
            SectionTitle(L.s(R.string.msg_statistics_export_f996))
            Text(L.s(R.string.msg_separate_utf_8_reports_prevent_repeated_sess_ce91))
            JournalSecondaryButton(onClick = { sessionCsv.launch("Goldberg-sessions.csv") }, enabled = !busy) { Text(L.s(R.string.msg_export_sessions_csv_688a)) }
            JournalSecondaryButton(onClick = { partCsv.launch("Goldberg-pieces.csv") }, enabled = !busy) { Text(L.s(R.string.msg_export_piece_times_csv_bfc9)) }
        } }
            }
            "Privacy & Permissions" -> item { Panel {
                Text(L.s(R.string.google_identity_only),style=MaterialTheme.typography.bodySmall)
                SectionTitle(L.s(R.string.msg_privacy_permissions_d558))
                val mic=androidx.core.content.ContextCompat.checkSelfPermission(context,android.Manifest.permission.RECORD_AUDIO)==android.content.pm.PackageManager.PERMISSION_GRANTED
                val notificationsAllowed=androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
                Text(if(mic) L.s(R.string.msg_microphone_permission_granted_e72e) else L.s(R.string.msg_microphone_permission_not_granted_4124))
                Text(if(notificationsAllowed) L.s(R.string.msg_notifications_allowed_53e3) else L.s(R.string.msg_notifications_blocked_5ba8))
                Text(L.s(R.string.msg_permission_is_requested_only_when_needed_mic_95eb))
                JournalTextButton(onClick={context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:${context.packageName}")))}) { Text(L.s(R.string.msg_open_android_settings_610e)) }
                JournalTextButton(onClick={legal=true}) { Text(L.s(R.string.msg_privacy_licenses_2fee)) }
            } }
            "About" -> {
        item { Panel {
            Image(painterResource(R.drawable.ic_goldberg_classic_foreground),L.s(R.string.goldberg_logo),Modifier.size(80.dp).combinedClickable(onClick={},onLongClick={tux=true}))

            SectionTitle(L.s(R.string.msg_about_6b21))
            JournalTextButton(onClick = {
                val now = android.os.SystemClock.elapsedRealtime()
                taps = if (now-lastTap <= 1500) taps+1 else 1; lastTap=now
                if (taps>=7) {
                    taps=0
                    if (model.timer.value.running) profile.notice.value=L.s(R.string.msg_pause_practice_before_opening_bach_s_secret_9404)
                    else { secret=true; profile.curious() }
                }
            }) { Text(L.s(R.string.version_092)) }
            JournalTextButton(onClick = onboarding) { Text(L.s(R.string.msg_revisit_introduction_caf0)) }
            JournalTextButton(onClick = { legal=true }) { Text(L.s(R.string.msg_privacy_licenses_2fee)) }
        } }            }
        }
    }
    if (tux) TuxScreen {tux=false}
    if (legal) LegalScreen { legal=false }
    if (secret) HarpsichordScreen { secret=false }
    preview?.let { summary ->
        val timer by model.timer.collectAsStateWithLifecycle()
        AlertDialog(onDismissRequest = profile::cancelRestore, title = { Text(L.s(R.string.msg_replace_this_device_s_journal_ff1d)) }, text = { Text(L.s(R.string.msg_summary_n_nall_current_saved_data_will_be_re_4d42, (summary), (if (timer.elapsed > 0 || timer.running) L.fixed(" You have an unfinished session.") else ""))) },
            confirmButton = { JournalTextButton(enabled = !busy && !timer.saving, onClick = { profile.restore(model::reset) }) { Text(L.s(R.string.msg_replace_restore_4066)) } },
            dismissButton = { JournalTextButton(enabled = !busy, onClick = profile::cancelRestore) { Text(L.s(R.string.msg_cancel_77df)) } })
    }
}
