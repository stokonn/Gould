import {randomUUID} from 'node:crypto';

const reads=new Set(['/v1/catalogue/overlay','/v1/catalogue/changes']);
/** Public catalogue mode never dispatches identity, private records or writes.
 * Routing remains independently disabled until publication prerequisites pass. */
export function catalogueBoundary(request,env){
 if(env.CATALOGUE_READ_ONLY!=='true')return null;
 const allowed=reads.has(new URL(request.url).pathname);
 if(allowed&&request.method==='GET')return null;
 const status=allowed?405:404,requestId=randomUUID();
 return new Response(JSON.stringify({schemaVersion:1,error:{code:allowed?'method_not_allowed':'not_found',message:allowed?'Method not allowed':'Resource not found',requestId}}),{status,headers:{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':requestId,...(allowed?{Allow:'GET'}:{})}});
}
