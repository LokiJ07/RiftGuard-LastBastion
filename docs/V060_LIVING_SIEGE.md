# RiftGuard v0.60.0 — Living Siege implementation plan and checkpoint

**Current candidate**: `0.60.0-dev1`, not yet a finished v0.60.0 release.

## Integrated in this first checkpoint

- New `LivingRigArt` creates an articulated Warden with separately animated legs, cape, torso, gauntlets, helmet, shield and moving weapon hand.
- Weapon in that hand is rendered by existing `EvolvedWeaponArt` with the **purchased form** from `EvolutionProgress.weaponForm` (six families x five tiers). Attack movement uses the actual `WeaponRig.aimedPose`, preserving combat/muzzle coordinate logic.
- All seven current enemy species have distinct vector silhouettes, limb/wings/quadraped gait, per-species weapons and attack poses, size variants, biome-specific colors for all ten worlds, real facing and hit flash.
- Runtime `RiftGameView` integration preserves enemy HP, attack timers, boss logic, contact/muzzle rules, tower pad positions, old save data, upgrade trees, paid arsenal, and stage progression.
- Original `MotionArt` + `CharacterArt` remain fallbacks; `USE_LIVING_RIG` can be flipped for regression comparison.
- Enemy variant and facing are carried into their death echoes.
- Robolectric visual tests exercise 30 actual held-weapon forms, seven enemy species and ten realm palettes, producing images in the CI screenshot gallery.
- Android versionCode increments from 47 to 48, using versionName `0.60.0-dev1` to avoid implying final release.

## Design provenance

The user supplied `WardenRig.java` and `EnemyRig.java` as references. This implementation adapts their articulated-body and biome-variety ideas into a smaller renderer rather than copying their older `RiftGameView.java`, which would revert v0.40/v0.51 gameplay, UI and progression work.

## Further required work for v0.60.0

1. Character detail pass: better layered armor shading, cape simulation, directional idle/walk, dodge/hurt/death readability, individually selected Mastery/Ascension armor surfaces.
2. Enemy variety pass: biome-exclusive anatomical variants, elite silhouettes, bosses with specific skeletons and telegraphed attack phases rather than palette-only changes.
3. Combat feel: animation event markers, anticipation/contact/recovery for all six weapons, readable combat FX without overwhelming HUD.
4. Realm environment and fortress machinery integration; retain all current rank/arsenal purchases and saves.
5. Performance: compare frame times and memory against v0.51 on Android emulator, reduce per-frame primitive count or bake static layers as needed, then test actual midrange phone.
6. QA: no GitHub merge until CI build, Robolectric, screenshot review and emulator interaction are verified.

## Known limits

- Procedural Canvas shapes still require better authored/illustrated detailing to reach the target premium 2D/2.5D style.
- The seven enemy families are enhanced but the proposed 20–30 truly independent enemy archetypes are **not implemented** here.
- No physical handset performance guarantee is made.
