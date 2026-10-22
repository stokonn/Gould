-- Local Auth extension; original 001/002 migrations and ledger stay unchanged.
CREATE TABLE __S__.auth_families (
 id uuid PRIMARY KEY, principal_id uuid NOT NULL REFERENCES __S__.principals ON DELETE RESTRICT,
 created_at timestamptz NOT NULL, expires_at timestamptz NOT NULL, revoked_at timestamptz,
 CHECK(expires_at>created_at), CHECK(revoked_at IS NULL OR revoked_at>=created_at), UNIQUE(id,principal_id)
);
-- Preserve seeded base sessions; incompatible cross-principal family IDs fail atomically.
INSERT INTO __S__.auth_families(id,principal_id,created_at,expires_at,revoked_at)
 SELECT family_id,principal_id,min(issued_at),max(expires_at),NULL FROM __S__.sessions GROUP BY family_id,principal_id;
ALTER TABLE __S__.sessions ADD CONSTRAINT session_auth_family
 FOREIGN KEY(family_id,principal_id) REFERENCES __S__.auth_families(id,principal_id) ON DELETE RESTRICT;
CREATE TABLE __S__.auth_challenges (
 token_hash __S__.sha256_hex PRIMARY KEY, created_at timestamptz NOT NULL,
 expires_at timestamptz NOT NULL, consumed_at timestamptz,
 CHECK(expires_at>created_at), CHECK(consumed_at IS NULL OR consumed_at>=created_at)
);
CREATE TABLE __S__.auth_refresh (
 token_hash __S__.sha256_hex PRIMARY KEY, session_id uuid NOT NULL UNIQUE,
 principal_id uuid NOT NULL, family_id uuid NOT NULL, created_at timestamptz NOT NULL,
 expires_at timestamptz NOT NULL, consumed_at timestamptz,
 CHECK(expires_at>created_at), CHECK(consumed_at IS NULL OR consumed_at>=created_at),
 FOREIGN KEY(session_id,principal_id,family_id) REFERENCES __S__.sessions(id,principal_id,family_id) ON DELETE RESTRICT
);
CREATE INDEX auth_family_sessions ON __S__.sessions(family_id);
CREATE INDEX auth_family_refresh ON __S__.auth_refresh(family_id);
CREATE TRIGGER family_revocation BEFORE UPDATE ON __S__.auth_families FOR EACH ROW EXECUTE FUNCTION __S__.revocation_only();
CREATE FUNCTION __S__.consume_only() RETURNS trigger LANGUAGE plpgsql SET search_path=pg_catalog AS $$
BEGIN
 IF (to_jsonb(NEW)-'consumed_at') IS DISTINCT FROM (to_jsonb(OLD)-'consumed_at')
 OR (OLD.consumed_at IS NOT NULL AND NEW.consumed_at IS DISTINCT FROM OLD.consumed_at) THEN
  RAISE EXCEPTION 'only first consumption allowed' USING ERRCODE='55000'; END IF;
 RETURN NEW;
END; $$;
CREATE TRIGGER challenge_consumption BEFORE UPDATE ON __S__.auth_challenges FOR EACH ROW EXECUTE FUNCTION __S__.consume_only();
CREATE TRIGGER refresh_consumption BEFORE UPDATE ON __S__.auth_refresh FOR EACH ROW EXECUTE FUNCTION __S__.consume_only();
REVOKE ALL ON ALL TABLES IN SCHEMA __S__ FROM PUBLIC;
REVOKE ALL ON ALL FUNCTIONS IN SCHEMA __S__ FROM PUBLIC;
