package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.opus.piano.OpusApplication
import com.opus.piano.preferences.*

@Composable internal fun CustomizePanel(profile:ProfileViewModel) {
    val a=LocalAppearance.current
    val store=(LocalContext.current.applicationContext as OpusApplication).appearance
    var reset by remember { mutableStateOf(false) }
    JournalCard {
        CompactSection(L.s(R.string.ui010_color_and_surfaces))
        Choice(L.s(R.string.msg_accent_color_e495),AppearanceRules.accents,a.accent,modifier=Modifier.fillMaxWidth()) { value->store.edit { it.copy(accent=value) } }
        Surface(color=MaterialTheme.colorScheme.primary,contentColor=MaterialTheme.colorScheme.onPrimary,shape=MaterialTheme.shapes.small) {Text(L.s(R.string.msg_start_practice_ae1b),Modifier.padding(12.dp))}
        Choice(L.s(R.string.msg_card_style_f910),listOf("Soft","Outline","Open"),a.cards,modifier=Modifier.fillMaxWidth()) { value->store.edit { it.copy(cards=value) } }
        AppearancePreview()
    }
    JournalCard {
        CompactSection(L.s(R.string.ui010_repertoire_display))
        Choice(L.s(R.string.msg_density_f916),listOf("Compact","Comfortable"),a.density,modifier=Modifier.fillMaxWidth()) { value->store.edit { it.copy(density=value) } }
        Choice(L.s(R.string.msg_sort_adc4),AppearanceRules.sorts,a.sort,modifier=Modifier.fillMaxWidth()) { value->store.edit { it.copy(sort=value) } }
        AppearancePreview(listPreview=true)
    }
    JournalSecondaryButton(onClick={reset=true}) { Text(L.s(R.string.msg_reset_appearance_6a59)) }
    if(reset) AlertDialog(onDismissRequest={reset=false},title={Text(L.s(R.string.msg_reset_appearance_fa24))},text={Text(L.s(R.string.msg_reset_visual_preferences_only_your_profile_p_cec0))},confirmButton={JournalTextButton(onClick={profile.resetAppearance();reset=false}) { Text(L.s(R.string.msg_reset_44c5)) }},dismissButton={JournalTextButton(onClick={reset=false}) { Text(L.s(R.string.msg_cancel_77df)) }})
}
private fun sectionLabel(id:String)=when(id) { "statistics"->"Statistics";"recent"->"Recently practiced";"activity"->"Practice activity";"badges"->"Badge showcase";"frequent"->"Most practiced";else->"Session history" }
@Composable private fun SectionEditor(order:List<String>,hidden:Set<String>,toggle:(String)->Unit,move:(String,Int)->Unit) {
    order.forEachIndexed { i,id ->
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
            Switch(id !in hidden,{toggle(id)},modifier=Modifier.semantics { contentDescription=L.fixed(sectionLabel(id)) })
            Text(L.fixed(sectionLabel(id)),Modifier.weight(1f).padding(horizontal=8.dp))
            JournalTextButton(onClick={move(id,-1)},enabled=i>0) { Text(L.s(R.string.msg_up_2038)) }
            JournalTextButton(onClick={move(id,1)},enabled=i<order.lastIndex) { Text(L.s(R.string.msg_down_bf93)) }
        }
    }
}

/** Uses the same theme, card and spacing tokens as the selected preferences. */
@Composable internal fun AppearancePreview(theme:String?=null,typography:String?=null,listPreview:Boolean=false) {
    val a=LocalAppearance.current
    val preview:@Composable ()->Unit={
        JournalCard(padding=PaddingValues(10.dp)) {
            Text(L.s(R.string.ui101_preview),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
            val examples=when(a.sort) {
                "Composer"->listOf("Prelude" to "J. S. Bach","Arabesque" to "Claude Debussy")
                "Recently added"->listOf("Prelude" to "J. S. Bach","Arabesque" to "Claude Debussy")
                else->listOf("Arabesque" to "Claude Debussy","Prelude" to "J. S. Bach")
            }
            examples.take(if(listPreview) 2 else 1).forEach {(title,composer)->
                Row(Modifier.fillMaxWidth().padding(vertical=if(a.density=="Compact") 2.dp else 8.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("♫",color=MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {Text(title,style=MaterialTheme.typography.titleLarge);Text(composer,style=MaterialTheme.typography.bodySmall)}
                }
            }
        }
    }
    if(theme!=null && typography!=null) OpusTheme(theme,typography,a) {preview()} else preview()
}
