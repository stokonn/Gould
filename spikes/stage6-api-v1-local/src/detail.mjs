// Approved Detail only. One SQL statement supplies the entire content snapshot.
// Bounded first-page slice: refuse >20 rather than invent history cursors or hide rows.
import {notFound,unavailable} from '../../stage6-auth-local/src/errors.mjs';
import {text} from '../../stage6-handlers-local/src/validation.mjs';
import {id,readRevision,timestamp} from './wire.mjs';
const states=['submitted','in_review','approved','published','changes_requested','rejected','withdrawn'];
const kinds=['work','arrangement','artist','album','release','recording','collection'];
const hash=v=>{if(typeof v!=='string'||! /^[0-9a-f]{64}$/.test(v))throw unavailable();return v;};
const state=v=>{if(!states.includes(v))throw unavailable();return v;};
const stamp=column=>`to_char(${column} AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"')`;
export async function latestDetail({authorization,validator},token,submissionId,requestId){
 return authorization.require(token,'queue.read',async c=>{
  const q=authorization.q;
  const {rows:[s]}=await c.query(`SELECT s.id,s.author_id,s.status,s.target_kind,s.catalogue_uuid,
   s.expected_revision::text,s.current_revision::text,${stamp('s.created_at')} AS created_at,
   ${stamp('s.updated_at')} AS updated_at,
   CASE WHEN s.mapping_id IS NOT NULL AND (m.state IS DISTINCT FROM 'resolved' OR m.catalogue_uuid IS DISTINCT FROM s.catalogue_uuid)
     OR s.catalogue_uuid IS NULL THEN 'unmapped'
     WHEN s.expected_revision=0 THEN 'new' ELSE 'existing' END AS target_state,
   (SELECT coalesce(jsonb_agg(r ORDER BY r.revision::bigint DESC),'[]'::jsonb) FROM
    (SELECT revision::text,payload,prev_hash,this_hash,${stamp('created_at')} AS created_at
     FROM ${q}.submission_revisions WHERE submission_id=s.id ORDER BY submission_revisions.revision DESC LIMIT 21) r) AS revisions,
   (SELECT coalesce(jsonb_agg(r ORDER BY r.audit_seq::bigint),'[]'::jsonb) FROM
    (SELECT audit_seq::text,submission_revision::text,actor_id,from_status,to_status,reason,${stamp('created_at')} AS created_at
     FROM ${q}.review_events WHERE submission_id=s.id ORDER BY review_events.audit_seq LIMIT 21) r) AS reviews
   FROM ${q}.submissions s LEFT JOIN ${q}.identity_mappings m ON m.id=s.mapping_id WHERE s.id=$1`,[submissionId]);
  if(!s)throw notFound();
  try{
   if(!kinds.includes(s.target_kind)||s.revisions.length<1||s.revisions.length>20||s.reviews.length>20)throw unavailable();
   const revision=readRevision(s.current_revision,{positive:true});
   const revisions=s.revisions.map(r=>{
    // Exact nested allowlist + trusted policy; no raw row or unapproved evidence escapes.
    validator.payload(r.payload);
    return {submissionRevision:readRevision(r.revision,{positive:true}),payload:structuredClone(r.payload),createdAt:timestamp(r.created_at),previousHash:hash(r.prev_hash),hash:hash(r.this_hash)};
   });
   if(revisions[0].submissionRevision!==revision)throw unavailable();
   const reviews=s.reviews.map(r=>({auditSeq:readRevision(r.audit_seq,{positive:true}),submissionRevision:readRevision(r.submission_revision,{positive:true}),actorId:r.actor_id===null?null:id(r.actor_id),from:state(r.from_status),to:state(r.to_status),reason:r.reason===null?null:text(r.reason),createdAt:timestamp(r.created_at)}));
   const result={schemaVersion:1,requestId,submissionId:id(s.id),authorId:id(s.author_id),status:state(s.status),kind:s.target_kind,
    targetEntityId:s.catalogue_uuid===null?null:id(s.catalogue_uuid),targetState:s.target_state,
    expectedEntityRevision:readRevision(s.expected_revision),submissionRevision:revision,
    createdAt:timestamp(s.created_at),updatedAt:timestamp(s.updated_at),revisions:{items:revisions,nextCursor:null},reviews:{items:reviews,nextCursor:null}};
   if(Buffer.byteLength(JSON.stringify(result))>262144)throw unavailable();
   return result;
  }catch{throw unavailable();}
 });
}
