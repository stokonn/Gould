import {randomUUID} from 'node:crypto';
import {allowObject,invalid,forbidden,failure} from '../../stage6-auth-local/src/errors.mjs';
import {bounded} from './validation.mjs';
export class CommunityApi {
 constructor({submissions,moderation,extensions={},logger=()=>{}}){Object.assign(this,{submissions,moderation,extensions,logger});}
 async invoke(request,transport={}){const requestId=randomUUID();try{
  allowObject(request,['operation','accessToken','body']);
  if(typeof request.operation!=='string'||request.operation.length===0)throw invalid();
  // Snapshot before awaiting: caller mutation cannot change validation/hash/effects between awaits.
  bounded(request.body);const body=structuredClone(request.body);let result;
  if(Object.hasOwn(this.extensions,request.operation)){result=await this.extensions[request.operation](request.accessToken,body,requestId,transport);}else switch(request.operation){
   case 'submission.create':result=await this.submissions.create(request.accessToken,body,requestId,transport);break;
   case 'submission.revise':result=await this.submissions.revise(request.accessToken,body,requestId,transport);break;
   case 'submission.withdraw':result=await this.submissions.withdraw(request.accessToken,body,requestId,transport);break;
   case 'submission.read':result=await this.submissions.read(request.accessToken,body);break;
   case 'submission.mine':result=await this.submissions.list(request.accessToken,body);break;
   case 'moderation.queue':result=await this.submissions.list(request.accessToken,body,true);break;
   case 'moderation.open':case 'moderation.decide':result=await this.moderation.act(request.accessToken,request.operation,body,requestId,transport);break;
   default:throw forbidden();
  }
  return {...result,body:{requestId,...result.body}};
 }catch(e){const response=failure(e,requestId);if(e?.status===429&&Number.isSafeInteger(e.retryAfter)&&e.retryAfter>0){response.headers={'Retry-After':String(e.retryAfter)};response.body.error.retryAfterSeconds=e.retryAfter;}try{this.logger({event:'request.denied',requestId,status:response.status,code:response.body.error.code});}catch{}return response;}}
}
