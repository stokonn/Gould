# Gould 0.12 — owner app icon addendum

**Status:** Approved for inclusion in the Phase 1 architectural review scope; implementation remains subject to Phase 2 approval.

**Source:** Owner-supplied artwork: `docs/design/gould-0.12/references/15_launcher_icon_owner_reference.png` (local-only). It is a visual reference, not a prebuilt Android icon asset.

## Design intention

Replace or refine the app launcher icon to faithfully follow the supplied reference: three soft warm-ivory piano/keyboard white keys, two black keys, restrained warm-gold bordering, clean ivory rounded-square background, subtle depth, uncluttered and premium editorial aesthetics. Preserve the supplied composition and overall proportions; do not replace with unrelated keyboard/harpsichord/logo motifs, or add extra symbols/text. Do not treat exact raster shadows or the image's outer canvas as mandatory Android icon layers.

## Architecture/design review requirements (Phase 1)

- Inspect the current launcher icon, adaptive icon resources, themed icon support, manifest references, branding/name, and icon export pipeline.
- Propose a **faithful production implementation**, ideally separate foreground and background assets suitable for Android adaptive icons, densities, masks and safe area. Include Android 13+ monochrome/themed icon behavior, and sensible pre-adaptive fallback.
- Document legibility at launcher sizes and Android icon masks (circle, squircle, etc.), visual consistency with Ivory and Nocturne, and how the artwork will be built (e.g., vectors or assets with clearly tracked rights).
- Produce a before/after visual acceptance check plan. Do not silently change package name, application ID, signing identity, backups or any compatibility-sensitive resources.
- Keep the owner reference image locally/private; it was deliberately excluded from Git with `.git/info/exclude`. Avoid committing the raster source to public Git history unless owner specifically authorizes that.

## Phase 2 acceptance criteria

- Faithful appearance to owner reference; no cropped keys or ugly aliasing in launcher preview.
- Adaptive light/dark/masked and themed variants verified on emulator(s) across supported Android API levels and density profiles.
- Old installed versions upgrade safely in place; launcher/icon changes do not modify app IDs or persistent data.
- Existing approved project signature `created by stokonn with love <3` remains unchanged.

**Important workflow:** Add this request to the same Gould 0.12 Phase 1 architecture review; **do not restart architecture review, open another development stage, or begin Phase 2** because of this addendum. Phase 1 final report must explicitly cover this icon.
