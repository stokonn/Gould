# Actual Data & Storage visual acceptance

Source for this screen is unchanged from02c5298 through46db844. Actual
protected API36 emulator5582; owner draft, three saved WAVs and widgets retained.

EN/light at normal font: actual destination and backup disclosures/buttons captured
and inspected in widget-current/actual-data-storage-en-light-normal.png and
actual-data-storage-en-light-backup.png. EN/light320dp/actual200%: JSON export/
import buttons visible and reachable by scrolling; ZIP disclosure captured, but
the older file named audio-actions did not contain ZIP actions and is not counted.

CZ/light at normal font: actual destination captured/inspected in
widget-current/final-data-cs-normal.png. CZ/light320dp/actual200%: the corrected
scroll started above bottom navigation and found all four localized actions in
private data-storage-cs-320-font2 screenshots09–12/XML. Screenshot12 inspected:
ZIP export/restore buttons fully render within content and have readable labels.
No export/import/restore/cache deletion activated. First script started from
Settings, so its14 captures are diagnostic only; fixed script entered the actual
destination and passed. No blanket screen/theme acceptance inferred.

Original Gold accent/system-following theme/language, font1 and1080x2400 window
restored. Read-only final tar comparison:27 app-data tables and3WAV hashes exact,
SQLite integrityok/FKempty. The Android-managed android_metadata locale row moved
from en_US to cs during Czech QA and did not yet rewrite after locale restoration.
App UI returned to English; no protected draft/recording change or reset.

Remaining mandatory security/privacy/licensing/performance, full native/CI,
trusted published0.11 seeded upgrade and clean signed nondebuggable Stable gates
remain open. No Stable publication.

created by stokonn with love <3
