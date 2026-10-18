"""Host SQLite fixture preservation check. Does not execute Android backup code."""
import json
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parents[1]
source = (root / 'app/src/main/java/com/opus/piano/data/OpusDatabase.kt').read_text()
schemas = root / 'app/schemas/com.opus.piano.data.OpusDatabase'
for path in sorted((root / 'app/src/androidTest/assets/backups').glob('*.json')):
    snapshot = json.loads(path.read_text())
    version = snapshot['schema']
    assert snapshot['format'] == 'goldberg-backup'
    db = sqlite3.connect(':memory:')
    db.execute('PRAGMA foreign_keys = ON')
    schema = json.loads((schemas / f'{version}.json').read_text())['database']
    for entity in schema['entities']:
        db.execute(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
        for index in entity.get('indices', []):
            db.execute(index['createSql'].replace('${TABLE_NAME}', entity['tableName']))
    originals = {}
    for table, rows in snapshot['tables'].items():
        known = {r[1] for r in db.execute(f'PRAGMA table_info({table})')}
        assert known, table
        for row in rows:
            assert set(row) == known
            columns = list(row)
            db.execute(f'INSERT INTO {table} ({",".join(columns)}) VALUES ({",".join("?" for _ in columns)})', list(row.values()))
        originals[table] = rows
    for old in range(version, 8):
        block = source.split(f'val MIGRATION_{old}_{old+1} =', 1)[1].split('\n        }', 1)[0]
        for sql in re.findall(r'db\.execSQL\("([^"]+)"\)', block):
            db.execute(sql)
    bridge = (root / 'app/src/main/java/com/opus/piano/data/PersonalCatalogueBridge.kt').read_text()
    for sql in re.findall(r'"(CREATE [^"\n]+)"', bridge.split('val create = listOf(',1)[1].split('\n    )',1)[0]):
        db.execute(sql)
    backup_source=(root/'app/src/main/java/com/opus/piano/data/BackupState.kt').read_text()
    for sql in re.findall(r'"(CREATE [^"\n]+)"',backup_source.split('val ddl=listOf(',1)[1].split('\n    )',1)[0]):
        db.execute(sql)
    assert db.execute('PRAGMA foreign_key_check').fetchall() == []
    db.row_factory = sqlite3.Row
    for table, rows in originals.items():
        actual = list(db.execute(f'SELECT * FROM {table}'))
        assert len(actual) == len(rows)
        for row in rows:
            assert any(all(value == candidate[column] for column, value in row.items()) for candidate in actual), (table, row)
    assert db.execute('SELECT SUM(durationMillis) FROM sessions').fetchone()[0] == db.execute('SELECT SUM(durationMillis) FROM session_parts').fetchone()[0]
    print(f'PASS {path.name}: schema {version} → 10 DDL, every original portable field retained')
