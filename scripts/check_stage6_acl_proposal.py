from pathlib import Path


path = Path("docs/planning/goldberg-0.11/stage-6/ACL-MIGRATION-PROPOSAL.md")
text = path.read_text(encoding="utf-8")

required = [
    "to_regprocedure('public.rls_auto_enable()')",
    "SECURITY DEFINER",
    "ensure_rls",
    "REVOKE EXECUTE ON FUNCTION public.rls_auto_enable()",
    "a.grantee=0",
    "ROLLBACK",
]
for fragment in required:
    if fragment not in text:
        raise SystemExit(f"missing ACL safeguard: {fragment}")

for forbidden in ("DROP FUNCTION", "DROP EVENT TRIGGER", "CREATE OR REPLACE FUNCTION"):
    if forbidden in text:
        raise SystemExit(f"unexpected destructive ACL operation: {forbidden}")

print("PASS: ACL proposal is function-scoped, trigger-guarded, and rollback-oriented")
