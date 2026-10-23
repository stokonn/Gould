-- Same synthetic disposable cluster as single-user.sql. Intake stays disabled.
BEGIN;
INSERT INTO proto_stage679_single.abuse_limits(scope,scope_key,window_start,window_end,count) VALUES
 ('coarse_ip',repeat('a',64),'2029-01-01T00:00:00Z','2029-01-01T01:00:00Z',1),
 ('coarse_ip',repeat('b',64),'2030-01-01T23:59:59Z','2030-01-02T00:59:59Z',1);
DELETE FROM proto_stage679_single.abuse_limits WHERE scope='coarse_ip' AND window_end<='2030-01-01T23:59:58Z'::timestamptz;
DO $$ BEGIN
 IF (SELECT enabled FROM proto_stage679_single.intake_control WHERE singleton) THEN RAISE EXCEPTION 'intake must remain disabled'; END IF;
 IF (SELECT count(*) FROM proto_stage679_single.abuse_limits WHERE scope='coarse_ip')<>1 THEN RAISE EXCEPTION 'expiry boundary'; END IF;
 IF NOT EXISTS(SELECT 1 FROM proto_stage679_single.abuse_limits WHERE scope_key=repeat('b',64)) THEN RAISE EXCEPTION 'active counter deleted'; END IF;
 IF (SELECT count FROM proto_stage679_single.abuse_limits WHERE scope='principal')<>2 THEN RAISE EXCEPTION 'principal counter changed'; END IF;
 IF (SELECT count(*) FROM proto_stage679_single.submissions)<>2 OR (SELECT count(*) FROM proto_stage679_single.published_revisions)<>2 OR (SELECT count(*) FROM proto_stage679_single.audit_events)<>8 THEN RAISE EXCEPTION 'evidence changed'; END IF;
END $$;
COMMIT;
SELECT 'PASS: expired coarse hash only removed with intake disabled; active hashes, principal counters and all submission/publication/audit evidence preserved' AS result;
