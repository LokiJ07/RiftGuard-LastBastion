# RiftGuard public release gates — v0.65 alpha5

Status: **development / draft PR #23**. This document is an acceptance checklist, not a declaration of release readiness.

## A. Android build / distribution
- [x] Android 16 API 36 compile/target configuration and supported AGP/Gradle build tooling.
- [x] Android manifest declares appCategory=game; landscape-only, fullscreen gameplay remains.
- [x] Debug APK and unsigned release App Bundle build in CI (test artifacts, not distributable store products).
- [ ] Secure **production upload key**, Google Play App Signing, and **signed** AAB. Do not commit keystore or passwords.
- [ ] Check app icon, feature graphic, accurate actual-game screenshots, trailer, description, privacy policy, Data safety form, content rating, and copyright/license audit.
- [ ] Test internal/closed track, release signing updates, staged rollout and data continuity before production.

## B. Engine / rendering
- [x] High-detail original Warden idle/walk frame cache; attacks/casting/hurt/death still use live rigs.
- [x] Bounded enemy/fortress/warden caches selected from Android's actual low-memory and per-app memory-class signals; never by a particular phone brand.
- [x] Static five-rank fortress art cached with live turrets, aim, recoil, hit reactions and animated overlays; fallback to the original renderer on memory exhaustion.
- [x] Cached map staging, reused full-screen HUD/cinematic shaders, lightweight adaptive ambience.
- [x] Android low-memory cache reclaim that preserves all gameplay and saved progress.
- [ ] **Physical-device** CPU/GPU profiling and frame pacing verified on representative low/mid/high Android phones; emulator timing is NOT a real-phone benchmark.
- [ ] Long-run thermal/battery/ANR/GC measurements during dense 20+ enemy waves and boss scenes. Pass criteria: no hangs; stable 30 FPS entry-class, stable 60 FPS on capable mid/high devices without visible combat input drops, and no explosive memory growth.
- [ ] Full original illustrated 2D sprite atlas (license-cleared) and more differentiated realm background art. Do not substitute copyrighted third-party sprite assets in the public repo.

## C. Functional tests
- [x] Pure structural audit traverses all 300 stages, route coordinates, 5–8 live pads, hazards, destructible obstacles and 100 Last Stand memory objectives.
- [x] Robolectric input/draw matrix covers 4:3, 16:10, 16:9 and 21:9 screen layouts; real phone cutouts/gesture navigation remain unverified.
- [x] Existing automation covers upgrades, wave checkpoints, defenses, boss components, combos, memory, three endings and cinematics.
- [ ] Human complete-game QA from fresh save and migrated save, including 300 stages and all 100 recoverable memories.
- [ ] Live boss attack telegraph balance, tower repair/rebuild UX, dodge/skill control readability and every upgrade's exact value/appearance.
- [x] Restart from the last **saved wave boundary** after Android process death: durable JSON snapshot of active towers, type/tier/health, gold, kills, hero/core/relic HP, mission counters and difficulty. HOME shows a CONTINUE action; battle resumes paused, re-spawns the wave rather than restoring in-flight projectiles/enemies.
- [x] Clear recovery on intentional exit/defeat, and commit campaign reward/unlocks plus checkpoint deletion as **one durable preferences transaction** to prevent replayable rewards.
- [ ] Real Android app-process-kill/reboot automated instrumentation and interrupted-storage simulation; current tests simulate restart with a fresh game view and shared persisted preferences (Robolectric).
- [ ] Full pixel-level mid-wave continuity is **not** supported: recovery intentionally restarts at wave start for deterministic safe game state. Eclipse Cave must have a separate special-boss recovery design.
- [ ] Accessibility: scalable text and large hit targets, reduced-motion visuals, contrast, one-handed options and safe-area testing.

## D. Story, presentation and live art
- [x] Distinct story world settings, memory replay, boss dialogue, victory road walk, death outro and Eclipse Cave branching are wired.
- [ ] Production-grade, individually authored animated story scenes with expressive original characters and licensed SFX/music.
- [ ] Further distinct encounter patterns and tactical level objectives across 300 stages to prevent same-wave repetition.
- [ ] Polish shop/HUD/fortress transforms after real player sessions.

## E. Release process
- Gate 0: CI build, unit and emulator smoke green; no accidental regressions.
- Gate 1: API 36 + reproducible unsigned AAB build.
- Gate 2: QA device matrix green, process-death/save verification, boss and densely populated wave frame profiles.
- Gate 3: license-cleared art/audio, complete progression, usability playtest, final signed bundle.
- Gate 4: closed testing and production rollout checklist completed.

The current implementation addresses Gate 0 / Gate 1 foundation. **Do not merge or submit to Play Console until the remaining gates are verified.**
