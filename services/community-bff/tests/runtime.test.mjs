import test from 'node:test';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {mkdtemp,rm} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join,resolve} from 'node:path';
import {venue} from '../../../spikes/stage6-api-v1-local/tests/pg-http-venue.mjs';
import {migrateOperations} from '../../../spikes/stage6-public-admin-abuse/src/storage.mjs';
import {createBffAdapter} from '../adapter.mjs';

test('actual local workerd Fetch → pg TCP → disposable PostgreSQL session and queue',async t=>{
 const require=createRequire(new URL('../package.json',import.meta.url)),{build}=require('esbuild'),{Miniflare,convertV4MiniflareOptions}=require('miniflare');
 const out=await mkdtemp(join(tmpdir(),'goldberg-bff-workerd-'));t.after(()=>rm(out,{recursive:true,force:true}));
 const v=await venue(t,{adapterFactory:createBffAdapter,migrate:migrateOperations});
 const bundle=join(out,'worker.mjs');
 await build({entryPoints:[resolve('services/community-bff/tests/runtime-worker.mjs')],bundle:true,format:'esm',platform:'node',conditions:['workerd'],external:['cloudflare:sockets'],target:'es2022',outfile:bundle,banner:{js:'import {createRequire} from "node:module";const require=createRequire("/worker.js");'}});
 const options=convertV4MiniflareOptions({host:'127.0.0.1',port:0,rootPath:out,cf:false,modules:true,scriptPath:bundle,compatibilityDate:'2026-10-25',compatibilityFlags:['nodejs_compat'],bindings:{SCHEMA:v.schema,PG_PORT:process.env.SCHEMA_PG_PORT}});options.telemetry={enabled:false};
 const mf=new Miniflare(options);t.after(()=>mf.dispose());
 const coldStart=performance.now();
 const response=await mf.dispatchFetch('https://private.invalid/v1/session',{headers:{authorization:'Bearer '+v.admin.accessToken}});
 t.diagnostic('local first request wall milliseconds: '+(performance.now()-coldStart).toFixed(3));
 assert.equal(response.status,200,await response.clone().text());assert.equal((await response.json()).principalId,v.admin.principalId);
 const warmStart=performance.now();
 const queue=await mf.dispatchFetch('https://private.invalid/v1/admin/queue',{headers:{authorization:'Bearer '+v.admin.accessToken}});assert.equal(queue.status,200);assert.deepEqual((await queue.json()).items,[]);
 t.diagnostic('local warm request wall milliseconds: '+(performance.now()-warmStart).toFixed(3)+'; 3 requests total; hosted CPU unmeasured');
 const denied=await mf.dispatchFetch('https://private.invalid/v1/admin/queue');assert.equal(denied.status,401);
});
