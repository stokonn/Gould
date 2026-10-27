import {build} from 'esbuild';
import {Miniflare,convertV4MiniflareOptions} from 'miniflare';
import {mkdtemp,readFile,writeFile,rm,mkdir} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join,resolve} from 'node:path';
import {createHash,randomUUID} from 'node:crypto';
import assert from 'node:assert/strict';
const out=await mkdtemp(join(tmpdir(),'goldberg-bff-package-'));
let mf;
try {
 const file=join(out,'worker.mjs');
 await build({entryPoints:['services/community-bff/worker.mjs'],bundle:true,format:'esm',platform:'node',conditions:['workerd'],external:['cloudflare:sockets'],target:'es2022',outfile:file,banner:{js:'import {createRequire} from "node:module";const require=createRequire("/worker.js");'}});
 const bytes=await readFile(file),assertions=[];
 for(const bindings of [{},{COMMUNITY_BFF_ENABLED:'true'}]){
  const options=convertV4MiniflareOptions({host:'127.0.0.1',port:0,cf:false,modules:true,rootPath:out,scriptPath:file,compatibilityDate:'2026-10-25',compatibilityFlags:['nodejs_compat'],bindings});options.telemetry={enabled:false};
  mf=new Miniflare(options);
  for(const path of ['/v1/session','/v1/admin/queue','/v1/submissions','/v1/catalogue/overlay']){
   const r=await mf.dispatchFetch('https://private.invalid'+path);assert.equal(r.status,503);const body=await r.text();assert.ok(!/password|postgres|DB_HOST|stack/i.test(body));assertions.push({enabled:!!bindings.COMMUNITY_BFF_ENABLED,path,status:r.status});
  }
  for(const path of ['/v1/session/challenge','/v1/session','/v1/session/refresh']){
   const r=await mf.dispatchFetch('https://private.invalid'+path,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({schemaVersion:1})});
   assert.equal(r.status,503);assert.equal(r.headers.get('cache-control'),'no-store');const body=await r.json();assert.equal(body.error.code,'service_unavailable');assert.ok(!/password|postgres|DB_HOST|stack/i.test(JSON.stringify(body)));assertions.push({enabled:!!bindings.COMMUNITY_BFF_ENABLED,oauthConfigured:false,path,method:'POST',status:r.status});
  }
  await mf.dispose();mf=undefined;
 }
 const gated=convertV4MiniflareOptions({host:'127.0.0.1',port:0,cf:false,modules:true,rootPath:out,scriptPath:file,compatibilityDate:'2026-10-25',compatibilityFlags:['nodejs_compat'],bindings:{CATALOGUE_READ_ONLY:'true'}});gated.telemetry={enabled:false};mf=new Miniflare(gated);
 for(const [method,path,status] of [['POST','/v1/session/challenge',404],['POST','/v1/session',404],['POST','/v1/session/refresh',404],['GET','/v1/admin/queue',404],['POST','/v1/submissions',404],['GET','/v1/not-found',404],['POST','/v1/catalogue/overlay',405],['POST','/v1/catalogue/changes',405],['GET','/v1/catalogue/overlay',503],['GET','/v1/catalogue/changes',503]]){
  const r=await mf.dispatchFetch('https://catalogue.invalid'+path,{method});assert.equal(r.status,status);assert.equal(r.headers.get('cache-control'),'no-store');assert.ok(!/password|postgres|DB_HOST|stack/i.test(await r.text()));assertions.push({catalogueOnly:true,method,path,status});
 }
 await mf.dispose();mf=undefined;
 const resultDir='services/community-bff/results/package-'+randomUUID();
 await mkdir(resultDir,{recursive:true});
 await writeFile(join(resultDir,'receipt.json'),JSON.stringify({bundleSha256:createHash('sha256').update(bytes).digest('hex'),bundleBytes:bytes.length,conditions:['workerd'],runtime:'local workerd; not hosted',assertions},null,2)+'\n');
 console.log('Worker package: 24 actual workerd fail-closed requests PASS; '+resultDir);
}finally{await mf?.dispose();await rm(out,{recursive:true,force:true});}
