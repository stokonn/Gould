import test from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {pragueWindow,literalSearch} from '../src/statistics.mjs';
import {queueQuery,auditQuery} from '../src/query.mjs';

const fixtures=JSON.parse(await readFile(new URL('../../../contracts/admin/v1/decision-proposal/design-approval-2026-10-23/fixtures.json',import.meta.url)));

test('Europe/Prague windows preserve civil midnight and DST duration',()=>{
 for(const expected of fixtures.windows)assert.deepEqual(pragueWindow(expected.civilDate),{
  timeZone:'Europe/Prague',
  startInclusive:expected.startInclusive,
  endExclusive:expected.endExclusive,
  elapsedHours:expected.elapsedHours
 });
});
test('search is NFC-normalized, case-insensitive, and diacritic-sensitive',()=>{
 for(const scene of fixtures.searchCases)assert.equal(literalSearch(scene.text,scene.query),scene.matches,scene.id);
});
test('queue query accepts only bounded approved filters',()=>{
 assert.deepEqual(queueQuery({status:'in_review',kind:'work',q:'Dvořák',limit:'20'}),{status:'in_review',kind:'work',q:'Dvořák',limit:'20',cursor:undefined});
 for(const value of [{limit:'0'},{limit:'21'},{status:'approved'},{q:' '},{extra:'x'}])assert.throws(()=>queueQuery(value));
});
test('audit query rejects ambiguous or unsafe filters',()=>{
 assert.deepEqual(auditQuery({action:'approved->published',limit:'1'}),{action:'approved->published',limit:'1'});
 for(const value of [{action:'decision.approve'},{from:'2030-01-02T00:00:00.000000Z',to:'2030-01-01T00:00:00.000000Z'},{limit:'01'},{q:'\u0000'}])assert.throws(()=>auditQuery(value));
});
