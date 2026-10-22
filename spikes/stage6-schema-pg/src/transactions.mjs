// LOCAL PROTOTYPE transaction exerciser, not an HTTP endpoint or hosted auth implementation.
import {randomUUID} from 'node:crypto';
import {identifier,safeInt8} from './database.mjs';
export class Conflict extends Error {status=409;}
export class Mismatch extends Error {status=422;}
export async function appendAudit(c,s,{actor,action='synthetic_decision',kind='submission',target,revision=1,request=randomUUID(),reason=null}) {
  const {rows:[row]}=await c.query(`SELECT ${identifier(s)}.append_audit($1,$2,$3,$4,$5,$6,$7) AS seq`,[actor,action,kind,target,revision===null?null:String(revision),request,reason]);
  return row.seq;
}
export async function storedResult(c,s,principal,key,hash,now) {
  const q=identifier(s);const {rows:[row]}=await c.query(`SELECT * FROM ${q}.idempotency_results WHERE principal_id=$1 AND key=$2 FOR UPDATE`,[principal,key]);
  if(!row || row.expires_at<=now) return null;
  if(row.request_hash!==hash) throw new Mismatch('idempotency_mismatch');
  return {status:row.http_status,body:row.response};
}
export async function insertResult(c,s,{principal,key,hash,status=201,body,now=new Date()}) {
  const q=identifier(s);
  await c.query('SAVEPOINT idem_insert');
  try {
    await c.query(`INSERT INTO ${q}.idempotency_results(principal_id,key,request_hash,http_status,response,created_at,expires_at)
      VALUES($1,$2,$3,$4,$5,$6,$6::timestamptz+interval '24 hours')`,[principal,key,hash,status,body,now]);
    await c.query('RELEASE SAVEPOINT idem_insert');return {stored:true,status,body};
  }catch(error){
    await c.query('ROLLBACK TO SAVEPOINT idem_insert');await c.query('RELEASE SAVEPOINT idem_insert');
    if(error.code!=='23505' || error.constraint!=='idempotency_results_pkey') throw error;
    const replay=await storedResult(c,s,principal,key,hash,now);
    if(!replay) throw new Conflict('expired key requires transaction-level locked reuse');
    return {stored:false,...replay};
  }
}
export async function transition(c,s,{submission,revision,actor,from,to,reason=null}) {
  const q=identifier(s);
  const seq=await appendAudit(c,s,{actor,action:`${from}->${to}`,target:submission,revision,reason});
  await c.query(`INSERT INTO ${q}.review_events(id,submission_id,submission_revision,actor_id,from_status,to_status,reason,audit_seq)
    VALUES($1,$2,$3,$4,$5,$6,$7,$8)`,[randomUUID(),submission,String(revision),actor,from,to,reason,seq]);
  await c.query(`UPDATE ${q}.submissions SET status=$2,current_revision=$3,updated_at=clock_timestamp() WHERE id=$1`,[submission,to,String(revision)]);
  return seq;
}
export async function publishSynthetic(c,s,{submission,actor,key,hash,expectedSubmissionRevision,expectedEntityRevision,failAt,now=new Date()}) {
  for(const value of [expectedSubmissionRevision,expectedEntityRevision]) {
    if(typeof value==='number' && !Number.isSafeInteger(value)) throw new RangeError('unsafe expected revision');
    safeInt8(String(value));
  }
  const q=identifier(s); const checkpoint=name=>{if(failAt===name) throw new Error(`injected:${name}`);};
  await c.query('BEGIN');
  try {
    await c.query("SET LOCAL lock_timeout='2s'");
    await c.query("SET LOCAL statement_timeout='5s'");
    // One global order: idempotency key, submission, entity, audit_head. All are transaction locks.
    await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`idempotency:${s}:${actor}:${key}`]);
    const replay=await storedResult(c,s,actor,key,hash,now);
    if(replay){await c.query('COMMIT');return {...replay,replayed:true};}
    await c.query(`DELETE FROM ${q}.idempotency_results WHERE principal_id=$1 AND key=$2 AND expires_at<=$3`,[actor,key,now]);
    const {rows:[sub]}=await c.query(`SELECT * FROM ${q}.submissions WHERE id=$1 FOR UPDATE`,[submission]);
    if(!sub || sub.status!=='in_review' || sub.current_revision!==String(expectedSubmissionRevision)) throw new Conflict('submission conflict');
    const {rows:[authorization]}=await c.query(`SELECT 1 FROM ${q}.principals p JOIN ${q}.role_grants g ON g.principal_id=p.id
      WHERE p.id=$1 AND p.state='active' AND g.role IN ('moderator','administrator') AND g.revoked_at IS NULL`,[actor]);
    if(!authorization || actor===sub.author_id) throw new Error('synthetic actor not authorized');
    if(!sub.catalogue_uuid) throw new Error('unresolved external mapping is not publishable in this prototype');
    const entity=sub.catalogue_uuid;
    await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`entity:${s}:${entity}`]);
    const {rows:[current]}=await c.query(`SELECT * FROM ${q}.published_entities WHERE id=$1 FOR UPDATE`,[entity]);
    if(BigInt(current?.revision??'0')!==BigInt(expectedEntityRevision)) throw new Conflict('entity conflict');
    const next=BigInt(current?.revision??'0')+1n;
    // Validate BEFORE commit; never commit a result that cannot be represented by the integer envelope.
    const numericRevision=safeInt8(next);
    checkpoint('locked');
    const {rows:[snapshot]}=await c.query(`SELECT payload FROM ${q}.submission_revisions WHERE submission_id=$1 AND revision=$2`,[submission,sub.current_revision]);
    if(!current) await c.query(`INSERT INTO ${q}.published_entities(id,kind,revision,status) VALUES($1,$2,$3,'published')`,[entity,sub.target_kind,String(next)]);
    await c.query(`INSERT INTO ${q}.published_revisions(entity_id,revision,submission_id,submission_revision,actor_id,payload,source_evidence)
      VALUES($1,$2,$3,$4,$5,$6,jsonb_path_query_array($6::jsonb,'$.fields[*].evidence[*]'))`,[entity,String(next),submission,sub.current_revision,actor,snapshot.payload]);
    await c.query(`UPDATE ${q}.published_entities SET revision=$2,status='published',reason=NULL WHERE id=$1`,[entity,String(next)]);
    checkpoint('publication');
    await transition(c,s,{submission,revision:sub.current_revision,actor,from:'in_review',to:'approved'});
    checkpoint('approved');
    const seq=await transition(c,s,{submission,revision:sub.current_revision,actor,from:'approved',to:'published'});
    checkpoint('audit');
    await c.query(`INSERT INTO ${q}.change_feed(entity_id,revision,status,audit_seq) VALUES($1,$2,'published',$3)`,[entity,String(next),seq]);
    checkpoint('feed');
    const result=await insertResult(c,s,{principal:actor,key,hash,body:{entityId:entity,revision:numericRevision},now});
    checkpoint('idempotency');
    if(!result.stored){await c.query('ROLLBACK');return {...result,replayed:true};}
    await c.query('COMMIT');return {status:result.status,body:result.body,replayed:false};
  }catch(error){await c.query('ROLLBACK');throw error;}
}
