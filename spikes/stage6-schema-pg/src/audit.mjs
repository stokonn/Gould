import {createHash} from 'node:crypto';
import {identifier} from './database.mjs';
export const digest=text=>createHash('sha256').update(text,'utf8').digest('hex');
const genesis='0'.repeat(64);
export async function exportAudit(client,schema) {
  const q=identifier(schema);
  // Caller must use a consistent transaction snapshot; this function supplies one.
  await client.query('BEGIN ISOLATION LEVEL REPEATABLE READ READ ONLY');
  try {
    const {rows:events}=await client.query(`SELECT seq::text,prev_hash,this_hash,actor_id,action,target_kind,target_id,target_revision::text,
      before_hash,after_hash,reason,to_char(created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS created_at,request_id,canonical_content FROM ${q}.audit_events ORDER BY seq`);
    const {rows:[head]}=await client.query(`SELECT last_seq::text,last_hash FROM ${q}.audit_head WHERE singleton`);
    await client.query('COMMIT');return {version:1,events,head};
  }catch(error){await client.query('ROLLBACK');throw error;}
}
// Independent Node recomputation from exported fields, not merely a hash of stored canonical_content.
export function verifyAudit(snapshot) {
  if(snapshot.version!==1 || !snapshot.head) throw new Error('missing export version/head');
  let seq=0n,prev=genesis;
  for(const e of snapshot.events) {
    if(BigInt(e.seq)!==++seq || e.prev_hash!==prev) throw new Error('audit order/link mismatch');
    const fields=['goldberg-audit-v1',e.seq,e.actor_id,e.action,e.target_kind,e.target_id,e.target_revision,
      e.before_hash,e.after_hash,e.reason,e.created_at,e.request_id];
    if(fields.some(v=>v===undefined)) throw new Error('incomplete exported row');
    const canonical='['+fields.map(v=>JSON.stringify(v)).join(', ')+']';
    if(canonical!==e.canonical_content || digest(prev+':'+canonical)!==e.this_hash) throw new Error('audit content/hash mismatch');
    prev=e.this_hash;
  }
  if(String(seq)!==snapshot.head.last_seq || prev!==snapshot.head.last_hash) throw new Error('audit head mismatch');
  return true;
}
