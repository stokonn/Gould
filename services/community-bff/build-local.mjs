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
  await mf.dispose();mf=undefined;
 }
 const resultDir='services/community-bff/results/package-'+randomUUID();
 await mkdir(resultDir,{recursive:true});
 await writeFile(join(resultDir,'receipt.json'),JSON.stringify({bundleSha256:createHash('sha256').update(bytes).digest('hex'),bundleBytes:bytes.length,conditions:['workerd'],runtime:'local workerd; not hosted',assertions},null,2)+'\n');
 console.log('Worker package: 8 actual workerd fail-closed requests PASS; '+resultDir);
}finally{await mf?.dispose();await rm(out,{recursive:true,force:true});}
