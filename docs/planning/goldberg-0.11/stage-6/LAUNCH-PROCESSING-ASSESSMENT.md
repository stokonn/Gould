# Read-only catalogue processing assessment — October 27, 2026

Prepared under the owner's operational instruction for the existing public-read
deployment. This is a bounded, documented assessment of actual processing, not a
professional certification or approval of community accounts, uploads or writes.
Accepted Q1/D5b eligibility and owner-reported public identity/contact acceptability
are retained. The accountable operator is the owner; public developer/contact are
stokonn and stokonn.us@gmail.com. No private identity is recorded here.

## A. Catalogue requests — Article 6(1)(f)

**Purpose and interest.** Deliver the public music catalogue requested by a user,
keep that service available, and protect its transport and restricted database
from misuse. These are present service-delivery/security interests of the operator
and users. Advertising, profiling, measuring individual practice and monetization
are outside this purpose.

**Data and flow.** Cloudflare receives the connecting IP, TLS/HTTP metadata,
endpoint, catalogue filters/cursor and request time. The Worker processes request
headers and assigns a random request identifier. Only two catalogue GET paths
dispatch. Its catalogue service does not persist the visitor's address, headers,
account or practice record. Hyperdrive sends bounded parameterized catalogue
queries through a service connection to the Frankfurt PostgreSQL origin; the
end-user IP is not a query parameter. The IP-key binding and authentication
throttle present in the shared backend do not mean public catalogue GETs create
coarse-IP counters: session and write paths are rejected before that dispatch.

**Necessity and alternatives.** Network addresses and protocol metadata are needed
transiently to return an HTTPS response and operate the edge security service.
An offline-only catalogue avoids this processing but cannot supply requested
updates. Requiring an account or recording every visit would add data without
being necessary. Goldberg therefore keeps the online read anonymous and local
practice independent. Endpoint/filter/cursor data is limited to catalogue selection
and pagination; private notes, credentials and personal search text must not be
placed in these URLs. This assessment does not justify retaining a browsing
history, arbitrary HTTP headers or raw IPs in an operator analytics store.

**Balancing.** Someone requesting an online catalogue reasonably expects delivery
and basic network/security processing. IPs and work selections can nevertheless
identify or reveal interests, especially when combined by infrastructure providers.
Children may use the app; lack of an account is not proof that all users are adults.
Risks include linkage, provider access, international processing and accidental
disclosure in URLs/errors. There are no decisions about people, marketing,
cross-service profiling, journal uploads or requirement to use the online API.
Offline practice remains available. The safeguards below reduce impact; unknown
provider-wide retention is disclosed rather than equated to zero. The limited
delivery purpose, expected request context and reduced data support reliance on
Article 6(1)(f) for this scope, provided these safeguards remain in force.

**Enforced safeguards.** Exact catalogue route/method allowlist; disabled backend
OAuth and intake; read-only origin role; verified origin TLS and disabled query
caching; no persisted Worker invocation/application logs, traces or tail consumers;
bounded requests, queries and deadlines; fixed-category errors; signed expiring
cursors; no-store responses; no personal-data upload or access to the local journal.
Provider security/operational processing remains subject to its applicable terms.

**Rights.** The notice states the purpose/basis and contact. Objections and other
requests go privately to the monitored mailbox. Assess an objection on its facts;
do not promise automatic refusal or claim security always overrides it. Do not
collect identity or recreate visitor tracking simply to identify an anonymous
request. Explain when Goldberg does not hold the requested information; use the
verified provider process when appropriate. Local journals cannot be remotely
accessed or erased. No blanket erasure or provider deletion deadline is promised.

**Outcome.** Proportionate favourable assessment for anonymous catalogue delivery
and its necessary security metadata, bounded to the reviewed configuration. It is
not a basis for every provider's independent purposes. Reassess before accounts,
writes, analytics, content/query changes, new providers, logging changes or a
material security/privacy incident. A failed safeguard blocks activation until
corrected. The public security smoke pass remains separate execution evidence.

## B. Voluntary support — separate Article 6(1)(f) assessment

Interest: answer the sender's requested product help. Necessary data: reply address,
message and minimal relevant diagnostic details, not credentials or full journals.
A private response is expected by the person initiating contact; indefinite reuse,
marketing or publication is not. Ask for less information where possible and
avoid copying sensitive attachments. The owner adopts ordinary correspondence
retention of 90 days after closure, with justified legal exceptions and monthly
eligibility review. Provider mail copies and Trash are separate. On this limited
purpose and safeguards, legitimate interests are proportionate; unsolicited
sensitive/third-party data requires case-specific minimization, not blanket consent.

## C. Applicable rights requests — Article 6(1)(c), separately

Where GDPR requires action, handling the request and minimal deadline/outcome
records serves that legal obligation (Articles 12–22 as applicable), rather than
ordinary support or assumed consent. Check applicability, necessary verification,
scope and any exception individually. Respond without undue delay, normally within
one month; extensions/refusals need their actual lawful conditions and explanation.
The 90-day ordinary-support rule does not invent a statutory retention period for
rights evidence. Keep only necessary case dates/outcome while needed for the
obligation or a justified claim; review monthly and document any longer retention
and its reason privately. Do not retain cases indefinitely by default.

## Operational implementation evidence

The owner reports access to the mailbox and actual Inbox/Spam checks, scheduled
Tuesday/Friday monitoring and first-of-month closed-case/retention review. The
owner will privately track receipt, applicable deadline, status and closure dates;
no correspondence/case register goes to GitHub. This is owner-reported manual
implementation, not agent mailbox access, delivered notifications or automation.
The manual procedure needs no Gmail labels, app connection or automated deletion.
No test message or provider contact was sent. Future execution is the operator's
ongoing duty, not something established indefinitely by this checkpoint.

## References and limits

- [European Commission legal grounds](https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/legal-grounds-processing-data_en):
  basis and impact assessment framework.
- [Commission rights-request guidance](https://commission.europa.eu/law/law-topic/data-protection/information-business-and-organisations/dealing-requests-individuals_en).
- [EDPB Guidelines 1/2024 consultation version](https://www.edpb.europa.eu/public-consultations/guidelines-12024-on-processing-of-personal-data-based-on-article-61f-gdpr_en):
  purpose, necessity and balancing framework; not represented as a final version.
- EUR-Lex retrieval returned a JavaScript verification page this session; no claim
  of a fresh full-text retrieval. Provider-specific evidence is recorded separately
  in PROVIDER-PROCESSING-2026-10-27.md.

created by stokonn with love <3
