import {CommunityApi} from '../../stage6-handlers-local/src/local-api.mjs';
import {Cursors} from './cursor.mjs';
import {CatalogueService} from './catalogue.mjs';
import {AdminService} from './admin.mjs';
import {AbuseProtection,ProtectedSubmissions,ProtectedModeration} from './abuse.mjs';
export function compose({connect,schema,authorization,idempotency,validator,clock,cursorKey,ipKey,limits={},logger,onBoundary}){
 const cursors=new Cursors(cursorKey),abuse=new AbuseProtection({...limits,schema,clock,key:ipKey,onBoundary});
 const common={authorization,idempotency,validator,abuse};
 const submissions=new ProtectedSubmissions(common),moderation=new ProtectedModeration({...common,onBoundary});
 const catalogue=new CatalogueService({connect,schema,cursors}),admin=new AdminService({authorization,idempotency,cursors,onBoundary});
 const extensions={};for(const op of ['catalogue.overlay','catalogue.changes'])extensions[op]=(_token,body)=>catalogue.read(op,body);
 for(const op of ['admin.quarantine','admin.rollback'])extensions[op]=(token,body,id)=>admin.entity(token,op,body,id);
 for(const op of ['admin.roles','admin.suspend'])extensions[op]=(token,body,id)=>admin.principal(token,op,body,id);
 extensions['admin.audit']=(token,body)=>admin.audit(token,body);
 return {api:new CommunityApi({submissions,moderation,extensions,logger}),abuse,admin,catalogue,submissions,moderation};
}
