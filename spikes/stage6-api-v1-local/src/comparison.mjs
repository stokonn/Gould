// Approved live-only Comparison; Detail separately retains recorded proposal intent.
import {notFound,unavailable} from '../../stage6-auth-local/src/errors.mjs';
import {id,readRevision} from './wire.mjs';
export async function currentComparison({authorization,validator},token,entityId,submissionId,requestId){
 try{return await authorization.require(token,'queue.read',async c=>{
  const q=authorization.q;
  // One statement snapshot binds the submission target/mapping to the live head and
  // that exact revision's payload/hash. Never use the submission payload as before-values.
  const {rows:[s]}=await c.query(`SELECT s.catalogue_uuid,s.target_kind,
   CASE WHEN s.catalogue_uuid IS NULL OR s.mapping_id IS NOT NULL AND
    (m.state IS DISTINCT FROM 'resolved' OR m.catalogue_uuid IS DISTINCT FROM s.catalogue_uuid)
    THEN true ELSE false END AS unmapped,
   e.id AS entity_id,e.kind AS entity_kind,e.revision::text AS entity_revision,
   e.status AS entity_status,r.payload,r.this_hash AS payload_hash
   FROM ${q}.submissions s LEFT JOIN ${q}.identity_mappings m ON m.id=s.mapping_id
   LEFT JOIN ${q}.published_entities e ON e.id=s.catalogue_uuid
   LEFT JOIN ${q}.published_revisions r ON r.entity_id=e.id AND r.revision=e.revision
   WHERE s.id=$1`,[submissionId]);
  if(!s || s.catalogue_uuid!==null && s.catalogue_uuid!==entityId)throw notFound();
  const base={schemaVersion:1,requestId,submissionId,entityId:s.unmapped?null:entityId};
  const missing=reason=>({...base,state:'unavailable',reason});
  if(s.unmapped)return missing('unmapped');
  if(s.entity_id===null)return missing('absent');
  try{
   if(s.target_kind!==s.entity_kind || s.payload?.target?.kind!==s.entity_kind ||
    !['published','superseded','quarantined'].includes(s.entity_status))throw unavailable();
   validator.payload(s.payload); // Exact nested allowlists and reviewed source/license tuples.
   if(s.payload.target.catalogueUuid && s.payload.target.catalogueUuid!==entityId)throw unavailable();
   if(!/^[0-9a-f]{64}$/.test(s.payload_hash))throw unavailable();
   const fields=s.payload.fields.map(f=>({field:f.field,value:f.newValue,
    evidence:f.evidence.map(e=>({type:e.type,value:e.value,license:e.license}))}));
   const result={...base,state:'available',entityRevision:readRevision(s.entity_revision,{positive:true}),
    entityStatus:s.entity_status,payloadHash:s.payload_hash,fields};
   id(s.entity_id);
   if(Buffer.byteLength(JSON.stringify(result))>262144)throw unavailable();
   return result;
  }catch{return missing('projection_unavailable');}
 });}catch(e){if(e?.status===403)throw notFound();throw e;}
}
