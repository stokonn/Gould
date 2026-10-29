package com.opus.piano.audio

/** Only advancing decoded playback bounded by monotonic real time counts. Seek
 * jumps, pause time, preparation and clicks never count as listening evidence. */
class PlaybackEvidence {
    var playedMillis = 0L
        private set
    private var position = 0L
    private var at = 0L
    fun boundary(position: Long, now: Long) { this.position = position; at = now }
    fun advance(position: Long, now: Long) {
        val elapsed = (now - at).coerceAtLeast(0)
        val delta = position - this.position
        if (delta in 1..(elapsed + 100)) playedMillis = Math.addExact(playedMillis, delta)
        boundary(position, now)
    }
}
