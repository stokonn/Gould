package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.opus.piano.OpusApplication
import com.opus.piano.R
import com.opus.piano.data.catalogue.*
import com.opus.piano.i18n.L
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Library is a local view of Discover's catalogue, not a second provider search. */
@Composable internal fun SharedLibraryCatalogue(query:String,category:String,open:(String)->Unit,explore:()->Unit) {
    val app=LocalContext.current.applicationContext as OpusApplication
    val scope=rememberCoroutineScope()
    var page by remember(query,category){mutableStateOf<CatalogueSearchPage?>(null)}
    var loading by remember(query,category){mutableStateOf(true)}
    var failed by remember(query,category){mutableStateOf(false)}
    var retry by remember{mutableIntStateOf(0)}
    LaunchedEffect(query,category,retry) {
        loading=true;failed=false
        try {page=app.discover.library(query.take(256),category)}
        catch(e:CancellationException){throw e}
        catch(_:Exception){failed=true}
        finally {loading=false}
    }
    Column(verticalArrangement=Arrangement.spacedBy(JournalSpace.small)) {
        EditorialSectionHeader(L.s(if(category=="artists")R.string.stage7a_catalogue_artists else R.string.stage7a_from_catalogue),L.s(R.string.stage7a_catalogue_not_saved))
        if(loading && page==null)EditorialSkeleton()
        page?.items.orEmpty().forEach {entity->key(entity.id) {
            CatalogueRow(entity,page?.workTypes?.get(entity.id),page?.rowInfo?.get(entity.id)){open(entity.id)}
        }}
        if(failed || page?.generation=="unavailable") {
            Text(L.s(R.string.discover_unavailable),style=MaterialTheme.typography.bodySmall)
            JournalTextButton(onClick={retry++}){Text(L.s(R.string.discover_retry))}
        } else if(!loading && page?.items.isNullOrEmpty())Text(L.s(R.string.discover_empty),style=MaterialTheme.typography.bodySmall)
        page?.nextCursor?.let {cursor->
            JournalSecondaryButton(enabled=!loading,onClick={scope.launch {
                loading=true
                try {
                    val next=app.discover.library(query.take(256),category,cursor)
                    val previous=page
                    page=next.copy(items=(previous?.items.orEmpty()+next.items).distinctBy {it.id},
                        workTypes=previous?.workTypes.orEmpty()+next.workTypes,rowInfo=previous?.rowInfo.orEmpty()+next.rowInfo)
                }catch(e:CancellationException){throw e}
                catch(_:Exception){failed=true}
                finally {loading=false}
            }}){Text(L.s(R.string.discover_more))}
        }
        JournalTextButton(onClick=explore){Text(L.s(R.string.msg_discover_in_catalogue_28f0))}
    }
}
