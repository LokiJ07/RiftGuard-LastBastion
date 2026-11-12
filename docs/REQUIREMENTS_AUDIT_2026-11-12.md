# RiftGuard: Last Bastion — requirements audit (2026-11-12)

> Evidence base: GitHub main v0.9.3 (commit 25e08e8, green Android CI #59); v0.9.4 combat correction is being validated in PR #1. A green CI verifies build/tests, not artistic realism, fun or performance on a physical phone.

| Requirement | Status | Evidence / gap |
|---|---|---|
| Offline Android fortress-defense | Implemented foundation | Java Canvas game loop; CI debug APK, no mandatory network |
| 100 levels × A/B/C (300 stage IDs) | Implemented mechanically | Campaign, RegionJourney and stage progression logic; not 300 authored landscapes |
| 10 explorable worlds with actual rich region pictures | Partial | Linked world/region map screens, 60 biome prop sprites, procedural mission previews; backgrounds still noticeably reused |
| Full high-fidelity 2.5D maps with unique interiors/dungeons | Not met | Procedural paths and props; no complete authored interiors/dungeons or dungeon puzzle progression |
| Story pop-ups/cutscenes and mission narrative | Not met | Simple boss notices and mission labels, not a narrative scene system |
| Detailed player and enemy characters | Partial | Painted torso atlas + cutout legs, 7 enemy groups; not complete rigged, direction-specific sprite sets |
| Hero has no built-in weapon artwork | Not met | Original hero atlas visibly contains painted equipment; must replace with authentic weapon-free body and separate arm layers |
| Six visibly distinct unlockable/equippable weapons | Implemented foundation | Progression: crossbow, greatsword, ember sabre, arc staff, repeater, frost lance; persistent armory |
| Weapon projectile and slash from actual tip | In PR #1 | World-space muzzle calculation, target-angle pose, edge-following slash and sparks; awaiting final CI/device review |
| High-quality separate transparent weapon game assets | Partial | In-hand transparent Canvas weapon geometry replaces unsuitable opaque inventory badges; need authored cutout PNG/WebP art |
| Five fortress upgrades and animated mounts | Partial | Permanent ranks, core HP changes, 4 functional towers and 5 visual turret levels; handcrafted construction/worker animations missing |
| Varied monsters/bosses, intelligent attacks | Partial | Timed waves, 7 behaviors, world-boss names and shockwave with damage; more unique attack animation and AI needed |
| Responsive touch controls and gameplay menus | Partial | Joystick/buttons, HUD viewports, screen tests; physical device/safe-area and foldable verification missing |
| Challenge systems, unlockable upgrades and replays | Implemented foundation | Four difficulties, hazard zones, kingdom hub shop/rewards/events/achievements |
| Offline download art pack that genuinely improves visuals | Not met | Integrity-checked opt-in pack exists but initially mirrors bundled assets |
| Music, layered audio and SFX | Not met | No implemented production-quality sound system |
| Playtested 300 stages on actual Android hardware | Not met | Robolectric and unit tests do not equal exhaustive human playtesting |

## Next acceptance gates
1. **Weapon-free hero atlas:** idle, walk, cast, attack, hurt, death; transparent correctly separated body, arms and equipment. No duplicate blade when changing between the six weapons.
2. **Art-grade held weapons:** six clean transparent silhouettes (no card backgrounds), grip/weapon-tip calibration, visible recoil/swing. Muzzle, flash, projectile trajectory and impact originate from same pose and target.
3. **Battle world polish:** regions are geographically distinct in real gameplay; stage changes are more than recoloring and procedural prop shuffling; route, buildings and hero movement remain physically coherent.
4. **Fortress evolution:** visible construction and damage/repair, four weapon mounts integrated in masonry, real upgrade appearance at every rank.
5. **Mobile verification:** repeat test suite + Android APK, screenshots at representative aspect ratios, physical phone smoke tests for controls, FPS, texture artifacts, installs and save progress.
6. **Asset packs:** genuinely new, appropriately licensed or original high-resolution transparent sprites and scenes; memory budgets and offline integrity tests.

## Known risks
- Current base artwork is still composited 2D, not true 3D or a full skeletal character rig.
- Transparent in-hand Canvas weapon drawings improve the badge artifact but are not production-grade illustrated sprites.
- Screenshot galleries are visual QA aids; screenshots alone cannot certify physical device input, performance or fun.
- Do not call this release complete until all game visuals and physical-device playtests pass.
