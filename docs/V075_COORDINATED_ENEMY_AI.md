# RiftGuard v0.75 — Tactical Enemy Coordination

Draft stacked on v0.74 PR #34, **not merged to main**.

## Live mechanics
- SIEGE ENGINEERS waves in later realms mark ONE built defense pad. Existing sling raiders, brutes and sappers move toward that physical turret and can actually hit it. The marked pad changes only when destroyed or the wave changes; the Warden can draw aggro, kill enemies or repair the tower. Individual focused hits are limited to 22% of full tower HP.
- Shamans below 42% HP and within 260 world units of Warden retreat for 1.35 seconds **only when near an alive brute protector**. Shield destruction or close interception prevents new retreat. The fallback has a 7.4-second per-shaman cooldown and no heal bonus.
- Existing raiders take two shorter, readable pincer approaches during FLANK formation; these merge back at castle gate. No new enemies, art replacement, save keys or particle spam.
- Stages 1–30, mission objective priorities, 10 boss attack geometries, and the 96-event schedule remain intact.

## Verification
Unit tests for all 300 stage IDs and counterplay. Real Robolectric tests drive actual update() to verify tower pressure, live reassignment, healer movement and distinct pincer paths. Android CI with debug APK, release-bundle verification, Android 10 / Android 16 emulator input/graphics smoke QA.

## Limitations
Emulator QA alone is not an all-stage manual gameplay quality guarantee; full real-device/thermal and playtester checks still gate release.
