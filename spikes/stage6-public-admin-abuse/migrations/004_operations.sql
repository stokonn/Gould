-- Local synthetic PostgreSQL 17 only. Historical migrations and payloads preserved.
ALTER TABLE __S__.change_feed ADD COLUMN event_kind text NOT NULL DEFAULT 'publication'
 CHECK(event_kind IN ('publication','supersession','quarantine','rollback'));
CREATE FUNCTION __S__.feed_kind() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NEW.status='quarantined' THEN NEW.event_kind:='quarantine';
 ELSIF NEW.status='superseded' THEN NEW.event_kind:='supersession';
 ELSIF NEW.event_kind='publication' AND EXISTS(SELECT 1 FROM __S__.change_feed WHERE entity_id=NEW.entity_id AND status='published')
 THEN NEW.event_kind:='supersession'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER feed_kind BEFORE INSERT ON __S__.change_feed FOR EACH ROW EXECUTE FUNCTION __S__.feed_kind();
CREATE INDEX feed_entity_sequence ON __S__.change_feed(entity_id,seq DESC);
CREATE INDEX abuse_window_expiry ON __S__.abuse_limits(window_end);
CREATE TABLE __S__.intake_control(singleton boolean PRIMARY KEY DEFAULT true CHECK(singleton), enabled boolean NOT NULL DEFAULT false);
-- Deliberately fail closed until a local operator explicitly enables intake.
INSERT INTO __S__.intake_control VALUES(true,false);
REVOKE ALL ON __S__.intake_control FROM PUBLIC;
REVOKE ALL ON FUNCTION __S__.feed_kind() FROM PUBLIC;
