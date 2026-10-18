# Branch cleanup audit — 0.10.1

Default branch: main. No open PRs at baseline audit. No branch has been deleted. Deletion is gated on verified 0.10.1 integration; tags are preserved.

| Remote branch | Head | Commits not reachable from main | Status |
|---|---|---:|---|
| origin/feature/goldberg-0.10 | `7b5874e8ffd2d01eec8de9ad58cf64e195cf0a5b` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.6 | `20cf5eb86711c9126915f682e50311c54b892d90` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.6.1 | `29d7d7a764a1a33d9d8d7c7d5e5552c5169ad86c` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.7 | `5d85749ac52ed01f44d755a0eacc1ccfc3765844` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.8 | `5b22fba2fdfd2dd4f4dc3f5f68293f3e7d5e7654` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.9 | `4618b50c67e223d7f1b5bf9ee0951f3a79ecfac7` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.9.1 | `9eeabd798dcf8c70766a530f32835e499d68da3b` | 0 | Retained pending verified 0.10.1 integration |
| origin/feature/goldberg-0.9.2 | `78fd88fbf4ead8772b87d2b986807a381241b7dd` | 0 | Retained pending verified 0.10.1 integration |

Current feature/goldberg-0.10.1 and main are retained. Recheck remote refs, open PRs and workflow references immediately before any deletion. Use normal local branch deletion and explicit remote branch refs only; never force-delete.
