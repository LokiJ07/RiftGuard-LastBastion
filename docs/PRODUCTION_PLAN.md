# RIFTGUARD: Last Bastion — Production Plan

## Design pillars
1. **Active defense**: movement, combat, hero abilities, targeting, tower deployment.
2. **Fortress identity**: visible repairs, damage, wall tiers, garrisons, NPC workers.
3. **Map progression**: 100 levels × 3 maps = 300 campaign stages; 10 biomes.
4. **Varied siege encounters**: enemy families, flanking, elite enemies, boss phases.
5. **Mobile performance**: 60 fps target on midrange Android hardware, scalable effects, offline saves.

## Current prototype (V0.1)
- Offline landscape Android Canvas renderer, original vector placeholder art.
- Touch joystick, hold-to-fire, area pulse, turret building/upgrading.
- Hero HP, fortress core HP, waves, enemy types and enemy targeting.
- 100-level/300-stage *logical campaign* with deterministic terrain variation, local unlock saving.
- Home screen, map select, victory/defeat, pause, Android CI debug APK.
- JUnit checks for campaign progression and battle formulas.

**Clarification:** All 300 stages are indexable and playable using a procedural layout system, but they are not yet 300 individually art-directed maps. The V5 art and animation goals are not claimed to be implemented.

## V1
- Polish full combat loop; collision and steering, touchscreen usability.
- First 9 signature hand-designed stages with biome tilesets.
- Enemy telegraphs, boss attacks, combat sound, UI and save recovery.

## V2
- 45 signature stages, hero classes, weapons + silhouettes, loot economy.
- Destructible walls, building construction, attack animations, NPC workers.

## V3
- 120 signature stages, dungeons, optional exploration, events, elite waves.
- Improved enemy steering and pathfinding, dynamic weather, chapter quests.

## V4
- 210 signature stages, companion characters, stories and major bosses.
- Full skill and weapon-evolution systems and advanced map interactables.

## V5
- All 300 curated map experiences, 10 detailed regions, final campaign story.
- Approximately 1,250 approved visual assets/variants, character animation atlases,
  optimized audio/VFX, accessibility, difficulty balancing and regression tests.

## Proposed asset allocation (target, not yet produced)
- 240 monsters and variants
- 240 terrain/prop visuals
- 180 weapon variants
- 150 base structure and upgrade visuals
- 140 effects
- 120 decorative assets
- 100 UI graphics
- 80 heroes, allies and NPCs

## Validation standard
- Green CI is a build gate, not proof of gameplay quality.
- Test on physical Android devices for frame rate, touch controls, crashes,
  orientation, tutorial clarity, save/resume, and campaign progression.
