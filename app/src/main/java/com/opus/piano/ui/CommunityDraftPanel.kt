package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opus.piano.R
import com.opus.piano.i18n.L
import com.opus.piano.data.community.*
import kotlinx.coroutines.launch
import java.util.UUID

/** Settings subpage only: no navigation redesign, fixture success or auth provider. */
@Composable internal fun CommunityDraftPanel(repository: CommunityRepository) {
    val scope=rememberCoroutineScope()
    val context=androidx.compose.ui.platform.LocalContext.current
    var drafts by remember {mutableStateOf<List<CommunityDraft>>(emptyList())}
    var selectedId by rememberSaveable {mutableStateOf<String?>(null)}
    var title by rememberSaveable {mutableStateOf("")}
    var evidence by rememberSaveable {mutableStateOf("")}
    var license by rememberSaveable {mutableStateOf("")}
    var preview by remember {mutableStateOf<String?>(null)}
    var problem by remember {mutableStateOf<CommunityProblem?>(null)}
    var retryAt by remember {mutableStateOf<Long?>(null)}
    var busy by remember {mutableStateOf(false)}
    val selected=drafts.firstOrNull {it.id==selectedId}
    fun select(d: CommunityDraft) {selectedId=d.id;title=d.payload.fields.firstOrNull()?.newValue.orEmpty();evidence=d.payload.fields.firstOrNull()?.evidence?.firstOrNull()?.value.orEmpty();license=d.payload.intendedLicense;preview=null;problem=null}
    LaunchedEffect(repository) {runCatching {repository.drafts()}.onSuccess {drafts=it}.onFailure {problem=CommunityProblem.STORAGE}}
    fun action(block: suspend ()->Unit) {scope.launch {busy=true;try {block()}catch(e: kotlinx.coroutines.CancellationException){throw e}catch(_: Exception){problem=CommunityProblem.STORAGE}finally {busy=false}}}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text(L.s(R.string.community_local_title),style=MaterialTheme.typography.titleLarge)
        Text(L.s(R.string.community_disabled))
        Text(L.s(R.string.community_privacy),style=MaterialTheme.typography.bodySmall)
        JournalSecondaryButton(enabled=!busy,onClick={action {
            val p=ContributionPayload(CatalogueUuid(UUID.randomUUID().toString()),CommunityKind.WORK,listOf(ContributionField("title","",listOf(SourceEvidence("reference","","")))),"")
            val d=repository.create(p);drafts=repository.drafts();select(d)
        }}) {Text(L.s(R.string.community_new))}
        drafts.forEach {d -> JournalTextButton(enabled=!busy,onClick={select(d)}) {Text(d.payload.fields.firstOrNull()?.newValue?.takeIf {it.isNotBlank()}?:L.s(R.string.community_untitled))} }
        selected?.let {d ->
            val editable=d.pending==null&&d.receipt==null
            OutlinedTextField(title,{title=it},label={Text(L.s(R.string.community_title))},enabled=editable&&!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(evidence,{evidence=it},label={Text(L.s(R.string.community_evidence))},enabled=editable&&!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(license,{license=it},label={Text(L.s(R.string.community_license))},enabled=editable&&!busy,modifier=Modifier.fillMaxWidth())
            Text(L.s(R.string.community_target)+" "+(d.payload.target?.value?:d.payload.externalIdentity?.externalId.orEmpty()),style=MaterialTheme.typography.bodySmall)
            Text(L.s(R.string.community_save_hint),style=MaterialTheme.typography.bodySmall)
            JournalSecondaryButton(enabled=editable&&!busy,onClick={action {
                val p=d.payload.copy(fields=listOf(ContributionField("title",title,listOf(SourceEvidence("reference",evidence,license)))),intendedLicense=license)
                val saved=repository.edit(d.id,d.version,p);drafts=repository.drafts();select(saved)
            }}) {Text(L.s(R.string.community_save))}
            JournalTextButton(enabled=!busy,onClick={action {preview=try {repository.preview(d.id)}catch(_: IllegalArgumentException){problem=CommunityProblem.VALIDATION;null}}}) {Text(L.s(R.string.community_preview))}
            preview?.let {Text(it,style=MaterialTheme.typography.bodySmall)}
            JournalPrimaryButton(enabled=!busy&&repository.availability!=CommunityAvailability.DISABLED,onClick={action {
                when(val result=repository.submit(d.id)) {
                    is CommunityAttempt.Failed -> {problem=result.failure.problem;retryAt=result.failure.retryAtMillis}
                    is CommunityAttempt.SyntheticReceipt -> {problem=CommunityProblem.DISABLED} // Never a real publication claim.
                };drafts=repository.drafts()
            }}) {Text(L.s(R.string.community_submit))}
            if(d.pending!=null) {
                Text(L.s(R.string.community_frozen))
                JournalTextButton(enabled=!busy,onClick={action {val copied=repository.copyForNewRequest(d.id);drafts=repository.drafts();select(copied)}}) {Text(L.s(R.string.community_copy))}
            }
        }
        problem?.let {Text(L.s(communityProblemText(it)),color=MaterialTheme.colorScheme.error)}
        retryAt?.let {Text(L.s(R.string.community_retry_after)+" "+(android.text.format.DateFormat.getMediumDateFormat(context).format(java.util.Date(it))+" "+android.text.format.DateFormat.getTimeFormat(context).format(java.util.Date(it))))}
        JournalTextButton(enabled=!busy,onClick={action {repository.signOut();selectedId=null;preview=null;drafts=repository.drafts()}}) {Text(L.s(R.string.community_sign_out))}
    }
}
internal fun communityProblemText(p: CommunityProblem)=when(p) {
    CommunityProblem.DISABLED->R.string.community_disabled
    CommunityProblem.OFFLINE->R.string.community_offline
    CommunityProblem.UNAVAILABLE->R.string.community_unavailable
    CommunityProblem.VALIDATION->R.string.community_validation
    CommunityProblem.UNAUTHENTICATED->R.string.community_unauthenticated
    CommunityProblem.FORBIDDEN->R.string.community_forbidden
    CommunityProblem.NOT_FOUND->R.string.community_not_found
    CommunityProblem.CONFLICT->R.string.community_conflict
    CommunityProblem.IDEMPOTENCY_MISMATCH->R.string.community_mismatch
    CommunityProblem.RATE_LIMITED->R.string.community_limited
    CommunityProblem.SERVER->R.string.community_server
    CommunityProblem.STORAGE->R.string.community_storage
    CommunityProblem.FROZEN->R.string.community_frozen
    CommunityProblem.OWNERSHIP->R.string.community_ownership
}
