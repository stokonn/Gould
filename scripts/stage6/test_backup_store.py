"""New local archive lifecycle tests; accepted database/encryption suites are not rerun."""
import datetime as dt
import json
import os
from pathlib import Path
import tempfile
import unittest
from backup_store import publish, restore_dump, health, run_gpg
from unittest.mock import patch
import subprocess

class BackupStoreTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory(prefix='goldberg-archive-test-')
        self.addCleanup(self.tmp.cleanup)
        self.base = Path(self.tmp.name)
        self.destination = self.base / 'archives'
        self.destination.mkdir(mode=0o700)
        self.restore = self.base / 'isolated'
        self.restore.mkdir(mode=0o700)
        self.key = self.base / 'synthetic-key'
        self.key.write_bytes(os.urandom(32).hex().encode())
        self.key.chmod(0o600)
        self.dump = self.base / 'synthetic.dump'
        self.dump.write_bytes(b'SYNTHETIC ONLY - NO USER OR DATABASE DATA\n' * 64)
        self.when = dt.datetime(2026, 10, 25, tzinfo=dt.timezone.utc)

    def test_atomic_archive_restore_and_existing_data_preserved(self):
        first = publish(self.dump, self.destination, self.key, self.when)
        second = publish(self.dump, self.destination, self.key, self.when)
        self.assertNotEqual(first, second)
        result = restore_dump(first, self.key, self.restore / 'new.dump')
        self.assertEqual(result.read_bytes(), self.dump.read_bytes())
        with self.assertRaises(ValueError):
            restore_dump(second, self.key, result)
        self.assertEqual(len(list(self.destination.glob('archive-*'))), 2)
        self.assertEqual(list(self.destination.glob('.publishing-*')), [])
        self.assertEqual(list(self.restore.glob('.restoring-*')), [])

    def test_wrong_key_and_changed_ciphertext_never_publish_plaintext(self):
        archive = publish(self.dump, self.destination, self.key, self.when)
        wrong = self.base / 'wrong-key'
        wrong.write_text('synthetic wrong key'); wrong.chmod(0o600)
        with self.assertRaises(ValueError):
            restore_dump(archive, wrong, self.restore / 'wrong.dump')
        self.assertFalse((self.restore / 'wrong.dump').exists())
        ciphertext = archive / 'dump.gpg'
        ciphertext.write_bytes(ciphertext.read_bytes() + b'changed')
        with self.assertRaises(ValueError):
            restore_dump(archive, self.key, self.restore / 'changed.dump')
        self.assertFalse((self.restore / 'changed.dump').exists())
        self.assertEqual(list(self.restore.glob('.restoring-*')), [])

    def test_age_health_is_bounded_and_never_deletes_archives(self):
        self.assertFalse(health(self.destination, self.when)['healthy'])
        archive = publish(self.dump, self.destination, self.key, self.when)
        self.assertTrue(health(self.destination, self.when + dt.timedelta(hours=24))['healthy'])
        self.assertFalse(health(self.destination, self.when + dt.timedelta(hours=24, seconds=1))['healthy'])
        self.assertFalse(health(self.destination, self.when - dt.timedelta(seconds=1))['healthy'])
        self.assertTrue(archive.exists())

    def test_key_permissions_symlink_and_destination_are_rejected(self):
        self.key.chmod(0o644)
        with self.assertRaises(ValueError): publish(self.dump, self.destination, self.key)
        self.key.chmod(0o600)
        alias = self.base / 'key-link'; alias.symlink_to(self.key)
        with self.assertRaises(ValueError): publish(self.dump, self.destination, alias)
        self.destination.chmod(0o755)
        with self.assertRaises(ValueError): publish(self.dump, self.destination, self.key)

    def test_incomplete_archive_and_key_inside_destination_are_rejected(self):
        archive = self.destination / 'archive-interrupted'
        archive.mkdir(mode=0o700)
        self.assertFalse(health(self.destination, self.when)['healthy'])
        self.assertEqual(health(self.destination, self.when)['invalidArchives'], 1)
        local_key = self.destination / 'key'
        local_key.write_text('synthetic'); local_key.chmod(0o600)
        with self.assertRaises(ValueError): publish(self.dump, self.destination, local_key)
        self.assertTrue(archive.exists())

    def test_timeout_still_stops_only_the_invocations_isolated_agent(self):
        commands = []
        def execute(command, **kwargs):
            commands.append(command)
            if command[0] == 'gpg':
                raise subprocess.TimeoutExpired(command[0], 120)
            return subprocess.CompletedProcess(command, 0)
        with patch('backup_store.subprocess.run', side_effect=execute):
            with self.assertRaises(subprocess.TimeoutExpired):
                run_gpg(self.key, ['--symmetric'])
        self.assertEqual([c[0] for c in commands], ['gpg', 'gpgconf'])
        self.assertEqual(commands[0][2], commands[1][2])
        self.assertEqual(commands[1][-2:], ['--kill', 'gpg-agent'])

if __name__ == '__main__': unittest.main()
