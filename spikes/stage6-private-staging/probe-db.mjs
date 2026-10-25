// Server-only adapter factory. Driver injection keeps this preparation inert.
// Future Worker packaging must pin pg>=8.16.3 and enable nodejs_compat.
/**
 * @typedef {{connect():Promise<void>,query(sql:string):Promise<{rows:Array<{id:number,marker:string}>}>,end():Promise<void>}} ProbeClient
 * @param {{Client:new(options:Record<string,unknown>)=>ProbeClient,env:{DATABASE_URL:string,EXPECTED_DB_HOST:string,DB_CA_PEM:string}}} options
 */
export function createReadProbe({Client,env}) {
  return async () => {
    const url=new URL(env.DATABASE_URL);
    const user=decodeURIComponent(url.username);
    if(!['postgres:','postgresql:'].includes(url.protocol) || url.search || url.hash
      || !/^goldberg_staging_probe(?:\.[a-z0-9]{20})?$/.test(user)
      || !env.EXPECTED_DB_HOST || url.hostname!==env.EXPECTED_DB_HOST
      || !url.password || url.pathname!=='/postgres'
      || !['5432','6543'].includes(url.port || '5432')
      || typeof env.DB_CA_PEM!=='string' || !env.DB_CA_PEM.includes('BEGIN CERTIFICATE')) {
      throw new Error('Invalid private probe transport configuration');
    }
    const c=new Client({host:url.hostname,port:Number(url.port || 5432),user,
      password:decodeURIComponent(url.password),database:'postgres',
      ssl:{rejectUnauthorized:true,ca:env.DB_CA_PEM,servername:url.hostname,expectedServerHostname:url.hostname},
      connectionTimeoutMillis:3000,query_timeout:3000,statement_timeout:3000,
      application_name:'goldberg-synthetic-probe'});
    try {
      await c.connect();
      const result=await c.query('SELECT id,marker FROM goldberg_private.staging_probe WHERE id=1');
      return result.rows.length===1 && result.rows[0].id===1 && result.rows[0].marker==='synthetic-only';
    } finally {
      await c.end();
    }
  };
}
