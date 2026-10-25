"""Local encrypted archive publication and inspection. Never connects to a database.

No scheduler, deletion, destination selection or key generation is implicit.
Production custody/retention approval remains external. Errors never print gpg stderr.
"""
from pathlib import Path
import argparse
import datetime as dt
import hashlib
import json
import os
import shutil
import stat
import subprocess
import tempfile
import uuid


def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b''):
            h.update(chunk)
    return h.hexdigest()


def private_file(path):
    info = path.lstat()
    if not stat.S_ISREG(info.st_mode) or info.st_uid != os.getuid() or stat.S_IMODE(info.st_mode) != 0o600:
        raise ValueError('Key must be an owner-only regular file')


def private_directory(path):
    info = path.lstat()
    if not stat.S_ISDIR(info.st_mode) or info.st_uid != os.getuid() or stat.S_IMODE(info.st_mode) != 0o700:
        raise ValueError('Directory must be owner-only and preselected explicitly')


def run_gpg(key, args):
    private_file(key)
    with tempfile.TemporaryDirectory(prefix='goldberg-gpg-') as home:
        os.chmod(home, 0o700)
        result = subprocess.run(['gpg', '--homedir', home, '--batch', '--no-symkey-cache',
                                 '--pinentry-mode', 'loopback', '--passphrase-file', str(key),
                                 *args], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL,
                                timeout=120)
        subprocess.run(['gpgconf', '--homedir', home, '--kill', 'gpg-agent'],
                       stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=10)
        if result.returncode:
            raise ValueError('Encryption or authentication failed')


def sync_file(path):
    with path.open('rb') as source:
        os.fsync(source.fileno())


def sync_directory(path):
    fd = os.open(path, os.O_RDONLY | os.O_DIRECTORY)
    try:
        os.fsync(fd)
    finally:
        os.close(fd)


def publish(source, destination, key, now=None):
    """Publish a fresh directory atomically. Existing archives are never overwritten."""
    source, destination, key = Path(source).absolute(), Path(destination).absolute(), Path(key).absolute()
    private_directory(destination)
    private_file(key)
    if source.is_symlink() or not source.is_file() or source.stat().st_size == 0:
        raise ValueError('Nonempty regular dump required')
    # Recovery material must not be accidentally included in an archive tree.
    if key.is_relative_to(destination) or source == key:
        raise ValueError('Separate key custody path required')
    now = now or dt.datetime.now(dt.timezone.utc)
    if now.tzinfo is None:
        raise ValueError('UTC-aware time required')
    stamp = now.astimezone(dt.timezone.utc).strftime('%Y%m%dT%H%M%SZ')
    final = destination / ('archive-' + stamp + '-' + uuid.uuid4().hex)
    temporary = Path(tempfile.mkdtemp(prefix='.publishing-', dir=destination))
    try:
        encrypted = temporary / 'dump.gpg'
        run_gpg(key, ['--symmetric', '--cipher-algo', 'AES256', '--output', str(encrypted), str(source)])
        os.chmod(encrypted, 0o600)
        manifest = {'format': 1, 'createdAt': now.astimezone(dt.timezone.utc).isoformat(),
                    'ciphertextSha256': digest(encrypted), 'ciphertextBytes': encrypted.stat().st_size}
        receipt = temporary / 'manifest.json'
        receipt.write_text(json.dumps(manifest, sort_keys=True) + '\n')
        os.chmod(receipt, 0o600)
        sync_file(encrypted)
        sync_file(receipt)
        sync_directory(temporary)
        os.rename(temporary, final)
        sync_directory(destination)
        return final
    finally:
        if temporary.exists():
            shutil.rmtree(temporary)  # only this invocation's unpublished temporary directory


def inspect(archive):
    archive = Path(archive)
    private_directory(archive)
    encrypted, receipt = archive / 'dump.gpg', archive / 'manifest.json'
    private_file(encrypted)
    private_file(receipt)
    manifest = json.loads(receipt.read_text())
    if manifest.get('format') != 1 or manifest.get('ciphertextSha256') != digest(encrypted) or manifest.get('ciphertextBytes') != encrypted.stat().st_size:
        raise ValueError('Incomplete or changed archive')
    created = dt.datetime.fromisoformat(manifest['createdAt'])
    if created.tzinfo is None:
        raise ValueError('Invalid archive time')
    return manifest


def restore_dump(archive, key, isolated_output):
    """Authenticate/decrypt into a NEW isolated path; never invokes pg_restore."""
    archive, key, output = Path(archive).absolute(), Path(key).absolute(), Path(isolated_output).absolute()
    inspect(archive)
    private_directory(output.parent)
    if output.exists() or output.is_symlink() or output.is_relative_to(archive) or key == output:
        raise ValueError('New isolated output path required')
    fd, temporary = tempfile.mkstemp(prefix='.restoring-', dir=output.parent)
    os.close(fd)
    temporary = Path(temporary)
    try:
        # gpg writes only to an owned mode600 temporary; failed authentication never publishes it.
        run_gpg(key, ['--yes', '--output', str(temporary), '--decrypt', str(archive / 'dump.gpg')])
        sync_file(temporary)
        os.link(temporary, output)  # exclusive publication; concurrent/pre-existing output fails
        sync_directory(output.parent)
        return output
    finally:
        temporary.unlink(missing_ok=True)


def health(destination, now=None, max_age_hours=24):
    private_directory(Path(destination))
    now = now or dt.datetime.now(dt.timezone.utc)
    valid, invalid = [], 0
    for archive in Path(destination).glob('archive-*'):
        try:
            manifest = inspect(archive)
            created = dt.datetime.fromisoformat(manifest['createdAt'])
            if created > now:
                raise ValueError('Future receipt')
            valid.append(created)
        except (ValueError, OSError, KeyError, TypeError):
            invalid += 1
    age = (now - max(valid)).total_seconds() if valid else None
    return {'validArchives': len(valid), 'invalidArchives': invalid, 'latestAgeSeconds': age,
            'healthy': age is not None and age <= max_age_hours * 3600 and invalid == 0,
            'automaticDeletion': False}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='operation', required=True)
    export = commands.add_parser('publish')
    for name in ['dump', 'destination', 'key']:
        export.add_argument('--' + name, type=Path, required=True)
    restore = commands.add_parser('restore-dump')
    for name in ['archive', 'key', 'isolated-output']:
        restore.add_argument('--' + name, type=Path, required=True)
    check = commands.add_parser('health')
    check.add_argument('--destination', type=Path, required=True)
    args = parser.parse_args()
    try:
        if args.operation == 'publish':
            publish(args.dump, args.destination, args.key)
            print('Encrypted archive published; source retained')
        elif args.operation == 'restore-dump':
            restore_dump(args.archive, args.key, args.isolated_output)
            print('Authenticated dump restored to new isolated path')
        else:
            result = health(args.destination)
            print(json.dumps(result))
            return 0 if result['healthy'] else 1
    except (OSError, ValueError, KeyError, TypeError, subprocess.TimeoutExpired):
        print('Backup operation failed; no secret-bearing diagnostics emitted')
        return 2
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
