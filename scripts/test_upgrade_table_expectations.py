"""Regression tests for the upgrade verifier's table-set expectations.

GitHub Actions run 43246507990, job 5.11 failed a CORRECT 8 -> 12 migration:
the expected additive-table set was hand-maintained inside
verify_emulator_upgrade.py and disagreed with the row-comparison exclusions
in its values() function - the three personal_catalogue_* tables are filtered
out of both snapshots, so they could never appear as "added" while the
expectation still required them. Expectations now derive from the checked-in
Room schema JSONs and are asserted against raw sqlite_master table sets.
"""
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'scripts'))

from upgrade_table_expectations import (  # noqa: E402
    SYSTEM_TABLES,
    app_table_sets,
    assert_table_transition,
    expected_added_tables,
    schema_entity_tables,
)

# The complete 8 -> 12 declared schema diff (3 catalogue + 3 backup/evidence +
# 4 studio/badge + 7 Activity/cosmetic tables), derived independently here so
# the test fails if the checked-in schema JSONs change unexpectedly.
EXPECTED_8_TO_12 = {
    'personal_catalogue_anchors', 'personal_catalogue_identities', 'personal_catalogue_state',
    'backup_restore_state', 'recovery_evidence', 'practice_completion_evidence',
    'achievement_events', 'badge_notices', 'local_audio', 'local_audio_parts',
    'activity_reward_grants', 'activity_settings', 'activity_weekly_goals',
    'activity_weeks', 'cosmetic_loadout', 'cosmetic_ownership', 'manual_activity_entries',
}


class SchemaEntityTablesTest(unittest.TestCase):
    def test_schema_8_declares_fifteen_tables(self):
        tables = schema_entity_tables(8)
        self.assertEqual(len(tables), 15)
        self.assertIn('pieces', tables)
        self.assertIn('session_piece_links', tables)
        # Added only later: these must not exist in the baseline schema.
        self.assertNotIn('personal_catalogue_state', tables)
        self.assertNotIn('badge_notices', tables)
        self.assertNotIn('activity_settings', tables)

    def test_schema_12_declares_thirty_two_tables(self):
        tables = schema_entity_tables(12)
        self.assertEqual(len(tables), 32)
        self.assertTrue(EXPECTED_8_TO_12 <= tables, sorted(EXPECTED_8_TO_12 - tables))

    def test_expected_added_tables_8_to_12_matches_declared_diff(self):
        added = expected_added_tables(8, 12)
        self.assertEqual(added, EXPECTED_8_TO_12)
        self.assertEqual(expected_added_tables(8, 8), frozenset())


class AppTableSetsTest(unittest.TestCase):
    def test_only_system_tables_are_excluded(self):
        observed = app_table_sets([
            'pieces', 'android_metadata', 'room_master_table', 'sqlite_sequence',
            'personal_catalogue_state', 'catalog_cache', 'local_audio',
        ])
        self.assertEqual(observed, {'pieces', 'personal_catalogue_state', 'catalog_cache', 'local_audio'})

    def test_system_tables_constant(self):
        self.assertEqual(SYSTEM_TABLES, {'android_metadata', 'room_master_table', 'sqlite_sequence'})


class TableTransitionTest(unittest.TestCase):
    def baseline_names(self):
        """What a fresh schema-8 install actually creates (entities + system tables)."""
        return sorted(schema_entity_tables(8) | SYSTEM_TABLES)

    def upgraded_names(self):
        """What the 8 -> 12 in-place upgrade actually creates."""
        return sorted(schema_entity_tables(12) | SYSTEM_TABLES)

    def test_correct_8_to_12_transition_passes(self):
        added = assert_table_transition(self.baseline_names(), self.upgraded_names(), 8, 12)
        self.assertEqual(added, sorted(EXPECTED_8_TO_12))

    def test_missing_baseline_schema_fails_loudly(self):
        with self.assertRaisesRegex(AssertionError, '--baseline-schema'):
            assert_table_transition(self.baseline_names(), self.upgraded_names(), None, 12)

    def test_migration_manufactured_extra_table_fails(self):
        after = self.upgraded_names() + ['surprise_manufactured_table']
        with self.assertRaisesRegex(AssertionError, 'extra'):
            assert_table_transition(self.baseline_names(), after, 8, 12)

    def test_missing_declared_table_fails(self):
        after = [name for name in self.upgraded_names() if name != 'badge_notices']
        with self.assertRaisesRegex(AssertionError, 'missing'):
            assert_table_transition(self.baseline_names(), after, 8, 12)

    def test_wrong_baseline_generation_fails(self):
        # A schema-9-era database is not a valid schema-8 baseline.
        before = sorted(schema_entity_tables(9) | SYSTEM_TABLES)
        with self.assertRaisesRegex(AssertionError, 'Baseline database tables'):
            assert_table_transition(before, self.upgraded_names(), 8, 12)

    def test_dropped_row_comparable_table_fails(self):
        after = [name for name in self.upgraded_names() if name != 'pieces']
        with self.assertRaisesRegex(AssertionError, 'missing'):
            assert_table_transition(self.baseline_names(), after, 8, 12)

    def test_declared_removal_is_still_rejected_as_table_loss(self):
        # Even a declared schema diff may not silently destroy data: a future
        # 1 -> 2 diff that drops a table must fail the destructive-migration
        # guard after both schema-equality checks pass.
        with tempfile.TemporaryDirectory() as tmp:
            schema_dir = Path(tmp)
            (schema_dir / '1.json').write_text(
                '{"database": {"entities": [{"tableName": "pieces"}, {"tableName": "sessions"}]}}'
            )
            (schema_dir / '2.json').write_text(
                '{"database": {"entities": [{"tableName": "pieces"}]}}'
            )
            with self.assertRaisesRegex(AssertionError, 'Critical table loss'):
                assert_table_transition(
                    ['pieces', 'sessions', 'android_metadata'],
                    ['pieces', 'android_metadata'],
                    1, 2, schema_dir=schema_dir,
                )


if __name__ == '__main__':
    unittest.main()
