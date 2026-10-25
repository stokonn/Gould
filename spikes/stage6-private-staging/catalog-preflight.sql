-- Read-only metadata only. No rows, credentials or hosted objects are changed.
SELECT jsonb_build_object(
 'server_version',current_setting('server_version'),
 'pg17',current_setting('server_version_num')::integer BETWEEN 170000 AND 179999,
 'ssl_server_enabled',current_setting('ssl')='on',
 'external_ssl_enforced',NULL,
 'api_exposed_schemas_observed',current_setting('pgrst.db_schemas',true),
 'private_schema_absent',to_regnamespace('goldberg_private') IS NULL,
 'application_tables',(SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
   WHERE n.nspname IN ('public','goldberg_private') AND c.relkind IN ('r','p')),
 'provider_roles_available',(SELECT count(*)=3 FROM pg_roles WHERE rolname IN ('anon','authenticated','service_role')),
 'postgres_can_create_schema',has_database_privilege('postgres',current_database(),'CREATE'),
 'postgres_can_create_role',(SELECT rolcreaterole FROM pg_roles WHERE rolname='postgres'),
 'runtime_role_absent',NOT EXISTS(SELECT 1 FROM pg_roles WHERE rolname='goldberg_staging_probe'),
 'function',(SELECT jsonb_build_object(
   'owner',pg_get_userbyid(p.proowner),'security_definer',p.prosecdef,'config',p.proconfig,
   'acl',p.proacl::text,'anon_execute',has_function_privilege('anon',p.oid,'EXECUTE'),
   'authenticated_execute',has_function_privilege('authenticated',p.oid,'EXECUTE'),
   'definition_sha256',encode(sha256(convert_to(pg_get_functiondef(p.oid),'UTF8')),'hex'),
   'guard_matches',p.prosecdef AND pg_get_userbyid(p.proowner)='postgres'
     AND p.proconfig=ARRAY['search_path=pg_catalog']
     AND p.prorettype='pg_catalog.event_trigger'::regtype
     AND EXISTS(SELECT 1 FROM pg_event_trigger t WHERE t.evtname='ensure_rls'
       AND t.evtfoid=p.oid AND t.evtenabled='O' AND t.evtevent='ddl_command_end'
       AND t.evttags @> ARRAY['CREATE TABLE','CREATE TABLE AS','SELECT INTO']::text[]
       AND cardinality(t.evttags)=3))
   FROM pg_proc p WHERE p.oid=to_regprocedure('public.rls_auto_enable()')),
 'public_definer_functions',(SELECT coalesce(jsonb_agg(jsonb_build_object(
   'name',p.proname,'anon_execute',has_function_privilege('anon',p.oid,'EXECUTE'),
   'authenticated_execute',has_function_privilege('authenticated',p.oid,'EXECUTE'))),'[]'::jsonb)
   FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace WHERE n.nspname='public' AND p.prosecdef)
);
