// In-process local transport adapter; no listener, URL configuration or deploy entry point.
import {randomUUID} from 'node:crypto';
import {allowObject,forbidden,failure} from './errors.mjs';
export class LocalApi {
 constructor({sessions,authorization,logger=()=>{}}){Object.assign(this,{sessions,authorization,logger});}
 async invoke(request){
  const requestId=randomUUID();
  try{
   allowObject(request,['operation','accessToken','body']);const body=request.body??{};
   let result,status=200;
   switch(request.operation){
    case 'challenge':allowObject(body,[]);result=await this.sessions.challenge();break;
    case 'login':allowObject(body,['credential','challenge']);result=await this.sessions.login(body,requestId);status=201;break;
    case 'refresh':allowObject(body,['refreshToken']);result=await this.sessions.refresh(body.refreshToken,requestId);break;
    case 'logout':allowObject(body,[]);await this.sessions.logout(request.accessToken,requestId);status=204;break;
    case 'capabilities':allowObject(body,[]);result={capabilities:await this.authorization.capabilities(request.accessToken)};break;
    case 'session':allowObject(body,[]);result=await this.sessions.authenticate(request.accessToken);break;
    case 'submission.read':allowObject(body,['id']);result=await this.authorization.submission(request.accessToken,body.id,'read');break;
    case 'submission.revise.guard':allowObject(body,['id']);result=await this.authorization.submission(request.accessToken,body.id,'revise');break;
    case 'decision.guard':allowObject(body,['id']);result=await this.authorization.submission(request.accessToken,body.id,'decide');break;
    case 'roles.grant':allowObject(body,['principalId','role']);result=await this.authorization.changeRole(request.accessToken,body,false,requestId);break;
    case 'roles.revoke':allowObject(body,['principalId','role']);result=await this.authorization.changeRole(request.accessToken,body,true,requestId);break;
    case 'principal.suspend':allowObject(body,['principalId','suspended']);result=await this.authorization.suspend(request.accessToken,body,requestId);break;
    case 'audit.read':allowObject(body,[]);result=await this.authorization.require(request.accessToken,'audit.read',async(c)=>
      ({events:(await c.query(`SELECT seq::text,action,request_id FROM ${this.sessions.q}.audit_events ORDER BY seq DESC LIMIT 20`)).rows}));break;
    default:throw forbidden();
   }
   return {status,body:status===204?null:{requestId,...result}};
  }catch(error){
   const response=failure(error,requestId);
   // Logging never receives exception messages/objects, request bodies, claims, subjects or tokens.
   try{this.logger({event:'request.denied',requestId,code:response.body.error.code,status:response.status});}catch{}
   return response;
  }
 }
}
