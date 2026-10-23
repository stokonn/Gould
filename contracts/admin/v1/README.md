# Admin contract comparison fixtures v1

Synthetic, test-only comparison corpus; not an approved replacement wire contract.
No real identity, token, source content, privileged credential or live endpoint.
The Gradle test resource directory exposes these JSON files only to JVM tests;
they must not enter an application APK.

`fixtures.json` records three distinct authorities:

- `normative-design`: specified routes/error semantics in API-CONTRACTS.md; the
  normative document does not freeze every success DTO shown by the old spike.
- `legacy-c6-reference`: C6 DTO shapes currently used by Stage 7B, not proof of
  compatibility with the latest local candidate.
- `candidate-local`: committed in-process Stage 6 operation/SQL projections. These
  are authored representative shapes, not captured runtime/HTTP responses. Detail
  explicitly shows a relevant-column projection of SELECT s.*, not an exhaustive
  public DTO. Every case names its operation; no new HTTP route is invented.
- `client-condition`: not-configured/unavailable state, not a fake server response.

`mapping-proposal.json` is separately `proposal-unapproved`, `active: false`.
The entity expectation 7, submission expectation 3 and published result 8 are
intentionally unequal. Tests must prevent accidental substitution; they do not
activate a DTO adapter, open endpoint, session flow or decision mapping.

`source-provenance.json` binds inspected source bytes to immutable commit
`bcc779685d79e99451796b3fa45657b3bca8dd99`. The later committed security branch
`faf9ba3` adds security tests/docs but does not change these contract implementations.
The latest verified Android host lineage is documented in Issue #31 comment
6362788710; the later packaging/recovery clarification is committed at bcc7796.

Cases are independent authored operation scenes unless explicitly linked by replayOf;
Admin actor and contributor author are distinct, and the submitted/open scene uses
submission revision 1. They are not a simulated end-to-end execution transcript.

Fixtures cover legacy success, candidate capabilities/session/logout, structured
queue cursors, raw detail, explicit open and decision results, stale entity/proposal/
state conflicts, authorization failures, 422 mismatch, same-body/key replay and
service unavailability. BIGINT sequences and opaque audit cursor bytes exercise
precision/preservation; this corpus is not a hash-chain/transaction/hosted-auth proof.
The cursor signing input is an openly authored test-only string, never a secret.

Candidate approval appends `in_review->approved` then `approved->published`, unlike
legacy `decision.approve`; statistics must not silently alias/double-count them.
Retry requires current authorization before stored replay. No response-shape
heuristic is approved for auto-selecting a dialect; empty pages may look alike.

Stage 6 and other clients may consume this same JSON format after review. Any
acceptance of a shared mapping must explicitly name its version/provenance and
resolve the open decisions in the Stage 7B contract audit first.
