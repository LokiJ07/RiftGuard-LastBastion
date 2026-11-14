# RiftGuard v0.76 alpha1 — Combat Readability and Fair Late-Realm Hits

Stacked draft on v0.75 PR #35. No merge to main and NO commercial release.

## Actual changes

1. **Live actionable battle guidance.** The left-side tactical card (world
   independent HUD, outside the default mobile controls) tells the Warden
   HOW to respond to active events. Caravan: approach to shield / critical HP.
   Siege Engine: real remaining cannon charge seconds or how to find it.
   Rescue: rescue requirement and emergency if a villager died.
   Rift Seals: actual progress remaining when standing at a portal, otherwise
   the next unsealed pulse countdown.
   Split lanes: protect both routes. Non-mission SIEGE raids: exact marked
   turret and repair/draw-aggro instructions. Healer fallback/pincer attacks:
   actionable and brief instructions. A bounded card replaces stacked alerts.
   The panel does not replace original character art, environment or weapons.

2. **Late-campaign per-impact fairness** (stages 31–300 ONLY):
   - Individual normal enemy hero contact, boar impact, shaman bolt,
     stage hazard and non-cave elite blast limited to **29% of full hero HP**.
   - Ordinary tower impact and non-cave legacy elite tower blast limited to
     **24% of full tower max HP**.
   - Ordinary core strike and non-cave legacy elite siege strike limited to
     **17% of max fortress HP**.
   - Non-boss Memory Trial relic hits limited to **20% of max relic HP**.
   These budgets apply AFTER existing difficulty, role, commander and
   elite multipliers; repeated hits still accumulate, and higher modes still
   increase enemy HP, wave pressure, speed, positioning and frequency.
   The previously tested 10 world-boss charged strike budgets from v0.73
   remain authoritative and unchanged (34/25/43%). Stage 1–30 tutorial and
   optional Eclipse Cave boss battle remain mechanically unchanged.

3. **Immediate shaman interrupt.** Destruction of the nearby actual living
   shield cancels an ongoing healer retreat in the SAME next game tick; the
   shaman stops casting while actively retreating. Player skillful targeting
   now gives direct counterplay to the shielded fallback mechanic.

4. **No extra waves, spawns, art removal, save-data fields, or new controls.**
   Same 300 stage IDs, 96 active objective stages and fortress appearance.

## Validation
- New full 300-stage × four difficulty math matrix (ordinary, elite,
  environment, shaman, turret, relic), first-realm unchanged damage,
  five mission-state hints, real turret / hero contact damage in a late
  Expert battle, and live healer cancellation.
- Existing 400+ gameplay/unit tests, debug APK + unsigned candidate AAB,
  Android CI + Android 10/16 emulator input/graphics diagnostics.
- Physical-device sustained FPS/thermal and human late-stage balance
  remain mandatory prior to merging/publishing.
