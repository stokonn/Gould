# MusicBrainz dump signing-key authentication

## Key material

- User ID: `MusicBrainz (MusicBrainz data dump signing key) <support@musicbrainz.org>`.
- Primary key: RSA 2048, long key ID `B8FC2375C777580F` (short ID
  `C777580F`).
- Full primary fingerprint: `D5E63B4BDCCE195642948684B8FC2375C777580F`.
- Creation time from the certificate: 2012-09-18 17:38:38 UTC.
- Expiration: no expiration time is present in the captured public certificate.
- Revocation: the captured certificate contains no revocation packet and GnuPG
  did not mark it revoked. The official download page still published this key
  on 2026-11-16. This is the status evidenced by the captured certificate and
  current official documentation; it is not a promise that no revocation can
  ever be published later.
- The certificate also contains RSA encryption subkey
  `3CA330EE23B47A79AB76FA3619AF6A3243513089`; the two snapshot signatures were
  made by the primary key, not that subkey.
- Public certificate SHA-256:
  `6b5a59c190d89b3c4993813c0ae932b6c070f922de44aef47331880c1d6a6f8b`.

## Independent source comparison

The public certificate was fingerprinted locally, then compared with live
first-party references checked on 2026-11-16:

1. MusicBrainz's current [database download instructions](https://musicbrainz.org/doc/MusicBrainz_Database/Download)
   publish the complete fingerprint and the same user ID, and explain the
   detached-signature verification procedure.
2. MetaBrainz's separate [official musicbrainz-server installation guide](https://github.com/metabrainz/musicbrainz-server/blob/master/INSTALL.md)
   instructs consumers to use `C777580F` for dump verification and shows the
   same user ID.

These checks do not rely on a keyserver's identity claim or on Gould's local
copy of the fingerprint. The independently retrieved certificate's calculated
full fingerprint matches both current first-party references. These are two
publisher-controlled channels, not an external Web-of-Trust certification.
The key is therefore authenticated here under the publisher's documented
fingerprint-pinning model; GnuPG's trust database still reports owner trust as
unknown. The public-key file was already present from the earlier isolated
key-retrieval attempt and was copied into this evidence package; keyserver
transport was not treated as authentication.

## Signature-time validity and caveats

GnuPG 2.4.9 with libgcrypt 1.12.2, default policy and no weak-algorithm
overrides accepted both snapshot signatures. The certificate has no expiry and
the captured copy has no revocation. Both signatures use the primary RSA key
and SHA-512 digest:

- `SHA256SUMS.asc`: 2026-11-14 05:08:46 UTC.
- `mbdump.tar.bz2.asc`: 2026-11-14 01:47:57 UTC.

GnuPG reports `Good signature` and `VALIDSIG` for each, but also prints
`[unknown]` / `TRUST_UNDEFINED`; this is expected for an untrusted isolated
keyring and is why the official-source fingerprint comparison is recorded
separately. GnuPG's cryptographic verification succeeded under its normal
policy.

MetaBrainz issue [MBS-14311](https://tickets.metabrainz.org/browse/MBS-14311)
was still In Progress / Unresolved when checked. Its reporter describes
SHA-1-based certificate/binding signatures rejected by Sequoia's current
policy; this is not a maintainer-confirmed key revocation or replacement.
GnuPG 2.4.9 accepted the captured certificate and the data signatures without
overrides, but a verifier enforcing Sequoia's policy may reject this legacy
certificate. This is an interoperability/security-maintenance caveat, not a
failure of the recorded GnuPG checks.

Raw certificate, isolated keyring output, and both signature results are in
`raw/original-transfer/key.asc`, `gnupg/`, and `logs/gpg-verification.txt`.
