#!/usr/bin/env python3
"""Inspect the signed APK's catalogue ZIP entries and verify the bundled seed bytes."""
import argparse
import gzip
import hashlib
import json
import zipfile
from pathlib import Path

ASSET = "assets/catalogue/musicbrainz-core-20261114/catalogue.dbz"
FULL_DATABASE_SHA256 = "7dcd72951f21f3647c7a4e6e08efddba6855debe731156a6df30ea4f6dbadce5"
FULL_PAYLOAD_SHA256 = "70d63aa8552a822e0c8a8aed261860bb03820542f90c65e20bac1021151636f5"
SEED_SHA256 = "2b4c67b414dd985f7caa4e2303b5a54562340ed947d83670a85d32c2820b428f"
PAYLOAD_SHA256 = "c96a16be03720d18a606efd1517b30d1daea9fdd2b7467203add7ab930b20a5f"


def check(apk_path: Path):
    with zipfile.ZipFile(apk_path) as apk:
        assert apk.testzip() is None, "APK ZIP integrity check failed"
        names = apk.namelist()
        catalogue_entries = [name for name in names if name.endswith((".db", ".dbz", ".db.gz"))]
        assert catalogue_entries == [ASSET], catalogue_entries
        info = apk.getinfo(ASSET)
        assert info.file_size == 89_123_721 and info.compress_type == zipfile.ZIP_STORED
        digest = hashlib.sha256()
        unpacked = hashlib.sha256()
        unpacked_bytes = 0
        with apk.open(ASSET) as compressed:
            for block in iter(lambda: compressed.read(1024 * 1024), b""):
                digest.update(block)
            compressed.seek(0)
            with gzip.GzipFile(fileobj=compressed) as seed:
                for block in iter(lambda: seed.read(1024 * 1024), b""):
                    unpacked.update(block)
                    unpacked_bytes += len(block)
        assert digest.hexdigest() == PAYLOAD_SHA256
        assert unpacked.hexdigest() == SEED_SHA256 and unpacked_bytes == 283_987_968
        assert unpacked.hexdigest() != FULL_DATABASE_SHA256
        assert digest.hexdigest() != FULL_PAYLOAD_SHA256
        manifest = json.loads(apk.read("assets/catalogue/musicbrainz-core-20261114/manifest.json"))
        selection = json.loads(apk.read("assets/catalogue/classical-composers.json"))
        assert len(selection) == len({entry["mbid"] for entry in selection}) == 100
        assert manifest["files"][0]["sha256"] == SEED_SHA256
        assert manifest["files"][0]["bytes"] == unpacked_bytes
        assert not any("full-dump" in name or "generated-catalogue" in name for name in names)
        return {"apk_bytes": apk_path.stat().st_size, "catalogue_entries": catalogue_entries,
                "catalogue_payload_bytes": info.file_size, "catalogue_payload_sha256": digest.hexdigest(),
                "seed_bytes": unpacked_bytes, "seed_sha256": unpacked.hexdigest(),
                "diagnostic_full_database_sha256_absent": FULL_DATABASE_SHA256 not in {digest.hexdigest(), unpacked.hexdigest()},
                "diagnostic_full_payload_sha256_absent": FULL_PAYLOAD_SHA256 != digest.hexdigest()}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    args = parser.parse_args()
    print(json.dumps(check(args.apk), indent=2))
