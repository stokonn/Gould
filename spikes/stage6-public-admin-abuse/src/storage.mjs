import {readFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {migrateAuth,identifier} from '../../stage6-auth-local/src/storage.mjs';
export async function migrateOperations(c,schema){
 await migrateAuth(c,schema);const q=identifier(schema),sql=await readFile(new URL('../migrations/004_operations.sql',import.meta.url),'utf8'),hash=createHash('sha256').update(sql).digest('hex');
 await c.query('BEGIN');try{
  await c.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`operations-migration:${schema}`]);
  await c.query(`CREATE TABLE IF NOT EXISTS ${q}.operations_migrations(version integer PRIMARY KEY,checksum text NOT NULL)`);
  const {rows}=await c.query(`SELECT * FROM ${q}.operations_migrations`);
  if(rows.length){if(rows.length!==1||rows[0].version!==4||rows[0].checksum!==hash)throw new Error('operations migration checksum mismatch');}
  else {await c.query(sql.replaceAll('__S__',q));await c.query(`INSERT INTO ${q}.operations_migrations VALUES(4,$1)`,[hash]);}
  await c.query('COMMIT');
 }catch(e){await c.query('ROLLBACK');throw e;}
}
