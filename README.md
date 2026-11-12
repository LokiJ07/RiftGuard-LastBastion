# RIFTGUARD: Last Bastion

Mobile-first, fully offline Android fortress-defense action game.

## Current version: V0.12.0 — bottom-to-top ascension tree and true four-mode difficulty
- Playable 100-level × 3-stage campaign structure with deterministic battleground layouts.
- **Real bundled art**: first original AI-generated paintings are packed into a 1024×768 WebP texture atlas under `res/drawable-nodpi`, decoded offline and drawn in Home, Campaign, Workshop and stage thumbnails.
- **Production environment layer**: 60 independently rendered transparent prop sprites (10 regions × 6 types), world-space depth sorting, near-hero occlusion fading, gentle vegetation motion and closer gameplay camera. No fixed screenshot overlays in the battlefield.\n- **State-driven animation**: original artwork reworked into 4×4 Warden pose frames (idle/run/attack/cast); 7 enemy groups with 4 locomotion and 4 attack frames each. Frames change based on actual movement and combat states, not static image bobbing.
- **Weapon and projectile synchronization**: six transparent in-hand Canvas weapon models anchored to animated wrist grips, with target-dependent aim, true muzzle spawning, weapon-tip slash trails and directional hit fragments. The original painted inventory icon atlas remains for Armory; the source hero sprite still has baked-in painted equipment that requires a clean future atlas.
- **Textured living battleground**: ten biome palettes and four illustrated texture variations per biome replace the plain solid-color floor. Atlas is assembled into one cached scene texture per mission for Android performance.
- **Living world map**: ten clickable region nodes and three A/B/C mission previews over the illustrated world. Progression remains controlled in code, not baked into painting.
- **New permanent fortress upgrade**: five ranks, supplies earned on mission wins, castle appearance/core HP/starting towers progress, saved after reopening the app.
- **New Armory**: six visible weapon designs with unlock rules, equip selection, persistent loadout and distinct attack stats/effects (crossbow, greatsword, fire sabre, arc staff, siege repeater, frost lance).
- Upgradable turrets, touch joystick, hold-to-attack, Arc Pulse, monster classes, win/lose, pause/resume, responsive HUD.
- Android CI builds, JVM rules tests and Robolectric screen/gameplay tests, debug APK uploaded as Actions artifact.

## Important visual scope
This build combines **original generative painting assets** in interactive screens with original procedural vector gameplay artwork. The game does **not** yet include hand-painted high-resolution sprite atlases, animated skeleton rigs, 1,000+ imported art assets, or 300 artist-authored unique map scenes. Those are planned V3–V5 improvements. See [production pack](docs/CONTENT_INTEGRATION_V1.md).

## Optional downloadable resources

The game works **offline immediately**, because all current essential game resources remain bundled. The new **Home → Resources** screen lets players download/verify the original art pack from GitHub over HTTPS. The app stores checked assets in private storage; on the next app launch it uses those assets offline. There are **no ads, account requirements, tracking SDKs, or forced network checks**.

The current initial downloadable pack contains the same art that ships with the APK: it demonstrates secure downloading and offline caching. It **does not yet introduce new ultra-HD artwork**. Future independently generated and optimized layers can be shipped by updating `game-content/manifest.json`. See [resource pack guide](docs/RESOURCE_PACKS.md).

## V0.10.0 — storybook briefing and hero art cleanup

- Before **every mission deployed from the campaign atlas**, the player sees a distinct illustrated chronicle panel with biome lore, A/B/C mission orders, wave goals, and boss warnings. An explicit **BEGIN BATTLE** button starts the actual simulation; combat input and waves are frozen during the story.
- The Warden's upper body uses a composited armor source that masks off the old painted lower-right sidearm area while keeping its detailed face, chest, pauldron and cape. The right arm/weapon is held by an independently animated attachment. **This does not replace the hero with a fully new equipment-free source sprite atlas.**
- Blade swings now rotate toward the actual enemy's direction. Greatsword and sabre hits occur **at mid-swing**, applying damage once in a forward attack cone, not immediately in a 360-degree radius around the player.
- Full offline story logic and actual Robolectric View/touch tests, weapon/body render tests, melee-hit tests and screen-gallery captures are part of Android CI.
- Roadmap and limitations: [V0.10 QA notes](docs/V010_MISSION_STORY_COMBAT_QA.md).

## V0.11.0 — abilities, offline audio and more reactive battles
- **Six meaningful and equipable combat skills** with permanent unlock requirements: Arc Pulse (rank 1), Ember Cleave (rank 2), Frost Nova and Healing Banner (rank 3), Spike Trap (rank 4), Meteor Strike (rank 5). Every one has real simulation effects such as damage, burn damage-over-time, enemy slow, hero/core heal, persistent trap detonation and delayed area impact.
- **Skill loadouts:** Home → Armory → EQUIP COMBAT ABILITIES. Tap an unlocked ability to equip it, then use the existing battle skill button. Equipped skills save offline. Cooldowns and the capacitor upgrade apply to all skills.
- **Original offline sound effects:** real waveforms generated inside the app for shots, swords, spellcasting, impacts, buildings, victory and damage. Switch SOUND ON/OFF in Home; no sound asset downloads, advertisements or permissions.
- **New enemy behavior:** boars warn before charging at higher speed; shaman magic projectiles travel through the world to a targetable position and are dodgeable. The flying imp now uses actual illustrated actor atlas frames with physically flapping membrane wings instead of the old purple placeholder enemy.
- **Fortress responds to damage:** wood/stone structures progressively gain cracks, planks, smoke and embers as core health falls; repairs through healing remove damage visuals.
- **Better visual verification:** automated screenshots of all six skill attacks, equipped-skill menu and damaged fortress. Android tests cover real gameplay state and generated WAV data.
- **Not claimed yet:** all-new weapon-free high-definition hero art, full 300 artist-authored scenes, production soundtrack, additional original boss sprite families, device performance/final polish. See [v0.11 QA notes](docs/V011_ABILITY_AUDIO_ENEMY_QA.md).

## V0.12.0 — visually linked upgrades and real difficulty scaling

- **Bottom-to-top Ascension Tree** directly accessible from **Home → Fortress → ASCENSION / UPGRADE TREE**. A large 40-node graph spans ten tiers, beginning at the free Origin and leading up to LAST BASTION. Nodes have original weapon/tower/hero/spell icons, glowing animated connectors and status colors (purchased, available, locked).
- Swipe **up** to climb toward stronger upgrades; swipe down to return to the root, or tap GO UP. Select a node to view its name, benefits, parent/fortress gate and permanent supply purchase. Purchases survive app restarts.
- **Every paid node has a combat effect**: direct Warden damage, turret damage, core/hero max health, starting battle gold, spell cooldown or movement speed. All upgrades apply to the actual game loop, not just decorative icons. The five-stage fortress path and all six existing weapons are retained.
- **Rebalanced Easy / Normal / Hard / Expert**: at the same stage Expert has **3.85× Normal enemy health, 2.95× damage**, +5 waves, +11 enemies/wave, ~2.17× faster spawns, recurring elites every other wave and fewer starting defenses. Hard also adds elites and faster, stronger waves. Easy grants much more breathing room and supplies. Higher risks grant larger victory supply bonuses.
- The campaign difficulty UI now shows the selected HP/damage and wave/reward differences.
- **Regression-tested:** tree graph connectivity, unlocking, persistent benefits, swipe navigation, strong mode separation and real gameplay changes. See [v0.12 audit and table](docs/V012_ASCENSION_DIFFICULTY_QA.md).
- **Remaining:** fully authored HD sprite animations, sophisticated unique map art, physical phone FPS evaluation, and human-playtested balance. Android CI cannot guarantee the new Expert mode is fair across all 300 stages.

## Playing
1. Home → CAMPAIGN → choose a region, stage A/B/C (only unlocked stages are playable).
2. Move using left thumb, hold ATTACK to use equipped weapon, use PULSE and BUILD to defend.
3. Win to earn permanent supplies. Home → FORTRESS → upgrade rank to unlock weapons and abilities.
4. In FORTRESS tap ASCENSION / UPGRADE TREE; swipe up from the Origin and buy connected combat upgrades using supplies.
5. Home → ARMORY → tap an unlocked weapon to equip it; choose EQUIP COMBAT ABILITIES to set a distinct skill.
6. On the Home screen tap SOUND ON/OFF to control the locally generated game sounds.

## Build
Android Gradle Plugin 8.7.3, Gradle 8.10.2, JDK 17, Android SDK 35. On GitHub see Actions → Android CI → successful run → Artifacts → `RiftGuard-debug-apk`.

Debug CI builds are not a release-signed APK. Reinstall conflicts may occur across different debug signing keys. Do not use debug keys for production distribution.

## Development roadmap
[Full production plan](docs/PRODUCTION_PLAN.md) · [Asset and unlock pack](docs/CONTENT_INTEGRATION_V1.md)

## V0.6.0 combat and progression update
- Map selection has **Easy / Normal / Hard / Expert** difficulty controls persisted between sessions. Hard and Expert have more waves and larger enemy rosters, stronger/faster enemies, more damage, and higher supply rewards.
- Map C includes final-wave elite enemies, with 10 named world bosses at Level 10-C through Level 100-C.
- Four independently animated mounted defenses: Ballista, Siege Cannon (splash), Frost Battery (slow), Arc Coil (chain), each with distinct muzzle effects and combat stats.
- Individual level identity from deterministic watchtowers, camps, ruins, crystal shrines, windmills, paths and animated environmental effects. This is **procedural stage variation**, not 300 hand-painted maps.
- Robolectric UI screens and battle simulations test actual difficulty selection, boss stats, shots, tower slow effects.
- CI now includes a nightly `schedule` in addition to commits/PR runs. **It reports failures in GitHub Actions; it does not auto-fix or send ChatGPT messages**.

## V0.6.1 — combat UX and ultrawide correction
- World boss health and name appear on a live HUD bar, with animated charge-up warning.
- Boss shockwave has actual delayed damage to hero/core (tested), not a cosmetic alert.
- Battle lower control strip has translucent gradient to prevent scenery overpowering touch controls.
- Non-16:9 devices now show palette-matched gutters instead of black pillars, preserving touch hitboxes.
- Robolectric screenshots cover ultrawide layouts and actual boss combat.

## V0.6.2 — regional progression route
- World Atlas retains its 10 selectable kingdoms. Entering a realm now opens a **separate 10-location map** with paths, animated markers, distinct settlement silhouettes, boss destination and unlock/clear status.
- Tap a level location to select it, then choose Map A/B/C and difficulty before deploying.
- **World** button returns to kingdom overview; all 100 level IDs and 300 stage unlocks remain unchanged.
- Real-time layered illustration and independent hitboxes (not a static screenshot with numbers).
- Screenshot regression checks now capture both the world overview and the regional journey.
- This remains procedural 2D environmental artwork, not the target fully authored high-fidelity landscape.

## V0.6.3 — textured regional environments
- Region maps now render independent transparent **biome-specific ground props** (the original 60-asset atlas) behind selectable ten-stop routes, landmarks and dynamic progress markers.
- Path hitboxes remain separate interactive data; images are never used as unclickable UI screenshots.
- Still procedurally laid-out content, not 100 hand-painted regional locations or HD cinematics.

## V0.7.0 — actual Kingdom Hub systems
Open Home → **Kingdom Hub**. All five features are real game systems, not static painted UI:
- **Daily Rewards:** Claim once per UTC day, with consecutive-day streak rewards up to day 7; cached offline.
- **Daily Quests:** Kill enemies, win battles and cast Pulse to complete three repeatable objectives, claimable only once per day.
- **Weekly Events:** Offline weekly rotating challenges (monster invasion, win streak, boss kill) with a one-time reward.
- **Achievements:** Lifetime battle statistics with six permanent milestones and individually claimed supply rewards.
- **Shop:** Four permanent, rankable supply upgrades that change battle HP, core HP, tower damage and pulse cooldown.

Progress is persisted after real battle results in the same campaign save. No ads, account, online verification, payment systems or multiplayer. Rewards require explicit claims. Automated tests verify restarts, duplicate reward prevention, rollovers, battle stats, upgrade effects and actual Android UI screen rendering. Local clock adjustments cannot be fully prevented in an offline-only game.

**Not yet complete:** AAA-style character locomotion/rigging, 300 handcrafted environments, detailed animated castle stages, real high-resolution downloadable content, new game icon and native aspect-responsive UI.

### V0.7.1 UI/verification patch
Kingdom Hub status message moved out of content/footer areas. Shop inventory purchase-to-battle stats now covered by real UI interaction tests. Weekly event period is monotonic when the phone clock is rolled backwards.

## V0.8.0 — real environmental challenge
New `StageHazards` positions 1–4 animated objects along the actual route per stage. Every world has its own hazard style. Brambles, swamps, frost and shadow effects slow; other hazards deal genuine player and monster damage. Effects trigger only during an animated active phase with visible warning, and stop automatically outside their zone. Map A has fewer hazards than tactical Map B, and boss sieges have additional hazards. These interactions are tied to the live combat simulation and are tested across all 300 stages. Still lacks handcrafted unique map artwork and natural skeletal animation.

## V0.8.1 — mobile aspect-ratio controls
On 18:9, 19.5:9 and 21:9 displays, the battle joystick, BUILD, PULSE and ATTACK controls shift toward the actual device edges while keeping their real touch hitboxes aligned. The combat and character art are not stretched. 16:9 devices retain their original touch layout. Test coverage exercises all four controls on a 1780×720 virtual device. More adaptive native layout work is still needed for tall and foldable devices.

## V0.8.2 — fortress and equipment improvement
- Keep and turret render order fixed: upper turret guns no longer disappear behind the castle artwork.
- All four weapon slots have visual masonry platforms joining the fortress rather than floating on the outer corners.
- Rank 3 Stone Keep, Rank 4 War Stronghold (side bastions, flags, causeway) and Rank 5 Arc Citadel (crystal spires, animated magical barrier) now have distinctly different silhouettes. Ranks 1–2 retain wooden outpost/reinforced camp artwork.
- The six hero weapons use separate animated wrist sockets, a shoulder-elbow-hand chain and a gauntlet drawn over the grip; attack animations move the wrist instead of simply turning a sticker.
- Robolectric screenshots include 3 castle upgrade ranks and six equipped weapon modes. Tests cover image differences, mounted supports and seven viewport sizes.

Important honesty: the original hero sprite atlas has some painted equipment baked into character frames. This update improves anatomical weapon attachment but **does not complete a skeletal rig or truly weapon-free hero animation set**. Similarly, viewport tests are not universal physical-device certification.

## V0.8.3 camera and visibility fix
Real rendered screenshot inspection exposed a cropped fortress on the left. The camera is now recentered on the castle–hero lane, with the keep fully inside the battle viewport and the four mounting weapons slightly reduced to preserve visibility. A regression test asserts the castle bounds within 16:9 gameplay.

## V0.8.4 visible turret tier stages
Every turret family visibly changes across tiers 1 to 5 with reinforcing armor, auxiliary barrels, active energy rings and an upgraded crown. Screenshot gallery compares tier 1/3/5 guns in actual gameplay. The weapons and fortress remain native Canvas illustrated assets, not finished AAA-quality 3D skeletal models.

## V0.9.0 — actual articulated motion and real-time combat VFX
- A new **ActorGait** system animates independently moving left/right legs, knees and grounded footfalls. Detailed original illustrated upper torsos are clipped from their sprite sheets; motion is applied to leg bones instead of vertically shaking the whole photo. The cape moves independently.
- Hero weapon sockets now counter-swing naturally with the running stride; attack-specific wrist pivots and gauntlet grips remain active for all six equipment types.
- Ground enemies have staggered left/right leg motion. Flying enemies preserve their fluttering wing sprite animation.
- Real-time projectiles have meaningful ballista arrows, cannon shells/smoke, ice shards and electrical lightning instead of nearly identical circles.
- Defeated monsters now show a short fall/fade visual and linger briefly as a timed effect; kill rewards are counted once on enemy removal.
- Separate impact effects linger and expand from real projectile damage positions.
- Robolectric tests verify gait math, weapon wrists, different projectile renderings, and monster death lifetime. Gallery captures two separate hero run poses.

**Art-quality limitation:** this uses procedural bones combined with the original painted character torsos; it does not replace the underlying image atlas with a full professionally authored set of weapon-free, multi-directional skeletal animation sprites. Long-session performance, physical touch feel and all 300 late-game stages remain to be playtested on devices.

## V0.9.2 final animation-art correction
In-game screenshot inspection found that the first procedural legs used flat colored boots and did not match the detailed original hero artwork. They were replaced by independently animated transparent leg and cape **cutouts from the existing painted atlas**. The giant's original fire platform was accidentally attached to its foot cutouts; types 3 (winged), 5 (giant boss), and 6 (four-legged beast) now retain their intact illustrated animation until proper model-specific rigs are available. Screenshot tests cover two Warden strides and the real boss stage. The artwork is still not an authored full skeletal animation set.

## V0.9.3 screenshot-driven cleanup
Full-size screenshot inspection found the new cutout greaves swinging too wide and exposing a hard waist seam. This release reduces step spread and foot lift, limits boot rotation, and adds a moving painted central tabard over the hip joins. Original boss art remains intact. These changes are validated through actual run-frame captures and stride bounds, but the game still needs proper authored skeletal sprite sheets before its animations can be called production-quality.

## V0.9.4 — physically connected combat presentation
- Ranged weapon aim now rotates its actual held silhouette toward the target in the same coordinate transform used to spawn damage projectiles; left-facing characters mirror correctly.
- Weapon-specific tips determine bolt/energy launch positions and muzzle spark origins. No projectiles spawning from the hero's abdomen.
- Sword and sabre swipes follow the moving blade tip; impact VFX use directional fragments instead of a generic expanding ring.
- Opaque/colored inventory-card art is no longer mounted in the hero's hand. Separate transparent Canvas equipment silhouettes are used until production-grade transparent weapon sprites are available.
- Added real in-game projectile-origin tests for four weapon classes on both facing directions, target-angle tests, and a pixel-based no-badge visual regression test.
- **Still not done:** the original hero sprite includes some painted equipment; production-quality weapon-free hero and enemy atlases, advanced 2.5D maps, audio, and physical phone playtests remain outstanding. See [requirements audit](docs/REQUIREMENTS_AUDIT_2026-11-12.md).
