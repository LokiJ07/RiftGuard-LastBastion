# RiftGuard v0.72: Commercial Packaging Safety Gates

This is a development-only release-infrastructure milestone. It is NOT a
v1.0 release, Play submission, or assertion of player-ready quality.

## Normal Android CI
- scripts/release_gate.py verifies target/compile Android API 36+, numeric
  versionCode, minSdk relationship, manifest cleartext prohibition,
  explicit debuggable setting and unexpected sensitive permissions.
- scripts/test_release_gate.py simulates valid and invalid packages, missing
  QA signoff and missing production signing credentials.
- Outputs a JSON release-policy-preflight artifact.
- The normal debug APK remains development-signed. CI's default release
  bundle remains UNSIGNED because no production credentials are present.

## Protected manual release candidate, WITHOUT automatic publishing
1. Merge reviewed development PRs in order once gameplay validation passes.
   Set a stable versionName and increase versionCode: production gate blocks
   alpha, beta, dev, rc and snapshot versions.
2. Complete real-device and human playtests, 300-stage campaign audit,
   extended framerate/thermal tests, licensed art rights audit, accessibility,
   privacy policy, Data safety, content rating and Play Console store listing.
3. Configure GitHub Settings > Environments > production-release with
   restricted main-branch deployment and a required approval reviewer.
   Store these protected environment secrets:
   RIFTGUARD_RELEASE_JKS_B64 (base64 of the encrypted Play upload JKS),
   RIFTGUARD_RELEASE_STORE_PASSWORD, RIFTGUARD_RELEASE_KEY_ALIAS,
   RIFTGUARD_RELEASE_KEY_PASSWORD.
   Never commit or expose a signing key or its password in chat/issues.
4. Dispatch .github/workflows/release-candidate.yml from MAIN and provide
   the explicit human-QA attestation. Production audit requires all
   credentials and rejects pre-release versions; signed bundle must pass
   jarsigner verification. Workflow only produces a short-lived signed
   AAB artifact for controlled Play internal testing; it does NOT publish.
5. Play Console may require a closed test: personal developer accounts
   created after Nov 13, 2023 need at least 12 opted-in testers continuously
   for at least 14 days before requesting production access. Confirm actual
   account eligibility.

References:
https://support.google.com/googleplay/android-developer/answer/11926878
https://support.google.com/googleplay/android-developer/answer/14151465

Passing static audits cannot prove creative quality, full rights ownership
or actual real-device/player satisfaction. These remain manual release gates.
