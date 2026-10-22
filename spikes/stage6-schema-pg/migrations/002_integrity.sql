CREATE FUNCTION __S__.deny_mutation() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN RAISE EXCEPTION 'immutable/append-only record' USING ERRCODE='55000'; END; $$;
CREATE FUNCTION __S__.revision_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
DECLARE prior __S__.submission_revisions; header __S__.submissions; expected bigint; computed text;
BEGIN
 SELECT * INTO header FROM __S__.submissions WHERE id=NEW.submission_id FOR UPDATE;
 SELECT * INTO prior FROM __S__.submission_revisions WHERE submission_id=NEW.submission_id ORDER BY revision DESC LIMIT 1;
 expected:=coalesce(prior.revision,0)+1;
 IF prior.revision IS NOT NULL AND (header.status<>'changes_requested' OR header.current_revision<>prior.revision) THEN
  RAISE EXCEPTION 'new snapshot requires changes_requested' USING ERRCODE='23514'; END IF;
 IF NEW.revision<>expected OR NEW.prev_hash<>coalesce(prior.this_hash,repeat('0',64)) THEN
  RAISE EXCEPTION 'revision chain conflict' USING ERRCODE='23514'; END IF;
 computed:=encode(sha256(convert_to(jsonb_build_array(NEW.submission_id::text,NEW.revision::text,NEW.payload,NEW.prev_hash)::text,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'revision hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;
CREATE TRIGGER revision_chain BEFORE INSERT ON __S__.submission_revisions FOR EACH ROW EXECUTE FUNCTION __S__.revision_guard();
CREATE FUNCTION __S__.revision_pointer_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM __S__.submissions WHERE id=NEW.submission_id AND current_revision>=NEW.revision) THEN
  RAISE EXCEPTION 'revision must advance its header atomically' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END; $$;
CREATE CONSTRAINT TRIGGER revision_pointer AFTER INSERT ON __S__.submission_revisions
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION __S__.revision_pointer_guard();
CREATE FUNCTION __S__.published_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
DECLARE author uuid; kind text; computed text;
BEGIN
 SELECT s.author_id,s.target_kind INTO author,kind FROM __S__.submissions s
 WHERE s.id=NEW.submission_id;
 IF author=NEW.actor_id OR kind<>(SELECT e.kind FROM __S__.published_entities e WHERE e.id=NEW.entity_id) THEN
  RAISE EXCEPTION 'self approval or kind mismatch' USING ERRCODE='23514'; END IF;
 IF NEW.payload IS DISTINCT FROM (SELECT r.payload FROM __S__.submission_revisions r
  WHERE r.submission_id=NEW.submission_id AND r.revision=NEW.submission_revision) THEN
  RAISE EXCEPTION 'publication snapshot mismatch' USING ERRCODE='23514'; END IF;
 IF NEW.source_evidence IS DISTINCT FROM jsonb_path_query_array(NEW.payload,'$.fields[*].evidence[*]') THEN
  RAISE EXCEPTION 'publication evidence mismatch' USING ERRCODE='23514'; END IF;
 computed:=encode(sha256(convert_to(jsonb_build_array(NEW.entity_id::text,NEW.revision::text,NEW.payload,NEW.source_evidence)::text,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'published hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;
CREATE TRIGGER published_snapshot BEFORE INSERT ON __S__.published_revisions FOR EACH ROW EXECUTE FUNCTION __S__.published_guard();
CREATE FUNCTION __S__.submission_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF TG_OP='INSERT' THEN
  IF NEW.status<>'submitted' OR NEW.current_revision<>1 THEN RAISE EXCEPTION 'initial server state' USING ERRCODE='23514'; END IF;
  RETURN NEW;
 END IF;
 IF (NEW.id,NEW.author_id,NEW.target_kind,NEW.catalogue_uuid,NEW.mapping_id,NEW.target_entity_id,NEW.created_at)
 IS DISTINCT FROM (OLD.id,OLD.author_id,OLD.target_kind,OLD.catalogue_uuid,OLD.mapping_id,OLD.target_entity_id,OLD.created_at) THEN
  RAISE EXCEPTION 'submission identity immutable' USING ERRCODE='55000'; END IF;
 IF NEW.status=OLD.status AND NEW.current_revision=OLD.current_revision THEN RETURN NEW; END IF;
 IF NOT ((OLD.status='submitted' AND NEW.status IN ('in_review','withdrawn'))
 OR (OLD.status='in_review' AND NEW.status IN ('approved','changes_requested','rejected','withdrawn'))
 OR (OLD.status='changes_requested' AND NEW.status='in_review')
 OR (OLD.status='approved' AND NEW.status='published')
 OR (OLD.status='published' AND NEW.status IN ('superseded','quarantined'))) THEN
  RAISE EXCEPTION 'undocumented transition' USING ERRCODE='23514'; END IF;
 IF NEW.current_revision<>OLD.current_revision+(CASE WHEN OLD.status='changes_requested' AND NEW.status='in_review' THEN 1 ELSE 0 END) THEN
  RAISE EXCEPTION 'revision advance mismatch' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER submission_state BEFORE INSERT OR UPDATE ON __S__.submissions FOR EACH ROW EXECUTE FUNCTION __S__.submission_guard();
CREATE FUNCTION __S__.transition_evidence() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NEW.status=OLD.status THEN RETURN NULL; END IF;
 IF NOT EXISTS(SELECT 1 FROM __S__.review_events r JOIN __S__.audit_events a ON a.seq=r.audit_seq
  WHERE r.submission_id=NEW.id AND r.submission_revision=NEW.current_revision
    AND r.from_status=OLD.status AND r.to_status=NEW.status AND a.actor_id=r.actor_id
    AND a.target_id=NEW.id AND a.target_kind='submission' AND a.target_revision=NEW.current_revision) THEN
  RAISE EXCEPTION 'transition requires matching review and audit' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END; $$;
CREATE CONSTRAINT TRIGGER transition_evidence AFTER UPDATE ON __S__.submissions
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION __S__.transition_evidence();
CREATE FUNCTION __S__.review_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
DECLARE author uuid;
BEGIN
 SELECT author_id INTO author FROM __S__.submissions WHERE id=NEW.submission_id;
 IF NEW.to_status IN ('approved','rejected','changes_requested') AND NEW.actor_id=author THEN
  RAISE EXCEPTION 'self decision forbidden' USING ERRCODE='23514'; END IF;
 IF NEW.to_status IN ('withdrawn') AND NEW.actor_id<>author THEN
  RAISE EXCEPTION 'withdrawal actor must be author' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER review_actor BEFORE INSERT ON __S__.review_events FOR EACH ROW EXECUTE FUNCTION __S__.review_guard();
-- Hash input is a versioned PostgreSQL-jsonb text array. int8 values are decimal strings.
CREATE FUNCTION __S__.audit_content(e __S__.audit_events) RETURNS text LANGUAGE sql STABLE SET search_path=pg_catalog AS $$
 SELECT jsonb_build_array('goldberg-audit-v1',e.seq::text,e.actor_id::text,e.action,e.target_kind,
 e.target_id::text,e.target_revision::text,e.before_hash,e.after_hash,e.reason,
 to_char(e.created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),e.request_id::text)::text
$$;
CREATE FUNCTION __S__.audit_insert_guard() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE h __S__.audit_head; computed text;
BEGIN
 SELECT * INTO STRICT h FROM __S__.audit_head WHERE singleton FOR UPDATE;
 IF NEW.seq<>h.last_seq+1 OR NEW.prev_hash<>h.last_hash THEN RAISE EXCEPTION 'audit head conflict' USING ERRCODE='23514'; END IF;
 NEW.canonical_content:=__S__.audit_content(NEW);
 computed:=encode(sha256(convert_to(NEW.prev_hash||':'||NEW.canonical_content,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'audit hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;
CREATE FUNCTION __S__.audit_advance() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN UPDATE __S__.audit_head SET last_seq=NEW.seq,last_hash=NEW.this_hash WHERE singleton; RETURN NEW; END; $$;
CREATE FUNCTION __S__.head_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NEW.singleton IS DISTINCT FROM OLD.singleton OR NEW.last_seq<>OLD.last_seq+1 OR NOT EXISTS
  (SELECT 1 FROM __S__.audit_events WHERE seq=NEW.last_seq AND this_hash=NEW.last_hash) THEN
  RAISE EXCEPTION 'head must match appended event' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER head_consistency BEFORE UPDATE ON __S__.audit_head FOR EACH ROW EXECUTE FUNCTION __S__.head_guard();
CREATE TRIGGER audit_chain BEFORE INSERT ON __S__.audit_events FOR EACH ROW EXECUTE FUNCTION __S__.audit_insert_guard();
CREATE TRIGGER audit_head_advance AFTER INSERT ON __S__.audit_events FOR EACH ROW EXECUTE FUNCTION __S__.audit_advance();
CREATE FUNCTION __S__.append_audit(actor uuid, action text, target_kind text, target uuid, revision bigint,
 request uuid, reason text DEFAULT NULL, before_hash text DEFAULT NULL, after_hash text DEFAULT NULL)
 RETURNS bigint LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE h __S__.audit_head; result bigint;
BEGIN
 SELECT * INTO STRICT h FROM __S__.audit_head WHERE singleton FOR UPDATE;
 INSERT INTO __S__.audit_events(seq,prev_hash,actor_id,action,target_kind,target_id,target_revision,request_id,reason,before_hash,after_hash)
 VALUES(h.last_seq+1,h.last_hash,actor,action,target_kind,target,revision,request,reason,before_hash,after_hash) RETURNING seq INTO result;
 RETURN result;
END; $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['submission_revisions','published_revisions','review_events','audit_events','change_feed'] LOOP
  EXECUTE format('CREATE TRIGGER immutable BEFORE UPDATE OR DELETE ON __S__.%I FOR EACH ROW EXECUTE FUNCTION __S__.deny_mutation()',t);
  EXECUTE format('CREATE TRIGGER no_truncate BEFORE TRUNCATE ON __S__.%I FOR EACH STATEMENT EXECUTE FUNCTION __S__.deny_mutation()',t);
 END LOOP;
END; $$;
CREATE TRIGGER preserve_head BEFORE DELETE OR TRUNCATE ON __S__.audit_head FOR EACH STATEMENT EXECUTE FUNCTION __S__.deny_mutation();
CREATE FUNCTION __S__.revocation_only() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF (to_jsonb(NEW)-'revoked_at') IS DISTINCT FROM (to_jsonb(OLD)-'revoked_at')
 OR (OLD.revoked_at IS NOT NULL AND NEW.revoked_at IS DISTINCT FROM OLD.revoked_at) THEN
  RAISE EXCEPTION 'only first soft revocation allowed' USING ERRCODE='55000'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER role_revocation BEFORE UPDATE ON __S__.role_grants FOR EACH ROW EXECUTE FUNCTION __S__.revocation_only();
CREATE TRIGGER session_revocation BEFORE UPDATE ON __S__.sessions FOR EACH ROW EXECUTE FUNCTION __S__.revocation_only();
CREATE TRIGGER role_history BEFORE DELETE OR TRUNCATE ON __S__.role_grants FOR EACH STATEMENT EXECUTE FUNCTION __S__.deny_mutation();
CREATE INDEX submission_queue ON __S__.submissions(status,created_at,id);
CREATE INDEX author_submissions ON __S__.submissions(author_id,created_at,id);
CREATE INDEX audit_requests ON __S__.audit_events(request_id,seq);
CREATE INDEX idempotency_expiry ON __S__.idempotency_results(expires_at);
REVOKE ALL ON ALL TABLES IN SCHEMA __S__ FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA __S__ FROM PUBLIC;
