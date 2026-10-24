import {createServer} from 'node:http';
import {randomUUID} from 'node:crypto';
import {canonical} from '../../stage6-handlers-local/src/idempotency.mjs';

const MAX_BODY_BYTES=16384;
const requestId=()=>randomUUID();
const errorBody=(id,code='validation_failed',message='Invalid request')=>JSON.stringify({
 schemaVersion:1,requestId:id,error:{code,message,requestId:id}
});

function write(response,status,headers,body){
 response.writeHead(status,headers);
 if(status!==204&&body!==null)response.end(body);
 else response.end();
}

export function createLocalHttpServer({adapter,maxBodyBytes=MAX_BODY_BYTES}){
 if(!adapter||typeof adapter.invoke!=='function'||!Number.isSafeInteger(maxBodyBytes)||maxBodyBytes<1)throw new TypeError('explicit local adapter and body limit required');
 return createServer(async(request,response)=>{
  const id=requestId();
  try{
   if(request.method==='CONNECT'||request.method==='TRACE')return write(response,405,{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':id,'Allow':'GET, POST, DELETE'},errorBody(id,'method_not_allowed','Method not allowed'));
   const url=new URL(request.url??'/',`http://${request.headers.host??'127.0.0.1'}`);
   const length=request.headers['content-length'];
   if(length!==undefined&&(!/^(?:0|[1-9][0-9]*)$/.test(length)||Number(length)>maxBodyBytes)){
    request.resume();await new Promise(resolve=>request.once('end',resolve));
    return write(response,413,{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':id},errorBody(id,'validation_failed','Invalid request'));
   }
   const chunks=[];let size=0;
   for await(const chunk of request){
    size+=chunk.length;
    if(size>maxBodyBytes)return write(response,413,{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':id},errorBody(id,'validation_failed','Invalid request'));
    chunks.push(chunk);
   }
   const headers=Object.fromEntries(Object.entries(request.headers).filter(([key])=>!key.startsWith(':')).map(([key,value])=>[key,Array.isArray(value)?value.join(','):value]));
   const result=await adapter.invoke({
    method:request.method??'',
    path:url.pathname+(url.search||''),
    headers,
    ...(chunks.length?{body:Buffer.concat(chunks)}:{})
   });
   const responseHeaders={...result.headers,'X-Request-ID':result.headers?.['X-Request-ID']??id};
   const body=result.status===204?null:canonical(result.body);
   if(body!==null)responseHeaders['Content-Length']=String(Buffer.byteLength(body));
   write(response,result.status,responseHeaders,body);
  }catch{
   write(response,503,{'Content-Type':'application/json','Cache-Control':'no-store','X-Request-ID':id},errorBody(id,'service_unavailable','Service unavailable'));
  }
 });
}

export async function listenLocalHttp(server){
 await new Promise((resolve,reject)=>{
  const onError=error=>{server.off('listening',onListening);reject(error);};
  const onListening=()=>{server.off('error',onError);resolve();};
  server.once('error',onError);server.once('listening',onListening);server.listen({host:'127.0.0.1',port:0});
 });
 const address=server.address();
 if(!address||typeof address!=='object'||address.address!=='127.0.0.1'||!Number.isInteger(address.port)||address.port<1)throw new Error('local listener binding failed');
 return `http://127.0.0.1:${address.port}`;
}
