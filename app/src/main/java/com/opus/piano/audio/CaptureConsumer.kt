package com.opus.piano.audio

/** A take attached during a blocking read cannot claim that already-started
 * frame; a replaced/detached take cannot receive it either. Detection is separate. */
internal fun captureConsumer(before: ((ShortArray,Int)->Unit)?, after: ((ShortArray,Int)->Unit)?): ((ShortArray,Int)->Unit)? =
    before?.takeIf { it === after }
