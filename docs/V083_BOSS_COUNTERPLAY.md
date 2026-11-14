# RiftGuard v0.83 — Boss Counterplay and First-Clear Mastery

## Goal

A targeted, save-compatible combat engagement pass after the user's v0.81 phone performance/readability check and the v0.82 wave-composition work. Keep the existing realm art, animated bosses, difficulty curves, physical hitboxes, controls, enemy population, and durable victory/save transaction.

## Real boss counterplay

The ten existing **kingdom bosses** (their final level C maps) already have ten distinct ground telegraphs, boss parts and phase-specific attacks. v0.83 adds a real response to those marks:

1. When an existing world boss begins a visible charge, the Warden can **land weapon hits** to fill a charge-pressure threshold. The boss stays locked on the originally telegraphed target.
2. The required pressure scales gently with boss max HP, realm and phase, clamped to **48–174 actual weapon damage** so both starter and late-game weapons can achieve it. It resets each new charge.
3. Meeting the threshold **cancels that specific pending cast** before any hero/core/tower strike happens. The boss visibly flinches and is unable to move or attack for **2.15 seconds**, then resumes with at least **2.8 seconds** of recovery before another charge.
4. During stagger, *Warden weapon hits only* deal **+14% damage**; fortress tower autocombat, skills and passive burn damage get no added multiplier.
5. Boss HUD shows actionable counter instructions and counter-pressure percentage during the charge, then a timed `STAGGERED` message; real Warden hit VFX/audio confirm interruption. The existing 10 painted attack geometries remain authoritative.

No ordinary elites, Cave boss, or untelegraphed hits receive these mechanics. Tower attacks cannot fill the pressure gauge. It does not add a new health bar, boss spawn or permanent character stat.

## Reward balancing: optional first-clear boss mastery

If the player wins a kingdom boss level **for the first time**, having interrupted a boss at least once earns a small extra permanent supplies reward:

- One interrupt: **19–37 supplies** (scales by kingdom).
- Two or more interrupts: **38–74 supplies**, capped at two successful interrupts.
- Zero interrupts: no change to the existing stage reward.
- Replayed boss fights, non-boss stages and the story Cave: no mastery supplies reward, even if interrupted repeatedly.

The mastery reward is computed **inside the existing durable victory transaction**, together with the usual stage supplies and Warden XP. A failed victory write or defeat cannot pay mastery. There are **no new save keys or migration steps**; active stagger/pressure/counter count reset on fresh battle and safe wave retry, as other uncommitted combat state does.

## Tests and release gate

- `BossCounterplayV083Test`: all ten realm budgets, three phases, boundary conditions, invalid values and 0/1/2/100-interrupt reward caps across first-clear/replay.
- `BossCounterplayV083IntegrationTest`: real RiftGameView boss model, confirm two Warden hits cancel live telegraph and start stagger; a **real Warden projectile** can interrupt while a turret projectile cannot; Cave/ordinary elites excluded; first-clear mastery saved atomically, replay/defeat cannot farm. Captures `286-v083-boss-cast-counter-telegraph.png` and `287-v083-boss-stagger-confirmation.png` from Canvas.
- All original Robolectric and unit tests, Android debug APK/AAB builds, installed-emulator input/graphics QA and dedicated emulator combat/frame QA must pass for the **same branch commit** before merge.

## Deferred

A full 300-stage human playthrough, boss animation/audio variants, telemetry-driven difficulty tuning and Play Store production signing are not certified by unit/emulator CI. This feature is an **alpha gameplay improvement**, not public-release approval.
