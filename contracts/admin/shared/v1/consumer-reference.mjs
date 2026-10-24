// Executable integration reference only. No URL, token persistence, Android wiring
// or production entrypoint. The caller injects the local synthetic request venue.
import {randomUUID} from 'node:crypto';
const fail=code=>Object.assign(new Error(code),{code});
const stable=x=>JSON.stringify(x);
const fingerprint=r=>stable([r.detail.submissionId,r.detail.authorId,r.detail.status,r.detail.expectedEntityRevision,r.detail.submissionRevision,r.detail.revisions,
 r.comparison.state,r.comparison.entityStatus??null,r.comparison.reason??null,r.comparison.entityRevision??null,r.comparison.payloadHash??null,r.comparison.fields??null]);
export class ContractConsumer {
 constructor(request){this.request=request;this.epoch=0;this.tokens=null;this.pending=null;this.refreshing=null;}
 clear(){this.epoch++;this.tokens=null;this.pending=null;this.refreshing=null;}
 invalidateAuthorization(){this.epoch++;this.pending=null;this.refreshing=null;}
 authenticate(tokens){this.clear();this.tokens=structuredClone(tokens);}
 async checked(method,path,body,tokens=this.tokens){
  if(!tokens)throw fail('fresh_authentication_required');
  const epoch=this.epoch,r=await this.request(tokens,method,path,body);
  if(epoch!==this.epoch)throw fail('account_changed');
  if(r.status===401){this.clear();throw fail('fresh_authentication_required');}
  if(r.status===403){this.invalidateAuthorization();throw fail('capability_revoked');}
  return r;
 }
 async session(){
  const r=await this.checked('GET','/v1/session');
  if(r.status!==200)throw fail('session_unavailable');
  if(r.body.principalId!==this.tokens.principalId||r.body.sessionId!==this.tokens.sessionId){this.clear();throw fail('identity_mismatch');}
  return r.body;
 }
 refresh(){
  if(this.refreshing)return this.refreshing;
  if(!this.tokens)return Promise.reject(fail('fresh_authentication_required'));
  const epoch=this.epoch,previous=this.tokens;
  let operation;operation=(async()=>{
   try{
    await Promise.resolve();
    if(epoch!==this.epoch)throw fail('account_changed');
    const r=await this.request(null,'POST','/v1/session/refresh',{schemaVersion:1,refreshToken:previous.refreshToken});
    if(epoch!==this.epoch)throw fail('account_changed');
    if(r.status!==200||r.body.principalId!==previous.principalId||typeof r.body.sessionId!=='string'||r.body.sessionId===previous.sessionId)throw fail('fresh_authentication_required');
    this.tokens=structuredClone(r.body);this.epoch++;
    if(this.pending?.epoch===epoch)this.pending={...this.pending,epoch:this.epoch};
    return this.tokens;
   }catch(e){if(epoch===this.epoch)this.clear();throw e;}
   finally{if(this.refreshing===operation)this.refreshing=null;}
  })();
  this.refreshing=operation;return operation;
 }
 async logout(){
  const prior=this.tokens;this.clear(); // synchronous local clear BEFORE any I/O
  if(!prior)return {locallyCleared:true,revocationConfirmed:false};
  try{const r=await this.request(prior,'DELETE','/v1/session');return {locallyCleared:true,revocationConfirmed:r.status===204&&r.raw===''};}
  catch{return {locallyCleared:true,revocationConfirmed:false};}
 }
 async review(id){
  const caps=await this.checked('GET','/v1/me/capabilities');
  if(caps.status!==200||!caps.body.capabilities.includes('decide')){this.invalidateAuthorization();throw fail('capability_revoked');}
  const d=await this.checked('GET','/v1/admin/submissions/'+id);
  if(d.status!==200)throw fail('detail_unavailable');
  if(d.body.authorId===this.tokens.principalId)throw fail('self_review');
  const entity=d.body.targetEntityId;
  if(!entity)throw fail('comparison_unavailable');
  const c=await this.checked('GET','/v1/admin/entities/'+entity+'/comparison?submissionId='+id);
  if(c.status!==200)throw fail('comparison_unavailable');
  return {epoch:this.epoch,detail:d.body,comparison:c.body};
 }
 async confirm(review,decision,reason,{acknowledged=false}={}){
  if(!acknowledged||review.epoch!==this.epoch||this.pending)throw fail('explicit_reconfirmation_required');
  const fresh=await this.review(review.detail.submissionId);
  if(review.epoch!==this.epoch||fresh.epoch!==review.epoch)throw fail('explicit_reconfirmation_required');
  if(fingerprint(fresh)!==fingerprint(review)||fresh.detail.status!=='in_review')throw fail('refetch_and_reconfirm');
  if(decision==='approve'){
   if(fresh.comparison.state!=='available'&&fresh.detail.expectedEntityRevision!=='0')throw fail('comparison_unavailable');
   if(fresh.comparison.state==='available'&&(fresh.comparison.entityRevision!==fresh.detail.expectedEntityRevision||fresh.comparison.entityStatus!=='published'))throw fail('rebase_required');
  }
  this.pending={epoch:this.epoch,id:review.detail.submissionId,body:{schemaVersion:1,
   expectedEntityRevision:review.detail.expectedEntityRevision,expectedSubmissionRevision:review.detail.submissionRevision,
   decision,idempotencyKey:randomUUID(),...(reason===undefined?{}:{reason})}};
  return this.retryPending();
 }
 async retryPending(){ // explicit caller action only; no implicit decision retry
  const p=this.pending;if(!p||p.epoch!==this.epoch)throw fail('no_pending_intent');
  const caps=await this.checked('GET','/v1/me/capabilities');
  if(caps.status!==200||!caps.body.capabilities.includes('decide')){this.invalidateAuthorization();throw fail('capability_revoked');}
  if(p!==this.pending||p.epoch!==this.epoch)throw fail('account_changed');
  const r=await this.checked('POST','/v1/admin/submissions/'+p.id+'/decisions',p.body);
  if(r.status===409){this.pending=null;throw fail('refetch_and_reconfirm');}
  if(r.status===200||r.status===201){this.pending=null;return r;}
  if([400,404,422].includes(r.status))this.pending=null;
  throw fail('decision_unconfirmed');
 }
}
