# RiftGuard public release gates — v0.65 alpha5

Status: **development / draft PR #23**. This document is an acceptance checklist, not a declaration of release readiness.

## v0.66.0-alpha2 — Actual graphics quality tiers for Android publication

- [x] HIGH retains full 1900×1050 static battlefield raster (3.99 MB RGB_565 bitmap); BALANCED 1596×882 (~2.82 MB), PERFORMANCE 1292×714 (~1.84 MB). CPU-side bitmap footprint alone is ~54% lower in PERFORMANCE. GPU drivers may expand texture formats, so this is **not** a measured GPU-memory or FPS claim.
- [x] Keep every live Warden/enemy animation, fortress build model, gun aim/recoil, projectile hitboxes, AI, level routes and HUD fully rendered in their original world coordinates; only the static bitmap and optional particle density change.
- [x] More precise quality differentiation: atmospheric motes 23 / 16 / 9; realm snow, ember and firefly particles also scale by quality. Sustained slow frames temporarily trim optional effects without hiding combat.
- [x] PERFORMANCE now requests a 30 FPS battery/performance cadence on 60/90/120 Hz displays; HIGH/BALANCED keep a 60 FPS goal. Actual frame rates remain workload-dependent.
- [x] Static bitmap creation falls back to smaller terrain resolutions if Android bitmap allocation runs out of memory. Home/Pause quality selection persists using the existing saved quality preference.
- [x] New tests exercise all ten kingdom terrains, live quality switching, cache reuse, correct resolution, particle budgets and display-specific frame pacing.
- [ ] Physical-phone visuals and 30–60 FPS performance verification across 60/90/120 Hz and low/mid/high RAM/GPU configurations; no release FPS claims from JVM/emulator measurements.
- [ ] Evaluate anti-aliasing and texture upload/bandwidth with GPU profiling. Avoid reducing live actor sprite resolution or touch readability.

## v0.66.0-alpha1 combat-render / boss telegraph pass

- [x] Render-only `BattleViewportV066` matches the live combat / moving victory camera and zoom; prune off-camera projectile, shaman bolt, death echo, impact and spark drawing. Actual projectile simulation, enemy AI, hitboxes and saved progression remain unchanged.
- [x] Replace per-frame `Path` creation across actor polygon rendering, projectile lightning/shards and crystal weather with reusable native drawing objects, plus 76 direct polygon array allocations removed from fortress and other screens.
- [x] Reuse background and hero-contact primitives; omit one redundant full-screen opaque fill; menus/pause run at a 30 FPS cosmetic redraw target while combat/cinematics seek 60 FPS and touch input invalidates immediately.
- [x] Add ground-bound boss threat indicator tied to the existing 265-unit hero impact radius, 445-unit fortress siege range, actual boss countdown, kingdom color and reduced-motion preference. No gameplay balance changes.
- [x] Robolectric tests exercise viewport geometry, moving/outro camera visibility, repeated effects, all ten kingdom boss telegraph colors and accurate warning countdown.
- [ ] Compare **repeatable GPU frame pacing and 30-minute thermals on actual low/mid/high Android phones**; improvements to allocations do not guarantee FPS. The software Canvas/JVM benchmark and virtualized Android emulator are diagnostic only.
- [ ] Profile heap allocations, graphics overdraw, GPU draw calls and long-session GC under heavy waves.
- [ ] Confirm boss danger circles don't obstruct important scene art on a real handset.

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
