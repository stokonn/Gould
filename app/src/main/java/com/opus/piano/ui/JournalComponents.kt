package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

/** Natural-height content fits normal phones; scrolling remains an accessibility fallback. */
@Composable internal fun JournalViewport(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=JournalSpace.page,vertical=JournalSpace.small),
        verticalArrangement=Arrangement.spacedBy(JournalSpace.medium),content=content)
}
@Composable internal fun SettingsRow(title: String, subtitle: String="", action: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Surface(onClick=action,color=MaterialTheme.colorScheme.background) {
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).padding(vertical=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) { Text(title,style=MaterialTheme.typography.bodyLarge);if(subtitle.isNotBlank()) Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) }
            trailing()
        }
    }
}
@Composable internal fun JournalEmpty(title: String, description: String, actionLabel: String?=null, action: () -> Unit = {}) {
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) { SectionTitle(title);Text(description,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant);if(actionLabel!=null) TextButton(onClick=action) {Text(actionLabel)} }
}

/** One button system across all destinations, including dialogs and secondary pages. */
@Composable internal fun JournalPrimaryButton(onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,colors:ButtonColors=ButtonDefaults.buttonColors(),contentPadding:PaddingValues=ButtonDefaults.ContentPadding,content:@Composable RowScope.()->Unit) {
    Button(onClick,modifier.heightIn(min=48.dp),enabled,shape=androidx.compose.foundation.shape.RoundedCornerShape(14.dp),colors=colors,contentPadding=contentPadding,content=content)
}
@Composable internal fun JournalSecondaryButton(onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,colors:ButtonColors=ButtonDefaults.outlinedButtonColors(),contentPadding:PaddingValues=ButtonDefaults.ContentPadding,content:@Composable RowScope.()->Unit) {
    OutlinedButton(onClick,modifier.heightIn(min=48.dp),enabled,shape=androidx.compose.foundation.shape.RoundedCornerShape(14.dp),colors=colors,contentPadding=contentPadding,content=content)
}
@Composable internal fun JournalTextButton(onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true,colors:ButtonColors=ButtonDefaults.textButtonColors(),contentPadding:PaddingValues=ButtonDefaults.TextButtonContentPadding,content:@Composable RowScope.()->Unit) {
    TextButton(onClick,modifier.heightIn(min=48.dp),enabled,shape=androidx.compose.foundation.shape.RoundedCornerShape(12.dp),colors=colors,contentPadding=contentPadding,content=content)
}
