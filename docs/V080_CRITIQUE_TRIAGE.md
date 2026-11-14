# RiftGuard v0.80 — External Critique Audit and Continuity Fixes

**Source:** User-provided external static-code review (14 November 2026). The reviewer explicitly said they **did not play or build the game** and skimmed the ~6.5k-line `RiftGameView`. We checked its claims against the current `main` implementation rather than accepting every suggested code sample verbatim.

## Implemented in this change

| Critique | Verified code issue | Fix |
|---|---|---|
| A1: Chada appears twice | Prologue removed Chada into Cave, but world 4–7 recovered/reforged sword, and Cave again presented Chada waiting | Titan shatters Chada in the Knight's hand; shard/hilt recovery and refit remain. Cave shows the **Knight carrying** the already-reforged sword |
| A2: Cael is spoiled early | `StoryScript` exposed `CAEL` throughout pre-reveal dialogue; `RiftGameView` also showed CAEL as final boss's name on spawn and HP HUD | Use **TITAN** for all main-path dialogue until actual Cave phase 3; boss spawn/HUD switch to Cael only at phase 3. Keep Cael labels in **optional memories**, the reveal and endings |
| A3: Chada heirloom chronology | W1 Knight already drew Chada; W3 before-knighthood passages said handed over; W4 Warden claims to have named an older sword | W1 uses borrowed sword; W3 is trainee/practice; W4 proper heirloom transfer and established Chada name; W10 key matches shattered sword |
| A4: Tessa payoff inaccessible | `Salt, not sugar` pivotal memory was optional; chapter path barely used her | Main path foreshadows the phrase in world 3, pays off in world 6 with Pell, and adds short Tessa lines to world 2 and world 4 introductions |
| B1: All 100 optional trials gate mercy | `trueEndingUnlocked()` required `total()==100`, despite only **33 Cael memories** | Requires **30 of 33 Cael memories** (three can be missed) plus existing complete campaign; **100** collectible keys still exist and the save format does not change |
| B2: Bantay distant in partial ending | He was physically waiting in Cave before the partial ending claimed only a distant bark | In the partial ending Bantay actually runs to Knight; they leave together while Cael remains bound and the Rift unresolved |
| E: unreadable, outdated README | 350+ lines of version-by-version release notes; obsolete 'stable v0.30' sections | Preserve original complete README under `docs/archive/README_THROUGH_V079.md`, replace root README with a concise current overview, testing, play instructions and links; correct outdated v0.79 notes |

**Additional pacing improvement:** Short, two-line camp dialogue plays once per save every third campaign level when entering Map A. It can be skipped, never gates combat, cannot pay a reward, and is available to older saves that replay the level. Tessa speaks only before Frostpeak ends; later lines use allies already recruited.

**Verification:** Existing story/XP/save/weapon/fortress tests remain part of CI. Added `StoryContinuityV080Test` covers early speaker secrecy, Chada/Tessa/Bantay continuity, gate boundary at 29 vs 30, persistence and 300-stage cave restriction. Updated legacy story test to expect the mask rather than the premature name reveal. Emulator/API29/36 and production packaging preflight run through the existing workflows.

## Not changed yet — intentional risk control

- **B3 MemoryTrial overrides:** The review's example `OVERRIDE[5][9]=RELIC` claims it defends a kettle. In the actual game that stage is **Ironfall world boss** and the relic is the **Foundry Generator**, so the suggestion does not match the existing objective or lore. Trial types and actual combat geometry are unchanged. A future authored key-by-key trial design should change goals **and** environmental props/tests together.
- **C Long campaign / 300-stage cave access:** This is a legitimate player-retention risk, but turning A/B stages optional, changing the linear `unlocked` key or substantially cutting waves can invalidate old saves and balance. No change to unlock order or difficulty tuning without a dedicated campaign pacing playtest.
- **D1 God class:** `RiftGameView` still owns numerous scenes and combat/render/input code. Extract individual scenes gradually under separate integration tests, beginning with Cave presentation; avoid broad untested refactors.
- **D2 Synchronous preferences:** The atomic victory commit is **deliberately durable** and should not become `apply()`. The review's proposed `apply()` for `claimMemory` would report success before persistence; preserve it until a tested asynchronous/transactional approach is available. Measure real-device frame hitches first.
- **D3 Internet:** The manifest enables user-initiated **optional** art downloads via `ResourcePacks`, including checksummed files. Removing `INTERNET` without redesigning that UI would break the feature. The playable campaign remains offline; resource downloading needs network.
- **D4 Fixed timestep:** The render/simulation cadence warrants profiling, but replacing `update(dt)` with a loop changes boss ticks, animation/contact events, save checkpoints, cooldowns and hitstop. Avoid the supplied 1/60 snippet without deterministic simulation and emulator/device regression coverage.
- **D5 Draw allocations/minification:** Profiling is necessary to identify hotspots and protect low-memory devices. `minifyEnabled false` and package `0.78.0-alpha1` remain, since this is not a stable release candidate. Version bump/signing belongs in a controlled release step.

## Release policy

This patch is a **story and save-compatible alpha**. CI success demonstrates build/tests and emulator smoke, not guaranteed fun, FPS/thermal performance or full 300-stage content quality. Keep production release gates intact. No automated public Play Store publishing is introduced.
