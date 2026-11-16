#!/usr/bin/env python3
"""Create a deterministic gzip asset for Android's ZIP32 catalogue entry limit."""
import argparse
import gzip
import hashlib
import time
from pathlib import Path

CHUNK = 1024 * 1024
REPORT_EVERY = 1024 * 1024 * 1024


def _hash(stream):
    digest = hashlib.sha256()
    size = 0
    for block in iter(lambda: stream.read(CHUNK), b""):
        digest.update(block)
        size += len(block)
    return size, digest.hexdigest()


def package(source: Path, target: Path):
    if not source.is_file() or target.exists():
        raise ValueError("source must exist and target must not exist")
    started = time.monotonic()
    source_hash = hashlib.sha256()
    source_size = 0
    with source.open("rb") as raw, target.open("xb") as output:
        with gzip.GzipFile(filename="", mode="wb", fileobj=output, compresslevel=6, mtime=0) as archive:
            for block in iter(lambda: raw.read(CHUNK), b""):
                archive.write(block)
                source_hash.update(block)
                source_size += len(block)
                if source_size // REPORT_EVERY != (source_size - len(block)) // REPORT_EVERY:
                    print(f"phase=gzip source_bytes={source_size:,} elapsed={time.monotonic()-started:.0f}s", flush=True)
    with gzip.open(target, "rb") as compressed:
        restored_size, restored_hash = _hash(compressed)
    digest = source_hash.hexdigest()
    if (restored_size, restored_hash) != (source_size, digest):
        raise ValueError("gzip round-trip verification failed; preserve the partial output for diagnosis")
    result = {"source_bytes": source_size, "source_sha256": digest,
              "gzip_bytes": target.stat().st_size, "gzip_sha256": _file_hash(target),
              "round_trip": "verified", "elapsed_seconds": round(time.monotonic()-started)}
    print(f"phase=gzip complete {result}", flush=True)
    return result


def _file_hash(path):
    with path.open("rb") as stream:
        return _hash(stream)[1]


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("target", type=Path)
    args = parser.parse_args()
    package(args.source, args.target)
