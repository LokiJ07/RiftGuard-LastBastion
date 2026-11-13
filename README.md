# RIFTGUARD: Last Bastion

## v0.60.0-dev3 — Living Warden & Enemy Style Variants (tested development build)

- **Three distinct Warden combat appearances:** equipped crossbow/repeater = hooded Ranger, greatsword/sabre = armored Vanguard, staff/frost lance = crystal-crowned Arcanist. Distinct cloaks, helmets, shoulder geometry, gear and rune details render **on the real controllable Warden** in Home and battles.
- **Seven enemy species × four real equipment loadouts:** normal, raider, forged and elite; each has biome-themed materials, physical armor/helm/horn/wing/boar-harness geometry, and actual boss-phase anatomy. Draw calls use existing movement/attack/HP/death state, not detached cosmetics.
- **Animated actor foundation:** maintains the original 713-line Warden IK and 1,054-line EnemyRig reference geometry, then adds different styles and action-sensitive equipment movement. The 7-action/10-frame timing reference supports Idle, Walk, Run, Jump/Dodge, Attack, Hurt and Die. Real hit detection remains governed by existing WeaponRig and gameplay, not frame interpolation guesses.
- **Equipment and save continuity:** six families × five mastered weapon forms, five fortress ranks and four ascension arsenal unlocks retain their own existing saved rules; Android versionCode advances for side-loading updates. Original MotionArt remains a safe fallback.
- **Regressions:** tests render all 3 styles and 28 enemy loadouts, 30 equipped weapon forms, 5 armor ranks, 10 realm palettes and boss phases. CI build, Android emulator input/graphics QA and screenshot review are release gates. No claims of physical-phone FPS certification.
- **Important asset/license boundary:** The user's CraftPix animation pack provided *design inspiration only* (three knight designs, 7 actions x 10 frames); no CraftPix art/source files are committed to this public repo because its license forbids redistributing reusable source artwork. These are original Java Canvas actor designs, **not** a complete new high-resolution PNG spritesheet library. A later original illustrated sprite atlas remains planned; see `docs/V060_LIVING_SIEGE.md`.

## v0.51.0-rc1 — Dramatic Fortress and Weapon Identity (under test)

This update strengthens the existing saved progression without adding fake cosmetic items:
- **Five substantially different actual fortresses:** starter timber palisade, reinforced stone garrison, rune stronghold, broad royal bastion and towering arc citadel. The architecture occupies the real battle/Workshop/Home fort coordinates; all tower pads and enemy path physics remain unchanged.
- **Five prominent arsenal models:** unowned has no phantom tower; purchases unlock visible Ironbolt Battery (reinforced magazine crossbows), Ember Mortar (twin forged blast barrels), Storm Conductor (three-prong induction structure) and Guardian Prism (massive crystalline optical weapon). These supplement the already functional `FortressArmamentRules` projectile/combat system, not a new decoration-only currency.
- **All six weapon trees gain more exaggerated physical models:** wider sword guards, jagged elite blades, compound bow cams/magazines, runic staff prongs and crystal spear heads. They are attached inside the same held-weapon transform during live combat.
- **Workshop before/after:** current owned rank and next fortress/arsenal appearance shown side by side, plus a separate **PREVIEW NEXT** toggle on the live workshop scene. It does not change stats, rank, supply balance or save data.
- **Upgrade reveals:** purchasing fortress ranks, mastery nodes or ascension nodes displays a brief, labeled upgrade banner with an original offline confirmation sting. Both actual bought-node visuals and unlock requirements still derive from existing save keys; no automatic purchase in preview.
- **Release gates:** Android Java compile + JUnit/Robolectric rendering and interaction tests, visual screenshots of five fort ranks, four distinct purchased arsenal machines, all 30 weapon forms and actual workshop touch sequences. Emulator gameplay QA and real-device profiling remain separate.
- This is a release candidate, not a final physical-phone certification.

## v0.50.0-rc1 — Upgrade Evolution (development candidate)

- **Six weapons, thirty distinct visual forms:** each of the six existing melee/ranged/magic families has 5 distinct native transparent models. Every form derives from the highest **purchased** Weapon Mastery tier, not a clicked preview. The Armory, Weapon Mastery tabs, before/after node preview, battle HUD and Warden's **animated wrist-mounted equipped weapon** all share it.
- **Persistent, existing saves:** forms read original `weapon.mastery.<id>.v1` node ownership and `ascension.purchased.v1`, without new cosmetic flags or migration. Reopening the game keeps appearances intact.
- **Fortress weapons gained through Ascension TOWER purchases:** five arsenal configurations appear on the actual fortress in Home, Workshop and Battle: Timber Ballista, Ironbolt Battery, Ember Mortar, Storm Conductor and Guardian Prism. The built siege mounts and structural components gain distinct shapes; the ascension tech prerequisites plus fortress rank determine which is earned.
- **Real combat for fortress weapons:** arcane armament buys increase tower damage slightly; Ember Mortar adds burn, Storm Conductor chains to nearby foes, and Guardian Prism restores a small amount of core HP upon tower kills. Existing tower types, build pad geometry and the 300-stage campaign remain functional.
- **All permanent upgrade screens have visual consequences:** HERO ascension unlocks new Warden armor shapes, SKILL ascension adds live spell/ability effect forms and badges, TOWER ascension visibly changes castle artillery, ATTACK weapon upgrades transform the actual hand-held weapon. Buttons retain the same consistent bottom action geometry.
- **Automated visual proof:** `EvolutionUpgradeTest` checks real 30 weapon model differences, 5 fortress+hero+spell forms, saved purchase reload, real fortress-unlock requirements and an in-battle screenshot.
- **Limitations:** Native Canvas procedural models—not a new hand-painted sprite package. Physical-device thermal/FPS and battery testing require a connected handset. This is an RC candidate pending CI and Android emulator verification; not final production v0.50.


Mobile-first, fully offline Android fortress-defense action game.

## v0.40.0-rc2 — four-pillar polishing candidate (pending CI / emulator verification)

- **World detail:** `KingdomSetPieces` adds ten distinct architectural landmark families (Greenhaven timber gate, Hollow giant-tree shrine, swamp stilt sanctuary, volcanic forge, frozen temple, industrial watch, arcane crystal gate, tempest towers, shadow ossuary and eclipse obelisk). Seeded mission A/B/C variants render directly into the native cached battlefield. Light motes animate in real world positions without moving enemy/tower interaction coordinates.
- **Character/enemy motion:** all seven enemy species get independently anchored and timed weapon, wing, hooves and boss-phase embellishments over the original painted sprite animation; the Warden's *actual sprite* tilts and squashes during 210 ms dodge, not just a detached afterimage.
- **Audio/VFX:** 14 offline original synthesized audio events (including DODGE, BOSS_WARN, BOSS_PHASE and tower family cues, plus quiet ambience) with per-event debounce; timed live VFX for freezing, burning, boss telegraphs and Warden dodge direction.
- **Performance/QA:** `FinalPillarsV040Test` verifies painted pixel distinctions, audio signatures and draws a native frame-latency measurement explicitly labelled *Robolectric software Canvas only*. A second GitHub Actions job boots an Android API 29 emulator, installs the generated APK, navigates the menu, performs real touch inputs, collects PNG screenshots, Android meminfo and gfxinfo frame samples and flags crashes/ANRs. **Emulator metrics are NOT certified FPS or battery life for a physical phone.**
- **Release gate:** Do not call this production-ready until all tests and the emulator smoke job finish successfully and gallery images have been inspected. The user's own physical-device performance cannot be measured or controlled remotely; it remains a separately labelled acceptance check.

## v0.40.0-rc1 — Kingdoms at War gameplay candidate (under test)

This release candidate moves the v0.40 plan forward with **real playable code**, not a completed v0.40 claim:

- **5–8 stage-specific functional turret pads:** shared `TowerPadLayout` geometry is used by terrain artwork, construction selection, enemy targeting, tower collision, projectile graphics and save-safe stage reset. Mission A offers 5 (6 in later realms), B 6 (7 later) and C 7 (8 later) build sites. The original four castle-mounted gun positions remain. The additional pads are real ground turrets with four distinct attack families and five upgrade tiers.
- **Responsive new DODGE action** beside BUILD/ARC/ATTACK: directional 210 ms roll, 2.25 s cooldown, short invulnerable windows against elite, melee, spell bolts and terrain hazards, real movement against existing walls, animation state cancellation, visible afterimages and phone-aware HUD hitbox.
- **Boss combat has three real HP phases:** phase changes at 66%/33%; true enemy reinforcements join the existing live combat simulation; ten kingdom bosses have realm-specific seismic, renewal or rooting attacks, with varying charge durations, cooldowns and warning text.
- **Mission A/B/C side objectives:** build defenses / keep the fortress healthy / defeat an elite while protecting the hero. These are real optional extra-supply rewards and on-screen progress, never mandatory blockers for saved stage progression.
- Regression coverage: new `KingdomsAtWarTest` checks all 300 pad configurations, collision clearance, real turret purchases, dash physics, boss reinforcements, exact phase boundaries, animation cancellation and mission bonus requirements. Original saves remain in `riftguard_campaign_v1`.
- **Not yet complete** for the v0.40 target: full hand-illustrated environment packs, high-quality hero/enemy sprite overhaul, original boss animations, sound/music overhaul and physical-device performance certification. The build MUST pass GitHub CI and screenshot inspection before merging.

## Current stable version: V0.30.0 — LokiJ Identity and Consistent Upgrade UI

- **Real Android startup branding:** `LokiSplashView` presents the RIFTGUARD crest, `THE LAST BASTION`, and the creator credit **LokiJ** during actual game initialization. Indeterminate visual ornament is never a fake download percent, and no network is required.
- **New launcher artwork:** `res/drawable/ic_launcher.xml` is a genuinely bundled, scalable high-contrast shield with a rune blade, battlements, gilded trim and contrast-safe colors.
- **One consistent upgrade action bar:** Armory, Abilities, Fortress Workshop, Weapon Mastery and Ascension now share the SAME 4-button bottom-footer geometry (`UpgradeActionLayout`) with real, matching touch areas. Primary purchase/unlock actions always appear in the far-right slot. No phantom action in the details panel.
- **Actual world map and menus:** kingdom atlas now features a stronger readable frame, selected realm text that scales to its panel, visible PREVIOUS/NEXT level buttons, a clear BACK button, progress strip, and a tighter header. Buttons measure and shrink labels so long upgrade names no longer escape their frames.
- **Fortress art parity:** Home, Workshop and real battles show the SAME native 5-rank fortress family. Each tier has specific detail work: timber grains, mortar/stone rows, shingles, heraldic plaques, mounted buttresses, spire glass and damage cracks. No regression to the old workshop-only outpost.
- **Base weapon quality:** six held weapons have added mechanical details (crossbow rails/rivets/trigger, sword fuller/guard/pommel, staff/lance sockets and wrapped shafts). The combat weapons still use the current authoritative aim and hitbox logic.
- **Tests and screenshot proof:** `V030UpgradeIdentityTest` checks all four visible footer buttons and actual navigation/purchase, all five rank silhouettes, and the real native LokiJ splash. CI gallery additionally includes the rendered splash and app icon. The existing 300 stage IDs, save preferences, combat and map routes remain intact.
- **Known limits:** This is a major **UI, branding and fortress/weapon presentation release**, not 300 individually painted stages, a full new character rig or a physical Android device certification. Build/GUI tests and screenshot review are required before marking final. This build uses a development/debug signing identity.

## v0.21.0 — full Java HTML-style map replacement
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

## v0.21.0 — full native Java battle/map visual replacement

- The two attached user references, **deployed-map-generator.html** and **kingdom-atlas-kit.html**, now drive the real **Android Canvas** art design. This is NOT a WebView: `NativeDeployedMapArt.java` paints the entire world terrain and `NativeKingdomAtlas.java` paints the interactive atlas and ten-location region maps.
- The old `LivingBattlefield` environment composition is no longer used for battle/map visuals. Its previous cliff castle/lake parallax no longer covers the deployed map. The old textured `MotionArt` ground and legacy backdrop style are not used by the live gameplay renderer.
- Ten region palettes with source-inspired tree/rock/crystal/foliage/stele families, three distinct mission A/B/C scene variations, real curved pale enemy road, metallic turret build pads, rank-changing wood/stone fortress, gameplay-compatible right-side enemy spawning. The road is still exactly `LaneRules.stagePathY()`, so the visual path and enemy physics cannot disagree.
- Kingdom atlas displays a geographic-looking island, river and linked 10 kingdoms; Region maps render forest/rocks/structures with 10 connected selectable locations, lock/clear state, and selected node glow. The existing hit coordinates, unlock rules, stage persistence and all 300 missions are untouched.
- Full mission previews and stage A/B/C map thumbnails also show the **new deployed map**; not old generic cliff scenery.
- Only one 1900×1050 RGB_565 background is resident, cached by stage and regenerated on mission changes; no browser, external JavaScript or remote image dependency. Small thumbnails have nine-entry LRU.
- **Important limitation:** the HTML demo proposes 5–9 standalone build pads, but the actual current defense economy has **four working turret slots**. Four linked HTML-style pads are rendered intentionally rather than displaying fake pads without controls; an expanded tower economy would need a separate gameplay change. The artwork uses native generated Canvas shapes, not imported hand-drawn 300-stage paintings. Physical Android FPS/playtests remain necessary.
- Tests include genuine native gameplay drawing, deterministic 300-stage seeds, ten distinct palettes, map-vs-region output, cache reuse and interactive touch coordinates.

## v0.20.0 — full native Java scenic motion (NO HTML WebView)\n\n- The uploaded **RiftGuard World and Environment.html** is used as the design and motion reference. The game platform is still **native Android Java / Canvas**, with offline drawing inside the actual battle scene. The file is *not* launched in a browser or embedded as WebView.\n- The v0.18/0.19 port already included five cached background layers, ten palettes, 30 named landscape districts, 300 distinct stage seeds, parallax, river/cliff geometry and mission previews.\n- `HtmlSceneryMotion.java` now adds the missing **seven independently drifting multilobe cloud masses, six subtle light shafts from the seeded sun/moon, animated cliff waterfall/lavafall/rift-water highlights**, and grounded foam directly into live native rendering.\n- Scene decorations cannot intercept joystick/buttons, alter enemies or overwrite the ground lane. **Reduced Motion** stops live cloud drift, light flicker and water scrolling while retaining a complete still scene. No extra textures, remote assets, `WebView`, heavy animation libraries, or per-frame bitmaps.\n- Automated native Android Robolectric tests sample actual pixels and assert moving vs still rendering, four corresponding HTML reference regions (Verdant / Ember / Frostpeak / Rift), full stage index range and battle-HUD exclusion.\n- **Still pending:** manually painted HD sprite sheets and higher detail scenery, full real-device FPS review and art-direction matching to the user's screenshot. This update improves faithful HTML-inspired motion within the existing painterly renderer rather than claiming screenshot-perfect 300 handpainted maps.\n\n## v0.19.0 — more than a palette swap

The user's uploaded `RiftGuard World and Environment.html` provided four animated examples (Verdant Vale, Ember Highlands, Frostpeak Range and Rift Chasm): independently shifting sky, mountains, castle cliffs, mist, pines, lake reflections and foreground atmosphere. v0.18 ported that composition into native Android Canvas without embedding a WebView; v0.19 expands the result:

- **30 distinct named landscape districts** across the existing ten realms: three evolving subregions per realm. Each district uses real extra silhouettes, not merely another flat background color.
- Forest groves/ancient spires, bridge ruins/aqueducts, and biome-specific ritual gateways are built as cached landscape geometry. Every world has a distinct color, vegetation/rock treatment, architectural accents, and weather.
- **300 seeded compositions remain supported**, across 100 campaign levels × A/B/C map variants. Every map has real images in the selectable Mission Atlas, now labeled with its district name.
- Districts are rendered BEHIND the playable keep/enemies and above the textured world in the 8-layer depth composition. The mission road, physics, enemy AI, collision, and HUD are unchanged.
- Stage changes rebuild the five cached bitmap layers. Live animation only pans cached scenery with subtle foreground FX for performance and supports the existing reduced-motion preference.
- Automated pixel tests verify **all 30** district silhouettes, map/index boundaries, 300-to-30 mapping, cache reuse, and no drawing over the live battle lane. Screenshot gallery includes all 30 map preview renderings.
- **Scope disclaimer:** these are 30 distinct procedural district motifs in ten biome families with stage-specific seeded configurations, not 300 hand-painted high-resolution backgrounds or a new character atlas.

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

## V0.13.0 — actual animation and visual improvements (Phase 1)

- **Preserved original detailed actor artwork:** The first replacement prototype looked flat and the attempted hybrid had visible pasted rectangles in CI screenshots. Both were rejected during QA rather than shipping a visual downgrade.
- **Improved painted hero motion:** The existing sidearm-masked upper torso now has additional breathing weight, combat anticipation and cape follow-through, while existing planted feet, independently hinged leg crops and dynamically attached weapon arm remain intact. All six weapons still use the real WeaponRig with matching shot origins; no new fixed weapon introduced.
- **Enemy hit reactions:** Real projectile and melee damage triggers a timed, physical recoil/rotation on the painted enemy actor, not just a disconnected impact ring.
- **Ten world atmospheres:** `BiomeAtmosphere` adds layered background lighting, fog and front-facing snow, rain, ash, fireflies, crystal dust and shadow particles in world coordinates. Effects remain behind the HUD and do not affect combat collision.
- **Verified with native pixels:** Robolectric visual tests compare actual idle/run/attack/cast/hurt frames, independent weapon attachment, all 10 biome signatures and motion, and live enemy impacts. CI screenshots include real weaponless-body preview and weather compositions.
- **Not completed yet:** these are meaningful animation and environmental improvements but do not provide a completely new HD multi-directional hero/enemy sprite library or photo-realistic landscape overhaul. The exact “AI animation skills” isn't available here; Higgsfield has no available generation credits. Physical phone playtest still required. See [v0.13 Visual QA notes](docs/V013_WEAPONFREE_ANIMATION_VISUAL_QA.md).

## V0.14.0 — six weapon-specific animated attacks and Mastery trees

- **Every weapon has its own persistent tree:** Home → ARMORY → **WEAPON UPGRADE TREES**. Six selectable weapons, 11 tiered nodes each (66 total), bottom Origin → three branching Power/Technique/Special paths → summit mastery. Tap, swipe upward, buy with Supplies, and revisit to see animated illuminated connections and real status. Purchases persist across reinstalls only if the app data is retained; ordinary APK update preserves progress.
- **Not a decorative tree:** Weapon-specific damage multipliers, reduced attack cooldown, increased attack range, and signature masteries are calculated directly in real gameplay. Each weapon maintains independently saved progression, and fortress rank/branch prerequisites apply. Summit requires all three tier-3 branches; one route cannot bypass the others.
- **Unique combat animations:** Crossbow shoulder recoil, weighty greatsword windup, rapid Ember Sabre slash, raised/channelling Arc Staff, repeated Siege Repeater kickback, and linear Frost Lance thrust. Player's original painted torso uses weapon-specific anticipation; large weapons include a moving support hand. Actual muzzle/weapon-tip positions remain synchronized with projectiles and slash trails.
- **Mastery signatures:** Crossbow Phantom Volley extra hit; Greatsword Titan Splitter stagger; Ember Sabre Phoenix Drive burning; Arc Staff Tempest Crown extra lightning chains; Siege Repeater War Drum Barrage splash suppression; Frost Lance Absolute Zero extra freeze/shatter damage.
- **Regression checks:** six trees/66-node graph, purchase/save/gates, real shot damage and cooldown, UI navigation/swipes, six actual animated painted hero rigs, and rendered Android gallery of weapon attacks plus mastery tree screens.

**Not yet complete:** The single illustrative generated design image is NOT an in-game sprite sheet and is not bundled as completed character animation. Existing character art is still painterly cutout animation and weapon silhouettes are Canvas drawings; professional frame-by-frame illustrated HD art, mobile GPU testing and balance playtests remain required.

## V0.15.0 — 2D painted animation and world-depth quality pass

- **Actual production animation work:** `AnimationBlend` interpolates adjacent painted hero/monster pose frames only near transitions, avoids long ghosting, and never wraps the end of an attack into an incorrect first-frame windup. The Warden's six independently timed weapon animations and wrist-attached VFX continue using the real equipped weapon.
- **Living enemy motion:** seven painted enemy groups use sub-frame pose transitions, breathing/body displacement, and anticipatory lunges while retaining their original armor textures. Flying fiend wings remain separately animated.
- **Battlefield depth:** `BattlefieldLighting` adds grounded keep light, biome-specific animated illumination and physically attached left/right foot shadows, below the HUD and consistent with existing depth sorting.
- **Weapon/body visual QA fixes:** Android screenshots exposed a pinwheel-looking Crossbow and overly simple arm lines. Rebuilt the Crossbow/Repeater as compact recurved limbs, taut bowstring, loaded bolt and small feed mechanism. The Warden now uses contoured painted-metal texture fragments on dynamically hinged upper/forearms. All six weapon attacks shift the chest, shoulder, elbow and glove in concert with the actual targeted weapon, while muzzle effects remain attached.
- **Visual QA:** real Android Robolectric screenshots include eight sequential Warden pose timestamps and seven moving/attacking monsters; pixel tests measure transitions and lighting. No generated advertising screenshot is counted as a game asset.
- **Honest art limitation:** the user-named 'AI Animations' skill was not available in the installed skill catalog; Higgsfield generation credits were zero. A generated image was a marketing mockup, not a usable transparent sprite sheet, so it was rejected and was NOT bundled into the APK. Thus v0.15.0 is a real native-renderer animation improvement but not a new high-resolution frame-by-frame AI atlas or complete realistic world overhaul. See [v0.15 QA](docs/V015_VISUAL_ANIMATION_QA.md).

## V0.16.0 — actual combat animation-event feedback

- New `CombatAnimationFeel` system detects actual weapon contact with enemies (not merely a button press). Melee and ranged hits have different short animation hold times; the damage simulation and touch input **never freeze**.
- Camera shake is limited to the gameplay WORLD. UI and controls stay steady. Screen motion settles automatically within 190 ms, with tightly capped amplitude.
- Real contact points briefly display directional spark/slash streaks where the weapon hits the enemy, avoiding the previous disconnected ring-only illusion.
- A saved `reduced_motion` preference suppresses shake and animation hit-pause for accessibility while preserving a brief impact signal.
- Robolectric tests cover real projectile collisions, timed pause and decay, screen shake, reduced motion, and visible pixel effects.
- Implemented using the **game animation, game-feel and 2D Canvas principles** from the user's AI Animation Skills reference index. The full referenced `references/*.md` documents were not accessible here; no claims that an AI asset model generated a new HD sprite sheet. Fully illustrated HD player/enemy art remains outstanding.

## V0.17.0 — actual game-animation skill development, not an AI concept image

The user's uploaded **AI Animation Skills** `SKILL.md` was read directly; its complete game animation, game feel, sprite/skeletal 2D, scenery and performance references are present within that one file. RiftGuard is a **custom native Android Java Canvas 2D game**, not Unity/Godot/Roblox, so the corresponding concepts are implemented with Java and the real existing renderer.

- `WardenAnimationMachine`: real-time animation state machine with **DEATH > HURT > CAST > ATTACK > RUN > IDLE**, independent upper-body attack/cast clips and locomotion footstep cadence. Interrupted swings are canceled rather than creating invisible hits.
- **Unique weapon-contact frame events:** all six weapons have separate normalized contact timing. Damage now occurs only when the animated melee strike passes its contact frame, exactly once even on dropped frames. No early invisible melee damage.
- **120ms attack input buffer** gives short taps a chance to fire after a brief cooldown, while sustained touch still uses automatic shooting. Cast interrupts pending melee; enemy damage animates Hurt before normal locomotion resumes.
- **Hurt and defeat presentation:** hero plays short nonlooping hurt and a physical falling/degrading Warden pose when killed (tilted existing painted source, not fake new death-frame art).
- **Grounded footstep feedback:** alternating gait phase emits subtle dirt at actual planted feet with a strict particle cap; logic is independent of upper-body action clips.
- **Visual QA:** Robolectric captures the REAL painted atlas in all 7 state storyboard examples. Integration tests cover frame-rate independence, attack/cast interruption priority, exact melee HP impact and death pose changes.
- **New HD artwork quality gate:** `tools/verify_warden_hd_atlas.py` accepts a genuinely painted 8-column × 6-row RGBA source with 256px frames, 2px gutter and fixed feet pivot, and exports a validated transparent WebP plus six GIF previews. It rejects empty/duplicate idle/run frames, cropped art and opaque screenshot backgrounds. No such NEW HD source has passed the gate yet, so the new importer does not replace the in-game atlas.
- **Important remaining asset gap:** `riftguard_hero_motion.webp` is still the original 4-column × 4-row painted atlas. Generated imagery for this development turn returned a **marketing presentation**, NOT a usable 8-frame transparent Warden/monster sprite atlas, and was rejected. This update improves live animation behavior; it is NOT a claim that professional HD sprite art or the full 300-stage environment overhaul has been created.

## V0.18.0 — the uploaded HTML environment is now native Android scenery

The user supplied `RiftGuard World and Environment.html`, showing layered Verdant Vale, Ember Highlands, Frostpeak Range, and Rift Chasm scenes. Its 8-depth design is adapted into the real **offline Android Java Canvas renderer** (not a WebView or static game menu screenshot).

- New `LivingBattlefield`: sky / far mountains / near mountains / castle cliff / atmospheric mist / species-specific pines & ruins / reflective water / weather-touched foreground.
- **10 region palettes:** Greenhaven, Whispering Woods, Venom Marsh, Ember Canyon, Frostpeak, Ironfall, Crystal Depths, Stormbound Isles, Shadowfall and Eclipse Citadel. First four HTML themes are visually referenced; the six additional world treatments extend the palette with willow swamps, iron smokestacks, crystals, twilight forests and storm ruins.
- **All 300 missions:** Each stage receives a repeatable unique environment seed, one of three map archetypes (Scout Outpost, Broken Pass, Last Stand) and its stage-local weather/time-of-day beat. Ten distinct light/color grades include dawn, golden hour, dusk, twilight, night, storm rain and rift surge. This means 300 distinct **procedural compositions**, NOT 300 individually hand-painted HD pictures.
- **Gameplay integration:** the new native mountain/castle/water images are rendered behind the Warden, enemies, real fortress, UI and navigable route. The mission briefing now uses the exact corresponding native scene; **all three kingdom-atlas map cards now show small, stage-specific scenic previews** rather than unrelated generic card pictures. A separate nine-thumbnail LRU cap avoids map browsing lag.
- **Native parallax:** near scenery moves up to 5x faster than distant mountains relative to the moving hero, with fog and reflected ripples animating independently. Parallax is disabled by the existing reduced-motion setting.
- **Memory:** five downsampled bitmap layers are generated once per stage, reused each frame and released on stage change; no 300 full-size backgrounds loaded at once. All battle physics, touch controls and collision stay unchanged.
- **QA:** Robotlectric screenshot gallery adds **10 actual in-game biome screenshots + 3 map-type screenshots + final mission preview**. Tests cover all 300 stage keys, palette/variant pixels, scrolling, accessible reduced motion, deterministic caching and combat HUD safety.
- **Remaining limitation:** native Canvas illustrated/vector layers are derived from the provided HTML style; the source HTML was NOT directly embedded. These are not custom HD pixel-painted 300 maps. Detailed hand-painted assets and physical Android GPU profiling remain future milestones.

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
