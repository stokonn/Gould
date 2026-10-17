"""Validate local policy references/navigation; does not simulate agent runtimes."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
policy=(root/'AGENTS.md').read_text()
for required in ['permanent','physical phone','migration','CZ/EN','Wiki','Project','mandatory','signing','com.opus.piano']:
    assert required.lower() in policy.lower(),required
assert '@AGENTS.md' in (root/'CLAUDE.md').read_text()
assert '@./AGENTS.md' in (root/'GEMINI.md').read_text()
rule=(root/'.cursor/rules/goldberg.mdc').read_text()
assert rule.startswith('---\n') and 'alwaysApply: true\n' in rule and '@AGENTS.md' in rule
assert '../AGENTS.md' in (root/'.github/copilot-instructions.md').read_text()
for path in ['CLAUDE.md','GEMINI.md','.cursor/rules/goldberg.mdc','.github/copilot-instructions.md']:
    assert len((root/path).read_text().splitlines())<20,'Adapter must stay thin'
for path in (root/'docs/wiki').glob('*.md'):
    for target in re.findall(r'\]\(([^)]+)\)',path.read_text()):
        if '://' not in target:
            destination=path.parent/target
            assert destination.is_file() or (path.parent/(target+'.md')).is_file(),(path,target)
print('PASS: canonical policy, thin agent references, Cursor frontmatter and local Wiki navigation; runtime agent loading NOT TESTED')
