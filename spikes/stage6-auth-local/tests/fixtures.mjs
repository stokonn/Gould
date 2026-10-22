import {generateKeyPair,exportJWK,SignJWT} from 'jose';
export const issuer='https://identity.synthetic.invalid';
export const audience='goldberg-local-auth';
export class FakeClock {
 constructor(){this.ms=Date.parse('2030-01-01T00:00:00Z');}
 now=()=>this.ms;
 advance(ms){this.ms+=ms;}
}
export class FakeJwks {
 constructor(keys){this.keys=keys;this.calls=0;this.available=true;this.maxAgeSeconds=2;this.hang=false;}
 fetch=async()=>{this.calls++;if(this.hang)return new Promise(()=>{});if(!this.available)throw new Error('synthetic provider unavailable');return {jwks:{keys:structuredClone(this.keys)},maxAgeSeconds:this.maxAgeSeconds};};
}
export async function makeKey(kid){const pair=await generateKeyPair('RS256');const jwk=await exportJWK(pair.publicKey);return {...pair,jwk:{...jwk,kid,alg:'RS256',use:'sig'},kid};}
export async function credential(key,clock,{nonce='synthetic-nonce',subject='synthetic-subject',claims={},header={}}={}){
 const now=Math.floor(clock.now()/1000);
 return new SignJWT({iss:issuer,aud:audience,sub:subject,iat:now,exp:now+120,nonce,...claims})
  .setProtectedHeader({alg:'RS256',kid:key.kid,typ:'JWT',...header}).sign(key.privateKey);
}
