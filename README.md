# VazerOG-YTMusic

Custom [Morphe](https://morphe.software) patches for YouTube Music, adding true dual-player crossfade between tracks.

## Features

- **True crossfade** -- Creates a second ExoPlayer instance via YTM's internal factory, crossfades with an equal-power volume curve, then releases the old player. No silence gap.
- **Configurable duration** -- 1 to 12 seconds, adjustable in the VazerOG settings menu.
- **Works on skip and auto-advance** -- Crossfade triggers on manual next/previous and on natural track endings.
- **Video mode handling** -- Blocks switching to video mode while crossfade is active (audio-only requirement). Switching from video back to audio is always allowed.
- **VazerOG settings UI** -- Dedicated settings page accessible from YT Music settings, with toggle, duration slider, and info card.

## Compatibility

| App | Version |
|-----|---------|
| YouTube Music | 8.44.54 |

## How to use

### Add to Morphe (recommended)

Click here to add these patches to Morphe:

**https://morphe.software/add-source?github=VazerOG/VazerOG-YTMusic**

Or manually add this repository URL as a patch source in Morphe:

```
https://github.com/VazerOG/VazerOG-YTMusic
```

### Build from source

Prerequisites: JDK 17, Android SDK

```bash
git clone https://github.com/VazerOG/VazerOG-YTMusic.git
cd VazerOG-YTMusic
./gradlew :patches:build
```

The `.mpp` patch bundle will be at `patches/build/libs/patches-*.mpp`.

To apply with the Morphe CLI:

```bash
java -jar morphe-cli.jar patch \
  --patches patches/build/libs/patches-1.0.0.mpp \
  youtube-music.apk
```

## License

Licensed under the [GNU General Public License v3.0](LICENSE).

This project uses the Morphe Patches template and is compatible with Morphe. It is not authored by or affiliated with the Morphe project.
