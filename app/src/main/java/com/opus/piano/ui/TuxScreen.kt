package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.opus.piano.i18n.L
import com.opus.piano.R

@Composable internal fun TuxScreen(close:()->Unit) {
    Dialog(onDismissRequest=close) {
        Surface(color=MusicalColors.terminal,contentColor=MusicalColors.terminalInk,shape=MaterialTheme.shapes.large) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text(L.s(R.string.tux_terminal),fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.labelLarge)
                Text("""
       .--.
      |o_o |
      |:_/ |
     //   \ \
    (|     | )
   /'\_   _/`\
   \___)=(___/

  kernel panic: too much Bach
  > rebooting into harmony_
""".trimIndent(),fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodySmall,softWrap=false,modifier=Modifier.horizontalScroll(rememberScrollState()))
                Text("created by stokonn with love <3",fontFamily=FontFamily.Monospace,style=MaterialTheme.typography.bodySmall)
                JournalTextButton(onClick=close,colors=ButtonDefaults.textButtonColors(contentColor=MusicalColors.terminalInk)) {Text(L.s(R.string.msg_close_bbfa))}
            }
        }
    }
}
