-- SQL-only smoke checks on the disposable PostgreSQL 17 cluster.
-- Does not execute JavaScript services or prove concurrent request behavior.
BEGIN;
INSERT INTO proto_stage679_single.principals(id,iss,sub) VALUES
 ('10000000-0000-4000-8000-000000000001','https://identity.synthetic.invalid','synthetic-author'),
 ('20000000-0000-4000-8000-000000000001','https://identity.synthetic.invalid','synthetic-admin');
DO $$
DECLARE i integer; sid uuid; entity uuid := '40000000-0000-4000-8000-000000000001'; p jsonb; a bigint; f record; n integer; before_hash text; after_hash text;
BEGIN
 IF (SELECT enabled FROM proto_stage679_single.intake_control WHERE singleton) IS DISTINCT FROM false THEN RAISE EXCEPTION 'kill switch must default off'; END IF;
 FOR i IN 1..2 LOOP
  sid:=CASE WHEN i=1 THEN '30000000-0000-4000-8000-000000000001'::uuid ELSE '30000000-0000-4000-8000-000000000002'::uuid END;
  p:=jsonb_build_object('schemaVersion',1,'target',jsonb_build_object('kind','work','catalogueUuid',entity),'changeKind','new_metadata','intendedLicense','CC0-1.0','contributorNote','PRIVATE synthetic note','fields',jsonb_build_array(jsonb_build_object('field','title','newValue','Synthetic permitted title '||i,'evidence',jsonb_build_array(jsonb_build_object('type','reference','value','Explicitly permitted synthetic fixture','license','CC0-1.0')))));
  INSERT INTO proto_stage679_single.submissions(id,author_id,target_kind,catalogue_uuid,expected_revision,current_revision) VALUES(sid,'10000000-0000-4000-8000-000000000001','work',entity,i-1,1);
  INSERT INTO proto_stage679_single.submission_revisions(submission_id,revision,payload,prev_hash) VALUES(sid,1,p,repeat('0',64));
  IF i=1 THEN INSERT INTO proto_stage679_single.published_entities(id,kind,revision,status) VALUES(entity,'work',1,'published'); END IF;
  INSERT INTO proto_stage679_single.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence) VALUES(entity,i,sid,1,'20000000-0000-4000-8000-000000000001',p,jsonb_path_query_array(p,'$.fields[*].evidence[*]'));
  UPDATE proto_stage679_single.published_entities SET revision=i WHERE id=entity;
  FOR f IN SELECT * FROM (VALUES('submitted','in_review'),('in_review','approved'),('approved','published')) AS t(old_state,new_state) LOOP
   a:=proto_stage679_single.append_audit('20000000-0000-4000-8000-000000000001',f.old_state||'->'||f.new_state,'submission',sid,1,'90000000-0000-4000-8000-000000000001');
   INSERT INTO proto_stage679_single.review_events(id,submission_id,submission_revision,actor_id,from_status,to_status,audit_seq) VALUES(gen_random_uuid(),sid,1,'20000000-0000-4000-8000-000000000001',f.old_state,f.new_state,a);
   UPDATE proto_stage679_single.submissions SET status=f.new_state WHERE id=sid;
  END LOOP;
  INSERT INTO proto_stage679_single.change_feed(entity_id,revision,status,audit_seq) VALUES(entity,i,'published',a);
 END LOOP;
 IF (SELECT array_agg(event_kind ORDER BY seq) FROM proto_stage679_single.change_feed) <> ARRAY['publication','supersession'] THEN RAISE EXCEPTION 'supersession classification'; END IF;
 SELECT this_hash INTO before_hash FROM proto_stage679_single.published_revisions WHERE entity_id=entity AND revision=2;
 SELECT this_hash INTO after_hash FROM proto_stage679_single.published_revisions WHERE entity_id=entity AND revision=1;
 UPDATE proto_stage679_single.published_entities SET revision=1,status='published',reason='PRIVATE rollback reason' WHERE id=entity;
 a:=proto_stage679_single.append_audit('20000000-0000-4000-8000-000000000001','admin.rollback','entity',entity,1,'90000000-0000-4000-8000-000000000001','PRIVATE rollback reason',before_hash,after_hash);
 INSERT INTO proto_stage679_single.change_feed(entity_id,revision,status,audit_seq,event_kind) VALUES(entity,1,'published',a,'rollback');
 UPDATE proto_stage679_single.published_entities SET status='quarantined',reason='PRIVATE quarantine reason' WHERE id=entity;
 a:=proto_stage679_single.append_audit('20000000-0000-4000-8000-000000000001','admin.quarantine','entity',entity,1,'90000000-0000-4000-8000-000000000001','PRIVATE quarantine reason');
 INSERT INTO proto_stage679_single.change_feed(entity_id,revision,status,reason,audit_seq) VALUES(entity,1,'quarantined','PRIVATE quarantine reason',a);
 IF (SELECT count(*) FROM proto_stage679_single.published_entities WHERE status='published')<>0 THEN RAISE EXCEPTION 'quarantine projection'; END IF;
 IF (SELECT count(*) FROM proto_stage679_single.published_revisions)<>2 THEN RAISE EXCEPTION 'history lost'; END IF;
 IF (SELECT array_agg(event_kind ORDER BY seq) FROM proto_stage679_single.change_feed) <> ARRAY['publication','supersession','rollback','quarantine'] THEN RAISE EXCEPTION 'feed kind order'; END IF;
 BEGIN UPDATE proto_stage679_single.published_revisions SET payload='{}'; RAISE EXCEPTION 'history writable'; EXCEPTION WHEN SQLSTATE '55000' THEN NULL; END;
 BEGIN DELETE FROM proto_stage679_single.audit_events; RAISE EXCEPTION 'audit deletable'; EXCEPTION WHEN SQLSTATE '55000' THEN NULL; END;
 FOR i IN 1..3 LOOP
  INSERT INTO proto_stage679_single.abuse_limits(scope,scope_key,principal_id,window_start,window_end,count)
   VALUES('principal','10000000-0000-4000-8000-000000000001','10000000-0000-4000-8000-000000000001','2030-01-01T00:00:00Z','2030-01-02T00:00:00Z',1)
   ON CONFLICT(scope,scope_key,window_start) DO UPDATE SET count=proto_stage679_single.abuse_limits.count+1 WHERE proto_stage679_single.abuse_limits.count<2 RETURNING count INTO a;
  GET DIAGNOSTICS n=ROW_COUNT;
  IF (i<=2 AND n<>1) OR (i=3 AND n<>0) THEN RAISE EXCEPTION 'quota boundary'; END IF;
 END LOOP;
 BEGIN
  UPDATE proto_stage679_single.abuse_limits SET count=count+1;
  RAISE EXCEPTION 'synthetic transaction fault';
 EXCEPTION WHEN raise_exception THEN NULL;
 END;
 IF (SELECT count FROM proto_stage679_single.abuse_limits)<>2 THEN RAISE EXCEPTION 'counter rollback'; END IF;
 IF EXISTS(SELECT 1 FROM proto_stage679_single.audit_events WHERE this_hash<>encode(sha256(convert_to(prev_hash||':'||proto_stage679_single.audit_content(audit_events),'UTF8')),'hex')) THEN RAISE EXCEPTION 'audit hash mismatch'; END IF;
END $$;
COMMIT;
SELECT 'PASS: migration SQL, default-off kill switch, immutable history, supersession/rollback/quarantine events, quota boundary and counter rollback; no concurrency claim' AS result;
