package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import com.opus.piano.i18n.L
import com.opus.piano.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

/** Natural-height content fits normal phones; scrolling remains an accessibility fallback. */
@Composable internal fun JournalViewport(spacing:androidx.compose.ui.unit.Dp=JournalSpace.medium,content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=JournalSpace.page,vertical=JournalSpace.small),
        verticalArrangement=Arrangement.spacedBy(spacing),content=content)
}
@Composable internal fun SettingsRow(title: String, subtitle: String="", action: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Surface(onClick=action,color=MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().heightIn(min=52.dp).padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
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

/** The reference panel treatment, derived entirely from active theme/card preferences. */
@Composable internal fun JournalCard(modifier:Modifier=Modifier,padding:PaddingValues=PaddingValues(14.dp),content:@Composable ColumnScope.()->Unit) {
    val style=LocalAppearance.current.cards
    Surface(modifier.fillMaxWidth(),shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface,
        border=if(style=="Open") null else BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=if(style=="Outline") 1f else .45f)),
        tonalElevation=0.dp,shadowElevation=if(style=="Subtle") 1.dp else 0.dp) {
        Column(Modifier.padding(padding),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
    }
}
@Composable internal fun EditorialHeader(title:String,subtitle:String="",back:(()->Unit)?=null,actions:@Composable RowScope.()->Unit={}) {
    Row(Modifier.fillMaxWidth().padding(top=8.dp,bottom=4.dp),verticalAlignment=Alignment.CenterVertically) {
        if(back!=null) IconButton(onClick=back) {Icon(Icons.Outlined.ArrowBack,L.s(R.string.msg_back_b52b))}
        Column(Modifier.weight(1f)) {Text(title,style=MaterialTheme.typography.headlineMedium);if(subtitle.isNotBlank()) Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        actions()
    }
}
@Composable internal fun CompactSection(title:String,actionLabel:String?=null,action:()->Unit={}) {
    Row(Modifier.fillMaxWidth().heightIn(min=32.dp),verticalAlignment=Alignment.CenterVertically) {
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
        if(actionLabel!=null) JournalTextButton(onClick=action,contentPadding=PaddingValues(horizontal=4.dp,vertical=0.dp)) {Text(actionLabel,style=MaterialTheme.typography.labelMedium);Icon(Icons.Outlined.ChevronRight,null,Modifier.size(16.dp))}
    }
}
