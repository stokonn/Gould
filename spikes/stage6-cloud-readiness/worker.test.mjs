import test from 'node:test';
import assert from 'node:assert/strict';
import worker from './worker.mjs';

for (const method of ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS', 'HEAD']) {
  test(`${method}: all routes disabled even with activation-looking variables`, async () => {
    for (const path of ['/', '/v1/me', '/v1/admin/queue', '/health', '/oauth/callback']) {
      const env = new Proxy({}, { get() { throw new Error('must not access secrets or flags'); } });
      const response = await worker.fetch(new Request(`https://synthetic.invalid${path}`, {method}), env);
      assert.equal(response.status, 503);
      assert.equal(response.headers.get('cache-control'), 'no-store');
      assert.equal(response.headers.get('set-cookie'), null);
      assert.equal(response.headers.get('access-control-allow-origin'), null);
      assert.deepEqual(await response.json(), {error: 'private_staging_disabled'});
    }
  });
}
