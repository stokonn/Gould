import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';

const corpus=JSON.parse(await readFile(new URL('../../../contracts/admin/v1/stage7b-comparison/fixtures.json',import.meta.url)));
const mapping=JSON.parse(await readFile(new URL('../../../contracts/admin/v1/stage7b-comparison/mapping-proposal.json',import.meta.url)));
const readiness=JSON.parse(await readFile(new URL('../../../contracts/admin/v1/readiness.json',import.meta.url)));
const byId=new Map(corpus.cases.map(scene=>[scene.id,scene]));

test('compatibility corpus remains synthetic, inactive, and explicitly classified',()=>{
 assert.equal(corpus.synthetic,true);
 assert.equal(corpus.approval,'comparison-only; no shared wire-contract approval');
 assert.equal(corpus.cases.length,29);
 assert.equal(new Set(corpus.cases.map(scene=>scene.id)).size,corpus.cases.length);
 assert.equal(corpus.cases.filter(scene=>scene.classification==='candidate-local').length,21);
 assert.equal(corpus.cases.filter(scene=>scene.classification==='legacy-c6-reference').length,6);
 assert(corpus.cases.every(scene=>scene.httpBinding===undefined));
});

test('session identity is not inferred from capabilities',()=>{
 const capabilities=byId.get('candidate-capabilities').response.body;
 const session=byId.get('candidate-session').response.body;
 assert.equal(Object.hasOwn(capabilities,'principalId'),false);
 assert.equal(typeof session.principalId,'string');
 assert.equal(byId.get('candidate-capabilities').operation,'capabilities');
 assert.equal(byId.get('candidate-session').operation,'session');
});

test('queue and audit cursor dialects are never silently substituted',()=>{
 const queue=byId.get('candidate-queue').response.body;
 const legacyQueue=byId.get('legacy-queue').response.body;
 const audit=byId.get('candidate-audit').response.body;
 const legacyAudit=byId.get('legacy-audit-bigint').response.body;
 assert.equal(typeof queue.nextCursor,'object');
 assert.equal(typeof legacyQueue.nextCursor,'string');
 assert.equal(typeof audit.nextCursor,'string');
 assert.equal(typeof legacyAudit.nextCursor,'string');
 assert.notDeepEqual(queue.nextCursor,legacyQueue.nextCursor);
 assert.notDeepEqual(audit.nextCursor,legacyAudit.nextCursor);
});

test('decision expectations and result revisions remain distinct',()=>{
 const proposal=byId.get('candidate-approved');
 assert.equal(proposal.requestBody.expectedRevision,7);
 assert.equal(proposal.requestBody.expectedSubmissionRevision,3);
 assert.equal(proposal.response.body.revision,8);
 assert.equal(mapping.classification,'proposal-unapproved');
 assert.equal(mapping.active,false);
 assert.equal(mapping.requiresApproval,true);
});

test('unsafe numeric and unresolved server capabilities remain blocked',()=>{
 const numeric=byId.get('candidate-audit');
 assert.equal(numeric.response.body.items[0].seq,'9007199254740993');
 for(const id of ['G1','G2','G3','G4','G5','G6','G7','G8','G9','G11']){
  const row=readiness.dependencies.find(item=>item.id===id);
  assert.equal(row.verificationStatus,'BLOCKED',id);
 }
 assert.equal(readiness.dependencies.find(item=>item.id==='G10').verificationStatus,'CLOSED_ANDROID_STATIC_COMPATIBILITY_CONFIRMED');
});
