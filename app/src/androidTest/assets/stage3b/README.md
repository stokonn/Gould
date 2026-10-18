# Stage 3B golden backups

These synthetic records and exact raw payloads were authored for Goldberg tests and are dedicated to CC0-1.0. They contain no real user data, scraped catalogue, biographies, recordings or third-party artwork. The sample recording URL is an inert fixture string, never fetched. Generate with docs/planning/goldberg-0.11/stage-3b/fixtures.py from the actual Room 10 schema; check with check_fixtures.py. UUIDv5 is deterministic test data; production personal identities remain opaque UUIDs.

full-v5.json and minimal-v5.json contain every required section and real Room-compatible columns, hashes and references. unknown-v5.json deliberately contains unreadable non-UTF-8 draft bytes. historical-format4.json is an authored derivative of the retained format3 synthetic fixture with the format4 additions. Original formats1–3 are unchanged.
