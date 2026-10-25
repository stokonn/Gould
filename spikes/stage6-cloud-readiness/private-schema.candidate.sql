-- REVIEW CANDIDATE ONLY. Not applied remotely. Source bundle SHA256: d39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935
-- Run only after C4/security/owner gates, in a privileged maintenance transaction.
BEGIN;
SET LOCAL lock_timeout = '5s';
SET LOCAL statement_timeout = '30s';
SELECT pg_advisory_xact_lock(hashtextextended('goldberg-private-bootstrap-v1',0));
DO $bootstrap$
DECLARE installed text; t record;
BEGIN
 IF current_setting('server_version_num')::integer NOT BETWEEN 170000 AND 179999 THEN
  RAISE EXCEPTION 'PostgreSQL 17 required'; END IF;
 IF EXISTS (SELECT 1 FROM pg_namespace WHERE nspname='goldberg_private') THEN
  IF to_regclass('goldberg_private.deployment_migrations') IS NULL THEN
   RAISE EXCEPTION 'Unrecognized existing schema; preserve and investigate'; END IF;
  EXECUTE 'SELECT checksum FROM goldberg_private.deployment_migrations WHERE version=1' INTO installed;
  IF installed IS DISTINCT FROM 'd39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935' OR
     (SELECT count(*) FROM goldberg_private.deployment_migrations) <> 1 THEN
   RAISE EXCEPTION 'Deployment history/checksum mismatch'; END IF;
 ELSE
  CREATE SCHEMA goldberg_private;
  CREATE TABLE goldberg_private.deployment_migrations(version integer PRIMARY KEY, checksum text NOT NULL);
  -- spikes/stage6-schema-pg/migrations/001_entities.sql; SHA256 6ff749738f1c1194c213a1237e637d19f0ca2cf080257a27ef98d23d43e5030c
  EXECUTE $migration$-- LOCAL SYNTHETIC PROTOTYPE ONLY. goldberg_private is replaced with a validated schema identifier.
CREATE DOMAIN goldberg_private.sha256_hex AS text CHECK (VALUE ~ '^[0-9a-f]{64}$');
CREATE DOMAIN goldberg_private.submission_state AS text CHECK (VALUE IN
 ('submitted','in_review','changes_requested','approved','published','rejected','withdrawn','superseded','quarantined'));
CREATE TABLE goldberg_private.principals (
 id uuid PRIMARY KEY, iss text NOT NULL CHECK (length(iss)>0), sub text NOT NULL CHECK(length(sub)>0),
 state text NOT NULL DEFAULT 'active' CHECK(state IN ('active','suspended','deleted')),
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), UNIQUE(iss,sub)
);
CREATE TABLE goldberg_private.role_grants (
 id uuid PRIMARY KEY, principal_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 role text NOT NULL CHECK(role IN ('contributor','moderator','administrator')),
 granted_by uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 granted_at timestamptz NOT NULL DEFAULT clock_timestamp(), revoked_at timestamptz,
 CHECK(revoked_at IS NULL OR revoked_at>=granted_at)
);
CREATE UNIQUE INDEX active_role_grant ON goldberg_private.role_grants(principal_id,role) WHERE revoked_at IS NULL;
CREATE TABLE goldberg_private.sessions (
 id uuid PRIMARY KEY, principal_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 family_id uuid NOT NULL, predecessor_id uuid UNIQUE REFERENCES goldberg_private.sessions ON DELETE RESTRICT,
 token_hash goldberg_private.sha256_hex NOT NULL UNIQUE, issued_at timestamptz NOT NULL,
 expires_at timestamptz NOT NULL, revoked_at timestamptz,
 CHECK(predecessor_id IS DISTINCT FROM id), CHECK(expires_at>issued_at), CHECK(revoked_at IS NULL OR revoked_at>=issued_at),
 UNIQUE(id,principal_id,family_id),
 FOREIGN KEY(predecessor_id,principal_id,family_id) REFERENCES goldberg_private.sessions(id,principal_id,family_id)
);
CREATE TABLE goldberg_private.identity_mappings (
 id uuid PRIMARY KEY, provider text NOT NULL CHECK(length(provider)>0),
 provider_entity_type text NOT NULL CHECK(length(provider_entity_type)>0),
 external_id text NOT NULL CHECK(length(external_id)>0), catalogue_uuid uuid,
 state text NOT NULL CHECK(state IN ('needs_review','resolved')),
 CHECK((state='resolved')=(catalogue_uuid IS NOT NULL)), UNIQUE(provider,provider_entity_type,external_id)
);
-- catalogue_uuid is an external Stage 2/4 catalogue anchor, not a personal opus.db FK.
CREATE TABLE goldberg_private.published_entities (
 id uuid PRIMARY KEY, kind text NOT NULL CHECK(length(kind)>0), revision bigint NOT NULL CHECK(revision>0),
 status text NOT NULL CHECK(status IN ('published','superseded','quarantined')),
 reason text, CHECK(status='published' OR nullif(btrim(reason),'') IS NOT NULL), UNIQUE(id,kind)
);
CREATE TABLE goldberg_private.submissions (
 id uuid PRIMARY KEY, author_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 target_kind text NOT NULL CHECK(length(target_kind)>0), catalogue_uuid uuid,
 mapping_id uuid REFERENCES goldberg_private.identity_mappings ON DELETE RESTRICT,
 target_entity_id uuid, expected_revision bigint NOT NULL CHECK(expected_revision>=0),
 status goldberg_private.submission_state NOT NULL DEFAULT 'submitted',
 current_revision bigint NOT NULL CHECK(current_revision>0),
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), updated_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 CHECK(catalogue_uuid IS NOT NULL OR mapping_id IS NOT NULL), CHECK(updated_at>=created_at),
 FOREIGN KEY(target_entity_id,target_kind) REFERENCES goldberg_private.published_entities(id,kind) ON DELETE RESTRICT
);
CREATE TABLE goldberg_private.submission_revisions (
 submission_id uuid NOT NULL REFERENCES goldberg_private.submissions ON DELETE RESTRICT,
 revision bigint NOT NULL CHECK(revision>0), payload jsonb NOT NULL CHECK(jsonb_typeof(payload)='object'),
 prev_hash goldberg_private.sha256_hex NOT NULL, this_hash goldberg_private.sha256_hex NOT NULL,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), PRIMARY KEY(submission_id,revision)
);
ALTER TABLE goldberg_private.submissions ADD CONSTRAINT current_submission_revision
 FOREIGN KEY(id,current_revision) REFERENCES goldberg_private.submission_revisions(submission_id,revision)
 DEFERRABLE INITIALLY DEFERRED;
CREATE TABLE goldberg_private.published_revisions (
 entity_id uuid NOT NULL REFERENCES goldberg_private.published_entities ON DELETE RESTRICT,
 revision bigint NOT NULL CHECK(revision>0), submission_id uuid NOT NULL,
 submission_revision bigint NOT NULL, actor_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 payload jsonb NOT NULL CHECK(jsonb_typeof(payload)='object'), this_hash goldberg_private.sha256_hex NOT NULL,
 source_evidence jsonb NOT NULL CHECK(jsonb_typeof(source_evidence)='array'),
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), PRIMARY KEY(entity_id,revision),
 FOREIGN KEY(submission_id,submission_revision) REFERENCES goldberg_private.submission_revisions ON DELETE RESTRICT,
 UNIQUE(submission_id,submission_revision)
);
ALTER TABLE goldberg_private.published_entities ADD CONSTRAINT current_published_revision
 FOREIGN KEY(id,revision) REFERENCES goldberg_private.published_revisions(entity_id,revision)
 DEFERRABLE INITIALLY DEFERRED;
CREATE TABLE goldberg_private.audit_head (
 singleton boolean PRIMARY KEY DEFAULT true CHECK(singleton), last_seq bigint NOT NULL CHECK(last_seq>=0),
 last_hash goldberg_private.sha256_hex NOT NULL, CHECK(last_seq<>0 OR last_hash=repeat('0',64))
);
INSERT INTO goldberg_private.audit_head(singleton,last_seq,last_hash) VALUES(true,0,repeat('0',64));
CREATE TABLE goldberg_private.audit_events (
 seq bigint PRIMARY KEY CHECK(seq>0), prev_hash goldberg_private.sha256_hex NOT NULL, this_hash goldberg_private.sha256_hex NOT NULL,
 actor_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 action text NOT NULL CHECK(length(action)>0), target_kind text NOT NULL CHECK(length(target_kind)>0),
 target_id uuid NOT NULL, target_revision bigint CHECK(target_revision>0),
 before_hash goldberg_private.sha256_hex, after_hash goldberg_private.sha256_hex, reason text,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), request_id uuid NOT NULL,
 canonical_content text NOT NULL,
 submission_target uuid GENERATED ALWAYS AS (CASE WHEN target_kind='submission' THEN target_id END) STORED,
 entity_target uuid GENERATED ALWAYS AS (CASE WHEN target_kind='entity' THEN target_id END) STORED,
 principal_target uuid GENERATED ALWAYS AS (CASE WHEN target_kind='principal' THEN target_id END) STORED REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 CHECK(target_kind IN ('submission','entity','principal')),
 CHECK((target_kind='principal' AND target_revision IS NULL) OR (target_kind IN ('submission','entity') AND target_revision IS NOT NULL)),
 FOREIGN KEY(submission_target,target_revision) REFERENCES goldberg_private.submission_revisions ON DELETE RESTRICT,
 FOREIGN KEY(entity_target,target_revision) REFERENCES goldberg_private.published_revisions ON DELETE RESTRICT
);
CREATE TABLE goldberg_private.review_events (
 id uuid PRIMARY KEY, submission_id uuid NOT NULL, submission_revision bigint NOT NULL,
 actor_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 from_status goldberg_private.submission_state NOT NULL, to_status goldberg_private.submission_state NOT NULL,
 reason text, audit_seq bigint NOT NULL UNIQUE REFERENCES goldberg_private.audit_events ON DELETE RESTRICT,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(submission_id,submission_revision) REFERENCES goldberg_private.submission_revisions ON DELETE RESTRICT,
 CHECK(from_status<>to_status),
 CHECK(to_status NOT IN ('rejected','changes_requested','superseded','quarantined') OR nullif(btrim(reason),'') IS NOT NULL)
);
CREATE TABLE goldberg_private.change_feed (
 seq bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 entity_id uuid NOT NULL, revision bigint NOT NULL,
 status text NOT NULL CHECK(status IN ('published','superseded','quarantined')),
 reason text, audit_seq bigint NOT NULL REFERENCES goldberg_private.audit_events ON DELETE RESTRICT,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(entity_id,revision) REFERENCES goldberg_private.published_revisions ON DELETE RESTRICT,
 CHECK(status='published' OR nullif(btrim(reason),'') IS NOT NULL)
);
CREATE TABLE goldberg_private.idempotency_results (
 principal_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 key uuid NOT NULL, request_hash goldberg_private.sha256_hex NOT NULL,
 http_status integer NOT NULL CHECK(http_status BETWEEN 200 AND 299), response jsonb NOT NULL,
 created_at timestamptz NOT NULL, expires_at timestamptz NOT NULL,
 PRIMARY KEY(principal_id,key), CHECK(expires_at>=created_at+interval '24 hours')
);
-- Expiry is eligibility for key reuse, not a contribution/audit retention policy. No TTL deletion job.
CREATE TABLE goldberg_private.abuse_limits (
 scope text NOT NULL CHECK(scope IN ('principal','coarse_ip')), scope_key text NOT NULL,
 principal_id uuid REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 window_start timestamptz NOT NULL, window_end timestamptz NOT NULL, count bigint NOT NULL CHECK(count>=0),
 PRIMARY KEY(scope,scope_key,window_start), CHECK(window_end>window_start),
 CHECK((scope='principal' AND principal_id IS NOT NULL AND scope_key=principal_id::text)
    OR (scope='coarse_ip' AND principal_id IS NULL AND scope_key ~ '^[0-9a-f]{64}$'))
);
$migration$;
  -- spikes/stage6-schema-pg/migrations/002_integrity.sql; SHA256 a368bfee1c8dc77159f655f46c3ab09a7c78b1ed218d4fa91c7abb57fb6feb1e
  EXECUTE $migration$CREATE FUNCTION goldberg_private.deny_mutation() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN RAISE EXCEPTION 'immutable/append-only record' USING ERRCODE='55000'; END; $$;
CREATE FUNCTION goldberg_private.revision_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
DECLARE prior goldberg_private.submission_revisions; header goldberg_private.submissions; expected bigint; computed text;
BEGIN
 SELECT * INTO header FROM goldberg_private.submissions WHERE id=NEW.submission_id FOR UPDATE;
 SELECT * INTO prior FROM goldberg_private.submission_revisions WHERE submission_id=NEW.submission_id ORDER BY revision DESC LIMIT 1;
 expected:=coalesce(prior.revision,0)+1;
 IF prior.revision IS NOT NULL AND (header.status<>'changes_requested' OR header.current_revision<>prior.revision) THEN
  RAISE EXCEPTION 'new snapshot requires changes_requested' USING ERRCODE='23514'; END IF;
 IF NEW.revision<>expected OR NEW.prev_hash<>coalesce(prior.this_hash,repeat('0',64)) THEN
  RAISE EXCEPTION 'revision chain conflict' USING ERRCODE='23514'; END IF;
 computed:=encode(sha256(convert_to(jsonb_build_array(NEW.submission_id::text,NEW.revision::text,NEW.payload,NEW.prev_hash)::text,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'revision hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;
CREATE TRIGGER revision_chain BEFORE INSERT ON goldberg_private.submission_revisions FOR EACH ROW EXECUTE FUNCTION goldberg_private.revision_guard();
CREATE FUNCTION goldberg_private.revision_pointer_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM goldberg_private.submissions WHERE id=NEW.submission_id AND current_revision>=NEW.revision) THEN
  RAISE EXCEPTION 'revision must advance its header atomically' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END; $$;
CREATE CONSTRAINT TRIGGER revision_pointer AFTER INSERT ON goldberg_private.submission_revisions
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION goldberg_private.revision_pointer_guard();
CREATE FUNCTION goldberg_private.published_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
DECLARE author uuid; kind text; computed text;
BEGIN
 SELECT s.author_id,s.target_kind INTO author,kind FROM goldberg_private.submissions s
 WHERE s.id=NEW.submission_id;
 IF author=NEW.actor_id OR kind<>(SELECT e.kind FROM goldberg_private.published_entities e WHERE e.id=NEW.entity_id) THEN
  RAISE EXCEPTION 'self approval or kind mismatch' USING ERRCODE='23514'; END IF;
 IF NEW.payload IS DISTINCT FROM (SELECT r.payload FROM goldberg_private.submission_revisions r
  WHERE r.submission_id=NEW.submission_id AND r.revision=NEW.submission_revision) THEN
  RAISE EXCEPTION 'publication snapshot mismatch' USING ERRCODE='23514'; END IF;
 IF NEW.source_evidence IS DISTINCT FROM jsonb_path_query_array(NEW.payload,'$.fields[*].evidence[*]') THEN
  RAISE EXCEPTION 'publication evidence mismatch' USING ERRCODE='23514'; END IF;
 computed:=encode(sha256(convert_to(jsonb_build_array(NEW.entity_id::text,NEW.revision::text,NEW.payload,NEW.source_evidence)::text,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'published hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;
CREATE TRIGGER published_snapshot BEFORE INSERT ON goldberg_private.published_revisions FOR EACH ROW EXECUTE FUNCTION goldberg_private.published_guard();
CREATE FUNCTION goldberg_private.submission_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
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
CREATE TRIGGER submission_state BEFORE INSERT OR UPDATE ON goldberg_private.submissions FOR EACH ROW EXECUTE FUNCTION goldberg_private.submission_guard();
CREATE FUNCTION goldberg_private.transition_evidence() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NEW.status=OLD.status THEN RETURN NULL; END IF;
 IF NOT EXISTS(SELECT 1 FROM goldberg_private.review_events r JOIN goldberg_private.audit_events a ON a.seq=r.audit_seq
  WHERE r.submission_id=NEW.id AND r.submission_revision=NEW.current_revision
    AND r.from_status=OLD.status AND r.to_status=NEW.status AND a.actor_id=r.actor_id
    AND a.target_id=NEW.id AND a.target_kind='submission' AND a.target_revision=NEW.current_revision) THEN
  RAISE EXCEPTION 'transition requires matching review and audit' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END; $$;
CREATE CONSTRAINT TRIGGER transition_evidence AFTER UPDATE ON goldberg_private.submissions
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION goldberg_private.transition_evidence();
CREATE FUNCTION goldberg_private.review_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
DECLARE author uuid;
BEGIN
 SELECT author_id INTO author FROM goldberg_private.submissions WHERE id=NEW.submission_id;
 IF NEW.to_status IN ('approved','rejected','changes_requested') AND NEW.actor_id=author THEN
  RAISE EXCEPTION 'self decision forbidden' USING ERRCODE='23514'; END IF;
 IF NEW.to_status IN ('withdrawn') AND NEW.actor_id<>author THEN
  RAISE EXCEPTION 'withdrawal actor must be author' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER review_actor BEFORE INSERT ON goldberg_private.review_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.review_guard();
-- Hash input is a versioned PostgreSQL-jsonb text array. int8 values are decimal strings.
CREATE FUNCTION goldberg_private.audit_content(e goldberg_private.audit_events) RETURNS text LANGUAGE sql STABLE SET search_path=pg_catalog AS $$
 SELECT jsonb_build_array('goldberg-audit-v1',e.seq::text,e.actor_id::text,e.action,e.target_kind,
 e.target_id::text,e.target_revision::text,e.before_hash,e.after_hash,e.reason,
 to_char(e.created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),e.request_id::text)::text
$$;
CREATE FUNCTION goldberg_private.audit_insert_guard() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE h goldberg_private.audit_head; computed text;
BEGIN
 SELECT * INTO STRICT h FROM goldberg_private.audit_head WHERE singleton FOR UPDATE;
 IF NEW.seq<>h.last_seq+1 OR NEW.prev_hash<>h.last_hash THEN RAISE EXCEPTION 'audit head conflict' USING ERRCODE='23514'; END IF;
 NEW.canonical_content:=goldberg_private.audit_content(NEW);
 computed:=encode(sha256(convert_to(NEW.prev_hash||':'||NEW.canonical_content,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'audit hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;
CREATE FUNCTION goldberg_private.audit_advance() RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN UPDATE goldberg_private.audit_head SET last_seq=NEW.seq,last_hash=NEW.this_hash WHERE singleton; RETURN NEW; END; $$;
CREATE FUNCTION goldberg_private.head_guard() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NEW.singleton IS DISTINCT FROM OLD.singleton OR NEW.last_seq<>OLD.last_seq+1 OR NOT EXISTS
  (SELECT 1 FROM goldberg_private.audit_events WHERE seq=NEW.last_seq AND this_hash=NEW.last_hash) THEN
  RAISE EXCEPTION 'head must match appended event' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER head_consistency BEFORE UPDATE ON goldberg_private.audit_head FOR EACH ROW EXECUTE FUNCTION goldberg_private.head_guard();
CREATE TRIGGER audit_chain BEFORE INSERT ON goldberg_private.audit_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.audit_insert_guard();
CREATE TRIGGER audit_head_advance AFTER INSERT ON goldberg_private.audit_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.audit_advance();
CREATE FUNCTION goldberg_private.append_audit(actor uuid, action text, target_kind text, target uuid, revision bigint,
 request uuid, reason text DEFAULT NULL, before_hash text DEFAULT NULL, after_hash text DEFAULT NULL)
 RETURNS bigint LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE h goldberg_private.audit_head; result bigint;
BEGIN
 SELECT * INTO STRICT h FROM goldberg_private.audit_head WHERE singleton FOR UPDATE;
 INSERT INTO goldberg_private.audit_events(seq,prev_hash,actor_id,action,target_kind,target_id,target_revision,request_id,reason,before_hash,after_hash)
 VALUES(h.last_seq+1,h.last_hash,actor,action,target_kind,target,revision,request,reason,before_hash,after_hash) RETURNING seq INTO result;
 RETURN result;
END; $$;
DO $$ DECLARE t text; BEGIN
 FOREACH t IN ARRAY ARRAY['submission_revisions','published_revisions','review_events','audit_events','change_feed'] LOOP
  EXECUTE format('CREATE TRIGGER immutable BEFORE UPDATE OR DELETE ON goldberg_private.%I FOR EACH ROW EXECUTE FUNCTION goldberg_private.deny_mutation()',t);
  EXECUTE format('CREATE TRIGGER no_truncate BEFORE TRUNCATE ON goldberg_private.%I FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation()',t);
 END LOOP;
END; $$;
CREATE TRIGGER preserve_head BEFORE DELETE OR TRUNCATE ON goldberg_private.audit_head FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();
CREATE FUNCTION goldberg_private.revocation_only() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF (to_jsonb(NEW)-'revoked_at') IS DISTINCT FROM (to_jsonb(OLD)-'revoked_at')
 OR (OLD.revoked_at IS NOT NULL AND NEW.revoked_at IS DISTINCT FROM OLD.revoked_at) THEN
  RAISE EXCEPTION 'only first soft revocation allowed' USING ERRCODE='55000'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER role_revocation BEFORE UPDATE ON goldberg_private.role_grants FOR EACH ROW EXECUTE FUNCTION goldberg_private.revocation_only();
CREATE TRIGGER session_revocation BEFORE UPDATE ON goldberg_private.sessions FOR EACH ROW EXECUTE FUNCTION goldberg_private.revocation_only();
CREATE TRIGGER role_history BEFORE DELETE OR TRUNCATE ON goldberg_private.role_grants FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();
CREATE INDEX submission_queue ON goldberg_private.submissions(status,created_at,id);
CREATE INDEX author_submissions ON goldberg_private.submissions(author_id,created_at,id);
CREATE INDEX audit_requests ON goldberg_private.audit_events(request_id,seq);
CREATE INDEX idempotency_expiry ON goldberg_private.idempotency_results(expires_at);
REVOKE ALL ON ALL TABLES IN SCHEMA goldberg_private FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA goldberg_private FROM PUBLIC;
$migration$;
  -- spikes/stage6-auth-local/migrations/003_auth.sql; SHA256 cccffe54b97e8dc2dcc90fb0f39a10298b857ee5d22375196e35b0746ea2df5e
  EXECUTE $migration$-- Local Auth extension; original 001/002 migrations and ledger stay unchanged.
CREATE TABLE goldberg_private.auth_families (
 id uuid PRIMARY KEY, principal_id uuid NOT NULL REFERENCES goldberg_private.principals ON DELETE RESTRICT,
 created_at timestamptz NOT NULL, expires_at timestamptz NOT NULL, revoked_at timestamptz,
 CHECK(expires_at>created_at), CHECK(revoked_at IS NULL OR revoked_at>=created_at), UNIQUE(id,principal_id)
);
-- Preserve seeded base sessions; incompatible cross-principal family IDs fail atomically.
INSERT INTO goldberg_private.auth_families(id,principal_id,created_at,expires_at,revoked_at)
 SELECT family_id,principal_id,min(issued_at),max(expires_at),NULL FROM goldberg_private.sessions GROUP BY family_id,principal_id;
ALTER TABLE goldberg_private.sessions ADD CONSTRAINT session_auth_family
 FOREIGN KEY(family_id,principal_id) REFERENCES goldberg_private.auth_families(id,principal_id) ON DELETE RESTRICT;
CREATE TABLE goldberg_private.auth_challenges (
 token_hash goldberg_private.sha256_hex PRIMARY KEY, created_at timestamptz NOT NULL,
 expires_at timestamptz NOT NULL, consumed_at timestamptz,
 CHECK(expires_at>created_at), CHECK(consumed_at IS NULL OR consumed_at>=created_at)
);
CREATE TABLE goldberg_private.auth_refresh (
 token_hash goldberg_private.sha256_hex PRIMARY KEY, session_id uuid NOT NULL UNIQUE,
 principal_id uuid NOT NULL, family_id uuid NOT NULL, created_at timestamptz NOT NULL,
 expires_at timestamptz NOT NULL, consumed_at timestamptz,
 CHECK(expires_at>created_at), CHECK(consumed_at IS NULL OR consumed_at>=created_at),
 FOREIGN KEY(session_id,principal_id,family_id) REFERENCES goldberg_private.sessions(id,principal_id,family_id) ON DELETE RESTRICT
);
CREATE INDEX auth_family_sessions ON goldberg_private.sessions(family_id);
CREATE INDEX auth_family_refresh ON goldberg_private.auth_refresh(family_id);
CREATE TRIGGER family_revocation BEFORE UPDATE ON goldberg_private.auth_families FOR EACH ROW EXECUTE FUNCTION goldberg_private.revocation_only();
CREATE FUNCTION goldberg_private.consume_only() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF (to_jsonb(NEW)-'consumed_at') IS DISTINCT FROM (to_jsonb(OLD)-'consumed_at')
 OR (OLD.consumed_at IS NOT NULL AND NEW.consumed_at IS DISTINCT FROM OLD.consumed_at) THEN
  RAISE EXCEPTION 'only first consumption allowed' USING ERRCODE='55000'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER challenge_consumption BEFORE UPDATE ON goldberg_private.auth_challenges FOR EACH ROW EXECUTE FUNCTION goldberg_private.consume_only();
CREATE TRIGGER refresh_consumption BEFORE UPDATE ON goldberg_private.auth_refresh FOR EACH ROW EXECUTE FUNCTION goldberg_private.consume_only();
REVOKE ALL ON ALL TABLES IN SCHEMA goldberg_private FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA goldberg_private FROM PUBLIC;
$migration$;
  -- spikes/stage6-public-admin-abuse/migrations/004_operations.sql; SHA256 d4aef06f7ba1f50e3b0bb822df744b340a12a3a12ef1f4f0673f13c5fd87b6dd
  EXECUTE $migration$-- Local synthetic PostgreSQL 17 only. Historical migrations and payloads preserved.
ALTER TABLE goldberg_private.change_feed ADD COLUMN event_kind text NOT NULL DEFAULT 'publication'
 CHECK(event_kind IN ('publication','supersession','quarantine','rollback'));
CREATE FUNCTION goldberg_private.feed_kind() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF NEW.status='quarantined' THEN NEW.event_kind:='quarantine';
 ELSIF NEW.status='superseded' THEN NEW.event_kind:='supersession';
 ELSIF NEW.event_kind='publication' AND EXISTS(SELECT 1 FROM goldberg_private.change_feed WHERE entity_id=NEW.entity_id AND status='published')
 THEN NEW.event_kind:='supersession'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER feed_kind BEFORE INSERT ON goldberg_private.change_feed FOR EACH ROW EXECUTE FUNCTION goldberg_private.feed_kind();
CREATE INDEX feed_entity_sequence ON goldberg_private.change_feed(entity_id,seq DESC);
CREATE INDEX abuse_window_expiry ON goldberg_private.abuse_limits(window_end);
CREATE TABLE goldberg_private.intake_control(singleton boolean PRIMARY KEY DEFAULT true CHECK(singleton), enabled boolean NOT NULL DEFAULT false);
-- Deliberately fail closed until a local operator explicitly enables intake.
INSERT INTO goldberg_private.intake_control VALUES(true,false);
REVOKE ALL ON goldberg_private.intake_control FROM PUBLIC;
REVOKE ALL ON FUNCTION goldberg_private.feed_kind() FROM PUBLIC;
$migration$;
  INSERT INTO goldberg_private.deployment_migrations VALUES(1,'d39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935');
 END IF;
 -- No API/runtime grants. RLS with zero policies denies non-owner access.
 -- Owner bypass remains necessary for existing audit triggers; no FORCE RLS shortcut.
 REVOKE ALL ON SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 REVOKE ALL ON ALL TABLES IN SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 REVOKE ALL ON ALL SEQUENCES IN SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 REVOKE ALL ON ALL FUNCTIONS IN SCHEMA goldberg_private FROM PUBLIC, anon, authenticated, service_role;
 ALTER DEFAULT PRIVILEGES IN SCHEMA goldberg_private REVOKE ALL ON TABLES FROM PUBLIC, anon, authenticated, service_role;
 ALTER DEFAULT PRIVILEGES IN SCHEMA goldberg_private REVOKE ALL ON SEQUENCES FROM PUBLIC, anon, authenticated, service_role;
 ALTER DEFAULT PRIVILEGES IN SCHEMA goldberg_private REVOKE EXECUTE ON FUNCTIONS FROM PUBLIC, anon, authenticated, service_role;
 FOR t IN SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
   WHERE n.nspname='goldberg_private' AND c.relkind IN ('r','p') LOOP
  EXECUTE format('ALTER TABLE goldberg_private.%I ENABLE ROW LEVEL SECURITY',t.relname);
 END LOOP;
 IF (SELECT enabled FROM goldberg_private.intake_control WHERE singleton) THEN
  RAISE EXCEPTION 'Intake must remain disabled during readiness'; END IF;
END
$bootstrap$;
COMMIT;
