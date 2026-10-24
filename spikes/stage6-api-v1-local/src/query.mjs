import {invalid} from '../../stage6-auth-local/src/errors.mjs';

const statuses=new Set(['submitted','in_review','changes_requested']);
const kinds=new Set(['work','arrangement','artist','album','release','recording','collection']);
const actions=new Set([
 'submission.created','submission.revised','submission.withdrawn',
 'in_review->approved','approved->published','in_review->rejected',
 'in_review->changes_requested'
]);
const controls=/[\u0000-\u001f\u007f]/;
const text=value=>{
 if(typeof value!=='string'||!value.trim()||Buffer.byteLength(value)>200||controls.test(value))throw invalid();
 return value;
};
export function queueQuery(query={}){
 if(!query||typeof query!=='object'||Array.isArray(query))throw invalid();
 const allowed=['status','kind','q','limit','cursor'];
 if(Object.keys(query).some(key=>!allowed.includes(key)))throw invalid();
 if(query.status!==undefined&&!statuses.has(query.status))throw invalid();
 if(query.kind!==undefined&&!kinds.has(query.kind))throw invalid();
 if(query.q!==undefined)text(query.q);
 const limit=query.limit===undefined?'20':query.limit;
 if(typeof limit!=='string'||!/^(?:[1-9]|1[0-9]|20)$/.test(limit))throw invalid();
 if(query.cursor!==undefined&&(typeof query.cursor!=='string'||!query.cursor.length||query.cursor.length>4096||!/^[A-Za-z0-9_-]+$/.test(query.cursor)))throw invalid();
 return {status:query.status,kind:query.kind,q:query.q,limit,cursor:query.cursor};
}
export function auditQuery(query={}){
 if(!query||typeof query!=='object'||Array.isArray(query))throw invalid();
 const allowed=['action','actorId','subjectId','from','to','q','limit','cursor'];
 if(Object.keys(query).some(key=>!allowed.includes(key)))throw invalid();
 if(query.action!==undefined&&(typeof query.action!=='string'||!actions.has(query.action)))throw invalid();
 for(const key of ['actorId','subjectId'])if(query[key]!==undefined&&!/^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/.test(query[key]))throw invalid();
 for(const key of ['from','to'])if(query[key]!==undefined&&!/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{6}Z$/.test(query[key]))throw invalid();
 if(query.from!==undefined&&query.to!==undefined&&query.from>=query.to)throw invalid();
 if(query.q!==undefined)text(query.q);
 const limit=query.limit===undefined?'20':query.limit;
 if(typeof limit!=='string'||!/^(?:[1-9]|1[0-9]|20)$/.test(limit))throw invalid();
 if(query.cursor!==undefined&&(typeof query.cursor!=='string'||query.cursor.length>4096||!/^[A-Za-z0-9_-]+$/.test(query.cursor)))throw invalid();
 return {...query,limit};
}
