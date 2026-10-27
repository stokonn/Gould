# Existing private Worker deployment

This is the actual configuration for goldberg-community-bff-private, bound to the
existing catalogue Hyperdrive. It creates no database schema and enables no public
routes or Google OAuth. Origin TLS is verify-full with the approved Supabase CA;
query caching is disabled in the separately verified Hyperdrive configuration.

Use the locked cf1.0.0-beta.21/Wrangler4.152.0 tools. Supply an existing authorized
Cloudflare token through protected server environment/session handling;cf does not
automatically reuse Wrangler login. Never pass token/password values as CLI args.

From this directory:

```
npm ci --ignore-scripts --no-audit --no-fund
npm run build
```

Deploy the prebuilt output with `cf deploy --prebuilt --provision=false` and
`--secrets-file` pointing to the protected owner-local JSON containing only
CURSOR_KEY andIP_KEY. Never use administrative PostgreSQL credentials. The origin
catalogue password belongs only in Hyperdrive andprotected owner-local custody.
Read back exact binding ID,CA/verify-full/cacheoff,source hash,private routes and
secret names after deployment. Build success alone is not hosted verification.

The deployment project builds successfully. Bundles produced from this repository
path differ from the earlier temporary build in module source labels;deployed
source is compared with the exact build actually uploaded,not a different output.

Existing runtime credential expired2026-10-27T03:00:38Z. Do not reset it or silently
extend expiry. The reviewed renewal SQL is a proposal pending owner approval;
keep public access disabled until credential andpublication conditions are verified.
