# Stage 6 local handlers — synthetic PostgreSQL prototype

Owner-authorized 6.4–6.6 local milestone only. No listener, deployment entry point,
real provider credential or network evidence fetch. This directory composes the
unchanged schema and Auth prototypes; it is not formal task acceptance.

```sh
npm ci --ignore-scripts --no-audit --no-fund --prefix spikes/stage6-schema-pg
npm ci --ignore-scripts --no-audit --no-fund --prefix spikes/stage6-auth-local
spikes/stage6-handlers-local/run-local.sh
```

Requires Node 22 and Podman with the same locally cached pinned PG17 image used by
the baseline runners. The runner binds a random loopback-only port, mounts no host
data, uses synthetic DB credentials, and removes its own disposable container on
exit. It writes only this directory's results. No remote database URL is accepted.

Composition: existing `SessionService` → existing `Authorization` →
`SubmissionService` / `ModerationService` → `Idempotency` → existing transaction and
storage helpers. `CommunityApi.invoke()` is an in-process adapter. Actors and roles
come exclusively from the session/active server grants; request objects cannot
supply either. Tests log in through signed synthetic JWTs and the verified session
flow. Fixture SQL seeds roles out of band; reusable handlers have no bypass.

Operations: `submission.create/read/mine/revise/withdraw`,
`moderation.queue/open/decide`. Every body requires `schemaVersion:1`; writes also
require UUID `idempotencyKey`. Creation and moderation use `expectedRevision` for
the entity; revision/withdraw/open/decide additionally require
`expectedSubmissionRevision` for the immutable submission snapshot. Reads are
bounded to 20 items, ordered by timestamp plus UUID, with a microsecond cursor.
Moderation approval returns 201; other decisions/open/withdraw return 200;
create/revise return 201. Exact stored successful responses, including requestId,
are replayed. Retries require a current authorized session even for stored results.

Use the payload shape in DATA-MODEL §2. `Validator` requires trusted injected
field/source/license rules. Empty rules fail closed. Tests supply **only authored
synthetic CC0 fixture evidence** and synthetic plain-text fields; those are not a
new approved real-source or contribution-license policy. The repository does not
yet settle the complete field-level community evidence/intent compatibility rules.
Real rules need that decision before adoption; unknown/missing/NC fixture evidence
is denied now. This slice supports only reviewed bounded plain-text metadata;
more complex catalogue relationship values fail closed. References require an
exact reviewed record, just as URLs do. No blanket Wikipedia or CC license approval.

URLs require canonical HTTPS, an exact reviewed source and host, no credentials,
port, fragment, IP literal, whitespace or backslash. This code **never resolves,
follows redirects or fetches evidence**. DNS/rebinding/redirect fetch validation is
unexecuted and no fetcher/proxy is provided. A future fetch implementation must
resolve/check and pin public addresses, revalidate every redirect and enforce
resource limits before any request. A registered URL is evidence metadata, not
proof of its live contents or license. Free text cannot be semantically guaranteed
free of personal information; there is no structured practice-data field or upload.

External identities retain provider/type namespaces and needs_review mappings.
Supplying a catalogue UUID alongside an unresolved identity does not resolve it or
permit publication. There is no mapping-resolution/merge endpoint in this scope.
No automatic migration of unseen draft/returned records is attempted.

Publication order after session/role locks: principal-scoped idempotency lock →
submission row → advisory entity lock (including absent entities) + entity row →
audit_head. Current revision/status/kind comparisons return 409; arbitrary SQL
errors remain safe 503. Every review transition and publication/feed/idempotency
result commits in the same transaction. Existing immutable triggers protect history.
Expiry is 24 hours, permitting key reuse after expiry while retaining contributions.
The exact `idempotency_results_pkey` collision helper is reused; savepoints roll
back attempted effects if a stored committed response wins that recovery path.

`src/audit.mjs` fixes numeric ordering of an int8 export projection, discovered by
new >9-event cases. Original schema source and results are preserved byte for byte.
The old C6 same-key race `[201,409]` remains historical; the implemented same-key
handler returns `[201,201]` with identical committed response and one effect.
Different-key conflicting decisions still produce `[201,409]`.

No public overlay/change-feed endpoint, quarantine/rollback, full Admin API, quota
engine/kill switch, HTTP/browser/CSRF layer, production RLS, restore/RPO drill or
Android integration. Those remain scheduled work. See results/SUMMARY.md for
executed evidence and limitations. Q1/D5b, C4, C2 §12 and 6.0f remain unresolved.

created by stokonn with love <3
