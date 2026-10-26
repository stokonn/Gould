"""Local libpq service access. Never exports the administrative credential.

Only SQLSTATE is surfaced on failure: SQL text, parameters and libpq error text
may contain confidential provisioning material and must not enter tool logs.
"""
import ctypes as C
import ctypes.util
import os
from pathlib import Path
import stat


def protected_service(path):
    path = Path(path)
    info = path.lstat()
    if not stat.S_ISREG(info.st_mode) or info.st_uid != os.getuid() or stat.S_IMODE(info.st_mode) != 0o600:
        raise ValueError('Administrative service must be an owned regular 0600 file')
    if stat.S_IMODE(path.parent.stat().st_mode) & 0o077:
        raise ValueError('Administrative service directory must be private')
    return path


class _Option(C.Structure):
    _fields_ = [(name, C.c_char_p) for name in ['keyword', 'envvar', 'compiled', 'val', 'label', 'dispchar']] + [('dispsize', C.c_int)]


class ServiceConnection:
    def __init__(self, path, service='goldberg_stage6_admin'):
        path = protected_service(path)
        if service != 'goldberg_stage6_admin':
            raise ValueError('Explicit administrative service required')
        self.lib = p = C.CDLL(ctypes.util.find_library('pq'))
        signatures = {
            'PQconnectdb': ([C.c_char_p], C.c_void_p), 'PQstatus': ([C.c_void_p], C.c_int),
            'PQfinish': ([C.c_void_p], None), 'PQexec': ([C.c_void_p, C.c_char_p], C.c_void_p),
            'PQresultStatus': ([C.c_void_p], C.c_int), 'PQclear': ([C.c_void_p], None),
            'PQntuples': ([C.c_void_p], C.c_int), 'PQnfields': ([C.c_void_p], C.c_int),
            'PQgetvalue': ([C.c_void_p, C.c_int, C.c_int], C.c_char_p),
            'PQgetisnull': ([C.c_void_p, C.c_int, C.c_int], C.c_int),
            'PQresultErrorField': ([C.c_void_p, C.c_int], C.c_char_p),
            'PQsslInUse': ([C.c_void_p], C.c_int), 'PQconninfo': ([C.c_void_p], C.POINTER(_Option)),
            'PQconninfoFree': ([C.POINTER(_Option)], None),
            'PQencryptPasswordConn': ([C.c_void_p, C.c_char_p, C.c_char_p, C.c_char_p], C.c_void_p),
            'PQfreemem': ([C.c_void_p], None),
        }
        for name, (args, result) in signatures.items():
            fn = getattr(p, name); fn.argtypes = args; fn.restype = result
        # libpq consumes the protected service itself. Password options are never
        # returned to Python or passed to subprocess arguments/environment.
        previous = os.environ.get('PGSERVICEFILE')
        os.environ['PGSERVICEFILE'] = str(path)
        try:
            self.conn = p.PQconnectdb(b'service=goldberg_stage6_admin sslmode=verify-full connect_timeout=5 application_name=goldberg-private-provisioner')
        finally:
            if previous is None: os.environ.pop('PGSERVICEFILE', None)
            else: os.environ['PGSERVICEFILE'] = previous
        if not self.conn or p.PQstatus(self.conn) != 0:
            self.close(); raise RuntimeError('Protected PostgreSQL connection failed; no automatic retry')
        if p.PQsslInUse(self.conn) != 1:
            self.close(); raise RuntimeError('Client TLS is mandatory')

    def public_options(self):
        allowed = {'host', 'port', 'dbname', 'user', 'sslmode', 'sslrootcert'}
        options = self.lib.PQconninfo(self.conn); result = {}; i = 0
        try:
            while options[i].keyword:
                key = options[i].keyword.decode()
                if key in allowed and options[i].val:
                    result[key] = options[i].val.decode()
                i += 1
        finally:
            self.lib.PQconninfoFree(options)
        return result

    def query(self, sql):
        result = self.lib.PQexec(self.conn, sql.encode())
        try:
            if not result or self.lib.PQresultStatus(result) not in (1, 2):
                code = self.lib.PQresultErrorField(result, ord('C')) if result else None
                raise RuntimeError('PostgreSQL operation failed: SQLSTATE ' + (code.decode() if code else 'unavailable'))
            return [[None if self.lib.PQgetisnull(result, row, col) else self.lib.PQgetvalue(result, row, col).decode()
                     for col in range(self.lib.PQnfields(result))] for row in range(self.lib.PQntuples(result))]
        finally:
            if result: self.lib.PQclear(result)

    def password_verifier(self, password, role):
        value = self.lib.PQencryptPasswordConn(self.conn, password.encode(), role.encode(), b'scram-sha-256')
        if not value: raise RuntimeError('libpq SCRAM preparation failed')
        try: return C.string_at(value).decode()
        finally: self.lib.PQfreemem(value)

    def close(self):
        if getattr(self, 'conn', None):
            self.lib.PQfinish(self.conn); self.conn = None

    def __enter__(self): return self
    def __exit__(self, *_): self.close()
