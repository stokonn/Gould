# Codex task: Goldberg 0.10.1 — Final Redesign Polish, Compliance Review & Safe Repo Cleanup

You are working on an existing native Kotlin / Jetpack Compose / Material 3 / Room Android app, GitHub repository `stokonn/Goldberg`. The owner approved the UI changes described in `APPROVAL_MATRIX.md` and the corresponding image references in `references/`.

**Authorization:** Implement Goldberg **0.10.1** and follow the established release workflow in `AGENTS.md` after every mandatory gate passes. The owner has approved these specific product changes and safe repository hygiene, not arbitrary new features. Do not seek approval for routine commands/tests. Stop and ask about genuinely high-risk/destructive ambiguities. Work autonomously otherwise.

## 0. Baseline / safety checks
1. Read `AGENTS.md`, `APPROVAL_MATRIX.md`, `README.md` and all local visual references. Treat existing repo data, package ID, signing and full previous release history as protected.
2. Verify the GitHub **Goldberg 0.10** release, actual commit/tag, clean/correct local branch and worktree state. Do not blindly branch from an outdated branch or assume `main` is latest until checked.
3. Create/use `feature/goldberg-0.10.1` based on verified stable v0.10 without discarding any untracked edits. Make a concise task checklist and update project tracking.
4. Preserve existing data, preferences, notes, practice histories, unfinished drafts, badge dates, local identity, images, signature/upgrades and backups. Never use destructive migrations or silently change stored IDs.
5. Exclude the `references/` images from Git staging and public releases: they include a real individual's name and are **local-only** visual aids. Do not commit personal photos/screenshot names, publish them to Wiki, Issues or other public resources. Do not bundle in APK.

## 1. Implement exact owner-approved UI changes
Implement **all of** `APPROVAL_MATRIX.md`. That document is authoritative over any fictional screenshot content. Particular emphasis:
- **Home:** same 0.10 design; remove ONLY Home gear; move upper grouping gently up; slightly enlarge greeting/name/tagline; do not redesign other sections.
- **Repertoire → Library/Knihovna:** one combined catalog/personal collection view; `All` and `My Repertoire` filter (no duplicate tabs); consistent rounding on Search/Sort/Filter; preserve browse, select, add and favorites. Update navigation/text/strings throughout CZ/EN.
- **Practice:** keep main timer screen unchanged; reduce intrusive `Pieces & notes` expanded content; move note addition to an elegant **after-successfully-saved** `Practice completed!` summary. Include `Add pieces played`, `Add note` (to specific piece OR session), `Done`, with history-editing and accounting safety. No pseudo-futuristic neon.
- **Profile:** move short bio below name alongside avatar, centered within text column; remove subtitle `Your musical journey`, raise header a bit, make four stat cards slightly more compact with center-aligned content, enlarge badge showcase with direct selection `+` (max 5). Remove `Practice History` section from Profile and remove duplicate Customize buttons except avatar pencil, but **preserve access to all existing editable features** and Profile Settings gear.
- **About:** center large actual installed app icon, name, actual version; lower action row `Revisit introduction | GitHub | Privacy & licenses`; keep credits and both easter eggs; good CZ/EN responsiveness.
- **Tux:** existing pink ASCII easter egg is deliberately approved; repair only broken alignment/rendering with monospace and preserved spacing. No visual restyling.
- **Appearance:** compact, relevant mini previews for theme/font/accent/card/density/sorting reflecting actual settings; preserve all customization, persistence and fallback.

Do not add new database content/features under 0.10.1. The 0.11 database update is a separate future milestone.

## 2. Technical audit and legal/privacy/documentation fixes
Perform an evidence-based applicability audit, not generic US-web compliance theatre. Scope includes EU/GDPR context as relevant, optional Google sign-in, third-party APIs, microphone handling, data storage, local backup/restore/export, composer portraits, quotes, bundled fonts, licensing, Android permissions, and distribution notices.
- Specifically review `docs/PRIVACY.md` and bundled `app/src/main/assets/legal/privacy.md`: older versions may say "no account" even though optional Google identity exists. Update accurate, consistent notices in English/Czech where applicable and ensure accessible in-app Privacy & licenses.
- Verify permissions and the actual practice audio pipeline. Confirm documented local-only/raw-audio behavior from source; do not claim it without evidence.
- Confirm locally bundled Inter/Cormorant fonts and bundled OFL texts; no web font requests unless code actually has them.
- Review third-party image license, attribution, link and consent behavior, Project Gutenberg quote provenance, Open Opus/Wikimedia requests, data sharing and offline behavior.
- Inspect direct/transitive dependencies for trackers, analytics, session replay, crash reports, telemetry, sensitive input capture. Disable/mask only if such features exist and need correction.
- Evaluate **COPPA and other minors' privacy requirements** as potentially applicable; do not add an age gate without first demonstrating applicability and selecting lawful design/data minimization. An age gate alone is not compliance.
- Marketing unsubscribe / postal address rules apply only if the product sends marketing email; subscription renewal/cancellation disclosure only if it sells subscriptions; DMCA agent registration only if the specific service qualifies for the relevant US safe-harbor regime. Mark inapplicable cases N/A rather than inventing infrastructure.
- Correct verifiable technical or documentation issues. Flag legal judgments/required lawyer review as follow-ups; never claim guaranteed legal compliance.
- Produce `docs/LEGAL-AUDIT-0.10.1.md` with applicability, evidence, severity, affected paths, changes made, remaining issues and manual/external checks. Never publish sensitive personal data, secrets or identifying screenshots.

## 3. Goldberg naming cleanup — SAFE, NO BREAKING IDENTIFIERS
The owner wants consistency in visible names, repo folders and scripts, but **upgrade continuity is non-negotiable**.
- Audit legacy `Opus` branding in directory names, README/wiki, scripts, CI paths, CLI release scripts, Gradle display labels, tool configs, docs and internal symbols. Produce inventory. Rename clearly cosmetic/repository-level names to Goldberg where safe and update **all actual references**, internal links, automation scripts, working directories, tests and documentation atomically. Retain backwards compatibility aliases where needed.
- The physical local checkout may be `~/Projects/Opus`; suggest or safely perform move to `~/Projects/Goldberg` **only as a separately staged operation outside an actively running Codex/tmux working directory**. Do NOT move/delete the CWD of this active agent process or break Termius/tmux. Provide a one-time owner executable migration command/script if moving in-process is unsafe; optionally offer a temporary `~/Projects/Opus` symlink for old scripts. Do not claim a move was done unless verified.
- Preserve **applicationId/package** `com.opus.piano`, Room database filename `opus.db`, any schema identities and existing compatible signing key. These old internal IDs are upgrade-critical and **MUST NOT be renamed** just for branding. Likewise do not rename manifest components, provider authorities, SharedPreferences keys or backup schema fields without verified compatibility/migration. Internal class names can be safely renamed only if refactor scope is justified and all references/tests are updated; avoid mass mechanical renames without value.
- `stokonn/Goldberg` already has the correct repo name. Never try to recreate it under another name.

## 4. Branch cleanup / GitHub hygiene
The remote currently had older version branches `feature/goldberg-0.6` through `feature/goldberg-0.10`. Do not assume they are redundant merely because they are old.
- Enumerate local/remotes, branch heads, upstream state, closed/open PRs, protected default branch and release tags.
- Check exact merge/reachability to main and unique commits; preserve unmerged history and any active feature branch or branch referenced by open PR/workflows.
- After 0.10.1 is fully verified and safely integrated, delete **only** clearly obsolete, fully merged branches with no unique commits/open PRs/ongoing work, preserving annotated release tags and auditability. Do not force-delete, force-push, rewrite, delete main/current development branch or delete branches with ambiguous status. If any doubt, list candidates and ask for owner decision; don't delete them.
- Keep a written table with `retained/deleted/needs review`, reasoning and commit refs in `docs/BRANCH-CLEANUP-0.10.1.md`. Use a clean branch naming convention for future work; only create a future 0.11 branch when actual development is authorized.

## 5. Plan next release (NO implementation)
Document `docs/ROADMAP-0.11-DATABASE.md` as a PROPOSAL to review with owner:
- next version: **Goldberg 0.11 — Database Update**;
- assess current composer and work catalog quality, data sources/licensing, metadata normalization, search, deduplication, local cache and offline strategy;
- preserve every existing custom piece, favorited composer, repertoire record and practice session;
- propose an explicit safe migration/backup compatibility strategy, costs, open decisions, tests, acceptance criteria;
- do not populate new catalog, change schema or start that implementation now; do not invent the owner-approved 0.11 feature list beyond the database-update theme.
- Track as a planned milestone/issue, not as "implemented".

## 6. Quality & release gates
- Version should be **0.10.1** with strictly increasing versionCode and unchanged package/signing identity; check in-place upgrades from 0.10 with seeded data on emulator.
- JVM, Android instrumented tests, backup/migration compatibility, practice accounting/notes/session completion, font size/navigation/repertoire filter, bio editor, badge showcase limits, Tux whitespace, localization CZ/EN, offline/network media behavior; add regression tests for changed behavior.
- Build final signed-compatible APK from verified source. Verify manifest info, SHA-256, matching signer, ZIP/assets, lint, source/artifact identity. Do not test on the owner's physical Android phone; emulator serial explicit on every adb call.
- Capture and inspect real emulator screenshot sets of impacted Home, Library, Practice and completion, Profile, About, Tux, Appearance (dark/light and larger font). Compare to local references and written owner decisions; correct substantive deviations. Keep personal identities out of shared/public screenshots.
- Keep release blocked on mandatory failures/unknown high-risk compatibility. Document non-critical deviations for a future patch.
- Follow AGENTS.md for safe GitHub Issues/Project/Wiki/docs updates, commit, push, verified release APK, checksums and release publication **after all mandatory quality gates pass**, unless a material blocker needs owner decision.
- Report files modified, exact branch and status, renamed paths, deleted/retained branches, tests executed/pass/fail, APK location/hash/signature, release URL, the legal findings and remaining manual checks, plus proposal for 0.11.

Do not claim completion from only a passing Gradle build. Avoid destructive Git shortcuts, blind renames, fabricated legal conclusions or user-data loss.
