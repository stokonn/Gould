# Stage6 local encrypted archive execution

Implemented in scripts/stage6/backup_store.py. This tool never connects to a
database or chooses a destination/custodian, creates no scheduler/notification,
and never automatically deletes backups. Hosted/durable readiness remains NO-GO.

An operator must explicitly select an owner-only mode700 destination, an external
mode600 recovery-key file and a new isolated output directory. Paths below are
placeholders; no owner destination or secret has been provisioned by this task.

```sh
python3 scripts/stage6/backup_store.py publish --dump /approved/synthetic.dump --destination /approved/archives --key /separate-custody/recovery.key
python3 scripts/stage6/backup_store.py health --destination /approved/archives
python3 scripts/stage6/backup_store.py restore-dump --archive /approved/archives/archive-ID --key /separate-custody/recovery.key --isolated-output /approved/isolated/new.dump
```

The source remains intact. Encryption uses isolated GnuPG AES256, with key input
through file only and no raw subprocess diagnostics. Ciphertext and a minimal
SHA256/size/time manifest are fsynced inside a private directory and published by
one directory rename. Existing archives are never replaced. Restore validates
ciphertext integrity, authenticates decryption, then exclusively links a new
mode600 output; wrong key/corruption never publishes plaintext. Temporary owned
files and the invocation's isolated GnuPG agent are cleaned up.

Health checks detect incomplete/changed/future archives and a latest valid age
above24h; exit1 means unhealthy, exit2 an execution failure. This is an executable
local check for a later approved scheduler/alert target, not a configured alert
or measured operational RPO. The timestamp manifest is operator-controlled local
metadata, not a cryptographic external timestamp. Local fsync/rename semantics
and a single-host test do not establish off-site durability or power-loss recovery.

Retain accepted seven-daily/monthly policy; no retention deletion implemented or
authorized. Once custody and target are approved, bind the schedule/alert owner,
verify storage capacity and separate recovery copy, test isolated recovery before
using real data. Do not restore into the original project. Rotation preserves old
keys for old archives; do not rotate database/signing keys through this tool.

Verification:5 new archive lifecycle tests, and the new actual synthetic PG
export→archive→isolated-dump→fresh-database composition in verify-tls-local.py.
Accepted prior encrypted/audit/key-rotation drills are retained, not rerun.
Fresh database restore proves marker/RLS/policy preservation for this fixture;
whole community schema/audit acceptance comes from its separately pinned existing
backup tests. No real user data or credential enters any of these fixtures.

created by stokonn with love <3

Final timeout hardening: agent teardown runs in finally, including encryption
process timeout.6 lifecycle regressions now PASS; prior5-case evidence preserved.
No custody/durable backup readiness follows from this local failure-path proof.
