# RiftGuard v0.68 Tactical Engagement Milestone

Development stacked on v0.67-alpha2, isolated from main. NOT a commercial release.

Shipped implementation:
- Signature enemy wave-two threat per stage from stage 5 onward. Existing boars,
  shamans and bruisers only; wave population and boss schedule are unchanged.
- Shaman casting now heals one wounded non-boss ally within 240 world units.
  Healing is capped (12% HP, 42 HP absolute) and never overheals.
- Purchased tier-2 SPECIAL mastery node 6 unlocks actual control or burning
  utility on combat hits for all six weapon families.
- Five optional field contracts from stage 4: cast two real skills, upgrade a
  tower, maintain fortress/hero health, or protect two towers without losses.
  Contracts grant one moderate bonus on first clear, never lock progression.
  Live progress appears on the HUD and first-clear rewards are transactional.
- Cast counters survive wave-boundary checkpoints and cold process recovery.
  Legacy schema-1 saves fall back to the existing recorded Pulse count.
- Art, 10 realms, 300 stages, enemy density, purchased fortress aesthetics,
  memory trials, and original storyline remain intact.

QA:
- Run exact-head Android CI unit tests, debug APK, unsigned AAB, resource tests.
- Run Android 10/16 emulator QA, check real PLAY and all five controls.
- Exercise stage 4, stage 8 shaman, boss 10-C, replay and cold-start recovery.
- Human tactile and difficulty evaluation is NOT certified by green CI.

Next commercial milestones: real alternate-route/escort/siege-engine mission
mechanics, curated realm acts and signature bosses, expanded functional upgrade
preview, manual 300-stage campaign QA, blind player retention playtests, phone
performance/thermal tests, licensed asset audit, signed Play release AAB and
store-policy/privacy paperwork. Do not declare production ready without these.
