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
    Choice(L.s(R.string.msg_accent_color_e495),AppearanceRules.accents,a.accent) { value->store.edit { it.copy(accent=value) } }
    Choice(L.s(R.string.msg_card_style_f910),listOf("Soft","Outline","Open"),a.cards) { value->store.edit { it.copy(cards=value) } }
    SectionTitle(L.s(R.string.msg_home_layout_e196))
    Text(L.s(R.string.msg_start_practice_stays_pinned_choose_two_diffe_56ab))
    a.metrics.forEachIndexed { i,id -> Choice(if(i==0) L.s(R.string.msg_first_statistic_d223) else L.s(R.string.msg_second_statistic_6575),AppearanceRules.metrics.filter { it==id || it !in a.metrics }.map(::metricLabel),metricLabel(id)) { label ->
        val chosen=AppearanceRules.metrics.first { metricLabel(it)==label };store.edit { it.copy(metrics=it.metrics.toMutableList().apply { this[i]=chosen }) }
    } }
    SectionEditor(a.homeSections,a.hiddenHome,{id->store.edit { it.copy(hiddenHome=if(id in it.hiddenHome) it.hiddenHome-id else it.hiddenHome+id) }},{id,d->store.edit { it.copy(homeSections=AppearanceRules.move(it.homeSections,id,d)) }})
    SectionTitle(L.s(R.string.msg_repertoire_presentation_3f78))
    Choice(L.s(R.string.msg_density_f916),listOf("Compact","Comfortable"),a.density) { value->store.edit { it.copy(density=value) } }
    Choice(L.s(R.string.msg_sort_adc4),AppearanceRules.sorts,a.sort) { value->store.edit { it.copy(sort=value) } }
    SectionTitle(L.s(R.string.msg_profile_layout_5879))
    Text(L.s(R.string.msg_identity_settings_history_and_badge_navigati_abb8))
    SectionEditor(a.profileSections.filter { it != "history" },a.hiddenProfile,{id->store.edit { it.copy(hiddenProfile=if(id in it.hiddenProfile) it.hiddenProfile-id else it.hiddenProfile+id) }},{id,d->store.edit { it.copy(profileSections=AppearanceRules.move(it.profileSections,id,d)) }})
    OutlinedButton(onClick={reset=true}) { Text(L.s(R.string.msg_reset_appearance_6a59)) }
    if(reset) AlertDialog(onDismissRequest={reset=false},title={Text(L.s(R.string.msg_reset_appearance_fa24))},text={Text(L.s(R.string.msg_reset_visual_preferences_only_your_profile_p_cec0))},confirmButton={TextButton(onClick={profile.resetAppearance();reset=false}) { Text(L.s(R.string.msg_reset_44c5)) }},dismissButton={TextButton(onClick={reset=false}) { Text(L.s(R.string.msg_cancel_77df)) }})
}
private fun sectionLabel(id:String)=when(id) { "statistics"->"Statistics";"recent"->"Recently practiced";"activity"->"Practice activity";"badges"->"Badge showcase";"frequent"->"Most practiced";else->"Session history" }
@Composable private fun SectionEditor(order:List<String>,hidden:Set<String>,toggle:(String)->Unit,move:(String,Int)->Unit) {
    order.forEachIndexed { i,id ->
        Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
            Switch(id !in hidden,{toggle(id)},modifier=Modifier.semantics { contentDescription=L.fixed(sectionLabel(id)) })
            Text(L.fixed(sectionLabel(id)),Modifier.weight(1f).padding(horizontal=8.dp))
            TextButton(onClick={move(id,-1)},enabled=i>0) { Text(L.s(R.string.msg_up_2038)) }
            TextButton(onClick={move(id,1)},enabled=i<order.lastIndex) { Text(L.s(R.string.msg_down_bf93)) }
        }
    }
}
