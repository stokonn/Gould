package com.opus.piano.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.opus.piano.data.*
import com.opus.piano.i18n.L
import com.opus.piano.R

@Composable internal fun ComposerFavoriteButton(composer:CatalogComposer,selected:Boolean,toggle:(String,Boolean)->Unit) {
    IconButton(onClick={toggle(composer.id,!selected)}) {Icon(if(selected) Icons.Outlined.Star else Icons.Outlined.StarBorder,L.s(if(selected) R.string.ui010_unpin else R.string.ui010_pin,composer.name),tint=MaterialTheme.colorScheme.primary)}
}
@Composable internal fun FavoriteComposerStrip(catalog:CatalogSnapshot,favorites:List<FavoriteComposer>,open:(String)->Unit,toggle:(String,Boolean)->Unit) {
    val composers=favorites.mapNotNull {favorite->catalog.composers.find {it.id==favorite.composerId}}
    if(composers.isEmpty()) return
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        CompactSection(L.s(R.string.ui010_favorite_composers))
        LazyRow(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            items(composers,key={it.id}) {composer->
                Surface(onClick={open(composer.id)},shape=MaterialTheme.shapes.medium,color=MaterialTheme.colorScheme.surface) {
                    Column(Modifier.width(134.dp).padding(10.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Row {ComposerPortrait(composer,Modifier.size(62.dp,76.dp));ComposerFavoriteButton(composer,true,toggle)}
                        Text(composer.name,style=MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
