# Gould 0.13 screenshot capture evidence

Capture date: 2026-11-01
Source repository: `stokonn/Gould`, PR #45 branch
Capture source: installed published `Gould-v0.13.apk` on disposable Android emulator `emulator-5554`

## APK provenance

- Release: [Gould 0.13 pre-release](https://github.com/stokonn/Gould/releases/tag/v0.13)
- Package: `com.opus.piano`
- Version: `0.13` (`versionCode=15`)
- APK SHA-256: `5325fb6c37c9f8acf977dfbb7d2dd00c8e602cddca3f40384a62d467c739bfbd`
- Published checksum file: `Gould-v0.13.apk.sha256` matched exactly
- APK signature: APK v2, one signer
- Certificate SHA-256: `53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e`
- Installed APK: pulled from the emulator and byte-identical to the published APK

## Emulator and data

- Serial: `emulator-5554`
- `ro.kernel.qemu`: `1`
- Android API: `36`
- Resolution: `1080x2400`
- UI language: English, verified from the rendered application labels
- Data: synthetic fictional classical repertoire only; no account, profile identity,
  photo, journal text, recording, token, notification or private information
- Repertoire verification: the Library hierarchy was inspected at the top and
  after scrolling, and Profile reported `6` repertoire pieces. The six unique
  entries were:
  - `Clair de lune` — Claude Debussy
  - `Goldberg Variations, BWV 988` — Johann Sebastian Bach
  - `Liebestraum No. 3 in A-flat major` — Franz Liszt
  - `Nocturne in E-flat major, Op. 9 No. 2` — Fryderyk Chopin
  - `Prelude in C major, BWV 846` — Johann Sebastian Bach
  - `Sonata in C major, K. 545` — Wolfgang Amadeus Mozart

## Captures

All images are genuine PNG screenshots from the installed APK, 1080x2400.
They were visually inspected for English UI, consistent layout, clean status
bar, synthetic data only, and absence of private information.

| File | Theme | Screen | SHA-256 |
|---|---|---|---|
| `home-en-ivory.png` | Ivory | Home | `01cc27efc81c26d1b00cc4f7cf114049788bf393c3423db0df6175b227f340a1` |
| `repertoire-en-ivory.png` | Ivory | Library / repertoire | `3d6313a28a516a2d3730c02e8e5fd545d29ffcf662b99330ccd0f2e35097bc17` |
| `practice-en-nocturne.png` | Nocturne (Dark) | Studio, ready/non-running state | `7584ae5b28fe7fec8d995f398b554c280b8887e2c09cb2780469fec3ef76fd29` |
| `profile-en-ivory.png` | Ivory | Profile | `9da5a56bcb8ce6f34579ba6863efea99af3f3c25069714deaf4836adc8f0e75e` |
| `achievements-en-ivory.png` | Ivory | Badges | `b6d04d6f259cc9c5a9c8a26c2c42b11899d71777d55b56b15adbc76a6093036b` |
| `achievements-en-nocturne.png` | Nocturne (Dark) | Badges | `1f1506f750482e4b249d201a5d2fe8b78b6638d93a145fbd9d36acd5c91f01a3` |

No saved take was reproducibly available, so `studio-previous-en-nocturne.png`
was not fabricated or added.

created by stokonn with love <3
