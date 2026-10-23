import {randomUUID} from 'node:crypto';
import {allowObject,invalid,failure} from '../../stage6-auth-local/src/errors.mjs';
import {bounded} from './validation.mjs';
export class CommunityApi {
 constructor({submissions,moderation,logger=()=>{}}){Object.assign(this,{submissions,moderation,logger});}
 async invoke(request){const requestId=randomUUID();try{
  allowObject(request,['operation','accessToken','body']);
  // Snapshot before awaiting: caller mutation cannot change validation/hash/effects between awaits.
  bounded(request.body);const body=structuredClone(request.body);let result;
  switch(request.operation){
   case 'submission.create':result=await this.submissions.create(request.accessToken,body,requestId);break;
   case 'submission.revise':result=await this.submissions.revise(request.accessToken,body,requestId);break;
   case 'submission.withdraw':result=await this.submissions.withdraw(request.accessToken,body,requestId);break;
   case 'submission.read':result=await this.submissions.read(request.accessToken,body);break;
   case 'submission.mine':result=await this.submissions.list(request.accessToken,body);break;
   case 'moderation.queue':result=await this.submissions.list(request.accessToken,body,true);break;
   case 'moderation.open':case 'moderation.decide':result=await this.moderation.act(request.accessToken,request.operation,body,requestId);break;
   default:throw invalid();
  }
  return {...result,body:{requestId,...result.body}};
 }catch(e){const response=failure(e,requestId);try{this.logger({event:'request.denied',requestId,status:response.status,code:response.body.error.code});}catch{}return response;}}
}
