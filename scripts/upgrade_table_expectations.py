"""Table-set expectations for the in-place upgrade verifier.

Single source of truth for the post-upgrade table-set assertions:
verify_emulator_upgrade.py compares the RAW sqlite_master table sets of the
seeded baseline and upgraded databases against the checked-in Room schema
JSONs instead of hand-maintaining table names. The previous hand-maintained
copy of the schema-9/11/12 additions disagreed with the row-comparison
exclusions inside values() (personal_catalogue_* are filtered out of both
snapshots and therefore can never appear as "added"), which failed a CORRECT
8 -> 12 migration in GitHub Actions run 43246507990, job 5.11.

Pure functions only: no adb, no emulator, no argument parsing - covered by
scripts/test_upgrade_table_expectations.py.
"""
import json
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
SCHEMA_DIR = REPO_ROOT / 'app/schemas/com.opus.piano.data.OpusDatabase'

# Owned by Android/Room rather than by any Room migration; never part of a
# declared entity set and never an acceptable migration result.
SYSTEM_TABLES = frozenset({'android_metadata', 'room_master_table', 'sqlite_sequence'})


def schema_entity_tables(version, schema_dir=SCHEMA_DIR):
    """Declared entity tables of one checked-in Room schema version."""
    document = json.loads((Path(schema_dir) / (str(version) + '.json')).read_text())
    return frozenset(entity['tableName'] for entity in document['database']['entities'])


def app_table_sets(sqlite_master_names):
    """Observed application tables, excluding Android/Room system tables."""
    return frozenset(sqlite_master_names) - SYSTEM_TABLES


def expected_added_tables(baseline_schema, expected_schema, schema_dir=SCHEMA_DIR):
    """Tables the declared schema diff adds between two versions."""
    return schema_entity_tables(expected_schema, schema_dir) - schema_entity_tables(baseline_schema, schema_dir)


def assert_table_transition(before_names, after_names, baseline_schema, expected_schema, schema_dir=SCHEMA_DIR):
    """Verify a seeded-baseline -> upgraded table-set transition; return added tables.

    Raises AssertionError (never a silent skip) when:
    - baseline_schema is not provided (table-set verification requires it),
    - the baseline database does not exactly match the declared baseline schema,
    - the upgraded database does not exactly match the declared target schema
      (a missing declared table and a migration-manufactured extra table both
      fail, even when the extra table is excluded from row comparison),
    - any baseline table disappeared (destructive migration), even when the
      declared schema diff itself would allow the removal.
    """
    if baseline_schema is None:
        raise AssertionError('Table-set verification requires --baseline-schema')
    before = app_table_sets(before_names)
    after = app_table_sets(after_names)
    declared_before = schema_entity_tables(baseline_schema, schema_dir)
    declared_after = schema_entity_tables(expected_schema, schema_dir)
    if before != declared_before:
        raise AssertionError(
            'Baseline database tables differ from schema %s: missing %s, extra %s'
            % (baseline_schema, sorted(declared_before - before), sorted(before - declared_before))
        )
    if after != declared_after:
        raise AssertionError(
            'Upgraded database tables differ from schema %s: missing %s, extra %s'
            % (expected_schema, sorted(declared_after - after), sorted(after - declared_after))
        )
    removed = before - after
    if removed:
        raise AssertionError('Critical table loss: %s' % sorted(removed))
    return sorted(after - before)
