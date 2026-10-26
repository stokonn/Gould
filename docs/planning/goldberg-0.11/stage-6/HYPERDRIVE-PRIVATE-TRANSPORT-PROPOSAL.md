# Private custom-CA transport proposal — not deployed

Actual hosted reads failclosed503 while host Node/libpq verify-full succeed.
Credential-free OpenSSL against the exact pooler proves system-root verification
fails19 (Supabase private CA), configured CA succeeds and wrong hostname fails62.
Workers StartTLS exposes no custom CA option; the accepted pg-cloudflare path
cannot enforce the supplied Node `ca` property. A TLS socket-open event is not
proof of an encrypted PostgreSQL response; the strengthened hosted probe returned
no encrypted protocol response. No verification downgrade is acceptable.

Proposed exact change, requiring owner approval because
EXACT-PRIVATE-EXECUTION-PACKET.md explicitly excluded Hyperdrive:

- Upload the existing PUBLIC Supabase Root2021 CA as one account CA certificate.
- Create one new Hyperdrive configuration named goldberg-stage6-catalogue with
  existing exact session-pooler host/port5432/databasepostgres, and only the new
  catalogue-only runtime credential from protected local custody. Never admin.
- Set origin TLS mode **verify-full** and bind that exact uploaded CA ID.
- Disable query caching. Origin soft connection cap5 (API minimum), existing
  database hard limit4 remains unchanged. Bounded serial synthetic window only;
  connection-exhaustion tests prohibited. If incompatible, stop without raising it.
- Use existing pg8.23.1 through one server-only CATALOGUE_DB Hyperdrive binding
  on the existing route-less BFF. Per-request Client/transactions remain; no new
  API/workflow/database architecture, schema or data changes.
- Keep all Worker public/previews disabled, OAuthfalse and community intakefalse.
- Free-only, no subscription/card/trial/paid fallback. Cloudflare documents
  100000queries/day on WorkersFree, excess fails,10configurations/account.
  Verify account access/configuration before creation; stop if paid activation is
  requested. No origin probes/keepalive beyond the synthetic window.
- Existing new runtime login expires2026-10-27T03:00:38Z. Do not reset/extend it.
  At end of test window remove temporary verifier/sessions. Retain only the
  explicitly approved private configuration; no credential values in evidence.

Execution is contingent on approval and account permissions, not claimed ready or
successful merely from documentation. Actual readbacks must establish exact CA,
verify-full/cache-off/origin/role/Free settings, followed by hosted positive/negative
requests and unchanged database/public-route postflight. If it fails, preserve data,
keep API failclosed and report the actual blocker.

Sources checked October26:
- https://developers.cloudflare.com/hyperdrive/configuration/tls-ssl-certificates-for-hyperdrive/
- https://developers.cloudflare.com/hyperdrive/platform/pricing/
- https://developers.cloudflare.com/hyperdrive/platform/limits/
- https://developers.cloudflare.com/hyperdrive/examples/connect-to-postgres/postgres-drivers-and-libraries/node-postgres/
- https://supabase.com/docs/guides/platform/ssl-enforcement
