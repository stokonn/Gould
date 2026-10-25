/** Per-request connections. No shared pool crosses Workers request ownership.
 * No credential URLs: connection options and CA arrive as server-only bindings. */
export function requestConnections({Client,options,signal}) {
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
   catch{await c.end().catch(()=>{});throw new Error('database unavailable');}
  },close
 };
}

export function privateDatabaseOptions(env) {
 if(!env||!/^([a-z0-9-]+\.)*(supabase\.co|supabase\.com)$/.test(env.DB_HOST??'')||env.DB_HOST!==env.EXPECTED_DB_HOST||!['5432','6543'].includes(env.DB_PORT)||!/^goldberg_[a-z0-9_]+(?:\.hlfsouvndjsbsaldttvi)?$/.test(env.DB_USER??'')||typeof env.DB_PASSWORD!=='string'||!env.DB_PASSWORD||!env.DB_CA_PEM?.includes('BEGIN CERTIFICATE'))throw new Error('invalid private database bindings');
 return {host:env.DB_HOST,port:Number(env.DB_PORT),database:'postgres',user:env.DB_USER,password:env.DB_PASSWORD,ssl:{rejectUnauthorized:true,ca:env.DB_CA_PEM,servername:env.DB_HOST,expectedServerHostname:env.DB_HOST},connectionTimeoutMillis:3000,query_timeout:5000,statement_timeout:5000,application_name:'goldberg-community-bff'};
}
