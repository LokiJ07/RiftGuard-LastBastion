# Ad-free online resource packs + offline play

Implemented resource pack architecture (Android v0.5.4).

- **No ads, account, tracking, advertising SDK, background network polling or forced login.**
- Home → **RESOURCES / OFFLINE ART PACK** → Download Pack. Downloads only when explicitly tapped.
- GitHub hosts a small JSON manifest: `game-content/manifest.json`; assets use immutable commit-pinned GitHub raw URLs.
- Every file is checked against manifest-declared byte length and Git's SHA-1 blob checksum. HTTPS and strict allowlist prevent redirect/host/path substitution.
- Download happens on background thread with progress and timeouts. Files are installed atomically into private `noBackupFilesDir/riftguard-resources`.
- If data disappears, app is offline, or a download fails, the **bundled base art and gameplay continue to work**. Restart the app to reload downloaded resources into the renderer.
- The current initial pack mirrors the existing bundled art to prove this pipeline; **it is not a new high-fidelity art upgrade**. Future manifest versions can point to improved individual sprites and environments. Same expected atlas dimensions are needed until renderer versions add a metadata-driven format.
- Downloaded content stays on device and is used offline; app updates preserve private files, but uninstall generally removes them.
- Online resource delivery cannot by itself replace proper combat animations, separate layers and new authored art.
- To keep downloads optional and fair, do not gate game progression or gameplay behind the network.

## Updating an asset without an APK release
1. Generate game-ready separated artwork and pack it to the expected atlas layout/resolution.
2. Commit it to the repository, capture its Git blob SHA and byte size.
3. Update the manifest version and file URL (pin to commit SHA), bytes and git_sha.
4. Merge the manifest and let users tap **Download Pack** in the APK.
5. Regression test online success, partial download, offline app start, old cache and device rendering.

The signed production release and download CDN can be added separately; GitHub raw is a practical prototype host, not a guaranteed unlimited high-traffic CDN.

## v0.5.6 integrity hardening
- Full downloaded manifest is now stored in app-private storage **only after all assets pass checksum and size verification**.
- At offline load, every cached asset is checked against its saved Git blob checksum; corrupt/missing files automatically fall back to the bundled APK artwork.
- Resource Center's installed count reflects **verified** files, not arbitrary files with plausible filenames and sizes.
- Resource Center caches verification results for the UI rather than hashing downloads on every render frame.
- Test suite mutates a cached file without changing its length and verifies that it is rejected.
- An older early-preview cache without verified metadata is intentionally ignored until the user runs Download / Verify once; the offline base game keeps working.
