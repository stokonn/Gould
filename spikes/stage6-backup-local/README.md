# Stage 6 local backup and restore verification

This harness is a local, synthetic-only verification of the Stage 6 backup
design. It starts a fresh rootless Podman PostgreSQL 17 container with no
published port, creates only fixture rows, exports a PostgreSQL custom-format
logical dump, encrypts it with a temporary OpenSSL AES-256-CBC key, writes a
SHA-256 manifest, decrypts it into a separate restore path, and restores it
into a fresh database.

The harness also verifies:

- encrypted-artifact integrity and deliberate tamper rejection;
- restored row counts and schema-only dump identity;
- a synthetic SHA-256 audit chain after restore;
- absence of the encryption key and plaintext dump after cleanup;
- container cleanup on success or failure.

It does not connect to Supabase, accept a database URL, export real data,
schedule backups, measure production RPO/RTO, or establish legal/compliance
readiness. The measured durations are local synthetic timings only.

Run from this directory:

```sh
./run-local.sh
```

Set `BACKUP_RESULTS_DIR` to a private directory to preserve the encrypted
artifact, manifest, and machine-readable evidence. The key is never written
there; it remains temporary and is removed on exit.

created by stokonn with love <3
