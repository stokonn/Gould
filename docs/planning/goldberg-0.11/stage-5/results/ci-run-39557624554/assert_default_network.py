import re
import sys

expected, dump_path = sys.argv[1], sys.argv[2]
dump = open(dump_path, encoding='utf-8', errors='replace').read()
match = re.search(r'^\s*Active default network: (.*)$', dump, re.M)
if not match:
    sys.exit('FATAL: "Active default network:" line missing from dumpsys connectivity')
net_id = match.group(1).strip()
if not re.fullmatch(r'\d+', net_id):
    sys.exit(f'FATAL: expected a numeric active default network for transport {expected}, got: {net_id!r}')
blocks = re.findall(r'^[ \t]*NetworkAgentInfo\{network\{' + re.escape(net_id) + r'\}.*$', dump, re.M)
if not blocks:
    sys.exit(f'FATAL: no NetworkAgentInfo block found for active default network {net_id}')
block = blocks[0]
ni = re.search(r'ni\{([^}]*)\}', block)
# API 36 (run 39552116455 evidence, connectivity-dump.txt) prints the
# transport ONLY in the nc{[ Transports: ... ]} capabilities section;
# the legacy ni{...} "type:" token does not exist there
# (ni{WIFI CONNECTED extra: }). Assertions unchanged: the active default
# network must be numeric and its NAI must declare the expected
# transport (WIFI->WIFI, MOBILE->CELLULAR naming mapped explicitly).
nc = re.search(r'nc\{\[\s*Transports:\s*(\w+)', block)
if nc:
    found = nc.group(1)
    wanted = {'WIFI': 'WIFI', 'MOBILE': 'CELLULAR'}.get(expected, expected)
    if found != wanted:
        sys.exit(f'FATAL: active default network {net_id} transport is {found}, expected {wanted} ({expected})')
    print(f'OK: active default network {net_id} uses transport {found} ({expected})')
    sys.exit(0)
legacy = re.search(r'type: (\w+)', ni.group(1)) if ni else None
if not legacy:
    sys.exit(f'FATAL: no transport token (nc Transports / ni type:) in active-network block: {block[:240]!r}')
found = legacy.group(1)
if found != expected:
    sys.exit(f'FATAL: active default network {net_id} transport is {found}, expected {expected}')
print(f'OK: active default network {net_id} uses transport {found}')
