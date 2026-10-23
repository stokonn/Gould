#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")/.."
results="$(mktemp -d docs/planning/goldberg-0.11/stage-6/results/android-local/host-XXXXXXXX)"
python3 - "$results/source-sha256.json" <<'PYSOURCE'
from pathlib import Path
import hashlib,json,sys
paths=[p for base in ['app/src','app/schemas'] for p in Path(base).rglob('*') if p.is_file()]
paths+=[Path(p) for p in ['build.gradle.kts','settings.gradle.kts','app/build.gradle.kts','gradle.properties','gradle/wrapper/gradle-wrapper.properties','app/src/main/java/com/opus/piano/OpusApplication.kt','app/src/main/java/com/opus/piano/ui/SettingsScreen.kt','app/src/main/java/com/opus/piano/ui/CommunityDraftPanel.kt','app/src/androidTest/java/com/opus/piano/CommunityLocalInstrumentedTest.kt','app/src/main/AndroidManifest.xml','app/src/main/res/xml/data_extraction_rules.xml','app/src/main/res/values/strings.xml','app/src/main/res/values-cs/strings.xml','scripts/verify-community-android-local.sh']]
Path(sys.argv[1]).write_text(json.dumps({str(p):hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(paths)},indent=2)+'\n')
PYSOURCE
./gradlew --offline testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug --no-daemon --max-workers=1 > "$results/gradle.log" 2>&1
for check in check_agent_policy check_backup_fixtures check_history_paging check_localization check_migrations; do
 python3 "scripts/$check.py" > "$results/$check.log" 2>&1
done
python3 - "$results" <<'PYRESULTS'
from pathlib import Path
import sys,xml.etree.ElementTree as E,hashlib,json
out=Path(sys.argv[1]);counts={'tests':0,'failures':0,'errors':0,'skipped':0}
assert Path('app/build/test-results/testDebugUnitTest/TEST-com.opus.piano.community.CommunityRepositoryTest.xml').is_file(),'Community tests did not execute'
for path in Path('app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'):
 suite=E.parse(path).getroot()
 for key in counts:counts[key]+=int(suite.get(key,'0'))
 (out/path.name).write_bytes(path.read_bytes())
assert counts['tests']>0 and counts['failures']==counts['errors']==0,counts
(out/'jvm-counts.json').write_text(json.dumps(counts,indent=2)+'\n')
apk=Path('app/build/outputs/apk/debug/app-debug.apk')
(out/'apk-sha256.txt').write_text(hashlib.sha256(apk.read_bytes()).hexdigest()+'  '+str(apk)+'\n')
for path,want in json.loads((out/'source-sha256.json').read_text()).items():
 assert hashlib.sha256(Path(path).read_bytes()).hexdigest()==want,'Source changed during test run: '+path
print(out,counts)
PYRESULTS
# Device execution is opt-in and restricted to a named, confirmed emulator.
if [[ -n "${EMULATOR_SERIAL:-}" ]]; then
 [[ "$EMULATOR_SERIAL" =~ ^emulator-[0-9]+$ ]] || { echo 'Named emulator required'; exit 1; }
 adb_bin=/home/stokonn/Android/Sdk/platform-tools/adb
 [[ "$("$adb_bin" -s "$EMULATOR_SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')" == 1 ]] || { echo 'Target is not a confirmed emulator'; exit 1; }
 "$adb_bin" -s "$EMULATOR_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk > "$results/emulator-install.log"
 "$adb_bin" -s "$EMULATOR_SERIAL" install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk >> "$results/emulator-install.log"
 "$adb_bin" -s "$EMULATOR_SERIAL" shell am instrument -w -r -e class com.opus.piano.CommunityLocalInstrumentedTest com.opus.piano.test/androidx.test.runner.AndroidJUnitRunner > "$results/community-instrumentation.log"
 grep -q 'OK (5 tests)' "$results/community-instrumentation.log"
fi
