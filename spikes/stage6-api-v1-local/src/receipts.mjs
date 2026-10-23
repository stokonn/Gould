// Public result persistence uses the existing table/helpers and SQL transaction.
// No migration: the existing INSERT computes expires_at = created_at + 24 hours.
import {identifier} from '../../stage6-schema-pg/src/database.mjs';
import {storedResult,insertResult,Mismatch} from '../../stage6-schema-pg/src/transactions.mjs';
import {canonical} from '../../stage6-handlers-local/src/idempotency.mjs';
import {createHash} from 'node:crypto';
import {ApiError,readClock} from '../../stage6-auth-local/src/errors.mjs';
import {id} from './wire.mjs';
export class PublicReceipts {
 constructor({schema,clock}){this.schema=schema;this.q=identifier(schema);this.clock=()=>readClock(clock);}
 async run(c,principal,operation,body,fn){
  const key=id(body.idempotencyKey),hash=createHash('sha256').update(canonical({operation,body})).digest('hex');
  const mismatch=()=>new ApiError(422,'idempotency_mismatch','Key already used for a different request');
  await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`idempotency:${this.schema}:${principal}:${key}`]);
  // Sample the clock AFTER any lock wait, including a wait on a concurrent same-key result.
  const now=new Date(this.clock());let replay;
  try{replay=await storedResult(c,this.schema,principal,key,hash,now);}catch(e){if(e instanceof Mismatch)throw mismatch();throw e;}
  if(replay)return replay;
  await c.query(`DELETE FROM ${this.q}.idempotency_results WHERE principal_id=$1 AND key=$2 AND expires_at<=$3`,[principal,key,now]);
  await c.query('SAVEPOINT public_effects');
  const result=await fn(),completed=new Date(this.clock());let inserted;
  try{inserted=await insertResult(c,this.schema,{principal,key,hash,...result,now:completed});}
  catch(e){if(e instanceof Mismatch)throw mismatch();throw e;}
  if(!inserted.stored)await c.query('ROLLBACK TO SAVEPOINT public_effects');
  await c.query('RELEASE SAVEPOINT public_effects');
  return {status:inserted.status,body:inserted.body};
 }
}
