import {randomUUID} from 'node:crypto';
import {canonical} from '../../spikes/stage6-handlers-local/src/idempotency.mjs';

const failure=(status,code,id)=>new Response(JSON.stringify({schemaVersion:1,error:{code,message:status===400?'Invalid request':'Service unavailable',requestId:id}}),{status,headers:{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':id}});

/** Shared Workers Fetch entrypoint. Adapter and request-scoped DB cleanup are
 * explicit dependencies; no listening socket, remote session or deployment. */
export function createFetchHandler({createAdapter,maxBodyBytes=16384,timeoutMs=10000}) {
 if(typeof createAdapter!=='function'||!Number.isSafeInteger(maxBodyBytes)||maxBodyBytes<1||maxBodyBytes>16384||!Number.isSafeInteger(timeoutMs)||timeoutMs<1||timeoutMs>30000)throw new TypeError('bounded BFF configuration required');
 return async function handle(request,env,context={}) {
  const id=randomUUID(),controller=new AbortController();let timer,reader,scope,rejectAbort,closing;
  const closeScope=()=>{if(scope&&!closing)closing=Promise.resolve().then(()=>scope.close?.());return closing;};
  controller.signal.addEventListener('abort',()=>{void closeScope()?.catch(()=>{});},{once:true});
  const cancellation=new Promise((_,reject)=>{rejectAbort=reject;});
  const aborted=()=>{controller.abort();rejectAbort(new Error('cancelled'));};request.signal.addEventListener('abort',aborted,{once:true});
  const timeout=new Promise((_,reject)=>{timer=setTimeout(()=>{controller.abort();reject(new Error('deadline'));},timeoutMs);});
  const work=(async()=>{
   if(request.signal.aborted)throw new Error('cancelled');
   const url=new URL(request.url);
   if(url.username||url.password)return failure(400,'validation_failed',id);
   const length=request.headers.get('content-length');
   if(length!==null&&(!/^(0|[1-9][0-9]*)$/.test(length)||Number(length)>maxBodyBytes))return failure(413,'validation_failed',id);
   const chunks=[];let size=0;
   if(request.body){
    reader=request.body.getReader();
    while(true){const {done,value}=await reader.read();if(done)break;size+=value.byteLength;if(size>maxBodyBytes){await reader.cancel();return failure(413,'validation_failed',id);}chunks.push(value);}
   }
   if(controller.signal.aborted)throw new Error('cancelled');
   scope=await createAdapter({env,signal:controller.signal,context});
   if(controller.signal.aborted)throw new Error('cancelled');
   const body=Buffer.concat(chunks),result=await scope.adapter.invoke({method:request.method,path:url.pathname+url.search,headers:Object.fromEntries(request.headers),...(body.length?{body}:{})},{remoteAddress:context.remoteAddress});
   const headers=new Headers(result.headers);headers.set('Cache-Control','no-store');
   // Receipt requestId survives exact replay, including its HTTP header.
   if(!headers.has('X-Request-ID'))headers.set('X-Request-ID',id);
   const responseBody=result.status===204?null:canonical(result.body);
   if(responseBody!==null&&Buffer.byteLength(responseBody)>1048576)throw new Error('response limit');
   return new Response(responseBody,{status:result.status,headers});
  })();
  // Catch late failures after cancellation. Scope cleanup belongs to work's
  // lifetime, so a late-created connection cannot escape a deadline.
  const settled=work.finally(async()=>{await closeScope();});
  try{return await Promise.race([settled,timeout,cancellation]);}
  catch{return failure(503,'service_unavailable',id);}
  finally{
   clearTimeout(timer);request.signal.removeEventListener('abort',aborted);
   if(controller.signal.aborted){void reader?.cancel().catch(()=>{});context.waitUntil?.(settled.catch(()=>{}));}
  }
 };
}
