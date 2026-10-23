import {createHmac,timingSafeEqual} from 'node:crypto';
import {invalid} from '../../stage6-auth-local/src/errors.mjs';
export function decimal(v,{zero=true}={}){
 if(typeof v!=='string'||!/^(0|[1-9][0-9]*)$/.test(v)||BigInt(v)>9223372036854775807n||(!zero&&v==='0'))throw invalid();return v;
}
export const kinds=Object.freeze(['work','arrangement','artist','album','release','recording','collection']);
export class Cursors {
 constructor(key){if(!Buffer.isBuffer(key)||key.length<32)throw new TypeError('server cursor key required');this.key=Buffer.from(key);}
 sign(value){const raw=Buffer.from(JSON.stringify(value)).toString('base64url');return raw+'.'+createHmac('sha256',this.key).update(raw).digest('base64url');}
 read(value,scope){
  if(typeof value!=='string'||value.length>2048||!/^[A-Za-z0-9_-]+\.[A-Za-z0-9_-]{43}$/.test(value))throw invalid();
  const [raw,tag]=value.split('.'),expected=createHmac('sha256',this.key).update(raw).digest();
  if(Buffer.from(tag,'base64url').toString('base64url')!==tag||!timingSafeEqual(Buffer.from(tag,'base64url'),expected))throw invalid();
  let v;try{v=JSON.parse(Buffer.from(raw,'base64url').toString('utf8'));}catch{throw invalid();}
  if(v?.scope!==scope||v.version!==1)throw invalid();decimal(v.high);return v;
 }
}
