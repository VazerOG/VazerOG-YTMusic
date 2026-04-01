# Changelog

## [1.3.0] - 2026-04-01

### Added
- **Auto-advance crossfade via `playNextInQueue` pipeline** — the monitor now calls `atad.o()` (playNextInQueue) instead of `atad.J(5)` (stopVideo) when a track nears its end. `stopVideo` is pure teardown and never advances the queue or loads the next track; the new player was left at IDLE forever. `playNextInQueue` routes through `mo15738y` → `m15968e`, which advances the `atta` queue and triggers media loading on the swapped player.
- Auto-advance position monitor that polls ExoPlayer at 100ms intervals, triggering crossfade at `fadeDuration` remaining.
- `crossfadeOutPlayer` / `crossfadeInPlayer` tracking in both `onBeforeStopVideo` and `onBeforePlayNext` for proper pause-event handling during crossfades.
- `getAthuFromAtadQuiet` helper for silent monitor polling without traversal logging.
- `callLongMethod` reflection utility for reading `v()` (getCurrentPosition) and `w()` (getDuration).

### Fixed
- Auto-advance crossfade now correctly loads the next track by using YTM's queue advancement pipeline instead of the teardown-only `stopVideo` path.

## [1.2.0] - 2026-03-30

### Added
- Configurable fade curve selection (Equal Power, Ease Out Cubic, Ease Out Quad, Smoothstep).
- Advanced duration mode (millisecond precision, 500–30000ms).
- Separate crossfade controls for manual skip vs auto-advance.
- Session control with configurable long-press duration on shuffle button.
- Video mode toggle blocking with toast notification.
- VazerOG settings UI with fade curve, duration, session control, and credit sections.

### Fixed
- Video black screen when crossfade is session-paused.
- UI play/pause button state sync after crossfade.
- Video toggle state tracking across mode switches.

## [1.1.0] - 2026-03-29

### Added
- Dual-player crossfade with equal-power volume curve.
- Configurable crossfade duration (1–12 seconds).
- Session-scoped pause/resume via shuffle button long-press.
- Video mode detection and toggle blocking.

## [1.0.0] - 2026-03-28

### Added
- Initial release with basic crossfade between tracks.
- Morphe patch bundle for YouTube Music 8.44.54.
