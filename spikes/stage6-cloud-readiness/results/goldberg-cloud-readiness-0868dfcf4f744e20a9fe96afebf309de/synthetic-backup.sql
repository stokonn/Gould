--
-- PostgreSQL database dump
--

\restrict fHPmLgTcRazxMrWO7CcQOL4jS1UG1carOvo39EiCD8QZw7Q3gP48s0NLCHIHTFI

-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: goldberg_private; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA goldberg_private;


--
-- Name: sha256_hex; Type: DOMAIN; Schema: goldberg_private; Owner: -
--

CREATE DOMAIN goldberg_private.sha256_hex AS text
	CONSTRAINT sha256_hex_check CHECK ((VALUE ~ '^[0-9a-f]{64}$'::text));


--
-- Name: submission_state; Type: DOMAIN; Schema: goldberg_private; Owner: -
--

CREATE DOMAIN goldberg_private.submission_state AS text
	CONSTRAINT submission_state_check CHECK ((VALUE = ANY (ARRAY['submitted'::text, 'in_review'::text, 'changes_requested'::text, 'approved'::text, 'published'::text, 'rejected'::text, 'withdrawn'::text, 'superseded'::text, 'quarantined'::text])));


--
-- Name: append_audit(uuid, text, text, uuid, bigint, uuid, text, text, text); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.append_audit(actor uuid, action text, target_kind text, target uuid, revision bigint, request uuid, reason text DEFAULT NULL::text, before_hash text DEFAULT NULL::text, after_hash text DEFAULT NULL::text) RETURNS bigint
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'pg_catalog'
    AS $$
DECLARE h goldberg_private.audit_head; result bigint;
BEGIN
 SELECT * INTO STRICT h FROM goldberg_private.audit_head WHERE singleton FOR UPDATE;
 INSERT INTO goldberg_private.audit_events(seq,prev_hash,actor_id,action,target_kind,target_id,target_revision,request_id,reason,before_hash,after_hash)
 VALUES(h.last_seq+1,h.last_hash,actor,action,target_kind,target,revision,request,reason,before_hash,after_hash) RETURNING seq INTO result;
 RETURN result;
END; $$;


--
-- Name: audit_advance(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.audit_advance() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN UPDATE goldberg_private.audit_head SET last_seq=NEW.seq,last_hash=NEW.this_hash WHERE singleton; RETURN NEW; END; $$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: audit_events; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.audit_events (
    seq bigint NOT NULL,
    prev_hash goldberg_private.sha256_hex NOT NULL,
    this_hash goldberg_private.sha256_hex NOT NULL,
    actor_id uuid NOT NULL,
    action text NOT NULL,
    target_kind text NOT NULL,
    target_id uuid NOT NULL,
    target_revision bigint,
    before_hash goldberg_private.sha256_hex,
    after_hash goldberg_private.sha256_hex,
    reason text,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    request_id uuid NOT NULL,
    canonical_content text NOT NULL,
    submission_target uuid GENERATED ALWAYS AS (
CASE
    WHEN (target_kind = 'submission'::text) THEN target_id
    ELSE NULL::uuid
END) STORED,
    entity_target uuid GENERATED ALWAYS AS (
CASE
    WHEN (target_kind = 'entity'::text) THEN target_id
    ELSE NULL::uuid
END) STORED,
    principal_target uuid GENERATED ALWAYS AS (
CASE
    WHEN (target_kind = 'principal'::text) THEN target_id
    ELSE NULL::uuid
END) STORED,
    CONSTRAINT audit_events_action_check CHECK ((length(action) > 0)),
    CONSTRAINT audit_events_check CHECK ((((target_kind = 'principal'::text) AND (target_revision IS NULL)) OR ((target_kind = ANY (ARRAY['submission'::text, 'entity'::text])) AND (target_revision IS NOT NULL)))),
    CONSTRAINT audit_events_seq_check CHECK ((seq > 0)),
    CONSTRAINT audit_events_target_kind_check CHECK ((length(target_kind) > 0)),
    CONSTRAINT audit_events_target_kind_check1 CHECK ((target_kind = ANY (ARRAY['submission'::text, 'entity'::text, 'principal'::text]))),
    CONSTRAINT audit_events_target_revision_check CHECK ((target_revision > 0))
);


--
-- Name: audit_content(goldberg_private.audit_events); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.audit_content(e goldberg_private.audit_events) RETURNS text
    LANGUAGE sql STABLE
    SET search_path TO 'pg_catalog'
    AS $$
 SELECT jsonb_build_array('goldberg-audit-v1',e.seq::text,e.actor_id::text,e.action,e.target_kind,
 e.target_id::text,e.target_revision::text,e.before_hash,e.after_hash,e.reason,
 to_char(e.created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"'),e.request_id::text)::text
$$;


--
-- Name: audit_insert_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.audit_insert_guard() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'pg_catalog'
    AS $$
DECLARE h goldberg_private.audit_head; computed text;
BEGIN
 SELECT * INTO STRICT h FROM goldberg_private.audit_head WHERE singleton FOR UPDATE;
 IF NEW.seq<>h.last_seq+1 OR NEW.prev_hash<>h.last_hash THEN RAISE EXCEPTION 'audit head conflict' USING ERRCODE='23514'; END IF;
 NEW.canonical_content:=goldberg_private.audit_content(NEW);
 computed:=encode(sha256(convert_to(NEW.prev_hash||':'||NEW.canonical_content,'UTF8')),'hex');
 IF NEW.this_hash IS NOT NULL AND NEW.this_hash<>computed THEN RAISE EXCEPTION 'audit hash mismatch' USING ERRCODE='23514'; END IF;
 NEW.this_hash:=computed; RETURN NEW;
END; $$;


--
-- Name: consume_only(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.consume_only() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN
 IF (to_jsonb(NEW)-'consumed_at') IS DISTINCT FROM (to_jsonb(OLD)-'consumed_at')
 OR (OLD.consumed_at IS NOT NULL AND NEW.consumed_at IS DISTINCT FROM OLD.consumed_at) THEN
  RAISE EXCEPTION 'only first consumption allowed' USING ERRCODE='55000'; END IF;
 RETURN NEW;
END; $$;


--
-- Name: deny_mutation(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.deny_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN RAISE EXCEPTION 'immutable/append-only record' USING ERRCODE='55000'; END; $$;


--
-- Name: feed_kind(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.feed_kind() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN
 IF NEW.status='quarantined' THEN NEW.event_kind:='quarantine';
 ELSIF NEW.status='superseded' THEN NEW.event_kind:='supersession';
 ELSIF NEW.event_kind='publication' AND EXISTS(SELECT 1 FROM goldberg_private.change_feed WHERE entity_id=NEW.entity_id AND status='published')
 THEN NEW.event_kind:='supersession'; END IF;
 RETURN NEW;
END; $$;


--
-- Name: head_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.head_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN
 IF NEW.singleton IS DISTINCT FROM OLD.singleton OR NEW.last_seq<>OLD.last_seq+1 OR NOT EXISTS
  (SELECT 1 FROM goldberg_private.audit_events WHERE seq=NEW.last_seq AND this_hash=NEW.last_hash) THEN
  RAISE EXCEPTION 'head must match appended event' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;


--
-- Name: published_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.published_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $_$
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
END; $_$;


--
-- Name: review_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.review_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
DECLARE author uuid;
BEGIN
 SELECT author_id INTO author FROM goldberg_private.submissions WHERE id=NEW.submission_id;
 IF NEW.to_status IN ('approved','rejected','changes_requested') AND NEW.actor_id=author THEN
  RAISE EXCEPTION 'self decision forbidden' USING ERRCODE='23514'; END IF;
 IF NEW.to_status IN ('withdrawn') AND NEW.actor_id<>author THEN
  RAISE EXCEPTION 'withdrawal actor must be author' USING ERRCODE='23514'; END IF;
 RETURN NEW;
END; $$;


--
-- Name: revision_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.revision_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
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


--
-- Name: revision_pointer_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.revision_pointer_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN
 IF NOT EXISTS(SELECT 1 FROM goldberg_private.submissions WHERE id=NEW.submission_id AND current_revision>=NEW.revision) THEN
  RAISE EXCEPTION 'revision must advance its header atomically' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END; $$;


--
-- Name: revocation_only(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.revocation_only() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN
 IF (to_jsonb(NEW)-'revoked_at') IS DISTINCT FROM (to_jsonb(OLD)-'revoked_at')
 OR (OLD.revoked_at IS NOT NULL AND NEW.revoked_at IS DISTINCT FROM OLD.revoked_at) THEN
  RAISE EXCEPTION 'only first soft revocation allowed' USING ERRCODE='55000'; END IF;
 RETURN NEW;
END; $$;


--
-- Name: submission_guard(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.submission_guard() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
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


--
-- Name: transition_evidence(); Type: FUNCTION; Schema: goldberg_private; Owner: -
--

CREATE FUNCTION goldberg_private.transition_evidence() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path TO 'pg_catalog'
    AS $$
BEGIN
 IF NEW.status=OLD.status THEN RETURN NULL; END IF;
 IF NOT EXISTS(SELECT 1 FROM goldberg_private.review_events r JOIN goldberg_private.audit_events a ON a.seq=r.audit_seq
  WHERE r.submission_id=NEW.id AND r.submission_revision=NEW.current_revision
    AND r.from_status=OLD.status AND r.to_status=NEW.status AND a.actor_id=r.actor_id
    AND a.target_id=NEW.id AND a.target_kind='submission' AND a.target_revision=NEW.current_revision) THEN
  RAISE EXCEPTION 'transition requires matching review and audit' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END; $$;


--
-- Name: abuse_limits; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.abuse_limits (
    scope text NOT NULL,
    scope_key text NOT NULL,
    principal_id uuid,
    window_start timestamp with time zone NOT NULL,
    window_end timestamp with time zone NOT NULL,
    count bigint NOT NULL,
    CONSTRAINT abuse_limits_check CHECK ((window_end > window_start)),
    CONSTRAINT abuse_limits_check1 CHECK ((((scope = 'principal'::text) AND (principal_id IS NOT NULL) AND (scope_key = (principal_id)::text)) OR ((scope = 'coarse_ip'::text) AND (principal_id IS NULL) AND (scope_key ~ '^[0-9a-f]{64}$'::text)))),
    CONSTRAINT abuse_limits_count_check CHECK ((count >= 0)),
    CONSTRAINT abuse_limits_scope_check CHECK ((scope = ANY (ARRAY['principal'::text, 'coarse_ip'::text])))
);


--
-- Name: audit_head; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.audit_head (
    singleton boolean DEFAULT true NOT NULL,
    last_seq bigint NOT NULL,
    last_hash goldberg_private.sha256_hex NOT NULL,
    CONSTRAINT audit_head_check CHECK (((last_seq <> 0) OR ((last_hash)::text = repeat('0'::text, 64)))),
    CONSTRAINT audit_head_last_seq_check CHECK ((last_seq >= 0)),
    CONSTRAINT audit_head_singleton_check CHECK (singleton)
);


--
-- Name: auth_challenges; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.auth_challenges (
    token_hash goldberg_private.sha256_hex NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    consumed_at timestamp with time zone,
    CONSTRAINT auth_challenges_check CHECK ((expires_at > created_at)),
    CONSTRAINT auth_challenges_check1 CHECK (((consumed_at IS NULL) OR (consumed_at >= created_at)))
);


--
-- Name: auth_families; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.auth_families (
    id uuid NOT NULL,
    principal_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    CONSTRAINT auth_families_check CHECK ((expires_at > created_at)),
    CONSTRAINT auth_families_check1 CHECK (((revoked_at IS NULL) OR (revoked_at >= created_at)))
);


--
-- Name: auth_refresh; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.auth_refresh (
    token_hash goldberg_private.sha256_hex NOT NULL,
    session_id uuid NOT NULL,
    principal_id uuid NOT NULL,
    family_id uuid NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    consumed_at timestamp with time zone,
    CONSTRAINT auth_refresh_check CHECK ((expires_at > created_at)),
    CONSTRAINT auth_refresh_check1 CHECK (((consumed_at IS NULL) OR (consumed_at >= created_at)))
);


--
-- Name: change_feed; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.change_feed (
    seq bigint NOT NULL,
    entity_id uuid NOT NULL,
    revision bigint NOT NULL,
    status text NOT NULL,
    reason text,
    audit_seq bigint NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    event_kind text DEFAULT 'publication'::text NOT NULL,
    CONSTRAINT change_feed_check CHECK (((status = 'published'::text) OR (NULLIF(btrim(reason), ''::text) IS NOT NULL))),
    CONSTRAINT change_feed_event_kind_check CHECK ((event_kind = ANY (ARRAY['publication'::text, 'supersession'::text, 'quarantine'::text, 'rollback'::text]))),
    CONSTRAINT change_feed_status_check CHECK ((status = ANY (ARRAY['published'::text, 'superseded'::text, 'quarantined'::text])))
);


--
-- Name: change_feed_seq_seq; Type: SEQUENCE; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.change_feed ALTER COLUMN seq ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME goldberg_private.change_feed_seq_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: deployment_migrations; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.deployment_migrations (
    version integer NOT NULL,
    checksum text NOT NULL
);


--
-- Name: idempotency_results; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.idempotency_results (
    principal_id uuid NOT NULL,
    key uuid NOT NULL,
    request_hash goldberg_private.sha256_hex NOT NULL,
    http_status integer NOT NULL,
    response jsonb NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    CONSTRAINT idempotency_results_check CHECK ((expires_at >= (created_at + '24:00:00'::interval))),
    CONSTRAINT idempotency_results_http_status_check CHECK (((http_status >= 200) AND (http_status <= 299)))
);


--
-- Name: identity_mappings; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.identity_mappings (
    id uuid NOT NULL,
    provider text NOT NULL,
    provider_entity_type text NOT NULL,
    external_id text NOT NULL,
    catalogue_uuid uuid,
    state text NOT NULL,
    CONSTRAINT identity_mappings_check CHECK (((state = 'resolved'::text) = (catalogue_uuid IS NOT NULL))),
    CONSTRAINT identity_mappings_external_id_check CHECK ((length(external_id) > 0)),
    CONSTRAINT identity_mappings_provider_check CHECK ((length(provider) > 0)),
    CONSTRAINT identity_mappings_provider_entity_type_check CHECK ((length(provider_entity_type) > 0)),
    CONSTRAINT identity_mappings_state_check CHECK ((state = ANY (ARRAY['needs_review'::text, 'resolved'::text])))
);


--
-- Name: intake_control; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.intake_control (
    singleton boolean DEFAULT true NOT NULL,
    enabled boolean DEFAULT false NOT NULL,
    CONSTRAINT intake_control_singleton_check CHECK (singleton)
);


--
-- Name: principals; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.principals (
    id uuid NOT NULL,
    iss text NOT NULL,
    sub text NOT NULL,
    state text DEFAULT 'active'::text NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT principals_iss_check CHECK ((length(iss) > 0)),
    CONSTRAINT principals_state_check CHECK ((state = ANY (ARRAY['active'::text, 'suspended'::text, 'deleted'::text]))),
    CONSTRAINT principals_sub_check CHECK ((length(sub) > 0))
);


--
-- Name: published_entities; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.published_entities (
    id uuid NOT NULL,
    kind text NOT NULL,
    revision bigint NOT NULL,
    status text NOT NULL,
    reason text,
    CONSTRAINT published_entities_check CHECK (((status = 'published'::text) OR (NULLIF(btrim(reason), ''::text) IS NOT NULL))),
    CONSTRAINT published_entities_kind_check CHECK ((length(kind) > 0)),
    CONSTRAINT published_entities_revision_check CHECK ((revision > 0)),
    CONSTRAINT published_entities_status_check CHECK ((status = ANY (ARRAY['published'::text, 'superseded'::text, 'quarantined'::text])))
);


--
-- Name: published_revisions; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.published_revisions (
    entity_id uuid NOT NULL,
    revision bigint NOT NULL,
    submission_id uuid NOT NULL,
    submission_revision bigint NOT NULL,
    actor_id uuid NOT NULL,
    payload jsonb NOT NULL,
    this_hash goldberg_private.sha256_hex NOT NULL,
    source_evidence jsonb NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT published_revisions_payload_check CHECK ((jsonb_typeof(payload) = 'object'::text)),
    CONSTRAINT published_revisions_revision_check CHECK ((revision > 0)),
    CONSTRAINT published_revisions_source_evidence_check CHECK ((jsonb_typeof(source_evidence) = 'array'::text))
);


--
-- Name: review_events; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.review_events (
    id uuid NOT NULL,
    submission_id uuid NOT NULL,
    submission_revision bigint NOT NULL,
    actor_id uuid NOT NULL,
    from_status goldberg_private.submission_state NOT NULL,
    to_status goldberg_private.submission_state NOT NULL,
    reason text,
    audit_seq bigint NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT review_events_check CHECK (((from_status)::text <> (to_status)::text)),
    CONSTRAINT review_events_check1 CHECK ((((to_status)::text <> ALL (ARRAY['rejected'::text, 'changes_requested'::text, 'superseded'::text, 'quarantined'::text])) OR (NULLIF(btrim(reason), ''::text) IS NOT NULL)))
);


--
-- Name: role_grants; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.role_grants (
    id uuid NOT NULL,
    principal_id uuid NOT NULL,
    role text NOT NULL,
    granted_by uuid NOT NULL,
    granted_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    revoked_at timestamp with time zone,
    CONSTRAINT role_grants_check CHECK (((revoked_at IS NULL) OR (revoked_at >= granted_at))),
    CONSTRAINT role_grants_role_check CHECK ((role = ANY (ARRAY['contributor'::text, 'moderator'::text, 'administrator'::text])))
);


--
-- Name: sessions; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.sessions (
    id uuid NOT NULL,
    principal_id uuid NOT NULL,
    family_id uuid NOT NULL,
    predecessor_id uuid,
    token_hash goldberg_private.sha256_hex NOT NULL,
    issued_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    revoked_at timestamp with time zone,
    CONSTRAINT sessions_check CHECK ((predecessor_id IS DISTINCT FROM id)),
    CONSTRAINT sessions_check1 CHECK ((expires_at > issued_at)),
    CONSTRAINT sessions_check2 CHECK (((revoked_at IS NULL) OR (revoked_at >= issued_at)))
);


--
-- Name: submission_revisions; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.submission_revisions (
    submission_id uuid NOT NULL,
    revision bigint NOT NULL,
    payload jsonb NOT NULL,
    prev_hash goldberg_private.sha256_hex NOT NULL,
    this_hash goldberg_private.sha256_hex NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT submission_revisions_payload_check CHECK ((jsonb_typeof(payload) = 'object'::text)),
    CONSTRAINT submission_revisions_revision_check CHECK ((revision > 0))
);


--
-- Name: submissions; Type: TABLE; Schema: goldberg_private; Owner: -
--

CREATE TABLE goldberg_private.submissions (
    id uuid NOT NULL,
    author_id uuid NOT NULL,
    target_kind text NOT NULL,
    catalogue_uuid uuid,
    mapping_id uuid,
    target_entity_id uuid,
    expected_revision bigint NOT NULL,
    status goldberg_private.submission_state DEFAULT 'submitted'::text NOT NULL,
    current_revision bigint NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    updated_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT submissions_check CHECK (((catalogue_uuid IS NOT NULL) OR (mapping_id IS NOT NULL))),
    CONSTRAINT submissions_check1 CHECK ((updated_at >= created_at)),
    CONSTRAINT submissions_current_revision_check CHECK ((current_revision > 0)),
    CONSTRAINT submissions_expected_revision_check CHECK ((expected_revision >= 0)),
    CONSTRAINT submissions_target_kind_check CHECK ((length(target_kind) > 0))
);


--
-- Data for Name: abuse_limits; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.abuse_limits (scope, scope_key, principal_id, window_start, window_end, count) FROM stdin;
\.


--
-- Data for Name: audit_events; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.audit_events (seq, prev_hash, this_hash, actor_id, action, target_kind, target_id, target_revision, before_hash, after_hash, reason, created_at, request_id, canonical_content) FROM stdin;
\.


--
-- Data for Name: audit_head; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.audit_head (singleton, last_seq, last_hash) FROM stdin;
t	0	0000000000000000000000000000000000000000000000000000000000000000
\.


--
-- Data for Name: auth_challenges; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.auth_challenges (token_hash, created_at, expires_at, consumed_at) FROM stdin;
\.


--
-- Data for Name: auth_families; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.auth_families (id, principal_id, created_at, expires_at, revoked_at) FROM stdin;
\.


--
-- Data for Name: auth_refresh; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.auth_refresh (token_hash, session_id, principal_id, family_id, created_at, expires_at, consumed_at) FROM stdin;
\.


--
-- Data for Name: change_feed; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.change_feed (seq, entity_id, revision, status, reason, audit_seq, created_at, event_kind) FROM stdin;
\.


--
-- Data for Name: deployment_migrations; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.deployment_migrations (version, checksum) FROM stdin;
1	d39a3afd68a2671dd642a17a82f96edea76dfc0ae1a09edb4be9ffde6aa5c935
\.


--
-- Data for Name: idempotency_results; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.idempotency_results (principal_id, key, request_hash, http_status, response, created_at, expires_at) FROM stdin;
\.


--
-- Data for Name: identity_mappings; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.identity_mappings (id, provider, provider_entity_type, external_id, catalogue_uuid, state) FROM stdin;
\.


--
-- Data for Name: intake_control; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.intake_control (singleton, enabled) FROM stdin;
t	f
\.


--
-- Data for Name: principals; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.principals (id, iss, sub, state, created_at) FROM stdin;
00000000-0000-0000-0000-000000000001	synthetic.invalid	fixture	active	2026-10-24 19:20:50.137997+00
\.


--
-- Data for Name: published_entities; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.published_entities (id, kind, revision, status, reason) FROM stdin;
\.


--
-- Data for Name: published_revisions; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.published_revisions (entity_id, revision, submission_id, submission_revision, actor_id, payload, this_hash, source_evidence, created_at) FROM stdin;
\.


--
-- Data for Name: review_events; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.review_events (id, submission_id, submission_revision, actor_id, from_status, to_status, reason, audit_seq, created_at) FROM stdin;
\.


--
-- Data for Name: role_grants; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.role_grants (id, principal_id, role, granted_by, granted_at, revoked_at) FROM stdin;
\.


--
-- Data for Name: sessions; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.sessions (id, principal_id, family_id, predecessor_id, token_hash, issued_at, expires_at, revoked_at) FROM stdin;
\.


--
-- Data for Name: submission_revisions; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.submission_revisions (submission_id, revision, payload, prev_hash, this_hash, created_at) FROM stdin;
\.


--
-- Data for Name: submissions; Type: TABLE DATA; Schema: goldberg_private; Owner: -
--

COPY goldberg_private.submissions (id, author_id, target_kind, catalogue_uuid, mapping_id, target_entity_id, expected_revision, status, current_revision, created_at, updated_at) FROM stdin;
\.


--
-- Name: change_feed_seq_seq; Type: SEQUENCE SET; Schema: goldberg_private; Owner: -
--

SELECT pg_catalog.setval('goldberg_private.change_feed_seq_seq', 1, false);


--
-- Name: abuse_limits abuse_limits_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.abuse_limits
    ADD CONSTRAINT abuse_limits_pkey PRIMARY KEY (scope, scope_key, window_start);


--
-- Name: audit_events audit_events_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.audit_events
    ADD CONSTRAINT audit_events_pkey PRIMARY KEY (seq);


--
-- Name: audit_head audit_head_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.audit_head
    ADD CONSTRAINT audit_head_pkey PRIMARY KEY (singleton);


--
-- Name: auth_challenges auth_challenges_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_challenges
    ADD CONSTRAINT auth_challenges_pkey PRIMARY KEY (token_hash);


--
-- Name: auth_families auth_families_id_principal_id_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_families
    ADD CONSTRAINT auth_families_id_principal_id_key UNIQUE (id, principal_id);


--
-- Name: auth_families auth_families_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_families
    ADD CONSTRAINT auth_families_pkey PRIMARY KEY (id);


--
-- Name: auth_refresh auth_refresh_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_refresh
    ADD CONSTRAINT auth_refresh_pkey PRIMARY KEY (token_hash);


--
-- Name: auth_refresh auth_refresh_session_id_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_refresh
    ADD CONSTRAINT auth_refresh_session_id_key UNIQUE (session_id);


--
-- Name: change_feed change_feed_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.change_feed
    ADD CONSTRAINT change_feed_pkey PRIMARY KEY (seq);


--
-- Name: deployment_migrations deployment_migrations_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.deployment_migrations
    ADD CONSTRAINT deployment_migrations_pkey PRIMARY KEY (version);


--
-- Name: idempotency_results idempotency_results_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.idempotency_results
    ADD CONSTRAINT idempotency_results_pkey PRIMARY KEY (principal_id, key);


--
-- Name: identity_mappings identity_mappings_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.identity_mappings
    ADD CONSTRAINT identity_mappings_pkey PRIMARY KEY (id);


--
-- Name: identity_mappings identity_mappings_provider_provider_entity_type_external_id_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.identity_mappings
    ADD CONSTRAINT identity_mappings_provider_provider_entity_type_external_id_key UNIQUE (provider, provider_entity_type, external_id);


--
-- Name: intake_control intake_control_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.intake_control
    ADD CONSTRAINT intake_control_pkey PRIMARY KEY (singleton);


--
-- Name: principals principals_iss_sub_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.principals
    ADD CONSTRAINT principals_iss_sub_key UNIQUE (iss, sub);


--
-- Name: principals principals_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.principals
    ADD CONSTRAINT principals_pkey PRIMARY KEY (id);


--
-- Name: published_entities published_entities_id_kind_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_entities
    ADD CONSTRAINT published_entities_id_kind_key UNIQUE (id, kind);


--
-- Name: published_entities published_entities_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_entities
    ADD CONSTRAINT published_entities_pkey PRIMARY KEY (id);


--
-- Name: published_revisions published_revisions_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_revisions
    ADD CONSTRAINT published_revisions_pkey PRIMARY KEY (entity_id, revision);


--
-- Name: published_revisions published_revisions_submission_id_submission_revision_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_revisions
    ADD CONSTRAINT published_revisions_submission_id_submission_revision_key UNIQUE (submission_id, submission_revision);


--
-- Name: review_events review_events_audit_seq_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.review_events
    ADD CONSTRAINT review_events_audit_seq_key UNIQUE (audit_seq);


--
-- Name: review_events review_events_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.review_events
    ADD CONSTRAINT review_events_pkey PRIMARY KEY (id);


--
-- Name: role_grants role_grants_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.role_grants
    ADD CONSTRAINT role_grants_pkey PRIMARY KEY (id);


--
-- Name: sessions sessions_id_principal_id_family_id_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_id_principal_id_family_id_key UNIQUE (id, principal_id, family_id);


--
-- Name: sessions sessions_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);


--
-- Name: sessions sessions_predecessor_id_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_predecessor_id_key UNIQUE (predecessor_id);


--
-- Name: sessions sessions_token_hash_key; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_token_hash_key UNIQUE (token_hash);


--
-- Name: submission_revisions submission_revisions_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submission_revisions
    ADD CONSTRAINT submission_revisions_pkey PRIMARY KEY (submission_id, revision);


--
-- Name: submissions submissions_pkey; Type: CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submissions
    ADD CONSTRAINT submissions_pkey PRIMARY KEY (id);


--
-- Name: abuse_window_expiry; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX abuse_window_expiry ON goldberg_private.abuse_limits USING btree (window_end);


--
-- Name: active_role_grant; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE UNIQUE INDEX active_role_grant ON goldberg_private.role_grants USING btree (principal_id, role) WHERE (revoked_at IS NULL);


--
-- Name: audit_requests; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX audit_requests ON goldberg_private.audit_events USING btree (request_id, seq);


--
-- Name: auth_family_refresh; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX auth_family_refresh ON goldberg_private.auth_refresh USING btree (family_id);


--
-- Name: auth_family_sessions; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX auth_family_sessions ON goldberg_private.sessions USING btree (family_id);


--
-- Name: author_submissions; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX author_submissions ON goldberg_private.submissions USING btree (author_id, created_at, id);


--
-- Name: feed_entity_sequence; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX feed_entity_sequence ON goldberg_private.change_feed USING btree (entity_id, seq DESC);


--
-- Name: idempotency_expiry; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX idempotency_expiry ON goldberg_private.idempotency_results USING btree (expires_at);


--
-- Name: submission_queue; Type: INDEX; Schema: goldberg_private; Owner: -
--

CREATE INDEX submission_queue ON goldberg_private.submissions USING btree (status, created_at, id);


--
-- Name: audit_events audit_chain; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER audit_chain BEFORE INSERT ON goldberg_private.audit_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.audit_insert_guard();


--
-- Name: audit_events audit_head_advance; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER audit_head_advance AFTER INSERT ON goldberg_private.audit_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.audit_advance();


--
-- Name: auth_challenges challenge_consumption; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER challenge_consumption BEFORE UPDATE ON goldberg_private.auth_challenges FOR EACH ROW EXECUTE FUNCTION goldberg_private.consume_only();


--
-- Name: auth_families family_revocation; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER family_revocation BEFORE UPDATE ON goldberg_private.auth_families FOR EACH ROW EXECUTE FUNCTION goldberg_private.revocation_only();


--
-- Name: change_feed feed_kind; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER feed_kind BEFORE INSERT ON goldberg_private.change_feed FOR EACH ROW EXECUTE FUNCTION goldberg_private.feed_kind();


--
-- Name: audit_head head_consistency; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER head_consistency BEFORE UPDATE ON goldberg_private.audit_head FOR EACH ROW EXECUTE FUNCTION goldberg_private.head_guard();


--
-- Name: audit_events immutable; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER immutable BEFORE DELETE OR UPDATE ON goldberg_private.audit_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: change_feed immutable; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER immutable BEFORE DELETE OR UPDATE ON goldberg_private.change_feed FOR EACH ROW EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: published_revisions immutable; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER immutable BEFORE DELETE OR UPDATE ON goldberg_private.published_revisions FOR EACH ROW EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: review_events immutable; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER immutable BEFORE DELETE OR UPDATE ON goldberg_private.review_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: submission_revisions immutable; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER immutable BEFORE DELETE OR UPDATE ON goldberg_private.submission_revisions FOR EACH ROW EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: audit_events no_truncate; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER no_truncate BEFORE TRUNCATE ON goldberg_private.audit_events FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: change_feed no_truncate; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER no_truncate BEFORE TRUNCATE ON goldberg_private.change_feed FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: published_revisions no_truncate; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER no_truncate BEFORE TRUNCATE ON goldberg_private.published_revisions FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: review_events no_truncate; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER no_truncate BEFORE TRUNCATE ON goldberg_private.review_events FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: submission_revisions no_truncate; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER no_truncate BEFORE TRUNCATE ON goldberg_private.submission_revisions FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: audit_head preserve_head; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER preserve_head BEFORE DELETE OR TRUNCATE ON goldberg_private.audit_head FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: published_revisions published_snapshot; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER published_snapshot BEFORE INSERT ON goldberg_private.published_revisions FOR EACH ROW EXECUTE FUNCTION goldberg_private.published_guard();


--
-- Name: auth_refresh refresh_consumption; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER refresh_consumption BEFORE UPDATE ON goldberg_private.auth_refresh FOR EACH ROW EXECUTE FUNCTION goldberg_private.consume_only();


--
-- Name: review_events review_actor; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER review_actor BEFORE INSERT ON goldberg_private.review_events FOR EACH ROW EXECUTE FUNCTION goldberg_private.review_guard();


--
-- Name: submission_revisions revision_chain; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER revision_chain BEFORE INSERT ON goldberg_private.submission_revisions FOR EACH ROW EXECUTE FUNCTION goldberg_private.revision_guard();


--
-- Name: submission_revisions revision_pointer; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE CONSTRAINT TRIGGER revision_pointer AFTER INSERT ON goldberg_private.submission_revisions DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION goldberg_private.revision_pointer_guard();


--
-- Name: role_grants role_history; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER role_history BEFORE DELETE OR TRUNCATE ON goldberg_private.role_grants FOR EACH STATEMENT EXECUTE FUNCTION goldberg_private.deny_mutation();


--
-- Name: role_grants role_revocation; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER role_revocation BEFORE UPDATE ON goldberg_private.role_grants FOR EACH ROW EXECUTE FUNCTION goldberg_private.revocation_only();


--
-- Name: sessions session_revocation; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER session_revocation BEFORE UPDATE ON goldberg_private.sessions FOR EACH ROW EXECUTE FUNCTION goldberg_private.revocation_only();


--
-- Name: submissions submission_state; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE TRIGGER submission_state BEFORE INSERT OR UPDATE ON goldberg_private.submissions FOR EACH ROW EXECUTE FUNCTION goldberg_private.submission_guard();


--
-- Name: submissions transition_evidence; Type: TRIGGER; Schema: goldberg_private; Owner: -
--

CREATE CONSTRAINT TRIGGER transition_evidence AFTER UPDATE ON goldberg_private.submissions DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION goldberg_private.transition_evidence();


--
-- Name: abuse_limits abuse_limits_principal_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.abuse_limits
    ADD CONSTRAINT abuse_limits_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: audit_events audit_events_actor_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.audit_events
    ADD CONSTRAINT audit_events_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: audit_events audit_events_entity_target_target_revision_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.audit_events
    ADD CONSTRAINT audit_events_entity_target_target_revision_fkey FOREIGN KEY (entity_target, target_revision) REFERENCES goldberg_private.published_revisions(entity_id, revision) ON DELETE RESTRICT;


--
-- Name: audit_events audit_events_principal_target_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.audit_events
    ADD CONSTRAINT audit_events_principal_target_fkey FOREIGN KEY (principal_target) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: audit_events audit_events_submission_target_target_revision_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.audit_events
    ADD CONSTRAINT audit_events_submission_target_target_revision_fkey FOREIGN KEY (submission_target, target_revision) REFERENCES goldberg_private.submission_revisions(submission_id, revision) ON DELETE RESTRICT;


--
-- Name: auth_families auth_families_principal_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_families
    ADD CONSTRAINT auth_families_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: auth_refresh auth_refresh_session_id_principal_id_family_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.auth_refresh
    ADD CONSTRAINT auth_refresh_session_id_principal_id_family_id_fkey FOREIGN KEY (session_id, principal_id, family_id) REFERENCES goldberg_private.sessions(id, principal_id, family_id) ON DELETE RESTRICT;


--
-- Name: change_feed change_feed_audit_seq_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.change_feed
    ADD CONSTRAINT change_feed_audit_seq_fkey FOREIGN KEY (audit_seq) REFERENCES goldberg_private.audit_events(seq) ON DELETE RESTRICT;


--
-- Name: change_feed change_feed_entity_id_revision_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.change_feed
    ADD CONSTRAINT change_feed_entity_id_revision_fkey FOREIGN KEY (entity_id, revision) REFERENCES goldberg_private.published_revisions(entity_id, revision) ON DELETE RESTRICT;


--
-- Name: published_entities current_published_revision; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_entities
    ADD CONSTRAINT current_published_revision FOREIGN KEY (id, revision) REFERENCES goldberg_private.published_revisions(entity_id, revision) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: submissions current_submission_revision; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submissions
    ADD CONSTRAINT current_submission_revision FOREIGN KEY (id, current_revision) REFERENCES goldberg_private.submission_revisions(submission_id, revision) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: idempotency_results idempotency_results_principal_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.idempotency_results
    ADD CONSTRAINT idempotency_results_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: published_revisions published_revisions_actor_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_revisions
    ADD CONSTRAINT published_revisions_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: published_revisions published_revisions_entity_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_revisions
    ADD CONSTRAINT published_revisions_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES goldberg_private.published_entities(id) ON DELETE RESTRICT;


--
-- Name: published_revisions published_revisions_submission_id_submission_revision_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.published_revisions
    ADD CONSTRAINT published_revisions_submission_id_submission_revision_fkey FOREIGN KEY (submission_id, submission_revision) REFERENCES goldberg_private.submission_revisions(submission_id, revision) ON DELETE RESTRICT;


--
-- Name: review_events review_events_actor_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.review_events
    ADD CONSTRAINT review_events_actor_id_fkey FOREIGN KEY (actor_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: review_events review_events_audit_seq_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.review_events
    ADD CONSTRAINT review_events_audit_seq_fkey FOREIGN KEY (audit_seq) REFERENCES goldberg_private.audit_events(seq) ON DELETE RESTRICT;


--
-- Name: review_events review_events_submission_id_submission_revision_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.review_events
    ADD CONSTRAINT review_events_submission_id_submission_revision_fkey FOREIGN KEY (submission_id, submission_revision) REFERENCES goldberg_private.submission_revisions(submission_id, revision) ON DELETE RESTRICT;


--
-- Name: role_grants role_grants_granted_by_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.role_grants
    ADD CONSTRAINT role_grants_granted_by_fkey FOREIGN KEY (granted_by) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: role_grants role_grants_principal_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.role_grants
    ADD CONSTRAINT role_grants_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: sessions session_auth_family; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT session_auth_family FOREIGN KEY (family_id, principal_id) REFERENCES goldberg_private.auth_families(id, principal_id) ON DELETE RESTRICT;


--
-- Name: sessions sessions_predecessor_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_predecessor_id_fkey FOREIGN KEY (predecessor_id) REFERENCES goldberg_private.sessions(id) ON DELETE RESTRICT;


--
-- Name: sessions sessions_predecessor_id_principal_id_family_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_predecessor_id_principal_id_family_id_fkey FOREIGN KEY (predecessor_id, principal_id, family_id) REFERENCES goldberg_private.sessions(id, principal_id, family_id);


--
-- Name: sessions sessions_principal_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.sessions
    ADD CONSTRAINT sessions_principal_id_fkey FOREIGN KEY (principal_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: submission_revisions submission_revisions_submission_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submission_revisions
    ADD CONSTRAINT submission_revisions_submission_id_fkey FOREIGN KEY (submission_id) REFERENCES goldberg_private.submissions(id) ON DELETE RESTRICT;


--
-- Name: submissions submissions_author_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submissions
    ADD CONSTRAINT submissions_author_id_fkey FOREIGN KEY (author_id) REFERENCES goldberg_private.principals(id) ON DELETE RESTRICT;


--
-- Name: submissions submissions_mapping_id_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submissions
    ADD CONSTRAINT submissions_mapping_id_fkey FOREIGN KEY (mapping_id) REFERENCES goldberg_private.identity_mappings(id) ON DELETE RESTRICT;


--
-- Name: submissions submissions_target_entity_id_target_kind_fkey; Type: FK CONSTRAINT; Schema: goldberg_private; Owner: -
--

ALTER TABLE ONLY goldberg_private.submissions
    ADD CONSTRAINT submissions_target_entity_id_target_kind_fkey FOREIGN KEY (target_entity_id, target_kind) REFERENCES goldberg_private.published_entities(id, kind) ON DELETE RESTRICT;


--
-- Name: abuse_limits; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.abuse_limits ENABLE ROW LEVEL SECURITY;

--
-- Name: audit_events; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.audit_events ENABLE ROW LEVEL SECURITY;

--
-- Name: audit_head; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.audit_head ENABLE ROW LEVEL SECURITY;

--
-- Name: auth_challenges; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.auth_challenges ENABLE ROW LEVEL SECURITY;

--
-- Name: auth_families; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.auth_families ENABLE ROW LEVEL SECURITY;

--
-- Name: auth_refresh; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.auth_refresh ENABLE ROW LEVEL SECURITY;

--
-- Name: change_feed; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.change_feed ENABLE ROW LEVEL SECURITY;

--
-- Name: deployment_migrations; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.deployment_migrations ENABLE ROW LEVEL SECURITY;

--
-- Name: idempotency_results; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.idempotency_results ENABLE ROW LEVEL SECURITY;

--
-- Name: identity_mappings; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.identity_mappings ENABLE ROW LEVEL SECURITY;

--
-- Name: intake_control; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.intake_control ENABLE ROW LEVEL SECURITY;

--
-- Name: principals; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.principals ENABLE ROW LEVEL SECURITY;

--
-- Name: published_entities; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.published_entities ENABLE ROW LEVEL SECURITY;

--
-- Name: published_revisions; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.published_revisions ENABLE ROW LEVEL SECURITY;

--
-- Name: review_events; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.review_events ENABLE ROW LEVEL SECURITY;

--
-- Name: role_grants; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.role_grants ENABLE ROW LEVEL SECURITY;

--
-- Name: sessions; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.sessions ENABLE ROW LEVEL SECURITY;

--
-- Name: submission_revisions; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.submission_revisions ENABLE ROW LEVEL SECURITY;

--
-- Name: submissions; Type: ROW SECURITY; Schema: goldberg_private; Owner: -
--

ALTER TABLE goldberg_private.submissions ENABLE ROW LEVEL SECURITY;

--
-- PostgreSQL database dump complete
--

\unrestrict fHPmLgTcRazxMrWO7CcQOL4jS1UG1carOvo39EiCD8QZw7Q3gP48s0NLCHIHTFI

