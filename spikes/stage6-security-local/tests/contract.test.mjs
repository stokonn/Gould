import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {Cursors,decimal} from '../../stage6-public-admin-abuse/src/cursor.mjs';
import {verifyAudit} from '../../stage6-handlers-local/src/audit.mjs';
const corpus=JSON.parse(await readFile(new URL('../../../contracts/admin/v1/fixtures.json',import.meta.url)));
const imported=JSON.parse(await readFile(new URL('../../../contracts/admin/v1/stage7b-comparison/fixtures.json',import.meta.url)));
const indexed=Object.fromEntries(corpus.cases.map(c=>[c.id,c]));
const get=id=>{assert.ok(indexed[id],id);return indexed[id];};
const response=id=>get(id).response;
test('shared corpus keeps captures proposals and legacy authored cases separate, never frozen',()=>{
 assert.equal(corpus.schemaVersion,1);assert.equal(corpus.frozen,false);assert.equal(corpus.active,false);assert.equal(corpus.httpApproved,false);
 assert.equal(new Set(corpus.cases.map(c=>c.id)).size,41);assert.equal(imported.cases.length,29);
 assert.equal(corpus.cases.filter(c=>c.classification==='actual-local-capture').length,30);assert.equal(corpus.cases.filter(c=>c.classification==='proposal-unapproved').length,11);
 for(const c of corpus.cases){assert.equal(c.synthetic,true);assert.equal(c.httpBinding,null);if(c.classification==='actual-local-capture'){assert.equal(c.verification.executed,true);assert.equal(c.verification.http,false);assert.equal(c.verification.postgres,false);}}
 assert.ok(imported.cases.filter(c=>c.classification==='candidate-local').length>0);assert.ok(!imported.cases.some(c=>c.classification==='actual-local-capture'));
});
test('all eight error classes retain safe distinct envelopes and request correlation',()=>{
 for(const [status,code] of [[400,'validation_failed'],[401,'unauthenticated'],[403,'forbidden'],[404,'not_found'],[409,'conflict'],[422,'idempotency_mismatch'],[429,'rate_limited'],[503,'service_unavailable']]){
  const r=response(status===503?'actual-service-unavailable':'actual-error-'+status);assert.equal(r.status,status);assert.equal(r.body.error.code,code);assert.equal(r.body.error.requestId,r.body.requestId);
  assert.ok(!JSON.stringify(r).match(/SELECT |password=|gs_access_|gs_refresh_|Traceback/));
 }
 const limit=response('actual-error-429');assert.equal(limit.headers['Retry-After'],'2');assert.equal(limit.body.error.retryAfterSeconds,2);
});
test('capabilities do not invent principal binding and logout204 is not legacy revoked JSON',()=>{
 assert.equal(response('actual-auth-schemaVersion-rejected').status,400);const caps=response('actual-capabilities').body;assert.ok(Array.isArray(caps.capabilities));assert.equal(caps.principalId,undefined);
 const session=response('actual-session').body;assert.equal(typeof session.principalId,'string');assert.equal(typeof session.familyId,'string');assert.equal(typeof session.sessionId,'string');
 assert.equal(response('actual-logout-204').status,204);assert.equal(response('actual-logout-204').body,null);
});
test('queue uses object microsecond cursor and fixed20 rows; unsupported filters do not silently apply',()=>{
 const page=response('actual-queue-page').body;assert.equal(page.items.length,20);assert.equal(typeof page.nextCursor,'object');assert.equal(page.nextCursor.id,page.items.at(-1).id);assert.match(page.nextCursor.createdAt,/\.\d{6}Z$/);
 assert.equal(page.items[0].current_revision,'1');assert.equal(page.items[0].owner,undefined);assert.equal(page.items[0].title,undefined);assert.equal(response('actual-queue-filter-rejected').status,400);assert.equal(response('actual-queue-empty').body.items.length,0);
});
test('detail separates entity expectation7 and immutable submission3 with history and raw reviews',()=>{
 const d=response('actual-detail').body;assert.equal(d.submission.expected_revision,'7');assert.equal(d.submission.current_revision,'3');assert.equal(d.revisions[0].revision,'3');assert.equal(d.reviews[0].audit_seq,'10');assert.equal(d.reviews[0].actor_id,undefined);assert.equal(d.reviews[0].created_at,undefined);
});
test('open and decision bodies keep both expectations, approval8 is entity revision, other results omit revision',()=>{
 const o=get('actual-open');assert.equal(o.requestBody.expectedRevision,0);assert.equal(o.requestBody.expectedSubmissionRevision,1);assert.equal(o.response.status,200);assert.equal(o.response.body.status,'in_review');
 const a=get('actual-decision-approve');assert.equal(a.requestBody.expectedRevision,7);assert.equal(a.requestBody.expectedSubmissionRevision,3);assert.equal(a.response.status,201);assert.equal(a.response.body.revision,8);
 for(const d of ['reject','request_changes']){const r=response('actual-decision-'+d);assert.equal(r.status,200);assert.equal(r.body.revision,undefined);}
});
test('idempotent replay preserves exact body and key; changed body422; generic409 requires refetch',()=>{
 const a=get('actual-decision-approve'),b=get('actual-approve-replay');assert.equal(b.replayOf,a.id);assert.deepEqual(b.requestBody,a.requestBody);assert.deepEqual(b.response,a.response);assert.equal(response('actual-error-422').status,422);
 const conflict=response('actual-error-409');assert.equal(conflict.body.error.details,undefined);assert.equal(conflict.body.currentRevision,undefined);
});
test('audit pages preserve signed cursor highwater numeric order, vocabulary and independent chain check',()=>{
 const a=response('actual-audit-page-1').body,b=response('actual-audit-page-2').body;assert.equal(a.items.length,20);assert.equal(b.items.length,1);assert.equal(b.nextCursor,null);
 const cursor=new Cursors(Buffer.alloc(32,7)).read(a.nextCursor,'admin.audit');assert.equal(cursor.high,'21');assert.equal(cursor.after,'20');
 assert.equal(verifyAudit({version:1,events:[...a.items,...b.items],head:a.head}),true);assert.equal(a.items[0].action,'submitted->in_review');assert.equal(a.items[0].seq,'1');assert.equal(b.items[0].seq,'21');
 const tampered=structuredClone({version:1,events:[...a.items,...b.items],head:a.head});tampered.events[1].action='decision.approve';assert.throws(()=>verifyAudit(tampered));
 assert.equal(response('actual-audit-search-rejected').status,400);
});
test('BIGINT decimal bounds stay exact and outsideLong or floating representations fail closed',()=>{
 for(const text of ['9007199254740993','9223372036854775807'])assert.equal(get('actual-bigint-'+text).value,text);
 assert.throws(()=>decimal('9223372036854775808'));assert.throws(()=>decimal(9007199254740992));
});
test('before after fixtures are public display only, not privileged current-field lookup',()=>{
 const a=get('actual-public-display-7'),b=get('actual-public-display-8');assert.equal(a.value.displayTitle,'Synthetic before');assert.equal(b.value.displayTitle,'Synthetic after');assert.equal(a.value.revision,'7');assert.equal(b.value.revision,'8');assert.equal(a.value.fields,undefined);assert.ok(a.scope.includes('not current entity lookup'));
});
test('G1 throughG9 andG11 proposals remain inactive and require explicit approval; G10 is not reopened',()=>{
 const proposals=corpus.cases.filter(c=>c.classification==='proposal-unapproved');assert.deepEqual([...new Set(proposals.map(c=>c.gap))].sort(),['G1','G11','G2','G3','G4','G5','G6','G7','G8','G9']);
 for(const p of proposals){assert.equal(p.active,false);assert.equal(p.requiresApproval,true);}
});
