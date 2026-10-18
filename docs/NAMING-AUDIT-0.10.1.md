# Goldberg naming audit — 0.10.1

The remote is already stokonn/Goldberg and Gradle rootProject.name is Goldberg. Current README, workflow and release scripts use Goldberg branding. Open Opus is a third-party source name and must retain its attribution. Historical release documentation remains historical evidence.

Retained compatibility identifiers: com.opus.piano package/namespace, opus.db filename, OpusDatabase schema directory, manifest component classes, preferences/backup keys and existing signing configuration. Internal OpusApplication/OpusViewModel/OpusTheme names are not user-facing; renaming them adds risk without product benefit. No mass refactor is justified.

Display changes: Repertoire navigation becomes Library/Knihovna; personal collection remains My Repertoire/Můj repertoár. The checkout `/home/stokonn/Projects/Opus` has NOT been renamed because this Codex session uses it. No other path rename was needed after inspection of settings.gradle.kts, scripts, .github, README and Wiki.

After closing all Codex/tmux sessions using the checkout, the owner may run from a separate terminal outside the directory:

```bash
cd "$HOME/Projects"
test -d Opus && test ! -e Goldberg && mv -- Opus Goldberg
# Optional backwards-compatible alias, only when Opus no longer exists:
test -d Goldberg && test ! -e Opus && ln -s -- Goldberg Opus
```

Do not execute the move while tools or shells still use the old working directory. The destination-existence checks avoid overwriting another checkout. No move has been claimed or performed.
