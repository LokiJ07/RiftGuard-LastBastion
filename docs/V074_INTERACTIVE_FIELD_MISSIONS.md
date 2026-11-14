# RiftGuard v0.74 — Hero-Driven Field Missions

Stacked draft on v0.73 PR #33. No merge to main.

## Gameplay delivered
- 96 event slots remain fixed in the original 300-stage campaign: the first
  15 tutorial set pieces remain unchanged. Nine later realms retain nine
  event stages each, now mixing five real objective TYPES, not just three.
- 9 **Rescue the Stranded** missions (stage indices realm*30+15 for realms
  2–10). Three physically rendered survivors appear within Warden movement
  bounds. Walk inside a survivor's visible circle to rescue them.
  Enemies divert from the regular road to attack nearby unrescued NPCs.
  You must save at least 2 of 3; losing two ends the stage. Clearing every
  enemy does NOT advance a mission before survivor fates are resolved.
  Rescue all three to receive a modest extra BATTLE GOLD bonus.
- 9 **Seal the Rifts** missions (stage indices realm*30+24). Two original
  glowing world portals each require 2.1 seconds of proximity channeling.
  Pending portals periodically deal bounded CORE HP damage until sealed.
  Clearing the enemy queue does not skip the portals. Both sealed awards
  battle gold once and ends ongoing portal damage.
- First three existing event types (convoy escort, interruptible siege
  cannon, split-route attack) are preserved; there are no additional enemy
  spawns and no change to permanent save or resource schemas.
- Mission-specific briefings, named atlas badges, live HUD progress, and
  world-space object animations use real hero/AI state.
- Existing cold recovery and wave retry rewind the event to the
  deterministic wave-two checkpoint; in-progress survivors/seals and
  uncommitted reward gold do not persist to the previous wave boundary.

## QA
- Pure 300-stage/10-realm counts, enemy caps and all objective coordinates
  bounded by real Warden movement, non-fatal per-hit difficulty scaling,
  reserve/hero channel tests, fail/clear rules.
- Robolectric gameplay tests: actual rescue and reward once, enemies
  target villagers and can cause defeat, actual seal progress and keep
  draining, cold recovery resets unfinished missions.
- Android builds, Android 10 and 16 emulators, screenshot + fps metrics.
  Real-device late-game and human player testing still required.

## Limitations
- This is two **new mechanically distinct mission types**, not 18 unique
  hand-authored NPC scripts and not a 300-stage redesign.
- Objective art is original procedural Canvas artwork, but does not
  replace the detailed preexisting biome environment, weapons or NPC art.
- Still an alpha with manual Play signing, asset license audit and
  production certification pending.
