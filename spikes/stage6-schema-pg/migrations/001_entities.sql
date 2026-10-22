-- LOCAL SYNTHETIC PROTOTYPE ONLY. __S__ is replaced with a validated schema identifier.
CREATE DOMAIN __S__.sha256_hex AS text CHECK (VALUE ~ '^[0-9a-f]{64}$');
CREATE DOMAIN __S__.submission_state AS text CHECK (VALUE IN
 ('submitted','in_review','changes_requested','approved','published','rejected','withdrawn','superseded','quarantined'));
CREATE TABLE __S__.principals (
 id uuid PRIMARY KEY, iss text NOT NULL CHECK (length(iss)>0), sub text NOT NULL CHECK(length(sub)>0),
 state text NOT NULL DEFAULT 'active' CHECK(state IN ('active','suspended','deleted')),
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), UNIQUE(iss,sub)
);
CREATE TABLE __S__.role_grants (
 id uuid PRIMARY KEY, principal_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 role text NOT NULL CHECK(role IN ('contributor','moderator','administrator')),
 granted_by uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 granted_at timestamptz NOT NULL DEFAULT clock_timestamp(), revoked_at timestamptz,
 CHECK(revoked_at IS NULL OR revoked_at>=granted_at)
);
CREATE UNIQUE INDEX active_role_grant ON __S__.role_grants(principal_id,role) WHERE revoked_at IS NULL;
CREATE TABLE __S__.sessions (
 id uuid PRIMARY KEY, principal_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 family_id uuid NOT NULL, predecessor_id uuid UNIQUE REFERENCES __S__.sessions ON DELETE RESTRICT,
 token_hash __S__.sha256_hex NOT NULL UNIQUE, issued_at timestamptz NOT NULL,
 expires_at timestamptz NOT NULL, revoked_at timestamptz,
 CHECK(predecessor_id IS DISTINCT FROM id), CHECK(expires_at>issued_at), CHECK(revoked_at IS NULL OR revoked_at>=issued_at),
 UNIQUE(id,principal_id,family_id),
 FOREIGN KEY(predecessor_id,principal_id,family_id) REFERENCES __S__.sessions(id,principal_id,family_id)
);
CREATE TABLE __S__.identity_mappings (
 id uuid PRIMARY KEY, provider text NOT NULL CHECK(length(provider)>0),
 provider_entity_type text NOT NULL CHECK(length(provider_entity_type)>0),
 external_id text NOT NULL CHECK(length(external_id)>0), catalogue_uuid uuid,
 state text NOT NULL CHECK(state IN ('needs_review','resolved')),
 CHECK((state='resolved')=(catalogue_uuid IS NOT NULL)), UNIQUE(provider,provider_entity_type,external_id)
);
-- catalogue_uuid is an external Stage 2/4 catalogue anchor, not a personal opus.db FK.
CREATE TABLE __S__.published_entities (
 id uuid PRIMARY KEY, kind text NOT NULL CHECK(length(kind)>0), revision bigint NOT NULL CHECK(revision>0),
 status text NOT NULL CHECK(status IN ('published','superseded','quarantined')),
 reason text, CHECK(status='published' OR nullif(btrim(reason),'') IS NOT NULL), UNIQUE(id,kind)
);
CREATE TABLE __S__.submissions (
 id uuid PRIMARY KEY, author_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 target_kind text NOT NULL CHECK(length(target_kind)>0), catalogue_uuid uuid,
 mapping_id uuid REFERENCES __S__.identity_mappings ON DELETE RESTRICT,
 target_entity_id uuid, expected_revision bigint NOT NULL CHECK(expected_revision>=0),
 status __S__.submission_state NOT NULL DEFAULT 'submitted',
 current_revision bigint NOT NULL CHECK(current_revision>0),
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), updated_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 CHECK(catalogue_uuid IS NOT NULL OR mapping_id IS NOT NULL), CHECK(updated_at>=created_at),
 FOREIGN KEY(target_entity_id,target_kind) REFERENCES __S__.published_entities(id,kind) ON DELETE RESTRICT
);
CREATE TABLE __S__.submission_revisions (
 submission_id uuid NOT NULL REFERENCES __S__.submissions ON DELETE RESTRICT,
 revision bigint NOT NULL CHECK(revision>0), payload jsonb NOT NULL CHECK(jsonb_typeof(payload)='object'),
 prev_hash __S__.sha256_hex NOT NULL, this_hash __S__.sha256_hex NOT NULL,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), PRIMARY KEY(submission_id,revision)
);
ALTER TABLE __S__.submissions ADD CONSTRAINT current_submission_revision
 FOREIGN KEY(id,current_revision) REFERENCES __S__.submission_revisions(submission_id,revision)
 DEFERRABLE INITIALLY DEFERRED;
CREATE TABLE __S__.published_revisions (
 entity_id uuid NOT NULL REFERENCES __S__.published_entities ON DELETE RESTRICT,
 revision bigint NOT NULL CHECK(revision>0), submission_id uuid NOT NULL,
 submission_revision bigint NOT NULL, actor_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 payload jsonb NOT NULL CHECK(jsonb_typeof(payload)='object'), this_hash __S__.sha256_hex NOT NULL,
 source_evidence jsonb NOT NULL CHECK(jsonb_typeof(source_evidence)='array'),
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), PRIMARY KEY(entity_id,revision),
 FOREIGN KEY(submission_id,submission_revision) REFERENCES __S__.submission_revisions ON DELETE RESTRICT,
 UNIQUE(submission_id,submission_revision)
);
ALTER TABLE __S__.published_entities ADD CONSTRAINT current_published_revision
 FOREIGN KEY(id,revision) REFERENCES __S__.published_revisions(entity_id,revision)
 DEFERRABLE INITIALLY DEFERRED;
CREATE TABLE __S__.audit_head (
 singleton boolean PRIMARY KEY DEFAULT true CHECK(singleton), last_seq bigint NOT NULL CHECK(last_seq>=0),
 last_hash __S__.sha256_hex NOT NULL, CHECK(last_seq<>0 OR last_hash=repeat('0',64))
);
INSERT INTO __S__.audit_head(singleton,last_seq,last_hash) VALUES(true,0,repeat('0',64));
CREATE TABLE __S__.audit_events (
 seq bigint PRIMARY KEY CHECK(seq>0), prev_hash __S__.sha256_hex NOT NULL, this_hash __S__.sha256_hex NOT NULL,
 actor_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 action text NOT NULL CHECK(length(action)>0), target_kind text NOT NULL CHECK(length(target_kind)>0),
 target_id uuid NOT NULL, target_revision bigint CHECK(target_revision>0),
 before_hash __S__.sha256_hex, after_hash __S__.sha256_hex, reason text,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(), request_id uuid NOT NULL,
 canonical_content text NOT NULL,
 submission_target uuid GENERATED ALWAYS AS (CASE WHEN target_kind='submission' THEN target_id END) STORED,
 entity_target uuid GENERATED ALWAYS AS (CASE WHEN target_kind='entity' THEN target_id END) STORED,
 principal_target uuid GENERATED ALWAYS AS (CASE WHEN target_kind='principal' THEN target_id END) STORED REFERENCES __S__.principals ON DELETE RESTRICT,
 CHECK(target_kind IN ('submission','entity','principal')),
 CHECK((target_kind='principal' AND target_revision IS NULL) OR (target_kind IN ('submission','entity') AND target_revision IS NOT NULL)),
 FOREIGN KEY(submission_target,target_revision) REFERENCES __S__.submission_revisions ON DELETE RESTRICT,
 FOREIGN KEY(entity_target,target_revision) REFERENCES __S__.published_revisions ON DELETE RESTRICT
);
CREATE TABLE __S__.review_events (
 id uuid PRIMARY KEY, submission_id uuid NOT NULL, submission_revision bigint NOT NULL,
 actor_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 from_status __S__.submission_state NOT NULL, to_status __S__.submission_state NOT NULL,
 reason text, audit_seq bigint NOT NULL UNIQUE REFERENCES __S__.audit_events ON DELETE RESTRICT,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(submission_id,submission_revision) REFERENCES __S__.submission_revisions ON DELETE RESTRICT,
 CHECK(from_status<>to_status),
 CHECK(to_status NOT IN ('rejected','changes_requested','superseded','quarantined') OR nullif(btrim(reason),'') IS NOT NULL)
);
CREATE TABLE __S__.change_feed (
 seq bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 entity_id uuid NOT NULL, revision bigint NOT NULL,
 status text NOT NULL CHECK(status IN ('published','superseded','quarantined')),
 reason text, audit_seq bigint NOT NULL REFERENCES __S__.audit_events ON DELETE RESTRICT,
 created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
 FOREIGN KEY(entity_id,revision) REFERENCES __S__.published_revisions ON DELETE RESTRICT,
 CHECK(status='published' OR nullif(btrim(reason),'') IS NOT NULL)
);
CREATE TABLE __S__.idempotency_results (
 principal_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 key uuid NOT NULL, request_hash __S__.sha256_hex NOT NULL,
 http_status integer NOT NULL CHECK(http_status BETWEEN 200 AND 299), response jsonb NOT NULL,
 created_at timestamptz NOT NULL, expires_at timestamptz NOT NULL,
 PRIMARY KEY(principal_id,key), CHECK(expires_at>=created_at+interval '24 hours')
);
-- Expiry is eligibility for key reuse, not a contribution/audit retention policy. No TTL deletion job.
CREATE TABLE __S__.abuse_limits (
 scope text NOT NULL CHECK(scope IN ('principal','coarse_ip')), scope_key text NOT NULL,
 principal_id uuid REFERENCES __S__.principals ON DELETE RESTRICT,
 window_start timestamptz NOT NULL, window_end timestamptz NOT NULL, count bigint NOT NULL CHECK(count>=0),
 PRIMARY KEY(scope,scope_key,window_start), CHECK(window_end>window_start),
 CHECK((scope='principal' AND principal_id IS NOT NULL AND scope_key=principal_id::text)
    OR (scope='coarse_ip' AND principal_id IS NULL AND scope_key ~ '^[0-9a-f]{64}$'))
);
