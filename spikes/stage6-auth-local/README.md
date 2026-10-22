# Stage 6 local authentication/session/authorization prototype

Owner-authorized isolated local-only prototype; base schema branch 0f966dc.
Formal 6.2/6.3 and 6.0f remain unchecked; no cloud, real identities, production
credentials, OAuth, deployment or Stage 5/7 work is authorized.

- [x] Recover latest remote/schema/evidence and create dedicated branch.
- [x] Implement strict JOSE identity verification with bounded injected JWKS.
- [x] Implement durable principal/session/challenge/refresh storage and rotation.
- [x] Implement reusable server-side capability and resource authorization.
- [x] Execute local security tests (50/50) and original schema regression suite (34/34).
- [ ] Verify/checkpoint/commit/push and post existing Issue #31 milestone.

The original schema code, checksum ledger and evidence remain unmodified. New
schema changes are a separate local Auth migration, never a hosted migration.
Approved revision-state API remains unchanged; canonical changes_requested and
local drafts are already supported by the schema. See the existing compatibility
report; this discrepancy does not block independent Auth/AuthZ implementation.

created by stokonn with love <3

## Implemented components

- `identity.mjs`: jose 6.2.13 signature/JWKS verification with exact configured
  issuer/audience, explicit RS256/ES256 algorithm allowlist, required subject/nonce/
  issued/expiry claims, bounded credential age and ≤60s configured skew. Token
  headers cannot choose a JWKS URL/key. Trusted injected provider returns public
  keys plus cache max-age; bounded cache, single-flight fetch and unknown-kid
  refetch cooldown, hard timeout, no stale-key fallback after expiry/failure.
- `principals.mjs`: transactional issuer+subject identity resolution; no automatic
  role grant and no email/photo/name storage or authorization from JWT roles.
- `sessions.mjs`: CSPRNG 256-bit opaque access/refresh/challenge secrets; only
  domain-separated SHA-256 representations persist. This is high-entropy bearer
  token protection, not a password hashing scheme. Single-use nonce/challenge,
  durable families, access expiry, rotation, logout and family replay revocation.
- `authorization.mjs`: fresh effective server grants and principal/session state
  in each protected transaction; explicit capabilities; default deny; ownership
  in resource queries; existence hiding; actor≠author; immediate revocation;
  admin-controlled grants/suspension with no self-grant or operator-role elevation.
  The conservative local administrative wrapper also refuses self-revocation and
  self-suspension; those extra restrictions require endpoint-policy review before
  formal adoption. Roles are additive; staff receive contributor permissions only if that separate
  role is actually granted. Unknown/error/empty grants never confer privileges.
- `storage.mjs` / `003_auth.sql`: additive Auth-family/challenge/refresh tables,
  own checksum ledger (original 001/002 ledger unchanged), preserving valid seeded
  sessions; incompatible cross-principal families fail atomically. Parameterized
  PG transactions, 2s lock/5s statement waits, rollback and safe 503 failures.
- `errors.mjs` / `local-api.mjs`: safe API error envelopes, allowlisted bodies and
  logging metadata. In-process adapter only, no HTTP listener or deploy entry.
  Successful login/refresh returns secrets only to its caller after commit;
  diagnostics never receive JWTs, tokens, claims, subject or SQL exception text.

[Official JWT verification](https://github.com/panva/jose/blob/main/docs/jwt/verify/functions/jwtVerify.md)
and [local JWKS selection](https://github.com/panva/jose/blob/main/docs/jwks/local/functions/createLocalJWKSet.md)
were consulted for the library interface; tests verify the pinned installed version.
No custom signature/cryptographic algorithm or test bypass exists in runtime code.
Fake provider/clock and ephemeral private signing keys live only in test fixtures;
no production credentials, generated private keys or real identity are committed.

## Session/transaction semantics

Lifetimes are explicit constructor configuration, not invented retention policy.
Tests choose access 15min, family 1h and challenge 1min; these are synthetic test
security windows, not approved production configuration. Expired/consumed hashes
are retained; no retention jobs or irreversible account deletion are implemented.
Logout revokes the entire selected family, retains other sign-ins/accounts and is
idempotent for known/unknown well-formed access tokens. Callers must clear their
local secret copies on logout even on failure; no client token store is added here.

Lock order is sorted principal IDs → family → refresh record → audit_head.
Admin operations include actor/target in the same sorted lock set. Mint/rotation,
soft revocation and required audit append commit atomically. A consumed refresh
replay revokes the family and commits the security event before returning 401.
With two concurrent refreshes, one can receive tokens and the loser detects replay;
**the winning tokens are then revoked too**. Lost refresh responses require fresh
login after replay; no unapproved grace window or token reissue cache exists.

Access/refresh identifiers cannot substitute for one another. UUIDs are normalized
before self-action checks. Unavailable/malformed clocks cannot bypass expiry;
clock rollback rejects not-yet-issued sessions. Future-dated role grants are not
effective early. No principals/grants/security state is cached across requests.
The trusted server DB connection and injected configuration remain trust boundaries;
this library is not protection against its schema owner disabling triggers.

The protected callback interfaces are for trusted backend handlers only. A client
cannot submit a callback, actor, capability set, raw query or DB function name.
Revision/decision guard operations authorize access but do not implement 6.4/6.5
submission/moderation handlers. Full payload/URL/license validation remains future
work, not silently satisfied by an ID-only allowlist in this Auth milestone.

## Reproduce

Requires Node 22, rootless Podman and the cached pinned PostgreSQL image used by
the schema prototype. Dependency installation requires npm access once; actual
security/regression tests make no external requests. From repository root:

```bash
npm ci --ignore-scripts --no-audit --no-fund --prefix spikes/stage6-schema-pg
npm ci --ignore-scripts --no-audit --no-fund --prefix spikes/stage6-auth-local
spikes/stage6-auth-local/run-local.sh
```

The runner creates/removes only its own labeled loopback-bound container, uses no
host volume or cloud URL, executes Auth tests then the original 34 tests and saves
new regression output here. It never overwrites the original schema environment,
source/hash manifest or 34-test artifact. See [results](results/SUMMARY.md).

## Boundaries / next milestone

Formal 6.2/6.3 and all gates remain unchecked/open. Real Google/Supabase provider
settings, OAuth/PKCE/browser cookies/CSRF, production RLS/DB roles, HTTP deployment,
rate limiting and operational cleanup/retention are not verified or configured.
A real provider adapter must translate trusted response cache headers and honor the
abort signal; no live JWKS HTTP adapter was needed/executed here. Exact string
matching rejects multi-audience JWTs; any real-provider need for an audience array
requires an explicit reviewed policy, never relaxed verification by guesswork.

Next proposed separate local milestone: 6.4 bounded typed submission/revision
handlers and full allowlist/URL/license validation; 6.5 atomic authenticated
moderation using this server context; 6.6 request-bound idempotency/replay/mismatch
and expiry, reusing the existing verified transaction storage. Contract status
patch still needs owner review before endpoint adoption. Do not proceed without
new scope authorization, and do not deploy/promote before the existing gates.
