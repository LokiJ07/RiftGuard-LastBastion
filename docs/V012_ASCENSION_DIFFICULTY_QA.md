# RiftGuard v0.12.0 — Ascension Tree and Difficulty Rebalance

## Goal from player screenshot
A vertically explorable branching upgrade tree: **start at the bottom, swipe up towards the summit**. The image provided is a reference for progression mechanics and node readability, not assets to copy. Implement everything as genuine Android Canvas controls connected to live gameplay, rather than a static background screenshot.

## New playable screen
- Home → Fortress → **ASCENSION / UPGRADE TREE**.
- 40 named nodes across **10 rows**; one free Origin root, then branching prerequisites. Each node has a color-coded original symbolic icon and glowing connection path; purchased links pulse with travelling light.
- Drag upward to climb towards the summit, downward to return to the Origin. **GO UP** bottom button is an alternative.
- Tap a node to see its exact parent, fortress-rank requirement, effect and supply cost in the sidebar. BUY/UNLOCK spends supplies and is permanent. A failed purchase never deducts resources.
- The save keys use the existing app SharedPreferences; stable flags persist across app restart. Offline at all times.
- Every acquired upgrade directly affects the real combat system: weapon damage, turret damage, hero/core HP, starting gold, spell cooldown and movement speed. Prerequisite/fortress rank/currency validation happens in code.
- Existing 5 fortress ranks and 6 weapons remain usable; upgrading the fortress is required to reach higher branches.

## Difficulty design — baseline level 1

| Property | Easy | Normal | Hard | Expert |
|---|---:|---:|---:|---:|
| Enemy HP multiplier | 0.58 | 1.00 | 2.10 | 3.85 |
| Enemy damage multiplier | 0.46 | 1.00 | 1.78 | 2.95 |
| Enemy movement speed | 0.84 | 1.00 | 1.20 | 1.40 |
| Spawn-time multiplier | 1.36 | 1.00 | 0.72 | 0.46 |
| First-stage waves | 3 | 4 | 6 | 9 |
| First-wave enemy count | 4 | 6 | 11 | 17 |
| Starting gold (rank 1) | 250 | 150 | 115 | 70 |
| Starting towers (rank 1) | 1 | 1 | 1 | 0 |
| Hero max HP multiplier | 1.30 | 1.00 | 0.91 | 0.78 |
| Elite reinforcements | Story finale | Story finale | Every third wave | Every second wave |
| Supply reward premium | 0% | 0% | +43% | +118% |

Expert is **not** an easy mode with a label: it starts with fewer defenses and resources, nearly quadruple enemy health, nearly triple damage, more waves, much faster reinforcements and recurring elites even in world 1. Players are expected to progress their fortress and ascend upgrades before surviving later Expert stages. No guarantee of perfect balance until real-device player trials.

## AI animation skill example / integration note
Example production prompt: "Animate the RiftGuard fortress ascension tech tree for landscape Android play: forty original fantasy upgrade medallions in ten vertical tiers, starting at a bright bottom Origin and culminating in a top Last Bastion crown. A purchased node illuminates its ancestral branch, with subtle moving energy particles. Keep gestures readable at 1280×720 and avoid copying the reference game art."

The exact newly-mentioned "AI animation skills" is not exposed in the available installed-skill catalog for this session. The **working example is implemented directly in game code**, via smooth Canvas pulse and travel animation tied to actual owned nodes, rather than pretending an external skill created or exported project assets. Higgsfield offers a separate 3D animation action catalog but this Android game is 2D and no unverified 3D animation files were added.

## Regression acceptance
- Graph contains 40 unique IDs with parent exactly one row below; every climb can be reached through purchases and rank gates.
- UI can be navigated from Fortress; swipe up reveals higher tiers; nodes have real touch hitboxes and purchase/save effects. Existing fortress/armory taps must keep working.
- Same stage/difficulty comparison has monotonic health, damage, speed, waves, density, spawn pressure; Expert >30× Normal aggregate pressure metric at level 1.
- CI builds Android debug APK and runs Robolectric screen/UI tests; screenshot gallery includes Origin and Summit screens.
- Still incomplete: high-resolution all-new sprite sheets, expert per-device performance testing, exact balance against human players and customized animations beyond the native effect system.
