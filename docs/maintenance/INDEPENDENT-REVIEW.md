# Gould maintenance independent review

Review target: canonical main after the verified administration outcome at `12746b8`. This is maintenance review only, not Gould 0.12 implementation. Read AGENTS.md, FINAL-ACTIONS-AND-RC-AUDIT.md, FINAL-BRANCH-AUDIT.md, PORTABILITY.md and docs/ai/GOULD-MAIN-MAINTENANCE.md.

## Read-only review brief

Independently review the tracked repository for concrete correctness, portability, compatibility, CI and documentation inconsistencies. Inspect Android/Gradle configuration, backend composition/imports, migrations and backup fixtures, required assets, the three workflow definitions, administrative guard scripts and current handoff. Separate executable dependencies from paths/names in historical receipts. Review changes since the accepted maintenance baseline `bf837f2`; do not interpret unchanged historical code as newly implemented or newly accepted features.

Report findings by severity with file/line, concrete failure scenario, evidence, and a suggested bounded correction. Distinguish verified defects, questions, retained historical evidence and checks that cannot be executed. Review conclusions are not executed build/test evidence. Preserve historical workflow runs, releases/tags, private files, signing identity, com.opus.piano and user-data compatibility. Do not edit files, run shell commands, delegate, publish, modify GitHub, access .pmp/.gradle/.git private records, or implement features. Read tracked source only. Return the report in English.

## Existing OpenCode workflow and current blocker

Installed OpenCode is v2.1.4. The existing owner `architect` agent denies editing, shell/execute and subagent actions. This maintenance does not replace its configuration or auto-approve permissions. OpenCode startup was attempted once as a paired configuration/agent inspection; the local service failed before review with `EPERM: operation not permitted, listen`. No independent review has executed in this sandbox. Do not retry the same restricted service or claim a review passed.

In a normal host terminal, with existing authenticated OpenCode configuration, execute:

```sh
cd "$HOME/Projects/Opus"
mkdir -p .gradle/independent-review
pmp --project "$PWD" launch opencode -- run --agent architect --format json --file docs/maintenance/INDEPENDENT-REVIEW.md 'Perform the attached read-only review. Read tracked source only; no shell, edits, delegation, private records or GitHub mutations. Report in English.' > .gradle/independent-review/opencode-review.jsonl 2> .gradle/independent-review/opencode-review.stderr
```

This uses the existing PMP wrapper for lease, observer and exit checkpoint; it does not install experimental native hooks, change account/provider settings or grant review authority to implement findings. If the installed wrapper refuses a lease or source scope, preserve its diagnostic and resolve the specific conflict; do not force-release another session. Return the actual exit result and report paths. A successful process exit alone does not mean all findings are resolved: read and triage its findings before any changes.

PMP reports native runtime certification only for OpenCode v2.0.32. Installed v2.1.4 native hooks remain unverified, so use the documented wrapper. Keep private PMP out of public Git. See the standalone PMP CLI documentation on the host for wrapper limitations.

created by stokonn with love <3
