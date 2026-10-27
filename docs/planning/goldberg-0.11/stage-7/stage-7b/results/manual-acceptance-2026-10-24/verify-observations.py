"""Read retained observations only; never starts ADB, CI, or application code."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent


def read(name):
    return json.loads((root / (name + '.json')).read_text())


def has_label(state, label):
    return any(label in (n.get('text'), n.get('description')) for n in state['nodes'])


def focused_labels(state, kind='accessibilityFocused'):
    for node in state['nodes']:
        if not node[kind]:
            continue
        descendants = [node]
        ids = {node['index']}
        for child in state['nodes']:
            if child['parent'] in ids:
                ids.add(child['index'])
                descendants.append(child)
        return [n.get('text') or n.get('description') for n in descendants
                if n.get('text') or n.get('description')]
    return []


for item in read('raw-evidence-inventory'):
    published = item.get('published')
    if published:
        expected = item.get('publishedSha256', item['sha256'])
        assert hashlib.sha256((root / published).read_bytes()).hexdigest() == expected, published

# The unique recorder survives outside the active source set; closure restores normal builds.
receipt = read('sources/archive-receipt')
assert hashlib.sha256((root / 'sources/Stage7bManualProbe.kt').read_bytes()).hexdigest() == receipt['sourceSha256']
assert hashlib.sha256((root / 'sources/manual-probe-build.patch').read_bytes()).hexdigest() == receipt['archivedGradlePatchSha256']
closure = read('closure-verification')
assert closure['status'] == 'PASS' and closure['sourceManifestFilesVerified'] == 270
assert closure['defaultRunner'] == 'androidx.test.runner.AndroidJUnitRunner'
assert closure['executableDiffAgainst0887d3f'] == 'EMPTY'
assert closure['ownerApproval'] == 'PENDING' and not closure['fullCi25Repeated']
assert 'BUILD SUCCESSFUL in 2m 8s' in (root / 'closure-restored-build.log').read_text()

assert read('boot')['qemu'] == '1'
assert read('boot')['serial'] == 'emulator-5586'
assert read('talkback-order-observed') == [
    ['Audit Log'], ['Search title, composer, ID, type'],
    ['Search and filters cover fetched pages only. Load more for older or later matches; server search is unavailable.'],
    ['All'], ['My actions'], ['Moderation'], ['#1 · decision.reject'], ['sub_1']]
assert focused_labels(read('talkback-touch-search')) == ['Search title, composer, ID, type']
assert focused_labels(read('talkback-before-previous-2')) == ['Back']
assert has_label(read('talkback-doubletap-back'), 'Administration')
events = [json.loads(line) for line in (root / 'events-final.jsonl').read_text().splitlines()]
assert any(e['type'] == 'TYPE_TOUCH_EXPLORATION_GESTURE_START' for e in events)
assert any(e['type'] == 'TYPE_TOUCH_EXPLORATION_GESTURE_END' for e in events)

assert read('keyboard-tab-order') == [['Back'], ['Search title, composer, ID, type'],
    ['All'], ['My actions'], ['Moderation'], ['Refresh'], ['Home'], ['Library']]
for prefix in ['queue-normal-ime', 'endpoint-normal-ime']:
    state = read(prefix)
    assert state['imeVisible'] and state['imeBottom'] == 558
assert any(n.get('text') == 'SyntheticQuery' and n['editable'] for n in read('queue-normal-ime')['nodes'])
assert not read('queue-normal-first-back')['imeVisible']
assert has_label(read('queue-normal-first-back'), 'Moderation Queue')
assert has_label(read('queue-normal-second-back'), 'Administration')
assert not read('endpoint-normal-first-back')['imeVisible']
assert any(n['editable'] for n in read('endpoint-normal-first-back')['nodes'])
assert not any(n['editable'] for n in read('endpoint-normal-second-back')['nodes'])
assert has_label(read('endpoint-normal-second-back'), 'Admin Settings')
assert has_label(read('settings-normal-system-back'), 'Administration')

for name, title in [('moderation-queue', 'Moderation Queue'), ('audit-log', 'Audit Log'), ('admin-settings', 'Admin Settings')]:
    state = read('reduced-motion-' + name)
    assert all(state[key] == 0 for key in ['animatorScale', 'transitionScale', 'windowScale'])
    assert has_label(state, title)
    assert has_label(read('reduced-back-' + name), 'Administration')
assert has_label(read('reduced-motion-profile-back'), 'Edit Profile')
assert all(float(v) == 1 for v in read('cleanup-state')['animationScales'].values())

before = read('permission-before-host-revoke')
transition = read('cross-process-revoke')
granted = read('worker-granted-phase2')
denied = read('worker-revoked-phase1')
assert before['notificationGranted'] and 701 in before['activeNotificationIds']
assert str(before['pid']) == transition['beforePid'] == str(granted['pid'])
assert transition['afterRevokePid'] == ''
assert transition['restartedPid'] != transition['beforePid']
assert transition['runtimePermissionAfterRestart'] == 'DENIED'
assert denied['pid'] != before['pid'] and denied['pid'] != int(transition['restartedPid'])
assert not denied['granted'] and not denied['notificationPosted']
assert denied['fixtureCalls'] == 6 and denied['signatureChanged']
assert not read('after-revocation-worker')['notificationGranted']
assert not read('after-revocation-worker')['activeNotificationIds']
for file in ['permission-after-package.txt', 'permission-restarted-package.txt', 'cleanup-package-permission.txt']:
    assert 'android.permission.POST_NOTIFICATIONS: granted=false' in (root / file).read_text()

cs = read('cs-dark-verified-dashboard')
assert cs['locale'] == 'cs' and abs(cs['fontScale'] - 1.3) < .01 and cs['profileTheme'] == 'Dark'
assert read('cs-verified-talkback-order')[0] == ['Auditní záznam']
assert read('cs-verified-talkback-order')[3:] == [['Vše'], ['Moje akce'], ['Moderace']]
assert has_label(read('cs-accessible-back-pass'), 'Administrace')
settings = read('cs-settings-focus-observed')
for label_index, control_index in [(2, 3), (4, 5), (6, 7)]:
    label, control = settings[label_index], settings[control_index]
    assert label['labels'] == control['labels']
    assert not label['node']['checkable'] and control['node']['checkable']

result = {
    'result': 'PASS: retained observations consistent; not unconditional Stage7B acceptance',
    'TalkBack': 'Actual EN Audit8/CS Audit6 traversal; touch exploration; accessible Back in both locales',
    'keyboard': 'Audit Tab8; Queue and endpoint IME558px; actual layered system Back',
    'reducedMotion': 'All3 scales0; real route/Back behavior; original1 scales restored',
    'permissionRevocation': transition,
    'newFinding': 'Three Settings labels and named checkable controls are separate repeated TalkBack focus stops',
    'limits': 'No audible speech/vestibular assessment, exhaustive keyboard/locale/font matrix, real3h/reboot delivery or live G1–G9/G11 integration',
    'ownerApproval': 'PENDING',
}
(root / 'verified-results.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
print(json.dumps(result, ensure_ascii=False, indent=2))
