// Explicit local synthetic fixture for the actual Kotlin HTTP integration test.
// No environment URL or credentials accepted; PostgreSQL is loopback-only.
import {venue} from '../../../spikes/stage6-api-v1-local/tests/pg-http-venue.mjs';
import {migrateOperations} from '../../../spikes/stage6-public-admin-abuse/src/storage.mjs';
import {createBffAdapter} from '../adapter.mjs';
const cleanup=[];let stopped=false;
const stop=async()=>{if(stopped)return;stopped=true;for(const fn of cleanup.reverse())await fn();process.exit(0);};
const v=await venue({after:fn=>cleanup.push(fn)},{adapterFactory:createBffAdapter,migrate:migrateOperations});
await v.db.query(`UPDATE ${v.q}.intake_control SET enabled=true`);
const s=await v.create();
console.log(JSON.stringify({url:v.url(),submissionId:s.id}));
process.stdin.resume();process.stdin.once('data',stop);process.stdin.once('end',stop);
process.once('SIGTERM',stop);process.once('SIGINT',stop);
