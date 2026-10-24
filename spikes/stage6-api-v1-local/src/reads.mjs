// Local approved read DTOs. Authentication locks remain held while a separate,
// bounded READ ONLY snapshot is read; no historical native service is changed.
import {Authorization} from '../../stage6-auth-local/src/authorization.mjs';
import {unavailable} from '../../stage6-auth-local/src/errors.mjs';
import {id,readRevision,timestamp} from './wire.mjs';
import {pragueWindow} from './statistics.mjs';
import {text} from '../../stage6-handlers-local/src/validation.mjs';
const stamp=c=>`to_char(${c} AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"')`;
const hash=v=>{if(typeof v!=='string'||!/^[0-9a-f]{64}$/.test(v))throw unavailable();return v;};
const filters=(query,keys)=>Object.fromEntries(keys.filter(k=>query[k]!==undefined).map(k=>[k,k==='q'?query[k].normalize('NFC'):query[k]]));
// strpos, rather than LIKE, makes % and _ literal. PostgreSQL performs NFC and
// case folding on both sides. Missing/unapproved display metadata stays null.
const contains=(column,param)=>`strpos(lower(normalize(${column},NFC)),lower(normalize(${param}::text,NFC)))>0`;
const wireTime=ms=>timestamp(new Date(ms).toISOString(),{authMilliseconds:true});
async function snapshot(adapter,token,capability,fn){
 return adapter.authorization.require(token,capability,async(auth,ctx)=>{
  let c;
  try{
   c=await adapter.sessions.connect();
   await c.query('BEGIN ISOLATION LEVEL REPEATABLE READ READ ONLY');
   await c.query("SET LOCAL statement_timeout='5s'");
   const result=await fn(c,ctx,auth);await c.query('COMMIT');return result;
  }catch(e){if(c)await c.query('ROLLBACK').catch(()=>{});throw e;}
  finally{if(c)await c.end();}
 });
}
function paging(adapter,scope,ctx,query,keys){
 const f=filters(query,keys),order=scope==='queue'?'createdAt,id':'seq';
 const cursor=query.cursor?adapter.cursors.read(query.cursor,{scope,principalId:ctx.principalId,filters:f,order}):null;
 // Lifetime is fixed for the entire walk, rather than renewed on every page.
 const issuedAt=cursor?.issuedAt??wireTime(adapter.clock());
 return {cursor,sign:(after,extra={})=>adapter.cursors.sign({version:1,scope,principalId:ctx.principalId,filters:f,order,issuedAt,expiresAt:cursor?.expiresAt??wireTime(Date.parse(issuedAt)+900000),after,...extra})};
}
export async function queuePage(adapter,token,query,requestId){
 return snapshot(adapter,token,'queue.read',async(c,ctx)=>{
  const {cursor,sign}=paging(adapter,'queue',ctx,query,['status','kind','q']);
  const q=adapter.authorization.q,limit=Number(query.limit);
  // Bound candidate headers before payload/evidence joins. Measured5000-row
  // default/keyset plans otherwise hash-joined every revision before LIMIT.
  // All filter/keyset predicates remain before this inner limit; FK-backed
  // revision pointers and the same statement snapshot preserve page semantics.
  const {rows}=await c.query(`SELECT s.id,s.author_id,s.status,s.target_kind,
   s.expected_revision::text,s.current_revision::text,${stamp('s.created_at')} AS created_at,
   ${stamp('s.updated_at')} AS updated_at,
   (jsonb_typeof(r.payload->'fields')='array' AND NOT EXISTS
    (SELECT 1 FROM jsonb_array_elements(r.payload->'fields') f WHERE jsonb_typeof(f->'evidence') IS DISTINCT FROM 'array')) AS evidence_valid,
   (SELECT count(*)::text FROM jsonb_array_elements(r.payload->'fields') f,
    LATERAL jsonb_array_elements(f->'evidence') e) AS evidence_count
   FROM (SELECT s.* FROM ${q}.submissions s
   WHERE s.status IN ('submitted','in_review','changes_requested')
    AND ($1::text IS NULL OR s.status::text=$1) AND ($2::text IS NULL OR s.target_kind=$2)
    AND ($3::text IS NULL OR s.id::text=$3 OR ${contains('s.target_kind','$3')})
    AND ($4::timestamptz IS NULL OR (s.created_at,s.id)>($4::timestamptz,$5::uuid))
   ORDER BY s.created_at,s.id LIMIT $6) s
   JOIN ${q}.submission_revisions r ON r.submission_id=s.id AND r.revision=s.current_revision
   ORDER BY s.created_at,s.id LIMIT $6`,[query.status??null,query.kind??null,query.q?.normalize('NFC')??null,cursor?.after.createdAt??null,cursor?.after.id??null,limit+1]);
  const items=rows.slice(0,limit).map(s=>{
   if(s.evidence_valid!==true||!['work','arrangement','artist','album','release','recording','collection'].includes(s.target_kind))throw unavailable();
   return {submissionId:id(s.id),authorId:id(s.author_id),status:s.status,kind:s.target_kind,
    createdAt:timestamp(s.created_at),updatedAt:timestamp(s.updated_at),
    expectedEntityRevision:readRevision(s.expected_revision),submissionRevision:readRevision(s.current_revision,{positive:true}),
    evidenceCount:readRevision(s.evidence_count),display:{title:null,composer:null}};
  });
  const last=items.at(-1);
  return {schemaVersion:1,requestId,items,nextCursor:rows.length>limit?sign({createdAt:last.createdAt,id:last.submissionId}):null,coverage:'page',searchScope:'id_title_composer_kind'};
 });
}
export async function auditPage(adapter,token,query,requestId){
 return snapshot(adapter,token,'audit.read',async(c,ctx)=>{
  const {cursor,sign}=paging(adapter,'audit',ctx,query,['action','actorId','subjectId','from','to','q']);
  const q=adapter.authorization.q,limit=Number(query.limit);
  const {rows:[head]}=await c.query(`SELECT last_seq::text,last_hash FROM ${q}.audit_head WHERE singleton`);
  const highwater=readRevision(cursor?.highwater??head.last_seq),headHash=hash(cursor?.headHash??head.last_hash);
  const {rows}=await c.query(`SELECT seq::text,${stamp('created_at')} AS created_at,action,actor_id,target_kind,target_id,target_revision::text,
   before_hash,after_hash,reason,request_id,prev_hash,this_hash,canonical_content
   FROM ${q}.audit_events WHERE seq>$1::bigint AND seq<=$2::bigint
    AND ($3::text IS NULL OR action=$3) AND ($4::uuid IS NULL OR actor_id=$4)
    AND ($5::uuid IS NULL OR target_id=$5) AND ($6::timestamptz IS NULL OR created_at>=$6)
    AND ($7::timestamptz IS NULL OR created_at<$7)
    AND ($8::text IS NULL OR ${contains('action','$8')} OR ${contains('target_id::text','$8')} OR ${contains("coalesce(reason,'')",'$8')})
   ORDER BY audit_events.seq LIMIT $9`,[cursor?.after.seq??'0',highwater,query.action??null,query.actorId??null,query.subjectId??null,query.from??null,query.to??null,query.q?.normalize('NFC')??null,limit+1]);
  const items=rows.slice(0,limit).map(e=>{
   if(typeof e.action!=='string'||!e.action.length||Buffer.byteLength(e.action)>200||typeof e.canonical_content!=='string'||Buffer.byteLength(e.canonical_content)>16384)throw unavailable();
   return {seq:readRevision(e.seq,{positive:true}),createdAt:timestamp(e.created_at),action:e.action,
    actor:{principalId:e.actor_id===null?null:id(e.actor_id)},
    subject:{kind:e.target_kind,id:id(e.target_id),revision:e.target_revision===null?null:readRevision(e.target_revision,{positive:true})},
    beforeHash:e.before_hash===null?null:hash(e.before_hash),afterHash:e.after_hash===null?null:hash(e.after_hash),reason:e.reason===null?null:text(e.reason),
    requestId:id(e.request_id),prevHash:hash(e.prev_hash),thisHash:hash(e.this_hash),canonicalContent:e.canonical_content};
  });
  return {schemaVersion:1,requestId,items,nextCursor:rows.length>limit?sign({seq:items.at(-1).seq},{highwater,headHash}):null,highwater,headHash,
   coverage:Object.keys(filters(query,['action','actorId','subjectId','from','to','q'])).length?'filtered':'chain'};
 });
}
export async function statistics(adapter,token,requestId){
 return snapshot(adapter,token,'queue.read',async(c,ctx,auth)=>{
  // Reuse the locked authentication transaction, never acquire a nested session.
  const bound={q:adapter.sessions.q,withSession:(_token,fn)=>fn(auth,ctx)};
  await new Authorization({sessions:bound,clock:adapter.clock}).require(token,'audit.read');
  const q=adapter.authorization.q;
  // First content statement establishes the repeatable-read snapshot and the
  // database clock. Calendar boundaries and every count use this same snapshot.
  const {rows:[at]}=await c.query(`SELECT ${stamp('statement_timestamp()')} AS as_of,
   to_char(statement_timestamp() AT TIME ZONE 'Europe/Prague','YYYY-MM-DD') AS day`);
  const {elapsedHours:_,...window}=pragueWindow(at.day);
  const {rows:[s]}=await c.query(`SELECT
   (SELECT last_seq::text FROM ${q}.audit_head WHERE singleton) AS highwater,
   (SELECT count(*)::text FROM ${q}.submissions WHERE status IN ('submitted','in_review')) AS pending,
   (SELECT count(*)::text FROM ${q}.submissions WHERE status='changes_requested') AS needs_info,
   count(*) FILTER (WHERE action='in_review->approved')::text AS approved,
   count(*) FILTER (WHERE action='in_review->rejected')::text AS rejected
   FROM ${q}.audit_events WHERE created_at>=$1::timestamptz AND created_at<$2::timestamptz
    AND seq<=(SELECT last_seq FROM ${q}.audit_head WHERE singleton)`,[window.startInclusive,window.endExclusive]);
  return {schemaVersion:1,requestId,asOf:timestamp(at.as_of),auditHighwater:readRevision(s.highwater),window,state:'exact',
   pending:readRevision(s.pending),needsInfo:readRevision(s.needs_info),approvedInWindow:readRevision(s.approved),rejectedInWindow:readRevision(s.rejected)};
 });
}
