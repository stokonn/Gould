# Stage 6 local backup and restore verification

This harness is a local, synthetic-only verification of the Stage 6 backup
design. It starts fresh rootless Podman PostgreSQL 17 containers with no
published port, creates only fixture rows, exports a PostgreSQL custom-format
logical dump, encrypts it with GnuPG symmetric AES-256 plus MDC integrity
protection, writes a SHA-256 manifest, and restores it into a fresh database
in a separate process.

The harness also verifies:

- authenticated encrypted-artifact integrity and deliberate tamper rejection;
- durable external key custody, wrong-key/missing-key rejection and rotation;
- partial/interrupted artifact rejection and atomic `.part` publication;
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

Set `BACKUP_RESULTS_DIR` to a private backup-destination directory and
`BACKUP_KEY_DIR` to a separate private recovery-key custody directory. Both
must be absolute and outside the repository. Keys are retained only in the
supplied custody directory; never commit it or place it beside the backups.

created by stokonn with love <3
