# Existing catalogue Worker deployment

This is the actual configuration for `goldberg-community-bff-private`, bound to
its existing catalogue Hyperdrive. It creates no database schema and keeps Google OAuth disabled. The existing workers.dev route is enabled
for the two read-only catalogue paths; previews remain disabled. Hyperdrive uses the approved Supabase CA,
`verify-full` origin TLS and disabled query caching.

Use the locked cf 1.0.0-beta.21 / Wrangler 4.152.0 tools. Supply the existing
Cloudflare session/token through protected server environment handling. cf does
not automatically reuse Wrangler login. Never pass secret values as CLI arguments.

From this directory:

```sh
npm ci --ignore-scripts --no-audit --no-fund
npm run build
```

Deploy with `cf deploy --prebuilt --provision=false --secrets-file <protected-file>`.
The owner-local JSON contains only `CURSOR_KEY` and `IP_KEY`. The origin catalogue
password belongs in Hyperdrive and protected local custody. Never upload the
PostgreSQL admin credential. Read back the exact binding, CA, TLS/cache settings,
source hash, private routes and secret names after deployment. Build success
alone does not prove hosted behavior.

The deployment project builds successfully. Earlier temporary-project builds use
different module path labels; compare deployed bytes to the build actually
uploaded, not an output from another directory.

The owner-authorized manual renewal policy is 90 days from execution. Current
expiry is **2027-01-25T04:39:50.052095Z**. Password, grants, RLS, connection limit
and data are unchanged; fresh private hosted reads pass. See
`docs/planning/goldberg-0.11/stage-6/CREDENTIAL-ROTATION.md` and its calendar file.
Calendar import is required; no reminder service or admin-based renewal job was
installed.

The deployed candidate sets `CATALOGUE_READ_ONLY=true`. Only the two catalogue
GET routes reach the BFF; auth, admin and write routes are rejected before
configuration/database dispatch. Public routing was enabled under owner decision6435585794 after the documented
processing assessment and owner-confirmed manual mailbox operation. The single
public HTTPS smoke pass on October27 passed all10requests. Catalogue/feed are
empty200; auth/admin/write/private paths404,method405,cursor/query400. All
responses are JSON/no-store/redacted;20tablecounts/runtime flags/intake remain
unchanged. See results/public-activation-2026-10-27 and launch assessment.
No catalogue records were imported; this is not a populated music catalogue.
