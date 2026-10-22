import {readFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {migrate,identifier} from '../../stage6-schema-pg/src/database.mjs';
import {ApiError,unavailable} from './errors.mjs';
export {identifier};
export async function transaction(connect,fn){
 let c;
 try{
  c=await connect();
  await c.query('BEGIN');await c.query("SET LOCAL lock_timeout='2s'");await c.query("SET LOCAL statement_timeout='5s'");
  const result=await fn(c);await c.query('COMMIT');return result;
 }catch(e){if(c)await c.query('ROLLBACK').catch(()=>{});throw e instanceof ApiError?e:unavailable();}
 finally{if(c)await c.end();}
}
export async function migrateAuth(c,s){
 await migrate(c,s);const q=identifier(s);
 const sql=await readFile(new URL('../migrations/003_auth.sql',import.meta.url),'utf8');
 const hash=createHash('sha256').update(sql).digest('hex');
 await c.query('BEGIN');
 try{
  await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`auth-migration:${s}`]);
  await c.query(`CREATE TABLE IF NOT EXISTS ${q}.auth_migrations(version integer PRIMARY KEY,checksum text NOT NULL)`);
  const {rows}=await c.query(`SELECT * FROM ${q}.auth_migrations`);
  if(rows.length){if(rows.length!==1||rows[0].version!==3||rows[0].checksum!==hash)throw new Error('auth migration checksum mismatch');}
  else{await c.query(sql.replaceAll('__S__',q));await c.query(`INSERT INTO ${q}.auth_migrations VALUES(3,$1)`,[hash]);}
  await c.query('COMMIT');
 }catch(e){await c.query('ROLLBACK');throw e;}
}
