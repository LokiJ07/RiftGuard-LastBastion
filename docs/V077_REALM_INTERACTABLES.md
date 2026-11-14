# RiftGuard v0.77.0-alpha1 — Physical Realm Interactables

Development stacked draft based on v0.76 PR #36, not merged to main.

## What has changed in the actual game

The ten realm backdrops remain artistically intact and keep the same native
Canvas terrain, real winding enemy lanes, built turret pads, destructible
scenery and 300-stage progression. We add THREE hand-modeled **interactive
world structures** with 10 existing realm-color identities (one color per
realm). There are exactly 60 **scheduled later-campaign stages**, distributed
across all nine post-tutorial realms: 24 Wardstone, 18 Forge, 18 Purifier.
These occupy optional battles and never overwrite the 96 existing special
mission events or the two defensive relic trials per realm.

Each active structure is placed deterministically within the Warden's
REAL navigation bounds after scoring clearances against built pad positions,
living hazards and destructible terrain landmarks. An illustrated gold
circle is the same exact radius as the activation logic (81 world units).
Stand inside the ring for 1.65 seconds; moving away drains progress. Unlocks
in wave 2 and may be used ONCE PER WAVE after that, not an automatic bonus.
Activation, power durations and progress visibly appear inside the world,
with an actionable HUD note (not a new button). Campaign stage thumbnails
identify which stage hosts a Ward, Forge or Purifier.

- **Bastion Wardstone (24 stages)**: 18 seconds of 28% lower actual damage to
  the fortress core, including ordinary rush/legacy elite/boss impacts. It
  does not reduce enemy damage to the hero or make the hero invulnerable.
- **Field Forge (18 stages)**: physically repair the most-damaged still-built
  turret by up to 22% of that tower's full HP; never rebuild a destroyed pad.
  Existing turret projectiles have 14% higher damage for 15 seconds.
  If all guns are gone, provide a limited 7% core repair instead. No
  permanent purchase, rank, tier, or equipment schema is changed.
- **Storm Purifier (18 stages)**: 18 seconds of genuinely inactive hazards;
  real hazard simulation stops damaging/slow-applying, and the world-drawn
  hazard rings visibly cool down. Existing weather/background remains.
  Existing team boosts and boss mechanics still function.

## Balance, durability and verification

- One activation per wave, not per frame. Both paid fortress build/repair
  choices and enemy population caps remain unchanged. No permanent saves,
  offline scripts, HTTP traffic or new asset downloads.
- Wave retries / cold recovery reset the available site to the saved wave
  boundary, exactly like existing convoy/portal mission events.
- Pure 300-stage scan and all-60-site real reachability/clearance checks.
  Robolectric runs the actual game update loop to activate each site and
  verify live enemy core damage, damaged turret HP, and hazard suppression.
- GitHub Android CI, Android 10 and 16 emulator input/graphics tests
  must pass on this exact release candidate commit.
- Not a full 300-handmade-map rewrite; there are 60 curated *scheduled*
  optional structure encounters and three distinct physical effects. Human
  late-realm playtesting and physical-phone thermal/FPS QA still required.
