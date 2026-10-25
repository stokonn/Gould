import {randomBytes} from 'node:crypto';
import {createLocalAdapter} from '../../spikes/stage6-api-v1-local/src/adapter.mjs';
import {compose} from '../../spikes/stage6-public-admin-abuse/src/compose.mjs';
import {ProtectedModeration} from '../../spikes/stage6-public-admin-abuse/src/abuse.mjs';
import {Validator} from '../../spikes/stage6-handlers-local/src/validation.mjs';
import {ApiError,invalid,unauthenticated,unavailable} from '../../spikes/stage6-auth-local/src/errors.mjs';
import {id,object,parseBody,decimal} from '../../spikes/stage6-api-v1-local/src/wire.mjs';
import {Cursors} from '../../spikes/stage6-public-admin-abuse/src/cursor.mjs';
import {AuthThrottle} from './auth-throttle.mjs';

const routes=[
 [/^\/v1\/submissions$/,['POST'],'submission.create'],
 [/^\/v1\/submissions\/mine$/,['GET'],'submission.mine'],
 [/^\/v1\/submissions\/([^/]+)$/,['GET'],'submission.read'],
 [/^\/v1\/submissions\/([^/]+)\/revisions$/,['POST'],'submission.revise'],
 [/^\/v1\/submissions\/([^/]+)\/withdraw$/,['POST'],'submission.withdraw'],
 [/^\/v1\/catalogue\/overlay$/,['GET'],'catalogue.overlay'],
 [/^\/v1\/catalogue\/changes$/,['GET'],'catalogue.changes'],
 [/^\/v1\/admin\/entities\/([^/]+)\/quarantine$/,['POST'],'admin.quarantine'],
 [/^\/v1\/admin\/entities\/([^/]+)\/rollback$/,['POST'],'admin.rollback'],
 [/^\/v1\/admin\/principals\/([^/]+)\/roles$/,['POST'],'admin.roles'],
 [/^\/v1\/admin\/principals\/([^/]+)\/suspend$/,['POST'],'admin.suspend'],
];

/** Executable composition, never migrates or seeds a database. The canonical
 * session/Admin DTO path is unchanged; additional operations use the accepted
 * contribution/catalogue/domain fields, with lossless decimal revisions. */
export function createBffAdapter({connect,schema,verifier,clock=Date.now,validator=new Validator(),cursorKey=randomBytes(32),ipKey=randomBytes(32),limits={}}) {
 const core=createLocalAdapter({localOnly:true,connect,schema,verifier,clock,validator,cursorKey});
 const operations=compose({connect,schema,authorization:core.authorization,idempotency:core.idempotency,validator,clock,cursorKey,ipKey,limits});
 const signed=new Cursors(cursorKey);
 const authThrottle=new AuthThrottle({connect,schema,key:ipKey,clock,limit:limits.authBurst??30});
 const cursors={sign:value=>signed.sign({...value,high:value.high??'0',expiresAt:clock()+900000}),read:(value,scope)=>{const decoded=signed.read(value,scope);if(!Number.isSafeInteger(decoded.expiresAt)||decoded.expiresAt<=clock()||decoded.expiresAt>clock()+900000)throw invalid();return decoded;}};
 operations.catalogue.cursors=cursors;
 // Canonical moderation keeps its public receipt transaction and now enforces
 // the very same intake switch as contribution creation and revision.
 core.moderationFactory=options=>new ProtectedModeration({...options,abuse:operations.abuse});
 return {core,operations,authorization:core.authorization,sessions:core.sessions,async invoke(request,transport={}) {
  const route=typeof request.path==='string'?request.path.split('?')[0]:'';
  const spec=routes.find(([pattern])=>pattern.test(route));
  if(!spec){
   if(request.method==='POST'&&['/v1/session/challenge','/v1/session','/v1/session/refresh'].includes(route)){
    try{await authThrottle.consume(transport.remoteAddress);}
    catch(error){
     const e=error instanceof ApiError?error:unavailable(),requestId=crypto.randomUUID();
     return {status:e.status,headers:{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':requestId,...(e.status===429?{'Retry-After':String(e.retryAfter)}:{})},body:{schemaVersion:1,error:{code:e.code,message:e.status===429?'Authentication limit reached':e.status===400?'Invalid request':'Service unavailable',requestId}}};
    }
   }
   return core.invoke(request);
  }
  let requestId=crypto.randomUUID();
  const reply=(status,body,extra={})=>({status,headers:{'X-Request-ID':requestId,'Content-Type':'application/json','Cache-Control':'no-store',...extra},body});
  try{
   object(request,['method','path','headers','body'],['method','path']);
   const [pattern,methods,operation]=spec,match=pattern.exec(route);
   if(!methods.includes(request.method))return reply(405,{schemaVersion:1,error:{code:'method_not_allowed',message:'Method not allowed',requestId}},{Allow:methods.join(', ')});
   const headers=new Headers(request.headers),url=new URL(request.path,'https://private.invalid');
   if(url.hash||url.pathname!==route)throw invalid();
   let body={schemaVersion:1};
   if(request.method==='POST'){
    if(url.search||!/^application\/json(?:;[ \t]*charset=utf-8)?$/i.test(headers.get('content-type')??''))throw invalid();
    body=parseBody(request.body);if(body.schemaVersion!==1)throw invalid();
    // Path identity is authoritative; supplied aliases must never overwrite it.
    if(match[1]){if(Object.hasOwn(body,'id')||Object.hasOwn(body,'principalId'))throw invalid();body={...body,[operation.startsWith('admin.')&&operation.match(/roles|suspend/)?'principalId':'id']:id(match[1])};}
    for(const key of ['expectedRevision','expectedSubmissionRevision','expectedEventSeq','targetRevision'])if(Object.hasOwn(body,key))decimal(body[key],{positive:key!=='expectedRevision'});
    id(body.idempotencyKey);
   }else{
    if(request.body!==undefined)throw invalid();
    const allowed=operation==='catalogue.overlay'?['kind','cursor']:operation==='submission.mine'||operation==='catalogue.changes'?['cursor']:[];
    const seen=new Set();for(const [key,value] of url.searchParams){if(!allowed.includes(key)||seen.has(key)||!value)throw invalid();seen.add(key);body[key]=value;}
    if(match[1])body.id=id(match[1]);
    // Own-list domain cursor is structured; wire cursor uses a signed opaque
    // token bound to this operation. A client cannot supply SQL keyset objects.
   }
   let accessToken;
   if(!operation.startsWith('catalogue.')){const value=headers.get('authorization');if(!/^Bearer [A-Za-z0-9_-]+$/.test(value??''))throw unauthenticated();accessToken=value.slice(7);}
   let principal;
   if(operation==='submission.mine'){
    principal=await core.sessions.withSession(accessToken,async(_c,ctx)=>ctx.principalId);
    if(body.cursor){const c=cursors.read(body.cursor,'submission.mine');if(c.principal!==principal)throw invalid();body.cursor={createdAt:c.createdAt,id:c.id};}
   }
   const result=await operations.api.invoke({operation,body,...(accessToken?{accessToken}:{})},transport);
   requestId=result.body.requestId;
   if(result.status>=400)return reply(result.status,{schemaVersion:1,error:{code:result.body.error.code,message:result.body.error.message,requestId}},result.headers);
   let value=result.body;
   if(operation==='submission.read'){
    const s=value.submission;
    // Never return the raw database header (identity, mapping, driver dates).
    value={requestId,submission:{id:s.id,kind:s.target_kind,status:s.status,expectedRevision:s.expected_revision,revision:s.current_revision},revisions:value.revisions,reviews:value.reviews};
   }
   if(operation==='submission.mine'&&value.nextCursor)value={...value,nextCursor:cursors.sign({version:1,scope:'submission.mine',principal,...value.nextCursor})};
   if(Object.hasOwn(value,'revision'))value={...value,revision:String(value.revision)};
   return reply(result.status,{schemaVersion:1,...value});
  }catch(error){
   const e=error instanceof ApiError?error:unavailable();
   const messages={400:'Invalid request',401:'Authentication required',403:'Operation not permitted',404:'Resource not found',409:'Revision or state conflict',422:'Key already used for a different request',429:'Submission limit reached',503:'Service unavailable'};
   return reply(e.status,{schemaVersion:1,error:{code:e.code,message:messages[e.status]??'Service unavailable',requestId}});
  }
 }};
}
