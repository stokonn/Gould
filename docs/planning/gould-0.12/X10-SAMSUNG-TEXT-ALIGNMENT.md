# Gould 0.12 — X10 Samsung text alignment follow-up

Owner-authorized deferral, 2026-10-23. This file records one follow-up; it does not
invent a new implementation stage or authorize a speculative typography change.

**Status: DEFERRED — NOT FIXED, NOT VERIFIED, NOT PASS.** Stage 7A for Goldberg
0.11 is COMPLETE by final owner acceptance with a documented visual deviation;
X9 ACCEPTED. Minor cosmetic alignment alone no longer blocks 7A/0.11.

## Original observations and preserved evidence

- Owner-reported physical Samsung feedback after round2: top-biased/inconsistent
  alignment in Settings rows, Discover results, Profile cards and shared components.
- Round3 implementation `d7c0aef` adjusted shared `includeFontPadding=false` and
  CustomizePanel row centering. Those fixes and passing host/emulator checks did
  not establish that the residual Samsung-specific symptom was resolved.
- Owner reported inconsistent Samsung alignment still present after round3.
  Historical acceptance `576f0d2` was conditional; the explicit 2026-10-23 decision
  supersedes that acceptance condition, without changing the observed defect.
- Existing round1–3 screenshots/manifests/logs and [Stage7A checkpoints](../goldberg-0.11/stage-7/stage-7a/CHECKPOINT.md)
  remain intact. No agent performed physical-device verification; emulator renders
  cannot close a reported Samsung-specific rendering difference.

## Potential cause — hypothesis only

Samsung/One UI font substitution, font ascent/descent or leading metrics, and
system/per-app font or display scaling may interact with Compose lineHeight,
baseline alignment and font padding. This is NOT a confirmed root cause. No new
padding, baseline trim or shared-typography change is justified without reproduction
and verified before/after renders. Record actual font/settings rather than assuming
Samsung necessarily substitutes a particular font.

## Future verification requirements (pending)

- [ ] Owner reproduces the symptom and supplies exact affected screens/components,
  Samsung model/OS/One UI, selected font, text/display scale, language and app theme.
- [ ] Preserve owner-provided before screenshots and observations with explicit
  attribution; do not infer checks that the owner did not report.
- [ ] Investigate Compose constraints/baselines/lineHeight/font padding against the
  actual metrics. Confirm or reject the hypothesis before a targeted fix is proposed.
- [ ] Under separately authorized future work, compare before/after renders on the
  reported configuration and permitted emulators, both themes, CZ/EN, normal and
  1.5x/2x fonts, representative sizes. Agents never operate the owner's phone.
- [ ] Check clipping, wrapping, material readability, accessibility labels/order and
  touch targets; preserve regression evidence and explicitly record who ran each check.
- [ ] Claim X10 verified only after specific, attributed evidence supports it.

## Goldberg 0.11 Stage 8 boundary

Stage8 release verification MUST separately check material readability,
accessibility and touch-target defects. The cosmetic deferral waives none of those
checks. No speculative typography fix in0.11, no release/merge/deployment authority.

created by stokonn with love <3
