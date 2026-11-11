# RiftGuard — Production Pack V1

## Current deliverable and limitations
This production pass introduces a genuine working unlock–equip–fight progression loop, persistent fortress ranks, illustrated **vector-drawn** region cards and distinct stage thumbnails, permanent supply rewards, and a different visible weapon effect for each weapon. These visuals are procedurally drawn original placeholders, **not generated high-resolution sprites or 300 hand-crafted illustrations**.

## Upgrade/unlock economy
| Rank | Fortress | Supply cost | New weapon |
|---|---|---:|---|
| 1 | Woodland Outpost | Starting | Warden Crossbow + Iron Greatsword |
| 2 | Reinforced Camp | 120 | Ember Sabre |
| 3 | Stone Keep | 300 | Arc Staff |
| 4 | War Stronghold | 650 | Siege Repeater |
| 5 | Arc Citadel | 1,100 | Frost Lance |

First clear supplies: 120 + 10 × biome index + 15 × map variant. Replay: 45. 
Rewards persist between missions, while battle gold purchases/upgrades towers during a mission.

## First playable vertical slice — Level 1A/1B/1C
- A: scout outpost — forest and woodland tracks
- B: broken pass — split river crossing and dangerous approaches
- C: last stand — fortified keep, stronger boss wave
- All three stages unlock sequentially and present different mission pictures; generated battle terrain continues to be improved.

## Content manifest target for V1–V5
- Heroes: Warden Knight plus 3 distinct unlockable classes, each idle/run/attack/cast/hurt animations
- Enemies: 6 core behaviors × 10 visual families + bosses
- Weapons: 6 playable launch weapons, expand to 25+ with separate in-hand and projectile art
- Towers: crossbow, flame, frost, bombard and lightning types with unique tiers
- Fortress: five ranks with visibly growing construction and defense detail
- Abilities: Arc Pulse, Fire Slash, Frost Nova, Healing Banner, Trap Drop, Meteor Strike
- Rewards: supply pouches, health, mana and crafting materials
- Scenes: 10 biomes × authored campaigns; 100 levels × 3 unique mission layouts
- Inventory, crafting, NPC workers, sound, narrative and cutscenes

## Art generation prompt collection (asset-ready spec)
All prompts demand **original artwork** with top-down / three-quarter isometric view, no lettering, transparent backdrop on characters and objects, coherent lighting and orientation, and readable silhouette at 96–192px.

**Warden Knight:** Original blue steel fantasy ranger/knight, leather cape, gold crescent shield, six directions of walking, idle, strike, cast and hurt frames, three-quarter view, painted hand-authored shading.

**Enemy base set:** Goblin runner, forest brute, feral wolf, flying imp, siege beast and spirit shaman. Every creature distinct anatomy, attack pose, idle/motion/impact/death poses, painterly top-down fantasy game art.

**Weapon set:** Iron Greatsword, Warden Crossbow, Ember Sabre, Arc Staff, Siege Repeater and Frost Lance; show equipment hero pose, inventory icon and colored projectile trail; separate transparent images.

**Fortress set:** five increasingly large fortress stages, consistent 3/4 view and footprint, barracks, gates, towers, scaffolds, upgraded stonework, decorated flags, weathered materials, day and night variants.

**Stage thumbnails:** Greenhaven Scout Outpost, Broken Pass and Last Stand, each painterly distinct landscape featuring path structure and defensible keep; all 10 regions require recognizable vegetation, climate and architecture.

## Acceptance criteria for each release
1. Test every unlock and equip: new weapon must produce different DPS/cooldown/range and projectile or swing visuals in live combat.
2. Stage selection shows a picture for its actual biome and mission type, not bare numbers.
3. Fortress upgrade permanently increases core capacity and changes construction drawing.
4. Save/load, pause/resume, touches, enemy collision and victory progression pass automated tests.
5. Physical Android playtests validate animations, performance, visibility, controls and actual fun.

**CI passing does not certify artwork quality, complete V5 content, or device playtesting.**
