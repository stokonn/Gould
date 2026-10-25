import {identifier} from '../../spikes/stage6-schema-pg/src/database.mjs';

// Explicit server-only privileges, never ALL TABLES/FUNCTIONS or default grants.
// RLS is defense in depth against client roles. BFF ownership/live-role checks
// remain authoritative for requests; a database credential is never a user token.
export const runtimePrivileges=Object.freeze({
 principals:['SELECT','INSERT','UPDATE'],role_grants:['SELECT','INSERT','UPDATE'],
 sessions:['SELECT','INSERT','UPDATE'],auth_families:['SELECT','INSERT','UPDATE'],
 auth_challenges:['SELECT','INSERT','UPDATE'],auth_refresh:['SELECT','INSERT','UPDATE'],
 identity_mappings:['SELECT','INSERT','UPDATE'],submissions:['SELECT','INSERT','UPDATE'],
 submission_revisions:['SELECT','INSERT'],published_entities:['SELECT','INSERT','UPDATE'],
 published_revisions:['SELECT','INSERT'],review_events:['SELECT','INSERT'],
 audit_head:['SELECT','UPDATE'],audit_events:['SELECT','INSERT'],change_feed:['SELECT','INSERT'],
 idempotency_results:['SELECT','INSERT','DELETE'],abuse_limits:['SELECT','INSERT','UPDATE','DELETE'],
 // SELECT FOR SHARE needs UPDATE privilege. Column-scoped UPDATE on singleton
 // permits the lock, without granting permission to change the enabled switch.
 intake_control:['SELECT'],
});

/** Returns a reviewable transaction. Never connects, creates a role, changes
 * credentials, runs bootstrap or modifies existing application rows. The role
 * must be freshly provisioned separately, have no inherited roles/ownership and
 * no elevated flags. Only the schema owner's non-inheriting, non-SET creator
 * administration is allowed (PostgreSQL 17 non-superuser CREATE ROLE behavior).
 * Replay intentionally fails on existing policies. */
export function runtimeAccessSql({schema='goldberg_private',role='goldberg_bff_runtime'}={}) {
 const q=identifier(schema);
 if(!/^goldberg_bff_[a-z0-9_]{1,32}$/.test(role))throw new TypeError('restricted BFF role required');
 const r='"'+role+'"';
 const statements=['BEGIN;',"SET LOCAL lock_timeout='2s';","SET LOCAL statement_timeout='5s';",
 `DO $guard$ DECLARE r pg_roles; BEGIN
 SELECT * INTO STRICT r FROM pg_roles WHERE rolname='${role}';
 IF r.rolsuper OR r.rolcreaterole OR r.rolcreatedb OR r.rolreplication OR r.rolbypassrls
 OR EXISTS(SELECT 1 FROM pg_auth_members m WHERE m.member=r.oid OR
  (m.roleid=r.oid AND NOT(m.member=(SELECT nspowner FROM pg_namespace WHERE nspname='${schema}')
   AND m.admin_option AND NOT m.inherit_option AND NOT m.set_option)))
 OR EXISTS(SELECT 1 FROM pg_class WHERE relowner=r.oid)
 OR EXISTS(SELECT 1 FROM pg_namespace WHERE nspowner=r.oid)
 OR EXISTS(SELECT 1 FROM pg_proc WHERE proowner=r.oid)
 THEN RAISE EXCEPTION 'runtime role must be unprivileged, unowned and have no memberships'; END IF;
 END $guard$;`,
 `GRANT USAGE ON SCHEMA ${q} TO ${r};`];
 for(const [table,permissions] of Object.entries(runtimePrivileges)) {
  statements.push(`ALTER TABLE ${q}.${table} ENABLE ROW LEVEL SECURITY;`);
  statements.push(`GRANT ${permissions.join(',')} ON ${q}.${table} TO ${r};`);
  for(const permission of permissions)statements.push(`CREATE POLICY bff_${permission.toLowerCase()} ON ${q}.${table} FOR ${permission} TO ${r} ${permission==='INSERT'?'WITH CHECK (true)':permission==='UPDATE'?'USING (true) WITH CHECK (true)':'USING (true)'};`);
 }
 statements.push(`GRANT UPDATE(singleton) ON ${q}.intake_control TO ${r};`,
 `CREATE POLICY bff_lock ON ${q}.intake_control FOR UPDATE TO ${r} USING (true) WITH CHECK (singleton);`,
 // storedResult uses SELECT FOR UPDATE; append-only triggers still reject any
 // actual UPDATE of a persisted receipt. Never grant UPDATE on response/hash.
 `GRANT UPDATE(key) ON ${q}.idempotency_results TO ${r};`,
 `CREATE POLICY bff_lock ON ${q}.idempotency_results FOR UPDATE TO ${r} USING (true) WITH CHECK (true);`,
 `GRANT EXECUTE ON FUNCTION ${q}.append_audit(uuid,text,text,uuid,bigint,uuid,text,text,text) TO ${r};`,
 'COMMIT;');
 return statements.join('\n')+'\n';
}
