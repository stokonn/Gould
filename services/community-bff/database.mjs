/** Per-request connections. No shared pool crosses Workers request ownership.
 * No credential URLs: connection options and CA arrive as server-only bindings. */
export function connectionFailure(error) {
 const allowed=new Set(['ECONNREFUSED','ECONNRESET','ETIMEDOUT','ENOTFOUND','EHOSTUNREACH','ERR_TLS_CERT_ALTNAME_INVALID','UNABLE_TO_VERIFY_LEAF_SIGNATURE','SELF_SIGNED_CERT_IN_CHAIN','CERT_HAS_EXPIRED','ERR_NOT_IMPLEMENTED','28P01','42501','53300']);
 const message=typeof error?.message==='string'?error.message:'';
 return {code:allowed.has(error?.code)?error.code:'UNKNOWN',reason:error?.code==='ETIMEDOUT'?'timeout':/certificate|issuer|self.signed/i.test(message)?'certificate':/timeout|timed out/i.test(message)?'timeout':/not implemented|not supported|unsupported/i.test(message)?'unsupported':/password|authentication|SCRAM/i.test(message)?'authentication':/TLS|SSL/i.test(message)?'tls':'connection'};
}

export function requestConnections({Client,options,signal,onError}) {
 const active=new Set();let closed=false;
 const close=async()=>{closed=true;await Promise.allSettled([...active].map(c=>c.end()));};
 const abort=()=>{void close();};
 signal.addEventListener('abort',abort,{once:true});
 return {
  connect:async()=>{
   if(closed||signal.aborted)throw new Error('request ended');
   const c=new Client(options),end=c.end.bind(c);let ending;
   // pg emits errors on idle sockets too. Consume the event and retire the
   // connection instead of letting EventEmitter terminate the Worker/request.
   c.on?.('error',()=>{void c.end().catch(()=>{});});
   c.end=()=>{if(!ending)ending=Promise.resolve().then(end).finally(()=>active.delete(c));return ending;};
   active.add(c);
   try{await c.connect();if(closed||signal.aborted)throw new Error('request ended');return c;}
   catch(error){try{onError?.(connectionFailure(error));}catch{}await c.end().catch(()=>{});throw new Error('database unavailable');}
  },close
 };
}

export function privateDatabaseOptions(env) {
 if(!env||!/^([a-z0-9-]+\.)*(supabase\.co|supabase\.com)$/.test(env.DB_HOST??'')||env.DB_HOST!==env.EXPECTED_DB_HOST||!['5432','6543'].includes(env.DB_PORT)||!/^goldberg_[a-z0-9_]+(?:\.hlfsouvndjsbsaldttvi)?$/.test(env.DB_USER??'')||typeof env.DB_PASSWORD!=='string'||!env.DB_PASSWORD||!env.DB_CA_PEM?.includes('BEGIN CERTIFICATE'))throw new Error('invalid private database bindings');
 return {host:env.DB_HOST,port:Number(env.DB_PORT),database:'postgres',user:env.DB_USER,password:env.DB_PASSWORD,ssl:{rejectUnauthorized:true,ca:env.DB_CA_PEM,servername:env.DB_HOST,expectedServerHostname:env.DB_HOST},connectionTimeoutMillis:3000,query_timeout:5000,statement_timeout:5000,application_name:'goldberg-community-bff'};
}

/** Hyperdrive's private binding supplies a platform-local connection. Origin TLS
 * is enforced by its separately verified CA/verify-full configuration; never
 * fall back to a direct public connection when the binding is missing. */
export function runtimeDatabaseOptions(env) {
 if(env.DB_TRANSPORT===undefined||env.DB_TRANSPORT==='direct')return privateDatabaseOptions(env);
 if(env.DB_TRANSPORT!=='hyperdrive'||typeof env.CATALOGUE_DB?.connectionString!=='string')throw new Error('invalid database transport');
 let url;try{url=new URL(env.CATALOGUE_DB.connectionString);}catch{throw new Error('invalid private binding');}
 const shape={protocol:['postgres:','postgresql:'].includes(url.protocol),privateHost:url.hostname==='hyperdrive.local'||url.hostname.endsWith('.hyperdrive.local'),database:/^\/[^/]{1,256}$/.test(url.pathname),username:!!url.username,password:!!url.password,query:[...url.searchParams.keys()].some(k=>!['sslmode','application_name'].includes(k))||url.searchParams.getAll('sslmode').length>1||(url.searchParams.has('sslmode')&&url.searchParams.get('sslmode')!=='disable'),fragment:!!url.hash};
 if(!shape.protocol||!shape.privateHost||!shape.database||!shape.username||!shape.password||shape.query||shape.fragment){console.warn('hyperdrive_binding_shape',shape);throw new Error('invalid private binding');}
 return {connectionString:env.CATALOGUE_DB.connectionString,connectionTimeoutMillis:3000,query_timeout:5000,statement_timeout:5000,application_name:'goldberg-community-bff'};
}
