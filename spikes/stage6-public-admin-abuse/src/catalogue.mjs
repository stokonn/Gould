import {identifier,transaction} from '../../stage6-auth-local/src/storage.mjs';
import {bounded,version,text} from '../../stage6-handlers-local/src/validation.mjs';
import {invalid,uuid} from '../../stage6-auth-local/src/errors.mjs';
import {decimal,kinds} from './cursor.mjs';
// Public projection intentionally has no spread of stored payload/evidence/header rows.
export function display(row){
 if(!kinds.includes(row.kind))throw invalid();uuid(row.entity_id);decimal(row.revision,{zero:false});
 const title=row.payload.fields.find(f=>['title','name','displayTitle'].includes(f.field))?.newValue;
 text(title,2048);
 return {kind:row.kind,catalogueUuid:row.entity_id,displayTitle:title,revision:row.revision,
  provenanceSummary:{licenses:[...new Set(row.source_evidence.map(e=>e.license))].sort(),sourceCount:row.source_evidence.length}};
}
export class CatalogueService {
 constructor({connect,schema,cursors}){Object.assign(this,{connect,q:identifier(schema),cursors});}
 async read(operation,body){
  bounded(body);body=structuredClone(body);version(body,['schemaVersion','kind','cursor']);if(!['catalogue.overlay','catalogue.changes'].includes(operation)||body.kind!==undefined&&(!kinds.includes(body.kind)||operation!=='catalogue.overlay'))throw invalid();
  const scope=operation+':'+(body.kind??''),cursor=body.cursor===undefined?null:this.cursors.read(body.cursor,scope);
  return transaction(this.connect,async c=>{
   // Writers take audit_head before appending feed; a SHARE barrier excludes uncommitted low sequences.
   await c.query(`SELECT last_seq FROM ${this.q}.audit_head WHERE singleton FOR SHARE`);
   const high=(cursor?.poll?null:cursor?.high)??(await c.query(`SELECT coalesce(max(seq),0)::text AS n FROM ${this.q}.change_feed`)).rows[0].n;
   let rows;
   if(operation==='catalogue.changes'){
    const after=cursor?decimal(cursor.after):'0';
    rows=(await c.query(`SELECT f.seq::text,f.entity_id,f.revision::text,f.status,f.event_kind,e.kind,
     to_char(f.created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS "createdAt",
     CASE WHEN e.status='published' AND e.revision=f.revision THEN r.source_evidence ELSE '[]'::jsonb END AS sources
     FROM ${this.q}.change_feed f JOIN ${this.q}.published_entities e ON e.id=f.entity_id
     JOIN ${this.q}.published_revisions r ON (r.entity_id,r.revision)=(f.entity_id,f.revision)
     WHERE f.seq>$1::bigint AND f.seq<=$2::bigint ORDER BY f.seq LIMIT 21`,[after,high])).rows;
   }else{
    if(cursor){uuid(cursor.id);if(typeof cursor.at!=='string'||!/^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d\.\d{6}Z$/.test(cursor.at))throw invalid();}
    rows=(await c.query(`WITH latest AS (SELECT DISTINCT ON(entity_id) entity_id,revision,status FROM ${this.q}.change_feed WHERE seq<=$1::bigint ORDER BY entity_id,seq DESC),
     first_seen AS (SELECT entity_id,min(created_at) AS created_at FROM ${this.q}.change_feed WHERE seq<=$1::bigint GROUP BY entity_id)
     SELECT l.entity_id,e.kind,l.revision::text,r.payload,r.source_evidence,to_char(b.created_at AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"') AS "createdAt"
     FROM latest l JOIN first_seen b USING(entity_id) JOIN ${this.q}.published_entities e ON e.id=l.entity_id
     JOIN ${this.q}.published_revisions r ON (r.entity_id,r.revision)=(l.entity_id,l.revision)
     WHERE l.status='published' AND e.status='published' AND e.revision=l.revision AND ($2::text IS NULL OR e.kind=$2)
     AND ($3::timestamptz IS NULL OR (b.created_at,l.entity_id)>($3::timestamptz,$4::uuid))
     ORDER BY b.created_at,l.entity_id LIMIT 21`,[high,body.kind??null,cursor?.at??null,cursor?.id??null])).rows;
   }
   const more=rows.length>20,page=rows.slice(0,20),last=page.at(-1);
   const items=page.map(r=>operation==='catalogue.overlay'?display(r):{sequence:r.seq,kind:r.kind,catalogueUuid:r.entity_id,revision:r.revision,event:r.event_kind,status:r.status,createdAt:r.createdAt,
    provenanceSummary:{licenses:[...new Set(r.sources.map(e=>e.license))].sort(),sourceCount:r.sources.length}});
   const nextCursor=more?this.cursors.sign({version:1,scope,high,...(operation==='catalogue.overlay'?{at:last.createdAt,id:last.entity_id}:{after:last.seq})}):null;
   // Poll only after draining nextCursor. Empty poll returns a new watermark as well.
   const pollCursor=operation==='catalogue.changes'&&!more?this.cursors.sign({version:1,scope,high,after:high,poll:true}):undefined;
   return {status:200,body:{items,nextCursor,...(pollCursor?{pollCursor}:{})}};
  });
 }
}
