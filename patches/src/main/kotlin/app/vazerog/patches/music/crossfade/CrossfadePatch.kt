package app.vazerog.patches.music.crossfade

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.vazerog.patches.shared.Constants.COMPATIBILITY_YOUTUBE_MUSIC

private const val MANAGER = "LCrossfadeManager;"

@Suppress("unused")
val crossfadePatch = bytecodePatch(
    name = "Track crossfade",
    description = "Adds a true dual-player crossfade between consecutive tracks. " +
        "Swaps in a new ExoPlayer via YTM's own factory while the old player " +
        "keeps playing, then an equal-power volume curve blends them.",
    default = true,
) {
    dependsOn(vazerOGSettingsResourcePatch)

    compatibleWith(COMPATIBILITY_YOUTUBE_MUSIC)

    extendWith("extensions/extension.rve")

    execute {
        // Hook atad.J(int) — stopVideo.
        //
        // p0 = atad instance (this), p1 = int stoppage reason.
        // Reason 5 = STOPPAGE_DIRECTOR_RESET_INTERNALLY (manual skip-next).
        //
        // Before the original code stops the player, CrossfadeManager
        // saves the old ExoPlayer (keeps playing), creates a new one
        // via atih.a(), and swaps athu.h.  The original flow then
        // loads the next track on the new player.
        StopVideoFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0, p1}, $MANAGER->onBeforeStopVideo(Ljava/lang/Object;I)V
            """
        )

        // Hook athu.y() — playNextInQueue (gapless auto-advance).
        //
        // Same dual-player crossfade but for the case where a track
        // ends naturally and the player auto-advances.
        PlayNextInQueueFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, $MANAGER->onBeforePlayNext(Ljava/lang/Object;)V
            """
        )

        // Hook nba.c() — the audio/video switcher toggle.
        //
        // When crossfade is enabled, block switching TO video mode
        // but allow switching FROM video to audio so the user can
        // exit video mode after enabling crossfade.
        AudioVideoToggleFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0}, $MANAGER->shouldBlockVideoToggle(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :allow_toggle
                return-void
                :allow_toggle
                nop
            """
        )
    }
}
