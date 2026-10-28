package com.opus.piano.data

/** A small, versionable draft; avatar bytes stay in private storage, never in a saved Bundle. */
data class ProfileEditValues(
    val name: String, val artist: String, val piece: String, val era: String,
    val biography: String, val statistics: String,
) {
    fun fields() = arrayListOf(name, artist, piece, era, biography, statistics)
    companion object {
        fun from(settings: ProfileSettings) = ProfileEditValues(settings.username, settings.favoriteArtist,
            settings.favoritePiece, settings.favoriteEra, settings.biography, settings.statistics)
        fun restore(fields: List<String>) = fields.takeIf { it.size == 6 }?.let {
            ProfileEditValues(it[0], it[1], it[2], it[3], it[4], it[5])
        }
    }
}

class ProfileEditConflict : IllegalStateException()

data class ProfileEditDraft(
    val original: ProfileEditValues, val value: ProfileEditValues,
    val originalAvatarHash: String, val avatar: String = "keep",
) {
    val dirty get() = value != original || avatar != "keep"
    fun fields() = ArrayList(original.fields() + value.fields() + listOf(originalAvatarHash, avatar))
    companion object {
        fun restore(fields: List<String>) = fields.takeIf { it.size == 14 }?.let {
            ProfileEditDraft(requireNotNull(ProfileEditValues.restore(it.take(6))),
                requireNotNull(ProfileEditValues.restore(it.subList(6, 12))), it[12], it[13])
        }
    }
}

/** Three-way merge: untouched fields/settings retain newer values; conflicting edits fail closed. */
fun mergeProfileEdit(current: ProfileSettings, original: ProfileEditValues, draft: ProfileEditValues): ProfileSettings {
    fun field(now: String, before: String, after: String, limit: Int): String {
        if (after == before) return now // Preserve historical/manual values, including their exact bytes.
        val value = after.trim().take(limit)
        if (now != before && now != value) throw ProfileEditConflict()
        return value
    }
    val name = field(current.username, original.name, draft.name, 80)
    require(name.isNotBlank())
    return current.copy(username = name,
        favoriteArtist = field(current.favoriteArtist, original.artist, draft.artist, 120),
        favoritePiece = field(current.favoritePiece, original.piece, draft.piece, 240),
        favoriteEra = field(current.favoriteEra, original.era, draft.era, 80),
        biography = field(current.biography, original.biography, draft.biography, 400),
        statistics = if (draft.statistics == original.statistics) current.statistics else
            field(current.statistics, original.statistics, StatisticLayout.normalize(draft.statistics.split(',')).joinToString(","), 100))
}

/** Search both canonical local periods and their localized labels without inventing catalogue entries. */
fun eraSuggestions(periods: List<String>, query: String, label: (String) -> String): List<String> {
    val tokens = catalogText(query).split(' ').filter { it.isNotBlank() }
    return periods.filter { it.isNotBlank() }.distinctBy(::catalogText)
        .filter { period -> tokens.all { token ->
            catalogText(period).contains(token) || catalogText(label(period)).contains(token)
        } }.sortedBy { catalogText(label(it)) }.take(8)
}
