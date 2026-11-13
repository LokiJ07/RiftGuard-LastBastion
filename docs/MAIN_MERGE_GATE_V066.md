# RiftGuard v0.66.0-beta1 — Development Mainline Merge Gate

**Purpose:** Decide whether the complete 300-stage, 10-world feature branch can
be merged into `main` as a **playable development integration baseline**.
`main` is **not** a claim of Google Play production readiness.

## Non-negotiable functional gates

1. Android 16 compile/target 36, Java 17, Gradle full build; `testDebugUnitTest`,
   `assembleDebug` and `bundleRelease` succeed. Release bundle is unsigned.
2. 300-stage structural audit with 10 world bosses, all four difficulties,
   limited but meaningful enemy populations, no dropped queued wave spawns,
   preserved per-kill gold and heal/supply drops.
3. Android 10 and Android 16 virtual devices actually enter `scene=PLAY`
   with enemies alive and receive `MOVE / DODGE / BUILD / SKILL / ATTACK`
   events. A screenshot of a story panel or an empty prewave field
   is **not** a passing combat check.
4. Android 16 virtual device force-stops the app during wave 1, cold launches
   a fresh Activity, and verifies `HOME` followed by `PLAY paused=true`
   after the visible Continue button is tapped. Recovered battles restart
   at saved **wave boundaries**, not the exact last projectile position.
5. Atomic victory reward/unlock/checkpoint commit must not finalize victory
   on storage commit failure; failed write can be retried without duplicated
   supplies or lost recovery. Terminal victory/defeat cannot be reversed.
6. Backgrounding during the branding splash must not activate game/audio,
   and foreground resume must work.
7. Original game art is kept: NativeDeployedMapArt, detailed Warden/enemy rigs,
   biome weather, terrain props, upgraded towers, actual siege weapons,
   fortress visuals, sound, cutscenes and animated storyline.

Merge protocol: inspect the PR's exact head commit and checks; only mark PR ready
and merge when the **final head** has both successful Android CI and Evolution
Android Emulator QA runs, with passing tests and no missing resources.
Use GitHub's expected-head SHA guard. Do not silently publish.

## Separate public-release gates (NOT satisfied by a merge)

- Actual low/mid/high-end Android device frame pacing, touch testing, thermal
  stability, long gameplay sessions and player feedback on difficulty.
- Production signing keys, Google Play App Signing, signed App Bundle and
  tested update continuity without uninstalling existing versions.
- Google Play listing assets, verified screenshots, privacy/data-safety details,
  content rating, original art and font usage rights, legal/branding audit.
- Representative human playthrough through story branches, world bosses,
  optional Eclipse Cave, milestones and full 300-stage progression.
- Device-level restart tests across OS/vendor policies beyond emulator coverage.

**Release classification after main merge:** `0.66.0-beta1` development baseline,
not a `1.0.0` production release.
