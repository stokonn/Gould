// Lifecycle for local coarse-IP expiry cleanup, independent of intake/kill switch.
// Explicit start/stop: importing or composing services never starts background work.
export class RetentionJanitor {
 constructor({sweep,intervalMs,setTimer=setInterval,clearTimer=clearInterval,onResult=()=>{}}){
  if(typeof sweep!=='function'||!Number.isSafeInteger(intervalMs)||intervalMs<1)throw new TypeError('explicit cleanup task and interval required');
  Object.assign(this,{sweep,intervalMs,setTimer,clearTimer,onResult});this.running=false;this.timer=null;this.inFlight=null;
 }
 start(){
  if(this.running)return;
  this.running=true;
  try{this.timer=this.setTimer(()=>{if(this.running)void this.run();},this.intervalMs);this.timer?.unref?.();}
  catch(error){this.running=false;throw error;}
  void this.run();
 }
 run(){
  if(this.inFlight)return this.inFlight;
  // A rejected store operation is retried at the next tick. Never emit SQL/errors,
  // peer values, hash keys, principal identifiers or exception text to diagnostics.
  this.inFlight=Promise.resolve().then(()=>this.sweep()).then(()=>true,()=>false).then(ok=>{
   try{this.onResult({event:'coarse_ip.cleanup',ok});}catch{}
   return ok;
  }).finally(()=>{this.inFlight=null;});
  return this.inFlight;
 }
 async stop(){
  this.running=false;
  if(this.timer!==null){this.clearTimer(this.timer);this.timer=null;}
  if(this.inFlight)await this.inFlight;
 }
}
