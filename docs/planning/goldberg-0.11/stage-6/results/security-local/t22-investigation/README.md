# T22 investigation evidence index

Current status: explicit owner approval recorded in OWNER-APPROVAL.json; proposed
local routing patch APPLIED. Original OWNER-DECISION-PROPOSAL.md and
PROPOSED-NOT-APPLIED.patch retain their historical review-time names/text; they are
superseded by the approval and current T22-INVESTIGATION/readiness reports.

Final current evidence: verified-t22.tap38, verified-contract.tap11,
verified-focused.tap27, verified-unit.tap32, verified-report.log11:119 PASS,
0 failures/skips/cancellations. Five check logs PASS. PostgreSQL preflightEPERM,
0 updated database bodies. Prior296 host executions are pre-patch evidence.

Preserved intermediates: initial.tap30/33 (housekeeping assertion),
approved-routing.tap32/33 (malformed-input test expectation), earlier33/33 and
38/38 runs, capture-initial-failure.log. These are separate executions and are not
added to the final119 count. Passing model/capture tests do not claim PostgreSQL,
HTTP, Android or6.17 acceptance. T22 remains PARTIAL.
