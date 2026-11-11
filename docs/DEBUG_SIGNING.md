# RiftGuard CI debug signing — update-install compatibility

## Verified issue (2026-11-11)
Earlier GitHub CI runs produced different APK signer certificates. Android treats packages with the same app ID but different certificates as incompatible, so installing an APK as an upgrade may fail.

## Mitigation (v0.5.7+)
- The workflow reuses an Android **development-only** signing keystore through GitHub Actions Cache.
- On the first run, it creates a fixed debug alias and caches the keystore; later runs restore it.
- Each build prints its signing certificate digest, allowing comparison across runs.
- This debug cache can be evicted or manually cleared; therefore it is not a cryptographically permanent substitute for a secure release signing identity.
- Real public distribution needs a separate, private release key stored in protected GitHub Actions secrets.

## Existing installs
The older APKs were signed with separate random debug keys. This fix cannot change those old signatures. If Android reports `App not installed` / `package conflict`, reinstall may be required. **Uninstalling can erase saved progress**; avoid uninstalling until a save export is available or you accept losing local progress.

Never present the public debug certificate as a production trust guarantee. No user credentials or ad SDKs are involved.
