\set ON_ERROR_STOP on
CREATE EXTENSION IF NOT EXISTS pgcrypto;
DROP SCHEMA IF EXISTS stage6_backup_synthetic CASCADE;
CREATE SCHEMA stage6_backup_synthetic;

CREATE TABLE stage6_backup_synthetic.metadata (
  key text PRIMARY KEY,
  value text NOT NULL
);
CREATE TABLE stage6_backup_synthetic.submissions (
  id uuid PRIMARY KEY,
  status text NOT NULL,
  payload jsonb NOT NULL
);
CREATE TABLE stage6_backup_synthetic.audit_events (
  seq bigint PRIMARY KEY,
  event_kind text NOT NULL,
  prev_hash text NOT NULL,
  this_hash text NOT NULL
);

INSERT INTO stage6_backup_synthetic.metadata VALUES
  ('schema_version', 'synthetic-stage6-backup-v1'),
  ('scope', 'synthetic-only-no-personal-data');
INSERT INTO stage6_backup_synthetic.submissions VALUES
  ('10000000-0000-4000-8000-000000000001', 'approved',
   '{"target":"synthetic-work","changeKind":"new_metadata","note":"synthetic-only"}'),
  ('10000000-0000-4000-8000-000000000002', 'published',
   '{"target":"synthetic-composer","changeKind":"correction","note":"synthetic-only"}');

INSERT INTO stage6_backup_synthetic.audit_events
  (seq, event_kind, prev_hash, this_hash)
VALUES
  (1, 'submission.approved', repeat('0', 64),
   encode(digest('1|submission.approved|' || repeat('0', 64), 'sha256'), 'hex')),
  (2, 'submission.published',
   encode(digest('1|submission.approved|' || repeat('0', 64), 'sha256'), 'hex'),
   encode(digest('2|submission.published|' ||
     encode(digest('1|submission.approved|' || repeat('0', 64), 'sha256'), 'hex'),
     'sha256'), 'hex'));
