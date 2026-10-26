import test from 'node:test';
import assert from 'node:assert/strict';
import {createRequire} from 'node:module';
import {googleIdentityVerifier,GOOGLE_JWKS_URL} from '../identity.mjs';
const require=createRequire(new URL('../../../spikes/stage6-auth-local/package.json',import.meta.url));
const {generateKeyPair,exportJWK,SignJWT}=await import(require.resolve('jose'));
const audience='synthetic-client.apps.googleusercontent.com';
async function fixture(fetcher){
 const {privateKey,publicKey}=await generateKeyPair('RS256'),jwk={...await exportJWK(publicKey),kid:'synthetic-1',alg:'RS256',use:'sig'};
 let now=Date.now(),calls=0;
 const verifier=googleIdentityVerifier({audience,clock:()=>now,fetcher:async(url,options)=>{calls++;assert.equal(url,GOOGLE_JWKS_URL);assert.equal(options.redirect,'error');assert.ok(options.signal);return fetcher?fetcher({keys:[jwk]},options):Response.json({keys:[jwk]},{headers:{'cache-control':'public, max-age=3600',age:'3590'}});}});
 const sign=(overrides={})=>new SignJWT({nonce:'synthetic-challenge',...overrides}).setProtectedHeader({alg:'RS256',typ:'JWT',kid:jwk.kid}).setIssuer('https://accounts.google.com').setSubject('synthetic-principal').setAudience(audience).setIssuedAt(Math.floor(now/1000)).setExpirationTime(Math.floor(now/1000)+120).sign(privateKey);
 return {verifier,sign,calls:()=>calls,advance:ms=>now+=ms};
}
test('fixed Google transport verifies actual signatures, caches with Age and discloses only identity',async()=>{
 const f=await fixture(),token=await f.sign({roles:['administrator'],email:'ignored@synthetic.invalid'});
 const results=await Promise.all(Array.from({length:8},()=>f.verifier.verify(token)));
 assert.deepEqual(results[0],{issuer:'https://accounts.google.com',subject:'synthetic-principal',nonce:'synthetic-challenge'});assert.equal(f.calls(),1);
 f.advance(10000);await f.verifier.verify(token);assert.equal(f.calls(),2);
});
test('missing client configuration and untrusted token key URLs fail closed',async()=>{
 for(const value of [undefined,'','https://evil.invalid','client.apps.googleusercontent.com/'])assert.throws(()=>googleIdentityVerifier({audience:value}));
 const f=await fixture();await assert.rejects(f.verifier.verify('invalid'),e=>e.status===401);assert.equal(f.calls(),0);
});
test('HTTP errors, wrong media, redirects and oversized streamed keys fail unavailable',async()=>{
 for(const response of [()=>new Response('secret',{status:503}),()=>new Response('{}',{headers:{'content-type':'text/html'}}),()=>Response.redirect('https://evil.invalid'),()=>Response.json({}, {headers:{'content-length':'65537'}}),()=>new Response('x'.repeat(65537),{headers:{'content-type':'application/json'}}),()=>new Response(new Uint8Array([255]),{headers:{'content-type':'application/json'}})]){
  const f=await fixture(response);await assert.rejects(f.verifier.verify(await f.sign()),e=>e.status===503);assert.equal(f.calls(),1);
 }
});
test('no-store and expired cache headers never retain remote keys',async()=>{
 for(const headers of [{'cache-control':'no-store, max-age=300'},{'cache-control':'max-age=2',age:'3'},{}]){
  const f=await fixture(jwks=>Response.json(jwks,{headers})),token=await f.sign();await f.verifier.verify(token);await f.verifier.verify(token);assert.equal(f.calls(),2);
 }
});
test('rejected key responses cancel their body without buffering private error text',async()=>{
 let cancelled=false;
 const f=await fixture(()=>new Response(new ReadableStream({pull(){},cancel(){cancelled=true;}}),{status:503}));
 await assert.rejects(f.verifier.verify(await f.sign()),e=>e.status===503);assert.equal(cancelled,true);
});
test('OAuth stays disabled without a client ID and never trusts substitute identity bindings',async()=>{
 const {configuredIdentityVerifier}=await import('../identity.mjs');let calls=0;
 const verifier=configuredIdentityVerifier({IDENTITY_VERIFIER:{verify:async()=>{calls++;return {subject:'forbidden'};}}});
 await assert.rejects(verifier.verify('any credential'),e=>e.status===503);assert.equal(calls,0);
});
test('enabling OAuth requires a real configured client-ID shape and caches only the real verifier',async()=>{
 const {configuredIdentityVerifier}=await import('../identity.mjs');
 assert.throws(()=>configuredIdentityVerifier({GOOGLE_OAUTH_ENABLED:'true'}));
 const env={GOOGLE_OAUTH_ENABLED:'true',GOOGLE_CLIENT_ID:audience};assert.equal(configuredIdentityVerifier(env),configuredIdentityVerifier(env));
});
