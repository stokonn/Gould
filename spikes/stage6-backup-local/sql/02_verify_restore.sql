\set ON_ERROR_STOP on
DO $$
DECLARE
  expected_head text;
  previous text := repeat('0', 64);
  row_count integer;
  item record;
BEGIN
  SELECT count(*) INTO row_count FROM stage6_backup_synthetic.metadata;
  IF row_count <> 2 THEN RAISE EXCEPTION 'metadata count mismatch: %', row_count; END IF;
  SELECT count(*) INTO row_count FROM stage6_backup_synthetic.submissions;
  IF row_count <> 2 THEN RAISE EXCEPTION 'submission count mismatch: %', row_count; END IF;
  SELECT count(*) INTO row_count FROM stage6_backup_synthetic.audit_events;
  IF row_count <> 2 THEN RAISE EXCEPTION 'audit count mismatch: %', row_count; END IF;

  FOR item IN SELECT * FROM stage6_backup_synthetic.audit_events ORDER BY seq LOOP
    IF item.prev_hash <> previous THEN
      RAISE EXCEPTION 'audit predecessor mismatch at seq %', item.seq;
    END IF;
    IF item.this_hash <> encode(
      digest(item.seq::text || '|' || item.event_kind || '|' || item.prev_hash, 'sha256'),
      'hex'
    ) THEN
      RAISE EXCEPTION 'audit hash mismatch at seq %', item.seq;
    END IF;
    previous := item.this_hash;
  END LOOP;
  SELECT this_hash INTO expected_head
  FROM stage6_backup_synthetic.audit_events ORDER BY seq DESC LIMIT 1;
  IF expected_head <> previous THEN RAISE EXCEPTION 'audit head mismatch'; END IF;
END $$;
SELECT 'PASS: restored row counts and audit hash chain verified' AS verification;
