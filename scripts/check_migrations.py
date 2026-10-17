"""Host SQLite safety check against exported Room schemas; not an Android runtime test."""
import json
from pathlib import Path
import re
import sqlite3

ROOT = Path(__file__).resolve().parents[1]
SCHEMAS = ROOT / "app/schemas/com.opus.piano.data.OpusDatabase"
SOURCE = (ROOT / "app/src/main/java/com/opus/piano/data/OpusDatabase.kt").read_text()


def make_schema(connection, version):
    schema = json.loads((SCHEMAS / f"{version}.json").read_text())["database"]
    for entity in schema["entities"]:
        connection.execute(entity["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
        for index in entity.get("indices", []):
            connection.execute(index["createSql"].replace("${TABLE_NAME}", entity["tableName"]))
    return schema


for starting_version in (1, 2, 3, 4, 5, 6, 7):
    db = sqlite3.connect(":memory:")
    db.execute("PRAGMA foreign_keys = ON")
    make_schema(db, starting_version)
    db.execute("INSERT INTO pieces(id, title, composer, difficulty, status, notes) VALUES(7, 'Legacy', 'Composer', 'ADVANCED', 'POLISHING', 'Keep notes')")
    db.execute("INSERT INTO sessions(id, pieceId, pieceTitle, startedAt, completedAt, durationMillis) VALUES(3, 7, 'Legacy', 1000, 61000, 60000)")
    if starting_version >= 2:
        db.execute("INSERT INTO catalog_cache VALUES('retained', '{}', 123)")
    if starting_version >= 3:
        db.execute("INSERT INTO badge_unlocks VALUES('first_piece', 123)")
        db.execute("INSERT INTO saved_recordings VALUES(12, 'piece:7', 'https://youtu.be/abcdefghijk', 'Reference', 'Performer', 'YOUTUBE', 321, 1)")
    if starting_version >= 4:
        db.execute("INSERT INTO session_parts(sessionId,pieceId,title,composer,musicalEra,durationMillis,notes) VALUES(3,7,'Legacy','Composer','',60000,'')")
        db.execute("INSERT INTO profile_settings(id,username,avatar,theme,showcase) VALUES(1,'Existing musician','','Dark','first_piece')")
    if starting_version >= 6:
        db.execute("INSERT INTO local_app_state VALUES(1,1,0,18,0,4,'2026-10-15')")
        db.execute("INSERT INTO practice_drafts VALUES(1,'unchanged original draft',123)")
    for version in range(starting_version, 8):
        block = SOURCE.split(f"val MIGRATION_{version}_{version + 1} =", 1)[1].split("\n        }", 1)[0]
        statements = re.findall(r'db\.execSQL\("([^"]+)"\)', block)
        assert statements, f"Migration {version} not found"
        for sql in statements:
            db.execute(sql)
    assert db.execute("SELECT id, title, notes, status FROM pieces").fetchone() == (7, "Legacy", "Keep notes", "POLISHING")
    assert db.execute("SELECT id, durationMillis FROM sessions").fetchone() == (3, 60000)
    assert db.execute("SELECT createdAt, instrument, genre, musicalEra FROM pieces").fetchone() == (0, "", "", "")
    if starting_version >= 2:
        assert db.execute("SELECT fetchedAt FROM catalog_cache WHERE cacheKey='retained'").fetchone() == (123,)
    if starting_version >= 3:
        assert db.execute("SELECT unlockedAt FROM badge_unlocks WHERE achievementId='first_piece'").fetchone() == (123,)
        assert db.execute("SELECT id, title, savedAt, preferred FROM saved_recordings").fetchone() == (12, 'Reference', 321, 1)
    assert db.execute("SELECT sessionId, pieceId, title, durationMillis FROM session_parts").fetchone() == (3, 7, "Legacy", 60000)
    assert db.execute("SELECT SUM(durationMillis) FROM session_parts").fetchone() == db.execute("SELECT SUM(durationMillis) FROM sessions").fetchone()
    if starting_version >= 4:
        assert db.execute("SELECT username,theme,showcase,typography,statistics FROM profile_settings").fetchone() == ('Existing musician','Dark','first_piece','Editorial','time,pieces,sessions,streak')
    if starting_version >= 4: assert db.execute("SELECT favoriteArtist,favoritePiece,favoriteEra,biography FROM profile_settings").fetchone() == ('','','','')
    if starting_version >= 6:
        assert db.execute("SELECT payload,updatedAt FROM practice_drafts WHERE id=1").fetchone() == ('unchanged original draft',123)
        assert db.execute("SELECT generation,lastReminderDay FROM local_app_state").fetchone() == (4,'2026-10-15')
    expected = sqlite3.connect(":memory:")
    for entity in make_schema(expected, 8)["entities"]:
        table = entity["tableName"]
        assert db.execute(f"PRAGMA table_info({table})").fetchall() == expected.execute(f"PRAGMA table_info({table})").fetchall(), table
        assert db.execute(f"PRAGMA index_list({table})").fetchall() == expected.execute(f"PRAGMA index_list({table})").fetchall(), table
        assert db.execute(f"PRAGMA foreign_key_list({table})").fetchall() == expected.execute(f"PRAGMA foreign_key_list({table})").fetchall(), table
    assert db.execute("PRAGMA foreign_key_check").fetchall() == []
    assert db.execute("SELECT onboardingDone, reminderEnabled FROM local_app_state").fetchone() == (1, 0)
    assert db.execute("SELECT elapsedMillis, detectedActiveMillis, notes FROM sessions").fetchone() == (None, None, '')
    db.execute("INSERT OR IGNORE INTO badge_unlocks VALUES('first_piece', 123)")
    db.execute("INSERT OR IGNORE INTO badge_unlocks VALUES('first_piece', 999)")
    assert db.execute("SELECT unlockedAt FROM badge_unlocks").fetchone() == (123,)
    print(f"PASS v{starting_version} → v8: legacy data retained, schema matches Room export, first timestamp retained")
