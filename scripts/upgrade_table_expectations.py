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


def column_transition(table, before_columns, after_columns):
    """Validate an additive ALTER TABLE transition and return the added columns.

    Existing columns must keep their order (ALTER TABLE ADD COLUMN appends), may
    not disappear and may not change affinity/position: only then is a positional
    row comparison on the common columns still meaningful.
    """
    if after_columns[:len(before_columns)] != list(before_columns):
        lost = [c for c in before_columns if c not in after_columns]
        raise AssertionError(
            'Non-additive column transition in %s (lost %s; before %s after %s)'
            % (table, sorted(lost), list(before_columns), list(after_columns))
        )
    return list(after_columns[len(before_columns):])


def project_rows(rows, columns, projected_columns):
    """Project positional rows onto named columns, preserving order and count."""
    indexes = [list(columns).index(column) for column in projected_columns]
    return [tuple(row[index] for index in indexes) for row in rows]


def declared_default(version, table, column, schema_dir=SCHEMA_DIR):
    """The Room-declared SQL default of one column (None when it has no default)."""
    document = json.loads((Path(schema_dir) / (str(version) + '.json')).read_text())
    entities = document['database']['entities']
    entity = next((item for item in entities if item['tableName'] == table), None)
    if entity is None:
        raise AssertionError('Schema %s does not declare table %s' % (version, table))
    field = next((item for item in entity['fields'] if item['columnName'] == column), None)
    if field is None:
        raise AssertionError('Schema %s does not declare %s.%s' % (version, table, column))
    raw = field.get('defaultValue')
    if raw is None:
        return None
    if len(raw) >= 2 and raw.startswith("'") and raw.endswith("'"):
        return raw[1:-1].replace("''", "'")
    if field.get('affinity') == 'INTEGER':
        return int(raw)
    return raw


def assert_column_preservation(table, before_columns, before_rows, after_columns,
                               after_rows, expected_schema, schema_dir=SCHEMA_DIR):
    """Verify an additive schema change preserves every seeded value.

    Common columns must compare equal row-for-row (count and order included), and
    every added column must hold exactly its Room/ALTER TABLE DEFAULT for every
    row. A changed user value, a vanished row, a reordered row or a non-default
    migration value fails.
    """
    added = column_transition(table, before_columns, after_columns)
    projected_before = project_rows(before_rows, before_columns, before_columns)
    projected_after = project_rows(after_rows, after_columns, before_columns)
    if projected_before != projected_after:
        raise AssertionError(
            'Persistent data changed in %s: %d seeded rows, %d upgraded rows, %d differing'
            % (table, len(projected_before), len(projected_after),
               sum(1 for a, b in zip(projected_before, projected_after) if a != b)
               + abs(len(projected_before) - len(projected_after)))
        )
    for column in added:
        expected = declared_default(expected_schema, table, column, schema_dir)
        index = list(after_columns).index(column)
        values = {row[index] for row in after_rows}
        if values - {expected}:
            raise AssertionError(
                'Added column %s.%s is not its declared default %r: observed %r'
                % (table, column, expected, sorted(values, key=repr))
            )
    return added


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
