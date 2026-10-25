// New preparation checks only. No cf CLI, account API or remote session calls.
const fs=require('node:fs');const path=require('node:path');const cp=require('node:child_process');
const crypto=require('node:crypto');const os=require('node:os');
const base=__dirname;
const tool='/tmp/goldberg-cloud-local-build-20261025';
const driver='/tmp/goldberg-private-staging-build-20261025';
const out=fs.mkdtempSync(path.join(base,'results/component-final-'));
const build=fs.mkdtempSync(path.join(os.tmpdir(),'goldberg-probe-bundle-'));
const hash=b=>crypto.createHash('sha256').update(b).digest('hex');
const sourceNames=fs.readdirSync(base).filter(n=>/\.(mjs|cjs|sql|template|py)$/.test(n));
const sources=Object.fromEntries(sourceNames.map(n=>[n,hash(fs.readFileSync(path.join(base,n)))]));
const checks=[];
function run(args){
 const p=cp.spawnSync(args[0],args.slice(1),{cwd:build,encoding:'utf8',timeout:45000});
 const name=`${String(checks.length).padStart(2,'0')}.log`;
 fs.writeFileSync(path.join(out,name),(p.stdout??'')+(p.stderr??''));checks.push({argv:args,exitCode:p.status,log:name});
 if(p.status!==0)throw Error(`Check failed: ${name}`);
}
async function main(){
 let status='FAILED';let mf;
 try{
  for(const n of sourceNames)fs.copyFileSync(path.join(base,n),path.join(build,n));
  for(const n of ['probe-worker.mjs','probe-worker.config.ts','local-caller.config.ts'])
   fs.copyFileSync(path.join(base,n+'.template'),path.join(build,n));
  fs.mkdirSync(path.join(build,'node_modules'));
  for(const [name,target] of [['pg',path.join(driver,'node_modules/pg')],['cf',path.join(tool,'node_modules/cf')]])
   fs.symlinkSync(target,path.join(build,'node_modules',name),'dir');
  fs.writeFileSync(path.join(build,'package.json'),'{"type":"module"}');
  fs.copyFileSync(path.join(driver,'tsconfig.json'),path.join(build,'tsconfig.json'));
  run(['node','--test',...['probe-handler.test.mjs','probe-db.test.mjs','local-caller.test.mjs'].map(n=>path.join(base,n))]);
  run([path.join(tool,'node_modules/.bin/tsc'),'-p',path.join(build,'tsconfig.json')]);
  // Node-compatible CJS import shim: Workers import.meta.url is not assumed.
  run([path.join(tool,'node_modules/.bin/esbuild'),'probe-worker.mjs','--bundle','--format=esm','--platform=node','--target=es2022',
   '--banner:js=import { createRequire } from "node:module"; const require = createRequire("/worker.js");','--outfile=probe-worker.bundle.mjs']);
  run(['node','--check',path.join(build,'probe-worker.bundle.mjs')]);
  const {Miniflare,convertV4MiniflareOptions}=require(path.join(tool,'node_modules/miniflare'));
  const options=convertV4MiniflareOptions({host:'127.0.0.1',port:0,rootPath:build,cf:false,modules:true,
   scriptPath:path.join(build,'probe-worker.bundle.mjs'),compatibilityDate:'2026-10-25',compatibilityFlags:['nodejs_compat']});
  options.telemetry={enabled:false};
  const assertions=[];
  mf=new Miniflare(options);
  let r=await mf.dispatchFetch('https://synthetic.invalid/__private_staging/probe',{method:'POST'});
  if(r.status!==503)throw Error('workerd default denial failed');assertions.push('disabled ->503');
  await mf.dispose();mf=undefined;
  // Synthetic configuration: never a hosted secret, no DATABASE_URL is supplied.
  const token='a'.repeat(64);
  const enabled=convertV4MiniflareOptions({host:'127.0.0.1',port:0,rootPath:build,cf:false,modules:true,
   scriptPath:path.join(build,'probe-worker.bundle.mjs'),compatibilityDate:'2026-10-25',compatibilityFlags:['nodejs_compat'],
   bindings:{STAGING_PROBE_ENABLED:'true',PRIVATE_VERIFICATION_TOKEN:token}});
  enabled.telemetry={enabled:false};mf=new Miniflare(enabled);
  for(const [route,authorization,expected] of [
   ['/__private_staging/probe','',403],['/__private_staging/probe','Bearer '+'b'.repeat(64),403],
   ['/v1/me','Bearer '+token,404],['/__private_staging/probe','Bearer '+token,503]]){
   r=await mf.dispatchFetch('https://synthetic.invalid'+route,{method:'POST',headers:authorization?{Authorization:authorization}:{}});
   if(r.status!==expected)throw Error('workerd authentication/fail-closed assertion failed');
   if((await r.text()).includes(token))throw Error('credential disclosure');
   assertions.push(route+' ->'+expected);
  }
  fs.writeFileSync(path.join(out,'workerd.log'),'PASS local workerd: '+assertions.join('; ')+'\n');
  fs.copyFileSync(path.join(driver,'package-lock.json'),path.join(out,'driver-package-lock.json'));
  fs.copyFileSync(path.join(build,'tsconfig.json'),path.join(out,'tsconfig.json'));
  status='PASS';
 }catch(e){fs.writeFileSync(path.join(out,'error.log'),String(e));}
 finally{
  if(mf)await mf.dispose();
  const unchanged=sourceNames.every(n=>sources[n]===hash(fs.readFileSync(path.join(base,n))));
  const result={status,checks,logicalComponentTests:8,nodeFileWrappers:3,localWorkerdRequests:status==='PASS'?5:0,
   hostedRequests:0,sources,sourceUnchanged:unchanged,buildDirectory:build,
   artifacts:Object.fromEntries(fs.readdirSync(out).map(n=>[n,hash(fs.readFileSync(path.join(out,n)))]))};
  fs.writeFileSync(path.join(out,'result.json'),JSON.stringify(result,null,2)+'\n');
  console.log(JSON.stringify({out,status,sourceUnchanged:unchanged}));
  if(status!=='PASS'||!unchanged)process.exitCode=1;
 }
}
main().catch(e=>{console.error(e);process.exitCode=1});
