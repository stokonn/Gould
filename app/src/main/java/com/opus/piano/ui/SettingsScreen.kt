package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class,ExperimentalLayoutApi::class)
@Composable internal fun SettingsScreen(profile: ProfileViewModel, model: OpusViewModel, onboarding: () -> Unit, back: () -> Unit) {
    var page by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    androidx.activity.compose.BackHandler(page.isNotEmpty()) { page="" }
    val context = androidx.compose.ui.platform.LocalContext.current
    val local by profile.local.collectAsStateWithLifecycle()
    val app=context.applicationContext as com.opus.piano.OpusApplication
    val scope=rememberCoroutineScope()
    val restoreState by profile.restoreState.collectAsStateWithLifecycle()
    val metered by app.media.cellular.collectAsStateWithLifecycle()
    var replaceIdentity by remember {mutableStateOf<String?>(null)}
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
    replaceIdentity?.let {kind -> AlertDialog(onDismissRequest={replaceIdentity=null},title={Text(L.s(if(kind=="name") R.string.ui010_google_name else R.string.ui010_google_photo))},text={Text(L.s(R.string.ui010_identity_confirm))},confirmButton={JournalTextButton(onClick={if(kind=="name") profile.useGoogleName() else profile.replaceGooglePhoto();replaceIdentity=null}) {Text(L.s(R.string.ui010_apply))}},dismissButton={JournalTextButton(onClick={replaceIdentity=null}) {Text(L.s(R.string.ui010_keep_local))}})}
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { CatalogBack(if(page.isEmpty()) L.s(R.string.msg_settings_c7f7) else page) { if(page.isEmpty()) back() else page="" } }
        if(page.isEmpty()) {
            listOf(
                R.string.google_account to listOf("Google"),
                R.string.msg_appearance_41de to listOf("Appearance & Customize","Language"),
                R.string.msg_smart_practice_1d73 to listOf("Smart Practice","Reminders"),
                R.string.ui010_data_storage to listOf("Data & Storage","Privacy & Permissions"),
                R.string.ui010_about_app to listOf("About")
            ).forEach { (heading,names) -> item {
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    CompactSection(L.s(heading))
                    JournalCard(padding=PaddingValues(2.dp)) {
                        names.forEachIndexed {i,name ->
                            SettingsRow(if(name=="Google") L.s(R.string.google_account) else L.fixed(name),action={page=name},trailing={Text("›",color=MaterialTheme.colorScheme.primary)})
                            if(i<names.lastIndex) HorizontalDivider(Modifier.padding(horizontal=12.dp),color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=.5f))
                        }
                    }
                }
            }}
        }
        when(page) {
            "Google" -> item {
                Text(L.s(R.string.google_identity_only),style=MaterialTheme.typography.bodyMedium)
                if(googleAccount == null) JournalPrimaryButton(enabled=!busy,onClick={profile.googleSignIn(context)}) {Text(L.s(R.string.google_sign_in))}
                else {
                    Text(googleAccount.orEmpty(),style=MaterialTheme.typography.bodyMedium)
                    Text(L.s(R.string.ui010_identity_explanation),style=MaterialTheme.typography.bodySmall)
                    JournalSecondaryButton(enabled=!busy,onClick={replaceIdentity="name"}) {Text(L.s(R.string.ui010_google_name))}
                    JournalSecondaryButton(enabled=!busy,onClick={replaceIdentity="photo"}) {Text(L.s(R.string.ui010_google_photo))}
                    Text(L.s(R.string.ui010_photo_consent),style=MaterialTheme.typography.bodySmall)
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
            AppearancePreview(settings.theme,settings.typography)
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
            AppearancePreview(settings.theme,settings.typography)
            Text(L.s(R.string.msg_cormorant_garamond_headings_with_inter_text_21da), style = MaterialTheme.typography.bodySmall)
        } }
        item { CustomizePanel(profile) }
            }
            "Language" -> item { Panel {
                SectionTitle(L.s(R.string.msg_language_89b8))
                val current=androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags()
                listOf("" to L.s(R.string.msg_follow_system_1b16),"cs" to L.s(R.string.msg_e_tina_a56c),"en" to L.s(R.string.msg_english_649d)).forEach { (tag,label) ->
                    JournalTextButton(onClick={scope.launch {app.portablePreferences.selectLocale(tag)}}) { RadioButton(current==tag,null);Text(L.fixed(label)) }
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
            // Stage 7A refinement: explicit JournalSpace grouping so the section header,
            // toggle cards and the supplementary hint never touch (20-24 sections /
            // 12-16 groups spacing audit). The online-catalogue preference and every
            // warning are preserved unchanged.
            Column(verticalArrangement=Arrangement.spacedBy(JournalSpace.medium)) {
                SectionTitle(L.s(R.string.downloaded_content))
                SettingsRow(L.s(R.string.mobile_media),L.s(R.string.mobile_media_description),action={scope.launch(kotlinx.coroutines.Dispatchers.IO) {app.media.consent(!metered)}},trailing={Switch(metered,{value->scope.launch(kotlinx.coroutines.Dispatchers.IO) {app.media.consent(value)}},modifier=Modifier.semantics {contentDescription=L.s(R.string.mobile_media)})})
                Text(L.s(R.string.supplementary_hint),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                // Stage 7A (owner): the "Include online catalogue results" preference moved here
                // from Discover; persisted, default OFF. Online searches still only run when the
                // user explicitly starts them in Discover.
                val onlineCatalogue by app.catalogOnline.enabled.collectAsStateWithLifecycle()
                SettingsRow(L.s(R.string.stage7a_online_catalogue_setting),L.s(R.string.stage7a_online_catalogue_setting_detail),action={app.catalogOnline.setEnabled(!onlineCatalogue)},trailing={Switch(onlineCatalogue,{app.catalogOnline.setEnabled(it)},modifier=Modifier.semantics {contentDescription=L.s(R.string.stage7a_online_catalogue_setting)})})
            }
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
            Text(L.s(R.string.backup_v5_note))
            if(restoreState?.status=="pending") {
                Text(L.s(R.string.backup_v5_pending))
                JournalSecondaryButton(onClick=profile::retryRestoreSettings,enabled=!busy){Text(L.s(R.string.backup_v5_retry))}
            }
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
        item { Column(Modifier.fillMaxWidth(),horizontalAlignment=androidx.compose.ui.Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
            val icon by produceState<android.graphics.Bitmap?>(null) {value=kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                context.packageManager.getApplicationIcon(context.packageName).toBitmap(192,192)
            }}
            icon?.let {Image(it.asImageBitmap(),L.s(R.string.goldberg_logo),Modifier.size(136.dp).combinedClickable(onClick={},onLongClick={tux=true}))}
            Text("Goldberg",style=MaterialTheme.typography.headlineLarge)
            JournalTextButton(onClick = {
                val now = android.os.SystemClock.elapsedRealtime()
                taps = if (now-lastTap <= 1500) taps+1 else 1; lastTap=now
                if (taps>=7) {
                    taps=0
                    if (model.timer.value.running) profile.notice.value=L.s(R.string.msg_pause_practice_before_opening_bach_s_secret_9404)
                    else { secret=true; profile.curious() }
                }
            }) { Text(L.s(R.string.ui101_version,remember {context.packageManager.getPackageInfo(context.packageName,0).versionName.orEmpty()})) }
            Text(L.s(R.string.ui010_home_support),style=MaterialTheme.typography.titleLarge,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            BoxWithConstraints {
                val stacked=maxWidth<340.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale>1.15f
                val actions:@Composable ()->Unit={
                    JournalSecondaryButton(onClick=onboarding,modifier=if(stacked) Modifier.fillMaxWidth() else Modifier.width(this.maxWidth/3-6.dp).heightIn(min=64.dp),contentPadding=PaddingValues(8.dp)) {Text(L.s(R.string.msg_revisit_introduction_caf0),textAlign=androidx.compose.ui.text.style.TextAlign.Center)}
                    val uri=androidx.compose.ui.platform.LocalUriHandler.current
                    JournalSecondaryButton(onClick={try {uri.openUri("https://github.com/stokonn/Goldberg")} catch(_:android.content.ActivityNotFoundException) {}},modifier=if(stacked) Modifier.fillMaxWidth() else Modifier.width(this.maxWidth/3-6.dp).heightIn(min=64.dp),contentPadding=PaddingValues(8.dp)) {Text("GitHub")}
                    JournalSecondaryButton(onClick={legal=true},modifier=if(stacked) Modifier.fillMaxWidth() else Modifier.width(this.maxWidth/3-6.dp).heightIn(min=64.dp),contentPadding=PaddingValues(8.dp)) {Text(L.s(R.string.msg_privacy_licenses_2fee),textAlign=androidx.compose.ui.text.style.TextAlign.Center)}
                }
                if(stacked) Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {actions()}
                else Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {actions()}
            }
            Text("created by stokonn with love <3",style=MaterialTheme.typography.bodySmall)

        } }            }
        }
    }
    if (tux) TuxScreen {tux=false}
    if (legal) LegalScreen { legal=false }
    if (secret) HarpsichordScreen { secret=false }
    preview?.let { summary ->
        val timer by model.timer.collectAsStateWithLifecycle()
        AlertDialog(onDismissRequest = profile::cancelRestore, title = { Text(L.s(R.string.msg_replace_this_device_s_journal_ff1d)) }, text = { Text(L.s(R.string.msg_summary_n_nall_current_saved_data_will_be_re_4d42, (summary), (if (timer.elapsed > 0 || timer.running) L.fixed(" You have an unfinished session.") else ""))) },
            confirmButton = { JournalTextButton(enabled = !busy && !timer.saving, onClick = { profile.restore() }) { Text(L.s(R.string.msg_replace_restore_4066)) } },
            dismissButton = { JournalTextButton(enabled = !busy, onClick = profile::cancelRestore) { Text(L.s(R.string.msg_cancel_77df)) } })
    }
}
