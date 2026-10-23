import {randomUUID} from 'node:crypto';
export async function audit(c,q,{actor,id,revision,requestId,action,reason=null,before=null,after=null}){
 return (await c.query(`SELECT ${q}.append_audit($1,$2,'submission',$3,$4,$5,$6,$7,$8) AS seq`,[actor,action,id,String(revision),requestId,reason,before,after])).rows[0].seq;
}
export async function transition(c,q,{actor,id,revision,from,to,requestId,reason=null,hash=null}){
 const seq=await audit(c,q,{actor,id,revision,requestId,action:`${from}->${to}`,reason,after:hash});
 await c.query(`INSERT INTO ${q}.review_events(id,submission_id,submission_revision,actor_id,from_status,to_status,reason,audit_seq) VALUES($1,$2,$3,$4,$5,$6,$7,$8)`,[randomUUID(),id,String(revision),actor,from,to,reason,seq]);
 await c.query(`UPDATE ${q}.submissions SET status=$2,current_revision=$3,updated_at=clock_timestamp() WHERE id=$1`,[id,to,String(revision)]);return seq;
}
