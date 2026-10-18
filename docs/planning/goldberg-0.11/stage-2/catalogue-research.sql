-- Research-only logical DDL. Never execute against opus.db or copy as a Room migration.
PRAGMA foreign_keys=ON;
CREATE TABLE entity(id TEXT PRIMARY KEY,kind TEXT NOT NULL CHECK(kind IN('work','arrangement','artist','album','release','recording','collection')),title TEXT NOT NULL,revision INTEGER NOT NULL CHECK(revision>0),status TEXT NOT NULL CHECK(status IN('active','redirect','quarantined')),UNIQUE(id,kind));
CREATE TABLE work(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'work' CHECK(kind='work'),work_type TEXT NOT NULL CHECK(work_type IN('composition','movement','part','medley','mashup')),FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE work_part(parent_id TEXT NOT NULL REFERENCES work(id),child_id TEXT NOT NULL REFERENCES work(id),position INTEGER NOT NULL CHECK(position>=0),PRIMARY KEY(parent_id,child_id),UNIQUE(parent_id,position),CHECK(parent_id<>child_id));
CREATE TABLE work_source(derived_id TEXT NOT NULL REFERENCES work(id),source_id TEXT NOT NULL REFERENCES work(id),relation TEXT NOT NULL CHECK(relation IN('medley_source','mashup_source')),PRIMARY KEY(derived_id,source_id),CHECK(derived_id<>source_id));
CREATE TABLE arrangement(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'arrangement' CHECK(kind='arrangement'),instrumentation TEXT NOT NULL,FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE arrangement_source(arrangement_id TEXT NOT NULL REFERENCES arrangement(id),work_id TEXT NOT NULL REFERENCES work(id),PRIMARY KEY(arrangement_id,work_id));
CREATE TABLE artist(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'artist' CHECK(kind='artist'),artist_type TEXT NOT NULL CHECK(artist_type IN('person','group','other')),FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE album(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'album' CHECK(kind='album'),album_type TEXT NOT NULL CHECK(album_type IN('album','ep','single','soundtrack')),FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE release(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'release' CHECK(kind='release'),album_id TEXT NOT NULL REFERENCES album(id),date_value TEXT,date_precision TEXT CHECK(date_precision IN('year','month','day')),FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE recording(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'recording' CHECK(kind='recording'),duration_ms INTEGER CHECK(duration_ms>=0),FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE track(release_id TEXT NOT NULL REFERENCES release(id),medium INTEGER NOT NULL,position INTEGER NOT NULL,recording_id TEXT NOT NULL REFERENCES recording(id),credited_title TEXT NOT NULL,PRIMARY KEY(release_id,medium,position));
CREATE TABLE performance(recording_id TEXT NOT NULL REFERENCES recording(id),target_id TEXT NOT NULL REFERENCES entity(id),PRIMARY KEY(recording_id,target_id));
CREATE TABLE collection(id TEXT PRIMARY KEY,kind TEXT NOT NULL DEFAULT 'collection' CHECK(kind='collection'),FOREIGN KEY(id,kind) REFERENCES entity(id,kind));
CREATE TABLE collection_member(collection_id TEXT NOT NULL REFERENCES collection(id),member_id TEXT NOT NULL,target_id TEXT NOT NULL REFERENCES entity(id),position INTEGER NOT NULL,PRIMARY KEY(collection_id,member_id),UNIQUE(collection_id,position));
CREATE TABLE credit(entity_id TEXT NOT NULL REFERENCES entity(id),artist_id TEXT NOT NULL REFERENCES artist(id),role TEXT NOT NULL,position INTEGER NOT NULL,credited_as TEXT NOT NULL,PRIMARY KEY(entity_id,artist_id,role));
CREATE TABLE external_identity(provider TEXT NOT NULL,provider_kind TEXT NOT NULL,external_id TEXT NOT NULL,entity_id TEXT NOT NULL REFERENCES entity(id),evidence TEXT NOT NULL,state TEXT NOT NULL CHECK(state IN('observed','reviewed','superseded')),PRIMARY KEY(provider,provider_kind,external_id));
CREATE INDEX identity_entity ON external_identity(entity_id);
CREATE TABLE redirect(source_id TEXT PRIMARY KEY REFERENCES entity(id),target_id TEXT NOT NULL REFERENCES entity(id),evidence TEXT NOT NULL,revision INTEGER NOT NULL,CHECK(source_id<>target_id));
CREATE TABLE alias(entity_id TEXT NOT NULL REFERENCES entity(id),language TEXT NOT NULL,value TEXT NOT NULL,normalized TEXT NOT NULL,alias_type TEXT NOT NULL,PRIMARY KEY(entity_id,language,value));
CREATE INDEX alias_lookup ON alias(normalized,entity_id);
CREATE VIRTUAL TABLE alias_fts USING fts4(entity_id,normalized,notindexed=entity_id,tokenize=unicode61);
-- Populate alias and alias_fts together in one catalogue transaction; rebuild FTS on generation swap.
CREATE INDEX entity_kind_title ON entity(kind,title,id);
CREATE TABLE source_record(id TEXT PRIMARY KEY,provider TEXT NOT NULL,record_id TEXT NOT NULL,revision TEXT NOT NULL,payload_sha256 TEXT NOT NULL,retrieved_at TEXT NOT NULL,source_url TEXT NOT NULL,UNIQUE(provider,record_id,revision));
CREATE TABLE rights(id TEXT PRIMARY KEY,license TEXT NOT NULL,license_url TEXT NOT NULL,attribution TEXT NOT NULL,review_state TEXT NOT NULL CHECK(review_state IN('approved','blocked','unknown')));
CREATE TABLE assertion(id TEXT PRIMARY KEY,entity_id TEXT NOT NULL REFERENCES entity(id),field TEXT NOT NULL,value_json TEXT NOT NULL,source_id TEXT NOT NULL REFERENCES source_record(id),rights_id TEXT NOT NULL REFERENCES rights(id),selected INTEGER NOT NULL CHECK(selected IN(0,1)));
CREATE TABLE document(id TEXT PRIMARY KEY,entity_id TEXT NOT NULL REFERENCES entity(id),language TEXT NOT NULL CHECK(language IN('cs','en')),revision TEXT NOT NULL,content_sha256 TEXT NOT NULL,rights_id TEXT NOT NULL REFERENCES rights(id),source_id TEXT NOT NULL REFERENCES source_record(id),relative_path TEXT NOT NULL,UNIQUE(entity_id,language,source_id,revision));
CREATE TABLE media(id TEXT PRIMARY KEY,content_sha256 TEXT NOT NULL,bytes INTEGER NOT NULL CHECK(bytes>=0),rights_id TEXT NOT NULL REFERENCES rights(id),source_id TEXT NOT NULL REFERENCES source_record(id),relative_path TEXT NOT NULL);
CREATE TABLE genre_assertion(entity_id TEXT NOT NULL REFERENCES entity(id),genre TEXT NOT NULL,source_id TEXT NOT NULL REFERENCES source_record(id),PRIMARY KEY(entity_id,genre,source_id));
-- Cross-row kind/cycle/rights validation is specified in CONTRACTS.md, not guaranteed by these FKs.
