# RiftGuard v0.60 — Authentic Character Rig Integration

**Status:** v0.60.0-dev1, draft branch, subject to Android CI and visual/performance QA. NOT the finished v0.60 game.

## User-provided code is the visual baseline

The 713-line original `WardenRig.java` and 1,054-line original `EnemyRig.java` from the user's supplied ZIPs are integrated **in their actual detailed form**, rather than redrawn into flat placeholder figures.

- `WardenRig.java`: preserves 2-bone leg IK, planted feet, directional stride, torso and head movement, separate gauntlet and shoulder pieces, cloth and cape secondary animation, slash tip trails, weapon-specific upper-body travel, cast/hurt/death states and detailed original multi-tone armor drawing.
- The Warden's actual held weapon is upgraded from the older `WorldArt.drawHeldWeapon` call to the v0.51 save-backed `EvolvedWeaponArt.draw` with **six weapon families x five mastery forms**. Exact arm socket and attack clock still use `WeaponRig.aimedPose`.
- A small articulated extra armor detail layer follows the existing purchased Ascension armor tier without replacing existing `EvolutionEffects.armor`.
- `EnemyRig.java`: preserves species-specific articulated attack anatomy (dagger, sling, club, magic orb, staff, slam, boar tusks), biome-specific horns/ice/mushrooms/moss/metal/crystals/lightning/bone/gold, glow veins, and themed particles.
- New seeded per-enemy physical equipment adornments distinguish variants beyond the original size-only scale; boss phases 1–3 gain visible body-anchored ornament / horns / crystals.
- `RiftGameView.java` retains existing enemy HP, combat behavior, camera, collisions, 300 campaign stages, 66 weapon mastery nodes, 40 Ascension nodes, 5 fortress ranks, arsenal gating and save keys. Only actor rendering is swapped using two reversible fallbacks (`USE_VECTOR_WARDEN`, `USE_VECTOR_ENEMIES`).
- Enemy-facing, per-enemy variant and boss phase survive into death echoes.
- `DetailedActorRigV060Test` renders 30 equipped forms, 5 armor looks, seven species, ten realms, four variants and three live boss-phase visual states into CI screenshot gallery.

## Removed

The earlier experimental `LivingRigArt.java` and its regression test were explicitly removed. Their visual fidelity did not match the user-provided detailed rigs.

## Next required release gates

1. Confirm Android Java compilation, all Robolectric regression tests and emulator screen capture on this candidate SHA.
2. Visually inspect the **actual** screenshots, rather than relying on class names, source assumptions or generated concepts.
3. Check frame latency and memory under the dense combat scenarios; avoid claiming optimized when not measured.
4. Additional authored/painterly texture and skeleton-specific animation polish, and biome-exclusive enemy anatomies, remain future v0.60 tasks, not completed claims.
5. Keep draft branch unmerged until visual QA and performance are acceptable.

## v0.60.0-dev3 — Three original character styles and enemy equipment families

**Asset reference audit:** The user's CraftPix archive contains 3 distinct knights,
each with 7 named action clips (Idle, Walk, Run, Jump, Attack, Hurt, Die) and
10 PNG frames per clip = 210 reference animation frames, plus separate
body-part vectors and Spriter/SCML data.

**Licensing:** CraftPix free assets may be used within game projects, but raw
or reusable source artwork must not be redistributed. Since this project
is a public repository, do not add those PNG/AI/EPS/SCML sources or
repackaged source atlases to git. This update uses their animation and
silhouette *ideas only* and includes **no CraftPix graphics**.
See https://craftpix.net/file-licenses/ (Freebie products section).

**Implemented original game changes:**
- Ranger (crossbow/repeater): evergreen hood, quiver, leather breast strap.
- Vanguard (greatsword/sabre): heavy T-visor helmet, steel crests, crimson
  mantle, shoulder spikes.
- Arcanist (staff/frost lance): purple cowl, split ceremonial crown, arcane
  crystals and ornamental glowing collar.
- All three styles retain the original WardenIK body, targeted weapon pose,
  purchased armor geometry, thirty earned weapon silhouettes and combat state.
- Ten-step seven-action clip clocks now drive character secondary gear motion
  without quantizing hit detection or input processing.
- Four physical gear loadouts per existing enemy species: normal, raider,
  armored and elite/royal. Armor and headgear change geometry, not only hue.
- Keep all ten biome themes and boss-phase anatomy.
- `WardenStylesV060Test` verifies three Warden rendered identities,
  all 7 x 4 enemy appearance combinations, animation clip boundaries, and
  pixel differences in live Canvas renders.

**Not claimed:** This is a first *sprite-sheet-inspired* pass based on
original rig illustrations, **not** a bundled 210-frame image-sprite system.
Original painted art/animation authoring and a genuinely textured
asset pipeline remain the next visual-quality milestone.
