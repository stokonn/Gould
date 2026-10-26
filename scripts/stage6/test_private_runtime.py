from pathlib import Path
import os
import tempfile
import unittest
from unittest.mock import patch
from libpq_service import protected_service
from provision_private_runtime import catalogue_grant_statements, secret_file, provision, ROLE

class PrivateRuntimeTest(unittest.TestCase):
    def test_service_permissions_and_symlink_rejected(self):
        with tempfile.TemporaryDirectory() as work:
            p=Path(work)/'service';p.write_text('[unused]');p.chmod(0o600)
            self.assertEqual(protected_service(p),p)
            p.chmod(0o640)
            with self.assertRaises(ValueError):protected_service(p)
            p.chmod(0o600);link=Path(work)/'link';link.symlink_to(p)
            with self.assertRaises(ValueError):protected_service(link)

    def test_secret_creation_is_private_and_never_overwrites(self):
        with tempfile.TemporaryDirectory() as work:
            p=Path(work)/'secret';secret_file(p,{'synthetic':'value'})
            self.assertEqual(p.stat().st_mode&0o777,0o600);before=p.read_bytes()
            with self.assertRaises(FileExistsError):secret_file(p,{'replacement':True})
            self.assertEqual(p.read_bytes(),before)

    def test_catalogue_scope_is_explicit_and_injection_rejected(self):
        sql='\n'.join(catalogue_grant_statements())
        self.assertNotIn('GRANT goldberg_bff_runtime',sql)
        self.assertNotIn('INSERT',sql);self.assertNotIn('DELETE',sql)
        self.assertEqual(sql.count('CREATE POLICY'),5)
        self.assertIn('UPDATE(singleton)',sql)
        for schema,role in [('public',ROLE),('goldberg_private','postgres'),('goldberg_private;',ROLE)]:
            with self.assertRaises(ValueError):catalogue_grant_statements(schema,role)

    def test_provision_does_not_put_plaintext_password_in_sql(self):
        class Fake:
            def __init__(self):self.statements=[]
            def query(self,s):self.statements.append(s);return []
            def password_verifier(self,*_):return 'SCRAM-SHA-256$synthetic-verifier'
        with tempfile.TemporaryDirectory() as work:
            c=Fake();options={'host':'synthetic.invalid'}
            with patch('provision_private_runtime.preflight',return_value=(options,'PUBLIC TEST CA',{})),patch('provision_private_runtime.secrets.token_urlsafe',return_value='SYNTHETIC-ONLY-PASSWORD'):
                r=provision(c,Path(work)/'custody')
            self.assertFalse(r['administrativeCredentialExported'])
            self.assertNotIn('SYNTHETIC-ONLY-PASSWORD','\n'.join(c.statements))
            self.assertNotIn('ALTER ROLE '+ROLE+' PASSWORD','\n'.join(c.statements))
            self.assertTrue((Path(work)/'custody/runtime.json').is_file())

    def test_uncertain_commit_retains_private_recovery_material(self):
        class Fake:
            def query(self,s):
                if s=='COMMIT':raise RuntimeError('uncertain')
                return []
            def password_verifier(self,*_):return 'SCRAM-SHA-256$synthetic-verifier'
        with tempfile.TemporaryDirectory() as work:
            with patch('provision_private_runtime.preflight',return_value=({'host':'synthetic.invalid'},'PUBLIC TEST CA',{})):
                with self.assertRaises(RuntimeError):provision(Fake(),Path(work)/'custody')
            p=Path(work)/'custody/runtime.pending.json'
            self.assertTrue(p.is_file());self.assertEqual(p.stat().st_mode&0o777,0o600)

if __name__=='__main__':unittest.main()
