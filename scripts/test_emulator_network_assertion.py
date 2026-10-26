"""Regression against the actual failed API36 dump and both supported formats."""
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
ROOT = Path(__file__).resolve().parents[1]

class NetworkAssertionTest(unittest.TestCase):
    def check_dump(self, text, expected, succeeds):
        shell = (ROOT / 'scripts/ci_stage6_acceptance.sh').read_text()
        marker = "cat > stage5-ci-results/assert_default_network.py <<'PYEOF'\n"
        code = shell.split(marker, 1)[1].split('\nPYEOF', 1)[0]
        with tempfile.TemporaryDirectory() as work:
            script, dump = Path(work) / 'assert.py', Path(work) / 'dump.txt'
            script.write_text(code); dump.write_text(text)
            result = subprocess.run([sys.executable, str(script), expected, str(dump)], capture_output=True, text=True)
        self.assertEqual(result.returncode == 0, succeeds, result.stdout + result.stderr)

    def test_actual_api36_wifi_dump(self):
        self.check_dump((ROOT / 'docs/planning/goldberg-0.11/stage-6/results/runtime-continuation-2026-10-25/network-active-wifi.txt').read_text(), 'WIFI', True)

    def test_actual_wifi_is_not_mobile(self):
        self.check_dump((ROOT / 'docs/planning/goldberg-0.11/stage-6/results/runtime-continuation-2026-10-25/network-active-wifi.txt').read_text(), 'MOBILE', False)

    def test_api36_mobile_capabilities(self):
        self.check_dump('Active default network: 101\n  NetworkAgentInfo{network{101} ni{MOBILE[LTE] CONNECTED extra: } nc{[ Transports: CELLULAR Capabilities: INTERNET]}\n', 'MOBILE', True)

    def test_legacy_mobile_type(self):
        self.check_dump('Active default network: 101\n  NetworkAgentInfo{network{101} ni{type: MOBILE state: CONNECTED}\n', 'MOBILE', True)

    def test_offline_is_not_an_active_wifi_network(self):
        self.check_dump('Active default network: none\n', 'WIFI', False)

    def test_missing_active_network_block_is_denied(self):
        self.check_dump('Active default network: 102\n  NetworkAgentInfo{network{101} nc{[ Transports: WIFI]}\n', 'WIFI', False)

    def test_inactive_wifi_cannot_mask_active_mobile(self):
        self.check_dump('Active default network: 102\n  NetworkAgentInfo{network{101} nc{[ Transports: WIFI]}\n  NetworkAgentInfo{network{102} nc{[ Transports: CELLULAR]}\n', 'WIFI', False)

    def test_capabilities_override_stale_legacy_type(self):
        self.check_dump('Active default network: 101\n  NetworkAgentInfo{network{101} ni{type: WIFI} nc{[ Transports: CELLULAR]}\n', 'WIFI', False)

if __name__ == '__main__':
    unittest.main()
