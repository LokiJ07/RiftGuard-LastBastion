# RiftGuard v0.82 — Combat Engagement and Balance Pass

## Intent
Make the existing 300-stage campaign less repetitive without undoing the player's smooth v0.81 phone experience, removing illustrated scenery, reducing responsiveness, or increasing the number of enemies. This is a **contained alpha combat pass**, not a claim of a complete long-term retention rebalance.

## Gameplay changes

**1. Authored enemy wave identities (from stage 31, wave 2 onward)**

Each later wave is assigned a rotating order: **Shield Vanguard**, **Swift Raiders**, **Hex Support**, or **Boar Stampede**. Each fifth ordinary enemy slot (max 1 in 5) uses the existing corresponding heavy/raider/shaman/boar enemy rig, movement and attack behavior. It **replaces** an ordinary enemy — it does not add population, change HP coefficients, spawn more bosses or alter wave limits.

The first 30 tutorial stages and the first wave of later battles retain their old composition. Realm Act pre-existing scripted signature units, mission-specific siege engines, flanking units, actual bosses and Cave boss actors take precedence and never get overwritten. The order is announced at wave start and briefly surfaced during the wave so players can adapt turret and Warden tactics.

**2. Warden Flow: skill-earned combat burst**

Perform *two different actual player techniques* inside a 9-second window: a **landed 3-hit combo finisher**, a **landed charged strike**, or a **dodge made near an actual telegraphed enemy attack**. A real contact/event is necessary: tower autoplay, empty shots, repeated charged strikes and dodging in empty space do not earn Flow. On activation:

- For **7 seconds**, the Warden's **weapon-only damage** is increased by **9%**, with brief green effects and a visible short HUD timer. Fortress turrets, enemy health and abilities are untouched.
- The first Flow activation in a wave earns **14–26 battle gold** (depending on realm). Any further triggers in the *same wave* grant no more gold; an **18-second cooldown** separates activations.
- This is **combat currency only**, not permanent supplies, Warden XP, an automatic character attribute or a saved progression value. The original atomic victory transaction and checkpoint schema are unchanged; restarting or recovering a wave resets its uncommitted Flow state.

**3. Less dead time between waves**

Preparation windows (Easy/Normal/Hard/Expert) have changed from **25/20/15/10** to **22/17/13/10 seconds**. Boss preparation changed from **50/40/30/25** to **44/34/28/23 seconds**. The existing manual early-start option, capped early-start gold reward and separate **3.1-second final boss entrance telegraph** remain. Spawn interval, concurrent caps, enemy population and existing health scaling remain **unchanged**.

## Verification gates

- `EncounterRhythmV082Test`: exact tutorial compatibility, deterministic 4-order variety, no added unit slots, scripted/elite exemption and wave pacing budgets.
- `CombatFlowV082Test`: two distinct actions required, anti-spam, expiry, cooldown, one gold award per wave, reset/retry and invalid-input behavior.
- `EncounterFlowV082IntegrationTest`: actual Java Canvas game spawns a real shaman in a later ordinary signature slot; Wave Flow increases real in-battle gold but cannot write permanent supplies, and resets on a new stage.
- Existing Android tests and emulator visual/controls diagnostics must pass on **the exact PR head** before merging.

## Deferred, not silently claimed done

Individual boss family move set expansion, complete 300-stage balance/retention playthrough, optional meta-reward loops, audio-directed threat cues, visual art overhaul, performance benchmarks on budget phones and protected Play Store release are separate work requiring full acceptance tests. No game logic is switched to high-frequency allocation or a new physics timestep.
