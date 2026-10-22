import {randomUUID} from 'node:crypto';
import {forbidden} from './errors.mjs';
export async function resolvePrincipal(c,q,identity,now){
 await c.query(`INSERT INTO ${q}.principals(id,iss,sub,created_at) VALUES($1,$2,$3,$4) ON CONFLICT(iss,sub) DO NOTHING`,[randomUUID(),identity.issuer,identity.subject,now]);
 const {rows:[p]}=await c.query(`SELECT id,state FROM ${q}.principals WHERE iss=$1 AND sub=$2 FOR UPDATE`,[identity.issuer,identity.subject]);
 if(!p||p.state!=='active')throw forbidden();return p.id;
}
