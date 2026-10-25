# Goldberg 0.11-rc.1 — published candidate verification

[PUBLISHED PRERELEASE](https://github.com/stokonn/Goldberg/releases/tag/v0.11-rc.1),
not the final0.11 release. Stable latest remains0.10.1. Annotated tagv0.11-rc.1
peels tosource a73a7e21adf656dcbe7ed6da351a71e9a681a384.

Release variant/com.opus.piano/version0.11-rc.1/code12/non-debuggable;16,335,515bytes.
APK SHA256 c5662d8ead2b175fac0be09145776e84936c2b8f794c85b967f71aaab1938bb5.
Trusted signer53eface4978f03dd7f0d7f1eb97ab94eed97055fe04a2a0fab9a7f280141d59e
matches the actual published0.10.1 APK. ZIP/resources,allowBackup=false andexclusion
of test/private fixtures verified. Remote asset digest andre-downloaded bytes match.
The release uses the existing local signing identity;no keys orpasswords entered Git.

-302 JVM tests PASS,0failures/errors/skips;local release lint0errors132warnings1hint.
-API36 native203 cases:192 PASS/0failures/0errors/11 existing skips(two external
 source fixtures,nine separately driven preservation/reboot/process probes).
 Allfour new canonical application tests PASS onintegration sourcef5fc296;
 exactRC fullsuite also passes. No skipped probe is counted as executed.
-Seeded0.10.1 schema8→10 upgrade:original rows/preferences and37sessions preserved;
 offlineVerified=true,FKpass;process-death restart integrity/FK checks PASS.
 CI upgrade uses debug rebuilds with a runner key;release signature compatibility
 is independently checked against the trusted published APK. No physical phone.
-Stage6:506 backend bodies,8packaged workerd denials,8probe components/5local
 probe runtime,6archive and6actual localTLS/export/restore checks PASS;actual
 Kotlin application service→HTTP→BFF→PostgreSQL1PASS. No hosted runtime inferred.

CI: [RC quality](https://github.com/stokonn/Goldberg/actions/runs/40212615987),
[RC native+upgrade](https://github.com/stokonn/Goldberg/actions/runs/40212684482),
[canonical native](https://github.com/stokonn/Goldberg/actions/runs/40212209517),
[BFF](https://github.com/stokonn/Goldberg/actions/runs/40212884120),
[Stage6 quality](https://github.com/stokonn/Goldberg/actions/runs/40212884026).
All COMPLETED/SUCCESS at their exact source SHAs. Earlier failed40212173970
historical-pin check is retained;reviewed tooling evolution fixed atf300b09.

Production community networking,cloud sign-in andreal-user collection remain
DISABLED. Supabase20private/RLS tables andaccepted ledger/ACL deployment verified;
no new Worker,password orremote session deployed. No paid service ormain merge.
Remaining final-release blockers:CF403/confidential credentials/privateTLS/runtime/
OAuth;production backup custody;privacy/source/license andpublic API/client adoption.
Stage6/6.0f/6.17 andIssue31 remain OPEN;Project InProgress. No API26 run ornew
UI screenshot inspection;existing accepted UI is unchanged.

created by stokonn with love <3
