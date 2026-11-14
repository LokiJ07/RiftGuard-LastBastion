# RiftGuard v0.73 — Campaign Encounter Director + Difficulty Fairness

Base: v0.72 draft PR #32. Isolated PR and NO merge to main.

## Actual playable changes
- 96 wave-two active objective stages across 10 realms: original first 15
  unchanged stage slots, plus 81 later stages (9 per realm; exactly 3 in each
  realm act). All three mission types occur equally per later realm, and the
  remaining 204 stages are freeform / elite / boss / hazard battles. Boss
  finales are unchanged and no spawn counts are increased.
- Caravan escort: moving convoy now rallies when Warden is nearby; visible
  GUARD aura, 39% less incoming damage and speed bonus; late-realm escort HP
  scales moderately with world/difficulty instead of impossible one-hit loss.
- Siege cannon: a 9.5–16% max-HP wound DURING the telegraphed windup now
  interrupts the actual cannon hit, with visible feedback and delayed retry.
  Later Hard/Expert siege engine multiplier capped at 1.35 rather than 1.65.
  Siege strikes have core-HP budgets and warnings.
- Three mission rewards now increase modestly by realm in **battle gold**,
  not duplicate permanent supplies. Existing checkpoint recovery still
  resumes at wave boundary; no save schema changes.
- Ten real world bosses: single charged hero/core/tower strikes capped to
  34%/25%/43% of the corresponding FULL maximum HP. A player at low health
  can still die; higher difficulty still increases speed, multiple hits,
  reinforcements, hazard pressure and shortened intervals. Broken boss weapon
  and dodge remain functional.

## Automated validation
- 300 stages × 4 difficulty scans, act/event distribution, 96 exact mission
  positions and boss exclusion, wave population checks, pure difficulty cap
  checks, real Robolectric guarded convoy, cannon interruption, cold restore,
  previous v0.69 mission tests and v0.71 boss action tests.
- Android build, test, unsigned release bundle, Android 10 + Android 16
  emulator **real PLAY** input/graphics verification, screenshot check.

## Important remaining work
- This implements more event distribution, NOT 96 individually hand-authored
  maps. Still 3 event mechanics with biome-specific art, difficulty, hazards,
  signature enemy roles and ten unique boss moves.
- No human players or physical phone thermal/performance certification yet.
  Need blind player retention testing, longer/late-game simulations, art and
  source-rights audit and signed commercial Play release checks.
