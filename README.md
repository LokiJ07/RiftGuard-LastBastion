# RIFTGUARD: Last Bastion

Mobile-first, fully offline Android fortress-defense action game.

## Current version: V0.6.2 — difficulty, 10 world-boss finales, four defense families and stage identities
- Playable 100-level × 3-stage campaign structure with deterministic battleground layouts.
- **Real bundled art**: first original AI-generated paintings are packed into a 1024×768 WebP texture atlas under `res/drawable-nodpi`, decoded offline and drawn in Home, Campaign, Workshop and stage thumbnails.
- **Production environment layer**: 60 independently rendered transparent prop sprites (10 regions × 6 types), world-space depth sorting, near-hero occlusion fading, gentle vegetation motion and closer gameplay camera. No fixed screenshot overlays in the battlefield.\n- **State-driven animation**: original artwork reworked into 4×4 Warden pose frames (idle/run/attack/cast); 7 enemy groups with 4 locomotion and 4 attack frames each. Frames change based on actual movement and combat states, not static image bobbing.
- **Separate raster weapon attachment**: six generated-asset-derived weapon images anchored at the hero's hand, with frame-synchronized swinging, spell glows, projectile muzzle effects and slash trails. No permanently attached second weapon.
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

## Playing
1. Home → CAMPAIGN → choose a region, stage A/B/C (only unlocked stages are playable).
2. Move using left thumb, hold ATTACK to use equipped weapon, use PULSE and BUILD to defend.
3. Win to earn permanent supplies. Home → FORTRESS → upgrade rank to unlock a new weapon.
4. Home → ARMORY → tap an unlocked weapon to equip it; return to campaign and see it in hero's hand.

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
