import pg from 'pg';
import {readFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';

// Deliberately accepts no URL, host, credentials, SSL or cloud configuration.
export function localClient() {
  const port = Number(process.env.SCHEMA_PG_PORT ?? 54339);
  if (!Number.isInteger(port) || port < 1024 || port > 65535) throw new Error('invalid local port');
  return new pg.Client({host:'127.0.0.1', port, database:'schema_prototype', user:'schema_prototype',
    password:'', connectionTimeoutMillis:3000, application_name:'goldberg-local-schema-prototype'});
}
export function identifier(schema) {
  if (!/^proto_[a-z0-9_]+$/.test(schema) || schema.length > 63) throw new Error('prototype schema only');
  return `"${schema}"`;
}
export async function migrationFiles() {
  return Promise.all(['001_entities.sql','002_integrity.sql'].map(async (name,index) => ({
    version:index+1, name, sql:await readFile(new URL(`../migrations/${name}`,import.meta.url),'utf8')
  })));
}
export async function migrate(client,schema,{through=2, files}={}) {
  const q=identifier(schema); files ??= await migrationFiles();
  const {rows:[v]}=await client.query('SHOW server_version_num');
  if (Number(v.server_version_num)<170000 || Number(v.server_version_num)>=180000) throw new Error('PostgreSQL 17 required');
  await client.query('BEGIN');
  try {
    await client.query('SELECT pg_advisory_xact_lock(hashtextextended($1,0))',[`schema-migration:${schema}`]);
    await client.query(`CREATE SCHEMA IF NOT EXISTS ${q}`);
    await client.query(`CREATE TABLE IF NOT EXISTS ${q}.schema_migrations(version integer PRIMARY KEY, name text NOT NULL, checksum text NOT NULL, applied_at timestamptz NOT NULL DEFAULT clock_timestamp())`);
    const {rows:applied}=await client.query(`SELECT * FROM ${q}.schema_migrations ORDER BY version`);
    for (const [i,row] of applied.entries()) {
      const file=files.find(f=>f.version===row.version);
      if(row.version!==i+1 || !file || row.name!==file.name || row.checksum!==createHash('sha256').update(file.sql).digest('hex')) throw new Error('migration history/checksum mismatch');
    }
    for(const [i,file] of files.entries()) {
      if(file.version!==i+1) throw new Error('noncontiguous migrations');
      if(file.version>through || applied.some(r=>r.version===file.version)) continue;
      await client.query(file.sql.replaceAll('__S__',q));
      await client.query(`INSERT INTO ${q}.schema_migrations(version,name,checksum) VALUES($1,$2,$3)`,[file.version,file.name,createHash('sha256').update(file.sql).digest('hex')]);
    }
    await client.query('COMMIT');
  }catch(error){await client.query('ROLLBACK');throw error;}
}
// pg's default int8 strings remain untouched. Never install a global Number parser.
export function safeInt8(value) {
  if(!['string','bigint'].includes(typeof value) || !/^-?(0|[1-9][0-9]*)$/.test(String(value))) throw new TypeError('exact decimal int8 required');
  const n=BigInt(value);
  if(n < -9223372036854775808n || n > 9223372036854775807n) throw new RangeError('outside PostgreSQL int8');
  if(n < BigInt(Number.MIN_SAFE_INTEGER) || n > BigInt(Number.MAX_SAFE_INTEGER)) throw new RangeError('unsafe JSON integer');
  return Number(n);
}
