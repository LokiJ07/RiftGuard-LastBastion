# RiftGuard 0.67.0-alpha1 — Combat Feel, Boss Fairness, HUD Clarity

Development branch: `feature/v067-combat-feel-hud-boss-polish` forked from merged `main` 0.66.0-beta1.
This branch must remain **DRAFT** until real Android emulators and all Robolectric tests pass.
This is a targeted *first* v0.67 milestone, not the complete art/story/release overhaul.

## Improvements implemented in this pass

- **Combat feel**: boss hits now trigger the Warden's actual hurt animation,
  clear pending weapon contact, play hurt feedback, and grant the same 0.8s
  post-hit grace period as ordinary melee hits. This prevents a non-obvious
  double-hit between a boss charge and a nearby mob in one combat moment.
  Player's dodge invulnerability still fully avoids the boss attack.
- **Meaningful boss differentiation**: separate seismic, sustaining and
  rooting families now have tactical phase-specific hero danger radii and
  sustained-heal strength. A pure `BossAssaultV067` helper is shared between
  world collision and the live ground warning circle. No false safe zones or
  untelegraphed hit range; existing biome/phase visuals remain intact.
- **Context-only HUD circles**: cooldown progress rings for Dodge and Skill,
  plus an actual held-attack charge ring around the movable Attack button.
  Coordinates match the current player-edited button positions. Rings do not
  intercept touches or render new obstructing panels; they disappear when no
  longer relevant.
- **Old HUD layout safety**: loaded invalid saved overlapping controls are
  replaced in memory with the original safe defaults. No permanent purchases,
  world progress or unrelated preferences are touched.
- **Performance**: `BattleRules.enemySpeed` and `enemyHealth` use static
  immutable lookup tables instead of allocating float arrays in active combat.
  Per-turret family damage arrays and per-hit contact slash `Path` reuse
  remove further short-lived allocations. This change **does not** claim an
  FPS improvement without repeatable real device evidence.

## Do not regress

- 300 illustrated stages, all existing 10 biomes, Warden rig / sprites,
  monster art / monster AI, evolved weapon art and sound, 5 fortress ranks,
  tower stat upgrades and weapon/fortress skill trees, deterministic
  campaign story and chapter endings.
- All movable gameplay control touch tests and saved HUD positions.
- Wave-boundary battle recoveries, atomic victory rewards, gold/HP economy.
- Original enemy count balance: Easy/Normal/Hard/Expert caps and queued spawns.
- Boss warning ring is **visual feedback for real damage**, not an additional
  damaging attack or a fake warning shape.

## Loop engineering acceptance

1. All existing plus new boss/hud/performance unit tests pass.
2. Real Android 16 emulator QA navigates through chapter screens and runs
   a real enemy battle with all five touch controls; force-stop / Continue
   restores a paused wave.
3. Android 10 emulator smoke and debug APK build pass.
4. Inspect generated screenshots for art intact, no misleading overlays.
5. Release this branch's debug APK for hands-on playtesting only.
6. Any failed check must be investigated, patched and re-run before
   considering the branch mergeable.

Remaining future v0.67 priorities: physically profile sustained phone GPU
frame pacing, more world-unique boss attacks and encounters, finer art polish,
player session feedback on wave length/health curves, and maintainability
refactoring of the large `RiftGameView` view controller.
