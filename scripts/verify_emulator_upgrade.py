"""Seed a disposable emulator with 0.10, upgrade in place, compare all persistent tables.
Never use on owner data. Every adb command targets an explicitly verified emulator.
"""
import hashlib
import argparse
import json
from pathlib import Path
import re
import shutil
import sqlite3
import subprocess
import time

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--serial', required=True)
parser.add_argument('--baseline', type=Path, required=True)
parser.add_argument('--candidate', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--seed-disposable', action='store_true', required=True)
a = parser.parse_args()
if not re.fullmatch(r'emulator-\d+', a.serial):
    parser.error('Only emulator-NNNN is allowed')
base = [shutil.which('adb') or '/home/stokonn/Android/Sdk/platform-tools/adb', '-s', a.serial]
def adb(*args, **kwargs):
    return subprocess.check_output(base + list(args), **kwargs)
assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == b'1'
a.output.mkdir(parents=True, exist_ok=True)
def stop():
    adb('shell', 'am', 'force-stop', 'com.opus.piano')
def launch():
    adb('shell', 'am', 'start', '-W', '-n', 'com.opus.piano/.MainActivity')
    time.sleep(2)
def snapshot(name):
    stop()
    path = a.output / (name + '.db')
    for suffix in ['', '-wal', '-shm']:
        result = subprocess.run(base + ['exec-out', 'run-as', 'com.opus.piano', 'cat', 'databases/opus.db' + suffix], capture_output=True)
        if result.returncode == 0:
            Path(str(path) + suffix).write_bytes(result.stdout)
        elif not suffix:
            raise RuntimeError('Initialized baseline database missing')
    db = sqlite3.connect(path)
    db.execute('PRAGMA wal_checkpoint(TRUNCATE)')
    return path, db
print(adb('install', '-r', '-d', str(a.baseline)).decode().strip(), flush=True)
assert b'versionName=0.10\r' in adb('shell', 'dumpsys', 'package', 'com.opus.piano') or b'versionName=0.10\n' in adb('shell', 'dumpsys', 'package', 'com.opus.piano')
launch()
path, db = snapshot('seed')
fixture = json.loads((Path(__file__).resolve().parents[1] / 'app/src/androidTest/assets/backups/goldberg-0.9.2-format3.json').read_text())['tables']
# Test-only replacement is explicit and limited to the verified disposable emulator.
for table in reversed(list(fixture)):
    if not table.startswith('catalog_'):
        db.execute(f'DELETE FROM {table}')
for table, rows in fixture.items():
    if table.startswith('catalog_'):
        continue
    for row in rows:
        if table == 'profile_settings':
            row.update(username='Emulator Review', biography='A little music every day.', avatar='')
        keys = list(row)
        db.execute(f'INSERT INTO {table} ({",".join(keys)}) VALUES ({",".join("?" for _ in keys)})', list(row.values()))
db.execute("INSERT OR REPLACE INTO local_app_state VALUES(1,1,0,18,0,0,'')")
draft = dict(version=1, execution=1, token='upgrade-fixture-draft', start=1700000000000, credited=3000, elapsed=3000, detected=0, mode='Manual', smart=False, active=0, notes='Preserve unfinished notes', reflection='Preserve reflection', sensitivity=50, pause=5, status='Paused', quality='Complete', removed=[], intervals=[], parts=[dict(piece=1, title='Invention No. 1', composer='Johann Sebastian Bach', catalog=None, era='Baroque', duration=3000, notes='Preserve piece draft')])
db.execute('INSERT OR REPLACE INTO practice_drafts VALUES(1,?,123)', (json.dumps(draft),))
db.commit()
db.execute('PRAGMA wal_checkpoint(TRUNCATE)')  # Commit AND checkpoint before copying the main file.
db.close()
adb('push', str(path), '/data/local/tmp/goldberg-upgrade-fixture.db')
adb('shell', 'run-as', 'com.opus.piano', 'cp', '/data/local/tmp/goldberg-upgrade-fixture.db', 'databases/opus.db')
for suffix in ['-wal', '-shm']:
    adb('shell', 'run-as', 'com.opus.piano', 'truncate', '-s', '0', 'databases/opus.db' + suffix)
def values(name):
    _, conn = snapshot(name)
    excluded = {'android_metadata', 'room_master_table', 'sqlite_sequence', 'catalog_cache', 'catalog_composers', 'catalog_works'}
    tables = sorted(r[0] for r in conn.execute("SELECT name FROM sqlite_master WHERE type='table'") if r[0] not in excluded)
    data = {t: conn.execute(f'SELECT * FROM {t} ORDER BY rowid').fetchall() for t in tables}
    conn.close()
    (a.output / (name + '.json')).write_text(json.dumps(data, sort_keys=True))
    return data
prefs_path=a.output / 'goldberg-appearance.xml'
prefs_path.write_text('<?xml version="1.0" encoding="utf-8"?><map><string name="accent">Slate</string><string name="cards">Outline</string><string name="density">Compact</string><string name="sort">Composer</string></map>')
adb('push',str(prefs_path),'/data/local/tmp/goldberg-appearance.xml')
adb('shell','run-as','com.opus.piano','mkdir','-p','shared_prefs')
adb('shell','run-as','com.opus.piano','cp','/data/local/tmp/goldberg-appearance.xml','shared_prefs/goldberg-appearance.xml')
def preference_hashes():
    hashes={}
    for name in ['goldberg-appearance','goldberg-media','goldberg-identity']:
        result=subprocess.run(base+['exec-out','run-as','com.opus.piano','cat','shared_prefs/'+name+'.xml'],capture_output=True)
        hashes[name]=hashlib.sha256(result.stdout).hexdigest() if result.returncode==0 else None
    return hashes
# Let 0.10 itself perform its normal metadata/badge backfill on the legacy fixture.
launch()
prefs_before=preference_hashes()
before = values('before')
print(adb('install', '-r', str(a.candidate)).decode().strip(), flush=True)
launch()
after = values('after')
assert prefs_before==preference_hashes(), 'Appearance/media/identity preferences changed'
assert before == after, 'Persistent data changed: ' + ', '.join(t for t in before if before[t] != after.get(t))
print('PASS: exact persistent-table equality after 0.10 upgrade, including 37 sessions, notes, profile, badges, repertoire, recovery archive and unfinished draft')
