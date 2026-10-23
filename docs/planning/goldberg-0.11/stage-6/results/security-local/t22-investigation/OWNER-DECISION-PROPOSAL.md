# T22 decision proposal — NOT APPROVED / NOT APPLIED

Current CommunityApi returns400 for an unregistered operation before authentication;
Auth LocalApi returns403. No audit-edit/bootstrap handler exists. Known admin.roles
requests return401 for missing/invalid/expired/revoked sessions,403 for missing live
capability, suspended principals or authenticated self-grant, and400 for malformed
payloads. No HTTP listener was executed.33 new socket-free scripted-SQL tests pass;
they exercise real routing/Auth/session/idempotency code, not PostgreSQL constraints.

Proposed local adapter patch: reject non-string/empty operation400 before lookup;
return403 forbidden for a well-formed unregistered operation, with no handler or
SQL dispatch. Keep registered-route401, malformed-body400 and capability/self-grant403.
This also prevents array-to-property-name coercion from selecting a registered route.
See PROPOSED-NOT-APPLIED.patch; normative API/AUTH/threat documents are unchanged.

Proposed explicit T22 clarification: valid unregistered audit-mutation/bootstrap
operations403 without dispatch; registered administrative operations require a valid
session (401 otherwise), live capability and no self-grant (403 denial); malformed
requests400. No client audit editing or bootstrap endpoint is added. Actual future
HTTP path/method binding remains unapproved and untested.

Owner approval is required because API-CONTRACTS section2 defines401/403/400 by
cause while T22 currently says blanket403, and published local tests assert400 for
absent operations. Approval must explicitly authorize the status/operation-validation
change, corresponding local regression expectations and precise T22 clarification.
Absent approval, retain behavior, preserve T22 FAIL, and publish investigation only.

created by stokonn with love <3
