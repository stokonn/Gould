import test from 'node:test';
import assert from 'node:assert/strict';
import {createReadProbe} from './probe-db.mjs';
const env={DATABASE_URL:'postgresql://goldberg_staging_probe:synthetic-only@db.synthetic.invalid:5432/postgres',
 EXPECTED_DB_HOST:'db.synthetic.invalid',DB_CA_PEM:'-----BEGIN CERTIFICATE-----synthetic-fixture'};
test('fixed read with verified TLS and bounded connection/query settings;connection closed',async()=>{
 let options,sql,ended=false;
 class Client{constructor(o){options=o;}async connect(){}async query(s){sql=s;return {rows:[{id:1,marker:'synthetic-only'}]};}async end(){ended=true;}}
 assert.equal(await createReadProbe({Client,env})(),true);
 assert.equal(options.ssl.rejectUnauthorized,true);assert.equal(options.ssl.servername,env.EXPECTED_DB_HOST);
 assert.equal(options.statement_timeout,3000);assert.equal(options.connectionTimeoutMillis,3000);assert.equal(options.query_timeout,3000);
 assert.equal(sql,'SELECT id,marker FROM goldberg_private.staging_probe WHERE id=1');assert.equal(ended,true);
});
test('wrong role/host/SSL override/missing CA fail before constructing client',async()=>{
 let calls=0;class Client{constructor(){calls++;}}
 for(const patch of [
  {DATABASE_URL:env.DATABASE_URL.replace('goldberg_staging_probe','postgres')},
  {EXPECTED_DB_HOST:'other.invalid'},{DATABASE_URL:env.DATABASE_URL+'?sslmode=disable'},
  {DB_CA_PEM:''},{DATABASE_URL:env.DATABASE_URL.replace(':5432',':9999')},
 ]) await assert.rejects(createReadProbe({Client,env:{...env,...patch}}));
 assert.equal(calls,0);
});
test('connection/query failure and unexpected rows close client and deny',async()=>{
 for(const failure of ['connect','query','rows']){
  let ended=false;class Client{async connect(){if(failure==='connect')throw Error('unavailable');}async query(){if(failure==='query')throw Error('timeout');return {rows:[]};}async end(){ended=true;}}
  const read=createReadProbe({Client,env});
  if(failure==='rows')assert.equal(await read(),false);else await assert.rejects(read);
  assert.equal(ended,true);
 }
});
