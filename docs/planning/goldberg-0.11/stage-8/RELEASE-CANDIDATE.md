## Permanent release policy — owner decision, October 25, 2026

Applies to Goldberg 0.11 and all future Gould versions. This supersedes earlier automatic publication authorization and historical RC instructions below.

- During development, build debug APKs only for internal testing and owner review. Do not create new release candidates, prerelease tags or GitHub Releases for development APKs.
- Preserve the existing `v0.11-rc.1` release, tag and artifacts as immutable historical evidence.
- Only after full implementation, verification and explicit owner approval, perform a fresh clean release build. It must be non-debuggable, signed with the trusted production certificate, pass all release-specific gates and support verified safe in-place upgrades.
- Publish only the explicitly approved stable version. Merging into `main` and publishing a stable GitHub Release each require explicit owner approval; passing checks alone grants neither.
- The current Stage 8 Discover repair delivers a debug APK and verification report for owner review, with no release publication.

Historical checkpoints below remain preserved; their prior publication authorization is superseded. The published RC source is `a73a7e2`; release/APK verification is recorded in Issue #31, not inferred from unchecked historical preparation tasks.

---

# Goldberg 0.11 release candidate

Owner now authorizes release when critical gates pass,or a truthful release
candidate withreal-user cloud collection disabled when hosted gates remain open.
Branch release/goldberg-0.11-rc.1,based onverified canonical f5fc296.

- [x] Both engineering branches preserved,pushed andexact remote SHAs verified.
- [x] Select0.11-rc.1/code12,above published0.10.1/code11;identity/database unchanged.
- [x] Keep production networking andreal-user cloud collection disabled.
- [ ] Build non-debug release APK,sign withthe existing trusted identity,verify ZIP/package/hash/signature.
- [ ] Essential JVM/lint andexisting native/upgradeCI;fix actual failures.
- [ ] Publish verified prerelease candidate andexact asset digest.
- [ ] Final0.11 release:hosted security/credentials/OAuth/privacy/backup/version/client adoption gates remain OPEN.

No main merge,hosted credential orWorker mutation is implied. Existing Stage6
Supabase20-table deployment is preserved;no repeated ACL/bootstrap action.
