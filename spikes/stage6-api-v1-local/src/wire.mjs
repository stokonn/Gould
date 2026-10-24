import {createHmac,timingSafeEqual} from 'node:crypto';
import {invalid,unavailable} from '../../stage6-auth-local/src/errors.mjs';
export const MAX=9223372036854775807n;
export function decimal(value,{positive=false}={}){
 if(typeof value!=='string'||!/^(0|[1-9][0-9]*)$/.test(value)||value.length>19||BigInt(value)>MAX||(positive&&value==='0'))throw invalid();return value;
}
export function localRevision(value,{positive=false}={}){
 decimal(value,{positive});if(BigInt(value)>BigInt(Number.MAX_SAFE_INTEGER))throw unavailable();return Number(value);
}
export function readRevision(value,{positive=false}={}){
 // Accept an int8 driver string or a provably exact local numeric result, never rounded data.
 if(typeof value==='number'){if(!Number.isSafeInteger(value)||value<0)throw unavailable();value=String(value);}
 try{return decimal(value,{positive});}catch{throw unavailable();}
}
export function id(value){if(typeof value!=='string'||!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/.test(value))throw invalid();return value;}
export function timestamp(value,{authMilliseconds=false}={}){
 if(authMilliseconds&&value instanceof Date)value=value.toISOString();
 if(authMilliseconds&&typeof value==='string'&&/\.\d{3}Z$/.test(value))value=value.replace(/(\.\d{3})Z$/,'$1000Z');
 if(typeof value!=='string'||!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{6}Z$/.test(value))throw invalid();
 const date=new Date(value);if(!Number.isFinite(+date)||date.toISOString().slice(0,19)!==value.slice(0,19))throw invalid();return value;
}
export function object(value,keys,required=keys){
 if(!value||Object.getPrototypeOf(value)!==Object.prototype||Reflect.ownKeys(value).some(k=>typeof k!=='string'||!keys.includes(k)||!Object.getOwnPropertyDescriptor(value,k)?.enumerable||!Object.hasOwn(Object.getOwnPropertyDescriptor(value,k),'value'))||required.some(k=>!Object.hasOwn(value,k)))throw invalid();
 return value;
}
export function secret(value){if(typeof value!=='string'||!value.isWellFormed()||!value.length||Buffer.byteLength(value)>8192)throw invalid();return value;}
// A bounded parser rejects duplicate keys before JSON.parse could silently erase them.
export function parseBody(input){
 let raw;
 if(Buffer.isBuffer(input)){try{raw=new TextDecoder('utf-8',{fatal:true,ignoreBOM:true}).decode(input);}catch{throw invalid();}}
 else raw=input;
 if(typeof raw!=='string'||!raw.isWellFormed()||Buffer.byteLength(raw)>16384)throw invalid();
 let i=0,nodes=0;
 const ws=()=>{while(/[\x20\x09\x0a\x0d]/.test(raw[i]??'!'))i++;};
 const string=()=>{const start=i++;let escaped=false;for(;i<raw.length;i++){const c=raw[i];if(!escaped&&c==='"'){i++;const v=JSON.parse(raw.slice(start,i));if(!v.isWellFormed())throw invalid();return v;}if(!escaped&&c==='\\')escaped=true;else escaped=false;}throw invalid();};
 function value(depth){
  ws();if(depth>7||++nodes>1000)throw invalid();const start=i;
  if(raw[i]==='"')return string();
  if(raw[i]==='{'){
   i++;ws();const keys=new Set();if(raw[i]==='}'){i++;return;}
   while(i<raw.length){if(raw[i]!=='"')throw invalid();const k=string();if(keys.has(k)||['__proto__','constructor','prototype'].includes(k))throw invalid();keys.add(k);ws();if(raw[i++]!==':')throw invalid();value(depth+1);ws();if(raw[i]==='}'){i++;return;}if(raw[i++]!==',')throw invalid();ws();}throw invalid();
  }
  if(raw[i]==='['){i++;ws();if(raw[i]===']'){i++;return;}while(i<raw.length){value(depth+1);ws();if(raw[i]===']'){i++;return;}if(raw[i++]!==',')throw invalid();}throw invalid();}
  while(i<raw.length&&!/[\s,\]}]/.test(raw[i]))i++;
  if(i===start)throw invalid();const v=JSON.parse(raw.slice(start,i));if(typeof v==='number'&&!Number.isSafeInteger(v))throw invalid();
 }
 try{value(0);ws();if(i!==raw.length)throw invalid();return JSON.parse(raw);}catch{throw invalid();}
}
const micros=v=>BigInt(Date.parse(v.slice(0,19)+'Z'))*1000n+BigInt(v.slice(20,26));
function cursorShape(v){
 object(v,['version','scope','principalId','filters','order','issuedAt','expiresAt','after','highwater','headHash'],['version','scope','principalId','filters','order','issuedAt','expiresAt','after']);
 if(v.version!==1||!['queue','audit'].includes(v.scope))throw invalid();id(v.principalId);timestamp(v.issuedAt);timestamp(v.expiresAt);
 if(micros(v.expiresAt)-micros(v.issuedAt)!==900000000n)throw invalid();
 const keys=v.scope==='queue'?['status','kind','q']:['action','actorId','subjectId','from','to','q'];object(v.filters,keys,[]);
 for(const [k,x] of Object.entries(v.filters)){if(typeof x!=='string'||!x.length||Buffer.byteLength(x)>200||/[\x00-\x1f\x7f]/.test(x))throw invalid();if(!x.trim())throw invalid();if(k==='status'&&!['submitted','in_review','changes_requested'].includes(x))throw invalid();if(k==='kind'&&!['work','arrangement','artist','album','release','recording','collection'].includes(x))throw invalid();if(['actorId','subjectId'].includes(k))id(x);if(['from','to'].includes(k))timestamp(x);}
 if(v.filters.from&&v.filters.to&&v.filters.from>=v.filters.to)throw invalid();
 if(v.scope==='queue'){
  if(v.order!=='createdAt,id'||Object.hasOwn(v,'highwater')||Object.hasOwn(v,'headHash'))throw invalid();
  object(v.after,['createdAt','id']);timestamp(v.after.createdAt);id(v.after.id);
 }else{
  if(v.order!=='seq'||!/^[0-9a-f]{64}$/.test(v.headHash))throw invalid();decimal(v.highwater);object(v.after,['seq']);decimal(v.after.seq);if(BigInt(v.after.seq)>BigInt(v.highwater))throw invalid();
 }
 return v;
}
export class CursorCodec {
 constructor({key,clock}){if(!Buffer.isBuffer(key)||key.length<32||typeof clock!=='function')throw new TypeError('explicit local cursor key and clock required');this.key=Buffer.from(key);this.clock=clock;}
 sign(value){const v=cursorShape(value),data=Buffer.from(JSON.stringify(v));const result=Buffer.concat([data,createHmac('sha256',this.key).update(data).digest()]).toString('base64url');if(result.length>4096)throw invalid();return result;}
 read(value,{scope,principalId,filters,order}){
  if(typeof value!=='string'||value.length>4096||!/^[A-Za-z0-9_-]+$/.test(value))throw invalid();
  const data=Buffer.from(value,'base64url');if(data.length<=32||data.toString('base64url')!==value)throw invalid();
  const raw=data.subarray(0,-32),tag=data.subarray(-32);if(!timingSafeEqual(tag,createHmac('sha256',this.key).update(raw).digest()))throw invalid();
  const v=cursorShape(parseBody(raw));
  const canonical=x=>JSON.stringify(Object.fromEntries(Object.entries(x).sort(([a],[b])=>a.localeCompare(b))));
  const now=this.clock();if(!Number.isSafeInteger(now)||micros(v.issuedAt)>BigInt(now)*1000n||micros(v.expiresAt)<=BigInt(now)*1000n||v.scope!==scope||v.principalId!==principalId||v.order!==order||canonical(v.filters)!==canonical(filters))throw invalid();
  return v;
 }
}
