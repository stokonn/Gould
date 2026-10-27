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
if not ni:
    sys.exit(f'FATAL: no ni{{...}} token in active-network block: {block}')
transport = re.search(r'type: (\w+)', ni.group(1))
if not transport:
    sys.exit(f'FATAL: no "type:" token in ni of active-network block: {ni.group(1)!r}')
found = transport.group(1)
if found != expected:
    sys.exit(f'FATAL: active default network {net_id} transport is {found}, expected {expected}')
print(f'OK: active default network {net_id} uses transport {found}')
