package com.opus.piano.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.opus.piano.OpusApplication
import com.opus.piano.data.CatalogComposer
import com.opus.piano.i18n.L
import com.opus.piano.R
import kotlinx.coroutines.CancellationException

@Composable internal fun ComposerPortrait(composer: CatalogComposer?, modifier: Modifier = Modifier.size(width=44.dp,height=54.dp)) {
    val app=LocalContext.current.applicationContext as OpusApplication
    val consent by app.media.cellular.collectAsState()
    var credit by remember(composer?.id) { mutableStateOf<com.opus.piano.data.Portrait?>(null) }
    val bitmap by produceState<android.graphics.Bitmap?>(null,composer?.id,consent) {
        if(composer != null) try {
            val portrait=app.catalogRepository.smallPortrait(composer);credit=portrait
            value=portrait?.let { app.catalogRepository.portrait(it.url) }
        } catch(c:CancellationException) { throw c } catch(_:Exception) { value=null }
    }
    var details by remember { mutableStateOf(false) }
    Surface(onClick={if(credit!=null) details=true},modifier=modifier,shape=androidx.compose.foundation.shape.RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.surfaceVariant) {
        if(bitmap!=null) Image(bitmap!!.asImageBitmap(),composer?.name,contentScale=ContentScale.Crop)
        else Icon(Icons.Outlined.PersonOutline,L.s(R.string.msg_portrait_unavailable_6f4e),Modifier.padding(8.dp))
    }
    if(details) AlertDialog(onDismissRequest={details=false},title={Text(composer?.name.orEmpty())},text={Column { credit?.let { p -> Text(p.artist);Text(p.credit);SourceLink(p.license,p.licenseUrl);SourceLink(L.s(R.string.msg_portrait_source_wikimedia_commons_a86c),p.sourceUrl) } }},confirmButton={JournalTextButton(onClick={details=false}) {Text(L.s(R.string.msg_close_bbfa))}})
}
