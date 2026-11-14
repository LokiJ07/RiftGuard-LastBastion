# RIFTGUARD: Last Bastion

[![Android CI](https://github.com/LokiJ07/RiftGuard-LastBastion/actions/workflows/android.yml/badge.svg?branch=main)](https://github.com/LokiJ07/RiftGuard-LastBastion/actions/workflows/android.yml)

**RiftGuard** is a single-player, offline-first **Android Java / Canvas** action and fortress-defense game. Control a Warden, fight the Rift, build and upgrade the Last Bastion, and discover the histories of the Knight, the First Warden and their companions.

> **Status:** v0.79 systems were integrated through [PR #40](https://github.com/LokiJ07/RiftGuard-LastBastion/pull/40); the v0.80 line adds story-continuity, ending and mid-chapter pacing improvements. The Android package declares **`0.80.0-alpha1` / versionCode `76`**. This remains a development alpha, not a signed production release or Play Store publication.

## Play at a glance

- **300 campaign stages** spanning **10 kingdoms × 10 levels × 3 maps (A/B/C)**, including 10 kingdom bosses and the final Eclipse Cave after the campaign.
- **Action combat:** movement joystick, chained/charged attacks, directional dodge, equipable spells and abilities, six evolving weapon families, visual feedback and telegraphed boss attacks.
- **Fortress defense:** real buildable/upgradable turret sites, five fortress ranks, attached siege weapons and visually changing purchased weapon/fortress/ascension upgrades.
- **Worlds and story:** region journeys, procedural native illustrated scenery, animated Warden/enemies, weather/hazards, optional objectives, Memory Trials, 100 collectible memories and branching story outcomes. v0.80 adds skippable one-time camp dialogue every 3 levels, including for older saves replaying those maps. The 300 environments are *not* 300 individually painted HD scenes.
- **Character Level 1–100:** separate from the 100 campaign levels; experience scales with stage and difficulty, first clears pay more than replays, and each level awards growing attribute points (402 in total by Lv100).
- **Four Warden attributes:** Vitality, Power, Agility and Arcana; each capped at 120. Upgrades apply to actual combat without replacing fortress/weapon progression.
- **Six automatic Warden oaths:** Guardian Heart (Lv10), Rift Siphon (Lv25), Battle Focus (Lv40), Arc Conduit (Lv60), Vanguard Stride (Lv80) and Rift Sovereign (Lv100).
- **Save continuity:** offline progression, persistent fortress/equipment/attributes, old-save Warden XP migration, and a durable campaign victory transaction.

## How to play

1. From **Home → Campaign**, select an unlocked kingdom, a level and Map A/B/C; choose Easy, Normal, Hard or Expert.
2. Use the **joystick**, **Attack**, **Dodge**, **Skill** and **Build/Upgrade** buttons. HUD layout can be customized; buttons and touch hitboxes move together.
3. Win stages to earn **supplies** for fortress/weapon upgrades and **XP** for the Warden. Defeats grant no Warden XP.
4. Open **Home → Warden LV** to spend level-up points on attributes. Unlock milestone oaths automatically.
5. Visit **Fortress / Ascension / Armory** for lasting upgrades. Explore **Chronicle** to view recovered memories or replay earlier story.
6. Complete optional last-stand Memory Trials on Map C, use repeatable challenges in the **Kingdom Hub**, or replay earlier maps at harder difficulty.

### Story decisions and memory progression

RiftGuard has **100 collectible memory keys**, many earned through optional Map C challenges. In **v0.80**, the compassionate Eclipse Cave ending requires the completed campaign plus **30 of 33 Cael memories**, instead of forcing collection of all 100 memories. Other Knight memories remain collectible; the final fight, other endings and original save keys are preserved. The story edits keep **Chada's heirloom/shattering/reforging** chronology consistent, protect the **Titan → Cael reveal** until the cave's third phase, and make **Tessa's "Salt, not sugar"** setup/payoff part of the main chapter route.

The final Cave is still **locked until the full 300-stage campaign is completed**; reducing that gate, changing wave counts or skipping mandatory maps is **not** part of the story fix. See [v0.80 review notes](docs/V080_CRITIQUE_TRIAGE.md).

## Build and automated checks

**Requirements:** JDK 17; Android SDK API 36 / Build Tools 36.0.0; Gradle 8.11.1 (as used in CI); Android Gradle Plugin 8.7.3. Android minSdk 24, targetSdk 36.

```bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug :app:bundleRelease
```

For downloadable debug builds, open [GitHub Actions → Android CI](https://github.com/LokiJ07/RiftGuard-LastBastion/actions/workflows/android.yml), select a **successful** run, and obtain `RiftGuard-debug-apk` from **Artifacts**. The CI also publishes test reports and emulator diagnostics. Android 10 and 16 emulator checks are not a replacement for real-device FPS, thermal, battery and long-session playtesting.

**Release warning:** CI debug APKs use development signing. Standard release bundles from CI are unsigned. The manually dispatched [signed release candidate workflow](.github/workflows/release-candidate.yml) has separate protected prerequisites and does **not** publish to Google Play.

## Documentation

- [v0.80 critique triage and scope](docs/V080_CRITIQUE_TRIAGE.md)
- [Character Lv1–100 rules](docs/V078_WARDEN_LEVELS_AND_STATS.md) · [Warden milestone oaths](docs/V079_WARDEN_OATHS.md)
- [Commercial release / physical QA gates](docs/V072_COMMERCIAL_RELEASE_GATES.md)
- [Full archived milestone history (previous README)](docs/archive/README_THROUGH_V079.md)
- [Production plan](docs/PRODUCTION_PLAN.md) · [Asset/content plan](docs/CONTENT_INTEGRATION_V1.md)

**Limitations:** This remains development software; many scenes use procedural Canvas artwork. Real-device quality, art/licensing review, 300-stage playtesting, accessibility and a signed production build must be verified before public release. The optional downloadable art pack needs internet **only when requested**; core gameplay works offline.
