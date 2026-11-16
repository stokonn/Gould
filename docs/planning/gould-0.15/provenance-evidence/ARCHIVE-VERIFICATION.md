# Original archive verification

All checks below use the preserved original archive's byte-identical copy in
`raw/original-transfer/`. The original files under
`/tmp/gould-015-mb-inspection-20261114/` were not moved or modified.

| File | Bytes | SHA-256 | Result |
|---|---:|---|---|
| `SHA256SUMS` | 903 | `e09d4470c42548ef73f8d1cf3762af9f1bc9b03a74a4370a1be531887a953f09` | GPG detached signature verified |
| `SHA256SUMS.asc` | 488 | `7ff8b1de19df6787ff1861464cb9157564c8fe883456f43af552fdad7cb8f001` | Signature file used above |
| `mbdump.tar.bz2` | 7,738,103,395 | `da4f1afcbca8ca54eb6362ae45173543f079e8491d097a376d060551d1a03335` | Archive detached signature verified; signed manifest SHA-256 matches |
| `mbdump.tar.bz2.asc` | 488 | `ec0016687eb4657d15c6f9ae41a9f666445cd2469bc4a0c71db532fecacd41c1` | Signature file used above |

## Results

In a new GnuPG home under `gnupg/`, with GnuPG 2.4.9/libgcrypt 1.12.2 and no
policy overrides:

1. The signature on `SHA256SUMS` verifies as a good signature by the exact
   primary fingerprint recorded in `SIGNING-KEY-AUTHENTICATION.md`.
2. The detached signature on `mbdump.tar.bz2` verifies as a good signature by
   the same key.
3. An independent SHA-256 calculation of the full 7,738,103,395-byte archive
   matches the `mbdump.tar.bz2` entry in the signed `SHA256SUMS` file.
4. A targeted `sha256sum -c` of that one signed-manifest entry exits 0. The
   full manifest lists supplementary archives not acquired for this pipeline;
   running `sha256sum -c` against all entries therefore reports those absent
   files as missing. That expected partial-manifest result is not treated as a
   Core archive failure.
5. The official snapshot directory still lists this same archive, checksum
   list, and detached signatures.

The three claims remain distinct: the GPG checks prove signature consistency
with the publisher-authenticated key; the independent SHA-256 check proves the
local archive bytes match the signed checksum entry; reproduction is addressed
separately in `CATALOGUE-EXTRACTION-AUDIT.md`.

Commands, status output, and calculated file hashes are retained in
`logs/gpg-verification.txt`, `logs/target-archive-checksum-entry.txt`, and
`logs/copied-evidence-sha256.txt`.
