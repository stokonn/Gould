import {createHash} from 'node:crypto';
import {identifier} from '../../stage6-schema-pg/src/database.mjs';
import {ApiError,uuid} from '../../stage6-auth-local/src/errors.mjs';
import {storedResult,insertResult,Mismatch} from '../../stage6-schema-pg/src/transactions.mjs';
export const conflict=()=>new ApiError(409,'conflict','Revision or state conflict');
const mismatch=()=>new ApiError(422,'idempotency_mismatch','Key already used for a different request');
export function canonical(v){if(Array.isArray(v))return '['+v.map(canonical).join(',')+']';if(v&&typeof v==='object')return '{'+Object.keys(v).sort().map(k=>JSON.stringify(k)+':'+canonical(v[k])).join(',')+'}';return JSON.stringify(v);}
export class Idempotency {
 constructor({schema,clock}){Object.assign(this,{schema,clock,q:identifier(schema)});}
 async run(c,principal,operation,body,fn){
  const key=uuid(body.idempotencyKey),hash=createHash('sha256').update(canonical({operation,body})).digest('hex'),now=new Date(this.clock());
  await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`idempotency:${this.schema}:${principal}:${key}`]);
  let replay;try{replay=await storedResult(c,this.schema,principal,key,hash,now);}catch(e){if(e instanceof Mismatch)throw mismatch();throw e;}
  if(replay)return replay;
  const q=this.q;
  await c.query(`DELETE FROM ${q}.idempotency_results WHERE principal_id=$1 AND key=$2 AND expires_at<=$3`,[principal,key,now]);
  // Protect business effects too if the exact primary-key collision path recovers a replay.
  await c.query('SAVEPOINT idem_effects');
  const response=await fn();let result;
  try{result=await insertResult(c,this.schema,{principal,key,hash,...response,now});}
  catch(e){if(e instanceof Mismatch)throw mismatch();throw e;}
  if(!result.stored)await c.query('ROLLBACK TO SAVEPOINT idem_effects');
  await c.query('RELEASE SAVEPOINT idem_effects');return {status:result.status,body:result.body};
 }
}
