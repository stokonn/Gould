package com.opus.piano.ui

import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable internal fun OnboardingScreen(done: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        "Your repertoire" to L.s(R.string.msg_discover_a_classical_work_or_add_a_piece_man_3aa2),
        "Your time at the piano" to L.s(R.string.msg_use_the_manual_timer_or_optional_experimenta_3128),
        "Your musical journal" to L.s(R.string.msg_review_sessions_edit_practice_notes_and_expl_2419)
    )
    Dialog(onDismissRequest = done) { Surface(shape = MaterialTheme.shapes.medium) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(L.s(R.string.msg_welcome_to_goldberg_page_1_3_4211, (page+1)),style = MaterialTheme.typography.labelSmall)
            Heading(L.s(R.string.msg_a_little_practice_0328),L.fixed(pages[page].first),pages[page].second)
            JournalPrimaryButton(onClick = { if (page==2) done() else page++ }) { Text(if (page==2) L.s(R.string.msg_begin_c365) else L.s(R.string.msg_continue_2e02)) }
            JournalTextButton(onClick = done) { Text(L.s(R.string.msg_skip_3da4)) }
        }
    } }
}
