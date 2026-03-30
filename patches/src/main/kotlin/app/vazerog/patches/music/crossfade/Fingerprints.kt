package app.vazerog.patches.music.crossfade

import app.morphe.patcher.Fingerprint

/**
 * Targets atad.m15632J(int) — the "stopVideo" entry point.
 *
 * Called every time playback is stopped, including on manual skip-next
 * (with reason 5 = STOPPAGE_DIRECTOR_RESET_INTERNALLY).
 *
 * Unique string references:
 *   "stopVideo"                   — state-lock key
 *   "MedialibPlayer.stopVideo"    — log prefix
 */
internal object StopVideoFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf("stopVideo", "MedialibPlayer.stopVideo"),
)

/**
 * Targets athu.y() — the "play next in queue" entry point (gapless auto-advance).
 *
 * Called when a track ends naturally and the player advances to the next
 * window in the playlist.  NOT called on manual skip-next.
 *
 * Unique string references:
 *   "gapless.seek.next"   — error tag
 *   "playNextInQueue."    — latency metric prefix
 */
internal object PlayNextInQueueFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf("gapless.seek.next", "playNextInQueue."),
)

/**
 * Targets nba.c() — the audio/video switcher toggle handler.
 *
 * Called when the user taps the Song/Video toggle in the player UI.
 * Blocked when crossfade is active because dual-player crossfade
 * is audio-only.
 *
 * Unique string reference:
 *   "Failed to update user last selected audio"
 */
internal object AudioVideoToggleFingerprint : Fingerprint(
    returnType = "V",
    strings = listOf("Failed to update user last selected audio"),
)
