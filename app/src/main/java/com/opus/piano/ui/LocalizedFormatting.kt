package com.opus.piano.ui

import com.opus.piano.R
import com.opus.piano.i18n.L
import com.opus.piano.data.SessionPart

internal fun localizedPracticeTime(millis:Long):String {
    val minutes=millis/60000
    return when {
        minutes>=60 -> L.s(R.string.hours_minutes_short,minutes/60,minutes%60)
        minutes>0 -> L.s(R.string.minute_short,minutes)
        else -> L.s(R.string.second_short,millis/1000)
    }
}
internal val SessionPart.displayTitle get()=if(pieceId==null && catalogWorkId==null && title=="Free practice") L.s(R.string.msg_free_practice_524f) else title
