package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.opus.piano.audio.HarpsichordAudioEngine

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun HarpsichordScreen(close: () -> Unit) {
    val context = LocalContext.current
    val audio = remember { HarpsichordAudioEngine(context) }
    val notice by audio.notice.collectAsState()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(audio,lifecycle) {
        val observer = LifecycleEventObserver { _,event -> if (event == Lifecycle.Event.ON_STOP) { audio.close(); close() } }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); audio.close() }
    }
    val names = listOf(L.s(R.string.note_0),L.s(R.string.note_1),L.s(R.string.note_2),L.s(R.string.note_3),L.s(R.string.note_4),L.s(R.string.note_5),L.s(R.string.note_6),L.s(R.string.note_7),L.s(R.string.note_8),L.s(R.string.note_9),L.s(R.string.note_10),L.s(R.string.note_11),L.s(R.string.note_12))
    Dialog(onDismissRequest = close,properties=androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth=false)) { Surface(modifier=Modifier.fillMaxWidth(),shape = MaterialTheme.shapes.medium) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(12.dp),verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Heading(L.s(R.string.msg_bach_s_secret_ec21),L.s(R.string.msg_a_curious_prelude_20f9),L.s(R.string.msg_a_little_harpsichord_hidden_in_your_journal_fb49))
            Text(L.s(R.string.msg_the_curious_pianist_play_a_note_f25f))
            notice?.let { Text(it) }
            HarpsichordKeyboard(names,audio::note)
            JournalTextButton(onClick = close) { Text(L.s(R.string.msg_close_harpsichord_2db2)) }
        }
    } }
}

/** Eight connected natural keys and a raised 2+3 chromatic grouping; all thirteen notes retained. */
@Composable internal fun HarpsichordKeyboard(names: List<String>, play: (Int) -> Unit) {
    val naturals=listOf(0,2,4,5,7,9,11,12)
    val darks=listOf(1 to 1,3 to 2,6 to 4,8 to 5,10 to 6)
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        Box(Modifier.width(384.dp).height(176.dp)) {
            Row {naturals.forEach {note->KeyboardKey(note,names[note],false,Modifier.width(48.dp).height(176.dp),play)}}
            darks.forEach {(note,boundary)->KeyboardKey(note,names[note],true,Modifier.offset(x=(boundary*48-24).dp).width(48.dp).height(112.dp),play)}
        }
    }
}
@Composable private fun KeyboardKey(note:Int,label:String,dark:Boolean,modifier:Modifier,play:(Int)->Unit) {
    val interactions=remember {androidx.compose.foundation.interaction.MutableInteractionSource()}
    val pressed by interactions.collectIsPressedAsState()
    val color by androidx.compose.animation.animateColorAsState(if(dark) {if(pressed) MusicalColors.accidentalPressed else MusicalColors.accidental} else {if(pressed) MusicalColors.naturalPressed else MusicalColors.natural},label="Piano key")
    Box(modifier.clickable(interactionSource=interactions,indication=null,role=androidx.compose.ui.semantics.Role.Button,onClick={play(note)}).semantics {contentDescription=label},contentAlignment=androidx.compose.ui.Alignment.TopCenter) {
        Surface(Modifier.fillMaxHeight().width(if(dark) 30.dp else 48.dp),color=color,contentColor=if(dark) MusicalColors.accidentalInk else MusicalColors.naturalInk,
            shape=androidx.compose.foundation.shape.RoundedCornerShape(bottomStart=4.dp,bottomEnd=4.dp),border=androidx.compose.foundation.BorderStroke(1.dp,MusicalColors.naturalInk.copy(alpha=.35f)),shadowElevation=if(dark) 4.dp else 0.dp) {
            Box(Modifier.fillMaxSize().padding(bottom=10.dp),contentAlignment=androidx.compose.ui.Alignment.BottomCenter) {Text(if(dark) when(note) {1->"C♯";3->"D♯";6->"F♯";8->"G♯";else->"A♯"} else label,style=MaterialTheme.typography.labelSmall)}
        }
    }
}
