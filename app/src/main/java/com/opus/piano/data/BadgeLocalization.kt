package com.opus.piano.data

import com.opus.piano.i18n.L
import com.opus.piano.R

private fun AchievementDefinition.localized(index:Int):String {
    val ids=when(id) {
        "curious_pianist" -> intArrayOf(R.string.badge_curious_pianist_name,R.string.badge_curious_pianist_description,R.string.badge_curious_pianist_requirement,R.string.badge_curious_pianist_unit)
        "first_notes" -> intArrayOf(R.string.badge_first_notes_name,R.string.badge_first_notes_description,R.string.badge_first_notes_requirement,R.string.badge_first_notes_unit)
        "dedicated_musician" -> intArrayOf(R.string.badge_dedicated_musician_name,R.string.badge_dedicated_musician_description,R.string.badge_dedicated_musician_requirement,R.string.badge_dedicated_musician_unit)
        "week_streak" -> intArrayOf(R.string.badge_week_streak_name,R.string.badge_week_streak_description,R.string.badge_week_streak_requirement,R.string.badge_week_streak_unit)
        "month_streak" -> intArrayOf(R.string.badge_month_streak_name,R.string.badge_month_streak_description,R.string.badge_month_streak_requirement,R.string.badge_month_streak_unit)
        "century_club" -> intArrayOf(R.string.badge_century_club_name,R.string.badge_century_club_description,R.string.badge_century_club_requirement,R.string.badge_century_club_unit)
        "first_piece" -> intArrayOf(R.string.badge_first_piece_name,R.string.badge_first_piece_description,R.string.badge_first_piece_requirement,R.string.badge_first_piece_unit)
        "growing_collection" -> intArrayOf(R.string.badge_growing_collection_name,R.string.badge_growing_collection_description,R.string.badge_growing_collection_requirement,R.string.badge_growing_collection_unit)
        "dedicated_collector" -> intArrayOf(R.string.badge_dedicated_collector_name,R.string.badge_dedicated_collector_description,R.string.badge_dedicated_collector_requirement,R.string.badge_dedicated_collector_unit)
        "grand_repertoire" -> intArrayOf(R.string.badge_grand_repertoire_name,R.string.badge_grand_repertoire_description,R.string.badge_grand_repertoire_requirement,R.string.badge_grand_repertoire_unit)
        "the_maestro" -> intArrayOf(R.string.badge_the_maestro_name,R.string.badge_the_maestro_description,R.string.badge_the_maestro_requirement,R.string.badge_the_maestro_unit)
        "piano_virtuoso" -> intArrayOf(R.string.badge_piano_virtuoso_name,R.string.badge_piano_virtuoso_description,R.string.badge_piano_virtuoso_requirement,R.string.badge_piano_virtuoso_unit)
        "chopin_enthusiast" -> intArrayOf(R.string.badge_chopin_enthusiast_name,R.string.badge_chopin_enthusiast_description,R.string.badge_chopin_enthusiast_requirement,R.string.badge_chopin_enthusiast_unit)
        "chopin_master" -> intArrayOf(R.string.badge_chopin_master_name,R.string.badge_chopin_master_description,R.string.badge_chopin_master_requirement,R.string.badge_chopin_master_unit)
        "baroque_lover" -> intArrayOf(R.string.badge_baroque_lover_name,R.string.badge_baroque_lover_description,R.string.badge_baroque_lover_requirement,R.string.badge_baroque_lover_unit)
        "bach_explorer" -> intArrayOf(R.string.badge_bach_explorer_name,R.string.badge_bach_explorer_description,R.string.badge_bach_explorer_requirement,R.string.badge_bach_explorer_unit)
        "mozart_admirer" -> intArrayOf(R.string.badge_mozart_admirer_name,R.string.badge_mozart_admirer_description,R.string.badge_mozart_admirer_requirement,R.string.badge_mozart_admirer_unit)
        else -> return listOf(name,description,requirement,unit)[index]
    }
    return L.s(ids[index])
}
val AchievementDefinition.localizedName get()=localized(0)
val AchievementDefinition.localizedDescription get()=localized(1)
val AchievementDefinition.localizedRequirement get()=localized(2)
val AchievementDefinition.localizedUnit get()=localized(3)
val BadgeProgress.localizedProgressText get()=if(definition.metric==AchievementMetric.PRACTICE_MILLIS) L.s(R.string.progress_hours, current/3600000.0, definition.target/3600000) else L.s(R.string.progress_fraction,current,definition.target,definition.localizedUnit)
