// Preparation module only: NOT wired into the deployed/approved readiness shell.
// The caller must provide a bounded, TLS-verified, least-privilege readProbe adapter.
const encoder = new TextEncoder();
const headers = {'Content-Type':'application/json','Cache-Control':'no-store',
  'Content-Security-Policy':"default-src 'none'; frame-ancestors 'none'",
  'X-Content-Type-Options':'nosniff','Referrer-Policy':'no-referrer'};
/** @param {number} status @param {string} error */
const reply = (status,error) => new Response(JSON.stringify({error}),{status,headers});

/** @param {string} expected @param {string} provided @param {SubtleCrypto} subtle */
export async function equalSecret(expected,provided,subtle) {
  if (!/^[a-f0-9]{64}$/.test(expected) || !/^[a-f0-9]{64}$/.test(provided)) return false;
  // HMAC verification performs the comparison in the cryptographic implementation.
  // Random comparison key is per request; neither credential is logged or returned.
  const key=await subtle.generateKey({name:'HMAC',hash:'SHA-256'},false,['sign','verify']);
  const tag=await subtle.sign('HMAC',key,encoder.encode(expected));
  return subtle.verify('HMAC',key,tag,encoder.encode(provided));
}

/** @param {{readProbe:()=>Promise<boolean>,subtle?:SubtleCrypto}} options */
export function createProbeHandler({readProbe,subtle=globalThis.crypto.subtle}) {
  /** @param {Request} request @param {{STAGING_PROBE_ENABLED?:string,PRIVATE_VERIFICATION_TOKEN?:string}} env */
  return async function fetch(request,env) {
    if (env.STAGING_PROBE_ENABLED !== 'true') return reply(503,'private_staging_disabled');
    const url=new URL(request.url);
    if(url.pathname!=='/__private_staging/probe' || url.search || request.method!=='POST') return reply(404,'not_found');
    try {
      const token=env.PRIVATE_VERIFICATION_TOKEN;
      if(typeof token!=='string' || !/^[a-f0-9]{64}$/.test(token)) return reply(503,'private_staging_disabled');
      const authorization=request.headers.get('Authorization') ?? '';
      if(authorization.length!==71 || !authorization.startsWith('Bearer ')
        || !await equalSecret(token,authorization.slice(7),subtle)) return reply(403,'forbidden');
      // Never forward caller headers/body/URL to PostgreSQL. No arbitrary SQL input.
      const result=await readProbe();
      if(result!==true) return reply(503,'probe_unavailable');
      return new Response('{"synthetic":true,"database":"reachable"}',{status:200,headers});
    } catch {
      return reply(503,'probe_unavailable');
    }
  };
}
