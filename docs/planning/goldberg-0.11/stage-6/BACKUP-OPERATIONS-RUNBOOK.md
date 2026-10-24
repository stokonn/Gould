# Stage 6 zero-cost backup operations runbook

**Status: design only.** This runbook does not provision a scheduler, access
the Supabase project, export production data, or create a cloud storage
destination. It is the operator procedure to review before a separately
authorized production backup activation.

## Operating contract

- Keep the Supabase organization and any Cloudflare account on Free plans:
  **0 CZK/month**, no trial, paid add-on, payment method, automatic upgrade or
  spend-cap workaround.
- Use an owner-controlled private destination outside the repository. Do not
  use GitHub, release assets, public object storage, application logs or a
  provider-managed free bucket as the sole copy.
- Keep recovery keys in separate owner-controlled custody, outside both the
  repository and backup destination. Maintain two independent recovery copies
  before retiring an older key.
- The target RPO is **24 hours or less**. No production RTO is promised until
  an authorized restore drill measures one.
- A failed or stale export puts the optional service into read-only/degraded
  mode. Never delete pending work or audit evidence to recover quota.

## Daily procedure

1. Confirm the owner-controlled destination is mounted/available and has
   sufficient space. Refuse to continue if it is inside the repository or
   lacks private access controls.
2. Create a PostgreSQL logical dump using the approved `supabase db dump`
   procedure (or an explicitly reviewed equivalent). Include schema, data,
   audit-chain rows/head and schema-version metadata; exclude secrets and
   provider credentials.
3. Encrypt the dump with the approved authenticated-encryption procedure.
   Publish only after encryption, keyed integrity validation and the
   SHA-256 manifest succeed. Write to a temporary `.part` name and atomically
   rename the complete artifact.
4. Record only timestamp, artifact identifier, byte count, manifest digest,
   key version and exit status in the private operations log. Never log keys,
   passphrases, connection strings or plaintext rows.
5. Verify the artifact can be read from the destination and that its age is
   below 24 hours. Retain the previous known-good backup until the new one
   passes validation.

The eventual scheduler may be an owner-controlled local `systemd` timer or
another already-authorized free scheduler. It must use a single-instance lock,
bounded timeouts, fail-closed exit handling and an alert path controlled by
the owner. No scheduler is activated by this document.

## Retention and failure handling

- Retain at least the last 7 daily encrypted artifacts and one monthly
  checkpoint, subject to the owner-controlled destination's available space.
  Never rotate away the only known-good artifact or the only copy of a
  recovery key.
- Alert the owner on export failure, manifest mismatch, key-access failure,
  destination-unavailable, age over 24 hours, quota warning, or cleanup
  failure. A failed run must leave no plaintext dump, decrypted dump, `.part`
  artifact or temporary secret.
- On repeated failure, disable optional writes/intake and preserve reads where
  safe. Do not retry indefinitely, silently switch providers, or activate a
  paid service.
- Key rotation creates a new key version, validates a new backup with it, and
  retains the old key until every artifact encrypted with it has expired and
  an isolated restore has passed.

## Quarterly restore drill

1. Copy one encrypted artifact and its manifest into a fresh temporary
   workspace; do not use the production database.
2. Restore into a fresh isolated PostgreSQL environment with synthetic or
   authorized test data only. Verify schema identity, row counts, audit-chain
   continuity and the chain head.
3. Measure export age, restore elapsed time and total recovery time. Record
   failures and cleanup results without claiming those synthetic timings as a
   production RTO.
4. Confirm the old workspace, plaintext, temporary keys, containers and
   credentials are removed. Preserve only non-secret evidence and digests.
5. Record the drill date, artifact/key versions, checks performed, elapsed
   times and limitations in the private operations log.

The completed local PostgreSQL 17.11 drill in
`spikes/stage6-backup-local/` verifies the procedure's synthetic cryptographic
and cleanup controls, but it does not verify provider access, destination
durability, production scheduling, legal readiness or hosted restoration.

## Activation gates

Before any production activation, obtain separate authorization for: a private
destination and disaster-recovery key custody; the scheduler and notification
route; sanitized provider billing/Free-plan evidence; a hosted export and
restore; and an owner-approved measurable RTO. Re-run the C4 day-of matrix and
keep 6.0f open until its independent acceptance evidence exists.

created by stokonn with love <3
