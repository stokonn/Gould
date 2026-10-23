package com.opus.piano.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.opus.piano.R
import com.opus.piano.admin.*
import com.opus.piano.i18n.L
import com.opus.piano.ui.EditorialHeader
import com.opus.piano.ui.JournalSpace
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun AdminAuditScreen(model: AdminViewModel, back: () -> Unit) {
    val service=model.repository.audit
    val state by service.state.collectAsStateWithLifecycle()
    val scope=rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable(model.auditInitialFilter) { mutableStateOf(model.auditInitialFilter) }
    var principal by remember { mutableStateOf("") }
    var authorized by remember { mutableStateOf(false) }
    var roleFailure by remember { mutableStateOf<AdminFailure?>(null) }
    suspend fun enter() {
        authorized=false
        when(val role=model.repository.verifyRole()) {
            is AdminResult.Err -> roleFailure=role.failure
            is AdminResult.Ok -> {
                authorized=role.value.canAuditRead
                principal=role.value.principalId
                roleFailure=if(authorized) null else AdminFailure.Forbidden
                if(authorized) service.load(reset=true)
            }
        }
    }
    AdminOnResume(onResume={enter()},onPause={authorized=false})
    val list=rememberLazyListState()
    Column(Modifier.fillMaxSize().padding(horizontal=JournalSpace.page,vertical=JournalSpace.small)) {
        EditorialHeader(title=L.s(R.string.stage7b_audit_log),back=back)
        if(roleFailure!=null) AdminFailureMessage(roleFailure!!) { scope.launch { enter() } }
        if(authorized) {
            OutlinedTextField(query,{query=it},label={Text(L.s(R.string.stage7b_search))},singleLine=true,modifier=Modifier.fillMaxWidth())
            Text(L.s(R.string.stage7b_audit_scope),style=MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf("all" to R.string.stage7b_all,"mine" to R.string.stage7b_my_actions,"moderation" to R.string.stage7b_moderation).forEach { (key,label) ->
                    FilterChip(filter==key,{filter=key},{Text(L.s(label))})
                }
            }
            val visible=service.filtered(query,filter,principal)
            LazyColumn(Modifier.weight(1f),state=list,verticalArrangement=Arrangement.spacedBy(8.dp)) {
                if(visible.isEmpty() && !state.loading) item {Text(L.s(R.string.stage7b_audit_empty))}
                items(visible,key={it.seq}) { event ->
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("#${event.seq} · ${event.action}",style=MaterialTheme.typography.titleSmall)
                            Text(event.subject)
                            Text("${event.actor} · ${formatAdminTime(event.ts)}",style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if(state.cursor!=null) item { TextButton({scope.launch {service.load()}},enabled=!state.loading) {Text(L.s(R.string.stage7b_load_more))} }
            }
            if(state.loading) AdminLoadingRow(L.s(R.string.stage7b_loading))
            state.failure?.let {AdminFailureMessage(it) {scope.launch {service.load(reset=!state.loaded)}}}
            TextButton({scope.launch {enter()}},enabled=!state.loading) {Text(L.s(R.string.stage7b_refresh))}
        } else if(roleFailure==null) AdminLoadingRow(L.s(R.string.stage7b_loading))
    }
}
