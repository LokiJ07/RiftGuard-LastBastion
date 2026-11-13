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
