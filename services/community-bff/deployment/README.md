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

Current runtime credential policy is manual90days fromexecution. Owner-authorized
expiry-only renewal verified2026-10-27;currentexpiry2027-01-25T04:39:50.052095Z.
Password/grants/RLS/connectionlimit/data preserved;freshprivatehostedreadsPASS.
See stage-6/CREDENTIAL-ROTATION.md andruntime-expiry-reminders.ics. Calendarimport
is required;no notification service oradmincredential-based renewal job installed.
Public routes remainoff until separately required publication conditions pass.
