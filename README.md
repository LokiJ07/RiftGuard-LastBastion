# RIFTGUARD: Last Bastion

Mobile-first, fully offline Android fortress-defense action game.

## Current version: V0.2 — illustrated campaign and progression
- Playable 100-level × 3-stage campaign structure with deterministic battleground layouts.
- **New illustrated realm atlas**: visible painted-style vector landscape portraits for 10 biome regions and distinct map cards A/B/C. These are actual game-rendered illustrations, not the numeric-only UI.
- **New permanent fortress upgrade**: five ranks, supplies earned on mission wins, castle appearance/core HP/starting towers progress, saved after reopening the app.
- **New Armory**: six visible weapon designs with unlock rules, equip selection, persistent loadout and distinct attack stats/effects (crossbow, greatsword, fire sabre, arc staff, siege repeater, frost lance).
- Upgradable turrets, touch joystick, hold-to-attack, Arc Pulse, monster classes, win/lose, pause/resume, responsive HUD.
- Android CI builds, JVM rules tests and Robolectric screen/gameplay tests, debug APK uploaded as Actions artifact.

## Important visual scope
The current scenes use **original procedurally drawn vector illustrations**, which are more detailed than prior placeholder number/map panels. The game does **not** yet include hand-painted high-resolution sprite atlases, animated skeleton rigs, 1,000+ imported art assets, or 300 artist-authored unique map scenes. Those are planned V3–V5 improvements. See [production pack](docs/CONTENT_INTEGRATION_V1.md).

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
