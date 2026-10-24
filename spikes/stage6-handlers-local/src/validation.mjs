import {isIP} from 'node:net';
import {ApiError,allowObject,invalid,uuid} from '../../stage6-auth-local/src/errors.mjs';
const kinds=['work','arrangement','artist','album','release','recording','collection'];
export function text(value,max=1000){
 if(typeof value!=='string'||!value.trim()||Buffer.byteLength(value)>max||/[<>\u0000-\u001f\u007f]/u.test(value)||/&#|&lt;|&gt;|javascript:|data:|vbscript:/i.test(value))throw invalid();return value;
}
export function bounded(value){
 let nodes=0,bytes=0;const seen=new Set();
 function walk(v,depth){
  if(depth>7||++nodes>1000)throw invalid();
  if(v===null||typeof v==='boolean'||(typeof v==='number'&&Number.isFinite(v))){bytes+=8;return;}
  if(typeof v==='string'){bytes+=Buffer.byteLength(v);if(bytes>16384)throw invalid();return;}
  if(typeof v!=='object'||seen.has(v))throw invalid();seen.add(v);
  if(!Array.isArray(v))allowObject(v,Object.keys(v));
  for(const k of Reflect.ownKeys(v)){
   if(Array.isArray(v)&&k==='length')continue;
   if(typeof k!=='string'||(Array.isArray(v)&&! /^(0|[1-9][0-9]*)$/.test(k)))throw invalid();
   const d=Object.getOwnPropertyDescriptor(v,k);if(!d?.enumerable||!Object.hasOwn(d,'value'))throw invalid();
   bytes+=Buffer.byteLength(k);walk(d.value,depth+1);
  }
  seen.delete(v);
 }
 walk(value,0);if(Buffer.byteLength(JSON.stringify(value))>16384)throw invalid();return value;
}
export function version(body,allowed){bounded(body);allowObject(body,allowed);if(body.schemaVersion!==1)throw invalid();}
const INT8_MAX=9223372036854775807n;
export function revision(value,{positive=false}={}){
 if(Number.isSafeInteger(value)&&value>=(positive?1:0))return value;
 // Checked full-range int8 decimal strings (canonical, no leading zeros, no
 // rounding through Number) accepted from the lossless public wire bridge.
 if(typeof value==='string'&&/^(0|[1-9][0-9]*)$/.test(value)&&value.length<=19&&BigInt(value)<=INT8_MAX&&(!positive||value!=='0'))return value;
 throw invalid();
}
export function incrementRevision(value){
 revision(value);
 const next=BigInt(value)+1n;
 if(next>INT8_MAX)throw new ApiError(409,'conflict','Revision or state conflict');
 return next.toString();
}
export function safeUrl(value,hosts){
 text(value,2048);if(/[\s\\]/u.test(value)||!/^https:\/\//.test(value))throw invalid();let u;
 try{u=new URL(value);}catch{throw invalid();}
 // Evidence is stored, never fetched. Exact reviewed hosts/URLs; no redirect or proxy surface.
 if(u.protocol!=='https:'||u.username||u.password||u.port||u.hash||isIP(u.hostname)||u.hostname.includes(':')||u.hostname.endsWith('.')||!hosts.includes(u.hostname)||u.origin!==`https://${u.hostname}`||u.href!==value)throw invalid();
 return value;
}
// No license/field/source policy is invented here. Empty policy denies all input.
// Reviewed source evidence and permitted metadata fields must be supplied by trusted composition.
export class Validator {
 constructor({fields={},intendedLicenses=[],sources=[]}={}){
  this.fields=structuredClone(fields);this.intendedLicenses=[...intendedLicenses];this.sources=structuredClone(sources);
 }
 payload(value){
  version(value,['schemaVersion','target','changeKind','fields','intendedLicense','contributorNote']);
  const t=value.target;allowObject(t,['kind','catalogueUuid','externalIdentity']);
  if(!kinds.includes(t.kind)||(!t.catalogueUuid&&!t.externalIdentity))throw invalid();
  if(t.catalogueUuid)uuid(t.catalogueUuid);
  if(t.externalIdentity){
   const e=t.externalIdentity;allowObject(e,['provider','providerKind','externalId']);
   text(e.provider,40);text(e.providerKind,40);text(e.externalId,128);
   const allowed={wikidata:{[t.kind]:/^Q[1-9][0-9]*$/},musicbrainz:{artist:/^[0-9a-f-]{36}$/i,work:/^[0-9a-f-]{36}$/i,recording:/^[0-9a-f-]{36}$/i,release:/^[0-9a-f-]{36}$/i,album:/^[0-9a-f-]{36}$/i},openopus:{work:/^[1-9][0-9]*$/,artist:/^[1-9][0-9]*$/}};
   if(e.providerKind!==t.kind||!allowed[e.provider]?.[e.providerKind]?.test(e.externalId))throw invalid();
   if(e.provider==='musicbrainz')uuid(e.externalId);
  }
  if(!['new_metadata','correction'].includes(value.changeKind)||!this.intendedLicenses.includes(value.intendedLicense))throw invalid();
  if(value.contributorNote!==undefined)text(value.contributorNote,1000);
  if(!Array.isArray(value.fields)||value.fields.length<1||value.fields.length>20)throw invalid();
  const names=new Set();let urls=0;
  for(const f of value.fields){
   allowObject(f,['field','newValue','evidence']);const rule=this.fields[t.kind]?.[f.field];
   if(!rule||names.has(f.field))throw invalid();names.add(f.field);
   // This local slice supports reviewed plain-text metadata only, never arbitrary nested JSON.
   if(rule.type!=='text'||!Number.isInteger(rule.maxBytes)||rule.maxBytes<1||rule.maxBytes>2048)throw invalid();text(f.newValue,rule.maxBytes);
   if(!Array.isArray(f.evidence)||f.evidence.length<1||f.evidence.length>10)throw invalid();
   for(const e of f.evidence){
    allowObject(e,['type','value','license']);if(!['source_url','reference'].includes(e.type))throw invalid();text(e.license,80);
    if(e.type==='source_url'){if(++urls>10)throw invalid();safeUrl(e.value,this.sources.map(s=>{try{return new URL(s.value).hostname;}catch{return '';}}));}else text(e.value,500);
    if(!this.sources.some(s=>s.type===e.type&&s.value===e.value&&s.license===e.license&&s.kind===t.kind&&s.fields.includes(f.field)&&s.intendedLicenses.includes(value.intendedLicense)))throw invalid();
   }
  }
  return value;
 }
}
